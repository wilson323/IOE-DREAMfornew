package net.lab1024.sa.consume.performance;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.SystemException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 轻量级性能监控器
 *
 * 设计原则:
 * 1. 最小化性能开销 (<1% CPU)
 * 2. 内存占用可控 (<10MB)
 * 3. 简单有效的指标收集
 * 4. 避免复杂的外部依赖
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class LightweightPerformanceMonitor {

    // 性能指标存储 - 使用轻量级数据结构
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PerformanceTimer> timers = new ConcurrentHashMap<>();

    /**
     * 记录计数器
     */
    public void incrementCounter(String name) {
        counters.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 记录性能计时器
     */
    public void recordTime(String name, long duration) {
        PerformanceTimer timer = timers.computeIfAbsent(name, k -> new PerformanceTimer());
        timer.record(duration);
    }

    /**
     * 性能计时器包装器
     * <p>
     * 注意：Supplier只能抛出RuntimeException或Error，不能抛出检查异常
     * </p>
     */
    public <T> T timeExecution(String name, Supplier<T> supplier) {
        long startTime = System.nanoTime();
        try {
            T result = supplier.get();
            long duration = System.nanoTime() - startTime;
            recordTime(name, duration);
            return result;
        } catch (RuntimeException e) {
            log.debug("[性能监控] 执行异常: name={}, error={}", name, e.getMessage());
            incrementCounter(name + "_error");
            throw e;
        } catch (Error e) {
            incrementCounter(name + "_error");
            throw e;
        }
    }

    /**
     * 获取性能统计信息
     */
    public PerformanceStats getStats() {
        return PerformanceStats.builder()
                .counters(new ConcurrentHashMap<>(counters))
                .timers(getTimerStats())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 清空统计数据
     */
    public void reset() {
        counters.clear();
        timers.clear();
    }

    /**
     * 获取计时器统计
     */
    private ConcurrentHashMap<String, TimerStats> getTimerStats() {
        ConcurrentHashMap<String, TimerStats> stats = new ConcurrentHashMap<>();

        timers.forEach((name, timer) -> {
            TimerStats timerStats = TimerStats.builder()
                    .count(timer.getCount())
                    .totalTime(timer.getTotalTime())
                    .minTime(timer.getMinTime())
                    .maxTime(timer.getMaxTime())
                    .avgTime(timer.getCount() > 0 ? timer.getTotalTime() / timer.getCount() : 0)
                    .build();
            stats.put(name, timerStats);
        });

        return stats;
    }

    /**
     * 性能计时器内部类
     */
    private static class PerformanceTimer {
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private volatile long minTime = Long.MAX_VALUE;
        private volatile long maxTime = Long.MIN_VALUE;

        public void record(long duration) {
            count.incrementAndGet();
            totalTime.addAndGet(duration);

            // 更新最小值和最大值
            if (duration < minTime) {
                minTime = duration;
            }
            if (duration > maxTime) {
                maxTime = duration;
            }
        }

        public long getCount() { return count.get(); }
        public long getTotalTime() { return totalTime.get(); }
        public long getMinTime() { return minTime == Long.MAX_VALUE ? 0 : minTime; }
        public long getMaxTime() { return maxTime == Long.MIN_VALUE ? 0 : maxTime; }
    }

    /**
     * 性能统计DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PerformanceStats {
        private ConcurrentHashMap<String, AtomicLong> counters;
        private ConcurrentHashMap<String, TimerStats> timers;
        private LocalDateTime timestamp;

        public long getCounter(String name) {
            AtomicLong counter = counters.get(name);
            return counter != null ? counter.get() : 0;
        }

        public TimerStats getTimer(String name) {
            return timers.get(name);
        }
    }

    /**
     * 计时器统计DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TimerStats {
        private long count;
        private long totalTime;
        private long minTime;
        private long maxTime;
        private long avgTime;

        public double getAvgTimeMs() {
            return avgTime / 1_000_000.0; // 纳秒转毫秒
        }

        public double getMinTimeMs() {
            return minTime / 1_000_000.0;
        }

        public double getMaxTimeMs() {
            return maxTime / 1_000_000.0;
        }
    }

    /**
     * 性能监控注解
     */
    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface Monitored {
        String value() default "";
    }

    /**
     * 性能监控AOP切面
     */
    @org.aspectj.lang.annotation.Aspect
    @org.springframework.stereotype.Component
    public static class PerformanceAspect {

        private final LightweightPerformanceMonitor monitor;

        public PerformanceAspect(LightweightPerformanceMonitor monitor) {
            this.monitor = monitor;
        }

        @org.aspectj.lang.annotation.Around("@annotation(monitored)")
        public Object monitor(ProceedingJoinPoint joinPoint, Monitored monitored) throws Throwable {
            String name = monitored.value().isEmpty()
                    ? joinPoint.getSignature().getName()
                    : monitored.value();

            return monitor.timeExecution(name, () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    monitor.incrementCounter(name + "_exception");
                    // 将Throwable转换为RuntimeException，因为Supplier不能抛出检查异常
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else if (e instanceof Error) {
                        throw (Error) e;
                    } else {
                        throw new SystemException("PERFORMANCE_MONITOR_EXECUTION_ERROR", "性能监控执行异常: " + e.getMessage(), e);
                    }
                }
            });
        }
    }
}



