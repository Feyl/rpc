package com.feyl.remoting.dto;

import com.feyl.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * 服务端响应实体
 *
 * @author Feyl
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;

    /**
     * RPC 请求ID
     */
    private String requestId;

    /**
     * 调用的响应状态码
     */
    private Integer code;

    /**
     * 调用的响应信息
     */
    private String message;

    /**
     * 调用的响应体数据
     */
    private T data;

    public static <T> RpcResponse<T> success(String requestId, T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (data != null) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }
}
