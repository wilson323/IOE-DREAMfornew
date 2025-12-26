package net.lab1024.sa.common.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.vo.UserBasicInfoVO;
import net.lab1024.sa.common.domain.vo.DeviceInfoVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 三级缓存服务
 *
 * 三级缓存架构：
 * - L1: Caffeine本地缓存（进程内，最快）
 * - L2: Redis分布式缓存（跨进程共享）
 * - L3: 数据库（数据源）
 *
 * 缓存策略：Cache-Aside（旁路缓存）
 * 1. 查询时先查L1，未命中查L2，未命中查L3
 * 2. L3查询后写入L2和L1
 * 3. 更新时先更新L3，然后删除L2和L1
 * 4. 删除时同时删除L1、L2、L3
 *
 * @author IOE-DREAM Team
 * @since 2025-01-XX
 */
@Slf4j
@Service
public class ThreeLevelCacheService {

    @Resource(name = "userBasicInfoCache")
    private Cache<String, UserBasicInfoVO> userBasicInfoCache;

    @Resource(name = "deviceInfoCache")
    private Cache<String, DeviceInfoVO> deviceInfoCache;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 缓存前缀定义 ====================

    private static final String CACHE_PREFIX_USER = "cache:user:";
    private static final String CACHE_PREFIX_DEVICE = "cache:device:";
    private static final String CACHE_PREFIX_DICT = "cache:dict:";
    private static final String CACHE_PREFIX_PERMISSION = "cache:permission:";

    // ==================== 用户信息缓存 ====================

    /**
     * 获取用户基本信息（三级缓存）
     *
     * @param userId 用户ID
     * @param dbLoader 数据库加载函数
     * @return 用户基本信息
     */
    public UserBasicInfoVO getUserBasicInfo(Long userId, Function<Long, UserBasicInfoVO> dbLoader) {
        String cacheKey = CACHE_PREFIX_USER + userId;

        // L1: Caffeine本地缓存
        UserBasicInfoVO user = userBasicInfoCache.getIfPresent(cacheKey);
        if (user != null) {
            log.debug("[三级缓存] L1命中: userId={}", userId);
            return user;
        }

        // L2: Redis分布式缓存
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
        if (cachedObj != null) {
            log.debug("[三级缓存] L2命中: userId={}", userId);
            user = (UserBasicInfoVO) cachedObj;
            // 写入L1
            userBasicInfoCache.put(cacheKey, user);
            return user;
        }

        // L3: 数据库查询
        log.debug("[三级缓存] L3查询: userId={}", userId);
        user = dbLoader.apply(userId);

        if (user != null) {
            // 写入L2（5分钟过期）
            redisTemplate.opsForValue().set(cacheKey, user, 5, TimeUnit.MINUTES);
            // 写入L1
            userBasicInfoCache.put(cacheKey, user);
        }

        return user;
    }

    /**
     * 更新用户基本信息（删除缓存）
     *
     * @param userId 用户ID
     */
    public void evictUserBasicInfo(Long userId) {
        String cacheKey = CACHE_PREFIX_USER + userId;

        log.debug("[三级缓存] 删除缓存: userId={}", userId);

        // 删除L1
        userBasicInfoCache.invalidate(cacheKey);

        // 删除L2
        redisTemplate.delete(cacheKey);
    }

    // ==================== 设备信息缓存 ====================

    /**
     * 获取设备信息（三级缓存）
     *
     * @param deviceId 设备ID
     * @param dbLoader 数据库加载函数
     * @return 设备信息
     */
    public DeviceInfoVO getDeviceInfo(String deviceId, Function<String, DeviceInfoVO> dbLoader) {
        String cacheKey = CACHE_PREFIX_DEVICE + deviceId;

        // L1: Caffeine本地缓存
        DeviceInfoVO device = deviceInfoCache.getIfPresent(cacheKey);
        if (device != null) {
            log.debug("[三级缓存] L1命中: deviceId={}", deviceId);
            return device;
        }

        // L2: Redis分布式缓存
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
        if (cachedObj != null) {
            log.debug("[三级缓存] L2命中: deviceId={}", deviceId);
            device = (DeviceInfoVO) cachedObj;
            // 写入L1
            deviceInfoCache.put(cacheKey, device);
            return device;
        }

        // L3: 数据库查询
        log.debug("[三级缓存] L3查询: deviceId={}", deviceId);
        device = dbLoader.apply(deviceId);

        if (device != null) {
            // 写入L2（10分钟过期）
            redisTemplate.opsForValue().set(cacheKey, device, 10, TimeUnit.MINUTES);
            // 写入L1
            deviceInfoCache.put(cacheKey, device);
        }

        return device;
    }

