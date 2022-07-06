package com.feyl.remoting.transport;

import com.feyl.extension.SPI;
import com.feyl.remoting.dto.RpcRequest;

/**
 * 封装远程调用请求
 *
 * @author Feyl
 */
@SPI
public interface RpcRequestTransport {

    /**
     * 向被调用方发送远程调用请求并返回结果
     *
     * @param rpcRequest 请求体
     * @return 来自被调用方的响应数据
     */
    Object sendRpcRequest(RpcRequest rpcRequest);

}
