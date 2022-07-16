package com.feyl.registry;

import com.feyl.extension.SPI;
import com.feyl.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @author Feyl
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 通过 rpc服务名 寻找服务
     *
     * @param rpcRequest rpc 服务对象
     * @return 服务地址
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
