package net.lab1024.sa.oa.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 工作流异步任务配置
 * <p>
 * 配置异步任务执行器，支持工作流通知、事件处理等异步操作
 * 提供合理的线程池配置，确保系统性能和稳定性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Configuration
@EnableAsync
public class WorkflowAsyncConfig {

    /**
     * 工作流通知异步执行器
     * 专门处理WebSocket实时通知的异步发送
     */
    @Bean("workflowNotificationExecutor")
    public Executor workflowNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 = CPU核心数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());

        // 最大线程数 = CPU核心数 * 2
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);

        // 队列容量
        executor.setQueueCapacity(1000);

        // 线程名前缀
        executor.setThreadNamePrefix("workflow-notification-");

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待关闭时间（秒）
        executor.setAwaitTerminationSeconds(60);

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        // 初始化
        executor.initialize();

        return executor;
    }

    /**
     * 工作流事件处理异步执行器
     * 专门处理Flowable事件监听器的异步处理
     */
    @Bean("workflowEventExecutor")
    public Executor workflowEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 = CPU核心数 + 1
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() + 1);

        // 最大线程数 = CPU核心数 * 3
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 3);

        // 队列容量
        executor.setQueueCapacity(2000);

        // 线程名前缀
        executor.setThreadNamePrefix("workflow-event-");

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待关闭时间（秒）
        executor.setAwaitTerminationSeconds(60);

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        // 初始化
        executor.initialize();

        return executor;
    }

    /**
     * 工作流批处理异步执行器
     * 专门处理批量操作和统计计算
     */
    @Bean("workflowBatchExecutor")
    public Executor workflowBatchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 = CPU核心数 / 2
        executor.setCorePoolSize(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

        // 最大线程数 = CPU核心数
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());

        // 队列容量
        executor.setQueueCapacity(500);

        // 线程名前缀
        executor.setThreadNamePrefix("workflow-batch-");

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待关闭时间（秒）
        executor.setAwaitTerminationSeconds(120);

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        // 初始化
        executor.initialize();

        return executor;
    }
}