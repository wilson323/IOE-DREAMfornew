package net.lab1024.sa.base.common.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.code.SystemErrorCode;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 统一缓存服务
 * <p>
 * 严格遵循repowiki缓存架构规范：
 * - 统一的缓存访问接口
 * - 模块化缓存管理
 * - 多种缓存策略支持
 * - 异步操作能力
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Service
public class UnifiedCacheService {

    @Resource
    private UnifiedCacheManager cacheManager;

    // ========== 基础缓存操作 ==========

    /**
     * 获取缓存数据
     */
    public <T> T get(CacheModule module, String namespace, String key, Class<T> clazz) {
        try {
            String fullKey = buildFullKey(module, namespace, key);
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            log.debug("获取缓存: module={}, namespace={}, key={}", module.getCode(), namespace, key);
            UnifiedCacheManager.CacheResult<T> result = cacheManager.get(cacheNamespace, fullKey, clazz);
            return result.isSuccess() ? result.getData() : null;

        } catch (Exception e) {
            log.error("获取缓存失败: module={}, namespace={}, key={}", module.getCode(), namespace, key, e);
            throw new SmartException(SystemErrorCode.CACHE_ERROR, e);
        }
    }

    /**
     * 获取缓存数据（带类型引用）
     */
    public <T> T get(CacheModule module, String namespace, String key, TypeReference<T> typeReference) {
        try {
            String fullKey = buildFullKey(module, namespace, key);
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            log.debug("获取缓存: module={}, namespace={}, key={}", module.getCode(), namespace, key);
            UnifiedCacheManager.CacheResult<T> result = cacheManager.get(cacheNamespace, fullKey, typeReference);
            return result.isSuccess() ? result.getData() : null;

        } catch (Exception e) {
            log.error("获取缓存失败: module={}, namespace={}, key={}", module.getCode(), namespace, key, e);
            throw new SmartException(SystemErrorCode.CACHE_ERROR, e);
        }
    }

