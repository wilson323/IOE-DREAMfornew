package net.lab1024.sa.base.common.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 模块缓存服务基类
 * <p>
 * 严格遵循repowiki模块化架构规范：
 * - 统一的模块缓存服务模板
 * - 标准化的缓存操作接口
 * - 自动化的监控和日志记录
 * - 支持异步和批量操作
 * - 缓存穿透和雪崩保护
 *
 * @author SmartAdmin Team
 * @since 2025-11-17
 */
@Slf4j
public abstract class BaseModuleCacheService {

    @Resource
    protected UnifiedCacheService unifiedCacheService;

    /**
     * 获取当前模块标识
     * 子类必须实现此方法来指定所属模块
     *
     * @return 缓存模块
     */
    protected abstract CacheModule getModule();

    /**
     * 获取模块默认的缓存命名空间
     * 子类可以重写此方法来指定默认命名空间
     *
     * @return 默认命名空间
     */
    protected String getDefaultNamespace() {
        return "default";
    }

    // ========== 基础缓存操作 ==========

    /**
     * 获取缓存值
     */
    protected <T> T getCachedData(String key, Class<T> clazz) {
        return getCachedData(getDefaultNamespace(), key, clazz);
    }

    /**
     * 获取缓存值（指定命名空间）
     */
    protected <T> T getCachedData(String namespace, String key, Class<T> clazz) {
        log.debug("获取缓存数据: module={}, namespace={}, key={}", getModule().getCode(), namespace, key);
        return unifiedCacheService.get(getModule(), namespace, key, clazz);
    }

    /**
     * 获取缓存值（带类型引用）
     */
    protected <T> T getCachedData(String key, TypeReference<T> typeReference) {
        return getCachedData(getDefaultNamespace(), key, typeReference);
    }

    /**
     * 获取缓存值（指定命名空间，带类型引用）
     */
    protected <T> T getCachedData(String namespace, String key, TypeReference<T> typeReference) {
        log.debug("获取缓存数据: module={}, namespace={}, key={}", getModule().getCode(), namespace, key);
        return unifiedCacheService.get(getModule(), namespace, key, typeReference);
    }

    /**
     * 设置缓存值
     */
    protected <T> void setCachedData(String key, T value, BusinessDataType dataType) {
        setCachedData(getDefaultNamespace(), key, value, dataType);
    }

    /**
     * 设置缓存值（指定命名空间）
     */
    protected <T> void setCachedData(String namespace, String key, T value, BusinessDataType dataType) {
        log.debug("设置缓存数据: module={}, namespace={}, key={}, dataType={}",
                getModule().getCode(), namespace, key, dataType.getDisplayName());
        unifiedCacheService.set(getModule(), namespace, key, value, dataType);
    }

    /**
     * 设置缓存值（自定义TTL）
     */
    protected <T> void setCachedData(String namespace, String key, T value, long ttl, java.util.concurrent.TimeUnit timeUnit) {
        log.debug("设置缓存数据: module={}, namespace={}, key={}, ttl={}ms",
                getModule().getCode(), namespace, key, timeUnit.toMillis(ttl));
        unifiedCacheService.set(getModule(), namespace, key, value, timeUnit.toSeconds(ttl));
    }

    /**
     * 删除缓存数据
     */
    protected boolean deleteCachedData(String key) {
        return deleteCachedData(getDefaultNamespace(), key);
    }

    /**
     * 删除缓存数据（指定命名空间）
     */
    protected boolean deleteCachedData(String namespace, String key) {
        log.debug("删除缓存数据: module={}, namespace={}, key={}", getModule().getCode(), namespace, key);
        boolean existed = unifiedCacheService.exists(getModule(), namespace, key);
        unifiedCacheService.delete(getModule(), namespace, key);
        return existed;
    }

    /**
     * 检查缓存是否存在
     */
    protected boolean cachedDataExists(String key) {
        return cachedDataExists(getDefaultNamespace(), key);
    }

    /**
     * 检查缓存是否存在（指定命名空间）
     */
    protected boolean cachedDataExists(String namespace, String key) {
        log.debug("检查缓存存在: module={}, namespace={}, key={}", getModule().getCode(), namespace, key);
        return unifiedCacheService.exists(getModule(), namespace, key);
    }

    // ========== 高级缓存操作 ==========

