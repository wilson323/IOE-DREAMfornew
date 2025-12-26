package net.lab1024.sa.common.util;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * 异常处理指标收集器
 * <p>
 * 简化版异常指标收集器，避免过度工程化
 * 提供基础的异常统计功能
 * 注意：纯Java类，不使用Spring注解，符合microservices-common-core定位
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
public class ExceptionMetricsCollector {

    private static final ExceptionMetricsCollector INSTANCE = new ExceptionMetricsCollector();

    private final Map<String, AtomicLong> exceptionCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCodeCounters = new ConcurrentHashMap<>();

    /**
     * 获取单例实例
     *
     * @return 异常指标收集器实例
     */
    public static ExceptionMetricsCollector getInstance() {
        return INSTANCE;
    }

    /**
     * 私有构造函数，防止外部实例化
     */
    private ExceptionMetricsCollector() {
    }

    /**
     * 记录异常指标
     *
     * @param exceptionType 异常类型
     * @param errorCode 错误码
     */
    public void recordExceptionMetrics(String exceptionType, String errorCode) {
        log.debug("[异常指标] 记录异常指标: type={}, code={}", exceptionType, errorCode);
        try {
            // 简化日志记录，避免依赖slf4j
            System.out.println("[异常指标] 记录异常指标: type=" + exceptionType + ", code=" + errorCode);

            // 本地计数器统计
            exceptionCounters.computeIfAbsent(exceptionType, k -> new AtomicLong(0)).incrementAndGet();
        } catch (Exception e) {
            System.err.println("[异常指标] 记录异常指标失败: type=" + exceptionType + ", code=" + errorCode + ", error=" + e.getMessage());
        }
    }

    /**
     * 获取异常总数统计
     *
     * @return 异常总数
     */
    public long getTotalExceptionCount() {
        return exceptionCounters.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
    }

    /**
     * 获取指定类型的异常数量
     *
     * @param exceptionType 异常类型
     * @return 异常数量
     */
    public long getExceptionCountByType(String exceptionType) {
        AtomicLong counter = exceptionCounters.get(exceptionType);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 获取指定错误码的异常数量
     *
     * @param errorCode 错误码
     * @return 异常数量
     */
    public long getExceptionCountByCode(String errorCode) {
        AtomicLong counter = errorCodeCounters.get(errorCode);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 获取异常统计摘要
     *
     * @return 异常统计摘要
     */
    public ExceptionMetricsSummary getExceptionMetricsSummary() {
        ExceptionMetricsSummary summary = new ExceptionMetricsSummary();
        summary.totalExceptions = getTotalExceptionCount();
        summary.exceptionTypeCounters = new ConcurrentHashMap<>(exceptionCounters);
        summary.errorCodeCounters = new ConcurrentHashMap<>(errorCodeCounters);
        summary.timestamp = LocalDateTime.now();
        return summary;
    }

    /**
     * 重置所有异常计数器
     */
    public void resetAllCounters() {
        log.info("[异常指标] 重置所有异常计数器");
        exceptionCounters.clear();
        errorCodeCounters.clear();
    }

    /**
     * 异常统计摘要
     */
    public static class ExceptionMetricsSummary {
        private Long totalExceptions;
        private Map<String, AtomicLong> exceptionTypeCounters;
        private Map<String, AtomicLong> errorCodeCounters;
        private LocalDateTime timestamp;

        // 默认构造函数
        public ExceptionMetricsSummary() {
        }

        // Getter和Setter方法
        public Long getTotalExceptions() {
            return totalExceptions;
        }

        public void setTotalExceptions(Long totalExceptions) {
            this.totalExceptions = totalExceptions;
        }

        public Map<String, AtomicLong> getExceptionTypeCounters() {
            return exceptionTypeCounters;
        }

        public void setExceptionTypeCounters(Map<String, AtomicLong> exceptionTypeCounters) {
            this.exceptionTypeCounters = exceptionTypeCounters;
        }

        public Map<String, AtomicLong> getErrorCodeCounters() {
            return errorCodeCounters;
        }

        public void setErrorCodeCounters(Map<String, AtomicLong> errorCodeCounters) {
            this.errorCodeCounters = errorCodeCounters;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
