package com.feyl.extension;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 参考 dubbo spi: https://dubbo.apache.org/zh-cn/docs/source_code_guide/dubbo-spi.html
 *
 * @author Feyl
 */
@Slf4j
public final class ExtensionLoader<T> {
    /**
     * 存储服务接口命名文件的目录路径
     */
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";

    /**
     * Key：服务接口 class对象
     * value：扩展加载器：用于加载服务接口的实现类
     */
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /**
     * Key：服务接口的实现类Class实例
     * value：服务接口对应实现类的实例
     */
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    /**
     * 服务接口对应的 Class实例
     */
    private final Class<?> type;

    /**
     * key：服务接口对应实现类在服务接口命名文件中的键值
     * value：存储服务接口对应实现类的实例的holder
     */
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    /**
     * Holder中：
     * key：服务接口对应实现类在服务接口命名文件中的键值
     * value：具体实现类全类名
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 通过接口的 class对象获取对应的 ExtensionLoader
     *
     * @param type 接口的 class对象
     * @return 接口的 class对象对应的 ExtensionLoader
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        // 先从缓存中获取 ExtensionLoader，如果缓存中不存在，则创建并放入缓存中后返回
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 根据服务接口对应实现类在服务接口命名文件中的键值 获取服务接口对应实现类的实例
     *
     * @param name 服务接口对应实现类在服务接口命名文件中的键值
     * @return 服务接口对应实现类的实例
     */
    public T getExtension(String name) {
        if (StrUtil.isBlank(name)) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        // 先从缓存中获取服务接口对应实现类的实例的Holder，如果不存在，则创建一个并放入缓存中
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // 如果 holder中不存在对应的实例，则创建对应实例放入holder中
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 根据服务接口对应实现类在服务接口命名文件中的键值获取实现类Class实例创建对应实现类实例
     *
     * @param name 服务接口对应实现类在服务接口命名文件中的键值
     * @return 对应实现类的实例
     */
    private T createExtension(String name) {
        // 加载所有 type类型服务接口的实现类，并根据就是实现类在服务接口命名文件中的键值获取对应实现类的实例
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    /**
     * 从缓存中获取存储服务接口对应的已加载扩展实现类的映射实例
     * 映射实例：
     * key：实现类键值
     * value：实现类全类名
     *
     * @return
     */
    private Map<String, Class<?>> getExtensionClasses() {
        // 从缓存中获取存储了已加载的扩展实现类的键值对（key：实现类键值，value：实现类全类名）的 Holder
        Map<String, Class<?>> classes = cachedClasses.get();
        // double check
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // 从扩展目录（服务接口命名文件）中加载所有扩展实现类
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 服务接口命名文件中加载所有扩展实现类
     *
     * @param extensionClasses 用于缓存加载的 Class对象（key：服务接口对应实现类键值，value：对应实现类的 Class对象）
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 根据资源路径（实现类键值对）加载扩展实现类
     * key：实现类键值
     * value：实现类全类名
     *
     * @param extensionClasses 用于缓存加载的 Class对象（key：服务接口名，value：对应实现类的 Class对象）
     * @param classLoader 类加载器
     * @param resourceUrl 资源路径：服务接口命名文件的路径
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // 读取服务接口命名的文件中的所有行
            while ((line = reader.readLine()) != null) {
                // 获取注释标志# 的下标
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // 忽略注释标志# 后的所有文字
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim(); //获取服务接口实现对应的键值
                        String clazzName = line.substring(ei + 1).trim();//获取服务接口实现对应的全类名
                        // 服务接口命名文件（SPI文件）中是以键值对的形式存储实现类的，键值均不能为空
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
