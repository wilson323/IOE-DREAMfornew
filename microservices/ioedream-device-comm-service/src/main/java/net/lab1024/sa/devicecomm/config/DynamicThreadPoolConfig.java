package net.lab1024.sa.devicecomm.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态线程池配置
 * <p>
 * 配置协议消息处理的动态线程池
 * </p>
 * <p>
 * ⚠️ 线程池已废弃：protocolMessageExecutor已废弃，
 * 请使用 UnifiedThreadPoolConfiguration 中的统一线程池：
 * - ioExecutor: IO密集型任务（协议消息处理）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 * @deprecated 请使用 UnifiedThreadPoolConfiguration
 */
@Deprecated
@Slf4j
@Configuration
public class DynamicThreadPoolConfig {

    /**
     * 核心线程数（从配置文件读取，默认：CPU核心数）
     */
    @Value("${device.protocol.thread-pool.core-size:10}")
    private int corePoolSize;

    /**
     * 最大线程数（从配置文件读取，默认：CPU核心数 * 2）
     */
    @Value("${device.protocol.thread-pool.max-size:50}")
    private int maxPoolSize;

    /**
     * 队列容量（从配置文件读取，默认：1000）
     */
    @Value("${device.protocol.thread-pool.queue-capacity:1000}")
    private int queueCapacity;

    /**
     * 线程存活时间（秒，从配置文件读取，默认：60）
     */
    @Value("${device.protocol.thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    /**
     * 协议消息处理线程池
     * <p>
     * 用于异步处理协议消息，支持动态调整线程数
     * </p>
     *
     * @return 线程池执行器
     * @deprecated 请使用 UnifiedThreadPoolConfiguration 中的 ioExecutor
     */
    @Deprecated
    @Bean(name = "protocolMessageExecutor")
    public ThreadPoolTaskExecutor protocolMessageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 如果配置的核心线程数为0或负数，使用CPU核心数
        int finalCorePoolSize = corePoolSize > 0 ? corePoolSize : Runtime.getRuntime().availableProcessors();

        // 如果配置的最大线程数为0或负数，使用CPU核心数 * 2
        int finalMaxPoolSize = maxPoolSize > 0 ? maxPoolSize : Runtime.getRuntime().availableProcessors() * 2;

        // 确保最大线程数 >= 核心线程数
        if (finalMaxPoolSize < finalCorePoolSize) {
            finalMaxPoolSize = finalCorePoolSize;
        }

        executor.setCorePoolSize(finalCorePoolSize);
        executor.setMaxPoolSize(finalMaxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);

        // 线程名前缀
        executor.setThreadNamePrefix("ProtocolMessage-");

        // 拒绝策略：调用者运行（确保任务不丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // 初始化线程池
        executor.initialize();

        log.info("[动态线程池] 协议消息处理线程池初始化完成，核心线程数={}, 最大线程数={}, 队列容量={}, 线程存活时间={}秒",
                finalCorePoolSize, finalMaxPoolSize, queueCapacity, keepAliveSeconds);

        return executor;
    }

    /**
     * 优雅关闭线程池
     * <p>
     * 在Spring容器关闭时，确保所有任务完成后再关闭线程池
     * </p>
     */
    @PreDestroy
    public void destroy() {
        log.info("[动态线程池] 开始关闭协议消息处理线程池");
    }
}

