package net.lab1024.sa.common.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存服务实现类
 * <p>
 * 实现CacheService接口，使用Spring Cache CacheManager和RedisTemplate
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在microservices-common中
 * - 使用@Resource注入依赖
 * - 提供统一的缓存操作接口
 * - 已迁移到Spring Cache标准方案
 * </p>
 * <p>
 * 注意：此实现类需要在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-01-30 迁移到Spring Cache CacheManager，移除UnifiedCacheManager依赖
 */
@Slf4j
@SuppressWarnings("null")
public class CacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：通过构造函数接收依赖
     * </p>
     *
     * @param cacheManager Spring Cache缓存管理器
     * @param redisTemplate Redis模板
     */
    public CacheServiceImpl(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @param clazz 数据类型
     * @param <T> 数据类型
     * @return 缓存值，如果不存在返回null
     */
    @Override
    @Observed(name = "cache.get", contextualName = "cache-get")
    public <T> T get(String key, Class<T> clazz) {
        try {
            // 使用默认命名空间
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    Object value = wrapper.get();
                    if (value != null && clazz.isInstance(value)) {
                        return clazz.cast(value);
                    }
                }
            }

            // 如果本地缓存未命中，尝试从Redis获取
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            Object value = redisTemplate.opsForValue().get(fullKey);
            if (value != null && clazz.isInstance(value)) {
                return clazz.cast(value);
            }

            return null;
        } catch (Exception e) {
            log.error("[缓存服务] 获取缓存系统异常，key：{}", key, e);
            return null;
        }
    }

    /**
     * 设置缓存值（使用默认过期时间）
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    @Override
    @Observed(name = "cache.set", contextualName = "cache-set")
    public void set(String key, Object value) {
        try {
            // 使用Spring Cache CacheManager设置缓存
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }

            // 同时写入Redis缓存
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            redisTemplate.opsForValue().set(fullKey, value);
        } catch (Exception e) {
            log.error("[缓存服务] 设置缓存系统异常，key：{}", key, e);
        }
    }

    /**
     * 设置缓存值（指定过期时间）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    @Override
    @Observed(name = "cache.setWithTimeout", contextualName = "cache-set-with-timeout")
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            // 使用Spring Cache CacheManager设置缓存
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }

            // 同时写入Redis缓存（指定过期时间）
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            redisTemplate.opsForValue().set(fullKey, value, timeout, unit);
        } catch (Exception e) {
            log.error("[缓存服务] 设置缓存失败，key：{}", key, e);
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 是否成功
     */
    @Override
    @Observed(name = "cache.delete", contextualName = "cache-delete")
    public Boolean delete(String key) {
        try {
            // 使用Spring Cache CacheManager删除缓存
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            boolean deleted = false;
            if (cache != null) {
                cache.evict(key);
                deleted = true;
            }

            // 同时删除Redis缓存
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            Boolean redisDeleted = redisTemplate.delete(fullKey);
            if (redisDeleted != null && redisDeleted) {
                deleted = true;
            }

            return deleted;
        } catch (Exception e) {
            log.error("[缓存服务] 删除缓存系统异常，key：{}", key, e);
            return false;
        }
    }

    /**
     * 判断缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    @Override
    @Observed(name = "cache.hasKey", contextualName = "cache-has-key")
    public Boolean hasKey(String key) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.hasKey(fullKey);
        } catch (Exception e) {
            log.error("[缓存服务] 判断缓存是否存在系统异常，key：{}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     *
     * @param key 缓存键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    @Override
    @Observed(name = "cache.expire", contextualName = "cache-expire")
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.expire(fullKey, timeout, unit);
        } catch (Exception e) {
            log.error("[缓存服务] 设置过期时间系统异常，key：{}", key, e);
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key 缓存键
     * @return 递增后的值
     */
    @Override
    @Observed(name = "cache.increment", contextualName = "cache-increment")
    public Long increment(String key) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.opsForValue().increment(fullKey);
        } catch (Exception e) {
            log.error("[缓存服务] 递增系统异常，key：{}", key, e);
            return null;
        }
    }

    /**
     * 递增（指定增量）
     *
     * @param key 缓存键
     * @param delta 增量
     * @return 递增后的值
     */
    @Override
    @Observed(name = "cache.incrementByDelta", contextualName = "cache-increment-by-delta")
    public Long increment(String key, long delta) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.opsForValue().increment(fullKey, delta);
        } catch (Exception e) {
            log.error("[缓存服务] 递增失败，key：{}", key, e);
            return null;
        }
    }

    /**
     * 递减
     *
     * @param key 缓存键
     * @return 递减后的值
     */
    @Override
    @Observed(name = "cache.decrement", contextualName = "cache-decrement")
    public Long decrement(String key) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.opsForValue().decrement(fullKey);
        } catch (Exception e) {
            log.error("[缓存服务] 递减系统异常，key：{}", key, e);
            return null;
        }
    }
}
