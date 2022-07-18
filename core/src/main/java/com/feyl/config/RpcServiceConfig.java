package com.feyl.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Feyl
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcServiceConfig {
    /**
     * 远程调用服务名
     */
    private Object service;

    /**
     * 当接口有多个实现类时，按组进行区分
     */
    private String group = "";

    /**
     * 服务版本：主要是为后续不兼容升级提供可能
     */
    private String version = "";


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
