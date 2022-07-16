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
 * @author Feyl
 */
@Slf4j
public class SocketRpcServer {

    private final ExecutorService threadPool;

    private final ServiceProvider serviceProvider;


    public SocketRpcServer() {
        this.threadPool = ThreadPoolUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

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
