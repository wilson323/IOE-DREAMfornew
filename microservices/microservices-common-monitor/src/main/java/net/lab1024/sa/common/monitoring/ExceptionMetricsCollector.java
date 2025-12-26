package net.lab1024.sa.common.monitoring;

import lombok.extern.slf4j.Slf4j;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IOE-DREAM 异常处理指标收集器
 * <p>
 * 内存优化版本，减少对象创建和内存占用
 * 纯Java类设计，通过构造函数注入依赖
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-16
 */
@Slf4j
public class ExceptionMetricsCollector {


    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicLong> exceptionCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> exceptionTimers = new ConcurrentHashMap<>();

    public ExceptionMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    private void initializeMetrics() {
        Counter.builder("system.exception.total")
                .description("系统异常总数")
                .register(meterRegistry);

        Counter.builder("business.exception.total")
                .description("业务异常总数")
                .register(meterRegistry);

        Counter.builder("param.exception.total")
                .description("参数异常总数")
                .register(meterRegistry);

        Counter.builder("general.exception.total")
                .description("通用异常总数")
                .register(meterRegistry);

        Timer.builder("system.exception.processing.time")
                .description("系统异常处理时间")
                .register(meterRegistry);

        Timer.builder("business.exception.processing.time")
                .description("业务异常处理时间")
                .register(meterRegistry);
    }

    public void recordSystemException(SystemException exception, long processingTimeMs) {
        String exceptionName = exception.getClass().getSimpleName();
        AtomicLong counter = exceptionCounters.computeIfAbsent(exceptionName, key -> new AtomicLong(0));
        counter.incrementAndGet();

        Timer timer = exceptionTimers.computeIfAbsent("system." + exceptionName, key ->
                Timer.builder("system.exception.processing.time")
                        .tag("type", exceptionName)
                        .description("系统异常处理时间")
                        .register(meterRegistry));

        timer.record(processingTimeMs, TimeUnit.MILLISECONDS);
        meterRegistry.counter("system.exception.total").increment();

        log.error("[系统异常] type={}, code={}, message={}, processingTime={}ms",
                exceptionName, exception.getCode(), exception.getMessage(), processingTimeMs);
    }

    public void recordBusinessException(BusinessException exception, long processingTimeMs) {
        String exceptionName = exception.getClass().getSimpleName();
        AtomicLong counter = exceptionCounters.computeIfAbsent(exceptionName, key -> new AtomicLong(0));
        counter.incrementAndGet();

        Timer timer = exceptionTimers.computeIfAbsent("business." + exceptionName, key ->
                Timer.builder("business.exception.processing.time")
                        .tag("type", exceptionName)
                        .description("业务异常处理时间")
                        .register(meterRegistry));

        timer.record(processingTimeMs, TimeUnit.MILLISECONDS);
        meterRegistry.counter("business.exception.total").increment();

        log.warn("[业务异常] type={}, code={}, message={}, processingTime={}ms",
                exceptionName, exception.getCode(), exception.getMessage(), processingTimeMs);
    }

    public void recordParamException(ParamException exception, long processingTimeMs) {
        String exceptionName = exception.getClass().getSimpleName();
        AtomicLong counter = exceptionCounters.computeIfAbsent(exceptionName, key -> new AtomicLong(0));
        counter.incrementAndGet();

        meterRegistry.counter("param.exception.total").increment();

        log.warn("[参数异常] type={}, code={}, message={}, processingTime={}ms",
                exceptionName, exception.getCode(), exception.getMessage(), processingTimeMs);
    }

    public void recordException(Exception exception, long processingTimeMs) {
        String exceptionName = exception.getClass().getSimpleName();
        AtomicLong counter = exceptionCounters.computeIfAbsent(exceptionName, key -> new AtomicLong(0));
        counter.incrementAndGet();

        Timer timer = exceptionTimers.computeIfAbsent("general." + exceptionName, key ->
                Timer.builder("general.exception.processing.time")
                        .tag("type", exceptionName)
                        .description("通用异常处理时间")
                        .register(meterRegistry));

        timer.record(processingTimeMs, TimeUnit.MILLISECONDS);
        meterRegistry.counter("general.exception.total").increment();

        log.error("[通用异常] type={}, message={}, processingTime={}ms",
                exceptionName, exception.getMessage(), processingTimeMs);
    }

    public long getTotalExceptionCount() {
        return exceptionCounters.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
    }

    public long getExceptionCount(String exceptionType) {
        AtomicLong counter = exceptionCounters.get(exceptionType);
        return counter != null ? counter.get() : 0;
    }

    private double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public void reset() {
        exceptionCounters.values().forEach(counter -> counter.set(0));
        log.info("[异常指标] 已重置所有异常计数器");
    }

    public ExceptionStatistics getStatistics() {
        return ExceptionStatistics.builder()
                .totalExceptions(getTotalExceptionCount())
                .systemExceptions(getExceptionCount("SystemException"))
                .businessExceptions(getExceptionCount("BusinessException"))
                .paramExceptions(getExceptionCount("ParamException"))
                .memoryUsage(getMemoryUsage())
                .build();
    }

    public static class ExceptionStatistics {
        private long totalExceptions;
        private long systemExceptions;
        private long businessExceptions;
        private long paramExceptions;
        private double memoryUsage;

        public static ExceptionStatisticsBuilder builder() {
            return new ExceptionStatisticsBuilder();
        }

        public long getTotalExceptions() {
            return totalExceptions;
        }

        public void setTotalExceptions(long totalExceptions) {
            this.totalExceptions = totalExceptions;
        }

        public long getSystemExceptions() {
            return systemExceptions;
        }

        public void setSystemExceptions(long systemExceptions) {
            this.systemExceptions = systemExceptions;
        }

        public long getBusinessExceptions() {
            return businessExceptions;
        }

        public void setBusinessExceptions(long businessExceptions) {
            this.businessExceptions = businessExceptions;
        }

        public long getParamExceptions() {
            return paramExceptions;
        }

        public void setParamExceptions(long paramExceptions) {
            this.paramExceptions = paramExceptions;
        }

        public double getMemoryUsage() {
            return memoryUsage;
        }

        public void setMemoryUsage(double memoryUsage) {
            this.memoryUsage = memoryUsage;
        }

        public static class ExceptionStatisticsBuilder {
            private long totalExceptions;
            private long systemExceptions;
            private long businessExceptions;
            private long paramExceptions;
            private double memoryUsage;

            public ExceptionStatisticsBuilder totalExceptions(long totalExceptions) {
                this.totalExceptions = totalExceptions;
                return this;
            }

            public ExceptionStatisticsBuilder systemExceptions(long systemExceptions) {
                this.systemExceptions = systemExceptions;
                return this;
            }

            public ExceptionStatisticsBuilder businessExceptions(long businessExceptions) {
                this.businessExceptions = businessExceptions;
                return this;
            }

            public ExceptionStatisticsBuilder paramExceptions(long paramExceptions) {
                this.paramExceptions = paramExceptions;
                return this;
            }

            public ExceptionStatisticsBuilder memoryUsage(double memoryUsage) {
                this.memoryUsage = memoryUsage;
                return this;
            }

            public ExceptionStatistics build() {
                ExceptionStatistics statistics = new ExceptionStatistics();
                statistics.totalExceptions = this.totalExceptions;
                statistics.systemExceptions = this.systemExceptions;
                statistics.businessExceptions = this.businessExceptions;
                statistics.paramExceptions = this.paramExceptions;
                statistics.memoryUsage = this.memoryUsage;
                return statistics;
            }
        }
    }
}

