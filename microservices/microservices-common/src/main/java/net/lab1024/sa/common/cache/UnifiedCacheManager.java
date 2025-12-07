package net.lab1024.sa.common.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 统一缓存管理器
 * <p>
 * 实现多级缓存架构（L1本地缓存 + L2 Redis缓存）
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * - 多级缓存策略
 * - 统一的缓存接口
 * - 完整的缓存管理
 * - 缓存击穿防护（分布式锁）
 * - 缓存命中率监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-01-30 集成缓存指标收集器和分布式锁，实现缓存击穿防护
 */
@Slf4j
@SuppressWarnings({"null", "unchecked"})
public class UnifiedCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final CacheMetricsCollector metricsCollector;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param redisTemplate Redis模板
     * @param redissonClient Redisson客户端（用于分布式锁）
     * @param meterRegistry Micrometer指标注册表（用于缓存监控）
     */
    public UnifiedCacheManager(RedisTemplate<String, Object> redisTemplate, 
                               RedissonClient redissonClient,
                               MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.metricsCollector = meterRegistry != null ? new CacheMetricsCollector(meterRegistry) : null;
        
        if (redissonClient == null) {
            log.warn("[缓存管理器] RedissonClient未配置，缓存击穿防护功能将不可用");
        }
        if (meterRegistry == null) {
            log.warn("[缓存管理器] MeterRegistry未配置，缓存命中率监控功能将不可用");
        }
    }

    /**
     * L1本地缓存（Caffeine）
     * <p>
     * 本地缓存配置：
     * - 最大容量：10000（提升容量以支持更多热点数据）
     * - 过期时间：5分钟（与L2缓存配合，提供快速访问）
     * </p>
     */
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats()  // 启用统计功能
            .build();

    /**
     * 缓存结果封装类
     *
     * @param <T> 数据类型
     */
    @Data
    @AllArgsConstructor
    public static class CacheResult<T> {
        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 数据
         */
        private T data;

        /**
         * 错误消息
         */
        private String errorMessage;

        /**
         * 创建成功结果
         */
        public static <T> CacheResult<T> success(T data) {
            return new CacheResult<>(true, data, null);
        }

        /**
         * 创建失败结果
         */
        public static <T> CacheResult<T> failure(String errorMessage) {
            return new CacheResult<>(false, null, errorMessage);
        }
    }

    /**
     * 获取缓存值
     * <p>
     * 多级缓存策略：
     * 1. 先查询L1本地缓存
     * 2. 如果L1未命中，查询L2 Redis缓存
     * 3. 如果L2命中，将数据写入L1本地缓存
     * </p>
     *
     * @param namespace 缓存命名空间
     * @param key       缓存键
     * @param clazz     数据类型
     * @param <T>       数据类型
     * @return 缓存结果
     */
    public <T> CacheResult<T> get(CacheNamespace namespace, String key, Class<T> clazz) {
        try {
            String fullKey = namespace.buildKey(key);

            long startTime = System.currentTimeMillis();
            
            // 1. 查询L1本地缓存
            Object value = localCache.getIfPresent(fullKey);
            if (value != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                if (metricsCollector != null) {
                    metricsCollector.recordHit("L1", fullKey);
                    metricsCollector.recordResponseTime("L1", responseTime);
                }
                log.debug("[缓存获取] L1本地缓存命中，namespace：{}，key：{}，响应时间：{}ms", 
                        namespace.getPrefix(), key, responseTime);
                return CacheResult.success(clazz.cast(value));
            }

            // 2. 查询L2 Redis缓存
            Object redisValue = redisTemplate.opsForValue().get(fullKey);
            if (redisValue != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                if (metricsCollector != null) {
                    metricsCollector.recordHit("L2", fullKey);
                    metricsCollector.recordResponseTime("L2", responseTime);
                }
                // 将数据写入L1本地缓存
                localCache.put(fullKey, redisValue);
                log.debug("[缓存获取] L2 Redis缓存命中，namespace：{}，key：{}，响应时间：{}ms", 
                        namespace.getPrefix(), key, responseTime);
                return CacheResult.success(clazz.cast(redisValue));
            }

            long responseTime = System.currentTimeMillis() - startTime;
            if (metricsCollector != null) {
                metricsCollector.recordMiss("DB", fullKey);
                metricsCollector.recordResponseTime("DB", responseTime);
            }
            log.debug("[缓存获取] 缓存未命中，namespace：{}，key：{}，响应时间：{}ms", 
                    namespace.getPrefix(), key, responseTime);
            return CacheResult.failure("缓存未命中");
        } catch (Exception e) {
            log.error("[缓存获取] 获取缓存失败，namespace：{}，key：{}", namespace.getPrefix(), key, e);
            return CacheResult.failure("获取缓存失败：" + e.getMessage());
        }
    }

    /**
     * 设置缓存值（使用默认过期时间）
     *
     * @param namespace 缓存命名空间
     * @param key       缓存键
     * @param value     缓存值
     * @param <T>       数据类型
     * @return 缓存结果
     */
    public <T> CacheResult<T> set(CacheNamespace namespace, String key, T value) {
        return set(namespace, key, value, namespace.getDefaultTtl());
    }

    /**
     * 设置缓存值（指定过期时间）
     *
     * @param namespace 缓存命名空间
     * @param key       缓存键
     * @param value     缓存值
     * @param ttl       过期时间（秒）
     * @param <T>       数据类型
     * @return 缓存结果
     */
    public <T> CacheResult<T> set(CacheNamespace namespace, String key, T value, Long ttl) {
        try {
            String fullKey = namespace.buildKey(key);

            // 1. 写入L2 Redis缓存
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(fullKey, value, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(fullKey, value);
            }

            // 2. 写入L1本地缓存
            localCache.put(fullKey, value);

            log.debug("[缓存设置] 设置缓存成功，namespace：{}，key：{}，ttl：{}", namespace.getPrefix(), key, ttl);
            return CacheResult.success(value);
        } catch (Exception e) {
            log.error("[缓存设置] 设置缓存失败，namespace：{}，key：{}", namespace.getPrefix(), key, e);
            return CacheResult.failure("设置缓存失败：" + e.getMessage());
        }
    }

    /**
     * 删除缓存键
     *
     * @param namespace 缓存命名空间
     * @param key       缓存键
     * @return 是否删除成功
     */
    public boolean delete(CacheNamespace namespace, String key) {
        try {
            String fullKey = namespace.buildKey(key);

            // 1. 删除L2 Redis缓存
            Boolean deleted = redisTemplate.delete(fullKey);

            // 2. 删除L1本地缓存
            localCache.invalidate(fullKey);

            log.debug("[缓存删除] 删除缓存成功，namespace：{}，key：{}", namespace.getPrefix(), key);
            return deleted != null && deleted;
        } catch (Exception e) {
            log.error("[缓存删除] 删除缓存失败，namespace：{}，key：{}", namespace.getPrefix(), key, e);
            return false;
        }
    }

    /**
     * 清理命名空间的所有缓存
     *
     * @param namespace 缓存命名空间
     */
    public void clearNamespace(CacheNamespace namespace) {
        try {
            String pattern = namespace.getFullPrefix() + "*";

            // 1. 清理L2 Redis缓存
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            // 2. 清理L1本地缓存（遍历所有本地缓存键）
            localCache.asMap().keySet().removeIf(key -> key.startsWith(namespace.getFullPrefix()));

            log.info("[缓存清理] 清理命名空间缓存成功，namespace：{}，清理键数量：{}",
                    namespace.getPrefix(), keys != null ? keys.size() : 0);
        } catch (Exception e) {
            log.error("[缓存清理] 清理命名空间缓存失败，namespace：{}", namespace.getPrefix(), e);
            throw new RuntimeException("清理命名空间缓存失败：" + e.getMessage(), e);
        }
    }

    /**
     * 缓存预热
     *
     * @param namespace 缓存命名空间
     * @param data      预热数据
     */
    public void warmUp(CacheNamespace namespace, Map<String, Object> data) {
        try {
            log.info("[缓存预热] 开始预热缓存，namespace：{}，数据量：{}", namespace.getPrefix(), data.size());

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                set(namespace, entry.getKey(), entry.getValue());
            }

            log.info("[缓存预热] 缓存预热成功，namespace：{}，数据量：{}", namespace.getPrefix(), data.size());
        } catch (Exception e) {
            log.error("[缓存预热] 缓存预热失败，namespace：{}", namespace.getPrefix(), e);
            throw new RuntimeException("缓存预热失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取缓存值，如果不存在则刷新（使用Duration）
     * <p>
     * 多级缓存策略：
     * 1. 先查询L1本地缓存
     * 2. 如果L1未命中，查询L2 Redis缓存
     * 3. 如果L2也未命中，调用数据加载器加载数据并写入缓存
     * </p>
     *
     * @param key      缓存键（完整键，不包含namespace前缀）
     * @param loader   数据加载器（当缓存未命中时调用）
     * @param duration 过期时间（Duration）
     * @param <T>      数据类型
     * @return 缓存值
     */
    public <T> T getWithRefresh(String key, Supplier<T> loader, Duration duration) {
        Long ttl = duration != null ? duration.getSeconds() : null;
        return getWithRefresh(key, loader, ttl);
    }

    /**
     * 获取缓存值，如果不存在则刷新（使用Long秒数）
     * <p>
     * 多级缓存策略 + 缓存击穿防护：
     * 1. 先查询L1本地缓存
     * 2. 如果L1未命中，查询L2 Redis缓存
     * 3. 如果L2也未命中，使用分布式锁防止缓存击穿，然后调用数据加载器加载数据并写入缓存
     * </p>
     *
     * @param key      缓存键（完整键，不包含namespace前缀）
     * @param loader   数据加载器（当缓存未命中时调用）
     * @param ttl      过期时间（秒）
     * @param <T>      数据类型
     * @return 缓存值
     */
    public <T> T getWithRefresh(String key, Supplier<T> loader, Long ttl) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 查询L1本地缓存
            Object value = localCache.getIfPresent(key);
            if (value != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                if (metricsCollector != null) {
                    metricsCollector.recordHit("L1", key);
                    metricsCollector.recordResponseTime("L1", responseTime);
                }
                log.debug("[缓存获取] L1本地缓存命中，key：{}，响应时间：{}ms", key, responseTime);
                return (T) value;
            }

            // 2. 查询L2 Redis缓存
            Object redisValue = redisTemplate.opsForValue().get(key);
            if (redisValue != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                if (metricsCollector != null) {
                    metricsCollector.recordHit("L2", key);
                    metricsCollector.recordResponseTime("L2", responseTime);
                }
                // 将数据写入L1本地缓存
                localCache.put(key, redisValue);
                log.debug("[缓存获取] L2 Redis缓存命中，key：{}，响应时间：{}ms", key, responseTime);
                return (T) redisValue;
            }

            // 3. 缓存未命中，使用分布式锁防止缓存击穿
            if (redissonClient == null) {
                // RedissonClient未配置，直接加载数据（无击穿防护）
                log.debug("[缓存获取] RedissonClient未配置，直接加载数据，key：{}", key);
                T loadedValue = loader.get();
                if (loadedValue != null) {
                    if (ttl != null && ttl > 0) {
                        redisTemplate.opsForValue().set(key, loadedValue, ttl, TimeUnit.SECONDS);
                    } else {
                        redisTemplate.opsForValue().set(key, loadedValue);
                    }
                    localCache.put(key, loadedValue);
                }
                if (metricsCollector != null) {
                    metricsCollector.recordMiss("DB", key);
                }
                return loadedValue;
            }
            
            String lockKey = "lock:cache:" + key;
            RLock lock = redissonClient.getLock(lockKey);
            
            try {
                // 尝试获取锁，最多等待5秒，锁10秒后自动释放
                if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                    try {
                        // 双重检查：再次查询L2 Redis缓存（可能其他线程已经加载了）
                        redisValue = redisTemplate.opsForValue().get(key);
                        if (redisValue != null) {
                            long responseTime = System.currentTimeMillis() - startTime;
                            if (metricsCollector != null) {
                                metricsCollector.recordHit("L2", key);
                                metricsCollector.recordResponseTime("L2", responseTime);
                            }
                            localCache.put(key, redisValue);
                            log.debug("[缓存获取] 双重检查L2 Redis缓存命中，key：{}，响应时间：{}ms", key, responseTime);
                            return (T) redisValue;
                        }
                        
                        // 从数据源加载
                        log.debug("[缓存获取] 缓存未命中，从数据源加载，key：{}", key);
                        T loadedValue = loader.get();

                        if (loadedValue != null) {
                            // 写入L2 Redis缓存
                            if (ttl != null && ttl > 0) {
                                redisTemplate.opsForValue().set(key, loadedValue, ttl, TimeUnit.SECONDS);
                            } else {
                                redisTemplate.opsForValue().set(key, loadedValue);
                            }

                            // 写入L1本地缓存
                            localCache.put(key, loadedValue);
                            
                            long responseTime = System.currentTimeMillis() - startTime;
                            if (metricsCollector != null) {
                                metricsCollector.recordMiss("DB", key);
                                metricsCollector.recordResponseTime("DB", responseTime);
                            }
                            log.debug("[缓存设置] 数据加载并写入缓存成功，key：{}，ttl：{}，响应时间：{}ms", 
                                    key, ttl, responseTime);
                        } else {
                            // 空值缓存（防止缓存穿透），设置较短的过期时间
                            redisTemplate.opsForValue().set(key, "NULL", 300, TimeUnit.SECONDS);
                            log.debug("[缓存设置] 空值缓存，key：{}", key);
                        }

                        return loadedValue;
                    } finally {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                } else {
                    // 获取锁失败，短暂等待后重试
                    log.debug("[缓存获取] 获取锁失败，等待后重试，key：{}", key);
                    Thread.sleep(100);
                    return getWithRefresh(key, loader, ttl);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[缓存获取] 获取锁被中断，key：{}", key, e);
                // 降级：直接调用数据加载器
                return loader.get();
            }
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            if (metricsCollector != null) {
                metricsCollector.recordMiss("ERROR", key);
                metricsCollector.recordResponseTime("ERROR", responseTime);
            }
            log.error("[缓存获取] 获取缓存失败，key：{}，响应时间：{}ms", key, responseTime, e);
            // 降级：直接调用数据加载器
            return loader.get();
        }
    }

    /**
     * 删除缓存键（简化方法，直接使用完整键）
     * <p>
     * 用于删除指定键的缓存，同时清除L1和L2缓存
     * </p>
     *
     * @param key 缓存键（完整键）
     * @return 是否删除成功
     */
    public boolean evict(String key) {
        try {
            // 1. 删除L2 Redis缓存
            Boolean deleted = redisTemplate.delete(key);

            // 2. 删除L1本地缓存
            localCache.invalidate(key);

            log.debug("[缓存删除] 删除缓存成功，key：{}", key);
            return deleted != null && deleted;
        } catch (Exception e) {
            log.error("[缓存删除] 删除缓存失败，key：{}", key, e);
            return false;
        }
    }

    /**
     * 获取缓存命中率
     *
     * @param cacheType 缓存类型（L1、L2、DB）
     * @return 命中率（0-100），如果指标收集器未配置则返回0
     */
    public double getHitRate(String cacheType) {
        return metricsCollector != null ? metricsCollector.getHitRate(cacheType) : 0.0;
    }

    /**
     * 获取总体缓存命中率
     *
     * @return 总体命中率（0-100），如果指标收集器未配置则返回0
     */
    public double getOverallHitRate() {
        return metricsCollector != null ? metricsCollector.getOverallHitRate() : 0.0;
    }

    /**
     * 获取缓存统计信息
     *
     * @param cacheType 缓存类型
     * @return 统计信息，如果指标收集器未配置则返回null
     */
    public CacheMetricsCollector.CacheStatsInfo getStats(String cacheType) {
        return metricsCollector != null ? metricsCollector.getStats(cacheType) : null;
    }

    /**
     * 获取缓存指标收集器（用于高级监控）
     *
     * @return 缓存指标收集器，如果未配置则返回null
     */
    public CacheMetricsCollector getMetricsCollector() {
        return metricsCollector;
    }
}
