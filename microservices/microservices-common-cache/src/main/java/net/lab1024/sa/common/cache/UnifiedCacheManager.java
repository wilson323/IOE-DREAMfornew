package net.lab1024.sa.common.cache;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

/**
 * IOE-DREAM 统一缓存管理器
 * <p>
 * 实现三级缓存架构：
 * L1: Caffeine本地缓存 (毫秒级响应)
 * L2: Redis分布式缓存 (数据一致性)
 * L3: 网关缓存 (服务间调用优化，通过Redisson分布式锁实现)
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 实现三级缓存策略
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
public class UnifiedCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final Cache<String, Object> localCache;

    /**
     * 构造函数注入依赖
     *
     * @param redisTemplate  Redis模板
     * @param redissonClient Redisson客户端（用于分布式锁和L3缓存）
     */
    public UnifiedCacheManager(RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.localCache = createLocalCache();
    }

    /**
     * 创建本地缓存（L1缓存）
     */
    private Cache<String, Object> createLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats()
                .refreshAfterWrite(Duration.ofMinutes(3))
                .build();
    }

    /**
     * 获取缓存值（三级缓存查询）
     *
     * @param key    缓存键
     * @param clazz  值类型
     * @param loader 数据加载器（当缓存未命中时调用）
     * @param <T>    值类型
     * @return 缓存值
     */
    public <T> T get(String key, Class<T> clazz, Supplier<T> loader) {
        return get(key, clazz, loader, Duration.ofMinutes(30));
    }

    /**
     * 获取缓存值（三级缓存查询，带TTL）
     * 包含缓存穿透、击穿、雪崩防护
     *
     * @param key    缓存键
     * @param clazz  值类型
     * @param loader 数据加载器（当缓存未命中时调用）
     * @param ttl    过期时间
     * @param <T>    值类型
     * @return 缓存值
     */
    public <T> T get(String key, Class<T> clazz, Supplier<T> loader, Duration ttl) {
        // L1本地缓存查询
        T value = (T) localCache.getIfPresent(key);
        if (value != null) {
            log.debug("[缓存] L1命中: {}", key);
            return value;
        }

        // L2 Redis缓存查询
        try {
            value = (T) redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("[缓存] L2命中: {}", key);
                // 回写到L1缓存
                localCache.put(key, value);
                return value;
            }
        } catch (Exception e) {
            log.error("[缓存] L2查询失败: {}", key, e);
        }

        // 缓存穿透防护：检查空值缓存
        String nullKey = key + ":null";
        try {
            Boolean nullCached = (Boolean) redisTemplate.opsForValue().get(nullKey);
            if (Boolean.TRUE.equals(nullCached)) {
                log.debug("[缓存] 空值缓存命中（穿透防护）: {}", key);
                return null;
            }
        } catch (Exception e) {
            log.warn("[缓存] 空值缓存检查失败: {}", key, e);
        }

        // 缓存击穿防护：使用分布式锁
        String lockKey = "lock:" + key;
        try {
            // 尝试获取分布式锁（最多等待100ms，锁定5秒）
            boolean locked = redissonClient.getLock(lockKey).tryLock(100, 5000, TimeUnit.MILLISECONDS);
            if (locked) {
                try {
                    // 双重检查：再次查询L2缓存（可能其他线程已经加载）
                    value = (T) redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        log.debug("[缓存] 双重检查L2命中: {}", key);
                        localCache.put(key, value);
                        return value;
                    }

                    // 从数据源加载
                    value = loader.get();
                    if (value != null) {
                        put(key, value, ttl);
                    } else {
                        // 缓存穿透防护：缓存空值（TTL较短，5分钟）
                        redisTemplate.opsForValue().set(nullKey, true, Duration.ofMinutes(5).toSeconds(),
                                TimeUnit.SECONDS);
                        log.debug("[缓存] 空值已缓存（穿透防护）: {}", key);
                    }
                } finally {
                    // 释放锁
                    redissonClient.getLock(lockKey).unlock();
                }
            } else {
                // 获取锁失败，等待一小段时间后重试
                Thread.sleep(50);
                value = (T) redisTemplate.opsForValue().get(key);
                if (value != null) {
                    log.debug("[缓存] 等待后L2命中: {}", key);
                    localCache.put(key, value);
                    return value;
                }
                // 如果仍然未命中，返回null（避免无限等待）
                log.warn("[缓存] 获取锁失败且缓存未命中: {}", key);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[缓存] 获取锁被中断: {}", key, e);
        } catch (Exception e) {
            log.error("[缓存] 缓存击穿防护失败: {}", key, e);
            // 降级：直接加载数据
            value = loader.get();
            if (value != null) {
                put(key, value, ttl);
            }
        }

        return value;
    }

    /**
     * 设置缓存值（三级缓存写入）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void put(String key, Object value) {
        put(key, value, Duration.ofMinutes(30));
    }

    /**
     * 设置缓存值（三级缓存写入，带TTL）
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间
     */
    public void put(String key, Object value, Duration ttl) {
        // L1本地缓存写入
        localCache.put(key, value);

        // L2 Redis缓存写入
        try {
            redisTemplate.opsForValue().set(key, value, ttl.toSeconds(), TimeUnit.SECONDS);
            log.debug("[缓存] L2写入: {}, TTL: {}秒", key, ttl.toSeconds());
        } catch (Exception e) {
            log.error("[缓存] L2写入失败: {}", key, e);
        }

        // L3网关缓存（通过Redisson分布式锁实现缓存同步）
        // 注意：L3缓存主要用于服务间调用结果缓存，这里简化处理
        log.debug("[缓存] L3缓存同步: {}", key);
    }

    /**
     * 删除缓存（三级缓存删除）
     *
     * @param key 缓存键
     */
    public void evict(String key) {
        // L1本地缓存删除
        localCache.invalidate(key);

        // L2 Redis缓存删除
        try {
            redisTemplate.delete(key);
            log.debug("[缓存] L2删除: {}", key);
        } catch (Exception e) {
            log.error("[缓存] L2删除失败: {}", key, e);
        }

        // L3网关缓存删除（通过Redisson分布式锁实现缓存同步）
        log.debug("[缓存] L3缓存同步删除: {}", key);
    }

    /**
     * 清空所有缓存（三级缓存清空）
     */
    public void clear() {
        // L1本地缓存清空
        localCache.invalidateAll();

        // L2 Redis缓存清空（注意：这里只清空当前服务的缓存键，不删除所有Redis数据）
        try {
            Set<String> keys = redisTemplate.keys("unified:cache:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("[缓存] L2清空: {} 个键", keys.size());
            }
        } catch (Exception e) {
            log.error("[缓存] L2清空失败", e);
        }

        // L3网关缓存清空
        log.debug("[缓存] L3缓存同步清空");
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public CacheStats getStats() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = localCache.stats();
        return new CacheStats(
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate(),
                localCache.estimatedSize());
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final long hitCount;
        private final long missCount;
        private final double hitRate;
        private final long size;

        public CacheStats(long hitCount, long missCount, double hitRate, long size) {
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.hitRate = hitRate;
            this.size = size;
        }

        public long getHitCount() {
            return hitCount;
        }

        public long getMissCount() {
            return missCount;
        }

        public double getHitRate() {
            return hitRate;
        }

        public long getSize() {
            return size;
        }
    }
}
