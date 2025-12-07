package net.lab1024.sa.devicecomm.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 协议缓存管理器
 * <p>
 * 实现多级缓存（L1本地缓存 + L2 Redis缓存）用于协议处理中的热点数据
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解
 * - 使用@Resource注入依赖
 * - 完整的函数级注释
 * </p>
 * <p>
 * 缓存策略：
 * - L1缓存（Caffeine）：设备信息、用户信息等热点数据，5分钟过期
 * - L2缓存（Redis）：设备信息、用户信息等共享数据，30分钟过期
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class ProtocolCacheManager {

    /**
     * Redis模板（L2分布式缓存）
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * L1本地缓存（Caffeine）
     * <p>
     * 缓存设备信息、用户信息等热点数据
     * 最大容量：5000
     * 过期时间：5分钟
     * </p>
     */
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX_DEVICE = "protocol:device:";
    private static final String CACHE_KEY_PREFIX_DEVICE_CODE = "protocol:device:code:";
    private static final String CACHE_KEY_PREFIX_USER_CARD = "protocol:user:card:";

    /**
     * L2缓存过期时间（30分钟）
     */
    private static final long L2_CACHE_EXPIRE_MINUTES = 30;

    /**
     * 根据设备ID获取设备信息（多级缓存）
     * <p>
     * 查询顺序：L1本地缓存 -> L2 Redis缓存 -> 数据库
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备实体，如果不存在返回null
     */
    public DeviceEntity getDeviceById(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        String cacheKey = CACHE_KEY_PREFIX_DEVICE + deviceId;

        try {
            // 1. 先查询L1本地缓存
            DeviceEntity device = (DeviceEntity) localCache.getIfPresent(cacheKey);
            if (device != null) {
                log.debug("[协议缓存] L1缓存命中，deviceId={}", deviceId);
                return device;
            }

            // 2. 查询L2 Redis缓存
            DeviceEntity deviceFromRedis = (DeviceEntity) redisTemplate.opsForValue().get(cacheKey);
            device = deviceFromRedis;
            if (device != null) {
                log.debug("[协议缓存] L2缓存命中，deviceId={}", deviceId);
                // 回填L1缓存
                localCache.put(cacheKey, device);
                return device;
            }

            log.debug("[协议缓存] 缓存未命中，deviceId={}", deviceId);
            return null;

        } catch (Exception e) {
            log.warn("[协议缓存] 获取设备信息异常，deviceId={}, 错误={}", deviceId, e.getMessage());
            return null;
        }
    }

    /**
     * 根据设备编码获取设备信息（多级缓存）
     * <p>
     * 查询顺序：L1本地缓存 -> L2 Redis缓存 -> 数据库
     * </p>
     *
     * @param deviceCode 设备编码（SN）
     * @return 设备实体，如果不存在返回null
     */
    public DeviceEntity getDeviceByCode(String deviceCode) {
        if (deviceCode == null || deviceCode.isEmpty()) {
            return null;
        }

        String cacheKey = CACHE_KEY_PREFIX_DEVICE_CODE + deviceCode;

        try {
            // 1. 先查询L1本地缓存
            DeviceEntity device = (DeviceEntity) localCache.getIfPresent(cacheKey);
            if (device != null) {
                log.debug("[协议缓存] L1缓存命中，deviceCode={}", deviceCode);
                return device;
            }

            // 2. 查询L2 Redis缓存
            DeviceEntity deviceFromRedis = (DeviceEntity) redisTemplate.opsForValue().get(cacheKey);
            device = deviceFromRedis;
            if (device != null) {
                log.debug("[协议缓存] L2缓存命中，deviceCode={}", deviceCode);
                // 回填L1缓存
                localCache.put(cacheKey, device);
                return device;
            }

            log.debug("[协议缓存] 缓存未命中，deviceCode={}", deviceCode);
            return null;

        } catch (Exception e) {
            log.warn("[协议缓存] 获取设备信息异常，deviceCode={}, 错误={}", deviceCode, e.getMessage());
            return null;
        }
    }

    /**
     * 缓存设备信息（多级缓存）
     * <p>
     * 同时缓存到L1和L2
     * </p>
     *
     * @param device 设备实体
     */
    public void cacheDevice(DeviceEntity device) {
        if (device == null) {
            return;
        }

        try {
            // 缓存到L1
            String cacheKeyById = CACHE_KEY_PREFIX_DEVICE + device.getDeviceId();
            localCache.put(cacheKeyById, device);

            // 缓存到L2
            redisTemplate.opsForValue().set(cacheKeyById, device, L2_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 如果设备编码不为空，也缓存设备编码映射
            if (device.getDeviceCode() != null && !device.getDeviceCode().isEmpty()) {
                String cacheKeyByCode = CACHE_KEY_PREFIX_DEVICE_CODE + device.getDeviceCode();
                localCache.put(cacheKeyByCode, device);
                redisTemplate.opsForValue().set(cacheKeyByCode, device, L2_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }

            log.debug("[协议缓存] 设备信息已缓存，deviceId={}, deviceCode={}", 
                    device.getDeviceId(), device.getDeviceCode());

        } catch (Exception e) {
            log.warn("[协议缓存] 缓存设备信息异常，deviceId={}, 错误={}", 
                    device.getDeviceId(), e.getMessage());
        }
    }

    /**
     * 根据卡号获取用户ID（多级缓存）
     * <p>
     * 查询顺序：L1本地缓存 -> L2 Redis缓存 -> 数据库
     * </p>
     *
     * @param cardNumber 卡号
     * @return 用户ID，如果不存在返回null
     */
    public Long getUserIdByCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return null;
        }

        String cacheKey = CACHE_KEY_PREFIX_USER_CARD + cardNumber;

        try {
            // 1. 先查询L1本地缓存
            Long userId = (Long) localCache.getIfPresent(cacheKey);
            if (userId != null) {
                log.debug("[协议缓存] L1缓存命中，cardNumber={}, userId={}", cardNumber, userId);
                return userId;
            }

            // 2. 查询L2 Redis缓存
            userId = (Long) redisTemplate.opsForValue().get(cacheKey);
            if (userId != null) {
                log.debug("[协议缓存] L2缓存命中，cardNumber={}, userId={}", cardNumber, userId);
                // 回填L1缓存
                localCache.put(cacheKey, userId);
                return userId;
            }

            log.debug("[协议缓存] 缓存未命中，cardNumber={}", cardNumber);
            return null;

        } catch (Exception e) {
            log.warn("[协议缓存] 获取用户ID异常，cardNumber={}, 错误={}", cardNumber, e.getMessage());
            return null;
        }
    }

    /**
     * 缓存卡号到用户ID的映射（多级缓存）
     * <p>
     * 同时缓存到L1和L2
     * </p>
     *
     * @param cardNumber 卡号
     * @param userId 用户ID
     */
    public void cacheUserCardMapping(String cardNumber, Long userId) {
        if (cardNumber == null || cardNumber.isEmpty() || userId == null) {
            return;
        }

        try {
            String cacheKey = CACHE_KEY_PREFIX_USER_CARD + cardNumber;

            // 缓存到L1
            localCache.put(cacheKey, userId);

            // 缓存到L2
            redisTemplate.opsForValue().set(cacheKey, userId, L2_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            log.debug("[协议缓存] 卡号映射已缓存，cardNumber={}, userId={}", cardNumber, userId);

        } catch (Exception e) {
            log.warn("[协议缓存] 缓存卡号映射异常，cardNumber={}, 错误={}", cardNumber, e.getMessage());
        }
    }

    /**
     * 清除设备缓存
     * <p>
     * 同时清除L1和L2缓存
     * </p>
     *
     * @param deviceId 设备ID
     */
    public void evictDevice(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        try {
            String cacheKey = CACHE_KEY_PREFIX_DEVICE + deviceId;
            localCache.invalidate(cacheKey);
            redisTemplate.delete(cacheKey);
            log.debug("[协议缓存] 设备缓存已清除，deviceId={}", deviceId);
        } catch (Exception e) {
            log.warn("[协议缓存] 清除设备缓存异常，deviceId={}, 错误={}", deviceId, e.getMessage());
        }
    }

    /**
     * 获取L1缓存统计信息
     * <p>
     * 用于监控缓存命中率
     * </p>
     *
     * @return 缓存统计信息字符串
     */
    public String getCacheStats() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = localCache.stats();
        return String.format("L1缓存统计 - 命中率: %.2f%%, 请求数: %d, 命中数: %d, 未命中数: %d, 驱逐数: %d",
                stats.hitRate() * 100,
                stats.requestCount(),
                stats.hitCount(),
                stats.missCount(),
                stats.evictionCount());
    }
}

