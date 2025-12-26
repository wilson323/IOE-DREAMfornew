package net.lab1024.sa.common.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import io.micrometer.observation.annotation.Observed;

/**
 * 缓存服务实现类
 * <p>
 * 实现CacheService接口，使用Spring Cache CacheManager和RedisTemplate
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
@SuppressWarnings("null")
@Slf4j
public class CacheServiceImpl implements CacheService {


    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    public CacheServiceImpl(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }
 
    @Override
    @Observed(name = "cache.get", contextualName = "cache-get") 
    public <T> T get(String key, Class<T> valueClass) {
        try {
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) { 
                Cache.ValueWrapper wrapper = cache.get(key); 
                if (wrapper != null) { 
                    Object value = wrapper.get();
                    if (value != null && valueClass.isInstance(value)) {
                        return valueClass.cast(value);
                    }
                } 
            }  

            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            Object value = redisTemplate.opsForValue().get(fullKey);
            if (value != null && valueClass.isInstance(value)) {
                return valueClass.cast(value);
            }
 
            return null;
        } catch (Exception e) {
            log.error("[缓存服务] 获取缓存系统异常，key：{}", key, e);
            return null;
        }
    } 
 
    @Override
    public <T> T get(String key, Class<T> valueClass, T defaultValue) {
        T value = get(key, valueClass);
        return value != null ? value : defaultValue;
    }
 
    @Override
    @Observed(name = "cache.put", contextualName = "cache-put") 
    public void put(String key, Object value) { 
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
    @Observed(name = "cache.putWithTimeout", contextualName = "cache-put-with-timeout")
    public void put(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value); 
            }  

            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            redisTemplate.opsForValue().set(fullKey, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("[缓存服务] 设置缓存失败，key：{}", key, e);
        }
    }
 
    @Override
    @Observed(name = "cache.evict", contextualName = "cache-evict")
    public boolean evict(String key) { 
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
    @Observed(name = "cache.exists", contextualName = "cache-exists")
    public boolean exists(String key) { 
        try {
            String fullKey = CacheNamespace.DEFAULT.buildKey(key);
            Boolean result = redisTemplate.hasKey(fullKey);
            return result != null && result;
        } catch (Exception e) {
            log.error("[缓存服务] 判断缓存是否存在系统异常，key：{}", key, e);
            return false;
        }
    }
 
    @Override
    @Observed(name = "cache.clear", contextualName = "cache-clear")
    public void clear() { 
        try {
            String cacheName = CacheNamespace.DEFAULT.getPrefix();
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        } catch (Exception e) {
            log.error("[缓存服务] 清空缓存失败", e);
        }
    }
 
    @Override
    @Observed(name = "cache.size", contextualName = "cache-size")
    public long size() {
        try {
            if (redisTemplate.getConnectionFactory() == null) {  
                return 0L;
            } 
            return redisTemplate.getConnectionFactory().getConnection().dbSize();
        } catch (Exception e) {
            log.error("[缓存服务] 获取缓存大小系统异常", e);
            return 0L;
        }
    }
}

