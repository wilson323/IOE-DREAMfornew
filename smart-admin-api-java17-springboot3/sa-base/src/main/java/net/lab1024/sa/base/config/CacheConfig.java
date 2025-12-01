package net.lab1024.sa.base.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * 缓存配置类
 * <p>
 * 配置缓存相关组件:
 * - 异步缓存执行器
 * - 缓存管理器Bean注册
 * <p>
 * 符合repowiki架构设计规范:
 * - Manager层负责缓存管理
 * - 异步处理避免影响主业务
 * - 合理的线程池配置
 *
 * @author IOE-DREAM Team
 * @version 1.0
 * @see <a href"docs/repowiki/zh/content/开发规范体系/核心规范/架构设计规范.md">架构设计规范</a>
 * @since 2025-11-16
 */
@Configuration
@EnableAsync
@Slf4j
public class CacheConfig {

    /**
     * 缓存异步执行器
     * <p>
     * 线程池配置:
     * - 核心线程数: 2
     * - 最大线程数: 10
     * - 队列容量: 100
     * - 线程名前缀: cache-executor-
     * - 拒绝策略: CallerRunsPolicy (调用者运行)
     * <p>
     * 用途:
     * - 异步设置缓存
     * - 异步清除缓存
     * - 异步缓存预热
     *
     * @return 缓存异步执行器
     */
    @Bean(name = "cacheExecutor")
    public Executor cacheExecutor() {
        log.info("初始化缓存异步执行器...");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(2);

        // 最大线程数
        executor.setMaxPoolSize(10);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程名前缀
        executor.setThreadNamePrefix("cache-executor-");

        // 线程空闲时间(秒)
        executor.setKeepAliveSeconds(60);

        // 是否等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间(秒)
        executor.setAwaitTerminationSeconds(60);

        // 拒绝策略: CallerRunsPolicy
        // 当线程池和队列都满时,由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 初始化
        executor.initialize();

        log.info("缓存异步执行器初始化完成, corePoolSize: {}, maxPoolSize: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }
}
