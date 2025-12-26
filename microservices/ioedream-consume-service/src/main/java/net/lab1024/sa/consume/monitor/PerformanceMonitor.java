package net.lab1024.sa.consume.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 性能监控管理器
 *
 * 职责：收集和统计性能指标
 *
 * 监控指标：
 * 1. TPS（每秒事务数）
 * 2. 响应时间（平均、P50、P95、P99）
 * 3. 成功率
 * 4. 错误率
 * 5. 缓存命中率
 *
 * 性能目标：
 * - TPS ≥ 1000
 * - 平均响应时间 ≤ 50ms
 * - P95响应时间 ≤ 100ms
 * - 成功率 ≥ 99.9%
 * - 缓存命中率 ≥ 90%
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
@Component
public class PerformanceMonitor {

    /**
     * 请求总数
     */
    private final LongAdder totalRequests = new LongAdder();

    /**
     * 成功请求数
     */
    private final LongAdder successRequests = new LongAdder();

    /**
     * 失败请求数
     */
    private final LongAdder failureRequests = new LongAdder();

    /**
     * 总响应时间（毫秒）
     */
    private final LongAdder totalResponseTime = new LongAdder();

    /**
     * 缓存命中数
     */
    private final LongAdder cacheHits = new LongAdder();

    /**
     * 缓存未命中数
     */
    private final LongAdder cacheMisses = new LongAdder();

    /**
     * 响应时间分布（用于计算百分位数）
     */
    private final ConcurrentHashMap<String, ResponseTimeSnapshot> responseTimeSnapshots = new ConcurrentHashMap<>();

    /**
     * 监控开始时间
     */
    private volatile LocalDateTime startTime;

    /**
     * 上次统计时间
     */
    private volatile LocalDateTime lastStatisticsTime;

    /**
     * 记录请求
     *
     * @param success 是否成功
     * @param responseTime 响应时间（毫秒）
     */
    public void recordRequest(boolean success, long responseTime) {
        totalRequests.increment();
        totalResponseTime.add(responseTime);

        if (success) {
            successRequests.increment();
        } else {
            failureRequests.increment();
        }

        // 记录响应时间快照（用于计算百分位数）
        String key = LocalDateTime.now().withSecond(0).withNano(0).toString();
        responseTimeSnapshots.compute(key, (k, snapshot) -> {
            if (snapshot == null) {
                snapshot = new ResponseTimeSnapshot();
            }
            snapshot.addResponseTime(responseTime);
            return snapshot;
        });
    }

    /**
     * 记录缓存命中
     */
    public void recordCacheHit() {
        cacheHits.increment();
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss() {
        cacheMisses.increment();
    }

    /**
     * 获取性能统计
     *
     * @return 性能统计信息
     */
    public PerformanceStatistics getStatistics() {
        long totalReqs = totalRequests.sum();
        long successReqs = successRequests.sum();
        long failureReqs = failureRequests.sum();
        long totalTime = totalResponseTime.sum();
        long hits = cacheHits.sum();
        long misses = cacheMisses.sum();

        PerformanceStatistics stats = new PerformanceStatistics();
        stats.setTotalRequests(totalReqs);
        stats.setSuccessRequests(successReqs);
        stats.setFailureRequests(failureReqs);
        stats.setTotalResponseTime(totalTime);
        stats.setCacheHits(hits);
        stats.setCacheMisses(misses);

        // 计算指标
        if (totalReqs > 0) {
            stats.setSuccessRate((double) successReqs / totalReqs);
            stats.setFailureRate((double) failureReqs / totalReqs);
            stats.setAverageResponseTime((double) totalTime / totalReqs);
        }

        if ((hits + misses) > 0) {
            stats.setCacheHitRate((double) hits / (hits + misses));
        }

        // 计算TPS（基于最近1分钟的请求）
        stats.setTps(calculateTPS());

        // 计算响应时间百分位数
        stats.setP50ResponseTime(calculatePercentile(50));
        stats.setP95ResponseTime(calculatePercentile(95));
        stats.setP99ResponseTime(calculatePercentile(99));

        stats.setSnapshotTime(LocalDateTime.now());

        return stats;
    }

    /**
     * 计算TPS（基于最近1分钟的请求）
     */
    private double calculateTPS() {
        // 清理超过1分钟的快照
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);

        responseTimeSnapshots.entrySet().removeIf(entry -> {
            LocalDateTime snapshotTime = LocalDateTime.parse(entry.getKey());
            return snapshotTime.isBefore(oneMinuteAgo);
        });

        // 统计最近1分钟的请求总数
        long recentRequests = responseTimeSnapshots.values().stream()
                .mapToLong(ResponseTimeSnapshot::getCount)
                .sum();

        return recentRequests / 60.0; // 每秒请求数
    }

