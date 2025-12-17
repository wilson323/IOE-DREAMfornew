package net.lab1024.sa.common.permission.manager;

import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 权限缓存管理器
 * <p>
 * 企业级多级权限缓存管理，提供：
 * - L1本地缓存（Caffeine）
 * - L2分布式缓存（Redis）
 * - 智能缓存策略
 * - 缓存预热和失效
 * - 性能监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface PermissionCacheManager {

    /**
     * 获取权限验证结果
     *
     * @param cacheKey 缓存键
     * @return 验证结果，null表示缓存未命中
     */
    PermissionValidationResult getValidationResult(String cacheKey);

    /**
     * 缓存权限验证结果
     *
     * @param cacheKey 缓存键
     * @param result 验证结果
     * @param ttlSeconds 缓存时间（秒）
     */
    void cacheValidationResult(String cacheKey, PermissionValidationResult result, int ttlSeconds);

    /**
     * 清除权限验证结果缓存
     *
     * @param cacheKey 缓存键
     */
    void evictValidationResult(String cacheKey);

    /**
     * 批量清除权限验证结果缓存
     *
     * @param cacheKeys 缓存键集合
     */
    void evictValidationResults(Set<String> cacheKeys);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限列表，null表示缓存未命中
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 缓存用户权限列表
     *
     * @param userId 用户ID
     * @param permissions 权限列表
     * @param ttlSeconds 缓存时间（秒）
     */
    void cacheUserPermissions(Long userId, Set<String> permissions, int ttlSeconds);

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     */
    void evictUserPermissions(Long userId);

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色列表，null表示缓存未命中
     */
    Set<String> getUserRoles(Long userId);

    /**
     * 缓存用户角色列表
     *
     * @param userId 用户ID
     * @param roles 角色列表
     * @param ttlSeconds 缓存时间（秒）
     */
    void cacheUserRoles(Long userId, Set<String> roles, int ttlSeconds);

    /**
     * 清除用户角色缓存
     *
     * @param userId 用户ID
     */
    void evictUserRoles(Long userId);

    /**
     * 获取权限映射关系
     *
     * @param permissionId 权限ID
     * @return 权限信息，null表示缓存未命中
     */
    Object getPermissionMapping(Long permissionId);

    /**
     * 缓存权限映射关系
     *
     * @param permissionId 权限ID
     * @param permission 权限信息
     * @param ttlSeconds 缓存时间（秒）
     */
    void cachePermissionMapping(Long permissionId, Object permission, int ttlSeconds);

    /**
     * 获取角色权限映射
     *
     * @param roleId 角色ID
     * @return 权限ID列表，null表示缓存未命中
     */
    Set<Long> getRolePermissions(Long roleId);

    /**
     * 缓存角色权限映射
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @param ttlSeconds 缓存时间（秒）
     */
    void cacheRolePermissions(Long roleId, Set<Long> permissionIds, int ttlSeconds);

    /**
     * 清除角色权限缓存
     *
     * @param roleId 角色ID
     */
    void evictRolePermissions(Long roleId);

    /**
     * 获取用户角色映射
     *
     * @param userId 用户ID
     * @return 角色ID列表，null表示缓存未命中
     */
    Set<Long> getUserRoleIds(Long userId);

    /**
     * 缓存用户角色映射
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @param ttlSeconds 缓存时间（秒）
     */
    void cacheUserRoleIds(Long userId, Set<Long> roleIds, int ttlSeconds);

    /**
     * 清除用户角色映射缓存
     *
     * @param userId 用户ID
     */
    void evictUserRoleIds(Long userId);

    /**
     * 预热用户缓存
     *
     * @param userId 用户ID
     */
    void warmUpUserCache(Long userId);

    /**
     * 预热权限数据缓存
     *
     * @param permissionIds 权限ID列表
     */
    void warmUpPermissionCache(Set<Long> permissionIds);

    /**
     * 预热角色数据缓存
     *
     * @param roleIds 角色ID列表
     */
    void warmUpRoleCache(Set<Long> roleIds);

    /**
     * 批量预热缓存
     *
     * @param userIds 用户ID列表
     */
    void warmUpBatchCache(Set<Long> userIds);

    /**
     * 通用缓存获取
     */
    <T> T get(String key);

    /**
     * 通用缓存设置
     */
    <T> void put(String key, T value, long ttlSeconds);

    /**
     * 通用缓存删除
     */
    void evict(String key);

    /**
     * 清除所有权限相关缓存
     */
    void evictAll();

    /**
     * 清除过期缓存
     */
    void evictExpired();

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    CacheStats getCacheStats();

    /**
     * 缓存统计信息
     */
    class CacheStats {
        private final long localCacheSize;
        private final long redisCacheSize;
        private final double localHitRate;
        private final double redisHitRate;
        private final double overallHitRate;
        private final long evictionCount;
        private final long loadCount;
        private final long loadExceptionCount;
        private final double averageLoadTime;

        public CacheStats(long localCacheSize, long redisCacheSize, double localHitRate,
                         double redisHitRate, double overallHitRate, long evictionCount,
                         long loadCount, long loadExceptionCount, double averageLoadTime) {
            this.localCacheSize = localCacheSize;
            this.redisCacheSize = redisCacheSize;
            this.localHitRate = localHitRate;
            this.redisHitRate = redisHitRate;
            this.overallHitRate = overallHitRate;
            this.evictionCount = evictionCount;
            this.loadCount = loadCount;
            this.loadExceptionCount = loadExceptionCount;
            this.averageLoadTime = averageLoadTime;
        }

        public long getLocalCacheSize() {
            return localCacheSize;
        }

        public long getRedisCacheSize() {
            return redisCacheSize;
        }

        public double getLocalHitRate() {
            return localHitRate;
        }

        public double getRedisHitRate() {
            return redisHitRate;
        }

        public double getOverallHitRate() {
            return overallHitRate;
        }

        public long getEvictionCount() {
            return evictionCount;
        }

        public long getLoadCount() {
            return loadCount;
        }

        public long getLoadExceptionCount() {
            return loadExceptionCount;
        }

        public double getAverageLoadTime() {
            return averageLoadTime;
        }

        @Override
        public String toString() {
            return "CacheStats{" +
                    "localCacheSize=" + localCacheSize +
                    ", redisCacheSize=" + redisCacheSize +
                    ", localHitRate=" + String.format("%.2f%%", localHitRate * 100) +
                    ", redisHitRate=" + String.format("%.2f%%", redisHitRate * 100) +
                    ", overallHitRate=" + String.format("%.2f%%", overallHitRate * 100) +
                    ", evictionCount=" + evictionCount +
                    ", loadCount=" + loadCount +
                    ", loadExceptionCount=" + loadExceptionCount +
                    ", averageLoadTime=" + String.format("%.2fms", averageLoadTime) +
                    '}';
        }
    }

    /**
     * 缓存配置
     */
    class CacheConfig {
        private final int localCacheMaximumSize;
        private final int localCacheExpireAfterWriteMinutes;
        private final int localCacheExpireAfterAccessMinutes;
        private final int redisCacheDefaultTtlSeconds;
        private final int redisCacheMaximumTtlSeconds;
        private final boolean enableLocalCache;
        private final boolean enableRedisCache;
        private final boolean enableCacheStats;

        public CacheConfig(int localCacheMaximumSize, int localCacheExpireAfterWriteMinutes,
                          int localCacheExpireAfterAccessMinutes, int redisCacheDefaultTtlSeconds,
                          int redisCacheMaximumTtlSeconds, boolean enableLocalCache,
                          boolean enableRedisCache, boolean enableCacheStats) {
            this.localCacheMaximumSize = localCacheMaximumSize;
            this.localCacheExpireAfterWriteMinutes = localCacheExpireAfterWriteMinutes;
            this.localCacheExpireAfterAccessMinutes = localCacheExpireAfterAccessMinutes;
            this.redisCacheDefaultTtlSeconds = redisCacheDefaultTtlSeconds;
            this.redisCacheMaximumTtlSeconds = redisCacheMaximumTtlSeconds;
            this.enableLocalCache = enableLocalCache;
            this.enableRedisCache = enableRedisCache;
            this.enableCacheStats = enableCacheStats;
        }

        /**
         * 默认配置
         */
        public static CacheConfig defaultConfig() {
            return new CacheConfig(
                10000,                    // 本地缓存最大条目数
                10,                       // 写入后10分钟过期
                5,                        // 访问后5分钟过期
                300,                      // Redis默认缓存5分钟
                3600,                     // Redis最大缓存1小时
                true,                     // 启用本地缓存
                true,                     // 启用Redis缓存
                true                      // 启用缓存统计
            );
        }

        /**
         * 高性能配置
         */
        public static CacheConfig highPerformanceConfig() {
            return new CacheConfig(
                50000,                    // 本地缓存最大条目数
                30,                       // 写入后30分钟过期
                15,                       // 访问后15分钟过期
                600,                      // Redis默认缓存10分钟
                7200,                     // Redis最大缓存2小时
                true,                     // 启用本地缓存
                true,                     // 启用Redis缓存
                true                      // 启用缓存统计
            );
        }

        /**
         * 低内存配置
         */
        public static CacheConfig lowMemoryConfig() {
            return new CacheConfig(
                5000,                     // 本地缓存最大条目数
                5,                        // 写入后5分钟过期
                2,                        // 访问后2分钟过期
                180,                      // Redis默认缓存3分钟
                1800,                     // Redis最大缓存30分钟
                true,                     // 启用本地缓存
                true,                     // 启用Redis缓存
                false                     // 禁用缓存统计
            );
        }

        public int getLocalCacheMaximumSize() {
            return localCacheMaximumSize;
        }

        public int getLocalCacheExpireAfterWriteMinutes() {
            return localCacheExpireAfterWriteMinutes;
        }

        public int getLocalCacheExpireAfterAccessMinutes() {
            return localCacheExpireAfterAccessMinutes;
        }

        public int getRedisCacheDefaultTtlSeconds() {
            return redisCacheDefaultTtlSeconds;
        }

        public int getRedisCacheMaximumTtlSeconds() {
            return redisCacheMaximumTtlSeconds;
        }

        public boolean isEnableLocalCache() {
            return enableLocalCache;
        }

        public boolean isEnableRedisCache() {
            return enableRedisCache;
        }

        public boolean isEnableCacheStats() {
            return enableCacheStats;
        }
    }
}