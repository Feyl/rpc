package com.feyl.registry.zookeeper;

import cn.hutool.core.collection.CollUtil;
import com.feyl.enums.RpcErrorMessageEnum;
import com.feyl.exception.RpcException;
import com.feyl.extension.ExtensionLoader;
import com.feyl.loadbalance.LoadBalance;
import com.feyl.registry.ServiceDiscovery;
import com.feyl.registry.zookeeper.util.CuratorUtil;
import com.feyl.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 基于 zookeeper 的服务发现
 *
 * @author Feyl
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    /**
     * 负载均衡器
     */
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    /**
     * 根据 RPC请求的服务获取提供服务的远程服务端地址
     *
     * @param rpcRequest 封装了RPC请求服务的对象
     * @return 根据请求服务获取的网络套接字地址（ip + port）
     */
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtil.getZookeeperClient();
        List<String> serviceUrlList = CuratorUtil.getChildNodes(zkClient, rpcServiceName);
        if (CollUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // 负载均衡
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address: [{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
