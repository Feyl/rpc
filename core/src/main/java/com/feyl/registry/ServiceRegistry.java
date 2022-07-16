package com.feyl.registry;

import com.feyl.extension.SPI;

import java.net.InetSocketAddress;

/**
 * service registration
 *
 * @author Feyl
 */
@SPI
public interface ServiceRegistry {

    /**
     *
     * @param rpcServiceName rpc服务名字
     * @param inetSocketAddress 服务地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
