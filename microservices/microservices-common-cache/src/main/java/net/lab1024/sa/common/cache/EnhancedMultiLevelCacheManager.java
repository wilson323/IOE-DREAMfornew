package net.lab1024.sa.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 增强版多级缓存管理器
 *
 * 职责：实现L1（Caffeine）+ L2（Redis）两级缓存 + 额外功能
 *
 * 新增功能：
 * 1. 缓存穿透防护（空值缓存）
 * 2. 缓存雪崩防护（随机过期时间）
 * 3. 熔断降级机制（缓存故障时降级）
 * 4. 缓存预热支持（批量预加载）
 *
 * 缓存策略：
 * - 读取：先查L1 → 未命中查L2 → 未命中查DB → 写入L1和L2
 * - 写入：同时写入L1和L2（带随机过期时间）
 * - 删除：同时删除L1和L2
 * - 预热：批量加载热点数据到缓存
 *
 * 性能目标：
 * - 缓存命中率 ≥ 90%
 * - 平均响应时间 ≤ 5ms（缓存命中）
 * - L1缓存大小 ≤ 10000条
 * - L1缓存过期时间 = 5分钟
 * - L2缓存过期时间 = 30分钟 ± 随机5分钟
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-26
 * @version 2.0.0 - 增强版
 */
@Slf4j
public class EnhancedMultiLevelCacheManager<K, V> {

    /**
     * 空值标记（用于缓存穿透防护）
     */
    private static final Object NULL_VALUE = new Object();

    /**
     * L1缓存：Caffeine本地缓存
     */
    private final Cache<K, V> l1Cache;

    /**
     * L2缓存：Redis分布式缓存
     */
    private final RedisTemplate<K, V> l2Cache;

    /**
     * 缓存名称（用于日志和监控）
     */
    private final String cacheName;

    /**
     * L2缓存基础过期时间（秒）
     */
    private final long l2ExpireSeconds;

    /**
     * L2缓存随机过期时间范围（秒）
     */
    private final long l2RandomExpireSeconds;

    /**
     * 是否启用缓存穿透防护
     */
    private final boolean enableCachePenetrationProtection;

    /**
     * 空值缓存过期时间（秒）
     */
    private final long nullValueExpireSeconds;

    /**
     * 缓存统计
     */
    private final EnhancedCacheStatistics statistics;

    /**
     * 随机数生成器（用于缓存雪崩防护）
     */
    private final Random random = new Random();

    /**
     * 熔断器状态
     */
    private volatile boolean circuitBreakerOpen = false;

    /**
     * 熔断器恢复时间戳
     */
    private volatile long circuitBreakerRecoverTime = 0;

    /**
     * 熔断器失败阈值
     */
    private final int circuitBreakerFailureThreshold;

    /**
     * 熔断器恢复时间（毫秒）
     */
    private final long circuitBreakerRecoverDuration;

    /**
     * 构造函数
     *
     * @param cacheName 缓存名称
     * @param l2Cache Redis模板
     * @param l1MaxSize L1缓存最大条数
     * @param l1ExpireMinutes L1缓存过期时间（分钟）
     * @param l2ExpireSeconds L2缓存基础过期时间（秒）
     */
    public EnhancedMultiLevelCacheManager(String cacheName,
                                          RedisTemplate<K, V> l2Cache,
                                          int l1MaxSize,
                                          int l1ExpireMinutes,
                                          long l2ExpireSeconds) {
        this(cacheName, l2Cache, l1MaxSize, l1ExpireMinutes, l2ExpireSeconds,
                300,  // 5分钟随机过期时间
                true,  // 启用缓存穿透防护
                60,    // 空值缓存1分钟
                10,    // 熔断器失败阈值10次
                60000  // 熔断器恢复时间1分钟
        );
    }

