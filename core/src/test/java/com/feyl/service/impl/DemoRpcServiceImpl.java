package com.feyl.service.impl;

import com.feyl.annotation.RpcService;
import com.feyl.service.DemoRpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Feyl
 */
@Slf4j
@RpcService(group = "test1", version = "version1")
public class DemoRpcServiceImpl implements DemoRpcService {

    @Override
    public String hello() {
        return "hello";
    }
}

