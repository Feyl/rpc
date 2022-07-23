package com.feyl.extension;

import java.lang.annotation.*;

/**
 * 标记接口
 * @SPI用来标记接口是一个可扩展的接口
 *
 * @author Feyl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
