package com.feyl.registry;

import com.feyl.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 *
 * @author Feyl
 */
@SPI
public interface ServiceRegistry {

    /**
     * @param rpcServiceName 服务提供方服务名（class name + group + version）
     * @param inetSocketAddress 服务提供方地址（IP + Port）
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
