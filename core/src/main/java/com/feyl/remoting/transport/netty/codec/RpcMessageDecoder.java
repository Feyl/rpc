package com.feyl.remoting.transport.netty.codec;

import com.feyl.compress.Compressor;
import com.feyl.enums.CompressTypeEnum;
import com.feyl.enums.SerializationTypeEnum;
import com.feyl.extension.ExtensionLoader;
import com.feyl.remoting.constants.RpcConstant;
import com.feyl.remoting.dto.RpcMessage;
import com.feyl.remoting.dto.RpcRequest;
import com.feyl.remoting.dto.RpcResponse;
import com.feyl.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 根据自定义协议设置的解码器
 *
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version |     full  length    |messageType| codec |compress|    RequestId      |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）    4B full length（消息长度）    1B messageType（消息类型）
 * 1B codec（序列化类型）    1B compress（压缩类型）  4B  requestId（请求的Id）
 * body（object类型数据）
 *
 * {@link LengthFieldBasedFrameDecoder} is a length-based decoder,
 *                      used to solve TCP unpacking and sticking problems.
 *
 * <a href="https://zhuanlan.zhihu.com/p/95621344"> LengthFieldBasedFrameDecoder解码器 </a>
 *
 * @author Feyl
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder{


    public RpcMessageDecoder() {

        /*
         * lengthFieldOffset: 魔码长度占4B，版本长度占1B，然后是总长度值。 所以值是5。
         * lengthFieldLength: 总长度值占4B，所以值是4。
         * lengthAdjustment: 总长度包括所有数据，之前读取9个字节，所以左边的长度是(fullLength-9)，所以值是-9。
         * initialBytesToStrip: 我们将手动检查魔术代码和版本，不剥离任何字节，所以值是0。
         */
        this(RpcConstant.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     *
     * @param maxFrameLength 最大帧长度。也就是可以接收的数据的最大长度。如果超过，此次数据会被丢弃。
     * @param lengthFieldOffset 长度域偏移。就是说数据开始的几个字节可能不是表示数据长度，需要后移几个字节才是长度域。
     * @param lengthFieldLength 长度域字节数。用几个字节来表示数据长度。
     * @param lengthAdjustment 数据长度修正。因为长度域指定的长度可以使header+body的整个长度，也可以只是body的长度。
     *                         如果表示header+body的整个长度，那么我们需要修正数据长度。
     * @param initialBytesToStrip 跳过的字节数。如果你需要接收header+body的所有数据，此值就是0，
     *                            如果你只想接收body数据，那么需要跳过header所占用的字节数。
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstant.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!",e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        // 注意：必须按照顺序读取
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        // 构建 RpcMessage 对象
        byte msgType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMsg = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(msgType).build();
        if (msgType == RpcConstant.HEARTBEAT_REQUEST_TYPE) {
            rpcMsg.setData(RpcConstant.PING);
            return rpcMsg;
        }
        if (msgType == RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
            rpcMsg.setData(RpcConstant.PONG);
            return rpcMsg;
        }
        int bodyLength = fullLength - RpcConstant.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] content = new byte[bodyLength];
            in.readBytes(content);
            // 对消息内容进行解压缩
            String compress = CompressTypeEnum.getName(compressType);
            Compressor compressor = ExtensionLoader.getExtensionLoader(Compressor.class)
                    .getExtension(compress);
            content = compressor.decompress(content);
            // 反序列化生成对象
            String codec = SerializationTypeEnum.getName(codecType);
            log.info("codec name: [{}]", codec);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codec);
            if (msgType == RpcConstant.REQUEST_TYPE) {
                RpcRequest data = serializer.deserialize(content, RpcRequest.class);
                rpcMsg.setData(data);
            } else {
                RpcResponse data = serializer.deserialize(content, RpcResponse.class);
                rpcMsg.setData(data);
            }
        }
        return rpcMsg;
    }


    private void checkMagicNumber(ByteBuf in) {
        // 读取前4个字节作为魔数，进行比较
        int len = RpcConstant.MAGIC_NUMBER.length;
        byte[] magic = new byte[len];
        in.readBytes(magic);
        for (int i = 0; i < len; i++) {
            if (magic[i] != RpcConstant.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(magic));
            }
        }
    }


    private void checkVersion(ByteBuf in) {
        // 读取1个字节作为版本号，进行比较
        byte version = in.readByte();
        if (version != RpcConstant.VERSION) {
            throw new RuntimeException("version isn't compatible: " + version);
        }
    }
}
