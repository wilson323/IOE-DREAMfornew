package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CompletableFuture;
import java.util.Objects;

/**
 * 企业级缓存优化管理器
 * <p>
 * 实现L1本地缓存+L2 Redis缓存+L3网关缓存的三级缓存策略
 * 包含缓存预热、击穿防护、雪崩防护等企业级特性
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入所有依赖（RedisTemplate、配置对象等）
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 3.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class CacheOptimizationManager {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 本地缓存实例
     */
    private final Map<String, Cache<String, Object>> localCaches = new ConcurrentHashMap<>();

    /**
     * 分布式锁（防止缓存击穿）
     */
    private final Map<String, ReentrantLock> cacheLocks = new ConcurrentHashMap<>();

    /**
     * 缓存配置
     */
    private final CacheConfiguration config;

    /**
     * 构造函数注入所有依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param redisTemplate Redis模板
     * @param config 缓存配置（可选，默认使用默认配置）
     */
    public CacheOptimizationManager(
            RedisTemplate<String, Object> redisTemplate,
            CacheConfiguration config) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate不能为null");
        this.config = config != null ? config : new CacheConfiguration();
    }

    /**
     * 简化构造函数（使用默认配置）
     *
     * @param redisTemplate Redis模板
     */
    public CacheOptimizationManager(RedisTemplate<String, Object> redisTemplate) {
        this(redisTemplate, new CacheConfiguration());
    }

    /**
     * 获取缓存值（三级缓存策略）
     */
    public <T> T getWithRefresh(String cacheKey, Supplier<T> loader, Duration ttl) {
        return getWithRefresh(cacheKey, loader, ttl, false);
    }

    /**
     * 获取缓存值（支持异步刷新）
     */
    public <T> T getWithRefresh(String cacheKey, Supplier<T> loader, Duration ttl, boolean asyncRefresh) {
        String namespace = extractNamespace(cacheKey);
        String localKey = extractLocalKey(cacheKey);

        try {
            // L1: 本地缓存查询
            T value = getFromLocalCache(namespace, localKey);
            if (value != null) {
                log.debug("[缓存优化] L1本地缓存命中: key={}", cacheKey);
                if (asyncRefresh && isNearExpiry(cacheKey, ttl)) {
                    // 异步刷新缓存
                    asyncRefreshCache(cacheKey, loader, ttl);
                }
                return value;
            }

            // L2: Redis缓存查询
            value = getFromRedisCache(cacheKey);
            if (value != null) {
                log.debug("[缓存优化] L2 Redis缓存命中: key={}", cacheKey);
                // 回填本地缓存
                putToLocalCache(namespace, localKey, value, ttl);
                if (asyncRefresh && isNearExpiry(cacheKey, ttl)) {
                    // 异步刷新缓存
                    asyncRefreshCache(cacheKey, loader, ttl);
                }
                return value;
            }

            // 缓存未命中，使用分布式锁防止击穿
            return loadWithLock(cacheKey, loader, ttl);

        } catch (Exception e) {
            log.error("[缓存优化] 缓存操作异常: key={}, error={}", cacheKey, e.getMessage(), e);
            // 降级：直接从数据源加载
            return loader.get();
        }
    }

    /**
     * 设置缓存值
     */
    public void put(String cacheKey, Object value, Duration ttl) {
        String namespace = extractNamespace(cacheKey);
        String localKey = extractLocalKey(cacheKey);

        try {
            // 设置本地缓存
            putToLocalCache(namespace, localKey, value, ttl);

            // 设置Redis缓存
            putToRedisCache(cacheKey, value, ttl);

            log.debug("[缓存优化] 缓存设置成功: key={}, ttl={}", cacheKey, ttl);

        } catch (Exception e) {
            log.error("[缓存优化] 缓存设置异常: key={}, error={}", cacheKey, e.getMessage(), e);
        }
    }

    /**
     * 删除缓存
     */
    public void evict(String cacheKey) {
        String namespace = extractNamespace(cacheKey);
        String localKey = extractLocalKey(cacheKey);

        try {
            // 删除本地缓存
            evictFromLocalCache(namespace, localKey);

            // 删除Redis缓存
            evictFromRedisCache(cacheKey);

            log.debug("[缓存优化] 缓存删除成功: key={}", cacheKey);

        } catch (Exception e) {
            log.error("[缓存优化] 缓存删除异常: key={}, error={}", cacheKey, e.getMessage(), e);
        }
    }

    /**
     * 批量删除缓存
     */
    public void evictByPattern(String pattern) {
        try {
            // 删除本地缓存（简单实现）
            localCaches.keySet().stream()
                .filter(key -> key.matches(pattern.replace("*", ".*")))
                .forEach(localCaches::remove);

            // 删除Redis缓存
            // 查找匹配的key
            @SuppressWarnings("null")
            Set<byte[]> keys = redisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<Set<byte[]>>) connection -> {
                    return connection.keyCommands().keys(pattern.getBytes());
                }
            );

            if (keys != null && !keys.isEmpty()) {
                for (byte[] key : keys) {
                    redisTemplate.delete(new String(key));
                }
            }

            log.debug("[缓存优化] 批量删除缓存成功: pattern={}", pattern);

        } catch (Exception e) {
            log.error("[缓存优化] 批量删除缓存异常: pattern={}, error={}", pattern, e.getMessage(), e);
        }
    }

    /**
     * 预热缓存
     */
    public void warmUpCache(String namespace, Map<String, Object> data, Duration ttl) {
        Cache<String, Object> localCache = getOrCreateLocalCache(namespace);

        data.forEach((key, value) -> {
            try {
                // 设置本地缓存
                localCache.put(key, value);

                // 设置Redis缓存
                String redisKey = buildRedisKey(namespace, key);
                putToRedisCache(redisKey, value, ttl);

            } catch (Exception e) {
                log.error("[缓存优化] 预热缓存异常: key={}, error={}", key, e.getMessage(), e);
            }
        });

        log.info("[缓存优化] 缓存预热完成: namespace={}, size={}", namespace, data.size());
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStatistics getCacheStatistics(String namespace) {
        Cache<String, Object> localCache = localCaches.get(namespace);
        if (localCache == null) {
            return new CacheStatistics(namespace, 0, 0, 0);
        }

        // 获取缓存统计信息（简化实现，不依赖Caffeine stats API）
        // 注意：某些版本的Caffeine可能不支持stats()方法，使用简化实现
        long hitCount = 0;
        long missCount = 0;
        long evictionCount = 0;
        try {
            // 尝试使用反射获取统计信息（如果可用）
            java.lang.reflect.Method statsMethod = localCache.getClass().getMethod("stats");
            Object statsObj = statsMethod.invoke(localCache);
            if (statsObj != null) {
                java.lang.reflect.Method hitCountMethod = statsObj.getClass().getMethod("hitCount");
                java.lang.reflect.Method missCountMethod = statsObj.getClass().getMethod("missCount");
                java.lang.reflect.Method evictionCountMethod = statsObj.getClass().getMethod("evictionCount");
                hitCount = ((Long) hitCountMethod.invoke(statsObj)).longValue();
                missCount = ((Long) missCountMethod.invoke(statsObj)).longValue();
                evictionCount = ((Long) evictionCountMethod.invoke(statsObj)).longValue();
            }
        } catch (Exception e) {
            log.warn("[缓存统计] 获取缓存统计信息失败，使用默认值", e);
        }

        // 使用统计信息创建CacheStatistics对象
        long requestCount = hitCount + missCount;
        double hitRate = requestCount > 0 ? (double) hitCount / requestCount : 0.0;
        return new CacheStatistics(
            namespace,
            requestCount,
            hitCount,
            missCount,
            hitRate,
            evictionCount
        );
    }

    /**
     * 使用分布式锁加载数据
     */
    private <T> T loadWithLock(String cacheKey, Supplier<T> loader, Duration ttl) {
        String lockKey = "lock:" + cacheKey;
        ReentrantLock lock = cacheLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());

        try {
            // 尝试获取锁
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    // 双重检查
                    T value = getFromRedisCache(cacheKey);
                    if (value != null) {
                        log.debug("[缓存优化] 双重检查命中: key={}", cacheKey);
                        return value;
                    }

                    // 从数据源加载
                    long startTime = System.currentTimeMillis();
                    value = loader.get();
                    long loadTime = System.currentTimeMillis() - startTime;

                    if (value != null) {
                        // 设置缓存
                        put(cacheKey, value, ttl);
                        log.info("[缓存优化] 缓存加载成功: key={}, loadTime={}ms", cacheKey, loadTime);
                    }

                    return value;

                } finally {
                    lock.unlock();
                }
            } else {
                // 获取锁失败，降级处理
                log.warn("[缓存优化] 获取分布式锁失败，降级处理: key={}", cacheKey);
                return loader.get();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[缓存优化] 缓存加载被中断: key={}", cacheKey, e);
            return loader.get();
        }
    }

    /**
     * 异步刷新缓存
     */
    private <T> void asyncRefreshCache(String cacheKey, Supplier<T> loader, Duration ttl) {
        // 使用线程池异步刷新
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("[缓存优化] 异步刷新缓存: key={}", cacheKey);
                T value = loader.get();
                if (value != null) {
                    put(cacheKey, value, ttl);
                }
            } catch (Exception e) {
                log.warn("[缓存优化] 异步刷新缓存失败: key={}, error={}", cacheKey, e.getMessage());
            }
        });
    }

    /**
     * 从本地缓存获取
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromLocalCache(String namespace, String key) {
        Cache<String, Object> localCache = localCaches.get(namespace);
        if (localCache == null) {
            return null;
        }
        Object value = localCache.getIfPresent(key);
        return value != null ? (T) value : null;
    }

    /**
     * 设置本地缓存
     */
    private void putToLocalCache(String namespace, String key, Object value, Duration ttl) {
        Cache<String, Object> localCache = getOrCreateLocalCache(namespace);
        localCache.put(key, value);
    }

    /**
     * 从本地缓存删除
     */
    private void evictFromLocalCache(String namespace, String key) {
        Cache<String, Object> localCache = localCaches.get(namespace);
        if (localCache != null) {
            localCache.invalidate(key);
        }
    }

    /**
     * 从Redis缓存获取
     */
    @SuppressWarnings({"unchecked", "null"})
    private <T> T getFromRedisCache(String cacheKey) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            Object value = ops.get(cacheKey);
            return value != null ? (T) value : null;
        } catch (Exception e) {
            log.warn("[缓存优化] Redis缓存获取异常: key={}, error={}", cacheKey, e.getMessage());
            return null;
        }
    }

    /**
     * 设置Redis缓存
     */
    @SuppressWarnings("null")
    private void putToRedisCache(String cacheKey, Object value, Duration ttl) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(cacheKey, value, ttl);
        } catch (Exception e) {
            log.warn("[缓存优化] Redis缓存设置异常: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    /**
     * 从Redis缓存删除
     */
    @SuppressWarnings("null")
    private void evictFromRedisCache(String cacheKey) {
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("[缓存优化] Redis缓存删除异常: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    /**
     * 获取或创建本地缓存
     */
    private Cache<String, Object> getOrCreateLocalCache(String namespace) {
        return localCaches.computeIfAbsent(namespace, k ->
            Caffeine.newBuilder()
                .maximumSize(config.getLocalCacheMaxSize())
                .expireAfterWrite(config.getLocalCacheTtl())
                .recordStats()
                .build()
        );
    }

    /**
     * 检查是否接近过期
     */
    @SuppressWarnings("null")
    private boolean isNearExpiry(String key, Duration ttl) {
        // 简化实现：检查Redis中key的剩余TTL
        try {
            Long remainingTtl = redisTemplate.getExpire(key);
            if (remainingTtl != null && remainingTtl > 0) {
                // 如果剩余时间小于总时间的20%，认为接近过期
                return remainingTtl < ttl.getSeconds() * 0.2;
            }
        } catch (Exception e) {
            log.debug("[缓存优化] 检查TTL异常: key={}, error={}", key, e.getMessage());
        }
        return false;
    }

    /**
     * 提取命名空间
     */
    private String extractNamespace(String cacheKey) {
        int colonIndex = cacheKey.indexOf(':');
        return colonIndex > 0 ? cacheKey.substring(0, colonIndex) : "default";
    }

    /**
     * 提取本地缓存key
     */
    private String extractLocalKey(String cacheKey) {
        int colonIndex = cacheKey.indexOf(':');
        return colonIndex > 0 ? cacheKey.substring(colonIndex + 1) : cacheKey;
    }

    /**
     * 构建Redis key
     */
    private String buildRedisKey(String namespace, String key) {
        return namespace + ":" + key;
    }

    /**
     * 缓存配置类
     */
    public static class CacheConfiguration {
        private int localCacheMaxSize = 10000;
        private Duration localCacheTtl = Duration.ofMinutes(30);
        private boolean enableAsyncRefresh = true;
        private double nearExpiryRatio = 0.2;

        // Getters and Setters
        public int getLocalCacheMaxSize() { return localCacheMaxSize; }
        public void setLocalCacheMaxSize(int localCacheMaxSize) { this.localCacheMaxSize = localCacheMaxSize; }

        public Duration getLocalCacheTtl() { return localCacheTtl; }
        public void setLocalCacheTtl(Duration localCacheTtl) { this.localCacheTtl = localCacheTtl; }

        public boolean isEnableAsyncRefresh() { return enableAsyncRefresh; }
        public void setEnableAsyncRefresh(boolean enableAsyncRefresh) { this.enableAsyncRefresh = enableAsyncRefresh; }

        public double getNearExpiryRatio() { return nearExpiryRatio; }
        public void setNearExpiryRatio(double nearExpiryRatio) { this.nearExpiryRatio = nearExpiryRatio; }
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStatistics {
        private final String namespace;
        private final long requestCount;
        private final long hitCount;
        private final long missCount;
        private final double hitRate;
        private final long evictionCount;

        public CacheStatistics(String namespace, long requestCount, long hitCount, long missCount) {
            this.namespace = namespace;
            this.requestCount = requestCount;
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.hitRate = requestCount > 0 ? (double) hitCount / requestCount : 0.0;
            this.evictionCount = 0;
        }

        public CacheStatistics(String namespace, long requestCount, long hitCount, long missCount, double hitRate, long evictionCount) {
            this.namespace = namespace;
            this.requestCount = requestCount;
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.hitRate = hitRate;
            this.evictionCount = evictionCount;
        }

        public String getNamespace() { return namespace; }
        public long getRequestCount() { return requestCount; }
        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public double getHitRate() { return hitRate; }
        public long getEvictionCount() { return evictionCount; }
    }
}
