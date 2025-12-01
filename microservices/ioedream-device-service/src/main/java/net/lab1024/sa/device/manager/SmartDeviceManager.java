package net.lab1024.sa.device.manager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.manager.BaseCacheManager;
import net.lab1024.sa.device.dao.SmartDeviceDao;
import net.lab1024.sa.device.domain.entity.SmartDeviceEntity;

/**
 * 智能设备Manager - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseCacheManager实现多级缓存
 * - 命名规范：{Module}Manager
 * - 职责清晰：缓存管理和业务协调
 * - 完整的缓存生命周期管理
 * - 支持批量操作和性能优化
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Component
@Slf4j
public class SmartDeviceManager extends BaseCacheManager {

    @Resource
    private SmartDeviceDao smartDeviceDao;

    // 缓存后缀常量
    private static final String INFO_SUFFIX = ":info";
    private static final String LIST_SUFFIX = ":list";
    private static final String STATUS_SUFFIX = ":status";
    private static final String TYPE_SUFFIX = ":type";
    private static final String STATISTICS_SUFFIX = ":statistics";

    // 默认缓存时间（秒）
    private static final long DEFAULT_CACHE_TIME = 300; // 5分钟
    private static final long LONG_CACHE_TIME = 1800; // 30分钟

    @Override
    protected String getCachePrefix() {
        return "smart:device:";
    }

    /**
     * 获取设备信息(多级缓存)
     *
     * @param deviceId 设备ID
     * @return 设备实体
     */
    public SmartDeviceEntity getDeviceInfo(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        String cacheKey = buildCacheKey(deviceId, INFO_SUFFIX);

        return getCache(cacheKey, () -> {
            SmartDeviceEntity entity = smartDeviceDao.selectById(deviceId);
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
     * @return 设备实体
     */
    public SmartDeviceEntity getDeviceInfoByCode(String deviceCode) {
        if (deviceCode == null || deviceCode.trim().isEmpty()) {
            return null;
        }

        String cacheKey = buildCacheKey("code:" + deviceCode, INFO_SUFFIX);

        return getCache(cacheKey, () -> {
            SmartDeviceEntity entity = smartDeviceDao.selectByDeviceCode(deviceCode);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return null;
            }
            return entity;
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 根据设备类型获取设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    public List<SmartDeviceEntity> getDevicesByType(String deviceType) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            return List.of();
        }

        String cacheKey = buildCacheKey("type:" + deviceType, LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<SmartDeviceEntity> devices = smartDeviceDao.selectByDeviceType(deviceType);
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 根据设备状态获取设备列表
     *
     * @param deviceStatus 设备状态
     * @return 设备列表
     */
    public List<SmartDeviceEntity> getDevicesByStatus(String deviceStatus) {
        if (deviceStatus == null || deviceStatus.trim().isEmpty()) {
            return List.of();
        }

        String cacheKey = buildCacheKey("status:" + deviceStatus, LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<SmartDeviceEntity> devices = smartDeviceDao.selectByDeviceStatus(deviceStatus);
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 根据分组ID获取设备列表
     *
     * @param groupId 分组ID
     * @return 设备列表
     */
    public List<SmartDeviceEntity> getDevicesByGroup(Long groupId) {
        if (groupId == null) {
            return List.of();
        }

        String cacheKey = buildCacheKey("group:" + groupId, LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<SmartDeviceEntity> devices = smartDeviceDao.selectByGroupId(groupId);
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 获取在线设备列表
     *
     * @param minutes 在线时间阈值(分钟)
     * @return 设备列表
     */
    public List<SmartDeviceEntity> getOnlineDevices(Integer minutes) {
        int thresholdMinutes = minutes != null ? minutes : 5;

        String cacheKey = buildCacheKey("online:" + thresholdMinutes, LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<SmartDeviceEntity> devices = smartDeviceDao.selectOnlineDevices(thresholdMinutes);
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 获取离线设备列表
     *
     * @param minutes 离线时间阈值(分钟)
     * @return 设备列表
     */
    public List<SmartDeviceEntity> getOfflineDevices(Integer minutes) {
        int thresholdMinutes = minutes != null ? minutes : 10;

        String cacheKey = buildCacheKey("offline:" + thresholdMinutes, LIST_SUFFIX);

        return getCache(cacheKey, () -> {
            List<SmartDeviceEntity> devices = smartDeviceDao.selectOfflineDevices(thresholdMinutes);
            return devices.stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .collect(Collectors.toList());
        }, DEFAULT_CACHE_TIME);
    }

    /**
     * 获取设备状态统计
     *
     * @return 统计结果
     */
    public Map<String, Object> getDeviceStatusStatistics() {
        String cacheKey = buildCacheKey("statistics", STATUS_SUFFIX);

        return getCache(cacheKey, () -> {
            List<Map<String, Object>> statusCounts = smartDeviceDao.countByDeviceStatus();

            Map<String, Object> statistics = statusCounts.stream()
                    .collect(Collectors.toMap(
                            map -> (String) map.get("status"),
                            map -> map.get("count")));

            // 添加总计
            Long totalCount = smartDeviceDao.countTotalDevices();
            statistics.put("total", totalCount);

            return statistics;
        }, LONG_CACHE_TIME);
    }

    /**
     * 获取设备类型统计
     *
     * @return 统计结果
     */
    public Map<String, Object> getDeviceTypeStatistics() {
        String cacheKey = buildCacheKey("statistics", TYPE_SUFFIX);

        return getCache(cacheKey, () -> {
            List<Map<String, Object>> typeCounts = smartDeviceDao.countByDeviceType();

            Map<String, Object> statistics = typeCounts.stream()
                    .collect(Collectors.toMap(
                            map -> (String) map.get("type"),
                            map -> map.get("count")));

            // 添加总计
            Long totalCount = smartDeviceDao.countTotalDevices();
            statistics.put("total", totalCount);

            return statistics;
        }, LONG_CACHE_TIME);
    }

    /**
     * 获取完整设备统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getFullDeviceStatistics() {
        String cacheKey = buildCacheKey("statistics", STATISTICS_SUFFIX);

        return getCache(cacheKey, () -> {
            Map<String, Object> statistics = Map.of(
                    "status", getDeviceStatusStatistics(),
                    "type", getDeviceTypeStatistics(),
                    "total", smartDeviceDao.countTotalDevices(),
                    "enabled", smartDeviceDao.countEnabledDevices());

            return statistics;
        }, LONG_CACHE_TIME);
    }

    /**
     * 清除设备缓存
     *
     * @param deviceId 设备ID
     */
    public void clearDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        // 清除设备基本信息缓存
        String infoCacheKey = buildCacheKey(deviceId, INFO_SUFFIX);
        removeCache(infoCacheKey);

        // 清除状态相关缓存
        String statusCacheKey = buildCacheKey("status", LIST_SUFFIX);
        removeCache(statusCacheKey);

        // 清除统计缓存
        String statisticsCacheKey = buildCacheKey("statistics", STATISTICS_SUFFIX);
        removeCache(statisticsCacheKey);

        log.info("智能设备缓存清除完成, deviceId: {}", deviceId);
    }

    /**
     * 清除设备类型相关缓存
     *
     * @param deviceType 设备类型
     */
    public void clearDeviceTypeCache(String deviceType) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            return;
        }

        // 清除类型列表缓存
        String typeListCacheKey = buildCacheKey("type:" + deviceType, LIST_SUFFIX);
        removeCache(typeListCacheKey);

        // 清除统计缓存
        String statisticsCacheKey = buildCacheKey("statistics", STATISTICS_SUFFIX);
        removeCache(statisticsCacheKey);

        log.info("设备类型缓存清除完成, deviceType: {}", deviceType);
    }

    /**
     * 批量清除设备缓存
     */
    public void clearAllDeviceCache() {
        String pattern = getCachePrefix() + "*";
        removeCacheByPattern(pattern);
        log.info("智能设备缓存批量清除完成");
    }

    /**
     * 预热设备缓存
     *
     * @param deviceId 设备ID
     */
    public void warmupDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        SmartDeviceEntity entity = smartDeviceDao.selectById(deviceId);
        if (entity != null && entity.getDeletedFlag() != 1) {
            String cacheKey = buildCacheKey(deviceId, INFO_SUFFIX);
            setCache(cacheKey, entity, DEFAULT_CACHE_TIME);
            log.info("智能设备缓存预热完成, deviceId: {}", deviceId);
        }
    }

    /**
     * 批量预热设备缓存
     *
     * @param deviceIds 设备ID列表
     */
    public void warmupDeviceCacheBatch(List<Long> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return;
        }

        int successCount = 0;
        for (Long deviceId : deviceIds) {
            try {
                warmupDeviceCache(deviceId);
                successCount++;
            } catch (Exception e) {
                log.warn("设备缓存预热失败, deviceId: {}, error: {}", deviceId, e.getMessage());
            }
        }

        log.info("智能设备批量缓存预热完成, 总数: {}, 成功: {}", deviceIds.size(), successCount);
    }

    /**
     * 更新设备状态后清除相关缓存
     *
     * @param deviceId     设备ID
     * @param deviceStatus 设备状态
     */
    public void updateDeviceStatusCache(Long deviceId, String deviceStatus) {
        // 清除设备信息缓存
        clearDeviceCache(deviceId);

        // 清除状态列表缓存
        String statusListKey = buildCacheKey("status:" + deviceStatus, LIST_SUFFIX);
        removeCache(statusListKey);

        // 清除统计缓存
        String statisticsCacheKey = buildCacheKey("statistics", STATISTICS_SUFFIX);
        removeCache(statisticsCacheKey);

        log.info("设备状态更新缓存清除完成, deviceId: {}, status: {}", deviceId, deviceStatus);
    }
}
