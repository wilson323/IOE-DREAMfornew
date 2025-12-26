package net.lab1024.sa.attendance.realtime.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 实时计算引擎缓存管理服务
 * <p>
 * 负责引擎的缓存数据存储、获取、过期清理等功能
 * </p>
 * <p>
 * 职责范围：
 * <ul>
 *   <li>缓存数据存储（支持过期时间）</li>
 *   <li>缓存数据获取（自动检查过期）</li>
 *   <li>缓存过期清理（定时任务）</li>
 *   <li>缓存统计和监控</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class RealtimeCacheManager {

    /**
     * 实时数据缓存
     * <p>
     * 使用ConcurrentHashMap保证线程安全
     * </p>
     */
    private final Map<String, Object> realtimeCache = new ConcurrentHashMap<>();

    /**
     * 缓存条目映射（用于过期管理）
     */
    private final Map<String, CacheEntry> cacheEntries = new ConcurrentHashMap<>();

    /**
     * 缓存统计
     */
    private final Map<String, Long> cacheStatistics = new ConcurrentHashMap<>();

    /**
     * 默认缓存过期时间（24小时，毫秒）
     */
    private static final long DEFAULT_TTL = 24 * 60 * 60 * 1000;

    /**
     * 存储缓存数据
     * <p>
     * P0级核心功能：将数据存入缓存，支持设置过期时间
     * </p>
     *
     * @param cacheKey 缓存键
     * @param data 缓存数据
     */
    public void putCache(String cacheKey, Object data) {
        putCache(cacheKey, data, DEFAULT_TTL);
    }

    /**
     * 存储缓存数据（带过期时间）
     * <p>
     * P0级核心功能：将数据存入缓存，并设置过期时间
     * </p>
     *
     * @param cacheKey 缓存键
     * @param data 缓存数据
     * @param ttlMillis 过期时间（毫秒）
     */
    public void putCache(String cacheKey, Object data, long ttlMillis) {
        if (cacheKey == null || data == null) {
            log.warn("[缓存管理] 存储缓存失败，cacheKey或data为null");
            return;
        }

        long expireTime = System.currentTimeMillis() + ttlMillis;
        CacheEntry cacheEntry = new CacheEntry(data, expireTime);

        realtimeCache.put(cacheKey, cacheEntry);
        cacheEntries.put(cacheKey, cacheEntry);

        // 更新统计
        cacheStatistics.put("cache.totalCount", (long) realtimeCache.size());

        log.trace("[缓存管理] 存储缓存成功: cacheKey={}, ttl={}ms", cacheKey, ttlMillis);
    }

    /**
     * 获取缓存数据（自动检查过期）
     * <p>
     * P0级核心功能：从缓存获取数据，如果已过期自动删除并返回null
     * </p>
     *
     * @param cacheKey 缓存键
     * @return 缓存数据，如果不存在或已过期返回null
     */
    public Object getCache(String cacheKey) {
        if (cacheKey == null) {
            return null;
        }

        Object cachedObject = realtimeCache.get(cacheKey);

        if (cachedObject == null) {
            // 更新未命中统计
            cacheStatistics.put("cache.missCount", cacheStatistics.getOrDefault("cache.missCount", 0L) + 1);
            return null;
        }

        // 如果是CacheEntry类型，检查过期
        if (cachedObject instanceof CacheEntry) {
            CacheEntry cacheEntry = (CacheEntry) cachedObject;
            if (cacheEntry.isExpired()) {
                // 缓存已过期，删除并返回null
                removeCache(cacheKey);
                log.trace("[缓存管理] 缓存已过期，已删除: cacheKey={}", cacheKey);

                // 更新未命中统计
                cacheStatistics.put("cache.missCount", cacheStatistics.getOrDefault("cache.missCount", 0L) + 1);
                return null;
            }

            // 更新命中统计
            cacheStatistics.put("cache.hitCount", cacheStatistics.getOrDefault("cache.hitCount", 0L) + 1);
            return cacheEntry.getData();
        }

        // 兼容旧代码：如果不是CacheEntry类型，直接返回
        cacheStatistics.put("cache.hitCount", cacheStatistics.getOrDefault("cache.hitCount", 0L) + 1);
        return cachedObject;
    }

    /**
     * 删除缓存数据
     * <p>
     * P1级功能：删除指定的缓存条目
     * </p>
     *
     * @param cacheKey 缓存键
     */
    public void removeCache(String cacheKey) {
        if (cacheKey == null) {
            return;
        }

        realtimeCache.remove(cacheKey);
        cacheEntries.remove(cacheKey);

        // 更新统计
        cacheStatistics.put("cache.totalCount", (long) realtimeCache.size());

        log.trace("[缓存管理] 删除缓存成功: cacheKey={}", cacheKey);
    }

    /**
     * 清空所有缓存
     * <p>
     * P1级功能：清空所有缓存数据
     * </p>
     */
    public void clearAllCache() {
        int previousSize = realtimeCache.size();
        realtimeCache.clear();
        cacheEntries.clear();

        // 重置统计
        cacheStatistics.clear();

        log.info("[缓存管理] 清空所有缓存成功: previousSize={}", previousSize);
    }

    /**
     * 获取缓存大小
     * <p>
     * P1级功能：获取当前缓存的条目数
     * </p>
     *
     * @return 缓存大小
     */
    public int getCacheSize() {
        return realtimeCache.size();
    }

    /**
     * 获取缓存统计信息
     * <p>
     * P1级功能：获取缓存的命中率、大小等统计信息
     * </p>
     *
     * @return 缓存统计映射
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        long hitCount = cacheStatistics.getOrDefault("cache.hitCount", 0L);
        long missCount = cacheStatistics.getOrDefault("cache.missCount", 0L);
        long totalCount = hitCount + missCount;
        double hitRate = totalCount > 0 ? (double) hitCount / totalCount : 0.0;

        statistics.put("cache.totalCount", realtimeCache.size());
        statistics.put("cache.hitCount", hitCount);
        statistics.put("cache.missCount", missCount);
        statistics.put("cache.hitRate", hitRate);
        statistics.put("cache.entryCount", cacheEntries.size());

        return statistics;
    }

    /**
     * 清理过期缓存（定时任务）
     * <p>
     * P1级功能：建议每小时执行一次，清理所有过期的缓存条目
     * </p>
     */
    public void cleanExpiredCache() {
        int cleanedCount = 0;
        long currentTime = System.currentTimeMillis();

        for (String key : realtimeCache.keySet()) {
            Object value = realtimeCache.get(key);
            if (value instanceof CacheEntry) {
                CacheEntry entry = (CacheEntry) value;
                if (entry.isExpired()) {
                    realtimeCache.remove(key);
                    cacheEntries.remove(key);
                    cleanedCount++;
                }
            }
        }

        // 更新统计
        cacheStatistics.put("cache.totalCount", (long) realtimeCache.size());
        cacheStatistics.put("cache.lastCleanupTime", currentTime);

        if (cleanedCount > 0) {
            log.info("[缓存管理] 清理过期缓存: cleanedCount={}, remainingCacheSize={}",
                    cleanedCount, realtimeCache.size());
        } else {
            log.trace("[缓存管理] 清理过期缓存: 无过期缓存");
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param cacheKey 缓存键
     * @return true-存在，false-不存在
     */
    public boolean containsCache(String cacheKey) {
        if (cacheKey == null) {
            return false;
        }
        return realtimeCache.containsKey(cacheKey);
    }

    /**
     * 获取所有缓存键
     *
     * @return 缓存键集合
     */
    public java.util.Set<String> getCacheKeys() {
        return new java.util.HashSet<>(realtimeCache.keySet());
    }

    /**
     * 缓存条目
     * <p>
     * 内部类，用于封装缓存数据和过期时间
     * </p>
     */
    public static class CacheEntry {
        private final Object data;
        private final long expireTime;

        /**
         * 构造函数
         *
         * @param data 缓存数据
         * @param expireTime 过期时间（毫秒时间戳）
         */
        public CacheEntry(Object data, long expireTime) {
            this.data = data;
            this.expireTime = expireTime;
        }

        /**
         * 获取缓存数据
         *
         * @return 缓存数据
         */
        public Object getData() {
            return data;
        }

        /**
         * 获取过期时间
         *
         * @return 过期时间（毫秒时间戳）
         */
        public long getExpireTime() {
            return expireTime;
        }

        /**
         * 检查是否已过期
         *
         * @return true-已过期，false-未过期
         */
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
