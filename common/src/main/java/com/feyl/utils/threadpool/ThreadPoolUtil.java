package com.feyl.utils.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * ThreadPool（线程池）的工具类
 *
 * @author Feyl
 */
@Slf4j
public final class ThreadPoolUtil {

    /**
     * 通过 threadNamePrefix 来区分不同线程池（把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）。
     * key: threadNamePrefix 业务线程名称前缀
     * value: threadPool 业务线程池
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    public ThreadPoolUtil() {

    }

    /**
     * 创建自定义线程池
     *
     * @param prefix 业务线程名称前缀
     * @return 线程池
     */
    public static ExecutorService createCustomThreadPoolIfAbsent(String prefix) {
        CustomThreadPoolConfig config = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(config, prefix);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent (CustomThreadPoolConfig config, String prefix) {
        return createCustomThreadPoolIfAbsent(config, prefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig config, String prefix, Boolean isDaemon) {
        ExecutorService pool = THREAD_POOLS.computeIfAbsent(prefix, k -> createThreadPool(config, prefix, isDaemon));
        // 如果 threadPool 被 shutdown 的话就重新创建一个
        if (pool.isShutdown() || pool.isTerminated()) {
            THREAD_POOLS.remove(prefix);
            pool = createThreadPool(config, prefix, isDaemon);
            THREAD_POOLS.put(prefix, pool);
        }
        return pool;
    }

    /**
     * 根据 线程池配置类中的配置参数创建线程池
     *
     * @param config 线程池配置类
     * @param prefix 业务线程名称前缀
     * @param isDaemon 是否为守护线程
     * @return
     */
    private static ExecutorService createThreadPool(CustomThreadPoolConfig config, String prefix, Boolean isDaemon) {
        ThreadFactory factory = createThreadFactory(prefix, isDaemon);
        return new ThreadPoolExecutor(config.getCorePoolSize(), config.getMaximumPoolSize(),
                config.getKeepAliveTime(), config.getUnit(),
                config.getWorkQueue(), factory);
    }

    /**
     * 创建 ThreadFactory。如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param prefix 作为创建的线程名字的前缀
     * @param isDaemon 指定是否为 Daemon Thread（守护线程）
     * @return ThreadFactory实例
     */
    public static ThreadFactory createThreadFactory(String prefix, Boolean isDaemon) {
        if (prefix != null) {
            if (isDaemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(prefix + "-%d")
                        .setDaemon(isDaemon).build();
            } else {
                return new ThreadFactoryBuilder()
                        .setNameFormat(prefix + "-%d")
                        .build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * shutDown 所有线程池
     */
    public static void shutDownAllThreadPool() {
        log.info("Call shutDownAllThreadPool method");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("Shut down thread pool: [{}], is terminated: [{}]", entry.getKey(), executorService.isTerminated());
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
                executorService.shutdownNow();
            }
        });
    }

    /**
     * 打印线程池的状态
     *
     * @param executor 线程池对象
     */
    public static void printThreadPoolStatus (ThreadPoolExecutor executor) {
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status",false));
        executorService.scheduleAtFixedRate(() -> {
            log.info("====================== ThreadPool Status ======================");
            log.info("ThreadPool size: [{}]", executor.getPoolSize());
            log.info("Active threads: [{}]", executor.getActiveCount());
            log.info("Number of tasks: [{}]", executor.getCompletedTaskCount());
            log.info("Number of tasks in queue: [{}]", executor.getQueue().size());
            log.info("===============================================================");
        }, 0, 1, TimeUnit.SECONDS);
    }

}
