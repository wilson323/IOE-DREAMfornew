package net.lab1024.sa.base.common.cache;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存指标收集器
 * <p>
 * 严格遵循repowiki监控规范：
 * - 实时收集缓存性能指标
 * - 支持多维度统计分析
 * - 提供缓存健康度评估
 * - 支持告警和通知
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class CacheMetricsCollector {

    @Resource
    private UnifiedCacheManager unifiedCacheManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 指标存储
    private final Map<String, NamespaceMetrics> namespaceMetricsMap = new ConcurrentHashMap<>();

    // 全局指标
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalHits = new AtomicLong(0);
    private final AtomicLong totalMisses = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalSets = new AtomicLong(0);
    private final AtomicLong totalDeletes = new AtomicLong(0);

    // 响应时间统计
    private final AtomicReference<ResponseTimeStats> responseTimeStats = new AtomicReference<>(
            ResponseTimeStats.builder()
                    .minTime(Long.MAX_VALUE)
                    .maxTime(0L)
                    .totalTime(0L)
                    .count(0L)
                    .build());

    // 初始化命名空间指标
    {
        for (CacheNamespace namespace : CacheNamespace.values()) {
            namespaceMetricsMap.put(namespace.getPrefix(), new NamespaceMetrics(namespace));
        }
    }

    /**
     * 记录缓存命中
     */
    public void recordHit(CacheNamespace namespace, long responseTime) {
        NamespaceMetrics metrics = getOrCreateNamespaceMetrics(namespace);
        metrics.recordHit(responseTime);

        totalRequests.incrementAndGet();
        totalHits.incrementAndGet();
        updateResponseTimeStats(responseTime);

        log.debug("记录缓存命中: namespace={}, responseTime={}ms", namespace.getPrefix(), responseTime);
    }

    /**
     * 记录缓存未命中
     */
    public void recordMiss(CacheNamespace namespace) {
        NamespaceMetrics metrics = getOrCreateNamespaceMetrics(namespace);
        metrics.recordMiss();

        totalRequests.incrementAndGet();
        totalMisses.incrementAndGet();

        log.debug("记录缓存未命中: namespace={}", namespace.getPrefix());
    }

    /**
     * 记录缓存错误
     */
    public void recordError(CacheNamespace namespace) {
        NamespaceMetrics metrics = getOrCreateNamespaceMetrics(namespace);
        metrics.recordError();

        totalErrors.incrementAndGet();

        log.debug("记录缓存错误: namespace={}", namespace.getPrefix());
    }

    /**
     * 记录缓存设置
     */
    public void recordSet(CacheNamespace namespace, long responseTime) {
        NamespaceMetrics metrics = getOrCreateNamespaceMetrics(namespace);
        metrics.recordSet(responseTime);

        totalSets.incrementAndGet();
        updateResponseTimeStats(responseTime);

        log.debug("记录缓存设置: namespace={}, responseTime={}ms", namespace.getPrefix(), responseTime);
    }

    /**
     * 记录缓存删除
     */
    public void recordDelete(CacheNamespace namespace) {
        NamespaceMetrics metrics = getOrCreateNamespaceMetrics(namespace);
        metrics.recordDelete();

        totalDeletes.incrementAndGet();

        log.debug("记录缓存删除: namespace={}", namespace.getPrefix());
    }

    /**
     * 获取命名空间统计信息
     */
    public Map<String, Object> getStatistics(CacheNamespace namespace) {
        NamespaceMetrics metrics = namespaceMetricsMap.get(namespace.getPrefix());
        if (metrics == null) {
            return Collections.emptyMap();
        }

        return metrics.toMap();
    }

    /**
     * 获取所有命名空间统计信息
     */
    public Map<String, Map<String, Object>> getAllStatistics() {
        Map<String, Map<String, Object>> allStats = new HashMap<>();

        // 添加各命名空间统计
        for (Map.Entry<String, NamespaceMetrics> entry : namespaceMetricsMap.entrySet()) {
            allStats.put(entry.getKey(), entry.getValue().toMap());
        }

        // 添加全局统计
        allStats.put("global", getGlobalStatistics());

        return allStats;
    }

    /**
     * 获取全局统计信息
     */
    public Map<String, Object> getGlobalStatistics() {
        long requests = totalRequests.get();
        long hits = totalHits.get();
        long misses = totalMisses.get();
        long errors = totalErrors.get();
        long sets = totalSets.get();
        long deletes = totalDeletes.get();

        double hitRate = requests > 0 ? (double) hits / requests : 0.0;
        double missRate = requests > 0 ? (double) misses / requests : 0.0;
        double errorRate = requests > 0 ? (double) errors / requests : 0.0;

        ResponseTimeStats rtStats = responseTimeStats.get();
        long count = rtStats.getCount();
        double avgResponseTime = count > 0 ? (double) rtStats.getTotalTime() / count : 0.0;

        Map<String, Object> globalStats = new HashMap<>();
        globalStats.put("totalRequests", requests);
        globalStats.put("totalHits", hits);
        globalStats.put("totalMisses", misses);
        globalStats.put("totalErrors", errors);
        globalStats.put("totalSets", sets);
        globalStats.put("totalDeletes", deletes);
        globalStats.put("hitRate", Math.round(hitRate * 10000.0) / 100.0); // 保留2位小数
        globalStats.put("missRate", Math.round(missRate * 10000.0) / 100.0);
        globalStats.put("errorRate", Math.round(errorRate * 10000.0) / 100.0);
        globalStats.put("avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);
        globalStats.put("minResponseTime", rtStats.getMinTime());
        globalStats.put("maxResponseTime", rtStats.getMaxTime());
        globalStats.put("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return globalStats;
    }

    /**
     * 获取缓存健康度评估
     */
    public Map<String, Object> getHealthAssessment() {
        Map<String, Object> assessment = new HashMap<>();

        // 全局健康度
        double globalHealthScore = calculateGlobalHealthScore();
        assessment.put("globalHealthScore", globalHealthScore);
        assessment.put("globalHealthLevel", getHealthLevel(globalHealthScore));

        // 各命名空间健康度
        Map<String, Object> namespaceHealth = new HashMap<>();
        for (Map.Entry<String, NamespaceMetrics> entry : namespaceMetricsMap.entrySet()) {
            double healthScore = entry.getValue().calculateHealthScore();
            namespaceHealth.put(entry.getKey(), Map.of(
                    "healthScore", healthScore,
                    "healthLevel", getHealthLevel(healthScore)));
        }
        assessment.put("namespaceHealth", namespaceHealth);

        // 告警信息
        List<String> warnings = generateWarnings();
        assessment.put("warnings", warnings);
        assessment.put("warningCount", warnings.size());

        // 评估时间
        assessment.put("assessmentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return assessment;
    }

    /**
     * 重置所有统计信息
     */
    public void resetStatistics() {
        // 重置全局指标
        totalRequests.set(0);
        totalHits.set(0);
        totalMisses.set(0);
        totalErrors.set(0);
        totalSets.set(0);
        totalDeletes.set(0);

        // 重置响应时间统计
        responseTimeStats.set(ResponseTimeStats.builder()
                .minTime(Long.MAX_VALUE)
                .maxTime(0L)
                .totalTime(0L)
                .count(0L)
                .build());

        // 重置各命名空间指标
        for (NamespaceMetrics metrics : namespaceMetricsMap.values()) {
            metrics.reset();
        }

        log.info("所有缓存统计信息已重置");
    }

    /**
     * 重置指定命名空间统计信息
     */
    public void resetStatistics(CacheNamespace namespace) {
        NamespaceMetrics metrics = namespaceMetricsMap.get(namespace.getPrefix());
        if (metrics != null) {
            metrics.reset();
            log.info("命名空间缓存统计信息已重置: namespace={}", namespace.getPrefix());
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 获取或创建命名空间指标
     */
    private NamespaceMetrics getOrCreateNamespaceMetrics(CacheNamespace namespace) {
        return namespaceMetricsMap.computeIfAbsent(namespace.getPrefix(), k -> new NamespaceMetrics(namespace));
    }

    /**
     * 更新响应时间统计
     */
    private void updateResponseTimeStats(long responseTime) {
        responseTimeStats.updateAndGet(stats -> {
            long newMinTime = Math.min(stats.getMinTime(), responseTime);
            long newMaxTime = Math.max(stats.getMaxTime(), responseTime);
            long newTotalTime = stats.getTotalTime() + responseTime;
            long newCount = stats.getCount() + 1;

            return stats.toBuilder()
                    .minTime(newMinTime)
                    .maxTime(newMaxTime)
                    .totalTime(newTotalTime)
                    .count(newCount)
                    .build();
        });
    }

    /**
     * 计算全局健康度分数
     */
    private double calculateGlobalHealthScore() {
        long requests = totalRequests.get();
        if (requests == 0) {
            return 100.0; // 无请求时健康度为100
        }

        double hitRateScore = (double) totalHits.get() / requests * 50; // 命中率权重50%
        double errorRatePenalty = (double) totalErrors.get() / requests * 100; // 错误率惩罚
        double responseTimeScore = calculateResponseTimeScore(); // 响应时间分数

        double healthScore = hitRateScore + responseTimeScore - errorRatePenalty;
        return Math.max(0, Math.min(100, healthScore));
    }

    /**
     * 计算响应时间分数
     */
    private double calculateResponseTimeScore() {
        ResponseTimeStats stats = responseTimeStats.get();
        long count = stats.getCount();

        if (count == 0) {
            return 50.0; // 无响应时间数据时给中等分数
        }

        double avgTime = (double) stats.getTotalTime() / count;

        if (avgTime < 10) {
            return 50.0; // 优秀
        } else if (avgTime < 50) {
            return 40.0; // 良好
        } else if (avgTime < 100) {
            return 30.0; // 一般
        } else if (avgTime < 500) {
            return 20.0; // 较差
        } else {
            return 10.0; // 很差
        }
    }

    /**
     * 获取健康等级
     */
    private String getHealthLevel(double healthScore) {
        if (healthScore >= 90) {
            return "优秀";
        } else if (healthScore >= 80) {
            return "良好";
        } else if (healthScore >= 70) {
            return "一般";
        } else if (healthScore >= 60) {
            return "较差";
        } else {
            return "很差";
        }
    }

    /**
     * 生成告警信息
     */
    private List<String> generateWarnings() {
        List<String> warnings = new ArrayList<>();

        // 全局告警
        long requests = totalRequests.get();
        if (requests > 0) {
            double errorRate = (double) totalErrors.get() / requests;
            if (errorRate > 0.05) { // 错误率超过5%
                warnings.add(String.format("全局错误率过高: %.2f%%", errorRate * 100));
            }

            ResponseTimeStats rtStats = responseTimeStats.get();
            if (rtStats.getCount() > 0) {
                double avgTime = (double) rtStats.getTotalTime() / rtStats.getCount();
                if (avgTime > 100) { // 平均响应时间超过100ms
                    warnings.add(String.format("全局响应时间过慢: %.2fms", avgTime));
                }
            }
        }

        // 命名空间告警
        for (Map.Entry<String, NamespaceMetrics> entry : namespaceMetricsMap.entrySet()) {
            NamespaceMetrics metrics = entry.getValue();
            Map<String, Object> stats = metrics.toMap();

            Double hitRate = (Double) stats.get("hitRate");
            if (hitRate != null && hitRate < 0.8) { // 命中率低于80%
                warnings.add(String.format("命名空间 %s 命中率过低: %.2f%%", entry.getKey(), hitRate * 100));
            }

            Long errorCount = (Long) stats.get("errors");
            Long requestCount = (Long) stats.get("requests");
            if (requestCount != null && requestCount > 0 && errorCount != null) {
                double errorRate = (double) errorCount / requestCount;
                if (errorRate > 0.05) { // 错误率超过5%
                    warnings.add(String.format("命名空间 %s 错误率过高: %.2f%%", entry.getKey(), errorRate * 100));
                }
            }
        }

        return warnings;
    }

    // ========== 内部类 ==========

    /**
     * 命名空间指标
     */
    private static class NamespaceMetrics {
        private final CacheNamespace namespace;
        private final AtomicLong requests = new AtomicLong(0);
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);
        private final AtomicLong errors = new AtomicLong(0);
        private final AtomicLong sets = new AtomicLong(0);
        private final AtomicLong deletes = new AtomicLong(0);

        // 响应时间统计
        private final AtomicReference<ResponseTimeStats> responseTimeStats = new AtomicReference<>(
                ResponseTimeStats.builder()
                        .minTime(Long.MAX_VALUE)
                        .maxTime(0L)
                        .totalTime(0L)
                        .count(0L)
                        .build());

        public NamespaceMetrics(CacheNamespace namespace) {
            this.namespace = namespace;
        }

        public void recordHit(long responseTime) {
            requests.incrementAndGet();
            hits.incrementAndGet();
            updateResponseTimeStats(responseTime);
        }

        public void recordMiss() {
            requests.incrementAndGet();
            misses.incrementAndGet();
        }

        public void recordError() {
            requests.incrementAndGet();
            errors.incrementAndGet();
        }

        public void recordSet(long responseTime) {
            sets.incrementAndGet();
            updateResponseTimeStats(responseTime);
        }

        public void recordDelete() {
            deletes.incrementAndGet();
        }

        public void reset() {
            requests.set(0);
            hits.set(0);
            misses.set(0);
            errors.set(0);
            sets.set(0);
            deletes.set(0);

            responseTimeStats.set(ResponseTimeStats.builder()
                    .minTime(Long.MAX_VALUE)
                    .maxTime(0L)
                    .totalTime(0L)
                    .count(0L)
                    .build());
        }

        public double calculateHealthScore() {
            long req = requests.get();
            if (req == 0) {
                return 100.0;
            }

            double hitRate = (double) hits.get() / req;
            double errorRate = (double) errors.get() / req;

            ResponseTimeStats stats = responseTimeStats.get();
            double avgTime = stats.getCount() > 0 ? (double) stats.getTotalTime() / stats.getCount() : 0.0;

            double healthScore = hitRate * 70 + calculateResponseTimeHealthScore(avgTime) - errorRate * 100;
            return Math.max(0, Math.min(100, healthScore));
        }

        private double calculateResponseTimeHealthScore(double avgTime) {
            if (avgTime < 10)
                return 30.0;
            if (avgTime < 50)
                return 25.0;
            if (avgTime < 100)
                return 20.0;
            if (avgTime < 500)
                return 15.0;
            return 10.0;
        }

        private void updateResponseTimeStats(long responseTime) {
            responseTimeStats.updateAndGet(stats -> {
                long newMinTime = Math.min(stats.getMinTime(), responseTime);
                long newMaxTime = Math.max(stats.getMaxTime(), responseTime);
                long newTotalTime = stats.getTotalTime() + responseTime;
                long newCount = stats.getCount() + 1;

                return stats.toBuilder()
                        .minTime(newMinTime)
                        .maxTime(newMaxTime)
                        .totalTime(newTotalTime)
                        .count(newCount)
                        .build();
            });
        }

        public Map<String, Object> toMap() {
            long req = requests.get();
            long hitsCount = hits.get();
            long missesCount = misses.get();
            long errorsCount = errors.get();

            double hitRate = req > 0 ? (double) hitsCount / req : 0.0;
            double missRate = req > 0 ? (double) missesCount / req : 0.0;
            double errorRate = req > 0 ? (double) errorsCount / req : 0.0;

            ResponseTimeStats rtStats = responseTimeStats.get();
            long count = rtStats.getCount();
            double avgResponseTime = count > 0 ? (double) rtStats.getTotalTime() / count : 0.0;

            Map<String, Object> map = new HashMap<>();
            map.put("namespace", namespace.getPrefix());
            map.put("description", namespace.getDescription());
            map.put("requests", req);
            map.put("hits", hitsCount);
            map.put("misses", missesCount);
            map.put("errors", errorsCount);
            map.put("sets", sets.get());
            map.put("deletes", deletes.get());
            map.put("hitRate", Math.round(hitRate * 10000.0) / 100.0);
            map.put("missRate", Math.round(missRate * 10000.0) / 100.0);
            map.put("errorRate", Math.round(errorRate * 10000.0) / 100.0);
            map.put("avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);
            map.put("minResponseTime", rtStats.getMinTime());
            map.put("maxResponseTime", rtStats.getMaxTime());
            map.put("healthScore", Math.round(calculateHealthScore() * 100.0) / 100.0);
            map.put("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return map;
        }
    }

    /**
     * 响应时间统计
     */
    @lombok.Data
    @lombok.Builder(toBuilder = true)
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    private static class ResponseTimeStats {
        private long minTime;
        private long maxTime;
        private long totalTime;
        private long count;
    }
}
