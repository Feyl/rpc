package com.feyl.annotation;

import java.lang.annotation.*;

/**
 * RPC service annotation, marked on the service implementation class
 * RPC服务注释，标记在服务实现类上
 *
 * @author Feyl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    /**
     * Service version, default value is empty string
     * 服务版本号
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     * 服务组
     */
    String group() default "";
}
