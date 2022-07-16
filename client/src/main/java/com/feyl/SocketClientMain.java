package com.feyl;

import com.feyl.config.RpcServiceConfig;
import com.feyl.proxy.RpcClientProxy;
import com.feyl.remoting.transport.RpcRequestTransport;
import com.feyl.remoting.transport.socket.SocketRpcClient;

/**
 * @author Feyl
 */
public class SocketClientMain {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceConfig);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}