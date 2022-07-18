package com.feyl.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * 客户端请求实体
 *
 * @author Feyl
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    /**
     * RPC 请求ID
     */
    private String requestId;

    /**
     * 调用的接口名
     */
    private String interfaceName;

    /**
     * 调用的方法名
     */
    private String methodName;

    /**
     * 调用方法传递的参数类型
     */
    private Class<?>[] paramTypes;

    /**
     * 调用方法传递的实际参数
     */
    private Object[] parameters;

    /**
     * 主要用于处理一个接口有多个类实现的情况
     */
    private String group;

    /**
     * 服务版本，主要是为后续不兼容升级提供可能
     */
    private String version;


    /**
     * 获取远程服务名
     *
     * @return 远程服务名
     */
    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
