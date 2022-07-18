package com.feyl.registry.zookeeper;

import com.feyl.registry.ServiceRegistry;
import com.feyl.registry.zookeeper.util.CuratorUtil;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * 基于 zookeeper 的服务注册
 *  服务被注册进zookeeper的时候，将完整的服务名称rpcServiceName
 * （class name + group + version）作为根节点，子节点是对应的服务地址（ip + 端口号）
 *      - class name：服务接口名也就是类名比如: com.feyl.HelloService
 *      - group：主要用于处理一个接口有多个类实现的情况
 *      - version：（服务版本）主要是为后续不兼容升级提供可能
 *
 *  一个根节点（rpcServiceName）可能会对应多个服务地址（相同服务被部署多份的情况）
 * @author Feyl
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {

    /**
     * 将服务注册到 zookeeper（以：服务名 + 远程服务地址的形式）
     *
     * @param rpcServiceName    服务名
     * @param inetSocketAddress 远程服务地址（IP + Port）
     */
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtil.getZookeeperClient();
        CuratorUtil.createPersistentNode(zkClient, servicePath);
    }
}