    /**
     * 更新设备信息（删除缓存）
     *
     * @param deviceId 设备ID
     */
    public void evictDeviceInfo(String deviceId) {
        String cacheKey = CACHE_PREFIX_DEVICE + deviceId;

        log.debug("[三级缓存] 删除缓存: deviceId={}", deviceId);

        // 删除L1
        deviceInfoCache.invalidate(cacheKey);

        // 删除L2
        redisTemplate.delete(cacheKey);
    }

    // ==================== 通用缓存操作 ====================

    /**
     * 通用缓存获取（三级缓存）
     *
     * @param cachePrefix 缓存前缀
     * @param key 缓存键
     * @param caffeineCache Caffeine缓存实例
     * @param dbLoader 数据库加载函数
     * @param ttlSeconds Redis过期时间（秒）
     * @param <T> 返回类型
     * @param <K> 键类型
     * @return 缓存值
     */
    public <T, K> T get(
            String cachePrefix,
            K key,
            Cache<String, T> caffeineCache,
            Function<K, T> dbLoader,
            long ttlSeconds) {

        String cacheKey = cachePrefix + key;

        // L1: Caffeine本地缓存
        T value = caffeineCache.getIfPresent(cacheKey);
        if (value != null) {
            log.debug("[三级缓存] L1命中: key={}", cacheKey);
            return value;
        }

        // L2: Redis分布式缓存
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
        if (cachedObj != null) {
            log.debug("[三级缓存] L2命中: key={}", cacheKey);
            value = (T) cachedObj;
            // 写入L1
            caffeineCache.put(cacheKey, value);
            return value;
        }

        // L3: 数据库查询
        log.debug("[三级缓存] L3查询: key={}", cacheKey);
        value = dbLoader.apply(key);

        if (value != null) {
            // 写入L2
            redisTemplate.opsForValue().set(cacheKey, value, ttlSeconds, TimeUnit.SECONDS);
            // 写入L1
            caffeineCache.put(cacheKey, value);
        }

        return value;
    }

    /**
     * 通用缓存删除（同时删除L1和L2）
     *
     * @param cachePrefix 缓存前缀
     * @param key 缓存键
     * @param caffeineCache Caffeine缓存实例
     */
    public void evict(String cachePrefix, Object key, Cache<String, ?> caffeineCache) {
        String cacheKey = cachePrefix + key;

        log.debug("[三级缓存] 删除缓存: key={}", cacheKey);

        // 删除L1
        caffeineCache.invalidate(cacheKey);

        // 删除L2
        redisTemplate.delete(cacheKey);
    }

    // ==================== 缓存预热 ====================

    /**
     * 缓存预热（系统启动时预加载热点数据）
     *
     * 使用场景：
     * - 应用启动时预加载热点数据
     * - 定时任务刷新缓存
     */
    public void warmUp() {
        log.info("[三级缓存] 开始缓存预热...");

        // 预热管理员用户信息
        // TODO: 实际业务中根据需要预加载热点数据

        log.info("[三级缓存] 缓存预热完成");
    }

    // ==================== 缓存统计 ====================

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息字符串
     */
    public String getCacheStats() {
        var userStats = userBasicInfoCache.stats();
        var deviceStats = deviceInfoCache.stats();

        StringBuilder stats = new StringBuilder();
        stats.append("=========================================\n");
        stats.append("[三级缓存] 缓存统计信息\n");
        stats.append("=========================================\n");

        if (userStats != null) {
            stats.append("用户基本信息缓存:\n");
            stats.append(String.format("  - 命中率: %.2f%%\n", userStats.hitRate() * 100));
            stats.append(String.format("  - 命中次数: %d\n", userStats.hitCount()));
            stats.append(String.format("  - 未命中次数: %d\n", userStats.missCount()));
            stats.append(String.format("  - 加载次数: %d\n", userStats.loadCount()));
            stats.append("\n");
        }

        if (deviceStats != null) {
            stats.append("设备信息缓存:\n");
            stats.append(String.format("  - 命中率: %.2f%%\n", deviceStats.hitRate() * 100));
            stats.append(String.format("  - 命中次数: %d\n", deviceStats.hitCount()));
            stats.append(String.format("  - 未命中次数: %d\n", deviceStats.missCount()));
            stats.append(String.format("  - 加载次数: %d\n", deviceStats.loadCount()));
        }

        stats.append("=========================================");

        return stats.toString();
    }
}
