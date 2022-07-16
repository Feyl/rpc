package com.feyl.loadbalance;

import com.feyl.extension.SPI;
import com.feyl.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 接口功能设置负载均衡策略
 *
 * @author Feyl
 */
@SPI
public interface LoadBalance {

    /**
     * 从现有服务地址列表中选择一个
     *
     * @param serviceUrlList 服务地址列表
     * @param rpcRequest rpc请求
     * @return 目标服务地址
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
