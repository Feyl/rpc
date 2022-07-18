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

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);

}
