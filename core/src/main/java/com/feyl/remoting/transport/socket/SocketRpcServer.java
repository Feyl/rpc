package com.feyl.remoting.transport.socket;

import com.feyl.config.CustomShutdownHook;
import com.feyl.config.RpcServiceConfig;
import com.feyl.factory.SingletonFactory;
import com.feyl.provider.ServiceProvider;
import com.feyl.provider.impl.ZkServiceProviderImpl;
import com.feyl.utils.threadpool.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static com.feyl.remoting.transport.netty.server.NettyRpcServer.PORT;

/**
 * 基于 Socket 传输 RPC请求 服务端
 *
 * @author Feyl
 */
@Slf4j
public class SocketRpcServer {

    /**
     * 用于存储执行客户端请求的线程的线程池
     */
    private final ExecutorService threadPool;

    private final ServiceProvider serviceProvider;


    public SocketRpcServer() {
        this.threadPool = ThreadPoolUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 将RPC服务注册到注册中心并且将服务提供对象存入内存
     *
     * @param rpcServiceConfig RPC服务相关的配置
     */
    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start() {
        try(ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("Client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandler(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("Occur IOException:", e);
        }
    }

}
