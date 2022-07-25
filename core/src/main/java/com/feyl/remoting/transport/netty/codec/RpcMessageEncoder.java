package com.feyl.remoting.transport.netty.codec;

import com.feyl.compress.Compressor;
import com.feyl.enums.CompressTypeEnum;
import com.feyl.enums.SerializationTypeEnum;
import com.feyl.extension.ExtensionLoader;
import com.feyl.remoting.constants.RpcConstant;
import com.feyl.remoting.dto.RpcMessage;
import com.feyl.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 根据自定义协议设置的编码器
 *
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version |   full  length      |messageType| codec |compress|    RequestId      |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 *  4B  magic code（魔法数）   1B version（版本）    4B full length（消息长度）   1B messageType（消息类型）
 *  1B codec（序列化类型）     1B compress（压缩类型）  4B  requestId（请求的Id）
 *  body（object类型数据）
 *
 * @author Feyl
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        try {
            out.writeBytes(RpcConstant.MAGIC_NUMBER);
            out.writeByte(RpcConstant.VERSION);
            // 保留一个4字节长的位置用于写入消息总长度
            out.writerIndex(out.writerIndex()  + 4);
            byte msgType = msg.getMessageType();
            out.writeByte(msgType);
            out.writeByte(msg.getCodec());
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            // 构建 总长度
            byte[] bodyBytes = null;
            int length = RpcConstant.HEAD_LENGTH;
            // 如果消息不是心跳消息那么 full length = head length + body length
            if (msgType != RpcConstant.HEARTBEAT_REQUEST_TYPE
                    && msgType != RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
                // 序列化消息内容
                String codec = SerializationTypeEnum.getName(msg.getCodec());
                log.info("codec name:[{}]", codec);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codec);
                bodyBytes = serializer.serialize(msg.getData());
                // 对序列化后的消息进行压缩
                String compress = CompressTypeEnum.getName(msg.getCompress());
                Compressor compressor = ExtensionLoader.getExtensionLoader(Compressor.class)
                        .getExtension(compress);
                bodyBytes = compressor.compress(bodyBytes);
                length += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }

            int writeIndex = out.writerIndex();
            out.writerIndex(writeIndex - length + RpcConstant.MAGIC_NUMBER.length + 1);
            out.writeInt(length);
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}
