package net.lab1024.sa.consume.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存管理器
 *
 * 职责：实现L1（Caffeine）+ L2（Redis）两级缓存
 *
 * 缓存策略：
 * 1. 读取：先查L1 → 未命中查L2 → 未命中查DB → 写入L1和L2
 * 2. 写入：同时写入L1和L2
 * 3. 删除：同时删除L1和L2
 * 4. 过期：L1定时过期，L2 Redis自动过期
 *
 * 性能目标：
 * - 缓存命中率 ≥ 90%
 * - 平均响应时间 ≤ 5ms（缓存命中）
 * - L1缓存大小 ≤ 10000条
 * - L1缓存过期时间 = 5分钟
 * - L2缓存过期时间 = 30分钟
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
public class MultiLevelCacheManager<K, V> {

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
     * L2缓存过期时间（秒）
     */
    private final long l2ExpireSeconds;

    /**
     * 缓存统计
     */
    private final CacheStatistics statistics;

    /**
     * 构造函数
     *
     * @param cacheName 缓存名称
     * @param l2Cache Redis模板
     * @param l1MaxSize L1缓存最大条数
     * @param l1ExpireMinutes L1缓存过期时间（分钟）
     * @param l2ExpireSeconds L2缓存过期时间（秒）
     */
    public MultiLevelCacheManager(String cacheName,
                                   RedisTemplate<K, V> l2Cache,
                                   int l1MaxSize,
                                   int l1ExpireMinutes,
                                   long l2ExpireSeconds) {
        this.cacheName = cacheName;
        this.l2Cache = l2Cache;
        this.l2ExpireSeconds = l2ExpireSeconds;
        this.statistics = new CacheStatistics();

        // 配置Caffeine L1缓存
        this.l1Cache = Caffeine.newBuilder()
                .maximumSize(l1MaxSize)
                .expireAfterWrite(l1ExpireMinutes, TimeUnit.MINUTES)
                .recordStats() // 启用统计
                .build();

        log.info("[多级缓存] 初始化完成: cacheName={}, l1MaxSize={}, l1ExpireMinutes={}, l2ExpireSeconds={}",
                cacheName, l1MaxSize, l1ExpireMinutes, l2ExpireSeconds);
    }

    /**
     * 获取缓存值（读穿透策略）
     *
     * @param key 缓存键
     * @param loader 数据加载器（从数据库加载）
     * @return 缓存值
     */
    public V get(K key, Supplier<V> loader) {
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: 尝试从L1缓存获取
            V value = l1Cache.getIfPresent(key);
            if (value != null) {
                statistics.recordHit(CacheLevel.L1);
                log.debug("[多级缓存] L1命中: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
                return value;
            }

            // Step 2: 尝试从L2缓存获取
            value = l2Cache.opsForValue().get(key);
            if (value != null) {
                // 写入L1缓存（回填）
                l1Cache.put(key, value);
                statistics.recordHit(CacheLevel.L2);
                log.debug("[多级缓存] L2命中: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
                return value;
            }

            // Step 3: 从数据库加载
            statistics.recordMiss();
            value = loader.get();
            if (value != null) {
                // 写入L1和L2缓存
                put(key, value);
                log.debug("[多级缓存] 数据库加载: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
            }

            return value;

        } catch (Exception e) {
            log.error("[多级缓存] 获取异常: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            statistics.recordError();
            return null;
        }
    }

    /**
     * 获取缓存值（不加载）
     *
     * @param key 缓存键
     * @return 缓存值（不存在返回null）
     */
    public V getIfPresent(K key) {
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: 尝试从L1缓存获取
            V value = l1Cache.getIfPresent(key);
            if (value != null) {
                statistics.recordHit(CacheLevel.L1);
                log.debug("[多级缓存] L1命中: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
                return value;
            }

            // Step 2: 尝试从L2缓存获取
            value = l2Cache.opsForValue().get(key);
            if (value != null) {
                // 写入L1缓存
                l1Cache.put(key, value);
                statistics.recordHit(CacheLevel.L2);
                log.debug("[多级缓存] L2命中: cacheName={}, key={},耗时={}ms",
                        cacheName, key, System.currentTimeMillis() - startTime);
                return value;
            }

            statistics.recordMiss();
            return null;

        } catch (Exception e) {
            log.error("[多级缓存] 获取异常: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            statistics.recordError();
            return null;
        }
    }

    /**
     * 写入缓存（写穿透策略）
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    public void put(K key, V value) {
        if (key == null || value == null) {
            log.warn("[多级缓存] 写入失败：key或value为空");
            return;
        }

        try {
            // 同时写入L1和L2缓存
            l1Cache.put(key, value);
            l2Cache.opsForValue().set(key, value, l2ExpireSeconds, TimeUnit.SECONDS);

            log.debug("[多级缓存] 写入成功: cacheName={}, key={}", cacheName, key);

        } catch (Exception e) {
            log.error("[多级缓存] 写入异常: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            statistics.recordError();
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    public void invalidate(K key) {
        if (key == null) {
            return;
        }

        try {
            // 同时删除L1和L2缓存
            l1Cache.invalidate(key);
            l2Cache.delete(key);

            log.debug("[多级缓存] 删除成功: cacheName={}, key={}", cacheName, key);

        } catch (Exception e) {
            log.error("[多级缓存] 删除异常: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            statistics.recordError();
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     */
    public void invalidateAll(Iterable<K> keys) {
        if (keys == null) {
            return;
        }

        try {
            // 同时删除L1和L2缓存
            l1Cache.invalidateAll(keys);
            // 将 Iterable 转换为 List 以满足 RedisTemplate.delete() 的参数类型要求
            java.util.List<K> keyList = new java.util.ArrayList<>();
            keys.forEach(keyList::add);
            l2Cache.delete(keyList);

            log.debug("[多级缓存] 批量删除成功: cacheName={}, count={}", cacheName, keyList.size());

        } catch (Exception e) {
            log.error("[多级缓存] 批量删除异常: cacheName={}, error={}",
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
            // 注意：不能清空整个Redis，只能清空当前缓存namespace
            log.warn("[多级缓存] 清空L1缓存: cacheName={}", cacheName);

        } catch (Exception e) {
            log.error("[多级缓存] 清空异常: cacheName={}, error={}",
                    cacheName, e.getMessage(), e);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息
     */
    public CacheStatistics getStatistics() {
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
     * 缓存统计信息
     */
    public static class CacheStatistics {
        private long l1Hits = 0;
        private long l2Hits = 0;
        private long misses = 0;
        private long errors = 0;
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

        // Getters and Setters
        public long getL1Hits() { return l1Hits; }
        public long getL2Hits() { return l2Hits; }
        public long getMisses() { return misses; }
        public long getErrors() { return errors; }
        public double getL1HitRate() { return l1HitRate; }
        public void setL1HitRate(double l1HitRate) { this.l1HitRate = l1HitRate; }
        public long getL1RequestCount() { return l1RequestCount; }
        public void setL1RequestCount(long l1RequestCount) { this.l1RequestCount = l1RequestCount; }
        public long getL1HitCount() { return l1HitCount; }
        public void setL1HitCount(long l1HitCount) { this.l1HitCount = l1HitCount; }
        public long getL1MissCount() { return l1MissCount; }
        public void setL1MissCount(long l1MissCount) { this.l1MissCount = l1MissCount; }

        @Override
        public String toString() {
            return String.format(
                    "CacheStatistics{l1Hits=%d, l2Hits=%d, misses=%d, errors=%d, hitRate=%.2f%%}",
                    l1Hits, l2Hits, misses, errors, getOverallHitRate() * 100
            );
        }
    }
}
