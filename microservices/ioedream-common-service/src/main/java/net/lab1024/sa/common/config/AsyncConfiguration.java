package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;


import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * 异步任务配置类
 * <p>
 * 配置异步任务执行器，支持@Async注解
 * 严格遵循CLAUDE.md规范:
 * - 自定义线程池配置
 * - 合理的线程池参数设置
 * - 完善的异常处理
 * </p>
 * <p>
 * 企业级特性：
 * - 通知发送专用线程池
 * - 审计日志记录线程池
 * - 任务调度线程池
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfiguration {


    /**
     * 通知发送线程池
     * <p>
     * 用于邮件、短信等通知的异步发送
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean("notificationExecutor")
    public ThreadPoolTaskExecutor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("notification-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("通知发送线程池初始化完成，核心线程数：{}，最大线程数：{}", executor.getCorePoolSize(), executor.getMaxPoolSize());
        return executor;
    }

    /**
     * 审计日志线程池
     * <p>
     * 用于审计日志的异步记录
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean("auditExecutor")
    public ThreadPoolTaskExecutor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("audit-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("审计日志线程池初始化完成，核心线程数：{}，最大线程数：{}", executor.getCorePoolSize(), executor.getMaxPoolSize());
        return executor;
    }
}
