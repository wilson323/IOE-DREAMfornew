package net.lab1024.sa.common.monitor.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 性能指标收集器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * 负责收集应用各层次的性能指标
 * </p>
 * <p>
 * 核心职责：
 * - HTTP请求指标收集
 * - 数据库查询指标收集
 * - 缓存命中率统计
 * - 方法执行时间统计
 * - 外部服务调用统计
 * - 性能瓶颈识别
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class PerformanceMetricsCollector {

    // HTTP请求指标
    private final Map<String, HttpMetrics> httpMetricsMap = new ConcurrentHashMap<>();

    // 数据库查询指标
    private final Map<String, DatabaseMetrics> databaseMetricsMap = new ConcurrentHashMap<>();

    // 缓存指标
    private final CacheMetrics cacheMetrics = new CacheMetrics();

    // 方法执行时间指标
    private final Map<String, MethodMetrics> methodMetricsMap = new ConcurrentHashMap<>();

    // 外部服务调用指标
    private final Map<String, ExternalServiceMetrics> serviceMetricsMap = new ConcurrentHashMap<>();

    // 慢查询阈值
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;
    private static final long VERY_SLOW_QUERY_THRESHOLD_MS = 3000;
    private static final long SLOW_API_THRESHOLD_MS = 2000;

    /**
     * 记录HTTP请求指标
     *
     * @param endpoint    API端点
     * @param method      HTTP方法
     * @param statusCode  响应状态码
     * @param duration    执行时长（毫秒）
     */
    public void recordHttpRequest(String endpoint, String method, int statusCode, long duration) {
        String key = method + ":" + endpoint;

        HttpMetrics metrics = httpMetricsMap.computeIfAbsent(key, k -> new HttpMetrics(key));
        metrics.recordRequest(statusCode, duration);

        // 慢API告警
        if (duration > SLOW_API_THRESHOLD_MS) {
            log.warn("[性能指标] 检测到慢API: endpoint={}, duration={}ms", endpoint, duration);
            metrics.incrementSlowCount();
        }
    }

    /**
     * 记录数据库查询指标
     *
     * @param sql     SQL语句（或方法名）
     * @param success 是否成功
     * @param duration 执行时长（毫秒）
     */
    public void recordDatabaseQuery(String sql, boolean success, long duration) {
        String key = sql;

        DatabaseMetrics metrics = databaseMetricsMap.computeIfAbsent(key, k -> new DatabaseMetrics(key));
        metrics.recordQuery(success, duration);

        // 慢查询告警
        if (duration > SLOW_QUERY_THRESHOLD_MS) {
            log.warn("[性能指标] 检测到慢查询: duration={}ms, sql={}", duration, sql);
            metrics.incrementSlowCount();
        }

        // 极慢查询告警
        if (duration > VERY_SLOW_QUERY_THRESHOLD_MS) {
            log.error("[性能指标] 检测到极慢查询: duration={}ms, sql={}", duration, sql);
            metrics.incrementVerySlowCount();
        }
    }

    /**
     * 记录缓存命中
     */
    public void recordCacheHit(String cacheName) {
        cacheMetrics.recordHit(cacheName);
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss(String cacheName) {
        cacheMetrics.recordMiss(cacheName);
    }

    /**
     * 记录方法执行时间
     *
     * @param className  类名
     * @param methodName 方法名
     * @param duration   执行时长（毫秒）
     */
    public void recordMethodExecution(String className, String methodName, long duration) {
        String key = className + "." + methodName;

        MethodMetrics metrics = methodMetricsMap.computeIfAbsent(key, k -> new MethodMetrics(key));
        metrics.recordExecution(duration);
    }

    /**
     * 记录外部服务调用
     *
     * @param serviceName 服务名称
     * @param success    是否成功
     * @param duration   执行时长（毫秒）
     */
    public void recordExternalServiceCall(String serviceName, boolean success, long duration) {
        ExternalServiceMetrics metrics = serviceMetricsMap.computeIfAbsent(
                serviceName,
                k -> new ExternalServiceMetrics(serviceName)
        );
        metrics.recordCall(success, duration);
    }

    /**
     * 获取HTTP请求指标摘要
     */
    public Map<String, Object> getHttpMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();

        long totalRequests = 0;
        long totalErrors = 0;
        long totalSlow = 0;
        double avgDuration = 0;

        for (HttpMetrics metrics : httpMetricsMap.values()) {
            totalRequests += metrics.getRequestCount();
            totalErrors += metrics.getErrorCount();
            totalSlow += metrics.getSlowCount();
            avgDuration += metrics.getTotalDuration();
        }

        if (!httpMetricsMap.isEmpty()) {
            avgDuration /= httpMetricsMap.size();
        }

        summary.put("totalRequests", totalRequests);
        summary.put("totalErrors", totalErrors);
        summary.put("errorRate", totalRequests > 0 ? (totalErrors * 100.0 / totalRequests) : 0);
        summary.put("totalSlow", totalSlow);
        summary.put("avgDuration", avgDuration);
        summary.put("endpointCount", httpMetricsMap.size());

        return summary;
    }

    /**
     * 获取数据库查询指标摘要
     */
    public Map<String, Object> getDatabaseMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();

        long totalQueries = 0;
        long totalErrors = 0;
        long totalSlow = 0;
        long totalVerySlow = 0;
        double avgDuration = 0;

        for (DatabaseMetrics metrics : databaseMetricsMap.values()) {
            totalQueries += metrics.getQueryCount();
            totalErrors += metrics.getErrorCount();
            totalSlow += metrics.getSlowCount();
            totalVerySlow += metrics.getVerySlowCount();
            avgDuration += metrics.getTotalDuration();
        }

        if (!databaseMetricsMap.isEmpty()) {
            avgDuration /= databaseMetricsMap.size();
        }

        summary.put("totalQueries", totalQueries);
        summary.put("totalErrors", totalErrors);
        summary.put("errorRate", totalQueries > 0 ? (totalErrors * 100.0 / totalQueries) : 0);
        summary.put("totalSlow", totalSlow);
        summary.put("totalVerySlow", totalVerySlow);
        summary.put("slowRate", totalQueries > 0 ? (totalSlow * 100.0 / totalQueries) : 0);
        summary.put("avgDuration", avgDuration);
        summary.put("queryTypeCount", databaseMetricsMap.size());

        return summary;
    }

    /**
     * 获取缓存指标摘要
     */
    public Map<String, Object> getCacheMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();

        long totalHits = cacheMetrics.getTotalHits();
        long totalMisses = cacheMetrics.getTotalMisses();
        long totalRequests = totalHits + totalMisses;

        double hitRate = totalRequests > 0 ? (totalHits * 100.0 / totalRequests) : 0;

        summary.put("totalHits", totalHits);
        summary.put("totalMisses", totalMisses);
        summary.put("totalRequests", totalRequests);
        summary.put("hitRate", hitRate);
        summary.put("cacheCount", cacheMetrics.getCacheCount());

        // 各缓存命中率
        Map<String, Double> cacheHitRates = new HashMap<>();
        for (Map.Entry<String, CacheStatistics> entry : cacheMetrics.getCacheStatistics().entrySet()) {
            String cacheName = entry.getKey();
            CacheStatistics stats = entry.getValue();
            long cacheRequests = stats.getHits() + stats.getMisses();
            double cacheHitRate = cacheRequests > 0 ? (stats.getHits() * 100.0 / cacheRequests) : 0;
            cacheHitRates.put(cacheName, cacheHitRate);
        }
        summary.put("cacheHitRates", cacheHitRates);

        return summary;
    }

    /**
     * 获取方法执行指标摘要
     */
    public Map<String, Object> getMethodMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();

        long totalExecutions = 0;
        double avgDuration = 0;

        for (MethodMetrics metrics : methodMetricsMap.values()) {
            totalExecutions += metrics.getExecutionCount();
            avgDuration += metrics.getAvgDuration();
        }

        if (!methodMetricsMap.isEmpty()) {
            avgDuration /= methodMetricsMap.size();
        }

        summary.put("totalExecutions", totalExecutions);
        summary.put("avgDuration", avgDuration);
        summary.put("methodCount", methodMetricsMap.size());

        // 找出最慢的5个方法
        List<Map.Entry<String, MethodMetrics>> sortedMethods = methodMetricsMap.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getAvgDuration(), e1.getValue().getAvgDuration()))
                .limit(5)
                .collect(Collectors.toList());

        List<Map<String, Object>> slowestMethods = new ArrayList<>();
        for (Map.Entry<String, MethodMetrics> entry : sortedMethods) {
            Map<String, Object> methodInfo = new HashMap<>();
            methodInfo.put("method", entry.getKey());
            methodInfo.put("avgDuration", entry.getValue().getAvgDuration());
            methodInfo.put("executionCount", entry.getValue().getExecutionCount());
            methodInfo.put("maxDuration", entry.getValue().getMaxDuration());
            slowestMethods.add(methodInfo);
        }
        summary.put("slowestMethods", slowestMethods);

        return summary;
    }

    /**
     * 获取外部服务调用指标摘要
     */
    public Map<String, Object> getExternalServiceMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();

        long totalCalls = 0;
        long totalErrors = 0;
        double avgDuration = 0;

        for (ExternalServiceMetrics metrics : serviceMetricsMap.values()) {
            totalCalls += metrics.getCallCount();
            totalErrors += metrics.getErrorCount();
            avgDuration += metrics.getAvgDuration();
        }

        if (!serviceMetricsMap.isEmpty()) {
            avgDuration /= serviceMetricsMap.size();
        }

        summary.put("totalCalls", totalCalls);
        summary.put("totalErrors", totalErrors);
        summary.put("errorRate", totalCalls > 0 ? (totalErrors * 100.0 / totalCalls) : 0);
        summary.put("avgDuration", avgDuration);
        summary.put("serviceCount", serviceMetricsMap.size());

        return summary;
    }

    /**
     * 获取完整性能报告
     */
    public PerformanceReport getFullReport() {
        PerformanceReport report = new PerformanceReport();
        report.setGeneratedAt(LocalDateTime.now());

        report.setHttpMetrics(getHttpMetricsSummary());
        report.setDatabaseMetrics(getDatabaseMetricsSummary());
        report.setCacheMetrics(getCacheMetricsSummary());
        report.setMethodMetrics(getMethodMetricsSummary());
        report.setExternalServiceMetrics(getExternalServiceMetricsSummary());

        // 识别性能瓶颈
        report.setPerformanceBottlenecks(identifyBottlenecks());

        return report;
    }

    /**
     * 识别性能瓶颈
     */
    private List<String> identifyBottlenecks() {
        List<String> bottlenecks = new ArrayList<>();

        // 检查慢查询
        Map<String, Object> dbSummary = getDatabaseMetricsSummary();
        long totalSlow = (Long) dbSummary.getOrDefault("totalSlow", 0L);
        double slowRate = (Double) dbSummary.getOrDefault("slowRate", 0.0);

        if (totalSlow > 100 || slowRate > 10) {
            bottlenecks.add(String.format("数据库慢查询过多: 慢查询数=%d, 慢查询率=%.2f%%", totalSlow, slowRate));
        }

        // 检查缓存命中率
        Map<String, Object> cacheSummary = getCacheMetricsSummary();
        Double hitRate = (Double) cacheSummary.getOrDefault("hitRate", 0.0);

        if (hitRate < 70) {
            bottlenecks.add(String.format("缓存命中率过低: 命中率=%.2f%%", hitRate));
        }

        // 检查HTTP错误率
        Map<String, Object> httpSummary = getHttpMetricsSummary();
        Double errorRate = (Double) httpSummary.getOrDefault("errorRate", 0.0);

        if (errorRate > 5) {
            bottlenecks.add(String.format("HTTP错误率过高: 错误率=%.2f%%", errorRate));
        }

        // 检查外部服务错误率
        Map<String, Object> serviceSummary = getExternalServiceMetricsSummary();
        Double serviceErrorRate = (Double) serviceSummary.getOrDefault("errorRate", 0.0);

        if (serviceErrorRate > 10) {
            bottlenecks.add(String.format("外部服务错误率过高: 错误率=%.2f%%", serviceErrorRate));
        }

        return bottlenecks;
    }

    /**
     * 清除所有指标
     */
    public void clearAllMetrics() {
        log.info("[性能指标] 清除所有性能指标");
        httpMetricsMap.clear();
        databaseMetricsMap.clear();
        cacheMetrics.clear();
        methodMetricsMap.clear();
        serviceMetricsMap.clear();
    }

    // ==================== 内部类 ====================

    /**
     * HTTP请求指标
     */
    public static class HttpMetrics {
        private final String endpoint;
        private final AtomicLong requestCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong slowCount = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);

        public HttpMetrics(String endpoint) {
            this.endpoint = endpoint;
        }

        public void recordRequest(int statusCode, long duration) {
            requestCount.incrementAndGet();
            totalDuration.addAndGet(duration);

            if (statusCode >= 400) {
                errorCount.incrementAndGet();
            }
        }

        public void incrementSlowCount() {
            slowCount.incrementAndGet();
        }

        public long getRequestCount() {
            return requestCount.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public long getSlowCount() {
            return slowCount.get();
        }

        public long getTotalDuration() {
            return totalDuration.get();
        }
    }

    /**
     * 数据库查询指标
     */
    public static class DatabaseMetrics {
        private final String sql;
        private final AtomicLong queryCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong slowCount = new AtomicLong(0);
        private final AtomicLong verySlowCount = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);

        public DatabaseMetrics(String sql) {
            this.sql = sql;
        }

        public void recordQuery(boolean success, long duration) {
            queryCount.incrementAndGet();
            totalDuration.addAndGet(duration);

            if (!success) {
                errorCount.incrementAndGet();
            }
        }

        public void incrementSlowCount() {
            slowCount.incrementAndGet();
        }

        public void incrementVerySlowCount() {
            verySlowCount.incrementAndGet();
        }

        public long getQueryCount() {
            return queryCount.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public long getSlowCount() {
            return slowCount.get();
        }

        public long getVerySlowCount() {
            return verySlowCount.get();
        }

        public long getTotalDuration() {
            return totalDuration.get();
        }
    }

    /**
     * 缓存指标
     */
    public static class CacheMetrics {
        private final AtomicLong totalHits = new AtomicLong(0);
        private final AtomicLong totalMisses = new AtomicLong(0);
        private final Map<String, CacheStatistics> cacheStatistics = new ConcurrentHashMap<>();

        public void recordHit(String cacheName) {
            totalHits.incrementAndGet();
            cacheStatistics.computeIfAbsent(cacheName, k -> new CacheStatistics()).recordHit();
        }

        public void recordMiss(String cacheName) {
            totalMisses.incrementAndGet();
            cacheStatistics.computeIfAbsent(cacheName, k -> new CacheStatistics()).recordMiss();
        }

        public long getTotalHits() {
            return totalHits.get();
        }

        public long getTotalMisses() {
            return totalMisses.get();
        }

        public int getCacheCount() {
            return cacheStatistics.size();
        }

        public Map<String, CacheStatistics> getCacheStatistics() {
            return cacheStatistics;
        }

        public void clear() {
            totalHits.set(0);
            totalMisses.set(0);
            cacheStatistics.clear();
        }
    }

    /**
     * 缓存统计
     */
    public static class CacheStatistics {
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);

        public void recordHit() {
            hits.incrementAndGet();
        }

        public void recordMiss() {
            misses.incrementAndGet();
        }

        public long getHits() {
            return hits.get();
        }

        public long getMisses() {
            return misses.get();
        }
    }

    /**
     * 方法执行指标
     */
    public static class MethodMetrics {
        private final String method;
        private final AtomicLong executionCount = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong maxDuration = new AtomicLong(0);

        public MethodMetrics(String method) {
            this.method = method;
        }

        public void recordExecution(long duration) {
            executionCount.incrementAndGet();
            totalDuration.addAndGet(duration);

            // 更新最大值
            long currentMax = maxDuration.get();
            while (duration > currentMax && !maxDuration.compareAndSet(currentMax, duration)) {
                currentMax = maxDuration.get();
            }
        }

        public long getExecutionCount() {
            return executionCount.get();
        }

        public double getAvgDuration() {
            long count = executionCount.get();
            return count > 0 ? (double) totalDuration.get() / count : 0;
        }

        public long getMaxDuration() {
            return maxDuration.get();
        }
    }

    /**
     * 外部服务调用指标
     */
    public static class ExternalServiceMetrics {
        private final String serviceName;
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);

        public ExternalServiceMetrics(String serviceName) {
            this.serviceName = serviceName;
        }

        public void recordCall(boolean success, long duration) {
            callCount.incrementAndGet();
            totalDuration.addAndGet(duration);

            if (!success) {
                errorCount.incrementAndGet();
            }
        }

        public long getCallCount() {
            return callCount.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public double getAvgDuration() {
            long count = callCount.get();
            return count > 0 ? (double) totalDuration.get() / count : 0;
        }
    }

    /**
     * 性能报告
     */
    public static class PerformanceReport {
        private LocalDateTime generatedAt;
        private Map<String, Object> httpMetrics;
        private Map<String, Object> databaseMetrics;
        private Map<String, Object> cacheMetrics;
        private Map<String, Object> methodMetrics;
        private Map<String, Object> externalServiceMetrics;
        private List<String> performanceBottlenecks;

        // Getters and Setters
        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
        }

        public Map<String, Object> getHttpMetrics() {
            return httpMetrics;
        }

        public void setHttpMetrics(Map<String, Object> httpMetrics) {
            this.httpMetrics = httpMetrics;
        }

        public Map<String, Object> getDatabaseMetrics() {
            return databaseMetrics;
        }

        public void setDatabaseMetrics(Map<String, Object> databaseMetrics) {
            this.databaseMetrics = databaseMetrics;
        }

        public Map<String, Object> getCacheMetrics() {
            return cacheMetrics;
        }

        public void setCacheMetrics(Map<String, Object> cacheMetrics) {
            this.cacheMetrics = cacheMetrics;
        }

        public Map<String, Object> getMethodMetrics() {
            return methodMetrics;
        }

        public void setMethodMetrics(Map<String, Object> methodMetrics) {
            this.methodMetrics = methodMetrics;
        }

        public Map<String, Object> getExternalServiceMetrics() {
            return externalServiceMetrics;
        }

        public void setExternalServiceMetrics(Map<String, Object> externalServiceMetrics) {
            this.externalServiceMetrics = externalServiceMetrics;
        }

        public List<String> getPerformanceBottlenecks() {
            return performanceBottlenecks;
        }

        public void setPerformanceBottlenecks(List<String> performanceBottlenecks) {
            this.performanceBottlenecks = performanceBottlenecks;
        }
    }
}