    /**
     * 获取或设置缓存（缓存穿透保护）
     */
    protected <T> T getOrSetCachedData(String key, Supplier<T> dataLoader,
                                       Class<T> clazz, BusinessDataType dataType) {
        return getOrSetCachedData(getDefaultNamespace(), key, dataLoader, clazz, dataType);
    }

    /**
     * 获取或设置缓存（指定命名空间）
     */
    protected <T> T getOrSetCachedData(String namespace, String key, Supplier<T> dataLoader,
                                       Class<T> clazz, BusinessDataType dataType) {
        log.debug("获取或设置缓存: module={}, namespace={}, key={}", getModule().getCode(), namespace, key);
        return unifiedCacheService.getOrSet(getModule(), namespace, key, clazz, dataLoader, dataType);
    }

    /**
     * 批量获取缓存
     */
    // 🔧 修复：使用独立的BatchCacheResult
    protected <T> BatchCacheResult<T> mGetCachedData(List<String> keys, Class<T> clazz) {
        return mGetCachedData(getDefaultNamespace(), keys, clazz);
    }

    /**
     * 批量获取缓存（指定命名空间）
     */
    // 🔧 修复：使用独立的BatchCacheResult
    protected <T> BatchCacheResult<T> mGetCachedData(String namespace, List<String> keys, Class<T> clazz) {
        log.debug("批量获取缓存: module={}, namespace={}, count={}", getModule().getCode(), namespace, keys.size());
        Map<String, T> resultMap = unifiedCacheService.mGet(getModule(), namespace, keys, clazz);
        return BatchCacheResult.success(resultMap);
    }

    /**
     * 批量设置缓存
     */
    // 🔧 修复：使用独立的BatchCacheResult
    protected <T> BatchCacheResult<T> mSetCachedData(Map<String, T> keyValues, BusinessDataType dataType) {
        return mSetCachedData(getDefaultNamespace(), keyValues, dataType);
    }

    /**
     * 批量设置缓存（指定命名空间）
     */
    // 🔧 修复：使用独立的BatchCacheResult
    protected <T> BatchCacheResult<T> mSetCachedData(String namespace, Map<String, T> keyValues, BusinessDataType dataType) {
        log.debug("批量设置缓存: module={}, namespace={}, count={}", getModule().getCode(), namespace, keyValues.size());
        unifiedCacheService.mSet(getModule(), namespace, keyValues, dataType);
        return BatchCacheResult.success(new HashMap<>());
    }

    // ========== 异步缓存操作 ==========

    /**
     * 异步获取缓存
     */
    protected <T> CompletableFuture<T> getCachedDataAsync(String key, Class<T> clazz) {
        return getCachedDataAsync(getDefaultNamespace(), key, clazz);
    }

    /**
     * 异步获取缓存（指定命名空间）
     */
    protected <T> CompletableFuture<T> getCachedDataAsync(String namespace, String key, Class<T> clazz) {
        log.debug("异步获取缓存: module={}, namespace={}, key={}", getModule().getCode(), namespace, key);
        return unifiedCacheService.getAsync(getModule(), namespace, key, clazz);
    }

    /**
     * 异步设置缓存
     */
    protected <T> CompletableFuture<Void> setCachedDataAsync(String key, T value, BusinessDataType dataType) {
        return setCachedDataAsync(getDefaultNamespace(), key, value, dataType);
    }

    /**
     * 异步设置缓存（指定命名空间）
     */
    protected <T> CompletableFuture<Void> setCachedDataAsync(String namespace, String key, T value, BusinessDataType dataType) {
        log.debug("异步设置缓存: module={}, namespace={}, key={}", getModule().getCode(), namespace, key);
        return unifiedCacheService.setAsync(getModule(), namespace, key, value, dataType);
    }

    /**
     * 异步删除缓存
     */
    protected CompletableFuture<Boolean> deleteCachedDataAsync(String key) {
        return deleteCachedDataAsync(getDefaultNamespace(), key);
    }

    /**
     * 异步删除缓存（指定命名空间）
     */
    protected CompletableFuture<Boolean> deleteCachedDataAsync(String namespace, String key) {
        log.debug("异步删除缓存: module={}, namespace={}, key={}", getModule().getCode(), namespace, key);
        return unifiedCacheService.deleteAsync(getModule(), namespace, key)
            .thenApply(v -> Boolean.TRUE);
    }

    // ========== 缓存管理操作 ==========

