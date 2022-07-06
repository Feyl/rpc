package com.feyl.annotation;

import java.lang.annotation.*;

/**
 * RPC reference annotation, autowire the service implementation class
 * RPC引用注释，自动装配服务实现类
 *
 * @author Feyl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcReference {

    /**
     * Service version, default value is empty string
     *
     * @return
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}
