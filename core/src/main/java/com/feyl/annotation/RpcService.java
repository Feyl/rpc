package com.feyl.annotation;

import java.lang.annotation.*;

/**
 * RPC服务注解，标记在服务实现类上。
 * 在可提供服务的类上标注该注解，在服务启动时，将标注该注解的类的实例在bean实例化之后注册到注册中心。
 *
 * @author Feyl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    /**
     * 远程调用服务组，默认值为空
     */
    String group() default "";

    /**
     * 远程调用服务版本号，默认值为空
     */
    String version() default "";

}
