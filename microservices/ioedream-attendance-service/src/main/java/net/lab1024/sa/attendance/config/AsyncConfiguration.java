package net.lab1024.sa.attendance.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * 异步处理配置
 * <p>
 * 配置异步线程池，提升考勤数据处理并发能力
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfiguration {

    /**
     * 考勤异步任务线程池
     * <p>
     * 用于处理Dashboard数据计算、报表生成等耗时操作
     * 核心线程数: 4（CPU核心数的50%）
     * 最大线程数: 8（与CPU核心数相同）
     * 队列容量: 100（防止内存溢出）
     * </p>
     */
    @Bean("attendanceTaskExecutor")
    public Executor attendanceTaskExecutor() {
        log.info("[异步配置] 初始化考勤异步任务线程池");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：CPU核心数的50%
        executor.setCorePoolSize(4);

        // 最大线程数：与CPU核心数相同
        executor.setMaxPoolSize(8);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);

        // 线程名称前缀
        executor.setThreadNamePrefix("attendance-async-");

        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("[异步配置] 考勤异步任务线程池初始化完成: coreSize={}, maxSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * WebSocket推送线程池
     * <p>
     * 用于WebSocket实时消息推送
     * 核心线程数: 2
     * 最大线程数: 4
     * 队列容量: 50
     * </p>
     */
    @Bean("websocketPushExecutor")
    public Executor websocketPushExecutor() {
        log.info("[异步配置] 初始化WebSocket推送线程池");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("websocket-push-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("[异步配置] WebSocket推送线程池初始化完成: coreSize={}, maxSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * 报表生成线程池
     * <p>
     * 用于生成考勤报表、统计数据等
     * 核心线程数: 2
     * 最大线程数: 4
     * 队列容量: 20
     * </p>
     */
    @Bean("reportGenerateExecutor")
    public Executor reportGenerateExecutor() {
        log.info("[异步配置] 初始化报表生成线程池");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("report-generate-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();

        log.info("[异步配置] 报表生成线程池初始化完成: coreSize={}, maxSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
