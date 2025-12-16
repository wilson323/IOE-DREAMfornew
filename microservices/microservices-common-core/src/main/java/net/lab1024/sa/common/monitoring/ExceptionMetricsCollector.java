package net.lab1024.sa.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IOE-DREAM 异常处理指标收集器
 * <p>
 * 内存优化版本，减少对象创建和内存占用
 * 提供企业级异常处理监控指标
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class ExceptionMetricsCollector {

    private final MeterRegistry meterRegistry;

    // 内存优化：使用原子计数器避免对象创建
    private final AtomicLong businessExceptionCount = new AtomicLong(0);
    private final AtomicLong systemExceptionCount = new AtomicLong(0);
    private final AtomicLong paramExceptionCount = new AtomicLong(0);
    private final AtomicLong totalExceptionCount = new AtomicLong(0);

    // 异常类型计数器（内存优化：预定义常见异常类型）
    private final ConcurrentHashMap<String, AtomicLong> exceptionTypeCounters = new ConcurrentHashMap<>();

    // Micrometer指标
    private final Counter exceptionCounter;
    private final Counter businessExceptionCounter;
    private final Counter systemExceptionCounter;
    private final Counter paramExceptionCounter;
    private final Timer exceptionHandlingTimer;

    // 内存使用监控
    private final Gauge memoryUsageGauge;
    private final Gauge cacheSizeGauge;

    /**
     * 构造函数（Spring自动注入MeterRegistry）
     */
    public ExceptionMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化计数器
        this.exceptionCounter = Counter.builder("exceptions.total")
                .description("异常总数")
                .tag("application", getApplicationName())
                .register(meterRegistry);

        this.businessExceptionCounter = Counter.builder("exceptions.business")
                .description("业务异常数")
                .tag("application", getApplicationName())
                .register(meterRegistry);

        this.systemExceptionCounter = Counter.builder("exceptions.system")
                .description("系统异常数")
                .tag("application", getApplicationName())
                .register(meterRegistry);

        this.paramExceptionCounter = Counter.builder("exceptions.param")
                .description("参数异常数")
                .tag("application", getApplicationName())
                .register(meterRegistry);

        this.exceptionHandlingTimer = Timer.builder("exception.handling.time")
                .description("异常处理时间")
                .tag("application", getApplicationName())
                .register(meterRegistry);

        // 内存使用监控
        this.memoryUsageGauge = Gauge.builder("jvm.memory.exception.usage")
                .description("异常处理内存使用量")
                .tag("application", getApplicationName())
                .register(meterRegistry, this, ExceptionMetricsCollector::getMemoryUsage);

        this.cacheSizeGauge = Gauge.builder("exception.cache.size")
                .description("异常缓存大小")
                .tag("application", getApplicationName())
                .register(meterRegistry, this, ExceptionMetricsCollector::getCacheSize);

        log.info("ExceptionMetricsCollector初始化完成，应用: {}", getApplicationName());
    }

    /**
     * 记录异常（内存优化版本）
     *
     * @param exception 异常对象
     * @param handlingTime 处理时间（毫秒）
     */
    public void recordException(Exception exception, long handlingTime) {
        if (exception == null) {
            return;
        }

        // 记录异常类型
        String exceptionType = classifyException(exception);
        String exceptionCode = extractExceptionCode(exception);

        // 更新原子计数器（避免对象创建）
        totalExceptionCount.incrementAndGet();

        // 更新Micrometer指标
        exceptionCounter.increment(
            "type", exceptionType,
            "code", exceptionCode,
            "class", exception.getClass().getSimpleName()
        );

        // 记录处理时间
        exceptionHandlingTimer.record(handlingTime, java.util.concurrent.TimeUnit.MILLISECONDS);

        // 更新分类计数器
        if (exception instanceof BusinessException) {
            businessExceptionCount.incrementAndGet();
            businessExceptionCounter.increment("code", exceptionCode);
        } else if (exception instanceof SystemException) {
            systemExceptionCount.incrementAndGet();
            systemExceptionCounter.increment("code", exceptionCode);
        } else if (exception instanceof ParamException) {
            paramExceptionCount.incrementAndGet();
            paramExceptionCounter.increment("code", exceptionCode);
        }

        // 更新异常类型计数器
        exceptionTypeCounters.computeIfAbsent(exceptionType, k -> new AtomicLong(0))
                .incrementAndGet();

        // 记录关键异常日志（减少日志输出）
        if (isCriticalException(exceptionCode)) {
            log.warn("[关键异常] type={}, code={}, message={}", exceptionType, exceptionCode, exception.getMessage());
        }
    }

    /**
     * 分类异常类型（内存优化：避免字符串拼接）
     */
    private String classifyException(Exception exception) {
        if (exception instanceof BusinessException) {
            return "business";
        } else if (exception instanceof SystemException) {
            return "system";
        } else if (exception instanceof ParamException) {
            return "param";
        } else if (exception instanceof RuntimeException) {
            return "runtime";
        } else {
            return "other";
        }
    }

    /**
     * 提取异常码（内存优化）
     */
    private String extractExceptionCode(Exception exception) {
        if (exception instanceof BusinessException) {
            return ((BusinessException) exception).getCode();
        } else if (exception instanceof SystemException) {
            return ((SystemException) exception).getCode();
        } else if (exception instanceof ParamException) {
            return ((ParamException) exception).getCode();
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 判断是否为关键异常
     */
    private boolean isCriticalException(String exceptionCode) {
        return exceptionCode.contains("DATABASE") ||
               exceptionCode.contains("FILE_SYSTEM") ||
               exceptionCode.contains("MEMORY") ||
               exceptionCode.contains("NETWORK");
    }

    /**
     * 获取异常统计信息（内存优化）
     */
    public ExceptionStatistics getStatistics() {
        return ExceptionStatistics.builder()
                .totalExceptions(totalExceptionCount.get())
                .businessExceptions(businessExceptionCount.get())
                .systemExceptions(systemExceptionCount.get())
                .paramExceptions(paramExceptionCount.get())
                .build();
    }

    /**
     * 获取内存使用量（用于Gauge监控）
     */
    private double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0; // MB
    }

    /**
     * 获取缓存大小（用于Gauge监控）
     */
    private double getCacheSize() {
        return exceptionTypeCounters.size();
    }

    /**
     * 清理指标缓存（内存优化）
     */
    public void cleanup() {
        exceptionTypeCounters.clear();
        log.info("异常指标缓存已清理");
    }

    /**
     * 获取应用名称（内存优化：缓存结果）
     */
    private volatile String applicationName = null;

    private String getApplicationName() {
        if (applicationName == null) {
            synchronized (this) {
                if (applicationName == null) {
                    applicationName = System.getProperty("spring.application.name", "unknown");
                }
            }
        }
        return applicationName;
    }

    /**
     * 异常统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class ExceptionStatistics {
        private long totalExceptions;
        private long businessExceptions;
        private long systemExceptions;
        private long paramExceptions;

        public double getBusinessExceptionRate() {
            return totalExceptions > 0 ? (double) businessExceptions / totalExceptions * 100 : 0;
        }

        public double getSystemExceptionRate() {
            return totalExceptions > 0 ? (double) systemExceptions / totalExceptions * 100 : 0;
        }
    }
}