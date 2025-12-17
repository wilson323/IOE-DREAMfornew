package net.lab1024.sa.oa.workflow.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 工作流高级缓存管理器 - 三级缓存架构
 * <p>
 * 三级缓存实现：
 * - L1本地缓存 (Caffeine)：毫秒级响应，TTL 5分钟，容量10000
 * - L2 Redis缓存：分布式一致性，TTL 30分钟
 * - L3 应用缓存：进程内持久化，TTL 2小时
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-17
 */
@Slf4j
@Component
public class WorkflowCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * L1缓存：Caffeine本地缓存（三级缓存架构）
     */
    private Cache<String, CacheEntry> l1Cache;

    // L3缓存：应用级缓存（进程内持久化）
    private final Map<String, CacheEntry> applicationCache = new ConcurrentHashMap<>();

    // 缓存配置
    private static final int MAX_LOCAL_CACHE_SIZE = 10000;
    private static final Duration DEFAULT_LOCAL_TTL = Duration.ofMinutes(5);
    private static final Duration DEFAULT_REDIS_TTL = Duration.ofMinutes(30);
    private static final Duration DEFAULT_APP_TTL = Duration.ofHours(2);

    @PostConstruct
    public void init() {
        l1Cache = Caffeine.newBuilder()
                .maximumSize(MAX_LOCAL_CACHE_SIZE)
                .expireAfterWrite(DEFAULT_LOCAL_TTL)
                .recordStats()
                .build();
        log.info("[工作流缓存] L1 Caffeine缓存初始化完成, 容量={}, TTL={}分钟", MAX_LOCAL_CACHE_SIZE, DEFAULT_LOCAL_TTL.toMinutes());
    }

    // 缓存统计
    private final CacheStatistics statistics = new CacheStatistics();

    /**
     * 获取缓存数据（多级缓存）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        return get(key, type, loader, CachePolicy.DEFAULT);
    }

    /**
     * 获取缓存数据（指定缓存策略）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, Supplier<T> loader, CachePolicy policy) {
        long startTime = System.nanoTime();
        String cacheKey = buildKey(key, type);

        try {
            // L1缓存查询
            if (policy.enableLocalCache) {
                T localValue = getFromLocalCache(cacheKey, type);
                if (localValue != null) {
                    statistics.recordHit(CacheLevel.L1, System.nanoTime() - startTime);
                    log.debug("[缓存] L1缓存命中: key={}", cacheKey);
                    return localValue;
                }
            }

            // L3缓存查询
            if (policy.enableAppCache) {
                T appValue = getFromApplicationCache(cacheKey, type);
                if (appValue != null) {
                    // 回填L1缓存
                    if (policy.enableLocalCache) {
                        putToLocalCache(cacheKey, appValue, policy.localTtl);
                    }
                    statistics.recordHit(CacheLevel.L3, System.nanoTime() - startTime);
                    log.debug("[缓存] L3缓存命中: key={}", cacheKey);
                    return appValue;
                }
            }

            // L2缓存查询（Redis）
            if (policy.enableRedisCache) {
                T redisValue = getFromRedisCache(cacheKey, type);
                if (redisValue != null) {
                    // 回填L1和L3缓存
                    if (policy.enableLocalCache) {
                        putToLocalCache(cacheKey, redisValue, policy.localTtl);
                    }
                    if (policy.enableAppCache) {
                        putToApplicationCache(cacheKey, redisValue, policy.appTtl);
                    }
                    statistics.recordHit(CacheLevel.L2, System.nanoTime() - startTime);
                    log.debug("[缓存] L2缓存命中: key={}", cacheKey);
                    return redisValue;
                }
            }

            // 缓存未命中，从数据源加载
            statistics.recordMiss();
            log.debug("[缓存] 缓存未命中，从数据源加载: key={}", cacheKey);

            T loadedValue = loader.get();
            if (loadedValue != null) {
                put(cacheKey, loadedValue, policy);
            }

            statistics.recordLoad(System.nanoTime() - startTime);
            return loadedValue;

        } catch (Exception e) {
            log.error("[缓存] 缓存操作异常: key={}", cacheKey, e);
            statistics.recordError();
            return loader.get(); // 降级到直接加载
        }
    }

    /**
     * 存储缓存数据
     */
    public <T> void put(String key, T value) {
        put(key, value, CachePolicy.DEFAULT);
    }

    /**
     * 存储缓存数据（指定缓存策略）
     */
    public <T> void put(String key, T value, CachePolicy policy) {
        String cacheKey = buildKey(key, value.getClass());

        try {
            // L1缓存存储
            if (policy.enableLocalCache) {
                putToLocalCache(cacheKey, value, policy.localTtl);
            }

            // L2缓存存储
            if (policy.enableRedisCache) {
                putToRedisCache(cacheKey, value, policy.redisTtl);
            }

            // L3缓存存储
            if (policy.enableAppCache) {
                putToApplicationCache(cacheKey, value, policy.appTtl);
            }

            statistics.recordPut();
            log.debug("[缓存] 数据已缓存: key={}", cacheKey);

        } catch (Exception e) {
            log.error("[缓存] 缓存存储异常: key={}", cacheKey, e);
            statistics.recordError();
        }
    }

    /**
     * 删除缓存数据
     */
    public void evict(String key) {
        evict(key, CacheLevel.ALL);
    }

    /**
     * 删除指定级别的缓存数据
     */
    public void evict(String key, CacheLevel level) {
        try {
            String cacheKey = buildKey(key, Object.class);

            if (level == CacheLevel.ALL || level == CacheLevel.L1) {
                l1Cache.invalidate(cacheKey);
            }

            if (level == CacheLevel.ALL || level == CacheLevel.L2) {
                redisTemplate.delete(cacheKey);
            }

            if (level == CacheLevel.ALL || level == CacheLevel.L3) {
                applicationCache.remove(cacheKey);
            }

            statistics.recordEvict();
            log.debug("[缓存] 缓存已删除: key={}, level={}", cacheKey, level);

        } catch (Exception e) {
            log.error("[缓存] 缓存删除异常: key={}", key, e);
            statistics.recordError();
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        clear(CacheLevel.ALL);
    }

    /**
     * 清空指定级别的缓存
     */
    public void clear(CacheLevel level) {
        try {
            if (level == CacheLevel.ALL || level == CacheLevel.L1) {
                l1Cache.invalidateAll();
                log.info("[缓存] L1 Caffeine缓存已清空");
            }

            if (level == CacheLevel.ALL || level == CacheLevel.L2) {
                // 清空Redis中的工作流相关缓存
                Set<String> keys = redisTemplate.keys("workflow:*");
                if (!keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.info("[缓存] L2缓存已清空，删除了{}个key", keys.size());
                }
            }

            if (level == CacheLevel.ALL || level == CacheLevel.L3) {
                applicationCache.clear();
                log.info("[缓存] L3缓存已清空");
            }

            statistics.recordClear();
            log.info("[缓存] 缓存已清空: level={}", level);

        } catch (Exception e) {
            log.error("[缓存] 缓存清空异常: level={}", level, e);
            statistics.recordError();
        }
    }

    /**
     * 预热缓存
     */
    public void warmUp(List<CacheWarmUpTask> tasks) {
        log.info("[缓存] 开始缓存预热，任务数: {}", tasks.size());

        tasks.parallelStream().forEach(task -> {
            try {
                Object value = task.getLoader().get();
                if (value != null) {
                    put(task.getKey(), value, task.getPolicy());
                    log.debug("[缓存] 预热完成: key={}", task.getKey());
                }
            } catch (Exception e) {
                log.error("[缓存] 预热失败: key={}", task.getKey(), e);
            }
        });

        log.info("[缓存] 缓存预热完成");
    }

    /**
     * 批量获取缓存
     */
    public <T> Map<String, T> mget(List<String> keys, Class<T> type) {
        return mget(keys, type, null);
    }

    /**
     * 批量获取缓存（支持加载器）
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> mget(List<String> keys, Class<T> type, Supplier<Map<String, T>> loader) {
        Map<String, T> result = new HashMap<>();
        List<String> missedKeys = new ArrayList<>();

        for (String key : keys) {
            T value = get(key, type);
            if (value != null) {
                result.put(key, value);
            } else {
                missedKeys.add(key);
            }
        }

        // 批量加载未命中的数据
        if (!missedKeys.isEmpty() && loader != null) {
            try {
                Map<String, T> loadedData = loader.get();
                result.putAll(loadedData);

                // 缓存新加载的数据
                loadedData.forEach((k, v) -> put(k, v));
                log.debug("[缓存] 批量加载完成: keyCount={}", loadedData.size());

            } catch (Exception e) {
                log.error("[缓存] 批量加载异常: keyCount={}", missedKeys.size(), e);
            }
        }

        return result;
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStatistics getStatistics() {
        return statistics.copy();
    }

    // ==================== 私有辅助方法 ====================

    private String buildKey(String key, Class<?> type) {
        return "workflow:" + type.getSimpleName().toLowerCase() + ":" + key;
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromLocalCache(String key, Class<T> type) {
        CacheEntry entry = l1Cache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            return (T) entry.getValue();
        }
        if (entry != null && entry.isExpired()) {
            l1Cache.invalidate(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromApplicationCache(String key, Class<T> type) {
        CacheEntry entry = applicationCache.get(key);
        if (entry != null && !entry.isExpired()) {
            return (T) entry.getValue();
        }
        if (entry != null && entry.isExpired()) {
            applicationCache.remove(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromRedisCache(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? (T) value : null;
        } catch (Exception e) {
            log.debug("[缓存] Redis缓存查询失败: key={}", key, e);
            return null;
        }
    }

    private <T> void putToLocalCache(String key, T value, Duration ttl) {
        CacheEntry entry = new CacheEntry(value, ttl);
        l1Cache.put(key, entry);
    }

    private <T> void putToApplicationCache(String key, T value, Duration ttl) {
        CacheEntry entry = new CacheEntry(value, ttl);
        applicationCache.put(key, entry);
    }

    private <T> void putToRedisCache(String key, T value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("[缓存] Redis缓存存储失败: key={}", key, e);
        }
    }

    /**
     * @deprecated Caffeine自动管理淘汰策略，无需手动实现
     */
    @Deprecated
    private void evictOldestLocalEntry() {
        // Caffeine自动管理LRU淘汰，此方法保留兼容性
        log.debug("[缓存] Caffeine自动管理淘汰策略");
    }

    // ==================== 内部类定义 ====================

    /**
     * 缓存策略
     */
    public static class CachePolicy {
        public static final CachePolicy DEFAULT = new CachePolicy(true, true, true,
                DEFAULT_LOCAL_TTL, DEFAULT_REDIS_TTL, DEFAULT_APP_TTL);

        public final boolean enableLocalCache;
        public final boolean enableRedisCache;
        public final boolean enableAppCache;
        public final Duration localTtl;
        public final Duration redisTtl;
        public final Duration appTtl;

        public CachePolicy(boolean enableLocalCache, boolean enableRedisCache, boolean enableAppCache,
                          Duration localTtl, Duration redisTtl, Duration appTtl) {
            this.enableLocalCache = enableLocalCache;
            this.enableRedisCache = enableRedisCache;
            this.enableAppCache = enableAppCache;
            this.localTtl = localTtl;
            this.redisTtl = redisTtl;
            this.appTtl = appTtl;
        }

        public static CachePolicy localOnly() {
            return new CachePolicy(true, false, false, DEFAULT_LOCAL_TTL, null, null);
        }

        public static CachePolicy redisOnly() {
            return new CachePolicy(false, true, false, null, DEFAULT_REDIS_TTL, null);
        }

        public static CachePolicy appOnly() {
            return new CachePolicy(false, false, true, null, null, DEFAULT_APP_TTL);
        }
    }

    /**
     * 缓存级别
     */
    public enum CacheLevel {
        L1,    // 本地缓存
        L2,    // Redis缓存
        L3,    // 应用缓存
        ALL    // 所有级别
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        private final Object value;
        private final long creationTime;
        private final long expirationTime;

        public CacheEntry(Object value, Duration ttl) {
            this.value = value;
            this.creationTime = System.currentTimeMillis();
            this.expirationTime = ttl != null ? creationTime + ttl.toMillis() : Long.MAX_VALUE;
        }

        public Object getValue() {
            return value;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    /**
     * 缓存预热任务
     */
    public static class CacheWarmUpTask {
        private final String key;
        private final Supplier<Object> loader;
        private final CachePolicy policy;

        public CacheWarmUpTask(String key, Supplier<Object> loader) {
            this(key, loader, CachePolicy.DEFAULT);
        }

        public CacheWarmUpTask(String key, Supplier<Object> loader, CachePolicy policy) {
            this.key = key;
            this.loader = loader;
            this.policy = policy;
        }

        public String getKey() {
            return key;
        }

        public Supplier<Object> getLoader() {
            return loader;
        }

        public CachePolicy getPolicy() {
            return policy;
        }
    }

    /**
     * 缓存统计
     */
    public static class CacheStatistics {
        private final AtomicLong hitCount = new AtomicLong(0);
        private final AtomicLong missCount = new AtomicLong(0);
        private final AtomicLong putCount = new AtomicLong(0);
        private final AtomicLong evictCount = new AtomicLong(0);
        private final AtomicLong clearCount = new AtomicLong(0);
        private final AtomicLong loadCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);

        private final Map<CacheLevel, AtomicLong> levelHitCount = new EnumMap<>(CacheLevel.class);
        private final Map<CacheLevel, AtomicLong> totalResponseTime = new EnumMap<>(CacheLevel.class);

        public CacheStatistics() {
            levelHitCount.put(CacheLevel.L1, new AtomicLong(0));
            levelHitCount.put(CacheLevel.L2, new AtomicLong(0));
            levelHitCount.put(CacheLevel.L3, new AtomicLong(0));
            totalResponseTime.put(CacheLevel.L1, new AtomicLong(0));
            totalResponseTime.put(CacheLevel.L2, new AtomicLong(0));
            totalResponseTime.put(CacheLevel.L3, new AtomicLong(0));
        }

        public void recordHit(CacheLevel level, long responseTimeNanos) {
            hitCount.incrementAndGet();
            levelHitCount.get(level).incrementAndGet();
            totalResponseTime.get(level).addAndGet(responseTimeNanos);
        }

        public void recordMiss() {
            missCount.incrementAndGet();
        }

        public void recordPut() {
            putCount.incrementAndGet();
        }

        public void recordEvict() {
            evictCount.incrementAndGet();
        }

        public void recordClear() {
            clearCount.incrementAndGet();
        }

        public void recordLoad(long responseTimeNanos) {
            loadCount.incrementAndGet();
        }

        public void recordError() {
            errorCount.incrementAndGet();
        }

        public double getHitRate() {
            long total = hitCount.get() + missCount.get();
            return total > 0 ? (double) hitCount.get() / total : 0.0;
        }

        public Map<String, Object> getMetrics() {
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("hitCount", hitCount.get());
            metrics.put("missCount", missCount.get());
            metrics.put("putCount", putCount.get());
            metrics.put("evictCount", evictCount.get());
            metrics.put("clearCount", clearCount.get());
            metrics.put("loadCount", loadCount.get());
            metrics.put("errorCount", errorCount.get());
            metrics.put("hitRate", getHitRate());

            // 各级别统计
            Map<String, Object> levelMetrics = new HashMap<>();
            for (CacheLevel level : CacheLevel.values()) {
                if (level != CacheLevel.ALL) {
                    long hits = levelHitCount.get(level).get();
                    long totalTime = totalResponseTime.get(level).get();
                    double avgResponseTime = hits > 0 ? (double) totalTime / hits : 0.0;

                    Map<String, Object> levelData = new HashMap<>();
                    levelData.put("hits", hits);
                    levelData.put("avgResponseTimeNanos", avgResponseTime);
                    levelMetrics.put(level.name(), levelData);
                }
            }
            metrics.put("levelMetrics", levelMetrics);

            return metrics;
        }

        public CacheStatistics copy() {
            CacheStatistics copy = new CacheStatistics();
            copy.hitCount.set(this.hitCount.get());
            copy.missCount.set(this.missCount.get());
            copy.putCount.set(this.putCount.get());
            copy.evictCount.set(this.evictCount.get());
            copy.clearCount.set(this.clearCount.get());
            copy.loadCount.set(this.loadCount.get());
            copy.errorCount.set(this.errorCount.get());
            return copy;
        }
    }
}
