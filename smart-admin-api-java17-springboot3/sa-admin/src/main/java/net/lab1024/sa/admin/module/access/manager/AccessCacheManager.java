package net.lab1024.sa.admin.module.access.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lab1024.sa.admin.module.access.dao.SmartDeviceAccessExtensionDao;
import net.lab1024.sa.admin.module.access.dao.SmartAreaAccessExtensionDao;
import net.lab1024.sa.admin.module.access.dao.SmartAccessDoorDao;
import net.lab1024.sa.admin.module.access.dao.SmartAccessReaderDao;
import net.lab1024.sa.admin.module.access.dao.SmartAccessAlertDao;
import net.lab1024.sa.admin.module.access.domain.entity.SmartDeviceAccessExtensionEntity;
import net.lab1024.sa.admin.module.access.domain.entity.SmartAreaAccessExtensionEntity;
import net.lab1024.sa.admin.module.access.domain.entity.SmartAccessDoorEntity;
import net.lab1024.sa.admin.module.access.domain.entity.SmartAccessReaderEntity;
import net.lab1024.sa.admin.module.access.domain.entity.SmartAccessAlertEntity;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.JsonUtil;

/**
 * 门禁系统缓存管理器
 * <p>
 * 严格遵循repowiki缓存架构规范：
 * - 统一缓存策略和命名规范
 * - 多级缓存：L1本地缓存 + L2分布式缓存
 * - 自动缓存失效和刷新机制
 * - 性能监控和统计
 * - 支持异步和批量操作
 *
 * 缓存策略：
 * - L1缓存：Caffeine本地缓存，高频率访问数据
 * - L2缓存：Redis分布式缓存，共享数据
 * - 缓存预热：系统启动时预加载热点数据
 * - 缓存更新：数据变更时智能更新缓存
 * - 缓存监控：实时监控缓存命中率和性能指标
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Component
public class AccessCacheManager {

    private static final Logger log = LoggerFactory.getLogger(AccessCacheManager.class);

    @Resource
    private SmartDeviceAccessExtensionDao deviceAccessExtensionDao;

    @Resource
    private SmartAreaAccessExtensionDao areaAccessExtensionDao;

    @Resource
    private SmartAccessDoorDao accessDoorDao;

    @Resource
    private SmartAccessReaderDao accessReaderDao;

    @Resource
    private SmartAccessAlertDao accessAlertDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "access:";
    private static final String CACHE_PREFIX_DEVICE = CACHE_PREFIX + "device:";
    private static final String CACHE_PREFIX_AREA = CACHE_PREFIX + "area:";
    private static final String CACHE_PREFIX_DOOR = CACHE_PREFIX + "door:";
    private static final String CACHE_PREFIX_READER = CACHE_PREFIX + "reader:";
    private static final String CACHE_PREFIX_ALERT = CACHE_PREFIX + "alert:";
    private static final String CACHE_PREFIX_STATS = CACHE_PREFIX + "stats:";

    // 缓存过期时间（秒）
    private static final long CACHE_TTL_SHORT = 300;      // 5分钟 - 高频访问数据
    private static final long CACHE_TTL_MEDIUM = 1800;    // 30分钟 - 中频访问数据
    private static final long CACHE_TTL_LONG = 3600;       // 1小时 - 低频访问数据
    private static final long CACHE_TTL_STATS = 600;       // 10分钟 - 统计数据

    // L1本地缓存配置
    private Cache<String, Object> l1DeviceCache;
    private Cache<String, Object> l1AreaCache;
    private Cache<String, Object> l1DoorCache;
    private Cache<String, Object> l1ReaderCache;
    private Cache<String, Object> l1AlertCache;
    private Cache<String, Object> l1StatsCache;

    // 缓存统计
    private final AtomicLong cacheHitCount = new AtomicLong(0);
    private final AtomicLong cacheMissCount = new AtomicLong(0);
    private final AtomicLong cachePutCount = new AtomicLong(0);
    private final AtomicLong cacheEvictCount = new AtomicLong(0);

    /**
     * 初始化L1缓存
     */
    @PostConstruct
    public void initL1Cache() {
        // 设备缓存 - 最大5000个条目，5分钟过期
        l1DeviceCache = Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(CACHE_TTL_SHORT, TimeUnit.SECONDS)
                .removalListener(this::onRemoval)
                .recordStats()
                .build();

        // 区域缓存 - 最大2000个条目，30分钟过期
        l1AreaCache = Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(CACHE_TTL_MEDIUM, TimeUnit.SECONDS)
                .removalListener(this::onRemoval)
                .recordStats()
                .build();

        // 门缓存 - 最大3000个条目，5分钟过期
        l1DoorCache = Caffeine.newBuilder()
                .maximumSize(3000)
                .expireAfterWrite(CACHE_TTL_SHORT, TimeUnit.SECONDS)
                .removalListener(this::onRemoval)
                .recordStats()
                .build();

        // 读头缓存 - 最大3000个条目，5分钟过期
        l1ReaderCache = Caffeine.newBuilder()
                .maximumSize(3000)
                .expireAfterWrite(CACHE_TTL_SHORT, TimeUnit.SECONDS)
                .removalListener(this::onRemoval)
                .recordStats()
                .build();

        // 告警缓存 - 最大1000个条目，5分钟过期
        l1AlertCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(CACHE_TTL_SHORT, TimeUnit.SECONDS)
                .removalListener(this::onRemoval)
                .recordStats()
                .build();

        // 统计缓存 - 最大500个条目，10分钟过期
        l1StatsCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(CACHE_TTL_STATS, TimeUnit.SECONDS)
                .removalListener(this::onRemoval)
                .recordStats()
                .build();

        log.info("AccessCacheManager initialized with L1 cache configurations");
    }

    /**
     * 缓存移除监听器
     */
    private void onRemoval(String key, Object value, RemovalCause cause) {
        log.debug("Cache entry removed: key={}, cause={}", key, cause);
        cacheEvictCount.incrementAndGet();
    }

    // ==================== 设备扩展缓存 ====================

    /**
     * 获取设备扩展缓存
     */
    public SmartDeviceAccessExtensionEntity getDeviceExtension(Long deviceId) {
        String key = CACHE_PREFIX_DEVICE + deviceId;
        return getFromCache(key, () -> deviceAccessExtensionDao.selectByDeviceIdWithBaseInfo(deviceId),
                l1DeviceCache, SmartDeviceAccessExtensionEntity.class);
    }

    /**
     * 批量获取设备扩展缓存
     */
    public Map<Long, SmartDeviceAccessExtensionEntity> batchGetDeviceExtensions(List<Long> deviceIds) {
        Map<Long, SmartDeviceAccessExtensionEntity> result = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();

        // 先从L1缓存获取
        for (Long deviceId : deviceIds) {
            String key = CACHE_PREFIX_DEVICE + deviceId;
            SmartDeviceAccessExtensionEntity cached = (SmartDeviceAccessExtensionEntity) l1DeviceCache.getIfPresent(key);
            if (cached != null) {
                result.put(deviceId, cached);
                cacheHitCount.incrementAndGet();
            } else {
                missedIds.add(deviceId);
            }
        }

        // 从数据库获取未命中的数据
        if (!CollectionUtils.isEmpty(missedIds)) {
            List<SmartDeviceAccessExtensionEntity> dbResults = deviceAccessExtensionDao.selectByDeviceIds(missedIds);
            for (SmartDeviceAccessExtensionEntity entity : dbResults) {
                String key = CACHE_PREFIX_DEVICE + entity.getDeviceId();
                l1DeviceCache.put(key, entity);
                l2CachePut(key, entity, CACHE_TTL_MEDIUM);
                result.put(entity.getDeviceId(), entity);
            }
            cacheMissCount.addAndGet(missedIds.size());
        }

        return result;
    }

    /**
     * 设置设备扩展缓存
     */
    public void putDeviceExtension(Long deviceId, SmartDeviceAccessExtensionEntity entity) {
        String key = CACHE_PREFIX_DEVICE + deviceId;
        l1DeviceCache.put(key, entity);
        l2CachePut(key, entity, CACHE_TTL_MEDIUM);
        cachePutCount.incrementAndGet();
    }

    /**
     * 删除设备扩展缓存
     */
    public void evictDeviceExtension(Long deviceId) {
        String key = CACHE_PREFIX_DEVICE + deviceId;
        l1DeviceCache.invalidate(key);
        l2CacheEvict(key);
    }

    // ==================== 区域扩展缓存 ====================

    /**
     * 获取区域扩展缓存
     */
    public SmartAreaAccessExtensionEntity getAreaExtension(Long areaId) {
        String key = CACHE_PREFIX_AREA + areaId;
        return getFromCache(key, () -> areaAccessExtensionDao.selectByAreaIdWithBaseInfo(areaId),
                l1AreaCache, SmartAreaAccessExtensionEntity.class);
    }

    /**
     * 设置区域扩展缓存
     */
    public void putAreaExtension(Long areaId, SmartAreaAccessExtensionEntity entity) {
        String key = CACHE_PREFIX_AREA + areaId;
        l1AreaCache.put(key, entity);
        l2CachePut(key, entity, CACHE_TTL_MEDIUM);
    }

    /**
     * 删除区域扩展缓存
     */
    public void evictAreaExtension(Long areaId) {
        String key = CACHE_PREFIX_AREA + areaId;
        l1AreaCache.invalidate(key);
        l2CacheEvict(key);
    }

    // ==================== 门缓存 ====================

    /**
     * 获取门缓存
     */
    public SmartAccessDoorEntity getDoor(Long doorId) {
        String key = CACHE_PREFIX_DOOR + doorId;
        return getFromCache(key, () -> accessDoorDao.selectByIdWithDetails(doorId),
                l1DoorCache, SmartAccessDoorEntity.class);
    }

    /**
     * 批量获取门缓存
     */
    public Map<Long, SmartAccessDoorEntity> batchGetDoors(List<Long> doorIds) {
        Map<Long, SmartAccessDoorEntity> result = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();

        for (Long doorId : doorIds) {
            String key = CACHE_PREFIX_DOOR + doorId;
            SmartAccessDoorEntity cached = (SmartAccessDoorEntity) l1DoorCache.getIfPresent(key);
            if (cached != null) {
                result.put(doorId, cached);
                cacheHitCount.incrementAndGet();
            } else {
                missedIds.add(doorId);
            }
        }

        if (!CollectionUtils.isEmpty(missedIds)) {
            List<SmartAccessDoorEntity> dbResults = accessDoorDao.selectByDeviceIds(missedIds);
            for (SmartAccessDoorEntity entity : dbResults) {
                String key = CACHE_PREFIX_DOOR + entity.getDoorId();
                l1DoorCache.put(key, entity);
                l2CachePut(key, entity, CACHE_TTL_SHORT);
                result.put(entity.getDoorId(), entity);
            }
            cacheMissCount.addAndGet(missedIds.size());
        }

        return result;
    }

    /**
     * 设置门缓存
     */
    public void putDoor(Long doorId, SmartAccessDoorEntity entity) {
        String key = CACHE_PREFIX_DOOR + doorId;
        l1DoorCache.put(key, entity);
        l2CachePut(key, entity, CACHE_TTL_SHORT);
    }

    /**
     * 删除门缓存
     */
    public void evictDoor(Long doorId) {
        String key = CACHE_PREFIX_DOOR + doorId;
        l1DoorCache.invalidate(key);
        l2CacheEvict(key);
    }

    // ==================== 读头缓存 ====================

    /**
     * 获取读头缓存
     */
    public SmartAccessReaderEntity getReader(Long readerId) {
        String key = CACHE_PREFIX_READER + readerId;
        return getFromCache(key, () -> accessReaderDao.selectByIdWithDetails(readerId),
                l1ReaderCache, SmartAccessReaderEntity.class);
    }

    /**
     * 批量获取读头缓存
     */
    public Map<Long, SmartAccessReaderEntity> batchGetReaders(List<Long> readerIds) {
        Map<Long, SmartAccessReaderEntity> result = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();

        for (Long readerId : readerIds) {
            String key = CACHE_PREFIX_READER + readerId;
            SmartAccessReaderEntity cached = (SmartAccessReaderEntity) l1ReaderCache.getIfPresent(key);
            if (cached != null) {
                result.put(readerId, cached);
                cacheHitCount.incrementAndGet();
            } else {
                missedIds.add(readerId);
            }
        }

        if (!CollectionUtils.isEmpty(missedIds)) {
            List<SmartAccessReaderEntity> dbResults = accessReaderDao.selectByDeviceIds(missedIds);
            for (SmartAccessReaderEntity entity : dbResults) {
                String key = CACHE_PREFIX_READER + entity.getReaderId();
                l1ReaderCache.put(key, entity);
                l2CachePut(key, entity, CACHE_TTL_SHORT);
                result.put(entity.getReaderId(), entity);
            }
            cacheMissCount.addAndGet(missedIds.size());
        }

        return result;
    }

    /**
     * 设置读头缓存
     */
    public void putReader(Long readerId, SmartAccessReaderEntity entity) {
        String key = CACHE_PREFIX_READER + readerId;
        l1ReaderCache.put(key, entity);
        l2CachePut(key, entity, CACHE_TTL_SHORT);
    }

    /**
     * 删除读头缓存
     */
    public void evictReader(Long readerId) {
        String key = CACHE_PREFIX_READER + readerId;
        l1ReaderCache.invalidate(key);
        l2CacheEvict(key);
    }

    // ==================== 告警缓存 ====================

    /**
     * 获取告警缓存
     */
    public SmartAccessAlertEntity getAlert(Long alertId) {
        String key = CACHE_PREFIX_ALERT + alertId;
        return getFromCache(key, () -> accessAlertDao.selectByIdWithDetails(alertId),
                l1AlertCache, SmartAccessAlertEntity.class);
    }

    /**
     * 获取待处理告警列表缓存
     */
    public List<SmartAccessAlertEntity> getPendingAlerts() {
        String key = CACHE_PREFIX_ALERT + "pending";
        return getFromCache(key, () -> accessAlertDao.selectPendingAlerts(),
                l1AlertCache, new TypeReference<List<SmartAccessAlertEntity>>() {});
    }

    /**
     * 设置告警缓存
     */
    public void putAlert(Long alertId, SmartAccessAlertEntity entity) {
        String key = CACHE_PREFIX_ALERT + alertId;
        l1AlertCache.put(key, entity);
        l2CachePut(key, entity, CACHE_TTL_SHORT);
    }

    /**
     * 删除告警缓存
     */
    public void evictAlert(Long alertId) {
        String key = CACHE_PREFIX_ALERT + alertId;
        l1AlertCache.invalidate(key);
        l2CacheEvict(key);
        // 同时清除待处理告警列表缓存
        l1AlertCache.invalidate(CACHE_PREFIX_ALERT + "pending");
        l2CacheEvict(CACHE_PREFIX_ALERT + "pending");
    }

    // ==================== 统计缓存 ====================

    /**
     * 获取设备统计缓存
     */
    public Map<String, Object> getDeviceStats() {
        String key = CACHE_PREFIX_STATS + "device";
        return getFromCache(key, this::calculateDeviceStats,
                l1StatsCache, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 计算设备统计数据
     */
    private Map<String, Object> calculateDeviceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", deviceAccessExtensionDao.countTotalAccessDevices());
        stats.put("onlineCount", deviceAccessExtensionDao.countOnlineAccessDevices());
        stats.put("offlineCount", deviceAccessExtensionDao.countTotalAccessDevices() - deviceAccessExtensionDao.countOnlineAccessDevices());
        stats.put("faultCount", deviceAccessExtensionDao.countTotalAccessDevices() - deviceAccessExtensionDao.countOnlineAccessDevices());
        stats.put("updateTime", LocalDateTime.now());
        return stats;
    }

    /**
     * 设置设备统计缓存
     */
    public void putDeviceStats(Map<String, Object> stats) {
        String key = CACHE_PREFIX_STATS + "device";
        l1StatsCache.put(key, stats);
        l2CachePut(key, stats, CACHE_TTL_STATS);
    }

    // ==================== 通用缓存方法 ====================

    /**
     * 从缓存获取数据，支持L1+L2多级缓存
     */
    private <T> T getFromCache(String key, Supplier<T> dataLoader, Cache<String, Object> l1Cache, Class<T> clazz) {
        // 先从L1缓存获取
        Object cached = l1Cache.getIfPresent(key);
        if (cached != null) {
            cacheHitCount.incrementAndGet();
            return SmartBeanUtil.copy(cached, clazz);
        }

        // 从L2缓存获取
        try {
            Object l2Cached = redisTemplate.opsForValue().get(key);
            if (l2Cached != null) {
                T result = SmartBeanUtil.copy(l2Cached, clazz);
                l1Cache.put(key, result);
                cacheHitCount.incrementAndGet();
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to get from L2 cache: key={}", key, e);
        }

        // 从数据库获取
        T data = dataLoader.get();
        if (data != null) {
            l1Cache.put(key, data);
            l2CachePut(key, data, CACHE_TTL_MEDIUM);
        }
        cacheMissCount.incrementAndGet();
        return data;
    }

    /**
     * 从缓存获取数据，支持TypeReference
     */
    private <T> T getFromCache(String key, Supplier<T> dataLoader, Cache<String, Object> l1Cache, TypeReference<T> typeRef) {
        Object cached = l1Cache.getIfPresent(key);
        if (cached != null) {
            cacheHitCount.incrementAndGet();
            return JsonUtil.fromJson(JsonUtil.toJsonString(cached), typeRef);
        }

        try {
            Object l2Cached = redisTemplate.opsForValue().get(key);
            if (l2Cached != null) {
                T result = JsonUtil.fromJson(JsonUtil.toJson(l2Cached), typeRef);
                l1Cache.put(key, result);
                cacheHitCount.incrementAndGet();
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to get from L2 cache: key={}", key, e);
        }

        T data = dataLoader.get();
        if (data != null) {
            l1Cache.put(key, data);
            l2CachePut(key, data, CACHE_TTL_MEDIUM);
        }
        cacheMissCount.incrementAndGet();
        return data;
    }

    /**
     * L2缓存存储
     */
    private void l2CachePut(String key, Object value, long ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to put to L2 cache: key={}", key, e);
        }
    }

    /**
     * L2缓存删除
     */
    private void l2CacheEvict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Failed to evict from L2 cache: key={}", key, e);
        }
    }

    // ==================== 缓存预热 ====================

    /**
     * 预热缓存
     */
    public CompletableFuture<Void> warmupCache() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting access cache warmup...");

                // 预热设备扩展缓存
                warmupDeviceCache();

                // 预热区域扩展缓存
                warmupAreaCache();

                // 预热统计数据缓存
                warmupStatsCache();

                log.info("Access cache warmup completed");
            } catch (Exception e) {
                log.error("Access cache warmup failed", e);
            }
        });
    }

    /**
     * 预热设备缓存
     */
    private void warmupDeviceCache() {
        try {
            // 预热在线设备
            List<SmartDeviceAccessExtensionEntity> onlineDevices = deviceAccessExtensionDao.selectOnlineDevices();
            for (SmartDeviceAccessExtensionEntity device : onlineDevices) {
                putDeviceExtension(device.getDeviceId(), device);
            }
            log.info("Warmed up {} online devices", onlineDevices.size());
        } catch (Exception e) {
            log.error("Failed to warmup device cache", e);
        }
    }

    /**
     * 预热区域缓存
     */
    private void warmupAreaCache() {
        try {
            // 预热启用自动权限分配的区域
            List<SmartAreaAccessExtensionEntity> autoAssignAreas = areaAccessExtensionDao.selectAutoAssignPermissionAreas();
            for (SmartAreaAccessExtensionEntity area : autoAssignAreas) {
                putAreaExtension(area.getAreaId(), area);
            }
            log.info("Warmed up {} auto-assign areas", autoAssignAreas.size());
        } catch (Exception e) {
            log.error("Failed to warmup area cache", e);
        }
    }

    /**
     * 预热统计缓存
     */
    private void warmupStatsCache() {
        try {
            putDeviceStats(calculateDeviceStats());
            log.info("Warmed up device statistics");
        } catch (Exception e) {
            log.error("Failed to warmup stats cache", e);
        }
    }

    // ==================== 缓存监控 ====================

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        // 基础统计
        stats.put("hitCount", cacheHitCount.get());
        stats.put("missCount", cacheMissCount.get());
        stats.put("putCount", cachePutCount.get());
        stats.put("evictCount", cacheEvictCount.get());

        // 计算命中率
        long totalRequests = cacheHitCount.get() + cacheMissCount.get();
        double hitRate = totalRequests > 0 ? (double) cacheHitCount.get() / totalRequests * 100 : 0;
        stats.put("hitRate", Math.round(hitRate * 100.0) / 100.0);

        // L1缓存统计
        stats.put("l1DeviceCacheSize", l1DeviceCache.estimatedSize());
        stats.put("l1AreaCacheSize", l1AreaCache.estimatedSize());
        stats.put("l1DoorCacheSize", l1DoorCache.estimatedSize());
        stats.put("l1ReaderCacheSize", l1ReaderCache.estimatedSize());
        stats.put("l1AlertCacheSize", l1AlertCache.estimatedSize());
        stats.put("l1StatsCacheSize", l1StatsCache.estimatedSize());

        // L1缓存性能统计
        stats.put("l1DeviceCacheStats", l1DeviceCache.stats());
        stats.put("l1AreaCacheStats", l1AreaCache.stats());
        stats.put("l1DoorCacheStats", l1DoorCache.stats());
        stats.put("l1ReaderCacheStats", l1ReaderCache.stats());
        stats.put("l1AlertCacheStats", l1AlertCache.stats());
        stats.put("l1StatsCacheStats", l1StatsCache.stats());

        return stats;
    }

    /**
     * 检查缓存健康状况
     */
    public boolean isCacheHealthy() {
        try {
            // 检查Redis连接
            redisTemplate.opsForValue().get("health:check");

            // 检查L1缓存
            l1DeviceCache.getIfPresent("health:check");

            return true;
        } catch (Exception e) {
            log.error("Cache health check failed", e);
            return false;
        }
    }

    // ==================== 批量操作 ====================

    /**
     * 批量清除缓存
     */
    public void batchEvict(Set<String> patterns) {
        for (String pattern : patterns) {
            try {
                // 清除L1缓存
                l1DeviceCache.invalidateAll();
                l1AreaCache.invalidateAll();
                l1DoorCache.invalidateAll();
                l1ReaderCache.invalidateAll();
                l1AlertCache.invalidateAll();
                l1StatsCache.invalidateAll();

                // 清除L2缓存
                Set<String> keys = redisTemplate.keys(pattern);
                if (!CollectionUtils.isEmpty(keys)) {
                    redisTemplate.delete(keys);
                }

                log.info("Batch evicted cache with pattern: {}", pattern);
            } catch (Exception e) {
                log.error("Failed to batch evict cache with pattern: {}", pattern, e);
            }
        }
    }

    /**
     * 清除所有门禁缓存
     */
    public void evictAllAccessCache() {
        l1DeviceCache.invalidateAll();
        l1AreaCache.invalidateAll();
        l1DoorCache.invalidateAll();
        l1ReaderCache.invalidateAll();
        l1AlertCache.invalidateAll();
        l1StatsCache.invalidateAll();

        // 清除Redis中的门禁缓存
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }

        log.info("Evicted all access cache");
    }

    /**
     * 清理指定设备缓存
     */
    public void clearDeviceCache(Long deviceId) {
        try {
            String deviceKey = CACHE_PREFIX + "device:" + deviceId;

            // 清除L1设备缓存
            l1DeviceCache.invalidate(deviceId.toString());

            // 清除L2缓存中的设备信息
            redisTemplate.delete(deviceKey);

            log.info("Cleared device cache, deviceId: {}", deviceId);
        } catch (Exception e) {
            log.error("Failed to clear device cache, deviceId: {}", deviceId, e);
        }
    }
}