package net.lab1024.sa.common.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 轻量级性能配置
 * <p>
 * 避免过度复杂的性能优化，只提供核心功能：
 * - 线程池优化
 * - 简单的性能监控
 * - 资源使用统计
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "performance.enabled", havingValue = "true", matchIfMissing = true)
public class LightPerformanceConfiguration {

    /**
     * 异步任务执行器
     */
    @Bean(name = "lightTaskExecutor")
    public Executor lightTaskExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                                // 核心线程数
            10,                               // 最大线程数
            60L,                              // 空闲时间
            TimeUnit.SECONDS,                 // 时间单位
            new LinkedBlockingQueue<>(100),    // 队列大小
            r -> {                            // 线程工厂
                Thread t = new Thread(r, "light-task-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );

        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("[线程池] 任务被拒绝，队列已满");
            // 简单的拒绝策略：由调用线程执行
            if (!executor1.isShutdown()) {
                r.run();
            }
        });

        return executor;
    }

    /**
     * 轻量级性能监控器
     */
    @Bean
    public LightPerformanceMonitor performanceMonitor() {
        return new LightPerformanceMonitor();
    }

    /**
     * 轻量级性能监控器
     */
    public static class LightPerformanceMonitor {

        private volatile long startTime = System.currentTimeMillis();

        /**
         * 记录方法执行时间
         */
        public <T> T timeExecution(String operation, java.util.function.Supplier<T> supplier) {
            long start = System.currentTimeMillis();
            try {
                T result = supplier.get();
                long duration = System.currentTimeMillis() - start;

                if (duration > 1000) { // 只记录超过1秒的操作
                    log.info("[性能监控] {} 执行时间: {}ms", operation, duration);
                }

                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - start;
                log.error("[性能监控] {} 执行失败，耗时: {}ms, 错误: {}", operation, duration, e.getMessage());
                throw e;
            }
        }

        /**
         * 获取系统运行时间
         */
        public long getUptime() {
            return System.currentTimeMillis() - startTime;
        }

        /**
         * 记录简单统计信息
         */
        public void logStats() {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            log.info("[性能统计] 运行时间: {}s, 内存使用: {}MB/{}MB ({:.1f}%)",
                getUptime() / 1000,
                usedMemory / 1024 / 1024,
                totalMemory / 1024 / 1024,
                (double) usedMemory / totalMemory * 100
            );
        }
    }

    /**
     * 性能监控注解
     */
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    public @interface PerformanceMonitor {
        String value() default "";
    }

    /**
     * 性能监控切面
     */
    @org.aspectj.lang.annotation.Aspect
    @org.springframework.stereotype.Component
    @ConditionalOnProperty(name = "performance.enabled", havingValue = "true", matchIfMissing = true)
    public static class PerformanceMonitorAspect {

        @org.springframework.beans.factory.annotation.Autowired
        private LightPerformanceMonitor performanceMonitor;

        @org.aspectj.lang.annotation.Around("@annotation(monitor)")
        public Object monitorPerformance(org.aspectj.lang.ProceedingJoinPoint joinPoint, PerformanceMonitor monitor) throws Throwable {
            String operation = monitor.value().isEmpty() ?
                joinPoint.getSignature().toShortString() : monitor.value();

            return performanceMonitor.timeExecution(operation, () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        }
    }
}