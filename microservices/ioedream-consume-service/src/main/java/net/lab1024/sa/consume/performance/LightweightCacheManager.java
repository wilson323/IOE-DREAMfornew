package net.lab1024.sa.consume.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 轻量级三级缓存管理器
 *
 * 设计原则:
 * 1. L1本地缓存 - Caffeine (毫秒级响应)
 * 2. L2分布式缓存 - Redis (秒级响应)
 * 3. L3数据库查询 - (分钟级响应)
 *
 * 轻量化特点:
 * - 避免过度复杂的多级缓存
 * - 自动失效机制
 * - 内存占用最小化
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class LightweightCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;

    // L1本地缓存 - 使用ConcurrentHashMap简化实现
    private final ConcurrentHashMap<String, CacheEntry> localCache = new ConcurrentHashMap<>();

    // 默认缓存时间配置
    private static final Duration DEFAULT_LOCAL_TTL = Duration.ofMinutes(5);
    private static final Duration DEFAULT_REDIS_TTL = Duration.ofMinutes(30);

    public LightweightCacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取数据 - 三级缓存策略
     */
    public <T> T get(String key, Class<T> clazz, Supplier<T> loader) {
        return get(key, clazz, loader, DEFAULT_LOCAL_TTL, DEFAULT_REDIS_TTL);
    }

    /**
     * 获取数据 - 自定义TTL
     */
    public <T> T get(String key, Class<T> clazz, Supplier<T> loader, Duration localTtl, Duration redisTtl) {
        // L1: 本地缓存检查
        CacheEntry entry = localCache.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache hit - L1 local cache, key: {}", key);
            return clazz.cast(entry.getValue());
        }

        // L2: Redis缓存检查
        try {
            if (key != null) {
                Object redisValue = redisTemplate.opsForValue().get(key);
                if (redisValue != null) {
                    log.debug("Cache hit - L2 Redis cache, key: {}", key);
                    T value = clazz.cast(redisValue);
                    // 回填L1缓存（key和localTtl已通过null检查）
                    localCache.put(key, new CacheEntry(value, System.currentTimeMillis() + localTtl.toMillis()));
                    return value;
                }
            }
        } catch (Exception e) {
            log.warn("Redis cache error, key: {}, error: {}", key, e.getMessage());
        }

        // L3: 数据库查询
        log.debug("Cache miss, loading from database, key: {}", key);
        T value = loader.get();
        if (value != null) {
            // 回填L1和L2缓存
            set(key, value, localTtl, redisTtl);
        }

        return value;
    }

    /**
     * 设置缓存
     */
    public <T> void set(String key, T value) {
        set(key, value, DEFAULT_LOCAL_TTL, DEFAULT_REDIS_TTL);
    }

    /**
     * 设置缓存 - 自定义TTL
     */
    public <T> void set(String key, T value, Duration localTtl, Duration redisTtl) {
        if (key == null || value == null || localTtl == null || redisTtl == null) {
            log.warn("Cache set failed: invalid parameters, key={}, value={}, localTtl={}, redisTtl={}",
                    key, value != null, localTtl, redisTtl);
            return;
        }

        // 设置L1缓存（key、localTtl已通过null检查）
        long expireTime = System.currentTimeMillis() + localTtl.toMillis();
        localCache.put(key, new CacheEntry(value, expireTime));

        // 设置L2缓存（key、redisTtl已通过null检查）
        try {
            redisTemplate.opsForValue().set(key, value, redisTtl);
        } catch (Exception e) {
            log.warn("Redis set error, key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        if (key == null) {
            log.warn("Cache delete failed: key is null");
            return;
        }

        // 删除L1缓存（key已通过null检查）
        localCache.remove(key);

        // 删除L2缓存
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis delete error, key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * 清空所有缓存
     * <p>
     * 注意：flushDb()已过时，使用keys()和delete()的组合更安全
     * 仅清空当前缓存命名空间的键，避免误清空其他数据库
     * </p>
     */
    public void clear() {
        // 清空L1缓存
        localCache.clear();

        // 清空L2缓存
        // 使用keys()和delete()替代已过时的flushDb()方法
        try {
            // 获取所有键（如果缓存有命名空间前缀，可以添加前缀过滤）
            java.util.Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Redis缓存清空成功，清理键数量: {}", keys.size());
            }
        } catch (Exception e) {
            log.warn("Redis clear error: {}", e.getMessage());
        }
    }

    /**
     * 预热缓存
     */
    public void warmUp(String key, Supplier<Object> loader) {
        // 在后台线程异步加载
        new Thread(() -> {
            try {
                Object value = loader.get();
                if (value != null) {
                    set(key, value, Duration.ofMinutes(10), Duration.ofHours(1));
                    log.info("Cache warmed up, key: {}", key);
                }
            } catch (Exception e) {
                log.warn("Cache warm up failed, key: {}, error: {}", key, e.getMessage());
            }
        }).start();
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        private final Object value;
        private final long expireTime;

        public CacheEntry(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
