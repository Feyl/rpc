package com.feyl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 远程调用响应状态码及描述
 *
 * @author Feyl
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
    SUCCESS(200, "远程调用成功"),

    FAIL(500, "远程调用失败");

    private final int code;

    private final String message;
}