    /**
     * 完整构造函数
     */
    public EnhancedMultiLevelCacheManager(String cacheName,
                                          RedisTemplate<K, V> l2Cache,
                                          int l1MaxSize,
                                          int l1ExpireMinutes,
                                          long l2ExpireSeconds,
                                          long l2RandomExpireSeconds,
                                          boolean enableCachePenetrationProtection,
                                          long nullValueExpireSeconds,
                                          int circuitBreakerFailureThreshold,
                                          long circuitBreakerRecoverDuration) {
        this.cacheName = cacheName;
        this.l2Cache = l2Cache;
        this.l2ExpireSeconds = l2ExpireSeconds;
        this.l2RandomExpireSeconds = l2RandomExpireSeconds;
        this.enableCachePenetrationProtection = enableCachePenetrationProtection;
        this.nullValueExpireSeconds = nullValueExpireSeconds;
        this.circuitBreakerFailureThreshold = circuitBreakerFailureThreshold;
        this.circuitBreakerRecoverDuration = circuitBreakerRecoverDuration;
        this.statistics = new EnhancedCacheStatistics();

        // 配置Caffeine L1缓存
        this.l1Cache = Caffeine.newBuilder()
                .maximumSize(l1MaxSize)
                .expireAfterWrite(l1ExpireMinutes, TimeUnit.MINUTES)
                .recordStats()
                .build();

        log.info("[增强多级缓存] 初始化完成: cacheName={}, l1MaxSize={}, l1ExpireMinutes={}, " +
                        "l2ExpireSeconds={}, l2RandomExpireSeconds={}, enablePenetrationProtection={}",
                cacheName, l1MaxSize, l1ExpireMinutes, l2ExpireSeconds,
                l2RandomExpireSeconds, enableCachePenetrationProtection);
    }

