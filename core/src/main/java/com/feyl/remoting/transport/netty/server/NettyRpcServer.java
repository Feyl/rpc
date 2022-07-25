package com.feyl.remoting.transport.netty.server;

import com.feyl.config.CustomShutdownHook;
import com.feyl.config.RpcServiceConfig;
import com.feyl.factory.SingletonFactory;
import com.feyl.provider.ServiceProvider;
import com.feyl.provider.impl.ZkServiceProviderImpl;
import com.feyl.remoting.transport.netty.codec.RpcMessageDecoder;
import com.feyl.remoting.transport.netty.codec.RpcMessageEncoder;
import com.feyl.utils.RuntimeUtil;
import com.feyl.utils.threadpool.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Netty 的 RPC请求服务端
 * 服务器：接收客户端消息，根据客户端消息调用相应的方法，然后将结果返回给客户端。
 *
 * <a href="https://baike.baidu.com/item/Nagle%E7%AE%97%E6%B3%95">Nagle算法</a>
 * <a href="https://blog.csdn.net/wdscq1234/article/details/52432095">TCP-IP详解：Nagle算法</a>
 *
 * @author Feyl
 */
@Slf4j
@Component
public class NettyRpcServer {

    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    public void registerService(RpcServiceConfig config) {
        serviceProvider.publishService(config);
    }

    @SneakyThrows
    public void start() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolUtil.createThreadFactory("service-handler-group", false));
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    /*
                        TCP 默认开启了 Nagle算法，该算法的作用是尽可能的发送大数据块，减少网络传输。
                        TCP_NODELAY 参数的作用就是控制是否请用 Nagle 算法。
                     */
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 30秒内没有收到客户端的请求就会关闭连接
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });
            // 绑定端口，同步等待绑定成功
            ChannelFuture future = bootstrap.bind(host, PORT).sync();
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("开启 Netty 服务端时发生异常：", e);
        } finally {
            log.error("关闭 bossGroup 和 workerGroup");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }
}
