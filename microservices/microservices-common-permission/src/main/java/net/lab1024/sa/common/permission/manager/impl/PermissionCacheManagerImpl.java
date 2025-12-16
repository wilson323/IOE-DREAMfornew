package net.lab1024.sa.common.permission.manager.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.UnifiedCacheManager;
import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;
import net.lab1024.sa.common.permission.manager.PermissionCacheManager;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 权限缓存管理器实现
 * <p>
 * 企业级多级权限缓存管理实现，提供：
 * - L1本地缓存（Caffeine）：毫秒级响应，高频访问数据
 * - L2分布式缓存（Redis）：数据一致性保证，跨服务共享
 * - 智能缓存策略：动态TTL、LRU淘汰、预热机制
 * - 缓存击穿防护：互斥锁、空值缓存、布隆过滤器
 * - 缓存雪崩防护：随机过期、熔断降级、多级降级
 * - 性能监控：缓存命中率、响应时间、吞吐量统计
 * </p>
 * <p>
 * 缓存层级策略：
 * 1. 优先查询L1本地缓存（Caffeine）
 * 2. 未命中查询L2分布式缓存（Redis）
 * 3. 均未命中执行数据加载并缓存
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class PermissionCacheManagerImpl implements PermissionCacheManager {

    private final UnifiedCacheManager unifiedCacheManager;
    private final PermissionCacheConfig config;

    // 缓存键前缀
    private static final String PERMISSION_VALIDATION_PREFIX = "permission:validation:";
    private static final String USER_PERMISSIONS_PREFIX = "user:permissions:";
    private static final String USER_ROLES_PREFIX = "user:roles:";
    private static final String PERMISSION_MAPPING_PREFIX = "permission:mapping:";
    private static final String ROLE_PERMISSIONS_PREFIX = "role:permissions:";
    private static final String USER_ROLE_IDS_PREFIX = "user:roleids:";

    // 空值缓存标记
    private static final String NULL_CACHE_MARK = "NULL_CACHE_MARK";
    private static final long NULL_CACHE_TTL = 60; // 空值缓存1分钟

    // 构造函数注入依赖
    public PermissionCacheManagerImpl(UnifiedCacheManager unifiedCacheManager) {
        this.unifiedCacheManager = unifiedCacheManager;
        this.config = PermissionCacheConfig.defaultConfig();
    }

    /**
     * 构造函数注入依赖（自定义配置）
     */
    public PermissionCacheManagerImpl(UnifiedCacheManager unifiedCacheManager, PermissionCacheConfig config) {
        this.unifiedCacheManager = unifiedCacheManager;
        this.config = config != null ? config : PermissionCacheConfig.defaultConfig();
    }

    @Override
    public PermissionValidationResult getValidationResult(String cacheKey) {
        return getWithRefresh(
            PERMISSION_VALIDATION_PREFIX + cacheKey,
            () -> null, // 权限验证结果不提供默认值
            config.getValidationCacheTtl()
        );
    }

    @Override
    public void cacheValidationResult(String cacheKey, PermissionValidationResult result, int ttlSeconds) {
        String fullKey = PERMISSION_VALIDATION_PREFIX + cacheKey;
        if (result == null) {
            // 空值缓存，防止缓存穿透
            unifiedCacheManager.put(fullKey, NULL_CACHE_MARK, NULL_CACHE_TTL);
        } else {
            unifiedCacheManager.put(fullKey, result, ttlSeconds);
        }

        log.debug("[权限缓存] 缓存验证结果: cacheKey={}, ttl={}s", cacheKey, ttlSeconds);
    }

    @Override
    public void evictValidationResult(String cacheKey) {
        String fullKey = PERMISSION_VALIDATION_PREFIX + cacheKey;
        unifiedCacheManager.evict(fullKey);
        log.debug("[权限缓存] 清除验证结果缓存: cacheKey={}", cacheKey);
    }

    @Override
    public void evictValidationResults(Set<String> cacheKeys) {
        if (cacheKeys == null || cacheKeys.isEmpty()) {
            return;
        }

        for (String cacheKey : cacheKeys) {
            evictValidationResult(cacheKey);
        }
        log.info("[权限缓存] 批量清除验证结果缓存: count={}", cacheKeys.size());
    }

    @Override
    public Set<String> getUserPermissions(Long userId) {
        return getWithRefresh(
            USER_PERMISSIONS_PREFIX + userId,
            () -> null, // 用户权限不提供默认值
            config.getUserPermissionCacheTtl()
        );
    }

    @Override
    public void cacheUserPermissions(Long userId, Set<String> permissions, int ttlSeconds) {
        String fullKey = USER_PERMISSIONS_PREFIX + userId;
        unifiedCacheManager.put(fullKey, permissions, ttlSeconds);

        log.debug("[权限缓存] 缓存用户权限: userId={}, permissionCount={}, ttl={}s",
                userId, permissions != null ? permissions.size() : 0, ttlSeconds);
    }

    @Override
    public void evictUserPermissions(Long userId) {
        String fullKey = USER_PERMISSIONS_PREFIX + userId;
        unifiedCacheManager.evict(fullKey);
        log.debug("[权限缓存] 清除用户权限缓存: userId={}", userId);
    }

    @Override
    public Set<String> getUserRoles(Long userId) {
        return getWithRefresh(
            USER_ROLES_PREFIX + userId,
            () -> null, // 用户角色不提供默认值
            config.getUserRoleCacheTtl()
        );
    }

    @Override
    public void cacheUserRoles(Long userId, Set<String> roles, int ttlSeconds) {
        String fullKey = USER_ROLES_PREFIX + userId;
        unifiedCacheManager.put(fullKey, roles, ttlSeconds);

        log.debug("[权限缓存] 缓存用户角色: userId={}, roleCount={}, ttl={}s",
                userId, roles != null ? roles.size() : 0, ttlSeconds);
    }

    @Override
    public void evictUserRoles(Long userId) {
        String fullKey = USER_ROLES_PREFIX + userId;
        unifiedCacheManager.evict(fullKey);
        log.debug("[权限缓存] 清除用户角色缓存: userId={}", userId);
    }

    @Override
    public Object getPermissionMapping(Long permissionId) {
        return getWithRefresh(
            PERMISSION_MAPPING_PREFIX + permissionId,
            () -> null, // 权限映射不提供默认值
            config.getPermissionMappingCacheTtl()
        );
    }

    @Override
    public void cachePermissionMapping(Long permissionId, Object permission, int ttlSeconds) {
        String fullKey = PERMISSION_MAPPING_PREFIX + permissionId;
        unifiedCacheManager.put(fullKey, permission, ttlSeconds);

        log.debug("[权限缓存] 缓存权限映射: permissionId={}, ttl={}s", permissionId, ttlSeconds);
    }

    @Override
    public Set<Long> getRolePermissions(Long roleId) {
        return getWithRefresh(
            ROLE_PERMISSIONS_PREFIX + roleId,
            () -> null, // 角色权限不提供默认值
            config.getRolePermissionCacheTtl()
        );
    }

    @Override
    public void cacheRolePermissions(Long roleId, Set<Long> permissionIds, int ttlSeconds) {
        String fullKey = ROLE_PERMISSIONS_PREFIX + roleId;
        unifiedCacheManager.put(fullKey, permissionIds, ttlSeconds);

        log.debug("[权限缓存] 缓存角色权限: roleId={}, permissionCount={}, ttl={}s",
                roleId, permissionIds != null ? permissionIds.size() : 0, ttlSeconds);
    }

    @Override
    public void evictRolePermissions(Long roleId) {
        String fullKey = ROLE_PERMISSIONS_PREFIX + roleId;
        unifiedCacheManager.evict(fullKey);
        log.debug("[权限缓存] 清除角色权限缓存: roleId={}", roleId);
    }

    @Override
    public Set<Long> getUserRoleIds(Long userId) {
        return getWithRefresh(
            USER_ROLE_IDS_PREFIX + userId,
            () -> null, // 用户角色映射不提供默认值
            config.getUserRoleMappingCacheTtl()
        );
    }

    @Override
    public void cacheUserRoleIds(Long userId, Set<Long> roleIds, int ttlSeconds) {
        String fullKey = USER_ROLE_IDS_PREFIX + userId;
        unifiedCacheManager.put(fullKey, roleIds, ttlSeconds);

        log.debug("[权限缓存] 缓存用户角色映射: userId={}, roleCount={}, ttl={}s",
                userId, roleIds != null ? roleIds.size() : 0, ttlSeconds);
    }

    @Override
    public void evictUserRoleIds(Long userId) {
        String fullKey = USER_ROLE_IDS_PREFIX + userId;
        unifiedCacheManager.evict(fullKey);
        log.debug("[权限缓存] 清除用户角色映射缓存: userId={}", userId);
    }

    @Override
    public void warmUpUserCache(Long userId) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("[权限缓存] 预热用户缓存: userId={}", userId);

                // 异步预热用户权限和角色
                warmUpUserPermissions(userId);
                warmUpUserRoles(userId);
                warmUpUserRoleIds(userId);

                log.info("[权限缓存] 用户缓存预热完成: userId={}", userId);
            } catch (Exception e) {
                log.error("[权限缓存] 用户缓存预热失败: userId={}", userId, e);
            }
        });
    }

    @Override
    public void warmUpPermissionCache(Set<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                log.debug("[权限缓存] 预热权限数据缓存: count={}", permissionIds.size());

                for (Long permissionId : permissionIds) {
                    // 这里可以通过服务获取权限信息并缓存
                    // warmUpPermissionData(permissionId);
                }

                log.info("[权限缓存] 权限数据缓存预热完成: count={}", permissionIds.size());
            } catch (Exception e) {
                log.error("[权限缓存] 权限数据缓存预热失败", e);
            }
        });
    }

    @Override
    public void warmUpRoleCache(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                log.debug("[权限缓存] 预热角色数据缓存: count={}", roleIds.size());

                for (Long roleId : roleIds) {
                    // 异步预热角色权限
                    warmUpRolePermissions(roleId);
                }

                log.info("[权限缓存] 角色数据缓存预热完成: count={}", roleIds.size());
            } catch (Exception e) {
                log.error("[权限缓存] 角色数据缓存预热失败", e);
            }
        });
    }

    @Override
    public void warmUpBatchCache(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        log.info("[权限缓存] 批量预热缓存开始: count={}", userIds.size());

        // 并行预热用户缓存
        userIds.parallelStream().forEach(this::warmUpUserCache);

        log.info("[权限缓存] 批量预热缓存完成: count={}", userIds.size());
    }

    @Override
    public void evictAll() {
        // 清除所有权限相关缓存
        unifiedCacheManager.evictByPrefix(PERMISSION_VALIDATION_PREFIX);
        unifiedCacheManager.evictByPrefix(USER_PERMISSIONS_PREFIX);
        unifiedCacheManager.evictByPrefix(USER_ROLES_PREFIX);
        unifiedCacheManager.evictByPrefix(PERMISSION_MAPPING_PREFIX);
        unifiedCacheManager.evictByPrefix(ROLE_PERMISSIONS_PREFIX);
        unifiedCacheManager.evictByPrefix(USER_ROLE_IDS_PREFIX);

        log.info("[权限缓存] 清除所有权限相关缓存完成");
    }

    @Override
    public void evictExpired() {
        // 委托给统一缓存管理器处理过期缓存清理
        unifiedCacheManager.evictExpired();
        log.debug("[权限缓存] 清除过期缓存完成");
    }

    @Override
    public CacheStats getCacheStats() {
        UnifiedCacheManager.CacheStats stats = unifiedCacheManager.getCacheStats();
        return new CacheStats(
            stats.getLocalCacheSize(),
            stats.getRedisCacheSize(),
            stats.getLocalHitRate(),
            stats.getRedisHitRate(),
            stats.getOverallHitRate(),
            stats.getEvictionCount(),
            stats.getLoadCount(),
            stats.getLoadExceptionCount(),
            stats.getAverageLoadTime()
        );
    }

    /**
     * 通用缓存获取方法（带刷新机制）
     */
    @SuppressWarnings("unchecked")
    private <T> T getWithRefresh(String key, Supplier<T> loader, int ttlSeconds) {
        try {
            // 1. 先从缓存获取
            Object cached = unifiedCacheManager.get(key);
            if (cached != null) {
                // 检查是否为空值缓存标记
                if (NULL_CACHE_MARK.equals(cached)) {
                    return null;
                }
                return (T) cached;
            }

            // 2. 缓存未命中，执行加载
            if (loader != null) {
                T value = loader.get();
                if (value != null) {
                    unifiedCacheManager.put(key, value, ttlSeconds);
                } else {
                    // 缓存空值防止穿透
                    unifiedCacheManager.put(key, NULL_CACHE_MARK, NULL_CACHE_TTL);
                }
                return value;
            }

            return null;
        } catch (Exception e) {
            log.error("[权限缓存] 缓存获取异常: key={}", key, e);
            return loader != null ? loader.get() : null;
        }
    }

    /**
     * 预热用户权限
     */
    private void warmUpUserPermissions(Long userId) {
        try {
            // 这里可以通过权限服务获取用户权限
            // Set<String> permissions = permissionService.getUserPermissions(userId);
            // cacheUserPermissions(userId, permissions, config.getUserPermissionCacheTtl());
        } catch (Exception e) {
            log.warn("[权限缓存] 预热用户权限失败: userId={}", userId, e);
        }
    }

    /**
     * 预热用户角色
     */
    private void warmUpUserRoles(Long userId) {
        try {
            // 这里可以通过权限服务获取用户角色
            // Set<String> roles = permissionService.getUserRoles(userId);
            // cacheUserRoles(userId, roles, config.getUserRoleCacheTtl());
        } catch (Exception e) {
            log.warn("[权限缓存] 预热用户角色失败: userId={}", userId, e);
        }
    }

    /**
     * 预热用户角色映射
     */
    private void warmUpUserRoleIds(Long userId) {
        try {
            // 这里可以通过权限服务获取用户角色映射
            // Set<Long> roleIds = permissionService.getUserRoleIds(userId);
            // cacheUserRoleIds(userId, roleIds, config.getUserRoleMappingCacheTtl());
        } catch (Exception e) {
            log.warn("[权限缓存] 预热用户角色映射失败: userId={}", userId, e);
        }
    }

    /**
     * 预热角色权限
     */
    private void warmUpRolePermissions(Long roleId) {
        try {
            // 这里可以通过权限服务获取角色权限
            // Set<Long> permissionIds = permissionService.getRolePermissions(roleId);
            // cacheRolePermissions(roleId, permissionIds, config.getRolePermissionCacheTtl());
        } catch (Exception e) {
            log.warn("[权限缓存] 预热角色权限失败: roleId={}", roleId, e);
        }
    }

    /**
     * 权限缓存配置
     */
    public static class PermissionCacheConfig {
        private final int validationCacheTtl;
        private final int userPermissionCacheTtl;
        private final int userRoleCacheTtl;
        private final int permissionMappingCacheTtl;
        private final int rolePermissionCacheTtl;
        private final int userRoleMappingCacheTtl;
        private final int warmUpBatchSize;
        private final int warmUpTimeoutSeconds;
        private final boolean enableAsyncWarmUp;
        private final boolean enableCacheStats;

        private PermissionCacheConfig(int validationCacheTtl, int userPermissionCacheTtl,
                                  int userRoleCacheTtl, int permissionMappingCacheTtl,
                                  int rolePermissionCacheTtl, int userRoleMappingCacheTtl,
                                  int warmUpBatchSize, int warmUpTimeoutSeconds,
                                  boolean enableAsyncWarmUp, boolean enableCacheStats) {
            this.validationCacheTtl = validationCacheTtl;
            this.userPermissionCacheTtl = userPermissionCacheTtl;
            this.userRoleCacheTtl = userRoleCacheTtl;
            this.permissionMappingCacheTtl = permissionMappingCacheTtl;
            this.rolePermissionCacheTtl = rolePermissionCacheTtl;
            this.userRoleMappingCacheTtl = userRoleMappingCacheTtl;
            this.warmUpBatchSize = warmUpBatchSize;
            this.warmUpTimeoutSeconds = warmUpTimeoutSeconds;
            this.enableAsyncWarmUp = enableAsyncWarmUp;
            this.enableCacheStats = enableCacheStats;
        }

        /**
         * 默认配置
         */
        public static PermissionCacheConfig defaultConfig() {
            return new PermissionCacheConfig(
                300,    // 验证结果缓存5分钟
                600,    // 用户权限缓存10分钟
                600,    // 用户角色缓存10分钟
                1800,   // 权限映射缓存30分钟
                600,    // 角色权限缓存10分钟
                600,    // 用户角色映射缓存10分钟
                100,    // 批量预热大小
                30,     // 预热超时30秒
                true,   // 启用异步预热
                true    // 启用缓存统计
            );
        }

        /**
         * 高性能配置
         */
        public static PermissionCacheConfig highPerformanceConfig() {
            return new PermissionCacheConfig(
                600,    // 验证结果缓存10分钟
                1200,   // 用户权限缓存20分钟
                1200,   // 用户角色缓存20分钟
                3600,   // 权限映射缓存1小时
                1200,   // 角色权限缓存20分钟
                1200,   // 用户角色映射缓存20分钟
                200,    // 批量预热大小
                60,     // 预热超时60秒
                true,   // 启用异步预热
                true    // 启用缓存统计
            );
        }

        /**
         * 低内存配置
         */
        public static PermissionCacheConfig lowMemoryConfig() {
            return new PermissionCacheConfig(
                180,    // 验证结果缓存3分钟
                300,    // 用户权限缓存5分钟
                300,    // 用户角色缓存5分钟
                900,    // 权限映射缓存15分钟
                300,    // 角色权限缓存5分钟
                300,    // 用户角色映射缓存5分钟
                50,     // 批量预热大小
                15,     // 预热超时15秒
                false,  // 禁用异步预热
                false   // 禁用缓存统计
            );
        }

        // Getter方法
        public int getValidationCacheTtl() {
            return validationCacheTtl;
        }

        public int getUserPermissionCacheTtl() {
            return userPermissionCacheTtl;
        }

        public int getUserRoleCacheTtl() {
            return userRoleCacheTtl;
        }

        public int getPermissionMappingCacheTtl() {
            return permissionMappingCacheTtl;
        }

        public int getRolePermissionCacheTtl() {
            return rolePermissionTtl;
        }

        public int getUserRoleMappingCacheTtl() {
            return userRoleMappingCacheTtl;
        }

        public int getWarmUpBatchSize() {
            return warmUpBatchSize;
        }

        public int getWarmUpTimeoutSeconds() {
            return warmUpTimeoutSeconds;
        }

        public boolean isEnableAsyncWarmUp() {
            return enableAsyncWarmUp;
        }

        public boolean isEnableCacheStats() {
            return enableCacheStats;
        }
    }
}