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
     * 远程调用请求ID
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

    private String version;

    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
