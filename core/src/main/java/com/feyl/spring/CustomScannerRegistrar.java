package com.feyl.spring;

import com.feyl.annotation.RpcScan;
import com.feyl.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * 扫描和筛选指定的注解
 *
 * <a href="https://segmentfault.com/a/1190000040585229">ImportBeanDefinitionRegistrar的作用</a>
 *
 * @author Feyl
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final String SPRING_BEAN_BASE_PACKAGE = "com.feyl";

    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        //获取RpcScan注解的属性和值
        AnnotationAttributes rpcScanAnnoAttrs = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanBasePackages = new String[0];
        if (rpcScanAnnoAttrs != null) {
            //获取basePackage属性的值
            rpcScanBasePackages = rpcScanAnnoAttrs.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanBasePackages.length == 0) {
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) metadata).getIntrospectedClass().getPackage().getName()};
        }
        // 扫描RpcService注解
        CustomScanner rpcServiceScanner = new CustomScanner(registry, RpcService.class);
        // 扫描Component注解
        CustomScanner springBeanScanner = new CustomScanner(registry, Component.class);
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        int springBeanCnt = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner 扫描的数量：[{}]", springBeanCnt);
        int rpcServiceCnt = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner 扫描的数量：[{}]", rpcServiceCnt);
    }
}
