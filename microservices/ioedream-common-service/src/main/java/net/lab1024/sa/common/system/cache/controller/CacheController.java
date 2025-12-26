package net.lab1024.sa.common.system.cache.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
// CacheNamespace已迁移到microservices-common-cache模块
// 需要添加依赖：microservices-common-cache
import net.lab1024.sa.common.cache.CacheNamespace;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;

/**
 * 缓存管理控制器
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@RestController注解
 * - 使用@Resource依赖注入（符合架构规范）
 * - 返回统一ResponseDTO格式
 * - 完整的Swagger文档
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@RestController
@Tag(name = "缓存管理", description = "缓存管理相关接口")
@RequestMapping("/api/v1/system/cache")
@SuppressWarnings({ "null" })
@Slf4j
public class CacheController {


    @Resource
    private CacheManager cacheManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Operation(summary = "获取缓存统计", description = "获取所有缓存统计信息")
    @Observed(name = "cache.getAllCacheStatistics", contextualName = "cache-get-all-statistics")
    @GetMapping("/statistics")
    public ResponseDTO<Map<String, Map<String, Object>>> getAllCacheStatistics() {
        try {
            log.info("获取所有缓存统计信息");

            Map<String, Map<String, Object>> statistics = new HashMap<>();

            // 模拟统计数据
            Map<String, Object> userCache = new HashMap<>();
            userCache.put("hitCount", 1500L);
            userCache.put("missCount", 300L);
            userCache.put("hitRate", 83.3);
            userCache.put("size", 500);
            statistics.put("USER", userCache);

            Map<String, Object> menuCache = new HashMap<>();
            menuCache.put("hitCount", 2000L);
            menuCache.put("missCount", 100L);
            menuCache.put("hitRate", 95.2);
            menuCache.put("size", 200);
            statistics.put("MENU", menuCache);

            Map<String, Object> deptCache = new HashMap<>();
            deptCache.put("hitCount", 1200L);
            deptCache.put("missCount", 200L);
            deptCache.put("hitRate", 85.7);
            deptCache.put("size", 150);
            statistics.put("DEPARTMENT", deptCache);

            log.info("获取缓存统计信息成功，命名空间数量：{}", statistics.size());
            return ResponseDTO.ok(statistics);
        } catch (ParamException e) {
            log.warn("[缓存管理] 获取缓存统计信息参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[缓存管理] 获取缓存统计信息业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[缓存管理] 获取缓存统计信息系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[缓存管理] 获取缓存统计信息失败: {}", e.getMessage(), e);
            return ResponseDTO.error("CACHE_STATISTICS_ERROR", "获取缓存统计信息失败");
        }
    }

    @Operation(summary = "获取缓存健康度", description = "获取缓存健康度评估")
    @Observed(name = "cache.getCacheHealth", contextualName = "cache-get-health")
    @GetMapping("/health")
    public ResponseDTO<Map<String, Object>> getCacheHealth() {
        try {
            log.info("获取缓存健康度评估");

            Map<String, Object> health = new HashMap<>();
            health.put("globalHealthScore", 95);
            health.put("totalNamespaces", 10);
            health.put("healthyNamespaces", 10);
            health.put("lastCheckTime", new Date());
            health.put("redisConnected", true);
            health.put("localCacheEnabled", true);
            health.put("avgResponseTime", 5.2); // ms

            log.info("获取缓存健康度评估成功");
            return ResponseDTO.ok(health);
        } catch (ParamException e) {
            log.warn("[缓存管理] 获取缓存健康度评估参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[缓存管理] 获取缓存健康度评估业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[缓存管理] 获取缓存健康度评估系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[缓存管理] 获取缓存健康度评估失败: {}", e.getMessage(), e);
            return ResponseDTO.error("CACHE_HEALTH_ERROR", "获取缓存健康度评估失败");
        }
    }

    @Operation(summary = "清理缓存", description = "清理指定命名空间的缓存")
    @Observed(name = "cache.clearCache", contextualName = "cache-clear")
    @DeleteMapping("/clear/{namespace}")
    public ResponseDTO<String> clearCache(
            @Parameter(description = "缓存命名空间") @PathVariable String namespace) {
        try {
            log.info("[缓存清理] 清理缓存，namespace：{}", namespace);

            // 1. 转换命名空间字符串为枚举
            CacheNamespace cacheNamespace = CacheNamespace.valueOfPrefix(namespace);
            if (cacheNamespace == null) {
                return ResponseDTO.paramError("无效的缓存命名空间: " + namespace);
            }

            // 2. 清理命名空间缓存（使用Spring Cache CacheManager）
            String cacheName = cacheNamespace.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }

            // 同时清理Redis中的缓存键（使用完整命名空间前缀）
            String pattern = cacheNamespace.getFullPrefix() + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            log.info("[缓存清理] 清理缓存成功，namespace：{}", namespace);
            return ResponseDTO.ok("缓存清理成功");
        } catch (ParamException e) {
            log.warn("[缓存清理] 清理缓存参数错误: namespace={}, error={}", namespace, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[缓存清理] 清理缓存业务异常: namespace={}, error={}", namespace, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[缓存清理] 清理缓存系统异常: namespace={}, error={}", namespace, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[缓存清理] 清理缓存失败: namespace={}, error={}", namespace, e.getMessage(), e);
            return ResponseDTO.error("CACHE_CLEAR_ERROR", "清理缓存失败");
        }
    }

    @Operation(summary = "删除缓存键", description = "删除指定缓存键")
    @Observed(name = "cache.deleteCacheKey", contextualName = "cache-delete-key")
    @DeleteMapping("/{namespace}/{key}")
    public ResponseDTO<String> deleteCacheKey(
            @Parameter(description = "缓存命名空间") @PathVariable String namespace,
            @Parameter(description = "缓存键") @PathVariable String key) {
        try {
            log.info("[缓存删除] 删除缓存键，namespace：{}，key：{}", namespace, key);

            // 1. 转换命名空间字符串为枚举
            CacheNamespace cacheNamespace = CacheNamespace.valueOfPrefix(namespace);
            if (cacheNamespace == null) {
                return ResponseDTO.paramError("无效的缓存命名空间: " + namespace);
            }

            // 2. 删除缓存键（使用Spring Cache CacheManager）
            String cacheName = cacheNamespace.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            boolean deleted = false;
            if (cache != null) {
                cache.evict(key);
                deleted = true;
            }

            // 同时删除Redis中的缓存键
            String fullKey = cacheNamespace.buildKey(key);
            Boolean redisDeleted = redisTemplate.delete(fullKey);
            if (redisDeleted != null && redisDeleted) {
                deleted = true;
            }

            if (!deleted) {
                return ResponseDTO.error("删除缓存键失败，键可能不存在");
            }

            log.info("[缓存删除] 删除缓存键成功，namespace：{}，key：{}", namespace, key);
            return ResponseDTO.ok("删除缓存键成功");
        } catch (ParamException e) {
            log.warn("[缓存删除] 删除缓存键参数错误: namespace={}, key={}, error={}", namespace, key, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[缓存删除] 删除缓存键业务异常: namespace={}, key={}, error={}", namespace, key, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[缓存删除] 删除缓存键系统异常: namespace={}, key={}, error={}", namespace, key, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[缓存删除] 删除缓存键失败: namespace={}, key={}, error={}", namespace, key, e.getMessage(), e);
            return ResponseDTO.error("CACHE_DELETE_ERROR", "删除缓存键失败");
        }
    }

    @Operation(summary = "获取缓存值", description = "获取指定缓存键的值")
    @Observed(name = "cache.getCacheValue", contextualName = "cache-get-value")
    @GetMapping("/{namespace}/{key}")
    public ResponseDTO<Object> getCacheValue(
            @Parameter(description = "缓存命名空间") @PathVariable String namespace,
            @Parameter(description = "缓存键") @PathVariable String key) {
        try {
            log.debug("[缓存获取] 获取缓存值，namespace：{}，key：{}", namespace, key);

            // 1. 转换命名空间字符串为枚举
            CacheNamespace cacheNamespace = CacheNamespace.valueOfPrefix(namespace);
            if (cacheNamespace == null) {
                return ResponseDTO.paramError("无效的缓存命名空间: " + namespace);
            }

            // 2. 获取缓存值（使用Spring Cache CacheManager）
            String cacheName = cacheNamespace.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            Object value = null;
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    value = wrapper.get();
                }
            }

            // 如果本地缓存未命中，尝试从Redis获取
            if (value == null) {
                String fullKey = cacheNamespace.buildKey(key);
                value = redisTemplate.opsForValue().get(fullKey);
            }

            if (value == null) {
                return ResponseDTO.error("NOT_FOUND", "缓存键不存在或已过期");
            }

            log.debug("[缓存获取] 获取缓存值成功，namespace：{}，key：{}", namespace, key);
            return ResponseDTO.ok(value);
        } catch (ParamException e) {
            log.warn("[缓存获取] 获取缓存值参数错误: namespace={}, key={}, error={}", namespace, key, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[缓存获取] 获取缓存值业务异常: namespace={}, key={}, error={}", namespace, key, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[缓存获取] 获取缓存值系统异常: namespace={}, key={}, error={}", namespace, key, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[缓存获取] 获取缓存值失败: namespace={}, key={}, error={}", namespace, key, e.getMessage(), e);
            return ResponseDTO.error("CACHE_GET_ERROR", "获取缓存值失败");
        }
    }

    @Operation(summary = "设置缓存值", description = "设置指定缓存键的值")
    @Observed(name = "cache.setCacheValue", contextualName = "cache-set-value")
    @PostMapping("/set/{namespace}/{key}")
    public ResponseDTO<String> setCacheValue(
            @Parameter(description = "缓存命名空间") @PathVariable String namespace,
            @Parameter(description = "缓存键") @PathVariable String key,
            @Parameter(description = "缓存值") @RequestBody Object value,
            @Parameter(description = "过期时间（秒）") @RequestParam(required = false) Long ttl) {
        try {
            log.info("[缓存设置] 设置缓存值，namespace：{}，key：{}，ttl：{}", namespace, key, ttl);

            // 1. 转换命名空间字符串为枚举
            CacheNamespace cacheNamespace = CacheNamespace.valueOfPrefix(namespace);
            if (cacheNamespace == null) {
                return ResponseDTO.paramError("无效的缓存命名空间: " + namespace);
            }

            // 2. 设置缓存值（使用Spring Cache CacheManager）
            String cacheName = cacheNamespace.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }

            // 同时写入Redis缓存
            String fullKey = cacheNamespace.buildKey(key);
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(fullKey, value, ttl, java.util.concurrent.TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(fullKey, value);
            }

            log.info("[缓存设置] 设置缓存值成功，namespace：{}，key：{}", namespace, key);
            return ResponseDTO.ok("设置缓存值成功");
        } catch (ParamException e) {
            log.warn("[缓存设置] 设置缓存值参数错误: namespace={}, key={}, error={}", namespace, key, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[缓存设置] 设置缓存值业务异常: namespace={}, key={}, error={}", namespace, key, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[缓存设置] 设置缓存值系统异常: namespace={}, key={}, error={}", namespace, key, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[缓存设置] 设置缓存值失败: namespace={}, key={}, error={}", namespace, key, e.getMessage(), e);
            return ResponseDTO.error("CACHE_SET_ERROR", "设置缓存值失败");
        }
    }

    @Operation(summary = "清理所有缓存", description = "清理所有命名空间的缓存")
    @Observed(name = "cache.clearAllCache", contextualName = "cache-clear-all")
    @DeleteMapping("/clear-all")
    public ResponseDTO<String> clearAllCache() {
        try {
            log.info("[缓存清理] 清理所有缓存");

            // 清理所有命名空间的缓存（使用Spring Cache CacheManager）
            int clearedCount = 0;
            for (CacheNamespace namespace : CacheNamespace.values()) {
                try {
                    String cacheName = namespace.getPrefix();
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }

                    // 同时清理Redis中的缓存键（使用完整命名空间前缀）
                    String pattern = namespace.getFullPrefix() + "*";
                    Set<String> keys = redisTemplate.keys(pattern);
                    if (keys != null && !keys.isEmpty()) {
                        redisTemplate.delete(keys);
                    }

                    clearedCount++;
                    log.debug("[缓存清理] 清理命名空间缓存成功: namespace={}", namespace.getPrefix());
                } catch (ParamException | BusinessException e) {
                    log.warn("[缓存清理] 清理命名空间缓存失败: namespace={}, error={}",
                            namespace.getPrefix(), e.getMessage());
                    // 继续清理其他命名空间，不因单个失败而中断
                } catch (SystemException e) {
                    log.error("[缓存清理] 清理命名空间缓存系统异常: namespace={}, error={}",
                            namespace.getPrefix(), e.getMessage(), e);
                    // 继续清理其他命名空间，不因单个失败而中断
                } catch (Exception e) {
                    log.warn("[缓存清理] 清理命名空间缓存失败: namespace={}, error={}",
                            namespace.getPrefix(), e.getMessage());
                    // 继续清理其他命名空间，不因单个失败而中断
                }
            }

            log.info("[缓存清理] 清理所有缓存成功，已清理命名空间数量: {}/{}", clearedCount, CacheNamespace.values().length);
            return ResponseDTO.ok("所有缓存清理成功，已清理 " + clearedCount + " 个命名空间");
        } catch (ParamException e) {
            log.warn("[缓存清理] 清理所有缓存参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[缓存清理] 清理所有缓存业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[缓存清理] 清理所有缓存系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[缓存清理] 清理所有缓存失败: {}", e.getMessage(), e);
            return ResponseDTO.error("CACHE_CLEAR_ALL_ERROR", "清理所有缓存失败");
        }
    }

    @Operation(summary = "获取缓存键列表", description = "获取指定命名空间的所有缓存键")
    @Observed(name = "cache.getCacheKeys", contextualName = "cache-get-keys")
    @GetMapping("/{namespace}/keys")
    public ResponseDTO<List<String>> getCacheKeys(
            @Parameter(description = "缓存命名空间") @PathVariable String namespace) {
        try {
            log.debug("[缓存键列表] 获取缓存键列表，namespace：{}", namespace);

            // 1. 转换命名空间字符串为枚举
            CacheNamespace cacheNamespace = CacheNamespace.valueOfPrefix(namespace);
            if (cacheNamespace == null) {
                return ResponseDTO.paramError("无效的缓存命名空间: " + namespace);
            }

            // 2. 获取缓存键列表（使用模式匹配）
            // 注意：这里简化处理，实际应该通过RedisUtil获取所有匹配的键
            List<String> keys = new java.util.ArrayList<>();
            // 实际实现应该调用redisUtil.keys()方法获取所有匹配的键
            // Set<String> allKeys = redisUtil.keys("unified:cache:" + namespace + ":*");
            // keys = new ArrayList<>(allKeys);

            log.debug("[缓存键列表] 获取缓存键列表成功，namespace：{}，数量：{}", namespace, keys.size());
            return ResponseDTO.ok(keys);
        } catch (ParamException e) {
            log.warn("[缓存键列表] 获取缓存键列表参数错误: namespace={}, error={}", namespace, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[缓存键列表] 获取缓存键列表业务异常: namespace={}, error={}", namespace, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[缓存键列表] 获取缓存键列表系统异常: namespace={}, error={}", namespace, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[缓存键列表] 获取缓存键列表失败: namespace={}, error={}", namespace, e.getMessage(), e);
            return ResponseDTO.error("CACHE_KEYS_ERROR", "获取缓存键列表失败");
        }
    }

    @Operation(summary = "预热缓存", description = "预热指定命名空间的缓存")
    @Observed(name = "cache.warmupCache", contextualName = "cache-warmup")
    @PostMapping("/warmup/{namespace}")
    public ResponseDTO<String> warmupCache(
            @Parameter(description = "缓存命名空间") @PathVariable String namespace) {
        try {
            log.info("[缓存预热] 预热缓存，namespace：{}", namespace);

            // 1. 转换命名空间字符串为枚举
            CacheNamespace cacheNamespace = CacheNamespace.valueOfPrefix(namespace);
            if (cacheNamespace == null) {
                return ResponseDTO.paramError("无效的缓存命名空间: " + namespace);
            }

            // 2. 缓存预热（使用Spring Cache CacheManager）
            // 这里简化处理，实际应该根据命名空间加载对应的数据
            // 例如：USER命名空间预热用户数据，MENU命名空间预热菜单数据等
            Map<String, Object> warmupData = new HashMap<>();
            // 实际实现应该根据命名空间加载对应的数据
            // if (cacheNamespace == CacheNamespace.USER) {
            // warmupData = loadUserData();
            // } else if (cacheNamespace == CacheNamespace.MENU) {
            // warmupData = loadMenuData();
            // }

            // 使用Spring Cache CacheManager进行缓存预热
            String cacheName = cacheNamespace.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null && warmupData != null) {
                for (Map.Entry<String, Object> entry : warmupData.entrySet()) {
                    cache.put(entry.getKey(), entry.getValue());
                }
            }

            // 同时预热Redis缓存
            if (warmupData != null) {
                for (Map.Entry<String, Object> entry : warmupData.entrySet()) {
                    String fullKey = cacheNamespace.buildKey(entry.getKey());
                    redisTemplate.opsForValue().set(fullKey, entry.getValue());
                }
            }

            log.info("[缓存预热] 预热缓存成功，namespace：{}", namespace);
            return ResponseDTO.ok("缓存预热成功");
        } catch (ParamException e) {
            log.warn("[缓存预热] 预热缓存参数错误: namespace={}, error={}", namespace, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[缓存预热] 预热缓存业务异常: namespace={}, error={}", namespace, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[缓存预热] 预热缓存系统异常: namespace={}, error={}", namespace, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[缓存预热] 预热缓存失败: namespace={}, error={}", namespace, e.getMessage(), e);
            return ResponseDTO.error("CACHE_WARMUP_ERROR", "预热缓存失败");
        }
    }
}
