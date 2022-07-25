package com.feyl.annotation;

import java.lang.annotation.*;

/**
 * RPC引用注解，自动装配服务实现类
 * 在需要进行远程调用的类的属性上添加该注解，在spring初始化类的时候，通过动态代理创建对象为其远程调用的代理类引用赋值。
 *
 * @author Feyl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    /**
     * 远程调用服务组，默认为空串
     */
    String group() default "";

    /**
     * 远程调用服务版本号，默认为空串
     */
    String version() default "";
}
