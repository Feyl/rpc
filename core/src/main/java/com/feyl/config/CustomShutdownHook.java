package com.feyl.config;

import com.feyl.registry.zookeeper.util.CuratorUtil;
import com.feyl.remoting.transport.netty.server.NettyRpcServer;
import com.feyl.utils.threadpool.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * When the server is closed, do something such as unregister all services
 * 当服务器关闭时，执行一些操作，例如取消注册所有服务
 *
 * @author Feyl
 */
@Slf4j
public class CustomShutdownHook {

    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("add ShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                CuratorUtil.clearRegistry(CuratorUtil.getZkClient(), isa);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            ThreadPoolUtil.shutDownAllThreadPool();
        }));
    }
}
