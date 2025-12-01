/*
 * Smart Redis工具类 - 简化版本
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-19
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.base.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Smart Redis工具类 - 简化版本
 * 提供Redis核心操作的便捷方法
 *
 * @author SmartAdmin Team
 * @date 2025/01/19
 */
public class SmartRedisUtil {

    @Resource
    private static RedisTemplate<String, Object> redisTemplate;

    @Resource
    private static StringRedisTemplate stringRedisTemplate;

    private static final long DEFAULT_EXPIRE_TIME = 3600L; // 默认过期时间1小时

    // ==================== String操作 ====================

    public static void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public static void set(String key, String value, long timeout, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    public static String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public static Boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    public static Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    public static Boolean exists(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public static Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public static Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, timeUnit));
    }

    public static Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    // ==================== Object操作 ====================

    public static void setObj(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public static void setObj(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObj(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    // ==================== Hash操作 ====================

    public static void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T hGet(String key, String field) {
        return (T) redisTemplate.opsForHash().get(key, field);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> hGetAll(String key) {
        Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);
        Map<String, Object> result = new java.util.HashMap<>();
        if (rawMap != null) {
            for (Map.Entry<Object, Object> entry : rawMap.entrySet()) {
                if (entry.getKey() instanceof String) {
                    result.put((String) entry.getKey(), entry.getValue());
                } else {
                    result.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        }
        return result;
    }

    public static void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public static Boolean hHas(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    public static Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    public static Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    // ==================== List操作 ====================

    public static Long lPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public static Long lPush(String key, Collection<Object> values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> lRange(String key, long start, long end) {
        return (List<T>) redisTemplate.opsForList().range(key, start, end);
    }

    @SuppressWarnings("unchecked")
    public static <T> T lIndex(String key, long index) {
        return (T) redisTemplate.opsForList().index(key, index);
    }

    public static Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public static void lSet(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    public static void lTrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    @SuppressWarnings("unchecked")
    public static <T> T lPop(String key) {
        return (T) redisTemplate.opsForList().leftPop(key);
    }

    public static void lClear(String key) {
        redisTemplate.delete(key);
    }

    // ==================== Set操作 ====================

    public static Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public static Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> sMembers(String key) {
        return (Set<T>) redisTemplate.opsForSet().members(key);
    }

    public static Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public static Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public static void sClear(String key) {
        redisTemplate.delete(key);
    }

    // ==================== ZSet操作 ====================

    public static Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> zRange(String key, long start, long end) {
        return (Set<T>) redisTemplate.opsForZSet().range(key, start, end);
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> zRangeByScore(String key, double min, double max) {
        return (Set<T>) redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public static Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    public static Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    public static Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    // ==================== 批量操作 ====================

    public static void mSet(Map<String, String> keyValueMap) {
        stringRedisTemplate.opsForValue().multiSet(keyValueMap);
    }

    public static List<String> mGet(Collection<String> keys) {
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    // ==================== 工具方法 ====================

    public static void clear(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    public static Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

    // ==================== 缓存工具方法 ====================

    public static void cacheValue(String key, Object value) {
        setObj(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public static void cacheValue(String key, Object value, long timeout, TimeUnit timeUnit) {
        setObj(key, value, timeout, timeUnit);
    }

    public static <T> T getCachedValue(String key, Class<T> clazz) {
        return getObj(key);
    }

    public static boolean isCached(String key) {
        return Boolean.TRUE.equals(exists(key));
    }

    // ==================== 分布式锁 ====================

    public static boolean tryLock(String key, String value, long expireTime) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    public static void releaseLock(String key, String value) {
        // 简化版本的分布式锁释放，先检查再删除
        String currentValue = get(key);
        if (value.equals(currentValue)) {
            delete(key);
        }
    }

    // ==================== 计数器 ====================

    public static long increment(String key) {
        Long result = stringRedisTemplate.opsForValue().increment(key);
        return result != null ? result : 0L;
    }

    public static long increment(String key, long delta) {
        Long result = stringRedisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0L;
    }

    public static long increment(String key, long delta, long expireTime, TimeUnit timeUnit) {
        Long result = stringRedisTemplate.opsForValue().increment(key, delta);
        if (result != null && result > 0) {
            stringRedisTemplate.expire(key, expireTime, timeUnit);
        }
        return result != null ? result : 0L;
    }
}