    /**
     * 计算响应时间百分位数
     *
     * @param percentile 百分位数（50, 95, 99）
     * @return 响应时间（毫秒）
     */
    private long calculatePercentile(int percentile) {
        // 收集所有响应时间
        List<Long> allResponseTimes = new ArrayList<>();
        for (ResponseTimeSnapshot snapshot : responseTimeSnapshots.values()) {
            allResponseTimes.addAll(snapshot.getResponseTimes());
        }

        if (allResponseTimes.isEmpty()) {
            return 0;
        }

        // 排序
        allResponseTimes.sort(Long::compareTo);

        // 计算百分位数
        int index = (int) Math.ceil((percentile / 100.0) * allResponseTimes.size()) - 1;
        index = Math.max(0, Math.min(index, allResponseTimes.size() - 1));

        return allResponseTimes.get(index);
    }

    /**
     * 重置统计
     */
    public void reset() {
        totalRequests.reset();
        successRequests.reset();
        failureRequests.reset();
        totalResponseTime.reset();
        cacheHits.reset();
        cacheMisses.reset();
        responseTimeSnapshots.clear();
        startTime = LocalDateTime.now();

        log.info("[性能监控] 统计已重置");
    }

    /**
     * 启动监控
     */
    public void start() {
        startTime = LocalDateTime.now();
        lastStatisticsTime = LocalDateTime.now();
        log.info("[性能监控] 已启动: startTime={}", startTime);
    }

    /**
     * 性能统计信息
     */
    public static class PerformanceStatistics {
        private long totalRequests;
        private long successRequests;
        private long failureRequests;
        private double successRate;
        private double failureRate;
        private long totalResponseTime;
        private double averageResponseTime;
        private long p50ResponseTime;
        private long p95ResponseTime;
        private long p99ResponseTime;
        private long cacheHits;
        private long cacheMisses;
        private double cacheHitRate;
        private double tps;
        private LocalDateTime snapshotTime;

        // Getters and Setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

        public long getSuccessRequests() { return successRequests; }
        public void setSuccessRequests(long successRequests) { this.successRequests = successRequests; }

        public long getFailureRequests() { return failureRequests; }
        public void setFailureRequests(long failureRequests) { this.failureRequests = failureRequests; }

        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }

        public double getFailureRate() { return failureRate; }
        public void setFailureRate(double failureRate) { this.failureRate = failureRate; }

        public long getTotalResponseTime() { return totalResponseTime; }
        public void setTotalResponseTime(long totalResponseTime) { this.totalResponseTime = totalResponseTime; }

        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }

        public long getP50ResponseTime() { return p50ResponseTime; }
        public void setP50ResponseTime(long p50ResponseTime) { this.p50ResponseTime = p50ResponseTime; }

        public long getP95ResponseTime() { return p95ResponseTime; }
        public void setP95ResponseTime(long p95ResponseTime) { this.p95ResponseTime = p95ResponseTime; }

        public long getP99ResponseTime() { return p99ResponseTime; }
        public void setP99ResponseTime(long p99ResponseTime) { this.p99ResponseTime = p99ResponseTime; }

        public long getCacheHits() { return cacheHits; }
        public void setCacheHits(long cacheHits) { this.cacheHits = cacheHits; }

        public long getCacheMisses() { return cacheMisses; }
        public void setCacheMisses(long cacheMisses) { this.cacheMisses = cacheMisses; }

        public double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(double cacheHitRate) { this.cacheHitRate = cacheHitRate; }

        public double getTps() { return tps; }
        public void setTps(double tps) { this.tps = tps; }

        public LocalDateTime getSnapshotTime() { return snapshotTime; }
        public void setSnapshotTime(LocalDateTime snapshotTime) { this.snapshotTime = snapshotTime; }

        @Override
        public String toString() {
            return String.format(
                    "PerformanceStatistics{" +
                    "totalRequests=%d, " +
                    "successRequests=%d, " +
                    "failureRequests=%d, " +
                    "successRate=%.2f%%, " +
                    "averageResponseTime=%.2fms, " +
                    "p50=%dms, p95=%dms, p99=%dms, " +
                    "cacheHitRate=%.2f%%, " +
                    "tps=%.2f, " +
                    "snapshotTime=%s}",
                    totalRequests, successRequests, failureRequests, successRate * 100,
                    averageResponseTime, p50ResponseTime, p95ResponseTime, p99ResponseTime,
                    cacheHitRate * 100, tps, snapshotTime
            );
        }
    }

    /**
     * 响应时间快照
     */
    private static class ResponseTimeSnapshot {
        private final List<Long> responseTimes = new ArrayList<>();
        private final AtomicLong count = new AtomicLong(0);

        public void addResponseTime(long responseTime) {
            responseTimes.add(responseTime);
            count.incrementAndGet();
        }

        public List<Long> getResponseTimes() {
            return responseTimes;
        }

        public long getCount() {
            return count.get();
        }
    }
}
