package com.feyl.provider;

import com.feyl.config.RpcServiceConfig;

/**
 * 存储和提供 服务对象
 *
 * @author Feyl
 */
public interface ServiceProvider {

    /**
     * 内存中缓存已经注册的服务名（interface name + group + version）
     *
     * @param rpcServiceConfig RPC服务相关的属性
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取服务对象
     *
     * @param rpcServiceName RPC服务名（interface name + group + version）
     * @return RPC服务实例
     */
    Object getService(String rpcServiceName);

    /**
     * 将 RPC服务配置注册到注册中心
     *
     * @param rpcServiceConfig RPC服务相关的属性
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
