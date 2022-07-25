package com.feyl.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * 自定义包扫描器
 *
 *
 * <a href="https://blog.csdn.net/shenchaohao12321/article/details/103115609">
 *                                      【Spring源码分析】ClassPathBeanDefinitionScanner</a>
 *
 * <a href="https://juejin.cn/post/6844904146638733325">ClassPathBeanDefinitionScanner</a>
 *
 * ClassPathBeanDefinitionScanner作用就是将指定包下的类通过一定规则过滤后，
 *              将Class 信息包装成 BeanDefinition 的形式，注册到IOC容器中。
 *
 * @author Feyl
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {

    /**
     * registry是BeanDefinition存储的地方
     * 添加AnnotationTypeFilter到includeFilters，代表只有被includeFilters内匹配的注解才可以被扫描解析，
     *                          这里默认会对标有@Component,@ManagedBean,@Named注解的类进行解析。
     * @param registry BeanDefinition存储的地方
     * @param annoType 需要被扫描解析的类
     */
    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        super(registry);
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
    }

    @Override
    public int scan(String... basePackages) {
        return super.scan(basePackages);
    }

}
