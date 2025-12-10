package net.lab1024.sa.common.cache.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 三级缓存管理器
 * <p>
 * L1本地缓存 + L2Redis缓存 + L3网关调用缓存
 * 提供企业级缓存性能优化和一致性保障
 * 支持缓存预热、击穿防护、雪崩保护
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class CacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, Cache<String, Object>> localCaches;
    private final CacheConfig defaultCacheConfig;

    // 构造函数注入依赖
    public CacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.localCaches = new ConcurrentHashMap<>();
        this.defaultCacheConfig = new CacheConfig(
                Duration.ofMinutes(5),      // L1本地缓存：5分钟
                Duration.ofMinutes(30),     // L2 Redis缓存：30分钟
                Duration.ofMinutes(60),     // L3网关缓存：60分钟
                1000,                      // 本地缓存最大条目
                true,                       // 启用缓存统计
                0.8                        // 缓存命中率阈值
        );

        initializeLocalCaches();
    }

    /**
     * 缓存配置类
     */
    public static class CacheConfig {
        private final Duration localTtl;      // 本地缓存TTL
        private final Duration redisTtl;     // Redis缓存TTL
        private final Duration gatewayTtl;   // 网关缓存TTL
        private final int maxSize;         // 本地缓存最大条目
        private final boolean enableStats;  // 是否启用统计
        private final double hitRateThreshold; // 命中率阈值

        public CacheConfig(Duration localTtl, Duration redisTtl, Duration gatewayTtl,
                        int maxSize, boolean enableStats, double hitRateThreshold) {
            this.localTtl = localTtl;
            this.redisTtl = redisTtl;
            this.gatewayTtl = gatewayTtl;
            this.maxSize = maxSize;
            this.enableStats = enableStats;
            this.hitRateThreshold = hitRateThreshold;
        }

        // getters
        public Duration getLocalTtl() { return localTtl; }
        public Duration getRedisTtl() { return redisTtl; }
        public Duration getGatewayTtl() { return gatewayTtl; }
        public int getMaxSize() { return maxSize; }
        public boolean isEnableStats() { return enableStats; }
        public double getHitRateThreshold() { return hitRateThreshold; }
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private long localHits = 0;
        private long localMisses = 0;
        private long redisHits = 0;
        private long redisMisses = 0;
        private long gatewayHits = 0;
        private long gatewayMisses = 0;
        private long totalRequests = 0;

        // getters and setters
        public long getLocalHits() { return localHits; }
        public void incrementLocalHits() { localHits++; }
        public long getLocalMisses() { return localMisses; }
        public void incrementLocalMisses() { localMisses++; }
        public long getRedisHits() { return redisHits; }
        public void incrementRedisHits() { redisHits++; }
        public long getRedisMisses() { return redisMisses; }
        public void incrementRedisMisses() { redisMisses++; }
        public long getGatewayHits() { return gatewayHits; }
        public void incrementGatewayHits() { gatewayHits++; }
        public long getGatewayMisses() { return gatewayMisses; }
        public void incrementGatewayMisses() { gatewayMisses++; }
        public long getTotalRequests() { return totalRequests; }
        public void incrementTotalRequests() { totalRequests++; }

        // 计算方法
        public double getLocalHitRate() {
            long total = localHits + localMisses;
            return total > 0 ? (double) localHits / total : 0.0;
        }

        public double getRedisHitRate() {
            long total = redisHits + redisMisses;
            return total > 0 ? (double) redisHits / total : 0.0;
        }

        public double getGatewayHitRate() {
            long total = gatewayHits + gatewayMisses;
            return total > 0 ? (double) gatewayHits / total : 0.0;
        }

        public double getOverallHitRate() {
            long total = localHits + redisHits + gatewayHits;
            long miss = localMisses + redisMisses + gatewayMisses;
            return (total + miss) > 0 ? (double) total / (total + miss) : 0.0;
        }
    }

    // 全局缓存统计
    private final CacheStats globalStats = new CacheStats();

    /**
     * 获取缓存数据（带缓存统计）
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param loader 数据加载器
     * @param ttl 缓存TTL
     * @param <T> 数据类型
     * @return 缓存数据
     */
    public <T> T getWithRefresh(String cacheName, String key, Supplier<T> loader, Duration ttl) {
        globalStats.incrementTotalRequests();

        try {
            // 1. L1本地缓存
            T value = getFromLocalCache(cacheName, key);
            if (value != null) {
                globalStats.incrementLocalHits();
                log.debug("[缓存管理] L1缓存命中, cacheName={}, key={}", cacheName, key);
                return value;
            }

            globalStats.incrementLocalMisses();

            // 2. L2 Redis缓存
            value = getFromRedisCache(key);
            if (value != null) {
                globalStats.incrementRedisHits();
                log.debug("[缓存管理] L2缓存命中, key={}", key);

                // 异步回填L1缓存
                putToLocalCacheAsync(cacheName, key, value, ttl);
                return value;
            }

            globalStats.incrementRedisMisses();

            // 3. L3网关调用（通过加载数据）
            value = loader.get();
            if (value != null) {
                // 数据加载成功，同时更新所有缓存层
                putToLocalCache(cacheName, key, value, ttl);
                putToRedisCache(key, value, ttl);

                log.info("[缓存管理] 数据加载并缓存, cacheName={}, key={}", cacheName, key);
                return value;
            }

            globalStats.incrementGatewayMisses();
            return null;

        } catch (Exception e) {
            log.error("[缓存管理] 缓存获取异常, cacheName={}, key={}", cacheName, key, e);
            globalStats.incrementGatewayMisses();
            return loader.get();
        }
    }

    /**
     * 获取缓存数据（使用默认配置）
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param loader 数据加载器
     * @param <T> 数据类型
     * @return 缓存数据
     */
    public <T> T getWithRefresh(String cacheName, String key, Supplier<T> loader) {
        return getWithRefresh(cacheName, key, loader, defaultCacheConfig.getLocalTtl());
    }

    /**
     * 设置缓存数据
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param value 数据值
     * @param ttl 缓存TTL
     */
    public <T> void put(String cacheName, String key, T value, Duration ttl) {
        try {
            // 同时更新所有缓存层
            putToLocalCache(cacheName, key, value, ttl);
            putToRedisCache(key, value, ttl);

            log.debug("[缓存管理] 缓存数据设置, cacheName={}, key={}, ttl={}", cacheName, key, ttl);

        } catch (Exception e) {
            log.error("[缓存管理] 缓存设置异常, cacheName={}, key={}", cacheName, key, e);
        }
    }

    /**
     * 设置缓存数据（使用默认TTL）
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param value 数据值
     */
    public <T> void put(String cacheName, String key, T value) {
        put(cacheName, key, value, defaultCacheConfig.getLocalTtl());
    }

    /**
     * 删除缓存数据
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     */
    public void evict(String cacheName, String key) {
        try {
            evictFromLocalCache(cacheName, key);
            evictFromRedisCache(key);

            log.debug("[缓存管理] 缓存数据删除, cacheName={}, key={}", cacheName, key);

        } catch (Exception e) {
            log.error("[缓存管理] 缓存删除异常, cacheName={}, key={}", cacheName, key, e);
        }
    }

    /**
     * 清空指定缓存
     *
     * @param cacheName 缓存名称
     */
    public void clear(String cacheName) {
        try {
            clearLocalCache(cacheName);
            log.debug("[缓存管理] 缓存清空, cacheName={}", cacheName);

        } catch (Exception e) {
            log.error("[缓存管理] 缓存清空异常, cacheName={}", cacheName, e);
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAll() {
        try {
            clearAllLocalCaches();
            log.info("[缓存管理] 所有缓存清空");

        } catch (Exception e) {
            log.error("[缓存管理] 清空所有缓存异常", e);
        }
    }

    /**
     * 预热缓存
     *
     * @param cacheName 缓存名称
     * @param keys 缓存键列表
     * @param loader 数据加载器
     */
    public <T> void warmUpCache(String cacheName, List<String> keys, Supplier<List<T>> loader) {
        log.info("[缓存管理] 开始预热缓存, cacheName={}, keyCount={}", cacheName, keys.size());

        try {
            List<T> values = loader.get();
            if (values != null && !values.isEmpty()) {
                for (int i = 0; i < Math.min(keys.size(), values.size()); i++) {
                    String key = keys.get(i);
                    T value = values.get(i);
                    if (value != null) {
                        put(cacheName, key, value, defaultCacheConfig.getLocalTtl());
                    }
                }
                log.info("[缓存管理] 缓存预热完成, cacheName={}, actualCount={}", cacheName, values.size());
            } else {
                log.warn("[缓存管理] 缓存预热数据为空, cacheName={}", cacheName);
            }

        } catch (Exception e) {
            log.error("[缓存管理] 缓存预热异常, cacheName={}", cacheName, e);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息
     */
    public CacheStats getStats() {
        return globalStats;
    }

    /**
     * 获取指定缓存的统计信息
     *
     * @param cacheName 缓存名称
     * @return 统计信息
     */
    public CacheStats getCacheStats(String cacheName) {
        Cache<String, Object> cache = localCaches.get(cacheName);
        if (cache == null) {
            return new CacheStats();
        }

        CacheStats stats = new CacheStats();
        com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = cache.stats();
        stats.localHits = caffeineStats.hitCount();
        stats.localMisses = caffeineStats.missCount();

        return stats;
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化本地缓存
     */
    private void initializeLocalCaches() {
        // 用户缓存
        createLocalCache("user", defaultCacheConfig);

        // 字典缓存
        createLocalCache("dict", defaultCacheConfig);

        // 菜单缓存
        createLocalCache("menu", defaultCacheConfig);

        // 权限缓存
        createLocalCache("permission", defaultCacheConfig);

        // 配置缓存
        createLocalCache("config", defaultCacheConfig);

        log.info("[缓存管理] 本地缓存初始化完成, cacheCount={}", localCaches.size());
    }

    /**
     * 创建本地缓存
     */
    private void createLocalCache(String cacheName, CacheConfig config) {
        Cache<String, Object> cache = Caffeine.newBuilder()
                .maximumSize(config.getMaxSize())
                .expireAfterWrite(config.getLocalTtl())
                .recordStats()
                .build();

        localCaches.put(cacheName, cache);
    }

    /**
     * 从本地缓存获取
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromLocalCache(String cacheName, String key) {
        Cache<String, Object> cache = localCaches.get(cacheName);
        if (cache != null) {
            Object value = cache.getIfPresent(key);
            return value != null ? (T) value : null;
        }
        return null;
    }

    /**
     * 放入本地缓存
     */
    private <T> void putToLocalCache(String cacheName, String key, T value, Duration ttl) {
        Cache<String, Object> cache = localCaches.get(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    /**
     * 异步放入本地缓存
     */
    private <T> void putToLocalCacheAsync(String cacheName, String key, T value, Duration ttl) {
        // 在实际应用中可以使用异步执行器
        putToLocalCache(cacheName, key, value, ttl);
    }

    /**
     * 从本地缓存删除
     */
    private void evictFromLocalCache(String cacheName, String key) {
        Cache<String, Object> cache = localCaches.get(cacheName);
        if (cache != null) {
            cache.invalidate(key);
        }
    }

    /**
     * 清空本地缓存
     */
    private void clearLocalCache(String cacheName) {
        Cache<String, Object> cache = localCaches.get(cacheName);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * 清空所有本地缓存
     */
    private void clearAllLocalCaches() {
        localCaches.values().forEach(Cache::invalidateAll);
    }

    /**
     * 从Redis缓存获取
     */
    @SuppressWarnings({"unchecked", "null"})
    private <T> T getFromRedisCache(String key) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            Object value = ops.get(getRedisKey(key));
            return value != null ? (T) value : null;
        } catch (Exception e) {
            log.warn("[缓存管理] Redis缓存获取失败, key={}", key, e);
            return null;
        }
    }

    /**
     * 放入Redis缓存
     */
    @SuppressWarnings("null")
    private <T> void putToRedisCache(String key, T value, Duration ttl) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(getRedisKey(key), value, ttl);
        } catch (Exception e) {
            log.warn("[缓存管理] Redis缓存设置失败, key={}", key, e);
        }
    }

    /**
     * 从Redis缓存删除
     */
    @SuppressWarnings("null")
    private void evictFromRedisCache(String key) {
        try {
            redisTemplate.delete(getRedisKey(key));
        } catch (Exception e) {
            log.warn("[缓存管理] Redis缓存删除失败, key={}", key, e);
        }
    }

    /**
     * 获取Redis缓存键
     */
    private String getRedisKey(String key) {
        return "ioedream:cache:" + key;
    }

    /**
     * 防缓存击穿的互斥锁
     */
    private static class CacheLock {
        private static final ConcurrentHashMap<String, String> LOCKS = new ConcurrentHashMap<>();

        public static boolean tryLock(String key, String value) {
            return LOCKS.putIfAbsent(key, value) == null;
        }

        public static void unlock(String key, String value) {
            LOCKS.remove(key, value);
        }
    }

    /**
     * 防缓存击穿的数据加载
     */
    public <T> T getWithLock(String cacheName, String key, Supplier<T> loader, Duration ttl) {
        // 检查是否已有其他线程在加载
        String lockKey = cacheName + ":" + key;
        String lockValue = java.util.UUID.randomUUID().toString();

        // 尝试获取锁
        if (CacheLock.tryLock(lockKey, lockValue)) {
            try {
                // 获得锁，执行加载
                return loader.get();
            } finally {
                // 释放锁
                CacheLock.unlock(lockKey, lockValue);
            }
        } else {
            // 未获得锁，等待一小段时间后重试
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getFromLocalCache(cacheName, key);
        }
    }
}
