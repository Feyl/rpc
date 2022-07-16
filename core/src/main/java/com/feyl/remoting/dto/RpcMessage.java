package com.feyl.remoting.dto;

import lombok.*;

/**
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
     * 序列化类型
     */
    private byte codec;

    /**
     * 压缩类型
     */
    private byte compress;

    /**
     * 请求 id
     */
    private int requestId;

    /**
     * 数据
     */
    private Object data;
}
