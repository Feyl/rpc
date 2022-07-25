package com.feyl.spring;

import com.feyl.annotation.RpcReference;
import com.feyl.annotation.RpcService;
import com.feyl.config.RpcServiceConfig;
import com.feyl.extension.ExtensionLoader;
import com.feyl.factory.SingletonFactory;
import com.feyl.provider.ServiceProvider;
import com.feyl.provider.impl.ZkServiceProviderImpl;
import com.feyl.proxy.RpcClientProxy;
import com.feyl.remoting.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 在bean实例化之后，初始化前后，以查看类是否有注解
 *
 * <a href="https://blog.csdn.net/qq_43185206/article/details/107787308">BeanPostProcessor接口的作用</a>
 * <a href="https://zhuanlan.zhihu.com/p/40871948">Spring中的BeanPostProcessor</a>
 *
 * @author Feyl
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;

    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }


    /**
     * 在每一个bean实例的初始化方法调用之前回调，
     * 将带有RpcService的bean实例注册到注册中心
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            //获取 RpcService 注解
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            //构建 RpcService属性
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }


    /**
     * 在每个bean对象的初始化方法调用之后被回调。
     * 为bean对象的类属性中被 @RpcReference 修饰的属性类创建代理对象并赋值
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declareFields = targetClass.getDeclaredFields();
        for (Field field : declareFields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
