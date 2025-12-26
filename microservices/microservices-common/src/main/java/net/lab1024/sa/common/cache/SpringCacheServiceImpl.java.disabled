package net.lab1024.sa.common.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("null")
public class SpringCacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    public SpringCacheServiceImpl(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Observed(name = "cache.get", contextualName = "cache-get")
    public <T> T get(String key, Class<T> clazz) {
        try {
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

    @Override
    @Observed(name = "cache.set", contextualName = "cache-set")
    public void set(String key, Object value) {
        try {
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }

            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            redisTemplate.opsForValue().set(fullKey, value);
        } catch (Exception e) {
            log.error("[缓存服务] 设置缓存系统异常，key：{}", key, e);
        }
    }

    @Override
    @Observed(name = "cache.setWithTimeout", contextualName = "cache-set-with-timeout")
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }

            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            redisTemplate.opsForValue().set(fullKey, value, timeout, unit);
        } catch (Exception e) {
            log.error("[缓存服务] 设置缓存失败，key：{}", key, e);
        }
    }

    @Override
    @Observed(name = "cache.delete", contextualName = "cache-delete")
    public Boolean delete(String key) {
        try {
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            boolean deleted = false;
            if (cache != null) {
                cache.evict(key);
                deleted = true;
            }

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

    @Override
    @Observed(name = "cache.decrement", contextualName = "cache-decrement")
    public Long decrement(String key) {
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            return redisTemplate.opsForValue().decrement(fullKey);
        } catch (Exception e) {
            log.error("[缓存服务] 递减异常，key：{}", key, e);
            return null;
        }
    }
}
