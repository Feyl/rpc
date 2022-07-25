package com.feyl.annotation;

import com.feyl.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 扫描自定义注解
 *
 * <a href="https://www.jianshu.com/p/3c5922ec3686">Spring @Import 机制</a>
 *
 * @author Feyl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Import(CustomScannerRegistrar.class)
public @interface RpcScan {

    /**
     * 定义要扫描的包
     */
    String[] basePackage();

}
