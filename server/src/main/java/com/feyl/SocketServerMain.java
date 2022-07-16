package com.feyl;

import com.feyl.config.RpcServiceConfig;
import com.feyl.remoting.transport.socket.SocketRpcServer;
import com.feyl.service.impl.HelloServiceImpl;

/**
 * @author Feyl
 */
public class SocketServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
