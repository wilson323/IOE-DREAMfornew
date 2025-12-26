package net.lab1024.sa.platform.core.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 异常处理指标收集器 - 重构版本
 * <p>
 * 解决原有ExceptionMetricsCollector的依赖混乱问题，提供简洁的异常统计功能
 * 避免过度工程化，专注于核心指标收集
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-22
 */
@Slf4j
public class ExceptionMetricsCollector {

    private final Map<String, AtomicLong> exceptionCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCodeCounters = new ConcurrentHashMap<>();

    /**
     * 记录异常指标
     *
     * @param exceptionType 异常类型
     * @param errorCode 错误码
     */
    public void recordException(String exceptionType, String errorCode) {
        try {
            log.debug("[异常指标] 记录异常指标: type={}, code={}", exceptionType, errorCode);

            // 本地计数器统计
            exceptionCounters.computeIfAbsent(exceptionType, k -> new AtomicLong(0)).incrementAndGet();
            errorCodeCounters.computeIfAbsent(errorCode, k -> new AtomicLong(0)).incrementAndGet();

        } catch (Exception e) {
            log.error("[异常指标] 记录异常指标失败: type={}, code={}, error={}",
                    exceptionType, errorCode, e.getMessage(), e);
        }
    }

    /**
     * 获取异常总数统计
     */
    public long getTotalExceptionCount() {
        return exceptionCounters.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
    }

    /**
     * 获取指定类型的异常数量
     */
    public long getExceptionCountByType(String exceptionType) {
        AtomicLong counter = exceptionCounters.get(exceptionType);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 获取指定错误码的异常数量
     */
    public long getExceptionCountByCode(String errorCode) {
        AtomicLong counter = errorCodeCounters.get(errorCode);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 获取异常统计摘要
     */
    public ExceptionMetricsSummary getExceptionMetricsSummary() {
        return ExceptionMetricsSummary.builder()
                .totalExceptions(getTotalExceptionCount())
                .exceptionTypeCounters(new ConcurrentHashMap<>(exceptionCounters))
                .errorCodeCounters(new ConcurrentHashMap<>(errorCodeCounters))
                .timestamp(LocalDateTime.now())
                .build();
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
     * 获取热门异常类型
     */
    public Map<String, Long> getTopExceptionTypes(int limit) {
        return exceptionCounters.entrySet().stream()
                .sorted(Map.Entry.<String, AtomicLong>comparingByValue((a, b) -> Long.compare(b.get(), a.get())))
                .limit(limit)
                .collect(ConcurrentHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().get()), ConcurrentHashMap::putAll);
    }

    /**
     * 获取热门错误码
     */
    public Map<String, Long> getTopErrorCodes(int limit) {
        return errorCodeCounters.entrySet().stream()
                .sorted(Map.Entry.<String, AtomicLong>comparingByValue((a, b) -> Long.compare(b.get(), a.get())))
                .limit(limit)
                .collect(ConcurrentHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().get()), ConcurrentHashMap::putAll);
    }

    /**
     * 异常统计摘要
     */
    @lombok.Data
    @lombok.Builder
    public static class ExceptionMetricsSummary {
        private Long totalExceptions;
        private Map<String, AtomicLong> exceptionTypeCounters;
        private Map<String, AtomicLong> errorCodeCounters;
        private LocalDateTime timestamp;
    }
}