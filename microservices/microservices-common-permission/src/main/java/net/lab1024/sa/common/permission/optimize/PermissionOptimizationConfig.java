package net.lab1024.sa.common.permission.optimize;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 权限优化配置类
 * <p>
 * 配置权限验证优化相关的Bean
 * </p>
 * <p>
 * ⚠️ 线程池已废弃：所有线程池Bean已废弃，请使用 UnifiedThreadPoolConfiguration 中的统一线程池：
 * - coreExecutor: CPU密集型任务
 * - ioExecutor: IO密集型任务
 * - scheduledExecutor: 定时任务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 * @deprecated 线程池配置请使用 UnifiedThreadPoolConfiguration
 */
@Deprecated
@Configuration
@EnableAsync
public class PermissionOptimizationConfig {

    /**
     * 权限验证优化异步执行器
     */
    @Bean(name = "permissionOptimizationExecutor")
    public Executor permissionOptimizationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);

        // 最大线程数：CPU核心数 * 3（权限验证需要更多并发）
        int maxPoolSize = corePoolSize * 3;
        executor.setMaxPoolSize(maxPoolSize);

        // 队列容量：500
        executor.setQueueCapacity(500);

        // 线程名前缀
        executor.setThreadNamePrefix("permission-opt-");

        // 拒绝策略：调用者运行，确保权限验证不丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 线程空闲时间：60秒
        executor.setKeepAliveSeconds(60);

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间：30秒
        executor.setAwaitTerminationSeconds(30);

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();
        return executor;
    }

    /**
     * 权限预测异步执行器
     */
    @Bean(name = "permissionPredictionExecutor")
    public Executor permissionPredictionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 预测线程数较少，主要用于后台模型训练
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("permission-pred-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);

        executor.initialize();
        return executor;
    }

    /**
     * 热点数据更新异步执行器
     */
    @Bean(name = "hotDataUpdateExecutor")
    public Executor hotDataUpdateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 热点数据更新需要较高并发
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(200);

        executor.setThreadNamePrefix("hot-data-update-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(45);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);

        executor.initialize();
        return executor;
    }

    /**
     * 批量权限验证异步执行器
     */
    @Bean(name = "batchValidationExecutor")
    public Executor batchValidationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 批量验证需要更多线程
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("batch-validation-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);

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
        executor.setQueueCapacity(50);

        executor.setThreadNamePrefix("perf-monitor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);

        executor.initialize();
        return executor;
    }

    /**
     * 权限优化指标收集执行器
     */
    @Bean(name = "optimizationMetricsExecutor")
    public Executor optimizationMetricsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 指标收集线程
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(30);

        executor.setThreadNamePrefix("opt-metrics-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);

        executor.initialize();
        return executor;
    }
}
