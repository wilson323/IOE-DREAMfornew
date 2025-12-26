package net.lab1024.sa.device.comm.protocol.cache;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 协议缓存服务
 * <p>
 * 为设备通讯协议提供Redis缓存支持，包括设备状态、协议配置等缓存功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Service
public class ProtocolCacheService {

    private static final String CACHE_PREFIX = "ioedream:protocol:";
    private static final String DEVICE_STATUS_PREFIX = CACHE_PREFIX + "device:status:";
    private static final String DEVICE_CONFIG_PREFIX = CACHE_PREFIX + "device:config:";
    private static final String PROTOCOL_RESULT_PREFIX = CACHE_PREFIX + "result:";
    private static final String LOCK_PREFIX = CACHE_PREFIX + "lock:";

    private static final long DEFAULT_EXPIRE_TIME = 30; // 默认30分钟
    private static final long LOCK_EXPIRE_TIME = 5; // 锁5分钟过期

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存设备状态
     *
     * @param deviceId 设备ID
     * @param status    设备状态
     */
    public void cacheDeviceStatus(Long deviceId, Object status) {
        String key = DEVICE_STATUS_PREFIX + deviceId;
        try {
            redisTemplate.opsForValue().set(key, status, DEFAULT_EXPIRE_TIME, TimeUnit.MINUTES);
            log.debug("[协议缓存] 缓存设备状态: deviceId={}, status={}", deviceId, status);
        } catch (Exception e) {
            log.error("[协议缓存] 缓存设备状态失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    public Object getDeviceStatus(Long deviceId) {
        String key = DEVICE_STATUS_PREFIX + deviceId;
        try {
            Object status = redisTemplate.opsForValue().get(key);
            log.debug("[协议缓存] 获取设备状态: deviceId={}, status={}", deviceId, status);
            return status;
        } catch (Exception e) {
            log.error("[协议缓存] 获取设备状态失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 删除设备状态缓存
     *
     * @param deviceId 设备ID
     */
    public void evictDeviceStatus(Long deviceId) {
        String key = DEVICE_STATUS_PREFIX + deviceId;
        try {
            redisTemplate.delete(key);
            log.debug("[协议缓存] 删除设备状态缓存: deviceId={}", deviceId);
        } catch (Exception e) {
            log.error("[协议缓存] 删除设备状态缓存失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 缓存设备配置
     *
     * @param deviceId 设备ID
     * @param config   设备配置
     */
    public void cacheDeviceConfig(Long deviceId, Object config) {
        String key = DEVICE_CONFIG_PREFIX + deviceId;
        try {
            redisTemplate.opsForValue().set(key, config, DEFAULT_EXPIRE_TIME * 2, TimeUnit.MINUTES);
            log.debug("[协议缓存] 缓存设备配置: deviceId={}", deviceId);
        } catch (Exception e) {
            log.error("[协议缓存] 缓存设备配置失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 获取设备配置
     *
     * @param deviceId 设备ID
     * @return 设备配置
     */
    public Object getDeviceConfig(Long deviceId) {
        String key = DEVICE_CONFIG_PREFIX + deviceId;
        try {
            Object config = redisTemplate.opsForValue().get(key);
            log.debug("[协议缓存] 获取设备配置: deviceId={}", deviceId);
            return config;
        } catch (Exception e) {
            log.error("[协议缓存] 获取设备配置失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 删除设备配置缓存
     *
     * @param deviceId 设备ID
     */
    public void evictDeviceConfig(Long deviceId) {
        String key = DEVICE_CONFIG_PREFIX + deviceId;
        try {
            redisTemplate.delete(key);
            log.debug("[协议缓存] 删除设备配置缓存: deviceId={}", deviceId);
        } catch (Exception e) {
            log.error("[协议缓存] 删除设备配置缓存失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 缓存协议处理结果
     *
     * @param requestId 请求ID
     * @param result     处理结果
     */
    public void cacheProtocolResult(String requestId, Object result) {
        String key = PROTOCOL_RESULT_PREFIX + requestId;
        try {
            redisTemplate.opsForValue().set(key, result, DEFAULT_EXPIRE_TIME, TimeUnit.MINUTES);
            log.debug("[协议缓存] 缓存协议结果: requestId={}", requestId);
        } catch (Exception e) {
            log.error("[协议缓存] 缓存协议结果失败: requestId={}", requestId, e);
        }
    }

    /**
     * 获取协议处理结果
     *
     * @param requestId 请求ID
     * @return 处理结果
     */
    public Object getProtocolResult(String requestId) {
        String key = PROTOCOL_RESULT_PREFIX + requestId;
        try {
            Object result = redisTemplate.opsForValue().get(key);
            log.debug("[协议缓存] 获取协议结果: requestId={}", requestId);
            return result;
        } catch (Exception e) {
            log.error("[协议缓存] 获取协议结果失败: requestId={}", requestId, e);
            return null;
        }
    }

    /**
     * 删除协议处理结果缓存
     *
     * @param requestId 请求ID
     */
    public void evictProtocolResult(String requestId) {
        String key = PROTOCOL_RESULT_PREFIX + requestId;
        try {
            redisTemplate.delete(key);
            log.debug("[协议缓存] 删除协议结果缓存: requestId={}", requestId);
        } catch (Exception e) {
            log.error("[协议缓存] 删除协议结果缓存失败: requestId={}", requestId, e);
        }
    }

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey   锁的key
     * @param requestId 请求ID
     * @param timeout   超时时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String requestId, long timeout) {
        String key = LOCK_PREFIX + lockKey;
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, requestId, timeout, TimeUnit.SECONDS);
            boolean acquired = Boolean.TRUE.equals(result);
            log.debug("[协议缓存] 尝试获取锁: key={}, requestId={}, acquired={}", key, requestId, acquired);
            return acquired;
        } catch (Exception e) {
            log.error("[协议缓存] 获取锁失败: key={}, requestId={}", key, requestId, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁的key
     * @param requestId 请求ID
     */
    public void releaseLock(String lockKey, String requestId) {
        String key = LOCK_PREFIX + lockKey;
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            // 使用 org.springframework.data.redis.core.script.DefaultRedisScript 解决执行模糊性
            org.springframework.data.redis.core.script.DefaultRedisScript<Long> redisScript = new org.springframework.data.redis.core.script.DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            
            Long result = redisTemplate.execute(redisScript, java.util.Collections.singletonList(key), requestId);
            boolean released = result != null && result == 1;
            log.debug("[协议缓存] 释放锁: key={}, requestId={}, released={}", key, requestId, released);
        } catch (Exception e) {
            log.error("[协议缓存] 释放锁失败: key={}, requestId={}", key, requestId, e);
        }
    }

    /**
     * 设置带过期时间的缓存
     *
     * @param key      缓存key
     * @param value    缓存值
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     */
    public void setWithExpire(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            log.debug("[协议缓存] 设置缓存: key={}, timeout={}", key, timeout);
        } catch (Exception e) {
            log.error("[协议缓存] 设置缓存失败: key={}, timeout={}", key, timeout, e);
        }
    }

    /**
     * 获取缓存值
     *
     * @param key 缓存key
     * @return 缓存值
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("[协议缓存] 获取缓存: key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("[协议缓存] 获取缓存失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     */
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("[协议缓存] 删除缓存: key={}", key);
        } catch (Exception e) {
            log.error("[协议缓存] 删除缓存失败: key={}", key, e);
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存key
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("[协议缓存] 检查缓存是否存在失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 生成缓存key
     *
     * @param prefix   前缀
     * @param segments 段
     * @return 完整的key
     */
    public String generateKey(String prefix, String... segments) {
        StringBuilder keyBuilder = new StringBuilder(CACHE_PREFIX).append(prefix);
        for (String segment : segments) {
            if (segment != null && !segment.isEmpty()) {
                keyBuilder.append(":").append(segment);
            }
        }
        return keyBuilder.toString();
    }
}