package net.lab1024.sa.common.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

/**
 * Redis工具类
 * 提供Redis操作的便捷方法
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public class RedisUtil {

    private static RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置RedisTemplate
     */
    public static void setRedisTemplate(RedisTemplate<String, Object> template) {
        redisTemplate = template;
    }

    /**
     * 获取RedisTemplate
     */
    public static RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * 设置缓存过期时间
     */
    public static boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key获取过期时间
     */
    public static long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     */
    public static boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     */
    public static void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(arrayToList(key));
            }
        }
    }

    /**
     * 获取ValueOperations
     */
    public static ValueOperations<String, Object> valueOps() {
        return redisTemplate.opsForValue();
    }

    /**
     * 获取HashOperations
     */
    public static HashOperations<String, Object, Object> hashOps() {
        return redisTemplate.opsForHash();
    }

    /**
     * 获取ListOperations
     */
    public static ListOperations<String, Object> listOps() {
        return redisTemplate.opsForList();
    }

    /**
     * 获取SetOperations
     */
    public static SetOperations<String, Object> setOps() {
        return redisTemplate.opsForSet();
    }

    /**
     * 获取ZSetOperations
     */
    public static ZSetOperations<String, Object> zSetOps() {
        return redisTemplate.opsForZSet();
    }

    /**
     * 普通缓存获取
     */
    public static Object get(String key) {
        return key == null ? null : valueOps().get(key);
    }

    /**
     * 普通缓存放入
     */
    public static boolean set(String key, Object value) {
        try {
            valueOps().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     */
    public static boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                valueOps().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     */
    public static long increment(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return valueOps().increment(key, delta);
    }

    /**
     * 递减
     */
    public static long decrement(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return valueOps().increment(key, -delta);
    }

    /**
     * HashGet
     */
    public static Object hget(String key, String item) {
        return hashOps().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     */
    public static Map<Object, Object> hmget(String key) {
        return hashOps().entries(key);
    }

    /**
     * HashSet
     */
    public static boolean hmset(String key, Map<String, Object> map) {
        try {
            hashOps().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     */
    public static boolean hmset(String key, Map<String, Object> map, long time, TimeUnit timeUnit) {
        try {
            hashOps().putAll(key, map);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     */
    public static boolean hset(String key, String item, Object value) {
        try {
            hashOps().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     */
    public static boolean hset(String key, String item, Object value, long time, TimeUnit timeUnit) {
        try {
            hashOps().put(key, item, value);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     */
    public static void hdel(String key, Object... item) {
        hashOps().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     */
    public static boolean hHasKey(String key, String item) {
        return hashOps().hasKey(key, item);
    }

    /**
     * 根据key获取Set中的所有值
     */
    public static Set<Object> sGet(String key) {
        try {
            return setOps().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     */
    public static boolean sHasKey(String key, Object value) {
        try {
            return setOps().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     */
    public static long sSet(String key, Object... values) {
        try {
            return setOps().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     */
    public static long sSetAndTime(String key, long time, TimeUnit timeUnit, Object... values) {
        try {
            Long count = setOps().add(key, values);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     */
    public static long sGetSetSize(String key) {
        try {
            return setOps().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     */
    public static long setRemove(String key, Object... values) {
        try {
            Long count = setOps().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 数组转List
     */
    private static List<String> arrayToList(Object[] objects) {
        return java.util.Arrays.stream(objects).map(obj -> obj.toString())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取泛型对象
     */
    public static <T> T getBean(String key, Class<T> clazz) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj == null) {
                return null;
            }
            // 如果是已经是目标类型，直接返回
            if (clazz.isInstance(obj)) {
                return clazz.cast(obj);
            }
            // 否则需要进行类型转换（这里简化处理，实际项目可能需要JSON序列化/反序列化）
            return clazz.cast(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置泛型对象
     */
    public static <T> void setBean(String key, T obj, long timeout) {
        try {
            if (timeout > 0) {
                redisTemplate.opsForValue().set(key, obj, timeout, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置泛型对象（带过期时间）
     */
    public static <T> void set(String key, T obj, long timeout) {
        try {
            if (timeout > 0) {
                redisTemplate.opsForValue().set(key, obj, timeout, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据模式删除键
     */
    public static void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从列表左侧推入元素
     */
    public static Long lPush(String key, Object value) {
        try {
            return listOps().leftPush(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 获取列表范围
     */
    public static List<String> lRange(String key, long start, long end) {
        try {
            List<Object> list = listOps().range(key, start, end);
            if (list == null) {
                return new java.util.ArrayList<>();
            }
            return list.stream()
                    .map(obj -> obj != null ? obj.toString() : "")
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 修剪列表，只保留指定范围的元素
     */
    public static void lTrim(String key, long start, long end) {
        try {
            listOps().trim(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加hash字段的值
     */
    public static Long hIncrBy(String key, String field, long delta) {
        try {
            return hashOps().increment(key, field, delta);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 获取缓存值并转换为指定类型
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz) {
        try {
            Object value = get(key);
            if (value == null) {
                return null;
            }
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            // 尝试字符串转换
            if (clazz == String.class) {
                return (T) value.toString();
            }
            // 尝试整数转换
            if (clazz == Integer.class && value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