    /**
     * 获取缓存值（读穿透策略 + 熔断保护）
     *
     * @param key 缓存键
     * @param loader 数据加载器（从数据库加载）
     * @return 缓存值
     */
    public V get(K key, Supplier<V> loader) {
        // 检查熔断器状态
        if (circuitBreakerOpen) {
            if (System.currentTimeMillis() > circuitBreakerRecoverTime) {
                // 尝试恢复
                circuitBreakerOpen = false;
                log.warn("[增强多级缓存] 熔断器尝试恢复: cacheName={}", cacheName);
            } else {
                // 熔断器开启，直接返回null或降级数据
                statistics.recordCircuitBreakerOpen();
                log.warn("[增强多级缓存] 熔断器开启，降级处理: cacheName={}, key={}", cacheName, key);
                return null;
            }
        }

        long startTime = System.currentTimeMillis();

        try {
            // Step 1: 尝试从L1缓存获取
            V value = l1Cache.getIfPresent(key);
            if (value != null) {
                // 检查是否是空值标记
                if (enableCachePenetrationProtection && value == NULL_VALUE) {
                    statistics.recordNullValueHit();
                    log.debug("[增强多级缓存] 空值缓存命中: cacheName={}, key={},耗时={}ms",
                            cacheName, key, System.currentTimeMillis() - startTime);
                    return null;
                }

                statistics.recordHit(CacheLevel.L1);
                log.debug("[增强多级缓存] L1命中: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
                return value;
            }

            // Step 2: 尝试从L2缓存获取
            value = l2Cache.opsForValue().get(key);
            if (value != null) {
                // 检查是否是空值标记
                if (enableCachePenetrationProtection && isNullValue(value)) {
                    // 写入L1空值缓存
                    l1Cache.put(key, (V) NULL_VALUE);
                    statistics.recordNullValueHit();
                    log.debug("[增强多级缓存] L2空值缓存命中: cacheName={}, key={},耗时={}ms",
                            cacheName, key, System.currentTimeMillis() - startTime);
                    return null;
                }

                // 写入L1缓存（回填）
                l1Cache.put(key, value);
                statistics.recordHit(CacheLevel.L2);
                log.debug("[增强多级缓存] L2命中: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
                return value;
            }

            // Step 3: 从数据库加载
            statistics.recordMiss();
            value = loader.get();

            if (value != null) {
                // 写入L1和L2缓存（带随机过期时间）
                put(key, value);
                log.debug("[增强多级缓存] 数据库加载: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
            } else {
                // 缓存穿透防护：缓存空值
                if (enableCachePenetrationProtection) {
                    putNullValue(key);
                    log.debug("[增强多级缓存] 空值缓存: cacheName={}, key={},耗时={}ms",
                            cacheName, key, System.currentTimeMillis() - startTime);
                }
            }

            return value;

        } catch (Exception e) {
            log.error("[增强多级缓存] 获取异常: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            statistics.recordError();

            // 检查是否需要打开熔断器
            checkCircuitBreaker();

            return null;
        }
    }

    /**
     * 获取缓存值（不加载）
     */
    public V getIfPresent(K key) {
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: 尝试从L1缓存获取
            V value = l1Cache.getIfPresent(key);
            if (value != null) {
                if (enableCachePenetrationProtection && value == NULL_VALUE) {
                    statistics.recordNullValueHit();
                    return null;
                }

                statistics.recordHit(CacheLevel.L1);
                log.debug("[增强多级缓存] L1命中: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
                return value;
            }

            // Step 2: 尝试从L2缓存获取
            value = l2Cache.opsForValue().get(key);
            if (value != null) {
                if (enableCachePenetrationProtection && isNullValue(value)) {
                    l1Cache.put(key, (V) NULL_VALUE);
                    statistics.recordNullValueHit();
                    return null;
                }

                // 写入L1缓存
                l1Cache.put(key, value);
                statistics.recordHit(CacheLevel.L2);
                log.debug("[增强多级缓存] L2命中: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
                return value;
            }

            statistics.recordMiss();
            return null;

        } catch (Exception e) {
            log.error("[增强多级缓存] 获取异常: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            statistics.recordError();
            return null;
        }
    }

    /**
     * 写入缓存（写穿透策略 + 随机过期时间）
     */
    public void put(K key, V value) {
        if (key == null || value == null) {
            log.warn("[增强多级缓存] 写入失败：key或value为空");
            return;
        }

        try {
            // 同时写入L1和L2缓存
            l1Cache.put(key, value);

            // 计算随机过期时间（防止缓存雪崩）
            long expireSeconds = calculateRandomExpireTime();
            l2Cache.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);

            log.debug("[增强多级缓存] 写入成功: cacheName={}, key={}, expireSeconds={}",
                    cacheName, key, expireSeconds);

        } catch (Exception e) {
            log.error("[增强多级缓存] 写入异常: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            statistics.recordError();
            checkCircuitBreaker();
        }
    }

    /**
     * 缓存空值（防止缓存穿透）
     */
    private void putNullValue(K key) {
        try {
            // L1缓存空值标记
            l1Cache.put(key, (V) NULL_VALUE);

            // L2缓存空值（短过期时间）
            l2Cache.opsForValue().set(key, (V) NULL_VALUE, nullValueExpireSeconds, TimeUnit.SECONDS);

            log.debug("[增强多级缓存] 空值缓存成功: cacheName={}, key={}, nullValueExpireSeconds={}",
                    cacheName, key, nullValueExpireSeconds);

        } catch (Exception e) {
            log.error("[增强多级缓存] 空值缓存异常: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
        }
    }

    /**
     * 计算随机过期时间（防止缓存雪崩）
     */
    private long calculateRandomExpireTime() {
        if (l2RandomExpireSeconds <= 0) {
            return l2ExpireSeconds;
        }
        // 基础过期时间 + 随机时间（±50%范围）
        long randomTime = (long) (random.nextDouble() * l2RandomExpireSeconds);
        return l2ExpireSeconds + randomTime - (l2RandomExpireSeconds / 2);
    }

    /**
     * 判断是否是空值标记
     */
    private boolean isNullValue(V value) {
        return value == NULL_VALUE;
    }

    /**
     * 删除缓存
     */
    public void invalidate(K key) {
        if (key == null) {
            return;
        }

        try {
            // 同时删除L1和L2缓存
            l1Cache.invalidate(key);
            l2Cache.delete(key);

            log.debug("[增强多级缓存] 删除成功: cacheName={}, key={}", cacheName, key);

        } catch (Exception e) {
            log.error("[增强多级缓存] 删除异常: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            statistics.recordError();
        }
    }

    /**
     * 批量删除缓存
     */
    public void invalidateAll(Iterable<K> keys) {
        if (keys == null) {
            return;
        }

        try {
            // 同时删除L1和L2缓存
            l1Cache.invalidateAll(keys);
            java.util.List<K> keyList = new java.util.ArrayList<>();
            keys.forEach(keyList::add);
            l2Cache.delete(keyList);

            log.debug("[增强多级缓存] 批量删除成功: cacheName={}, count={}", cacheName, keyList.size());

        } catch (Exception e) {
            log.error("[增强多级缓存] 批量删除异常: cacheName={}, error={}",
                    cacheName, e.getMessage(), e);
            statistics.recordError();
        }
    }

    /**
     * 清空所有缓存
     */
    public void invalidateAll() {
        try {
            l1Cache.invalidateAll();
            log.warn("[增强多级缓存] 清空L1缓存: cacheName={}", cacheName);
        } catch (Exception e) {
            log.error("[增强多级缓存] 清空异常: cacheName={}, error={}",
                    cacheName, e.getMessage(), e);
        }
    }

    /**
     * 缓存预热（批量加载）
     */
    public void warmUp(Map<K, Supplier<V>> dataLoaders) {
        if (dataLoaders == null || dataLoaders.isEmpty()) {
            log.warn("[增强多级缓存] 预热数据为空: cacheName={}", cacheName);
            return;
        }

        log.info("[增强多级缓存] 开始缓存预热: cacheName={}, count={}", cacheName, dataLoaders.size());

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<K, Supplier<V>> entry : dataLoaders.entrySet()) {
            try {
                K key = entry.getKey();
                Supplier<V> loader = entry.getValue();
                V value = loader.get();

                if (value != null) {
                    put(key, value);
                    successCount++;
                } else {
                    log.debug("[增强多级缓存] 预热数据为空: cacheName={}, key={}", cacheName, key);
                }

            } catch (Exception e) {
                failureCount++;
                log.error("[增强多级缓存] 预热失败: cacheName={}, key={}, error={}",
                        cacheName, entry.getKey(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[增强多级缓存] 缓存预热完成: cacheName={}, success={}, failure={}, duration={}ms",
                cacheName, successCount, failureCount, duration);
    }

    /**
     * 检查熔断器状态
     */
    private void checkCircuitBreaker() {
        if (statistics.getErrorCount() >= circuitBreakerFailureThreshold) {
            circuitBreakerOpen = true;
            circuitBreakerRecoverTime = System.currentTimeMillis() + circuitBreakerRecoverDuration;
            log.error("[增强多级缓存] 熔断器打开: cacheName={}, errorCount={}, recoverTime={}",
                    cacheName, statistics.getErrorCount(), circuitBreakerRecoverDuration);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public EnhancedCacheStatistics getStatistics() {
        com.github.benmanes.caffeine.cache.stats.CacheStats l1Stats = l1Cache.stats();
        statistics.setL1HitRate(l1Stats.hitRate());
        statistics.setL1RequestCount(l1Stats.requestCount());
        statistics.setL1HitCount(l1Stats.hitCount());
        statistics.setL1MissCount(l1Stats.missCount());

        return statistics;
    }

    /**
     * 缓存级别枚举
     */
    public enum CacheLevel {
        L1, L2
    }

    /**
     * 增强版缓存统计信息
     */
    public static class EnhancedCacheStatistics {
        private long l1Hits = 0;
        private long l2Hits = 0;
        private long misses = 0;
        private long errors = 0;
        private long nullValueHits = 0;
        private long circuitBreakerOpens = 0;
        private double l1HitRate = 0.0;
        private long l1RequestCount = 0;
        private long l1HitCount = 0;
        private long l1MissCount = 0;

        public void recordHit(CacheLevel level) {
            if (level == CacheLevel.L1) {
                l1Hits++;
            } else {
                l2Hits++;
            }
        }

        public void recordMiss() {
            misses++;
        }

        public void recordError() {
            errors++;
        }

        public void recordNullValueHit() {
            nullValueHits++;
        }

        public void recordCircuitBreakerOpen() {
            circuitBreakerOpens++;
        }

        public double getOverallHitRate() {
            long totalRequests = l1Hits + l2Hits + misses;
            if (totalRequests == 0) {
                return 0.0;
            }
            return (double) (l1Hits + l2Hits) / totalRequests;
        }

        public long getTotalRequests() {
            return l1Hits + l2Hits + misses;
        }

        public long getErrorCount() {
            return errors;
        }

        // Getters and Setters
        public long getL1Hits() { return l1Hits; }
        public long getL2Hits() { return l2Hits; }
        public long getMisses() { return misses; }
        public long getErrors() { return errors; }
        public long getNullValueHits() { return nullValueHits; }
        public long getCircuitBreakerOpens() { return circuitBreakerOpens; }
        public double getL1HitRate() { return l1HitRate; }
        public void setL1HitRate(double l1HitRate) { this.l1HitRate = l1HitRate; }
        public long getL1RequestCount() { return l1RequestCount; }
        public void setL1RequestCount(long l1RequestCount) { this.l1RequestCount = l1RequestCount; }
        public long getL1HitCount() { return l1HitCount; }
        public void setL1HitCount(long l1HitCount) { this.l1HitCount = l1HitCount; }
        public long getL1MissCount() { return l1MissCount; }
        public void setL1MissCount(long l1MissCount) { this.l1MissCount = l1MissCount; }
    }
}
