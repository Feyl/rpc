package com.feyl.loadbalance;

import cn.hutool.core.collection.CollUtil;
import com.feyl.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 负载均衡策略抽象类
 *
 * @author Feyl
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (CollUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    /**
     * 负载均衡策略，从目标服务地址列表中选取的服务地址
     *
     * @param serviceAddresses 目标服务地址列表
     * @param rpcRequest rpc请求的服务
     * @return 根据负载均衡算法从目标服务地址列表中选取的服务地址
     */
    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);

}
