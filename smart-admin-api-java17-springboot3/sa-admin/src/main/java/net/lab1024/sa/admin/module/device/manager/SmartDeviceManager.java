package net.lab1024.sa.admin.module.device.manager;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.lab1024.sa.admin.module.device.dao.SmartDeviceDao;
import net.lab1024.sa.admin.module.device.service.DeviceCacheService;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;

/**
 * 设备Manager - 缓存管理和业务协调
 *
 * <p>
 * 重构后的设备管理器，基于统一缓存架构实现
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 实现四层架构中的Manager层，提供设备管理和性能优化
 * 通过DeviceCacheService统一管理缓存，使用DEVICE命名空间
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class SmartDeviceManager {

    @Resource
    private SmartDeviceDao smartDeviceDao;

    @Resource
    private DeviceCacheService deviceCacheService;

    // ===== 初始化方法 =====

    @PostConstruct
    public void init() {
        log.info("初始化设备缓存管理器：基于统一缓存架构，使用DEVICE命名空间");
        log.info("设备缓存管理器初始化完成");
    }

    // ===== 设备信息缓存 =====

    /**
     * 获取设备信息(多级缓存)
     */
    public SmartDeviceEntity getDeviceInfo(Long deviceId) {
        SmartDeviceEntity device = deviceCacheService.getDeviceInfo(deviceId);
        if (device != null) {
            return device;
        }

        // 缓存未命中，从数据库加载
        SmartDeviceEntity entity = smartDeviceDao.selectById(deviceId);
        if (entity != null && entity.getDeletedFlag() != 1) {
            deviceCacheService.cacheDeviceInfo(entity);
        }

        return entity;
    }

    /**
     * 异步获取设备信息
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    public CompletableFuture<SmartDeviceEntity> getDeviceInfoAsync(Long deviceId) {
        return deviceCacheService.getDeviceInfoAsync(deviceId)
            .thenApply(device -> {
                if (device != null) {
                    return device;
                }

                // 缓存未命中，从数据库加载
                SmartDeviceEntity entity = smartDeviceDao.selectById(deviceId);
                if (entity != null && entity.getDeletedFlag() != 1) {
                    deviceCacheService.cacheDeviceInfoAsync(entity);
                }

                return entity;
            });
    }

    /**
     * 缓存设备信息
     *
     * @param device 设备信息
     */
    public void cacheDeviceInfo(SmartDeviceEntity device) {
        deviceCacheService.cacheDeviceInfo(device);
    }

    /**
     * 批量获取设备信息
     *
     * @param deviceIds 设备ID列表
     * @return 设备信息映射
     */
    public Map<Long, SmartDeviceEntity> batchGetDeviceInfo(List<Long> deviceIds) {
        Map<Long, SmartDeviceEntity> cachedDevices = deviceCacheService.batchGetDeviceInfo(deviceIds);

        // 找出缓存未命中的设备ID
        List<Long> missedIds = deviceIds.stream()
            .filter(id -> !cachedDevices.containsKey(id))
            .collect(java.util.stream.Collectors.toList());

        if (!missedIds.isEmpty()) {
            // 从数据库加载未命中的设备
            List<SmartDeviceEntity> dbDevices = smartDeviceDao.selectBatchIds(missedIds);
            List<SmartDeviceEntity> validDevices = dbDevices.stream()
                .filter(device -> device != null && device.getDeletedFlag() != 1)
                .collect(java.util.stream.Collectors.toList());

            // 缓存新加载的设备
            if (!validDevices.isEmpty()) {
                deviceCacheService.batchCacheDeviceInfo(validDevices);
                // 添加到结果中
                validDevices.forEach(device -> cachedDevices.put(device.getDeviceId(), device));
            }
        }

        return cachedDevices;
    }

    // ===== 设备状态缓存 =====

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    public Integer getDeviceStatus(Long deviceId) {
        return deviceCacheService.getDeviceStatus(deviceId);
    }

    /**
     * 设置设备状态
     *
     * @param deviceId 设备ID
     * @param status   设备状态
     */
    public void setDeviceStatus(Long deviceId, Integer status) {
        deviceCacheService.setDeviceStatus(deviceId, status);
    }

    /**
     * 批量获取设备状态
     *
     * @param deviceIds 设备ID列表
     * @return 设备状态映射
     */
    public Map<Long, Integer> batchGetDeviceStatus(List<Long> deviceIds) {
        return deviceCacheService.batchGetDeviceStatus(deviceIds);
    }

    /**
     * 批量设置设备状态
     *
     * @param deviceStatusMap 设备状态映射
     */
    public void batchSetDeviceStatus(Map<Long, Integer> deviceStatusMap) {
        deviceCacheService.batchSetDeviceStatus(deviceStatusMap);
    }

    // ===== 设备列表缓存 =====

    /**
     * 获取按类型分组的设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    public List<SmartDeviceEntity> getDevicesByType(String deviceType) {
        List<SmartDeviceEntity> devices = deviceCacheService.getDevicesByType(deviceType);
        if (!devices.isEmpty()) {
            return devices;
        }

        // 缓存未命中，从数据库加载
        // 这里假设有相应的DAO方法，实际使用时需要根据具体的查询条件调整
        // List<SmartDeviceEntity> dbDevices = smartDeviceDao.selectByDeviceType(deviceType);
        List<SmartDeviceEntity> dbDevices = Collections.emptyList(); // 占位符，实际使用时需要替换

        if (!dbDevices.isEmpty()) {
            deviceCacheService.cacheDevicesByType(deviceType, dbDevices);
        }

        return dbDevices;
    }

    /**
     * 获取按区域分组的设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    public List<SmartDeviceEntity> getDevicesByArea(Long areaId) {
        List<SmartDeviceEntity> devices = deviceCacheService.getDevicesByArea(areaId);
        if (!devices.isEmpty()) {
            return devices;
        }

        // 缓存未命中，从数据库加载
        // List<SmartDeviceEntity> dbDevices = smartDeviceDao.selectByAreaId(areaId);
        List<SmartDeviceEntity> dbDevices = Collections.emptyList(); // 占位符，实际使用时需要替换

        if (!dbDevices.isEmpty()) {
            deviceCacheService.cacheDevicesByArea(areaId, dbDevices);
        }

        return dbDevices;
    }

    // ===== 缓存管理方法 =====

    /**
     * 清除设备缓存
     */
    public void clearDeviceCache(Long deviceId) {
        deviceCacheService.clearDeviceCache(deviceId);
        log.info("设备缓存清除完成, deviceId: {}", deviceId);
    }

    /**
     * 清除设备状态缓存
     *
     * @param deviceId 设备ID
     */
    public void clearDeviceStatusCache(Long deviceId) {
        deviceCacheService.clearDeviceStatusCache(deviceId);
    }

    /**
     * 批量清除设备缓存
     */
    public void clearAllDeviceCache() {
        deviceCacheService.clearAllDeviceCache();
        log.info("设备缓存批量清除完成");
    }

    /**
     * 预热设备缓存
     */
    public void warmupDeviceCache(Long deviceId) {
        SmartDeviceEntity entity = smartDeviceDao.selectById(deviceId);
        if (entity != null && entity.getDeletedFlag() != 1) {
            deviceCacheService.cacheDeviceInfo(entity);
            deviceCacheService.warmupDeviceCache(deviceId);
            log.info("设备缓存预热完成, deviceId: {}", deviceId);
        }
    }

    /**
     * 预热所有设备缓存
     */
    @Async
    public void warmupAllDeviceCache() {
        deviceCacheService.warmupCache();
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    public Map<String, Object> getCacheStatistics() {
        return deviceCacheService.getCacheStatistics();
    }

    // ===== 兼容性方法 =====

    /**
     * 兼容旧版本的缓存键构建方法
     */
    private String buildCacheKey(Long deviceId, String suffix) {
        return "device:" + deviceId + suffix;
    }

    /**
     * 获取缓存前缀（兼容性）
     */
    public String getCachePrefix() {
        return "device:";
    }
}
