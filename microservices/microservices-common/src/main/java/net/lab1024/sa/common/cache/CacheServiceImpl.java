package net.lab1024.sa.common.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 缓存服务实现类
 * <p>
 * 实现CacheService接口，封装UnifiedCacheManager和RedisTemplate
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在microservices-common中
 * - 使用@Resource注入依赖
 * - 提供统一的缓存操作接口
 * </p>
 * <p>
 * 注意：此实现类需要在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@SuppressWarnings("null")
public class CacheServiceImpl implements CacheService {

    private final UnifiedCacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：通过构造函数接收依赖
     * </p>
     *
     * @param cacheManager 统一缓存管理器
     * @param redisTemplate Redis模板
     */
    public CacheServiceImpl(UnifiedCacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
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
    public <T> T get(String key, Class<T> clazz) {
        try {
            // 使用默认命名空间
            UnifiedCacheManager.CacheResult<T> result = cacheManager.get(
                    CacheNamespace.DEFAULT, key, clazz);
            if (result != null && result.isSuccess()) {
                return result.getData();
            }
            return null;
        } catch (Exception e) {
            log.error("[缓存服务] 获取缓存失败，key：{}", key, e);
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
    public void set(String key, Object value) {
        try {
            cacheManager.set(CacheNamespace.DEFAULT, key, value);
        } catch (Exception e) {
            log.error("[缓存服务] 设置缓存失败，key：{}", key, e);
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
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            // 转换为秒
            long ttlSeconds = TimeUnit.SECONDS.convert(timeout, unit);
            cacheManager.set(CacheNamespace.DEFAULT, key, value, ttlSeconds);
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
    public Boolean delete(String key) {
        try {
            return cacheManager.delete(CacheNamespace.DEFAULT, key);
        } catch (Exception e) {
            log.error("[缓存服务] 删除缓存失败，key：{}", key, e);
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
    public Boolean hasKey(String key) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.hasKey(fullKey);
        } catch (Exception e) {
            log.error("[缓存服务] 判断缓存是否存在失败，key：{}", key, e);
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
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.expire(fullKey, timeout, unit);
        } catch (Exception e) {
            log.error("[缓存服务] 设置过期时间失败，key：{}", key, e);
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
    public Long increment(String key) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.opsForValue().increment(fullKey);
        } catch (Exception e) {
            log.error("[缓存服务] 递增失败，key：{}", key, e);
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
    public Long decrement(String key) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.opsForValue().decrement(fullKey);
        } catch (Exception e) {
            log.error("[缓存服务] 递减失败，key：{}", key, e);
            return null;
        }
    }
}
