package net.lab1024.sa.common.permission.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 权限审计配置类
 * <p>
 * 配置权限审计相关的Bean，包括：
 * - 异步审计日志执行器配置
 * - 线程池参数优化
 * - 审计功能开关配置
 * - 性能监控配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Configuration
@EnableAsync
public class PermissionAuditConfig {

    /**
     * 审计日志异步执行器
     */
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);

        // 最大线程数：CPU核心数 * 2
        int maxPoolSize = corePoolSize * 2;
        executor.setMaxPoolSize(maxPoolSize);

        // 队列容量：200
        executor.setQueueCapacity(200);

        // 线程名前缀
        executor.setThreadNamePrefix("permission-audit-");

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 线程空闲时间：60秒
        executor.setKeepAliveSeconds(60);

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间：60秒
        executor.setAwaitTerminationSeconds(60);

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();
        return executor;
    }

    /**
     * 审计统计异步执行器
     */
    @Bean(name = "auditStatsExecutor")
    public Executor auditStatsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 统计线程数相对较小
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);

        executor.setThreadNamePrefix("permission-stats-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    /**
     * 性能监控异步执行器
     */
    @Bean(name = "performanceMonitorExecutor")
    public Executor performanceMonitorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 性能监控需要及时响应
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);

        executor.setThreadNamePrefix("perf-monitor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);

        executor.initialize();
        return executor;
    }
}