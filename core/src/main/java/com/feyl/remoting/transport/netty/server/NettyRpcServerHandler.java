package com.feyl.remoting.transport.netty.server;

import cn.hutool.core.util.ReferenceUtil;
import com.feyl.enums.CompressTypeEnum;
import com.feyl.enums.RpcResponseCodeEnum;
import com.feyl.enums.SerializationTypeEnum;
import com.feyl.factory.SingletonFactory;
import com.feyl.remoting.constants.RpcConstant;
import com.feyl.remoting.dto.RpcMessage;
import com.feyl.remoting.dto.RpcRequest;
import com.feyl.remoting.dto.RpcResponse;
import com.feyl.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义服务器的ChannelHandler来处理客户机发送的数据。
 *
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放，
 * {@link SimpleChannelInboundHandler} 内部的 channelRead 方法会替你释放 ByteBuf，
 * 避免可能导致的内存泄露问题。
 *
 * @author Feyl
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                log.info("server receive msg: [{}]", msg);
                byte msgType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.HESSIAN.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (msgType == RpcConstant.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstant.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstant.PONG);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    // 执行客户端需要执行的目标方法并且返回方法的执行结果
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstant.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(rpcRequest.getRequestId(), result);
                        rpcMessage.setData(rpcResponse);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writeable now, message dropped");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            // 确保 ByteBuf 被释放，否则会导致内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 长时间未收到客户端发送的远程调用请求或心跳信号则关闭连接
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 处理服务端 pipeline 中出现的未处理的异常，关闭 channel
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
