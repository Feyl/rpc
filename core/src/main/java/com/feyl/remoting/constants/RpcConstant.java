package com.feyl.remoting.constants;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Feyl
 */
public class RpcConstant {

    /**
     * 魔数：验证 RPC请求消息
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 版本信息
     */
    public static final byte VERSION = 1;

    public static final byte TOTAL_LENGTH = 16;

    public static final byte REQUEST_TYPE = 1;

    public static final byte RESPONSE_TYPE = 2;

    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;

    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    public static final int HEAD_LENGTH = 16;

    public static final String PING = "ping";

    public static final String PONG = "pong";

    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
