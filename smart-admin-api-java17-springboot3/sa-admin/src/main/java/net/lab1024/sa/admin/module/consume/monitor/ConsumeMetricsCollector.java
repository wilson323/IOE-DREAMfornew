package net.lab1024.sa.admin.module.consume.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 消费模块监控指标收集器
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Slf4j
@Component
public class ConsumeMetricsCollector {

    // 业务指标存储
    private final Map<String, Object> businessMetrics = new ConcurrentHashMap<>();
    private final Map<String, Object> technicalMetrics = new ConcurrentHashMap<>();

    // 监控统计数据
    private final Map<String, Long> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> timers = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        log.info("消费模块监控指标收集器初始化完成");
        startMetricsReporting();
        initCounters();
    }

    /**
     * 初始化计数器
     */
    private void initCounters() {
        // 业务指标计数器
        counters.put("consume.success.count", 0L);
        counters.put("consume.failure.count", 0L);
        counters.put("consume.total.amount", 0L);
        counters.put("consume.daily.count", 0L);

        // 技术指标计数器
        counters.put("cache.hit.count", 0L);
        counters.put("cache.miss.count", 0L);
        counters.put("db.query.count", 0L);
        counters.put("db.transaction.count", 0L);

        log.info("监控计数器初始化完成");
    }

    /**
     * 记录消费成功
     */
    public void recordConsumeSuccess(Long amount, String consumeMode) {
        counters.put("consume.success.count", counters.getOrDefault("consume.success.count", 0L) + 1);
        counters.put("consume.total.amount", counters.getOrDefault("consume.total.amount", 0L) + amount);
        counters.put("consume.daily.count", counters.getOrDefault("consume.daily.count", 0L) + 1);

        // 记录消费模式统计
        String modeKey = "consume.mode." + consumeMode + ".count";
        counters.put(modeKey, counters.getOrDefault(modeKey, 0L) + 1);

        // 记录时间指标
        String timeKey = "consume.response.time." + consumeMode;
        updateTimer(timeKey);

        log.debug("记录消费成功: 金额={}, 模式={}", amount, consumeMode);
    }

    /**
     * 记录消费失败
     */
    public void recordConsumeFailure(String errorType, String consumeMode) {
        counters.put("consume.failure.count", counters.getOrDefault("consume.failure.count", 0L) + 1);

        // 记录错误类型统计
        String errorKey = "consume.error." + errorType + ".count";
        counters.put(errorKey, counters.getOrDefault(errorKey, 0L) + 1);

        log.warn("记录消费失败: 错误类型={}, 模式={}", errorType, consumeMode);
    }

    /**
     * 记录缓存命中
     */
    public void recordCacheHit(String cacheType) {
        counters.put("cache.hit.count", counters.getOrDefault("cache.hit.count", 0L) + 1);
        String typeKey = "cache.hit." + cacheType + ".count";
        counters.put(typeKey, counters.getOrDefault(typeKey, 0L) + 1);
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss(String cacheType) {
        counters.put("cache.miss.count", counters.getOrDefault("cache.miss.count", 0L) + 1);
        String typeKey = "cache.miss." + cacheType + ".count";
        counters.put(typeKey, counters.getOrDefault(typeKey, 0L) + 1);
    }

    /**
     * 记录数据库查询
     */
    public void recordDbQuery(String operation, long duration) {
        counters.put("db.query.count", counters.getOrDefault("db.query.count", 0L) + 1);
        String opKey = "db.query." + operation + ".count";
        counters.put(opKey, counters.getOrDefault(opKey, 0L) + 1);

        // 记录查询时间
        updateTimer("db.query.time." + operation);
        updateTimer("db.query.time." + operation, duration);
    }

    /**
     * 记录数据库事务
     */
    public void recordDbTransaction(String operation, long duration) {
        counters.put("db.transaction.count", counters.getOrDefault("db.transaction.count", 0L) + 1);
        String txKey = "db.transaction." + operation + ".count";
        counters.put(txKey, counters.getOrDefault(txKey, 0L) + 1);

        // 记录事务时间
        updateTimer("db.transaction.time." + operation);
        updateTimer("db.transaction.time." + operation, duration);
    }

    /**
     * 更新计时器
     */
    private void updateTimer(String key, long value) {
        timers.put(key, value);
    }

    /**
     * 更新计时器（当前时间）
     */
    private void updateTimer(String key) {
        timers.put(key + ".last", System.currentTimeMillis());
    }

    /**
     * 获取业务指标
     */
    public Map<String, Object> getBusinessMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();

        // 基础业务指标
        metrics.put("successCount", counters.getOrDefault("consume.success.count", 0L));
        metrics.put("failureCount", counters.getOrDefault("consume.failure.count", 0L));
        metrics.put("totalAmount", counters.getOrDefault("consume.total.amount", 0L));
        metrics.put("dailyCount", counters.getOrDefault("consume.daily.count", 0L));

        // 成功率计算
        long total = counters.getOrDefault("consume.success.count", 0L) + counters.getOrDefault("consume.failure.count", 0L);
        double successRate = total > 0 ? (double) counters.getOrDefault("consume.success.count", 0L) / total * 100 : 0.0;
        metrics.put("successRate", successRate);

        // 消费模式分布
        Map<String, Long> modeDistribution = new ConcurrentHashMap<>();
        counters.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("consume.mode.") && entry.getKey().endsWith(".count"))
            .forEach(entry -> {
                String mode = entry.getKey().substring("consume.mode.".length(), entry.getKey().length() - ".count".length());
                modeDistribution.put(mode, entry.getValue());
            });
        metrics.put("modeDistribution", modeDistribution);

        // 错误类型分布
        Map<String, Long> errorDistribution = new ConcurrentHashMap<>();
        counters.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("consume.error.") && entry.getKey().endsWith(".count"))
            .forEach(entry -> {
                String errorType = entry.getKey().substring("consume.error.".length(), entry.getKey().length() - ".count".length());
                errorDistribution.put(errorType, entry.getValue());
            });
        metrics.put("errorDistribution", errorDistribution);

        // 时间统计
        Map<String, Long> responseTimeStats = new ConcurrentHashMap<>();
        timers.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("consume.response.time."))
            .forEach(entry -> {
                String mode = entry.getKey().substring("consume.response.time.".length());
                responseTimeStats.put(mode, entry.getValue());
            });
        metrics.put("responseTimeStats", responseTimeStats);

        return metrics;
    }

    /**
     * 获取技术指标
     */
    public Map<String, Object> getTechnicalMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();

        // 缓存指标
        long cacheHits = counters.getOrDefault("cache.hit.count", 0L);
        long cacheMisses = counters.getOrDefault("cache.miss.count", 0L);
        long totalCacheAccess = cacheHits + cacheMisses;
        double cacheHitRate = totalCacheAccess > 0 ? (double) cacheHits / totalCacheAccess * 100 : 0.0;

        metrics.put("cacheHitRate", cacheHitRate);
        metrics.put("totalCacheAccess", totalCacheAccess);
        metrics.put("cacheHits", cacheHits);
        metrics.put("cacheMisses", cacheMisses);

        // 数据库指标
        metrics.put("dbQueryCount", counters.getOrDefault("db.query.count", 0L));
        metrics.put("dbTransactionCount", counters.getOrDefault("db.transaction.count", 0L));

        // 数据库查询统计
        Map<String, Long> dbQueryStats = new ConcurrentHashMap<>();
        counters.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("db.query.") && entry.getKey().endsWith(".count"))
            .forEach(entry -> {
                String operation = entry.getKey().substring("db.query.".length(), entry.getKey().length() - ".count".length());
                dbQueryStats.put(operation, entry.getValue());
            });
        metrics.put("dbQueryStats", dbQueryStats);

        // 数据库事务统计
        Map<String, Long> dbTransactionStats = new ConcurrentHashMap<>();
        counters.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("db.transaction.") && entry.getKey().endsWith(".count"))
            .forEach(entry -> {
                String operation = entry.getKey().substring("db.transaction.".length(), entry.getKey().length() - ".count".length());
                dbTransactionStats.put(operation, entry.getValue());
            });
        metrics.put("dbTransactionStats", dbTransactionStats);

        // 数据库时间统计
        Map<String, Long> dbTimeStats = new ConcurrentHashMap<>();
        timers.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("db.query.time.") || entry.getKey().startsWith("db.transaction.time."))
            .forEach(entry -> {
                dbTimeStats.put(entry.getKey(), entry.getValue());
            });
        metrics.put("dbTimeStats", dbTimeStats);

        return metrics;
    }

    /**
     * 获取系统健康状态
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new ConcurrentHashMap<>();

        // 基础健康指标
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(formatter));
        health.put("uptime", getUptime());

        // 业务健康指标
        long totalOperations = counters.getOrDefault("consume.success.count", 0L) + counters.getOrDefault("consume.failure.count", 0L);
        double successRate = totalOperations > 0 ? (double) counters.getOrDefault("consume.success.count", 0L) / totalOperations * 100 : 0.0;
        health.put("businessSuccessRate", successRate);

        // 技术健康指标
        double cacheHitRate = calculateCacheHitRate();
        health.put("cacheHitRate", cacheHitRate);

        // 健康评级
        String healthGrade = calculateHealthGrade(successRate, cacheHitRate);
        health.put("healthGrade", healthGrade);

        // 告警状态
        Map<String, Boolean> alerts = getAlertStatus();
        health.put("alerts", alerts);

        return health;
    }

    /**
     * 重置统计数据
     */
    public void resetStatistics() {
        log.info("重置监控统计数据");
        initCounters();
        timers.clear();
    }

    /**
     * 启动定时指标报告
     */
    private void startMetricsReporting() {
        // 每分钟输出关键指标
        scheduler.scheduleAtFixedRate(this::logKeyMetrics, 0, 1, TimeUnit.MINUTES);

        // 每小时输出详细指标
        scheduler.scheduleAtFixedRate(this::logDetailedMetrics, 0, 1, TimeUnit.HOURS);

        // 每日重置日计数器
        scheduler.scheduleAtFixedRate(this::resetDailyCounters, 0, 24, TimeUnit.HOURS);
    }

    /**
     * 记录关键指标日志
     */
    private void logKeyMetrics() {
        try {
            long successCount = counters.getOrDefault("consume.success.count", 0L);
            long failureCount = counters.getOrDefault("consume.failure.count", 0L);
            double cacheHitRate = calculateCacheHitRate();

            log.info("消费模块关键指标 - 成功: {}, 失败: {}, 缓存命中率: {:.2f}%",
                    successCount, failureCount, cacheHitRate);
        } catch (Exception e) {
            log.error("记录关键指标失败", e);
        }
    }

    /**
     * 记录详细指标日志
     */
    private void logDetailedMetrics() {
        try {
            Map<String, Object> businessMetrics = getBusinessMetrics();
            Map<String, Object> technicalMetrics = getTechnicalMetrics();

            log.info("消费模块详细指标报告: 业务指标={}, 技术指标={}",
                    businessMetrics.size(), technicalMetrics.size());
        } catch (Exception e) {
            log.error("记录详细指标失败", e);
        }
    }

    /**
     * 重置日计数器
     */
    private void resetDailyCounters() {
        counters.put("consume.daily.count", 0L);
        log.info("重置日计数器完成");
    }

    /**
     * 计算缓存命中率
     */
    private double calculateCacheHitRate() {
        long hits = counters.getOrDefault("cache.hit.count", 0L);
        long misses = counters.getOrDefault("cache.miss.count", 0L);
        long total = hits + misses;
        return total > 0 ? (double) hits / total * 100 : 0.0;
    }

    /**
     * 计算健康评级
     */
    private String calculateHealthGrade(double successRate, double cacheHitRate) {
        if (successRate >= 99.0 && cacheHitRate >= 85.0) {
            return "A";
        } else if (successRate >= 95.0 && cacheHitRate >= 75.0) {
            return "B";
        } else if (successRate >= 90.0 && cacheHitRate >= 65.0) {
            return "C";
        } else {
            return "D";
        }
    }

    /**
     * 获取告警状态
     */
    private Map<String, Boolean> getAlertStatus() {
        Map<String, Boolean> alerts = new ConcurrentHashMap<>();

        long totalOperations = counters.getOrDefault("consume.success.count", 0L) + counters.getOrDefault("consume.failure.count", 0L);
        double successRate = totalOperations > 0 ? (double) counters.getOrDefault("consume.success.count", 0L) / totalOperations * 100 : 0.0;

        // 业务成功率告警
        alerts.put("businessSuccessRateLow", successRate < 95.0);

        // 缓存命中率告警
        double cacheHitRate = calculateCacheHitRate();
        alerts.put("cacheHitRateLow", cacheHitRate < 80.0);

        // 错误率告警
        double errorRate = totalOperations > 0 ? (double) counters.getOrDefault("consume.failure.count", 0L) / totalOperations * 100 : 0.0;
        alerts.put("errorRateHigh", errorRate > 5.0);

        return alerts;
    }

    /**
     * 获取运行时间
     */
    private long getUptime() {
        // 这里可以实现实际的运行时间计算
        return System.currentTimeMillis() - (this.startTime != 0 ? this.startTime : System.currentTimeMillis());
    }

    private long startTime = System.currentTimeMillis();

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("监控指标收集器关闭时被中断", e);
            }
        }
        log.info("消费模块监控指标收集器已关闭");
    }
}