package com.feyl.remoting.transport.netty.client;

import com.feyl.enums.CompressTypeEnum;
import com.feyl.enums.SerializationTypeEnum;
import com.feyl.extension.ExtensionLoader;
import com.feyl.factory.SingletonFactory;
import com.feyl.registry.ServiceDiscovery;
import com.feyl.remoting.constants.RpcConstant;
import com.feyl.remoting.dto.RpcMessage;
import com.feyl.remoting.dto.RpcRequest;
import com.feyl.remoting.dto.RpcResponse;
import com.feyl.remoting.transport.RpcRequestTransport;
import com.feyl.remoting.transport.netty.codec.RpcMessageDecoder;
import com.feyl.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 初始化并关闭Bootstrap对象
 *
 * <a href="https://blog.csdn.net/u013967175/article/details/78591810">IdleStateHandler心跳机制</a>
 * <a href="https://zhuanlan.zhihu.com/p/344431341">CompletableFuture用法详解</a>
 * <a href="https://blog.csdn.net/zhangphil/article/details/80731593">Java CompletableFuture的complete</a>
 *
 * @author Feyl
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {

    /**
     * 服务发现中心
     */
    private final ServiceDiscovery serviceDiscovery;

    /**
     * 服务器未响应的请求
     */
    private final UnprocessedRequests unprocessedRequests;

    /**
     * 根据网络套接字地址获取对应的 channel
     */
    private  final ChannelProvider channelProvider;

    private final Bootstrap bootstrap;

    private final EventLoopGroup eventLoopGroup;


    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //连接超时时间，如果超过这个时间，或者连接无法建立，则连接失败。
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        // 如果5秒内没有向服务器发送数据，则发送心跳请求
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zookeeper");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }


    /**
     * 根据网络套接字地址获取对应的 channel
     *
     * @param inetSocketAddress 网络套接字地址
     * @return 套接字地址对应的 channel
     */
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * 连接服务器并获得通信通道
     *
     * @param inetSocketAddress 网络套接字地址
     * @return 根据套接字地址连接服务器获取的通信通道
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端连接 [{}] 成功！", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    /**
     * 向服务端发送 RPC请求
     *
     * @param rpcRequest 封装RPC请求的实例
     * @return 用于存储响应结果的CompletableFuture<RpcResponse<Object>>
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMsg = RpcMessage.builder()
                    .data(rpcRequest)
                    .codec(SerializationTypeEnum.HESSIAN.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstant.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMsg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("客户端发送消息：[{}]", rpcMsg);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("发送失败：", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    /**
     * 关闭客户端 selector 和 thread
     */
    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
}
