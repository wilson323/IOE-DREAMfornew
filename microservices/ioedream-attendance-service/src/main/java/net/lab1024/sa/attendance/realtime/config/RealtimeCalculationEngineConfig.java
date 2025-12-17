package net.lab1024.sa.attendance.realtime.config;

import net.lab1024.sa.attendance.realtime.RealtimeCalculationEngine;
import net.lab1024.sa.attendance.realtime.event.EventProcessor;
import net.lab1024.sa.attendance.realtime.event.impl.AttendanceEventProcessor;
import net.lab1024.sa.attendance.realtime.impl.RealtimeCalculationEngineImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 实时计算引擎配置类
 * <p>
 * 配置实时计算引擎相关的Bean和参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Configuration
public class RealtimeCalculationEngineConfig {

    /**
     * 实时计算引擎Bean
     *
     * @return 实时计算引擎实例
     */
    @Bean(name = "realtimeCalculationEngine")
    public RealtimeCalculationEngine realtimeCalculationEngine() {
        return new RealtimeCalculationEngineImpl();
    }

    /**
     * 考勤事件处理器Bean
     *
     * @return 考勤事件处理器实例
     */
    @Bean(name = "attendanceEventProcessor")
    public EventProcessor attendanceEventProcessor() {
        return new AttendanceEventProcessor();
    }

    /**
     * 事件处理线程池 - 使用ThreadPoolTaskExecutor
     *
     * @return 事件处理线程池
     */
    @Bean(name = "eventProcessingExecutor")
    public ThreadPoolTaskExecutor eventProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        // 最大线程数：CPU核心数 * 2
        int maximumPoolSize = corePoolSize * 2;
        // 队列容量
        int queueCapacity = 1000;
        
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maximumPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("realtime-event-processor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        return executor;
    }

    /**
     * 计算任务线程池 - 使用ThreadPoolTaskExecutor
     *
     * @return 计算任务线程池
     */
    @Bean(name = "calculationExecutor")
    public ThreadPoolTaskExecutor calculationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：CPU核心数 / 2（计算密集型）
        int corePoolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        // 最大线程数：CPU核心数
        int maximumPoolSize = Runtime.getRuntime().availableProcessors();
        
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maximumPoolSize);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("realtime-calculation-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        return executor;
    }

    /**
     * 定时任务线程池 - 使用ThreadPoolTaskExecutor(由于需要ScheduledExecutorService功能，这里可能依然使用Executors或使用统一的scheduledExecutor)
     * 注：对于ScheduledExecutorService，可以使用统一配置中的scheduledExecutor Bean
     *
     * @return 定时任务线程池
     */
    // 不再在这里创建,使用统一配置中的scheduledExecutor
    // 如果确实需要，可以通过@Resource(name="scheduledExecutor")注入

    /**
     * 实时计算引擎参数配置Bean
     *
     * @return 实时计算引擎参数配置
     */
    @Bean(name = "realtimeCalculationEngineParameters")
    public RealtimeCalculationEngineParameters realtimeCalculationEngineParameters() {
        return RealtimeCalculationEngineParameters.builder()
                // 基础配置
                .engineName("IOE-DREAM实时计算引擎")
                .engineVersion("1.0.0")
                .enabled(true)
                .debugMode(false)

                // 性能配置
                .maxConcurrentEvents(1000)
                .eventProcessingTimeoutSeconds(30)
                .calculationTimeoutSeconds(60)
                .batchProcessingSize(100)

                // 缓存配置
                .cacheEnabled(true)
                .cacheExpireMinutes(30)
                .maxCacheSize(10000)
                .cacheCleanupIntervalMinutes(60)

                // 监控配置
                .monitoringEnabled(true)
                .metricsCollectionIntervalSeconds(30)
                .performanceTrackingEnabled(true)
                .anomalyDetectionEnabled(true)

                // 队列配置
                .eventQueueCapacity(10000)
                .calculationQueueCapacity(5000)
                .queueMonitoringIntervalSeconds(10)

                // 重试配置
                .maxRetryAttempts(3)
                .retryDelaySeconds(5)
                .exponentialBackoffEnabled(true)

                // 存储配置
                .resultPersistenceEnabled(true)
                .eventPersistenceEnabled(true)
                .storageBatchSize(50)

                // 安全配置
                .dataValidationEnabled(true)
                .inputSanitizationEnabled(true)
                .auditLogEnabled(true)

                // 集成配置
                .externalIntegrationEnabled(true)
                .notificationEnabled(true)
                .alertingEnabled(true)

                .build();
    }

    /**
     * 实时计算引擎参数配置类
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RealtimeCalculationEngineParameters {

        // 基础配置
        private String engineName;
        private String engineVersion;
        private Boolean enabled;
        private Boolean debugMode;

        // 性能配置
        private Integer maxConcurrentEvents;
        private Integer eventProcessingTimeoutSeconds;
        private Integer calculationTimeoutSeconds;
        private Integer batchProcessingSize;

        // 缓存配置
        private Boolean cacheEnabled;
        private Integer cacheExpireMinutes;
        private Integer maxCacheSize;
        private Integer cacheCleanupIntervalMinutes;

        // 监控配置
        private Boolean monitoringEnabled;
        private Integer metricsCollectionIntervalSeconds;
        private Boolean performanceTrackingEnabled;
        private Boolean anomalyDetectionEnabled;

        // 队列配置
        private Integer eventQueueCapacity;
        private Integer calculationQueueCapacity;
        private Integer queueMonitoringIntervalSeconds;

        // 重试配置
        private Integer maxRetryAttempts;
        private Integer retryDelaySeconds;
        private Boolean exponentialBackoffEnabled;

        // 存储配置
        private Boolean resultPersistenceEnabled;
        private Boolean eventPersistenceEnabled;
        private Integer storageBatchSize;

        // 安全配置
        private Boolean dataValidationEnabled;
        private Boolean inputSanitizationEnabled;
        private Boolean auditLogEnabled;

        // 集成配置
        private Boolean externalIntegrationEnabled;
        private Boolean notificationEnabled;
        private Boolean alertingEnabled;
    }
}