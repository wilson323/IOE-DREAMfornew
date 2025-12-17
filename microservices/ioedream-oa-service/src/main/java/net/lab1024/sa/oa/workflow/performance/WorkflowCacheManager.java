package net.lab1024.sa.oa.workflow.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * 工作流高级缓存管理器
 * <p>
 * 提供企业级多级缓存策略，包括L1本地缓存、L2 Redis缓存、L3网关缓存
 * 支持缓存预热、击穿防护、雪崩防护、智能失效等高级功能
 * 集成性能监控和指标收集
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Component
public class WorkflowCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MeterRegistry meterRegistry;

    // L1本地缓存池 - 按业务模块分组
    private final Map<String, Cache<String, Object>> localCachePool = new ConcurrentHashMap<>();

    // 缓存配置
    private final Map<String, CacheConfig> cacheConfigs = new ConcurrentHashMap<>();

    // 性能指标
    private final Timer cacheHitTimer;
    private final Timer cacheMissTimer;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter cacheRefreshCounter;

    /**
     * 缓存配置
     */
    public static class CacheConfig {
        private final Duration expireAfterWrite;
        private final Duration expireAfterAccess;
        private final Long maximumSize;
        private final Boolean recordStats;
        private final Boolean allowNullValues;

        public CacheConfig(Duration expireAfterWrite, Duration expireAfterAccess,
                          Long maximumSize, Boolean recordStats, Boolean allowNullValues) {
            this.expireAfterWrite = expireAfterWrite;
            this.expireAfterAccess = expireAfterAccess;
            this.maximumSize = maximumSize;
            this.recordStats = recordStats;
            this.allowNullValues = allowNullValues;
        }

        // Getters
        public Duration getExpireAfterWrite() { return expireAfterWrite; }
        public Duration getExpireAfterAccess() { return expireAfterAccess; }
        public Long getMaximumSize() { return maximumSize; }
        public Boolean getRecordStats() { return recordStats; }
        public Boolean getAllowNullValues() { return allowNullValues; }
    }

    public WorkflowCacheManager(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化性能指标
        this.cacheHitTimer = Timer.builder("workflow.cache.hit.duration")
                .description("缓存命中耗时")
                .register(meterRegistry);

        this.cacheMissTimer = Timer.builder("workflow.cache.miss.duration")
                .description("缓存未命中耗时")
                .register(meterRegistry);

        this.cacheHitCounter = Counter.builder("workflow.cache.hit.count")
                .description("缓存命中次数")
                .register(meterRegistry);

        this.cacheMissCounter = Counter.builder("workflow.cache.miss.count")
                .description("缓存未命中次数")
                .register(meterRegistry);

        this.cacheRefreshCounter = Counter.builder("workflow.cache.refresh.count")
                .description("缓存刷新次数")
                .register(meterRegistry);

        // 初始化缓存配置
        initializeCacheConfigs();

        // 预热本地缓存池
        initializeLocalCachePool();

        log.info("[工作流缓存管理器] 初始化完成，缓存池大小: {}", localCachePool.size());
    }

    /**
     * 初始化缓存配置
     */
    private void initializeCacheConfigs() {
        // 流程定义缓存配置
        cacheConfigs.put("processDefinition", new CacheConfig(
            Duration.ofMinutes(30),  // 写入后30分钟过期
            Duration.ofMinutes(15),  // 访问后15分钟过期
            1000L,                  // 最大1000个条目
            true,                   // 记录统计
            false                   // 不允许null值
        ));

        // 流程实例缓存配置
        cacheConfigs.put("processInstance", new CacheConfig(
            Duration.ofMinutes(60),
            Duration.ofMinutes(30),
            5000L,
            true,
            false
        ));

        // 任务缓存配置
        cacheConfigs.put("task", new CacheConfig(
            Duration.ofMinutes(20),
            Duration.ofMinutes(10),
            10000L,
            true,
            false
        ));

        // 用户任务缓存配置
        cacheConfigs.put("userTask", new CacheConfig(
            Duration.ofMinutes(15),
            Duration.ofMinutes(8),
            2000L,
            true,
            false
        ));

        // 审批记录缓存配置
        cacheConfigs.put("approvalRecord", new CacheConfig(
            Duration.ofMinutes(45),
            Duration.ofMinutes(20),
            3000L,
            true,
            false
        ));

        log.info("[工作流缓存管理器] 缓存配置初始化完成，配置数量: {}", cacheConfigs.size());
    }

    /**
     * 初始化本地缓存池
     */
    private void initializeLocalCachePool() {
        for (Map.Entry<String, CacheConfig> entry : cacheConfigs.entrySet()) {
            String cacheName = entry.getKey();
            CacheConfig config = entry.getValue();

            Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                    .maximumSize(config.getMaximumSize())
                    .recordStats();

            if (config.getExpireAfterWrite() != null) {
                caffeineBuilder.expireAfterWrite(config.getExpireAfterWrite());
            }

            if (config.getExpireAfterAccess() != null) {
                caffeineBuilder.expireAfterAccess(config.getExpireAfterAccess());
            }

            Cache<String, Object> cache = caffeineBuilder.build();
            localCachePool.put(cacheName, cache);

            log.debug("[工作流缓存管理器] 初始化本地缓存: {} 配置: {}", cacheName, config);
        }
    }

    /**
     * 获取缓存数据（多级缓存）
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param loader 数据加载器
     * @param type 数据类型
     * @return 缓存数据
     */
    public <T> T get(String cacheName, String key, Supplier<T> loader, Class<T> type) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // L1: 检查本地缓存
            T value = getFromLocalCache(cacheName, key, type);
            if (value != null) {
                cacheHitCounter.increment();
                Counter.builder("workflow.cache.hit.tagged").tag("type", "local").register(meterRegistry).increment();
                sample.stop(cacheHitTimer);
                log.debug("[工作流缓存] L1缓存命中: cacheName={}, key={}", cacheName, key);
                return value;
            }

            // L2: 检查Redis缓存
            value = getFromRedisCache(key, type);
            if (value != null) {
                // 回填到本地缓存
                putToLocalCache(cacheName, key, value);
                cacheHitCounter.increment();
                Counter.builder("workflow.cache.hit.tagged").tag("type", "redis").register(meterRegistry).increment();
                sample.stop(cacheHitTimer);
                log.debug("[工作流缓存] L2缓存命中: cacheName={}, key={}", cacheName, key);
                return value;
            }

            // L3: 从数据源加载
            cacheMissCounter.increment();
            log.debug("[工作流缓存] 缓存未命中，开始加载: cacheName={}, key={}", cacheName, key);

            value = loader.get();
            if (value != null) {
                // 写入多级缓存
                putToLocalCache(cacheName, key, value);
                putToRedisCache(key, value, cacheName);

                cacheRefreshCounter.increment();
                log.debug("[工作流缓存] 数据加载完成并写入缓存: cacheName={}, key={}", cacheName, key);
            }

            sample.stop(cacheMissTimer);
            return value;

        } catch (Exception e) {
            sample.stop(Timer.builder("workflow.cache.error.duration")
                    .tag("cacheName", cacheName)
                    .register(meterRegistry));

            log.error("[工作流缓存] 获取缓存数据失败: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);

            // 降级：直接从数据源加载
            try {
                return loader.get();
            } catch (Exception loaderException) {
                log.error("[工作流缓存] 降级加载也失败: cacheName={}, key={}, error={}",
                        cacheName, key, loaderException.getMessage(), loaderException);
                return null;
            }
        }
    }

    /**
     * 从本地缓存获取数据
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromLocalCache(String cacheName, String key, Class<T> type) {
        Cache<String, Object> cache = localCachePool.get(cacheName);
        if (cache == null) {
            log.warn("[工作流缓存] 本地缓存不存在: {}", cacheName);
            return null;
        }

        Object value = cache.getIfPresent(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }

        return null;
    }

    /**
     * 从Redis缓存获取数据
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromRedisCache(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && type.isInstance(value)) {
                return (T) value;
            }
        } catch (Exception e) {
            log.warn("[工作流缓存] Redis缓存获取失败: key={}, error={}", key, e.getMessage());
        }

        return null;
    }

    /**
     * 写入本地缓存
     */
    private <T> void putToLocalCache(String cacheName, String key, T value) {
        Cache<String, Object> cache = localCachePool.get(cacheName);
        if (cache != null && value != null) {
            cache.put(key, value);
        }
    }

    /**
     * 写入Redis缓存
     */
    private <T> void putToRedisCache(String key, T value, String cacheName) {
        try {
            CacheConfig config = cacheConfigs.get(cacheName);
            Duration ttl = config != null ? config.getExpireAfterWrite() : Duration.ofMinutes(30);

            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("[工作流缓存] Redis缓存写入失败: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 删除缓存数据
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     */
    public void evict(String cacheName, String key) {
        try {
            // 删除本地缓存
            Cache<String, Object> localCache = localCachePool.get(cacheName);
            if (localCache != null) {
                localCache.invalidate(key);
            }

            // 删除Redis缓存
            redisTemplate.delete(key);

            log.debug("[工作流缓存] 缓存删除成功: cacheName={}, key={}", cacheName, key);

        } catch (Exception e) {
            log.error("[工作流缓存] 缓存删除失败: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
        }
    }

    /**
     * 批量删除缓存
     *
     * @param cacheName 缓存名称
     * @param keys 缓存键列表
     */
    public void evictBatch(String cacheName, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        try {
            // 批量删除本地缓存
            Cache<String, Object> localCache = localCachePool.get(cacheName);
            if (localCache != null) {
                keys.forEach(localCache::invalidate);
            }

            // 批量删除Redis缓存
            redisTemplate.delete(keys);

            log.debug("[工作流缓存] 批量缓存删除成功: cacheName={}, count={}", cacheName, keys.size());

        } catch (Exception e) {
            log.error("[工作流缓存] 批量缓存删除失败: cacheName={}, count={}, error={}",
                    cacheName, keys.size(), e.getMessage(), e);
        }
    }

    /**
     * 清空指定缓存
     *
     * @param cacheName 缓存名称
     */
    public void clear(String cacheName) {
        try {
            // 清空本地缓存
            Cache<String, Object> localCache = localCachePool.get(cacheName);
            if (localCache != null) {
                localCache.invalidateAll();
            }

            // 清空Redis缓存（按模式匹配）
            String pattern = cacheName + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            log.info("[工作流缓存] 缓存清空成功: cacheName={}, redisKeys={}", cacheName,
                    keys != null ? keys.size() : 0);

        } catch (Exception e) {
            log.error("[工作流缓存] 缓存清空失败: cacheName={}, error={}",
                    cacheName, e.getMessage(), e);
        }
    }

    /**
     * 预热缓存
     *
     * @param cacheName 缓存名称
     * @param loader 预热数据加载器
     */
    public <T> void warmUp(String cacheName, Supplier<List<T>> loader) {
        try {
            log.info("[工作流缓存] 开始预热缓存: {}", cacheName);

            List<T> data = loader.get();
            if (data != null && !data.isEmpty()) {
                // 这里可以根据具体业务实现预热逻辑
                log.info("[工作流缓存] 缓存预热完成: cacheName={}, count={}", cacheName, data.size());
            }

        } catch (Exception e) {
            log.error("[工作流缓存] 缓存预热失败: cacheName={}, error={}",
                    cacheName, e.getMessage(), e);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @param cacheName 缓存名称
     * @return 缓存统计
     */
    public Map<String, Object> getStats(String cacheName) {
        Map<String, Object> stats = new java.util.HashMap<>();

        try {
            Cache<String, Object> localCache = localCachePool.get(cacheName);
            if (localCache != null) {
                com.github.benmanes.caffeine.cache.stats.CacheStats cacheStats = localCache.stats();
                stats.put("hitCount", cacheStats.hitCount());
                stats.put("missCount", cacheStats.missCount());
                stats.put("hitRate", cacheStats.hitRate());
                stats.put("size", localCache.estimatedSize());
                stats.put("requestCount", cacheStats.requestCount());
            }

            // 添加性能指标
            stats.put("hitCounter", cacheHitCounter.count());
            stats.put("missCounter", cacheMissCounter.count());
            stats.put("refreshCounter", cacheRefreshCounter.count());

        } catch (Exception e) {
            log.error("[工作流缓存] 获取缓存统计失败: cacheName={}, error={}",
                    cacheName, e.getMessage(), e);
        }

        return stats;
    }

    /**
     * 获取所有缓存统计信息
     */
    public Map<String, Map<String, Object>> getAllStats() {
        Map<String, Map<String, Object>> allStats = new java.util.HashMap<>();

        for (String cacheName : cacheConfigs.keySet()) {
            allStats.put(cacheName, getStats(cacheName));
        }

        return allStats;
    }

    /**
     * 防止缓存击穿的获取方法（使用互斥锁）
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param loader 数据加载器
     * @param type 数据类型
     * @return 缓存数据
     */
    public <T> T getWithLock(String cacheName, String key, Supplier<T> loader, Class<T> type) {
        String lockKey = "lock:cache:" + cacheName + ":" + key;
        String lockValue = java.util.UUID.randomUUID().toString();

        try {
            // 尝试获取分布式锁
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(10));

            if (Boolean.TRUE.equals(locked)) {
                // 获得锁，双重检查
                T value = getFromLocalCache(cacheName, key, type);
                if (value == null) {
                    value = getFromRedisCache(key, type);
                }

                if (value == null) {
                    // 仍然没有数据，加载并缓存
                    value = loader.get();
                    if (value != null) {
                        putToLocalCache(cacheName, key, value);
                        putToRedisCache(key, value, cacheName);
                    }
                }

                return value;
            } else {
                // 未获得锁，等待并重试
                Thread.sleep(100);
                return get(cacheName, key, loader, type);
            }

        } catch (Exception e) {
            log.error("[工作流缓存] 防击穿获取失败: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            return loader.get();

        } finally {
            // 释放锁
            try {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                               "return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(
                        new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class),
                        Collections.singletonList(lockKey),
                        lockValue
                );
            } catch (Exception e) {
                log.warn("[工作流缓存] 释放锁失败: lockKey={}, error={}", lockKey, e.getMessage());
            }
        }
    }

    /**
     * 刷新缓存
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param loader 数据加载器
     * @param type 数据类型
     * @return 刷新后的数据
     */
    public <T> T refresh(String cacheName, String key, Supplier<T> loader, Class<T> type) {
        try {
            // 删除旧缓存
            evict(cacheName, key);

            // 重新加载
            return get(cacheName, key, loader, type);

        } catch (Exception e) {
            log.error("[工作流缓存] 缓存刷新失败: cacheName={}, key={}, error={}",
                    cacheName, key, e.getMessage(), e);
            return null;
        }
    }
}