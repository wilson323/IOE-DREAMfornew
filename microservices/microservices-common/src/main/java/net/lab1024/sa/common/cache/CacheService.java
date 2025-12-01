package net.lab1024.sa.common.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.util.RedisUtil;

/**
 * 多级缓存服务实现
 * L1缓存: Caffeine本地缓存（5分钟过期）
 * L2缓存: Redis分布式缓存（30分钟过期）
 *
 * @author SmartAdmin
 * @since 2025-11-13
 */
@Slf4j
@Service
public class CacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 设置缓存 - 默认30分钟过期
     */
    public void set(String key, Object value) {
        this.set(key, value, 30, TimeUnit.MINUTES);
    }

    /**
     * 设置缓存带过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            log.debug("设置L2缓存成功: key={}, timeout={} {}", key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("设置L2缓存失败: key={}", key, e);
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("获取L2缓存: key={}, value={}", key, value != null ? "HIT" : "MISS");
            return value;
        } catch (Exception e) {
            log.error("获取L2缓存失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("删除L2缓存: key={}", key);
        } catch (Exception e) {
            log.error("删除L2缓存失败: key={}", key, e);
        }
    }

    /**
     * 检查缓存是否存在
     */
    public boolean hasKey(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("检查L2缓存存在性失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置缓存过期时间
     */
    public void expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.expire(key, timeout, timeUnit);
            log.debug("设置L2缓存过期时间: key={}, timeout={} {}", key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("设置L2缓存过期时间失败: key={}", key, e);
        }
    }

    /**
     * 获取缓存过期时间
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("获取L2缓存过期时间失败: key={}", key, e);
            return -1;
        }
    }

    /**
     * 批量删除缓存
     */
    public void delete(String... keys) {
        if (keys != null && keys.length > 0) {
            for (String key : keys) {
                this.delete(key);
            }
        }
    }
}