package net.lab1024.sa.common.permission.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.UnifiedCacheManager;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 权限验证性能监控器
 * <p>
 * 企业级权限验证性能监控，提供：
 * - 实时权限验证性能指标采集
 * - 权限验证吞吐量统计
 * - 权限缓存命中率监控
 * - 权限验证异常率统计和告警
 * - 性能指标聚合和历史趋势分析
 * - 性能Dashboard数据支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class PermissionPerformanceMonitor {

    @Resource
    private MeterRegistry meterRegistry;

    @Resource
    private UnifiedCacheManager unifiedCacheManager;

    // 性能指标存储
    private final Map<String, PerformanceMetrics> metricsMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    // Micrometer指标
    private Timer permissionValidationTimer;
    private Counter permissionValidationCounter;
    private Counter permissionErrorCounter;
    private Timer cacheAccessTimer;
    private Counter cacheHitCounter;
    private Counter cacheMissCounter;

    /**
     * 初始化性能监控指标
     */
    public PermissionPerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    /**
     * 初始化Micrometer指标
     */
    private void initializeMetrics() {
        // 权限验证耗时指标
        permissionValidationTimer = Timer.builder("permission.validation.duration")
                .description("权限验证耗时")
                .tag("module", "permission")
                .register(meterRegistry);

        // 权限验证计数器
        permissionValidationCounter = Counter.builder("permission.validation.count")
                .description("权限验证次数")
                .tag("module", "permission")
                .register(meterRegistry);

        // 权限验证错误计数器
        permissionErrorCounter = Counter.builder("permission.validation.errors")
                .description("权限验证错误次数")
                .tag("module", "permission")
                .register(meterRegistry);

        // 缓存访问耗时
        cacheAccessTimer = Timer.builder("permission.cache.access.duration")
                .description("权限缓存访问耗时")
                .tag("module", "permission")
                .register(meterRegistry);

        // 缓存命中计数器
        cacheHitCounter = Counter.builder("permission.cache.hits")
                .description("权限缓存命中次数")
                .tag("module", "permission")
                .register(meterRegistry);

        // 缓存未命中计数器
        cacheMissCounter = Counter.builder("permission.cache.misses")
                .description("权限缓存未命中次数")
                .tag("module", "permission")
                .register(meterRegistry);

        log.info("[权限性能监控] 性能指标初始化完成");
    }

    /**
     * 记录权限验证性能
     *
     * @param operation 操作类型
     * @param userId 用户ID
     * @param duration 耗时（毫秒）
     * @param success 是否成功
     * @param cacheHit 是否缓存命中
     */
    public void recordPermissionValidation(String operation, Long userId, long duration, boolean success, boolean cacheHit) {
        try {
            String key = buildMetricsKey(operation, userId);

            // 更新本地性能指标
            PerformanceMetrics metrics = metricsMap.computeIfAbsent(key, k -> new PerformanceMetrics());
            metrics.recordValidation(duration, success, cacheHit);

            // 更新Micrometer指标
            permissionValidationTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            permissionValidationCounter.increment();

            if (!success) {
                permissionErrorCounter.increment();
            }

            // 更新缓存指标
            if (cacheHit) {
                cacheHitCounter.increment();
            } else {
                cacheMissCounter.increment();
            }

            // 检查性能阈值
            checkPerformanceThreshold(operation, duration);

            log.debug("[权限性能监控] 记录权限验证: operation={}, duration={}ms, success={}, cacheHit={}",
                     operation, duration, success, cacheHit);

        } catch (Exception e) {
            log.error("[权限性能监控] 记录权限验证性能异常", e);
        }
    }

    /**
     * 记录缓存访问性能
     *
     * @param cacheType 缓存类型
     * @param key 缓存键
     * @param duration 耗时（毫秒）
     * @param hit 是否命中
     */
    public void recordCacheAccess(String cacheType, String key, long duration, boolean hit) {
        try {
            // 更新Micrometer指标
            cacheAccessTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);

            if (hit) {
                cacheHitCounter.increment();
            } else {
                cacheMissCounter.increment();
            }

            // 记录本地指标
            String cacheKey = "cache:" + cacheType;
            PerformanceMetrics metrics = metricsMap.computeIfAbsent(cacheKey, k -> new PerformanceMetrics());
            metrics.recordCacheAccess(duration, hit);

        } catch (Exception e) {
            log.error("[权限性能监控] 记录缓存访问性能异常", e);
        }
    }

    /**
     * 获取性能统计信息
     *
     * @param operation 操作类型（可选）
     * @param userId 用户ID（可选）
     * @return 性能统计信息
     */
    public PerformanceStats getPerformanceStats(String operation, Long userId) {
        try {
            PerformanceStats stats = new PerformanceStats();

            // 获取权限验证统计
            String validationKey = buildMetricsKey(operation, userId);
            PerformanceMetrics validationMetrics = metricsMap.get(validationKey);
            if (validationMetrics != null) {
                stats.setValidationCount(validationMetrics.getTotalCount());
                stats.setValidationSuccessCount(validationMetrics.getSuccessCount());
                stats.setValidationErrorCount(validationMetrics.getErrorCount());
                stats.setAverageValidationTime(validationMetrics.getAverageTime());
                stats.setMaxValidationTime(validationMetrics.getMaxTime());
                stats.setMinValidationTime(validationMetrics.getMinTime());
            }

            // 获取缓存统计
            String cacheKey = "cache:permission";
            PerformanceMetrics cacheMetrics = metricsMap.get(cacheKey);
            if (cacheMetrics != null) {
                stats.setCacheHitRate(cacheMetrics.getCacheHitRate());
                stats.setAverageCacheTime(cacheMetrics.getAverageCacheTime());
            }

            // 从Redis获取缓存管理器统计信息
            try {
                UnifiedCacheManager.CacheStats cacheStats = unifiedCacheManager.getCacheStats();
                stats.setLocalCacheSize(cacheStats.getLocalCacheSize());
                stats.setRedisCacheSize(cacheStats.getRedisCacheSize());
                stats.setLocalHitRate(cacheStats.getLocalHitRate());
                stats.setRedisHitRate(cacheStats.getRedisHitRate());
                stats.setOverallHitRate(cacheStats.getOverallHitRate());
                stats.setEvictionCount(cacheStats.getEvictionCount());
                stats.setLoadCount(cacheStats.getLoadCount());
                stats.setLoadExceptionCount(cacheStats.getLoadExceptionCount());
                stats.setAverageLoadTime(cacheStats.getAverageLoadTime());
            } catch (Exception e) {
                log.warn("[权限性能监控] 获取缓存统计信息失败", e);
            }

            return stats;

        } catch (Exception e) {
            log.error("[权限性能监控] 获取性能统计信息异常", e);
            return new PerformanceStats();
        }
    }

    /**
     * 获取实时性能指标
     *
     * @return 实时性能指标
     */
    public RealTimeMetrics getRealTimeMetrics() {
        try {
            RealTimeMetrics metrics = new RealTimeMetrics();

            // 获取当前时间窗口内的性能指标
            LocalDateTime now = LocalDateTime.now();
            metrics.setCurrentTime(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 权限验证QPS（每秒查询数）
            long validationCount = getRecentValidationCount(60); // 最近1分钟
            metrics.setValidationQps(validationCount / 60.0);

            // 错误率
            long errorCount = getRecentErrorCount(60);
            metrics.setErrorRate(validationCount > 0 ? (double) errorCount / validationCount : 0.0);

            // 平均响应时间
            double avgResponseTime = getRecentAverageTime(60);
            metrics.setAverageResponseTime(avgResponseTime);

            // 缓存命中率
            double cacheHitRate = getRecentCacheHitRate(60);
            metrics.setCacheHitRate(cacheHitRate);

            // 热点操作统计
            metrics.setHotOperations(getHotOperations(10));

            // 性能健康度评分（0-100）
            metrics.setHealthScore(calculateHealthScore(metrics));

            return metrics;

        } catch (Exception e) {
            log.error("[权限性能监控] 获取实时性能指标异常", e);
            return new RealTimeMetrics();
        }
    }

    /**
     * 定期清理过期性能数据
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void cleanupExpiredMetrics() {
        try {
            long currentTime = System.currentTimeMillis();
            long expireTime = 24 * 60 * 60 * 1000; // 24小时

            metricsMap.entrySet().removeIf(entry -> {
                PerformanceMetrics metrics = entry.getValue();
                return (currentTime - metrics.getLastUpdateTime()) > expireTime;
            });

            log.debug("[权限性能监控] 清理过期性能数据完成");

        } catch (Exception e) {
            log.error("[权限性能监控] 清理过期性能数据异常", e);
        }
    }

    /**
     * 定期生成性能报告
     */
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    @Async("performanceMonitorExecutor")
    public void generatePerformanceReport() {
        try {
            PerformanceReport report = new PerformanceReport();
            report.setReportTime(LocalDateTime.now());
            report.setPerformanceStats(getPerformanceStats(null, null));
            report.setRealTimeMetrics(getRealTimeMetrics());

            // 记录性能报告
            logPerformanceReport(report);

            log.debug("[权限性能监控] 生成性能报告完成");

        } catch (Exception e) {
            log.error("[权限性能监控] 生成性能报告异常", e);
        }
    }

    /**
     * 构建性能指标键
     */
    private String buildMetricsKey(String operation, Long userId) {
        StringBuilder key = new StringBuilder("permission:validation:");

        if (operation != null && !operation.isEmpty()) {
            key.append(operation);
        } else {
            key.append("all");
        }

        if (userId != null) {
            key.append(":user:").append(userId);
        }

        return key.toString();
    }

    /**
     * 检查性能阈值
     */
    private void checkPerformanceThreshold(String operation, long duration) {
        // 定义性能阈值
        long warningThreshold = 500;   // 警告阈值：500ms
        long criticalThreshold = 1000; // 严重阈值：1000ms

        if (duration > criticalThreshold) {
            log.error("[权限性能监控] 权限验证严重超时: operation={}, duration={}ms", operation, duration);
            // 触发性能告警
            triggerPerformanceAlert(operation, duration, "CRITICAL");
        } else if (duration > warningThreshold) {
            log.warn("[权限性能监控] 权限验证警告超时: operation={}, duration={}ms", operation, duration);
            // 触发性能告警
            triggerPerformanceAlert(operation, duration, "WARNING");
        }
    }

    /**
     * 触发性能告警
     */
    private void triggerPerformanceAlert(String operation, long duration, String level) {
        try {
            // 创建性能告警
            String message = String.format("权限验证性能告警: operation=%s, duration=%dms, level=%s",
                                          operation, duration, level);

            // 这里可以集成告警系统（如邮件、短信、钉钉等）
            log.warn("[权限性能告警] {}", message);

            // 记录告警指标
            counters.computeIfAbsent("alert:performance:" + level.toLowerCase(), k -> new AtomicLong(0))
                     .incrementAndGet();

        } catch (Exception e) {
            log.error("[权限性能监控] 触发性能告警异常", e);
        }
    }

    /**
     * 获取最近的权限验证次数
     */
    private long getRecentValidationCount(int seconds) {
        // TODO: 实现从Redis或时间窗口获取最近验证次数的逻辑
        return (long) permissionValidationCounter.count();
    }

    /**
     * 获取最近的错误次数
     */
    private long getRecentErrorCount(int seconds) {
        // TODO: 实现从Redis或时间窗口获取最近错误次数的逻辑
        return (long) permissionErrorCounter.count();
    }

    /**
     * 获取最近的平均响应时间
     */
    private double getRecentAverageTime(int seconds) {
        // TODO: 实现从Redis或时间窗口获取最近平均响应时间的逻辑
        return permissionValidationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 获取最近的缓存命中率
     */
    private double getRecentCacheHitRate(int seconds) {
        try {
            long hits = (long) cacheHitCounter.count();
            long misses = (long) cacheMissCounter.count();
            long total = hits + misses;

            return total > 0 ? (double) hits / total : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取热点操作
     */
    private Map<String, Long> getHotOperations(int limit) {
        Map<String, Long> hotOperations = new HashMap<>();

        // TODO: 实现获取热点操作统计的逻辑
        // 这里可以从性能指标中分析出调用频率最高的操作

        return hotOperations;
    }

    /**
     * 计算性能健康度评分
     */
    private int calculateHealthScore(RealTimeMetrics metrics) {
        int score = 100;

        // 响应时间评分（权重40%）
        if (metrics.getAverageResponseTime() > 1000) {
            score -= 40;
        } else if (metrics.getAverageResponseTime() > 500) {
            score -= 20;
        }

        // 错误率评分（权重30%）
        if (metrics.getErrorRate() > 0.05) {
            score -= 30;
        } else if (metrics.getErrorRate() > 0.01) {
            score -= 15;
        }

        // 缓存命中率评分（权重30%）
        if (metrics.getCacheHitRate() < 0.7) {
            score -= 30;
        } else if (metrics.getCacheHitRate() < 0.85) {
            score -= 15;
        }

        return Math.max(0, score);
    }

    /**
     * 记录性能报告
     */
    private void logPerformanceReport(PerformanceReport report) {
        log.info("[权限性能报告] 时间: {}, QPS: {:.2f}, 错误率: {:.2%}, 平均响应时间: {:.2f}ms, 缓存命中率: {:.2%}, 健康度: {}",
                report.getReportTime(),
                report.getRealTimeMetrics().getValidationQps(),
                report.getRealTimeMetrics().getErrorRate(),
                report.getRealTimeMetrics().getAverageResponseTime(),
                report.getRealTimeMetrics().getCacheHitRate(),
                report.getRealTimeMetrics().getHealthScore());
    }

    /**
     * 性能指标存储类
     */
    private static class PerformanceMetrics {
        private final AtomicLong totalCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private volatile long totalTime = 0;
        private volatile long maxTime = 0;
        private volatile long minTime = Long.MAX_VALUE;
        private volatile long lastUpdateTime = System.currentTimeMillis();

        private final AtomicLong cacheTotalCount = new AtomicLong(0);
        private final AtomicLong cacheHitCount = new AtomicLong(0);
        private volatile long cacheTotalTime = 0;

        public synchronized void recordValidation(long duration, boolean success, boolean cacheHit) {
            totalCount.incrementAndGet();
            totalTime += duration;
            maxTime = Math.max(maxTime, duration);
            minTime = Math.min(minTime, duration);

            if (success) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }

            lastUpdateTime = System.currentTimeMillis();
        }

        public synchronized void recordCacheAccess(long duration, boolean hit) {
            cacheTotalCount.incrementAndGet();
            cacheTotalTime += duration;

            if (hit) {
                cacheHitCount.incrementAndGet();
            }
        }

        public long getTotalCount() { return totalCount.get(); }
        public long getSuccessCount() { return successCount.get(); }
        public long getErrorCount() { return errorCount.get(); }
        public double getAverageTime() { return totalCount.get() > 0 ? (double) totalTime / totalCount.get() : 0.0; }
        public long getMaxTime() { return maxTime; }
        public long getMinTime() { return minTime == Long.MAX_VALUE ? 0 : minTime; }
        public double getCacheHitRate() { return cacheTotalCount.get() > 0 ? (double) cacheHitCount.get() / cacheTotalCount.get() : 0.0; }
        public double getAverageCacheTime() { return cacheTotalCount.get() > 0 ? (double) cacheTotalTime / cacheTotalCount.get() : 0.0; }
        public long getLastUpdateTime() { return lastUpdateTime; }
    }

    /**
     * 性能统计信息
     */
    @lombok.Data
    public static class PerformanceStats {
        private Long validationCount = 0L;
        private Long validationSuccessCount = 0L;
        private Long validationErrorCount = 0L;
        private Double averageValidationTime = 0.0;
        private Long maxValidationTime = 0L;
        private Long minValidationTime = 0L;
        private Double cacheHitRate = 0.0;
        private Double averageCacheTime = 0.0;
        private Long localCacheSize = 0L;
        private Long redisCacheSize = 0L;
        private Double localHitRate = 0.0;
        private Double redisHitRate = 0.0;
        private Double overallHitRate = 0.0;
        private Long evictionCount = 0L;
        private Long loadCount = 0L;
        private Long loadExceptionCount = 0L;
        private Double averageLoadTime = 0.0;
    }

    /**
     * 实时性能指标
     */
    @lombok.Data
    public static class RealTimeMetrics {
        private String currentTime;
        private Double validationQps = 0.0;
        private Double errorRate = 0.0;
        private Double averageResponseTime = 0.0;
        private Double cacheHitRate = 0.0;
        private Map<String, Long> hotOperations = new HashMap<>();
        private Integer healthScore = 100;
    }

    /**
     * 性能报告
     */
    @lombok.Data
    public static class PerformanceReport {
        private LocalDateTime reportTime;
        private PerformanceStats performanceStats;
        private RealTimeMetrics realTimeMetrics;
    }
}