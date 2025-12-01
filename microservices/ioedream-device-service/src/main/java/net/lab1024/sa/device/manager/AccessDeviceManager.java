package net.lab1024.sa.device.manager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.manager.BaseCacheManager;
import net.lab1024.sa.device.dao.AccessDeviceDao;
import net.lab1024.sa.device.domain.entity.AccessDeviceEntity;

/**
 * 门禁设备Manager - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseCacheManager实现多级缓存
 * - 命名规范：{Module}Manager
 * - 职责清晰：缓存管理和业务协调
 * - 完整的缓存生命周期管理
 * - 支持心跳管理和设备状态监控
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Component
@Slf4j
public class AccessDeviceManager extends BaseCacheManager {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    // 缓存后缀常量
    private static final String INFO_SUFFIX = ":info";
    private static final String LIST_SUFFIX = ":list";
    private static final String TYPE_SUFFIX = ":type";
    private static final String MODE_SUFFIX = ":mode";
    private static final String STATISTICS_SUFFIX = ":statistics";

    // 默认缓存时间（秒）
    private static final long DEFAULT_CACHE_TIME = 300; // 5分钟
    private static final long LONG_CACHE_TIME = 1800; // 30分钟
    private static final long SHORT_CACHE_TIME = 60; // 1分钟

    @Override
    protected String getCachePrefix() {
        return "access:device:";
    }

    /**
     * 获取门禁设备信息(多级缓存)
     *
     * @param accessDeviceId 门禁设备ID
     * @return 门禁设备实体
     */
    public AccessDeviceEntity getDeviceInfo(Long accessDeviceId) {
        if (accessDeviceId == null) {
            return null;
        }

        String cacheKey = buildCacheKey(accessDeviceId, INFO_SUFFIX);

        return getCache(cacheKey, () -> {
            AccessDeviceEntity entity = accessDeviceDao.selectById(accessDeviceId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return null;
            }
            return entity;
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 根据设备编码获取设备信息
     *
     * @param deviceCode 设备编码
     * @return 门禁设备实体
     */
    public AccessDeviceEntity getDeviceInfoByCode(String deviceCode) {
        if (deviceCode == null || deviceCode.trim().isEmpty()) {
            return null;
        }

        String cacheKey = buildCacheKey("code:" + deviceCode, INFO_SUFFIX);

        return getCache(cacheKey, () -> {
            AccessDeviceEntity entity = accessDeviceDao.selectByDeviceCode(deviceCode);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return null;
            }
            return entity;
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 根据区域ID获取设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    public List<AccessDeviceEntity> getDevicesByArea(Long areaId) {
        if (areaId == null) {
            return List.of();
        }

        String cacheKey = buildCacheKey("area:" + areaId, LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectByAreaId(areaId);
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 获取在线设备列表
     *
     * @return 设备列表
     */
    public List<AccessDeviceEntity> getOnlineDevices() {
        String cacheKey = buildCacheKey("online", LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectOnlineDevices();
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, SHORT_CACHE_TIME);
    }

    /**
     * 获取离线设备列表
     *
     * @return 设备列表
     */
    public List<AccessDeviceEntity> getOfflineDevices() {
        String cacheKey = buildCacheKey("offline", LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectOfflineDevices();
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, SHORT_CACHE_TIME);
    }

    /**
     * 获取需要维护的设备列表
     *
     * @return 设备列表
     */
    public List<AccessDeviceEntity> getMaintenanceDevices() {
        String cacheKey = buildCacheKey("maintenance", LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectDevicesNeedingMaintenance();
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 获取心跳超时的设备列表
     *
     * @param timeoutMinutes 超时阈值(分钟)
     * @return 设备列表
     */
    public List<AccessDeviceEntity> getHeartbeatTimeoutDevices(Integer timeoutMinutes) {
        int thresholdMinutes = timeoutMinutes != null ? timeoutMinutes : 5;

        String cacheKey = buildCacheKey("timeout:" + thresholdMinutes, LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectHeartbeatTimeoutDevices(thresholdMinutes);
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, SHORT_CACHE_TIME);
    }

    /**
     * 根据设备类型获取设备列表
     *
     * @param accessDeviceType 门禁设备类型
     * @return 设备列表
     */
    public List<AccessDeviceEntity> getDevicesByType(Integer accessDeviceType) {
        if (accessDeviceType == null) {
            return List.of();
        }

        String cacheKey = buildCacheKey("type:" + accessDeviceType, LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectByDeviceType(accessDeviceType);
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 根据工作模式获取设备列表
     *
     * @param workMode 工作模式
     * @return 设备列表
     */
    public List<AccessDeviceEntity> getDevicesByWorkMode(Integer workMode) {
        if (workMode == null) {
            return List.of();
        }

        String cacheKey = buildCacheKey("mode:" + workMode, LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectByWorkMode(workMode);
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 获取紧急模式设备列表
     *
     * @return 设备列表
     */
    public List<AccessDeviceEntity> getEmergencyModeDevices() {
        String cacheKey = buildCacheKey("emergency", LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectEmergencyModeDevices();
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, SHORT_CACHE_TIME);
    }

    /**
     * 获取锁闭模式设备列表
     *
     * @return 设备列表
     */
    public List<AccessDeviceEntity> getLockModeDevices() {
        String cacheKey = buildCacheKey("lock", LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectLockModeDevices();
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, SHORT_CACHE_TIME);
    }

    /**
     * 获取设备统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getDeviceStatistics() {
        String cacheKey = buildCacheKey("statistics", STATISTICS_SUFFIX);

        return getCache(cacheKey, () -> {
            Map<String, Object> statistics = Map.of(
                    "total", accessDeviceDao.countTotalDevices(),
                    "online", accessDeviceDao.countOnlineDevices(),
                    "enabled", accessDeviceDao.countEnabledDevices(),
                    "byType", getDeviceTypeStatistics(),
                    "byWorkMode", getDeviceWorkModeStatistics());

            return statistics;
        }, LONG_CACHE_TIME);
    }

    /**
     * 获取设备类型统计
     *
     * @return 统计结果
     */
    public List<Map<String, Object>> getDeviceTypeStatistics() {
        String cacheKey = buildCacheKey("statistics", TYPE_SUFFIX);

        return getCache(cacheKey, () -> {
            List<Map<String, Object>> typeCounts = accessDeviceDao.countDevicesByType();
            return typeCounts;
        }, LONG_CACHE_TIME);
    }

    /**
     * 获取设备工作模式统计
     *
     * @return 统计结果
     */
    public List<Map<String, Object>> getDeviceWorkModeStatistics() {
        String cacheKey = buildCacheKey("statistics", MODE_SUFFIX);

        return getCache(cacheKey, () -> {
            List<Map<String, Object>> modeCounts = accessDeviceDao.countDevicesByWorkMode();
            return modeCounts;
        }, LONG_CACHE_TIME);
    }

    /**
     * 更新设备心跳时间并清除相关缓存
     *
     * @param accessDeviceId 门禁设备ID
     * @param heartbeatTime  心跳时间
     */
    public void updateDeviceHeartbeat(Long accessDeviceId, LocalDateTime heartbeatTime) {
        try {
            // 更新数据库
            int result = accessDeviceDao.updateHeartbeatTime(accessDeviceId, heartbeatTime);

            if (result > 0) {
                // 清除相关缓存
                clearDeviceCache(accessDeviceId);

                // 清除在线/离线设备列表缓存
                String onlineCacheKey = buildCacheKey("online", LIST_SUFFIX);
                String offlineCacheKey = buildCacheKey("offline", LIST_SUFFIX);
                removeCache(onlineCacheKey);
                removeCache(offlineCacheKey);

                // 清除统计缓存
                clearStatisticsCache();

                log.debug("门禁设备心跳更新完成, accessDeviceId: {}, heartbeatTime: {}", accessDeviceId, heartbeatTime);
            }
        } catch (Exception e) {
            log.error("门禁设备心跳更新失败, accessDeviceId: {}", accessDeviceId, e);
            throw new RuntimeException("更新设备心跳失败", e);
        }
    }

    /**
     * 清除设备缓存
     *
     * @param accessDeviceId 门禁设备ID
     */
    public void clearDeviceCache(Long accessDeviceId) {
        if (accessDeviceId == null) {
            return;
        }

        // 清除设备基本信息缓存
        String infoCacheKey = buildCacheKey(accessDeviceId, INFO_SUFFIX);
        removeCache(infoCacheKey);

        // 清除区域相关缓存（需要重新获取设备信息来确定区域）
        AccessDeviceEntity device = accessDeviceDao.selectById(accessDeviceId);
        if (device != null && device.getAreaId() != null) {
            String areaCacheKey = buildCacheKey("area:" + device.getAreaId(), LIST_SUFFIX);
            removeCache(areaCacheKey);
        }

        log.debug("门禁设备缓存清除完成, accessDeviceId: {}", accessDeviceId);
    }

    /**
     * 清除区域设备缓存
     *
     * @param areaId 区域ID
     */
    public void clearAreaDeviceCache(Long areaId) {
        if (areaId == null) {
            return;
        }

        String areaCacheKey = buildCacheKey("area:" + areaId, LIST_SUFFIX);
        removeCache(areaCacheKey);

        log.debug("区域设备缓存清除完成, areaId: {}", areaId);
    }

    /**
     * 清除统计缓存
     */
    public void clearStatisticsCache() {
        String statisticsCacheKey = buildCacheKey("statistics", STATISTICS_SUFFIX);
        String typeStatisticsKey = buildCacheKey("statistics", TYPE_SUFFIX);
        String modeStatisticsKey = buildCacheKey("statistics", MODE_SUFFIX);

        removeCache(statisticsCacheKey);
        removeCache(typeStatisticsKey);
        removeCache(modeStatisticsKey);
    }

    /**
     * 批量清除设备缓存
     */
    public void clearAllDeviceCache() {
        String pattern = getCachePrefix() + "*";
        removeCacheByPattern(pattern);
        log.info("门禁设备缓存批量清除完成");
    }

    /**
     * 预热设备缓存
     *
     * @param accessDeviceId 门禁设备ID
     */
    public void warmupDeviceCache(Long accessDeviceId) {
        if (accessDeviceId == null) {
            return;
        }

        AccessDeviceEntity entity = accessDeviceDao.selectById(accessDeviceId);
        if (entity != null && entity.getDeletedFlag() != 1) {
            String cacheKey = buildCacheKey(accessDeviceId, INFO_SUFFIX);
            setCache(cacheKey, entity, DEFAULT_CACHE_TIME);
            log.debug("门禁设备缓存预热完成, accessDeviceId: {}", accessDeviceId);
        }
    }

    /**
     * 批量预热设备缓存
     *
     * @param accessDeviceIds 门禁设备ID列表
     */
    public void warmupDeviceCacheBatch(List<Long> accessDeviceIds) {
        if (accessDeviceIds == null || accessDeviceIds.isEmpty()) {
            return;
        }

        int successCount = 0;
        for (Long accessDeviceId : accessDeviceIds) {
            try {
                warmupDeviceCache(accessDeviceId);
                successCount++;
            } catch (Exception e) {
                log.warn("门禁设备缓存预热失败, accessDeviceId: {}, error: {}", accessDeviceId, e.getMessage());
            }
        }

        log.info("门禁设备批量缓存预热完成, 总数: {}, 成功: {}", accessDeviceIds.size(), successCount);
    }

    /**
     * 获取设备健康状态信息
     *
     * @param accessDeviceId 门禁设备ID
     * @return 健康状态信息
     */
    public Map<String, Object> getDeviceHealthStatus(Long accessDeviceId) {
        AccessDeviceEntity device = getDeviceInfo(accessDeviceId);
        if (device == null) {
            return Map.of("healthy", false, "message", "设备不存在");
        }

        boolean isOnline = device.getOnlineStatus() == 1;
        boolean isEnabled = device.getEnabled() == 1;
        boolean hasRecentHeartbeat = device.getLastHeartbeatTime() != null &&
                device.getLastHeartbeatTime().isAfter(LocalDateTime.now().minusMinutes(5));

        boolean healthy = isOnline && isEnabled && hasRecentHeartbeat;

        return Map.of(
                "healthy", healthy,
                "onlineStatus", isOnline,
                "enabled", isEnabled,
                "hasRecentHeartbeat", hasRecentHeartbeat,
                "lastHeartbeatTime", device.getLastHeartbeatTime(),
                "workMode", device.getWorkMode(),
                "message", healthy ? "设备状态正常" : "设备状态异常");
    }
}
