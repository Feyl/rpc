package com.feyl.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Feyl
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcServiceConfig {

    /**
     * service version
     */
    private String version = "";

    /**
     * when the interface has multiple implementation classes, distinguish by group
     * 当接口有多个实现类时，按组进行区分
     */
    private String group = "";

    /**
     * target service
     */
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.group + this.version;
    }

    /*
     *   canonical：规范化 .adj
     */
    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
