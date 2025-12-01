package net.lab1024.sa.common.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;

/**
 * 基础缓存管理器
 * <p>
 * 实现多级缓存架构:
 * - L1: Caffeine本地缓存,5分钟过期,1万条上限
 * - L2: Redis分布式缓存,30分钟过期,集群模式
 * <p>
 * 缓存一致性保障:
 * - 使用Cache Aside模式:先更新数据库,再删除缓存
 * - 双删策略:第一次删除后延迟500ms再次删除
 * - 异步清理:避免影响主业务性能
 *
 * @author IOE-DREAM Team
 * @version 1.0
 * @since 2025-11-16
 */
@Slf4j
public abstract class BaseCacheManager {

    @Resource
    protected RedisTemplate<String, Object> redisTemplate;

    /**
     * L1本地缓存配置
     * - 最大容量: 10,000条
     * - 过期时间: 5分钟
     * - 统计信息: 启用
     */
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * L2 Redis缓存过期时间(分钟)
     */
    protected static final long REDIS_EXPIRE_MINUTES = 30;

    /**
     * 双删策略延迟时间(毫秒)
     */
    protected static final long DOUBLE_DELETE_DELAY_MS = 500;

    /**
     * 获取缓存键前缀
     * 子类必须实现,用于区分不同业务的缓存
     *
     * @return 缓存键前缀,如"user:", "card:", "device:"
     */
    protected abstract String getCachePrefix();

    /**
     * 构建缓存键
     *
     * @param id     业务ID
     * @param suffix 后缀,如":info", ":list", ":detail"
     * @return 完整的缓存键
     */
    protected String buildCacheKey(Object id, String suffix) {
        return getCachePrefix() + id + suffix;
    }

    /**
     * 获取缓存数据(多级缓存)
     * 查询顺序: L1本地缓存 → L2 Redis缓存 → 数据库
     *
     * @param cacheKey 缓存键
     * @param dbLoader 数据库加载函数
     * @param <T>      数据类型
     * @return 数据对象,可能为null
     */
    protected <T> T getCache(String cacheKey, DataLoader<T> dbLoader) {
        // 1. 先查L1本地缓存
        @SuppressWarnings("unchecked")
        T data = (T) localCache.getIfPresent(cacheKey);
        if (data != null) {
            log.debug("L1缓存命中, cacheKey: {}", cacheKey);
            return data;
        }

        // 2. 查L2 Redis缓存
        try {
            @SuppressWarnings("unchecked")
            T redisData = (T) redisTemplate.opsForValue().get(cacheKey);
            if (redisData != null) {
                // 回写L1缓存
                localCache.put(cacheKey, redisData);
                log.debug("L2缓存命中, cacheKey: {}", cacheKey);
                return redisData;
            }
        } catch (Exception e) {
            log.warn("Redis访问异常, cacheKey: {}, error: {}", cacheKey, e.getMessage());
        }

        // 3. 查数据库
        if (dbLoader != null) {
            data = dbLoader.load();
            if (data != null) {
                // 4. 异步写入缓存
                setCacheAsync(cacheKey, data);
            }
        }

        return data;
    }

    /**
     * 设置缓存(同步)
     * 同时设置L1和L2缓存
     *
     * @param cacheKey 缓存键
     * @param data     数据对象
     */
    protected void setCache(String cacheKey, Object data) {
        try {
            localCache.put(cacheKey, data);
            redisTemplate.opsForValue().set(cacheKey, data, REDIS_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("缓存设置成功, cacheKey: {}", cacheKey);
        } catch (Exception e) {
            log.warn("缓存设置失败, cacheKey: {}, error: {}", cacheKey, e.getMessage());
        }
    }

    /**
     * 异步设置缓存
     * 避免影响主业务性能
     *
     * @param cacheKey 缓存键
     * @param data     数据对象
     */
    @Async
    protected void setCacheAsync(String cacheKey, Object data) {
        try {
            localCache.put(cacheKey, data);
            redisTemplate.opsForValue().set(cacheKey, data, REDIS_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("缓存异步设置成功, cacheKey: {}", cacheKey);
        } catch (Exception e) {
            log.warn("缓存异步设置失败, cacheKey: {}, error: {}", cacheKey, e.getMessage());
        }
    }

    /**
     * 清除缓存(双删策略)
     * 第一次删除 → 延迟500ms → 第二次删除
     * 避免数据库主从延迟导致的缓存不一致
     *
     * @param cacheKey 缓存键
     */
    @Async
    public void removeCache(String cacheKey) {
        try {
            // 第一次删除缓存
            localCache.invalidate(cacheKey);
            redisTemplate.delete(cacheKey);
            log.debug("缓存第一次删除, cacheKey: {}", cacheKey);

            // 延迟500ms后再次删除(避免双写问题)
            Thread.sleep(DOUBLE_DELETE_DELAY_MS);

            localCache.invalidate(cacheKey);
            redisTemplate.delete(cacheKey);
            log.debug("缓存第二次删除, cacheKey: {}", cacheKey);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("缓存删除被中断, cacheKey: {}", cacheKey, e);
        } catch (Exception e) {
            log.error("清除缓存失败, cacheKey: {}", cacheKey, e);
        }
    }

    /**
     * 批量清除缓存(模式匹配)
     * 用于清除某一类缓存,如 "user:*"
     *
     * @param pattern 缓存键模式
     */
    @Async
    public void removeCacheByPattern(String pattern) {
        try {
            // 清除L1缓存(全部清除,因为Caffeine不支持模式匹配)
            localCache.invalidateAll();
            log.debug("L1缓存全部清除");

            // 清除L2缓存(模式匹配)
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("L2缓存批量清除, pattern: {}, count: {}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("批量清除缓存失败, pattern: {}", pattern, e);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public CacheStats getCacheStats() {
        var stats = localCache.stats();
        return CacheStats.builder()
                .hitCount(stats.hitCount())
                .missCount(stats.missCount())
                .hitRate(stats.hitRate())
                .evictionCount(stats.evictionCount())
                .estimatedSize(localCache.estimatedSize())
                .build();
    }

    /**
     * 数据加载函数接口
     *
     * @param <T> 数据类型
     */
    @FunctionalInterface
    public interface DataLoader<T> {
        /**
         * 从数据库加载数据
         *
         * @return 数据对象
         */
        T load();
    }

    /**
     * 缓存统计信息
     */
    @Data
    @Builder
    public static class CacheStats {
        /**
         * 命中次数
         */
        private long hitCount;

        /**
         * 未命中次数
         */
        private long missCount;

        /**
         * 命中率
         */
        private double hitRate;

        /**
         * 驱逐次数
         */
        private long evictionCount;

        /**
         * 估算大小
         */
        private long estimatedSize;
    }
}