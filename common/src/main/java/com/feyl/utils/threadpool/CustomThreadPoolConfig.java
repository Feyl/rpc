package com.feyl.utils.threadpool;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 线程池自定义配置类，可自行根据业务场景修改配置参数。
 *
 * @author Feyl
 */
@Getter
@Setter
public class CustomThreadPoolConfig {

    /**
     * 线程池默认参数值
     */
    private static final int DEFAULT_CORE_POOL_SIZE = 10;

    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 100;

    private static final int DEFAULT_KEEP_ALIVE_TIME = 1;

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

    private static final int DEFAULT_BLOCKING_QUEUE_CAPACITY = 100;


    /**
     * 核心线程数
     */
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;

    /**
     * 最大线程数
     */
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;

    /**
     * 空闲线程存活时间
     */
    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;

    /**
     * 时间单位
     */
    private TimeUnit unit = DEFAULT_TIME_UNIT;

    /**
     * 工作（阻塞）队列容量
     */
    private int blockingQueueCapacity = DEFAULT_BLOCKING_QUEUE_CAPACITY;


    /**
     * 工作队列 （使用有界队列）
     */
    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(blockingQueueCapacity);

}
