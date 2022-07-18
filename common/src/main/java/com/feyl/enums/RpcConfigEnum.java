package com.feyl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * properties 文件中 rpc相关属性的键值
 *
 * @author Feyl
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    RPC_CONFIG_PATH("rpc.properties"),

    ZOOKEEPER_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;

}
