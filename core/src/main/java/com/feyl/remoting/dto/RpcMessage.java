package com.feyl.remoting.dto;

import lombok.*;

/**
 * 远程调用消息
 *
 * @author Feyl
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcMessage {

    /**
     * 消息类型
     */
    private byte messageType;

    /**
     * 消息序列化方式
     */
    private byte codec;

    /**
     * 消息压缩方式
     */
    private byte compress;

    /**
     * 请求 id
     */
    private int requestId;

    /**
     * 请求 数据
     */
    private Object data;
}
