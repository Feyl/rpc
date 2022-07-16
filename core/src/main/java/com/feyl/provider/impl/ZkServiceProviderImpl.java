package com.feyl.provider.impl;

import com.feyl.config.RpcServiceConfig;
import com.feyl.enums.RpcErrorMessageEnum;
import com.feyl.exception.RpcException;
import com.feyl.extension.ExtensionLoader;
import com.feyl.provider.ServiceProvider;
import com.feyl.registry.ServiceRegistry;
import com.feyl.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Feyl
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    /**
     * key: rpc 服务名字（interface name + version + group）
     * value：服务对象
     */
    private final Map<String, Object> serviceMap;

    private final Set<String> registeredService;

    private final ServiceRegistry serviceRegistry;


    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)){
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces: {}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("Occur exception when getHostAddress", e);
        }
    }
}
