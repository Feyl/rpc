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
 * 基于Zookeeper的服务提供者实现
 *
 * <a href="https://blog.csdn.net/liuxiao723846/article/details/88181144">concurrent set的创建</a>
 * <a href="https://zhuanlan.zhihu.com/p/58697324">如何在Java 8中创建线程安全的ConcurrentHashSet</a>
 *
 * @author Feyl
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    /**
     * key: rpc 服务名字（interface name + group + version）
     * value：服务对象
     */
    private final Map<String, Object> serviceMap;

    /**
     * 已经注册的服务
     * 以 interface name + group + version 的字符串形式注册
     */
    private final Set<String> registeredService;

    private final ServiceRegistry serviceRegistry;


    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zookeeper");
    }
    /**
     * 内存中缓存已经注册的服务名（interface name + group + version）
     */
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

    /**
     * 获取服务对象
     * @param rpcServiceName RPC服务名（interface name + group + version）
     * @return RPC服务实例
     */
    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    /**
     * 将 RPC服务配置注册到注册中心
     * 以 interface name + group + version / ip + port 的字符串形式注册（在zookeeper 中以该字符串为路径创建持久结点）
     */
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
