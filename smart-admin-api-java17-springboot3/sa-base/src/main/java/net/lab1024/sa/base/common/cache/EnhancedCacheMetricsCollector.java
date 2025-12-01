package net.lab1024.sa.base.common.cache;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 增强版缓存指标收集器
 * <p>
 * 严格遵循repowiki缓存架构规范：
 * - 详细的性能指标收集
 * - 多维度统计分析
 * - 实时健康状态监控
 * - 业务数据指标追踪
 * - 自动化报表生成
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class EnhancedCacheMetricsCollector {

    // 全局计数器
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalHits = new AtomicLong(0);
    private final AtomicLong totalMisses = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalSets = new AtomicLong(0);
    private final AtomicLong totalDeletes = new AtomicLong(0);

    // 响应时间统计
    private volatile ResponseTimeStats responseTimeStats = new ResponseTimeStats(Long.MAX_VALUE, 0, 0, 0);

    // 命名空间指标
    private final Map<String, NamespaceMetrics> namespaceMetricsMap = new ConcurrentHashMap<>();

    // 业务数据指标
    private final Map<String, BusinessDataMetrics> businessDataMetricsMap = new ConcurrentHashMap<>();

    // 健康状态历史
    private final Queue<HealthStatusSnapshot> healthHistory = new LinkedList<>();

    // 错误记录
    private final Queue<ErrorRecord> recentErrors = new LinkedList<>();

    @Resource
    private UnifiedCacheManager cacheManager;

    // ========== 指标记录方法 ==========

    /**
     * 记录缓存命中
     */
    public void recordHit(CacheNamespace namespace) {
        totalRequests.incrementAndGet();
        totalHits.incrementAndGet();

        String namespaceKey = namespace.getPrefix();
        NamespaceMetrics metrics = namespaceMetricsMap.computeIfAbsent(namespaceKey,
                k -> new NamespaceMetrics(namespaceKey));
        metrics.recordHit();
    }

    /**
     * 记录缓存命中（带响应时间）
     * 兼容CacheMetricsCollector接口
     */
    public void recordHit(CacheNamespace namespace, long responseTime) {
        recordHit(namespace);
        recordResponseTime(responseTime);
    }

    /**
     * 记录缓存未命中
     */
    public void recordMiss(CacheNamespace namespace) {
        totalRequests.incrementAndGet();
        totalMisses.incrementAndGet();

        String namespaceKey = namespace.getPrefix();
        NamespaceMetrics metrics = namespaceMetricsMap.computeIfAbsent(namespaceKey,
                k -> new NamespaceMetrics(namespaceKey));
        metrics.recordMiss();
    }

    /**
     * 记录缓存设置
     */
    public void recordSet(CacheNamespace namespace, long ttlSeconds) {
        totalSets.incrementAndGet();

        String namespaceKey = namespace.getPrefix();
        NamespaceMetrics metrics = namespaceMetricsMap.computeIfAbsent(namespaceKey,
                k -> new NamespaceMetrics(namespaceKey));
        metrics.recordSet(ttlSeconds);
        // ttlSeconds 是缓存过期时间，不应该记录为响应时间
    }

    /**
     * 记录缓存设置（带响应时间）
     * 兼容CacheMetricsCollector接口
     */
    public void recordSet(CacheNamespace namespace, long ttlSeconds, long responseTime) {
        recordSet(namespace, ttlSeconds);
        recordResponseTime(responseTime);
    }

    /**
     * 记录缓存删除
     */
    public void recordDelete(CacheNamespace namespace) {
        totalDeletes.incrementAndGet();

        String namespaceKey = namespace.getPrefix();
        NamespaceMetrics metrics = namespaceMetricsMap.computeIfAbsent(namespaceKey,
                k -> new NamespaceMetrics(namespaceKey));
        metrics.recordDelete();
    }

    /**
     * 记录缓存删除（批量）
     */
    public void recordDelete(CacheNamespace namespace, int count) {
        totalDeletes.addAndGet(count);

        String namespaceKey = namespace.getPrefix();
        NamespaceMetrics metrics = namespaceMetricsMap.computeIfAbsent(namespaceKey,
                k -> new NamespaceMetrics(namespaceKey));
        metrics.recordBatchDelete(count);
    }

    /**
     * 记录缓存清空
     */
    public void recordClear(CacheNamespace namespace) {
        String namespaceKey = namespace.getPrefix();
        NamespaceMetrics metrics = namespaceMetricsMap.computeIfAbsent(namespaceKey,
                k -> new NamespaceMetrics(namespaceKey));
        metrics.recordClear();
    }

    /**
     * 记录缓存获取请求
     */
    public void recordGet(CacheNamespace namespace) {
        String namespaceKey = namespace.getPrefix();
        NamespaceMetrics metrics = namespaceMetricsMap.computeIfAbsent(namespaceKey,
                k -> new NamespaceMetrics(namespaceKey));
        metrics.recordGet();
    }

    /**
     * 记录错误
     */
    public void recordError(CacheNamespace namespace) {
        totalRequests.incrementAndGet();
        totalErrors.incrementAndGet();

        String namespaceKey = namespace.getPrefix();
        NamespaceMetrics metrics = namespaceMetricsMap.computeIfAbsent(namespaceKey,
                k -> new NamespaceMetrics(namespaceKey));
        metrics.recordError();

        // 记录错误详情
        synchronized (recentErrors) {
            recentErrors.offer(new ErrorRecord(namespaceKey, System.currentTimeMillis()));
            if (recentErrors.size() > 100) {
                recentErrors.poll();
            }
        }
    }

    /**
     * 记录业务数据访问
     */
    public void recordBusinessDataAccess(BusinessDataType dataType, long dataSize, boolean hit) {
        BusinessDataMetrics metrics = getBusinessDataMetrics(dataType);
        metrics.recordAccess(dataSize, hit);
    }

    /**
     * 记录响应时间
     */
    public void recordResponseTime(long responseTimeMillis) {
        updateResponseTimeStats(responseTimeMillis);
    }

    // ========== 指标查询方法 ==========

    /**
     * 获取总体指标
     */
    public GlobalMetrics getGlobalMetrics() {
        return new GlobalMetrics(
                totalRequests.get(),
                totalHits.get(),
                totalMisses.get(),
                totalErrors.get(),
                totalSets.get(),
                totalDeletes.get(),
                calculateGlobalHitRate(),
                calculateGlobalErrorRate(),
                responseTimeStats);
    }

    /**
     * 获取命名空间指标
     */
    public Map<String, NamespaceMetrics> getNamespaceMetrics() {
        return new HashMap<>(namespaceMetricsMap);
    }

    /**
     * 获取业务数据指标
     */
    public Map<String, BusinessDataMetrics> getBusinessDataMetrics() {
        return new HashMap<>(businessDataMetricsMap);
    }

    /**
     * 获取健康状态
     */
    public HealthStatus getHealthStatus() {
        double healthScore = calculateGlobalHealthScore();
        return new HealthStatus(healthScore, getHealthLevel(healthScore), LocalDateTime.now());
    }

    /**
     * 获取错误统计
     */
    public ErrorStatistics getErrorStatistics() {
        return new ErrorStatistics(
                totalErrors.get(),
                calculateGlobalErrorRate(),
                new ArrayList<>(recentErrors));
    }

    /**
     * 获取健康评估
     * 兼容CacheMetricsCollector接口
     */
    public Map<String, Object> getHealthAssessment() {
        Map<String, Object> assessment = new HashMap<>();
        HealthStatus healthStatus = getHealthStatus();

        assessment.put("globalHealthScore", healthStatus.getScore());
        assessment.put("globalHealthLevel", healthStatus.getLevel());
        assessment.put("assessmentTime", healthStatus.getTimestamp());

        return assessment;
    }

    /**
     * 获取所有指标
     */
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> allMetrics = new HashMap<>();

        // 全局指标
        GlobalMetrics globalMetrics = getGlobalMetrics();
        allMetrics.put("global", Map.of(
                "totalRequests", globalMetrics.getTotalRequests(),
                "totalHits", globalMetrics.getTotalHits(),
                "totalMisses", globalMetrics.getTotalMisses(),
                "totalErrors", globalMetrics.getTotalErrors(),
                "hitRate", globalMetrics.getHitRate(),
                "errorRate", globalMetrics.getErrorRate(),
                "avgResponseTime", calculateAverageResponseTime()));

        // 命名空间指标
        Map<String, Object> namespaceData = new HashMap<>();
        namespaceMetricsMap.forEach((key, metrics) -> {
            namespaceData.put(key, Map.of(
                    "requests", metrics.getRequests(),
                    "hits", metrics.getHits(),
                    "misses", metrics.getMisses(),
                    "hitRate", metrics.getHitRate(),
                    "avgResponseTime", metrics.getAvgResponseTime()));
        });
        allMetrics.put("namespaces", namespaceData);

        // 业务数据指标
        Map<String, Object> businessData = new HashMap<>();
        businessDataMetricsMap.forEach((key, metrics) -> {
            businessData.put(key, Map.of(
                    "totalAccesses", metrics.getTotalAccesses(),
                    "hits", metrics.getHits(),
                    "misses", metrics.getMisses(),
                    "hitRate", metrics.getHitRate(),
                    "totalDataSize", metrics.getTotalDataSize()));
        });
        allMetrics.put("businessData", businessData);

        // 健康状态
        HealthStatus healthStatus = getHealthStatus();
        allMetrics.put("health", Map.of(
                "score", healthStatus.getScore(),
                "level", healthStatus.getLevel(),
                "timestamp", healthStatus.getTimestamp()));

        return allMetrics;
    }

    // ========== 私有方法 ==========

    private BusinessDataMetrics getBusinessDataMetrics(BusinessDataType dataType) {
        return businessDataMetricsMap.computeIfAbsent(dataType.name(), k -> new BusinessDataMetrics(dataType));
    }

    private void updateResponseTimeStats(long responseTime) {
        responseTimeStats = new ResponseTimeStats(
                Math.min(responseTimeStats.getMinTime(), responseTime),
                Math.max(responseTimeStats.getMaxTime(), responseTime),
                responseTimeStats.getTotalTime() + responseTime,
                responseTimeStats.getCount() + 1);
    }

    private double calculateGlobalHitRate() {
        long requests = totalRequests.get();
        return requests > 0 ? (double) totalHits.get() / requests : 0.0;
    }

    private double calculateGlobalErrorRate() {
        long requests = totalRequests.get();
        return requests > 0 ? (double) totalErrors.get() / requests : 0.0;
    }

    private double calculateAverageResponseTime() {
        long count = responseTimeStats.getCount();
        return count > 0 ? (double) responseTimeStats.getTotalTime() / count : 0.0;
    }

    private double calculateGlobalHealthScore() {
        long requests = totalRequests.get();
        if (requests == 0) {
            return 100.0;
        }

        double hitRateScore = calculateGlobalHitRate() * 50;
        double errorRatePenalty = calculateGlobalErrorRate() * 100;
        double responseTimeScore = calculateResponseTimeScore();

        double healthScore = hitRateScore + responseTimeScore - errorRatePenalty;
        return Math.max(0, Math.min(100, healthScore));
    }

    private double calculateResponseTimeScore() {
        double avgTime = calculateAverageResponseTime();

        if (avgTime < 10)
            return 50.0;
        if (avgTime < 50)
            return 40.0;
        if (avgTime < 100)
            return 30.0;
        if (avgTime < 500)
            return 20.0;
        return 10.0;
    }

    private String getHealthLevel(double score) {
        if (score >= 90)
            return "EXCELLENT";
        if (score >= 80)
            return "GOOD";
        if (score >= 70)
            return "FAIR";
        if (score >= 60)
            return "POOR";
        return "CRITICAL";
    }

    // ========== 内部类 ==========

    /**
     * 全局指标
     */
    public static class GlobalMetrics {
        private final long totalRequests;
        private final long totalHits;
        private final long totalMisses;
        private final long totalErrors;
        private final long totalSets;
        private final long totalDeletes;
        private final double hitRate;
        private final double errorRate;
        private final ResponseTimeStats responseTimeStats;

        public GlobalMetrics(long totalRequests, long totalHits, long totalMisses, long totalErrors,
                long totalSets, long totalDeletes, double hitRate, double errorRate,
                ResponseTimeStats responseTimeStats) {
            this.totalRequests = totalRequests;
            this.totalHits = totalHits;
            this.totalMisses = totalMisses;
            this.totalErrors = totalErrors;
            this.totalSets = totalSets;
            this.totalDeletes = totalDeletes;
            this.hitRate = hitRate;
            this.errorRate = errorRate;
            this.responseTimeStats = responseTimeStats;
        }

        // Getters
        public long getTotalRequests() {
            return totalRequests;
        }

        public long getTotalHits() {
            return totalHits;
        }

        public long getTotalMisses() {
            return totalMisses;
        }

        public long getTotalErrors() {
            return totalErrors;
        }

        public long getTotalSets() {
            return totalSets;
        }

        public long getTotalDeletes() {
            return totalDeletes;
        }

        public double getHitRate() {
            return hitRate;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public ResponseTimeStats getResponseTimeStats() {
            return responseTimeStats;
        }
    }

    /**
     * 命名空间指标
     */
    public static class NamespaceMetrics {
        private final String namespace;
        private final LongAdder requests = new LongAdder();
        private final LongAdder hits = new LongAdder();
        private final LongAdder misses = new LongAdder();
        private final LongAdder errors = new LongAdder();
        private final LongAdder sets = new LongAdder();
        private final LongAdder deletes = new LongAdder();
        private volatile long totalResponseTime = 0;

        public NamespaceMetrics(String namespace) {
            this.namespace = namespace;
        }

        public void recordGet() {
            requests.increment();
        }

        public void recordHit() {
            hits.increment();
        }

        public void recordMiss() {
            misses.increment();
        }

        public void recordError() {
            errors.increment();
        }

        public void recordSet(long ttlSeconds) {
            sets.increment();
        }

        public void recordDelete() {
            deletes.increment();
        }

        public void recordBatchDelete(int count) {
            deletes.add(count);
        }

        public void recordClear() {
            deletes.increment();
        }

        // Getters
        public String getNamespace() {
            return namespace;
        }

        public long getRequests() {
            return requests.sum();
        }

        public long getHits() {
            return hits.sum();
        }

        public long getMisses() {
            return misses.sum();
        }

        public long getErrors() {
            return errors.sum();
        }

        public long getSets() {
            return sets.sum();
        }

        public long getDeletes() {
            return deletes.sum();
        }

        public double getHitRate() {
            long req = requests.sum();
            return req > 0 ? (double) hits.sum() / req : 0.0;
        }

        public double getErrorRate() {
            long req = requests.sum();
            return req > 0 ? (double) errors.sum() / req : 0.0;
        }

        public double getAvgResponseTime() {
            long req = requests.sum();
            return req > 0 ? (double) totalResponseTime / req : 0.0;
        }
    }

    /**
     * 业务数据指标
     */
    public static class BusinessDataMetrics {
        private final BusinessDataType dataType;
        private final LongAdder totalAccesses = new LongAdder();
        private final LongAdder hits = new LongAdder();
        private final LongAdder misses = new LongAdder();
        private final LongAdder totalDataSize = new LongAdder();

        public BusinessDataMetrics(BusinessDataType dataType) {
            this.dataType = dataType;
        }

        public void recordAccess(long dataSize, boolean hit) {
            totalAccesses.increment();
            totalDataSize.add(dataSize);
            if (hit) {
                hits.increment();
            } else {
                misses.increment();
            }
        }

        // Getters
        public BusinessDataType getDataType() {
            return dataType;
        }

        public long getTotalAccesses() {
            return totalAccesses.sum();
        }

        public long getHits() {
            return hits.sum();
        }

        public long getMisses() {
            return misses.sum();
        }

        public long getTotalDataSize() {
            return totalDataSize.sum();
        }

        public double getHitRate() {
            long total = totalAccesses.sum();
            return total > 0 ? (double) hits.sum() / total : 0.0;
        }
    }

    /**
     * 响应时间统计
     */
    public static class ResponseTimeStats {
        private final long minTime;
        private final long maxTime;
        private final long totalTime;
        private final long count;

        public ResponseTimeStats(long minTime, long maxTime, long totalTime, long count) {
            this.minTime = minTime;
            this.maxTime = maxTime;
            this.totalTime = totalTime;
            this.count = count;
        }

        // Getters
        public long getMinTime() {
            return minTime;
        }

        public long getMaxTime() {
            return maxTime;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public long getCount() {
            return count;
        }

        public double getAverageTime() {
            return count > 0 ? (double) totalTime / count : 0.0;
        }
    }

    /**
     * 健康状态
     */
    public static class HealthStatus {
        private final double score;
        private final String level;
        private final LocalDateTime timestamp;

        public HealthStatus(double score, String level, LocalDateTime timestamp) {
            this.score = score;
            this.level = level;
            this.timestamp = timestamp;
        }

        // Getters
        public double getScore() {
            return score;
        }

        public String getLevel() {
            return level;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    /**
     * 错误记录
     */
    public static class ErrorRecord {
        private final String namespace;
        private final long timestamp;

        public ErrorRecord(String namespace, long timestamp) {
            this.namespace = namespace;
            this.timestamp = timestamp;
        }

        // Getters
        public String getNamespace() {
            return namespace;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * 错误统计
     */
    public static class ErrorStatistics {
        private final long totalErrors;
        private final double errorRate;
        private final List<ErrorRecord> recentErrors;

        public ErrorStatistics(long totalErrors, double errorRate, List<ErrorRecord> recentErrors) {
            this.totalErrors = totalErrors;
            this.errorRate = errorRate;
            this.recentErrors = new ArrayList<>(recentErrors);
        }

        // Getters
        public long getTotalErrors() {
            return totalErrors;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public List<ErrorRecord> getRecentErrors() {
            return new ArrayList<>(recentErrors);
        }
    }

    /**
     * 健康状态快照
     */
    public static class HealthStatusSnapshot {
        private final double score;
        private final String level;
        private final LocalDateTime timestamp;

        public HealthStatusSnapshot(double score, String level, LocalDateTime timestamp) {
            this.score = score;
            this.level = level;
            this.timestamp = timestamp;
        }

        // Getters
        public double getScore() {
            return score;
        }

        public String getLevel() {
            return level;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
