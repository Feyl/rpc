package com.feyl.registry;

import com.feyl.extension.SPI;
import com.feyl.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 *
 * @author Feyl
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 通过 服务提供方的服务名 查找服务提供方地址（IP + Port）
     *
     * @param rpcRequest rpc 服务对象
     * @return 服务提供方地址（IP + Port）
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
