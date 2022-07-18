package com.feyl.registry.zookeeper.util;

import com.feyl.enums.RpcConfigEnum;
import com.feyl.utils.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Zookeeper客户端工具 Curator util
 *
 * 详细参见：
 * <a href="https://javaguide.cn/distributed-system/distributed-process-coordination/zookeeper/zookeeper-in-action.html">ZooKeeper 实战</a>
 *
 * @author Feyl
 */
@Slf4j
public class CuratorUtil {

    /**
     *  重试之间等待的时间
     */
    private static final int BASE_SLEEP_TIME = 1000;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRIES = 3;

    /**
     * zookeeper 注册远程服务的根地址
     */
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";

    /**
     * key：远程服务
     * value：远程服务对应的所有远程地址
     * 相当于客户端本地缓存的作用
     */
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();

    /**
     * 存储已经在 Zookeeper 中注册的服务
     */
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    /**
     * 用于存储与 zookeeper服务器建立连接的客户端
     */
    private static CuratorFramework zkClient;

    /**
     * Zookeeper 服务器的默认地址
     */
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    private CuratorUtil() {
    }

    /**
     * 创建持久结点
     * 一旦创建就一直存在即使 ZooKeeper 集群宕机，直到将其删除。
     *
     * @param path node path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                //eg: /my-rpc/com.feyl.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("Create persistent node for path [{}] fail", path);
        }
    }

    /**
     * 获取注册到 Zookeeper中的服务名对应的持久结点下的子结点（远程服务地址）
     *
     * @param rpcServiceName 远程服务名称 eg:com.feyl.HelloServicetest2version1
     * @return 服务名对应的持久结点的所有子结点（远程服务地址）
     */
    public static List<String> getChildNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(zkClient, rpcServiceName);
        } catch (Exception e) {
            log.error("Get child nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * 注册并监听特定服务节点的变化
     *
     * 详细参见：
     * <a href="https://www.cnblogs.com/crazymakercircle/p/10228385.html#autoid-h3-4-2-0">PathChildrenCache 子节点监听</a>
     *
     * @param rpcServiceName 远程服务名 eg:com.feyl.HelloServicetest2version
     */
    private static void registerWatcher(CuratorFramework zkClient, String rpcServiceName) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache cache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener listener = ((curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        });
        cache.getListenable().addListener(listener);
        cache.start();
    }

    /**
     * 清空特定远程服务地址在 Zookeeper 注册的服务
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream().parallel().forEach(path -> {
            try {
                if (path.endsWith(inetSocketAddress.toString())) {
                    zkClient.delete().forPath(path);
                }
            } catch (Exception e) {
                log.error("Clear registry for path [{}] fail", path);
            }
        });
        log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
    }

    /**
     *获取与 zookeeper服务端建立连接的 zookeeper 客户端
     *
     * @return
     */
    public static CuratorFramework getZookeeperClient() {
        // 检查用户是否设置了 zookeeper 的地址
        Properties properties = PropertiesFileUtil.read(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties != null && properties.getProperty(RpcConfigEnum.ZOOKEEPER_ADDRESS.getPropertyValue()) != null
                ? properties.getProperty(RpcConfigEnum.ZOOKEEPER_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;
        // 如果 zookeeper 客户端已经启动，则直接返回启动的客户端
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // 重试策略：重试3次，并且将增加重试之间的睡眠时间。
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // 要连接的服务器（可以是一个服务器列表）
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // 等待30s直到连接到zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to Zookeeper!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

}
