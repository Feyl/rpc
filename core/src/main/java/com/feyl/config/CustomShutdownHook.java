package com.feyl.config;

import com.feyl.registry.zookeeper.util.CuratorUtil;
import com.feyl.remoting.transport.netty.server.NettyRpcServer;
import com.feyl.utils.threadpool.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * 当服务器关闭时，执行一些操作，例如取消注册所有服务、关闭所有线程池
 *
 * @author Feyl
 */
@Slf4j
public class CustomShutdownHook {
    //单例模式（饿汉）
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    /**
     * 从注册中心取消注册的所有服务并关闭添加关闭线程池的钩子函数
     *
     * <a href="https://blog.csdn.net/yangshangwei/article/details/102583944">
     *              高并发编程-Runtime.getRuntime().addShutdownHook为自己的应用添加hook</a>
     * <a href="https://blog.csdn.net/wk1134314305/article/details/78504269">addShutdownHook函数的用法和注意事项</a>
     */
    public void clearAll() {
        log.info("add ShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                //从zookeeper注册中心取消注册的所有服务
                CuratorUtil.clearRegistry(CuratorUtil.getZookeeperClient(), inetSocketAddress);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            ThreadPoolUtil.shutDownAllThreadPool();
        }));
    }
}
