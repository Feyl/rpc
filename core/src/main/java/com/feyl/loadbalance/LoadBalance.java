package com.feyl.loadbalance;

import com.feyl.extension.SPI;
import com.feyl.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 负载均衡策略接口
 *
 * <a href="https://www.pdai.tech/md/algorithm/alg-domain-load-balance.html">负载均衡算法</a>
 *
 * @author Feyl
 */
@SPI
public interface LoadBalance {

    /**
     * 从现有服务地址列表中选择一个
     *
     * @param serviceUrlList 远程服务地址列表
     * @param rpcRequest rpc请求
     * @return 目标服务地址
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
