package net.lab1024.sa.admin.module.consume.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 消费模块异步处理配置
 * 配置各种异步任务的线程池，优化系统性能
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 消费处理线程池
     * 用于处理消费记录创建、状态更新等核心消费操作
     */
    @Bean("consumeTaskExecutor")
    public Executor consumeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 = CPU核心数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());

        // 最大线程数 = CPU核心数 * 2
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);

        // 队列容量
        executor.setQueueCapacity(1000);

        // 线程名前缀
        executor.setThreadNamePrefix("consume-async-");

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 线程空闲时间
        executor.setKeepAliveSeconds(60);

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    /**
     * 通知发送线程池
     * 用于发送邮件、短信、推送等通知操作
     */
    @Bean("notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 通知操作IO密集，配置较大线程数
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("notification-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(120);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    /**
     * 统计计算线程池
     * 用于计算各种统计数据和报表生成
     */
    @Bean("statisticsTaskExecutor")
    public Executor statisticsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 统计计算CPU密集，线程数适中
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("statistics-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    /**
     * 数据对账线程池
     * 用于数据一致性检查和对账操作
     */
    @Bean("reconciliationTaskExecutor")
    public Executor reconciliationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 对账操作定时执行，线程数较少
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("reconciliation-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(300);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);

        executor.initialize();
        return executor;
    }

    /**
     * 批量处理线程池
     * 用于批量导入、批量更新等操作
     */
    @Bean("batchTaskExecutor")
    public Executor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 批量操作可能耗时较长，配置适中线程数
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("batch-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(180);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(90);

        executor.initialize();
        return executor;
    }

    /**
     * 数据清理线程池
     * 用于定期清理过期数据
     */
    @Bean("cleanupTaskExecutor")
    public Executor cleanupTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 数据清理操作频率较低，线程数较少
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("cleanup-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(600);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);

        executor.initialize();
        return executor;
    }

    /**
     * 报表生成线程池
     * 用于生成各类统计报表
     */
    @Bean("reportTaskExecutor")
    public Executor reportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 报表生成可能涉及大量数据处理，配置专门线程池
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(300);
        executor.setThreadNamePrefix("report-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(180);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);

        executor.initialize();
        return executor;
    }
}