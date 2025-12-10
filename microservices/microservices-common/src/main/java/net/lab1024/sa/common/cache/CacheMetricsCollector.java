package net.lab1024.sa.common.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存指标收集器
 * <p>
 * 用于统计缓存命中率、响应时间等性能指标
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * </p>
 * <p>
 * 功能说明：
 * - 缓存命中率统计（L1、L2、DB）
 * - 缓存响应时间统计
 * - 缓存操作计数
 * - 性能指标导出（Prometheus）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class CacheMetricsCollector {

    private final MeterRegistry meterRegistry;
    
    // 缓存统计（按缓存类型）
    private final ConcurrentHashMap<String, CacheStats> cacheStatsMap = new ConcurrentHashMap<>();

    /**
     * 构造函数注入依赖
     *
     * @param meterRegistry Micrometer指标注册表
     */
    public CacheMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录缓存命中
     *
     * @param cacheType 缓存类型（L1、L2）
     * @param cacheKey  缓存键（可选，用于详细统计）
     */
    public void recordHit(String cacheType, String cacheKey) {
        // 更新统计
        CacheStats stats = cacheStatsMap.computeIfAbsent(cacheType, k -> new CacheStats());
        stats.hits.incrementAndGet();
        stats.total.incrementAndGet();

        // 记录到Micrometer
        meterRegistry.counter("cache.hit", "type", cacheType).increment();
        
        log.debug("[缓存指标] 缓存命中，type={}, key={}", cacheType, cacheKey);
    }

    /**
     * 记录缓存未命中
     *
     * @param cacheType 缓存类型（DB表示从数据库加载）
     * @param cacheKey  缓存键（可选）
     */
    public void recordMiss(String cacheType, String cacheKey) {
        // 更新统计
        CacheStats stats = cacheStatsMap.computeIfAbsent(cacheType, k -> new CacheStats());
        stats.misses.incrementAndGet();
        stats.total.incrementAndGet();

        // 记录到Micrometer
        meterRegistry.counter("cache.miss", "type", cacheType).increment();
        
        log.debug("[缓存指标] 缓存未命中，type={}, key={}", cacheType, cacheKey);
    }

    /**
     * 记录缓存响应时间
     *
     * @param cacheType 缓存类型
     * @param duration  响应时间（毫秒）
     */
    public void recordResponseTime(String cacheType, long duration) {
        meterRegistry.timer("cache.response.time", "type", cacheType).record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 获取缓存命中率
     *
     * @param cacheType 缓存类型
     * @return 命中率（0-100）
     */
    public double getHitRate(String cacheType) {
        CacheStats stats = cacheStatsMap.get(cacheType);
        if (stats == null || stats.total.get() == 0) {
            return 0.0;
        }
        
        long hits = stats.hits.get();
        long total = stats.total.get();
        double hitRate = (double) hits / total * 100;
        
        log.debug("[缓存指标] 命中率统计，type={}, hits={}, total={}, hitRate={}%", 
                cacheType, hits, total, String.format("%.2f", hitRate));
        
        return hitRate;
    }

    /**
     * 获取总体缓存命中率
     *
     * @return 总体命中率（0-100）
     */
    public double getOverallHitRate() {
        long totalHits = 0;
        long totalRequests = 0;
        
        for (CacheStats stats : cacheStatsMap.values()) {
            totalHits += stats.hits.get();
            totalRequests += stats.total.get();
        }
        
        if (totalRequests == 0) {
            return 0.0;
        }
        
        return (double) totalHits / totalRequests * 100;
    }

    /**
     * 获取缓存统计信息
     *
     * @param cacheType 缓存类型
     * @return 统计信息
     */
    public CacheStatsInfo getStats(String cacheType) {
        CacheStats stats = cacheStatsMap.get(cacheType);
        if (stats == null) {
            return new CacheStatsInfo(cacheType, 0, 0, 0, 0.0);
        }
        
        long hits = stats.hits.get();
        long misses = stats.misses.get();
        long total = stats.total.get();
        double hitRate = total > 0 ? (double) hits / total * 100 : 0.0;
        
        return new CacheStatsInfo(cacheType, hits, misses, total, hitRate);
    }

    /**
     * 重置统计信息
     *
     * @param cacheType 缓存类型（null表示重置所有）
     */
    public void resetStats(String cacheType) {
        if (cacheType == null) {
            cacheStatsMap.clear();
            log.info("[缓存指标] 重置所有缓存统计");
        } else {
            cacheStatsMap.remove(cacheType);
            log.info("[缓存指标] 重置缓存统计，type={}", cacheType);
        }
    }

    /**
     * 缓存统计内部类
     */
    private static class CacheStats {
        final AtomicLong hits = new AtomicLong(0);
        final AtomicLong misses = new AtomicLong(0);
        final AtomicLong total = new AtomicLong(0);
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStatsInfo {
        private final String cacheType;
        private final long hits;
        private final long misses;
        private final long total;
        private final double hitRate;

        public CacheStatsInfo(String cacheType, long hits, long misses, long total, double hitRate) {
            this.cacheType = cacheType;
            this.hits = hits;
            this.misses = misses;
            this.total = total;
            this.hitRate = hitRate;
        }

        public String getCacheType() {
            return cacheType;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public long getTotal() {
            return total;
        }

        public double getHitRate() {
            return hitRate;
        }
    }
}