    /**
     * 清除模块下所有缓存
     */
    protected void clearModuleCache() {
        log.info("清除模块缓存: module={}", getModule().getCode());
        unifiedCacheService.clearModule(getModule());
    }

    /**
     * 根据模式删除缓存
     */
    protected int deleteCachedDataByPattern(String pattern) {
        return deleteCachedDataByPattern(getDefaultNamespace(), pattern);
    }

    /**
     * 根据模式删除缓存（指定命名空间）
     */
    protected int deleteCachedDataByPattern(String namespace, String pattern) {
        log.debug("模式删除缓存: module={}, namespace={}, pattern={}", getModule().getCode(), namespace, pattern);
        return unifiedCacheService.deleteByPattern(getModule(), namespace, pattern);
    }

    /**
     * 获取模块缓存统计信息
     */
    protected Map<String, Object> getModuleCacheStatistics() {
        return unifiedCacheService.getCacheStatistics(getModule());
    }

    /**
     * 获取模块缓存健康度评估
     */
    protected Map<String, Object> getModuleHealthAssessment() {
        return unifiedCacheService.getHealthAssessment();
    }

    // ========== 便捷操作方法 ==========

    /**
     * 缓存用户相关数据
     */
    protected <T> void cacheUserData(Long userId, String dataKey, T data, BusinessDataType dataType) {
        String key = String.format("user:%d:%s", userId, dataKey);
        setCachedData("user", key, data, dataType);
    }

    /**
     * 获取用户相关缓存数据
     */
    protected <T> T getUserCachedData(Long userId, String dataKey, Class<T> clazz) {
        String key = String.format("user:%d:%s", userId, dataKey);
        return getCachedData("user", key, clazz);
    }

    /**
     * 清除用户相关缓存
     */
    protected void evictUserData(Long userId) {
        deleteCachedDataByPattern("user", String.format("user:%d:*", userId));
    }

    /**
     * 缓存设备相关数据
     */
    protected <T> void cacheDeviceData(Long deviceId, String dataKey, T data, BusinessDataType dataType) {
        String key = String.format("device:%d:%s", deviceId, dataKey);
        setCachedData("device", key, data, dataType);
    }

    /**
     * 获取设备相关缓存数据
     */
    protected <T> T getDeviceCachedData(Long deviceId, String dataKey, Class<T> clazz) {
        String key = String.format("device:%d:%s", deviceId, dataKey);
        return getCachedData("device", key, clazz);
    }

    /**
     * 清除设备相关缓存
     */
    protected void evictDeviceData(Long deviceId) {
        deleteCachedDataByPattern("device", String.format("device:%d:*", deviceId));
    }

    /**
     * 缓存配置相关数据
     */
    protected <T> void cacheConfigData(String configType, String configKey, T data, BusinessDataType dataType) {
        String key = String.format("%s:%s", configType, configKey);
        setCachedData("config", key, data, dataType);
    }

    /**
     * 获取配置相关缓存数据
     */
    protected <T> T getConfigCachedData(String configType, String configKey, Class<T> clazz) {
        String key = String.format("%s:%s", configType, configKey);
        return getCachedData("config", key, clazz);
    }

    /**
     * 清除配置相关缓存
     */
    protected void evictConfigData(String configType) {
        deleteCachedDataByPattern("config", String.format("%s:*", configType));
    }

    // ========== 缓存预热和批量操作 ==========

    /**
     * 缓存预热
     * 子类可以重写此方法来实现特定模块的缓存预热逻辑
     */
    protected CompletableFuture<Void> warmUpCache() {
        log.info("开始{}模块缓存预热", getModule().getDescription());

        try {
            // 默认空实现，子类可以重写
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("{}模块缓存预热失败", getModule().getDescription(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 批量清除相关缓存
     */
    protected CompletableFuture<Void> batchEvictCache(String... patterns) {
        return CompletableFuture.runAsync(() -> {
            for (String pattern : patterns) {
                deleteCachedDataByPattern(pattern);
            }
        });
    }

    /**
     * 获取模块的缓存使用情况摘要
     */
    protected Map<String, Object> getCacheUsageSummary() {
        Map<String, Object> statistics = getModuleCacheStatistics();
        Map<String, Object> health = getModuleHealthAssessment();

        return Map.of(
            "module", getModule().getCode(),
            "description", getModule().getDescription(),
            "statistics", statistics,
            "health", health,
            "timestamp", System.currentTimeMillis()
        );
    }
}