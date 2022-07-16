package com.feyl.remoting.transport.netty.client;

import com.feyl.enums.CompressTypeEnum;
import com.feyl.enums.SerializationTypeEnum;
import com.feyl.factory.SingletonFactory;
import com.feyl.remoting.constants.RpcConstant;
import com.feyl.remoting.dto.RpcMessage;
import com.feyl.remoting.dto.RpcResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 自定义客户端 ChannelHandler 处理来自服务端发送的数据
 *
 * {@link SimpleChannelInboundHandler}
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放，
 * 内部的 channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。
 *
 * @author Feyl
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequests unprocessedRequests;

    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 读取服务器发送的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("客户端收到消息：[{}]",msg);
            if (msg instanceof RpcMessage) {
                RpcMessage rpcMsg = (RpcMessage) msg;
                byte msgType = rpcMsg.getMessageType();
                if (msgType == RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("心跳： [{}]", rpcMsg.getData());
                } else if (msgType == RpcConstant.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) rpcMsg.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("Write idle 发生 [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMsg = new RpcMessage();
                rpcMsg.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMsg.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMsg.setMessageType(RpcConstant.HEARTBEAT_REQUEST_TYPE);
                rpcMsg.setData(RpcConstant.PING);
                channel.writeAndFlush(rpcMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    /**
     * 在处理客户端消息时发生异常时调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端捕获异常：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
