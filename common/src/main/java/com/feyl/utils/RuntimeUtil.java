package com.feyl.utils;

/**
 * @author Feyl
 */
public class RuntimeUtil {

    /**
     * 获取CPU的核心数
     *      - Runtime.getRuntime().availableProcessors() 如果工作在 docker 容器下，
     *                              因为容器不是物理隔离的，会拿到物理 cpu 个数，而不是容器申请时的个数。
     *      - 这个问题直到 jdk 10 才修复，使用 jvm 参数 UseContainerSupport 配置， 默认开启。
     *
     * @return cpu的核心数
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