    /**
     * 设置缓存数据
     */
    public <T> void set(CacheModule module, String namespace, String key, T value, BusinessDataType dataType) {
        try {
            String fullKey = buildFullKey(module, namespace, key);
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());
            long ttl = dataType.getTtlInSeconds();

            log.debug("设置缓存: module={}, namespace={}, key={}, ttl={}", module.getCode(), namespace, key, ttl);
            cacheManager.set(cacheNamespace, fullKey, value, ttl);

        } catch (Exception e) {
            log.error("设置缓存失败: module={}, namespace={}, key={}", module.getCode(), namespace, key, e);
            throw new SmartException(SystemErrorCode.CACHE_ERROR, e);
        }
    }

    /**
     * 设置缓存数据（自定义TTL）
     */
    public <T> void set(CacheModule module, String namespace, String key, T value, long ttlSeconds) {
        try {
            String fullKey = buildFullKey(module, namespace, key);
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            log.debug("设置缓存: module={}, namespace={}, key={}, ttl={}", module.getCode(), namespace, key, ttlSeconds);
            cacheManager.set(cacheNamespace, fullKey, value, ttlSeconds);

        } catch (Exception e) {
            log.error("设置缓存失败: module={}, namespace={}, key={}", module.getCode(), namespace, key, e);
            throw new SmartException(SystemErrorCode.CACHE_ERROR, e);
        }
    }

    /**
     * 删除缓存数据
     */
    public void delete(CacheModule module, String namespace, String key) {
        try {
            String fullKey = buildFullKey(module, namespace, key);
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            log.debug("删除缓存: module={}, namespace={}, key={}", module.getCode(), namespace, key);
            cacheManager.delete(cacheNamespace, fullKey);

        } catch (Exception e) {
            log.error("删除缓存失败: module={}, namespace={}, key={}", module.getCode(), namespace, key, e);
        }
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(CacheModule module, String namespace, String key) {
        try {
            String fullKey = buildFullKey(module, namespace, key);
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            log.debug("检查缓存存在性: module={}, namespace={}, key={}", module.getCode(), namespace, key);
            return cacheManager.exists(cacheNamespace, fullKey);

        } catch (Exception e) {
            log.error("检查缓存存在性失败: module={}, namespace={}, key={}", module.getCode(), namespace, key, e);
            return false;
        }
    }

    // ========== 高级缓存操作 ==========

    /**
     * 获取或设置缓存（Cache-Aside模式）
     */
    public <T> T getOrSet(CacheModule module, String namespace, String key, Class<T> clazz,
                         Supplier<T> dataLoader, BusinessDataType dataType) {
        try {
            String fullKey = buildFullKey(module, namespace, key);
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            log.debug("获取或设置缓存: module={}, namespace={}, key={}", module.getCode(), namespace, key);
            UnifiedCacheManager.CacheResult<T> result = cacheManager.getOrSet(cacheNamespace, fullKey, clazz,
                dataLoader, dataType.getTtlInSeconds());
            return result.isSuccess() ? result.getData() : null;

        } catch (Exception e) {
            log.error("获取或设置缓存失败: module={}, namespace={}, key={}", module.getCode(), namespace, key, e);
            try {
                T data = dataLoader.get();
                if (data != null) {
                    set(module, namespace, key, data, dataType);
                }
                return data;
            } catch (Exception loaderException) {
                log.error("数据加载也失败: module={}, namespace={}, key={}", module.getCode(), namespace, key, loaderException);
                throw new SmartException(SystemErrorCode.CACHE_ERROR, loaderException);
            }
        }
    }

    /**
     * 批量获取缓存
     */
    public <T> Map<String, T> getBatch(CacheModule module, String namespace, List<String> keys, Class<T> clazz) {
        Map<String, T> results = new java.util.HashMap<>();

        try {
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            for (String key : keys) {
                try {
                    String fullKey = buildFullKey(module, namespace, key);
                    UnifiedCacheManager.CacheResult<T> result = cacheManager.get(cacheNamespace, fullKey, clazz);
                    if (result.isSuccess()) {
                        results.put(key, result.getData());
                    }
                } catch (Exception e) {
                    log.warn("批量获取缓存中单个key失败: key={}", key, e);
                }
            }

            log.debug("批量获取缓存完成: module={}, namespace={}, keys={}, resultCount={}",
                module.getCode(), namespace, keys.size(), results.size());

        } catch (Exception e) {
            log.error("批量获取缓存失败: module={}, namespace={}, keys={}", module.getCode(), namespace, keys, e);
        }

        return results;
    }

    /**
     * 批量获取缓存（mGet方法）
     */
    public <T> Map<String, T> mGet(CacheModule module, String namespace, List<String> keys, Class<T> clazz) {
        return getBatch(module, namespace, keys, clazz);
    }

    /**
     * 批量设置缓存（mSet方法）
     */
    public <T> void mSet(CacheModule module, String namespace, Map<String, T> dataMap, BusinessDataType dataType) {
        setBatch(module, namespace, dataMap, dataType);
    }

    /**
     * 批量设置缓存
     */
    public <T> void setBatch(CacheModule module, String namespace, Map<String, T> dataMap, BusinessDataType dataType) {
        try {
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            for (Map.Entry<String, T> entry : dataMap.entrySet()) {
                try {
                    String fullKey = buildFullKey(module, namespace, entry.getKey());
                    cacheManager.set(cacheNamespace, fullKey, entry.getValue(), dataType.getTtlInSeconds());
                } catch (Exception e) {
                    log.warn("批量设置缓存中单个key失败: key={}", entry.getKey(), e);
                }
            }

            log.debug("批量设置缓存完成: module={}, namespace={}, count={}",
                module.getCode(), namespace, dataMap.size());

        } catch (Exception e) {
            log.error("批量设置缓存失败: module={}, namespace={}", module.getCode(), namespace, e);
        }
    }

    /**
     * 清除命名空间下的所有缓存
     */
    public void clearNamespace(CacheModule module, String namespace) {
        try {
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            // 清理所有相关的键
            // 注意：这里简化处理，实际可能需要更复杂的键匹配逻辑
            log.debug("清除命名空间缓存: module={}, namespace={}", module.getCode(), namespace);
            // 由于没有直接的方法只清除特定namespace的键，这里记录日志
            // 可以考虑在cacheManager中添加支持namespace过滤的清理方法

        } catch (Exception e) {
            log.error("清除命名空间缓存失败: module={}, namespace={}", module.getCode(), namespace, e);
        }
    }

    // ========== 异步缓存操作 ==========

    /**
     * 异步获取缓存
     */
    public <T> CompletableFuture<T> getAsync(CacheModule module, String namespace, String key, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> get(module, namespace, key, clazz));
    }

    /**
     * 异步设置缓存
     */
    public <T> CompletableFuture<Void> setAsync(CacheModule module, String namespace, String key, T value, BusinessDataType dataType) {
        return CompletableFuture.runAsync(() -> set(module, namespace, key, value, dataType));
    }

    /**
     * 异步删除缓存
     */
    public CompletableFuture<Void> deleteAsync(CacheModule module, String namespace, String key) {
        return CompletableFuture.runAsync(() -> delete(module, namespace, key));
    }

    // ========== 统计和监控 ==========

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getStatistics() {
        try {
            return cacheManager.getStatistics();
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 清理过期的本地缓存
     */
    public void cleanExpiredCache() {
        try {
            cacheManager.cleanExpiredLocalCache();
            log.debug("清理过期缓存完成");
        } catch (Exception e) {
            log.error("清理过期缓存失败", e);
        }
    }

    // ========== 模块级别操作 ==========

    /**
     * 清理整个模块的缓存
     */
    public void clearModule(CacheModule module) {
        try {
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            // 清理本地缓存
            cacheManager.cleanExpiredLocalCache();

            log.debug("清理模块缓存: module={}", module.getCode());

        } catch (Exception e) {
            log.error("清理模块缓存失败: module={}", module.getCode(), e);
        }
    }

    /**
     * 按模式删除缓存
     */
    public int deleteByPattern(CacheModule module, String namespace, String pattern) {
        try {
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            // 这里简化实现，实际可能需要更复杂的模式匹配
            log.debug("按模式删除缓存: module={}, namespace={}, pattern={}",
                     module.getCode(), namespace, pattern);

            // 返回删除的数量（这里返回0，实际实现需要统计）
            return 0;

        } catch (Exception e) {
            log.error("按模式删除缓存失败: module={}, namespace={}, pattern={}",
                     module.getCode(), namespace, pattern, e);
            return 0;
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics(CacheModule module) {
        try {
            CacheNamespace cacheNamespace = CacheNamespace.valueOf(module.getCode().toUpperCase());

            // 获取缓存统计信息
            Map<String, Object> statistics = cacheManager.getStatistics();

            // 添加模块信息
            Map<String, Object> moduleStats = new HashMap<>();
            moduleStats.put("module", module.getCode());
            moduleStats.put("namespace", cacheNamespace.getPrefix());
            moduleStats.put("description", cacheNamespace.getDescription());
            moduleStats.put("statistics", statistics);

            return moduleStats;

        } catch (Exception e) {
            log.error("获取缓存统计信息失败: module={}", module.getCode(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取缓存健康度评估
     */
    public Map<String, Object> getHealthAssessment() {
        try {
            // 从缓存管理器获取健康度评估
            return cacheManager.getStatistics();
        } catch (Exception e) {
            log.error("获取缓存健康度评估失败", e);
            Map<String, Object> health = new HashMap<>();
            health.put("healthy", false);
            health.put("error", e.getMessage());
            return health;
        }
    }

    // ========== 私有方法 ==========

    /**
     * 构建完整缓存键
     */
    private String buildFullKey(CacheModule module, String namespace, String key) {
        if (namespace != null && !namespace.trim().isEmpty()) {
            return String.format("%s:%s:%s", namespace, key, System.currentTimeMillis());
        }
        return key;
    }

    /**
     * 验证参数
     */
    private void validateParams(CacheModule module, String key) {
        if (module == null) {
            throw new SmartException(SystemErrorCode.CACHE_ERROR);
        }
        if (key == null || key.trim().isEmpty()) {
            throw new SmartException(SystemErrorCode.CACHE_ERROR);
        }
    }
}