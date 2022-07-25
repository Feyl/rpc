package com.feyl.remoting.transport.netty.client;

import com.feyl.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器未响应的请求。
 *
 * @author Feyl
 */
public class UnprocessedRequests {

    /**
     * key：请求ID requestId
     * value：请求对应的响应结果（正常结果或异常对象）
     */
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    /**
     * 收到服务器的响应消息，从存储完成响应的请求的Map中移除该远程调用请求
     *
     * @param rpcResponse
     */
    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (future != null) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

}
