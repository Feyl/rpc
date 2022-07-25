package com.feyl.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Feyl
 */
public class RpcConstant {

    /**
     * 魔数：验证 RPC请求消息
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'f', (byte) 'l', (byte) 'o', (byte) 'w'};

    /**
     * 默认字符编码
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 版本信息
     */
    public static final byte VERSION = 1;

    public static final byte TOTAL_LENGTH = 16;

    /**
     * 消息类型
     */
    public static final byte REQUEST_TYPE = 1;

    public static final byte RESPONSE_TYPE = 2;

    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;

    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    /**
     * rpc 消息头长度
     */
    public static final int HEAD_LENGTH = 16;

    /**
     * 心跳信号消息体内容
     */
    public static final String PING = "ping";

    public static final String PONG = "pong";

    /**
     * 最大帧长度
     */
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024; // 8M

}
