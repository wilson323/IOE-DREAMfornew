package net.lab1024.sa.admin.module.device.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.admin.module.device.service.DeviceCacheService;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import net.lab1024.sa.base.common.cache.UnifiedCacheManager;

/**
 * 设备系统统一缓存服务实现
 * <p>
 * 基于UnifiedCacheManager实现的设备模块缓存服务
 * 遵循缓存架构统一化规范，使用DEVICE命名空间
 * 缓存键格式：iog:cache:DEVICE:{namespace}:{key}
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Slf4j
@Service
public class DeviceCacheServiceImpl implements DeviceCacheService {

    @Resource
    private UnifiedCacheManager unifiedCacheManager;

    // 缓存键命名空间定义
    private static final class CacheKey {
        // 设备信息
        public static final String DEVICE_INFO = "info";
        public static final String DEVICE_LIST = "list";

        // 设备状态
        public static final String DEVICE_STATUS = "status";
        public static final String DEVICE_STATUS_BATCH = "status:batch";

        // 设备分类
        public static final String DEVICE_BY_TYPE = "by:type";
        public static final String DEVICE_BY_AREA = "by:area";

        // 统计数据
        public static final String DEVICE_STATISTICS = "statistics";
        public static final String DEVICE_TYPE_STATISTICS = "statistics:type";
    }

    // ========== 设备信息缓存 ==========

    @Override
    public SmartDeviceEntity getDeviceInfo(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        String key = buildKey(CacheKey.DEVICE_INFO, deviceId.toString());
        UnifiedCacheManager.CacheResult<SmartDeviceEntity> result =
            unifiedCacheManager.get(CacheNamespace.DEVICE, key, SmartDeviceEntity.class);

        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public CompletableFuture<SmartDeviceEntity> getDeviceInfoAsync(Long deviceId) {
        if (deviceId == null) {
            return CompletableFuture.completedFuture(null);
        }

        String key = buildKey(CacheKey.DEVICE_INFO, deviceId.toString());
        return unifiedCacheManager.getAsync(CacheNamespace.DEVICE, key, SmartDeviceEntity.class)
            .thenApply(result -> result.isSuccess() ? result.getData() : null);
    }

    @Override
    public void cacheDeviceInfo(SmartDeviceEntity device) {
        if (device == null || device.getDeviceId() == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_INFO, device.getDeviceId().toString());
        unifiedCacheManager.set(CacheNamespace.DEVICE, key, device);

        log.debug("缓存设备信息: deviceId={}", device.getDeviceId());
    }

    @Override
    public CompletableFuture<Void> cacheDeviceInfoAsync(SmartDeviceEntity device) {
        if (device == null || device.getDeviceId() == null) {
            return CompletableFuture.completedFuture(null);
        }

        String key = buildKey(CacheKey.DEVICE_INFO, device.getDeviceId().toString());
        return unifiedCacheManager.setAsync(CacheNamespace.DEVICE, key, device)
            .thenRun(() -> log.debug("异步缓存设备信息: deviceId={}", device.getDeviceId()));
    }

    @Override
    public Map<Long, SmartDeviceEntity> batchGetDeviceInfo(List<Long> deviceIds) {
        Map<Long, SmartDeviceEntity> result = new HashMap<>();

        if (CollectionUtils.isEmpty(deviceIds)) {
            return result;
        }

        // 批量获取缓存
        List<String> keys = deviceIds.stream()
            .map(id -> buildKey(CacheKey.DEVICE_INFO, id.toString()))
            .collect(Collectors.toList());

        UnifiedCacheManager.BatchCacheResult<SmartDeviceEntity> batchResult =
            unifiedCacheManager.mGet(CacheNamespace.DEVICE, keys, SmartDeviceEntity.class);

        // 整理结果
        for (int i = 0; i < deviceIds.size() && i < batchResult.getResults().size(); i++) {
            Long deviceId = deviceIds.get(i);
            UnifiedCacheManager.CacheResult<SmartDeviceEntity> cacheResult = batchResult.getResults().get(i);

            if (cacheResult.isSuccess() && cacheResult.getData() != null) {
                result.put(deviceId, cacheResult.getData());
            }
        }

        log.debug("批量获取设备信息: 总数={}, 命中={}", deviceIds.size(), result.size());
        return result;
    }

    @Override
    public void batchCacheDeviceInfo(List<SmartDeviceEntity> devices) {
        if (CollectionUtils.isEmpty(devices)) {
            return;
        }

        Map<String, SmartDeviceEntity> keyValues = new HashMap<>();
        for (SmartDeviceEntity device : devices) {
            if (device != null && device.getDeviceId() != null) {
                String key = buildKey(CacheKey.DEVICE_INFO, device.getDeviceId().toString());
                keyValues.put(key, device);
            }
        }

        unifiedCacheManager.mSet(CacheNamespace.DEVICE, keyValues);

        log.debug("批量缓存设备信息: 数量={}", devices.size());
    }

    // ========== 设备状态缓存 ==========

    @Override
    public Integer getDeviceStatus(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        String key = buildKey(CacheKey.DEVICE_STATUS, deviceId.toString());
        UnifiedCacheManager.CacheResult<Integer> result =
            unifiedCacheManager.get(CacheNamespace.DEVICE, key, Integer.class);

        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public void setDeviceStatus(Long deviceId, Integer status) {
        if (deviceId == null || status == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_STATUS, deviceId.toString());
        unifiedCacheManager.set(CacheNamespace.DEVICE, key, status);

        log.debug("缓存设备状态: deviceId={}, status={}", deviceId, status);
    }

    @Override
    public Map<Long, Integer> batchGetDeviceStatus(List<Long> deviceIds) {
        Map<Long, Integer> result = new HashMap<>();

        if (CollectionUtils.isEmpty(deviceIds)) {
            return result;
        }

        // 先检查批量缓存
        String batchKey = buildKey(CacheKey.DEVICE_STATUS_BATCH, String.join(",", deviceIds.stream().map(String::valueOf).collect(Collectors.toList())));
        UnifiedCacheManager.CacheResult<Map<Long, Integer>> batchResult =
            unifiedCacheManager.get(CacheNamespace.DEVICE, batchKey, Map.class);

        if (batchResult.isSuccess() && batchResult.getData() != null) {
            return batchResult.getData();
        }

        // 批量获取单个状态
        List<String> keys = deviceIds.stream()
            .map(id -> buildKey(CacheKey.DEVICE_STATUS, id.toString()))
            .collect(Collectors.toList());

        UnifiedCacheManager.BatchCacheResult<Integer> statusBatchResult =
            unifiedCacheManager.mGet(CacheNamespace.DEVICE, keys, Integer.class);

        // 整理结果
        for (int i = 0; i < deviceIds.size() && i < statusBatchResult.getResults().size(); i++) {
            Long deviceId = deviceIds.get(i);
            UnifiedCacheManager.CacheResult<Integer> cacheResult = statusBatchResult.getResults().get(i);

            if (cacheResult.isSuccess() && cacheResult.getData() != null) {
                result.put(deviceId, cacheResult.getData());
            }
        }

        // 缓存批量结果
        if (!result.isEmpty()) {
            unifiedCacheManager.set(CacheNamespace.DEVICE, batchKey, result);
        }

        log.debug("批量获取设备状态: 总数={}, 命中={}", deviceIds.size(), result.size());
        return result;
    }

    @Override
    public void batchSetDeviceStatus(Map<Long, Integer> deviceStatusMap) {
        if (CollectionUtils.isEmpty(deviceStatusMap)) {
            return;
        }

        // 设置单个状态
        Map<String, Integer> keyValues = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : deviceStatusMap.entrySet()) {
            Long deviceId = entry.getKey();
            Integer status = entry.getValue();

            if (deviceId != null && status != null) {
                String key = buildKey(CacheKey.DEVICE_STATUS, deviceId.toString());
                keyValues.put(key, status);
            }
        }

        if (!keyValues.isEmpty()) {
            unifiedCacheManager.mSet(CacheNamespace.DEVICE, keyValues);
        }

        // 清除批量缓存
        String batchKey = buildKey(CacheKey.DEVICE_STATUS_BATCH, "*");
        unifiedCacheManager.deleteByPattern(CacheNamespace.DEVICE, batchKey);

        log.debug("批量设置设备状态: 数量={}", deviceStatusMap.size());
    }

    // ========== 设备列表缓存 ==========

    @Override
    public List<SmartDeviceEntity> getDeviceList(String cacheKey) {
        if (cacheKey == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.DEVICE_LIST, cacheKey);
        UnifiedCacheManager.CacheResult<List<SmartDeviceEntity>> result =
            unifiedCacheManager.get(CacheNamespace.DEVICE, key, List.class);

        return result.isSuccess() ? result.getData() : new ArrayList<>();
    }

    @Override
    public void cacheDeviceList(String cacheKey, List<SmartDeviceEntity> devices) {
        if (cacheKey == null || devices == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_LIST, cacheKey);
        unifiedCacheManager.set(CacheNamespace.DEVICE, key, devices);

        log.debug("缓存设备列表: cacheKey={}, count={}", cacheKey, devices.size());
    }

    @Override
    public List<SmartDeviceEntity> getDevicesByType(String deviceType) {
        if (deviceType == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.DEVICE_BY_TYPE, deviceType);
        UnifiedCacheManager.CacheResult<List<SmartDeviceEntity>> result =
            unifiedCacheManager.get(CacheNamespace.DEVICE, key, List.class);

        return result.isSuccess() ? result.getData() : new ArrayList<>();
    }

    @Override
    public void cacheDevicesByType(String deviceType, List<SmartDeviceEntity> devices) {
        if (deviceType == null || devices == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_BY_TYPE, deviceType);
        unifiedCacheManager.set(CacheNamespace.DEVICE, key, devices);

        log.debug("缓存按类型分组的设备列表: deviceType={}, count={}", deviceType, devices.size());
    }

    @Override
    public List<SmartDeviceEntity> getDevicesByArea(Long areaId) {
        if (areaId == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.DEVICE_BY_AREA, areaId.toString());
        UnifiedCacheManager.CacheResult<List<SmartDeviceEntity>> result =
            unifiedCacheManager.get(CacheNamespace.DEVICE, key, List.class);

        return result.isSuccess() ? result.getData() : new ArrayList<>();
    }

    @Override
    public void cacheDevicesByArea(Long areaId, List<SmartDeviceEntity> devices) {
        if (areaId == null || devices == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_BY_AREA, areaId.toString());
        unifiedCacheManager.set(CacheNamespace.DEVICE, key, devices);

        log.debug("缓存按区域分组的设备列表: areaId={}, count={}", areaId, devices.size());
    }

    // ========== 设备统计缓存 ==========

    @Override
    public Map<String, Object> getDeviceStatistics(String type) {
        if (type == null) {
            return new HashMap<>();
        }

        String key = buildKey(CacheKey.DEVICE_STATISTICS, type);
        UnifiedCacheManager.CacheResult<Map<String, Object>> result =
            unifiedCacheManager.get(CacheNamespace.DEVICE, key, Map.class);

        return result.isSuccess() ? result.getData() : new HashMap<>();
    }

    @Override
    public void cacheDeviceStatistics(String type, Map<String, Object> stats) {
        if (type == null || stats == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_STATISTICS, type);
        unifiedCacheManager.set(CacheNamespace.DEVICE, key, stats);

        log.debug("缓存设备统计信息: type={}", type);
    }

    @Override
    public Map<String, Object> getDeviceTypeStatistics() {
        String key = CacheKey.DEVICE_TYPE_STATISTICS;
        UnifiedCacheManager.CacheResult<Map<String, Object>> result =
            unifiedCacheManager.get(CacheNamespace.DEVICE, key, Map.class);

        return result.isSuccess() ? result.getData() : new HashMap<>();
    }

    @Override
    public void cacheDeviceTypeStatistics(Map<String, Object> stats) {
        if (stats == null) {
            return;
        }

        String key = CacheKey.DEVICE_TYPE_STATISTICS;
        unifiedCacheManager.set(CacheNamespace.DEVICE, key, stats);

        log.debug("缓存设备类型统计信息");
    }

    // ========== 缓存管理操作 ==========

    @Override
    public void clearDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        // 清除设备相关的所有缓存
        String devicePattern = "*:" + deviceId.toString() + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.DEVICE, devicePattern);

        // 清除设备信息缓存
        String infoKey = buildKey(CacheKey.DEVICE_INFO, deviceId.toString());
        unifiedCacheManager.delete(CacheNamespace.DEVICE, infoKey);

        // 清除设备状态缓存
        String statusKey = buildKey(CacheKey.DEVICE_STATUS, deviceId.toString());
        unifiedCacheManager.delete(CacheNamespace.DEVICE, statusKey);

        log.debug("清除设备缓存: deviceId={}", deviceId);
    }

    @Override
    public void clearDeviceStatusCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        // 清除设备状态缓存
        String statusKey = buildKey(CacheKey.DEVICE_STATUS, deviceId.toString());
        unifiedCacheManager.delete(CacheNamespace.DEVICE, statusKey);

        // 清除批量状态缓存
        String batchPattern = CacheKey.DEVICE_STATUS_BATCH + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.DEVICE, batchPattern);

        log.debug("清除设备状态缓存: deviceId={}", deviceId);
    }

    @Override
    public void clearDeviceListCache(String cacheKey) {
        if (cacheKey == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_LIST, cacheKey);
        unifiedCacheManager.delete(CacheNamespace.DEVICE, key);

        log.debug("清除设备列表缓存: cacheKey={}", cacheKey);
    }

    @Override
    public void clearDeviceTypeCache(String deviceType) {
        if (deviceType == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_BY_TYPE, deviceType);
        unifiedCacheManager.delete(CacheNamespace.DEVICE, key);

        log.debug("清除设备类型缓存: deviceType={}", deviceType);
    }

    @Override
    public void clearDeviceAreaCache(Long areaId) {
        if (areaId == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_BY_AREA, areaId.toString());
        unifiedCacheManager.delete(CacheNamespace.DEVICE, key);

        log.debug("清除设备区域缓存: areaId={}", areaId);
    }

    @Override
    public void clearDeviceStatisticsCache(String type) {
        if (type == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_STATISTICS, type);
        unifiedCacheManager.delete(CacheNamespace.DEVICE, key);

        log.debug("清除设备统计缓存: type={}", type);
    }

    @Override
    public void warmupCache() {
        log.info("开始预热设备缓存...");

        // 预热逻辑由具体的Manager类实现
        // 这里只是预留接口

        log.info("设备缓存预热完成");
    }

    @Override
    public void warmupDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        // 预热设备信息缓存会通过getDeviceInfo自动触发
        // 这里主要是确保相关缓存被正确初始化

        log.debug("预热设备缓存: deviceId={}", deviceId);
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        return unifiedCacheManager.getCacheStatistics(CacheNamespace.DEVICE);
    }

    @Override
    public void clearAllDeviceCache() {
        unifiedCacheManager.clearNamespace(CacheNamespace.DEVICE);
        log.info("清除DEVICE命名空间下的所有缓存");
    }

    // ========== 私有辅助方法 ==========

    /**
     * 构建缓存键
     *
     * @param parts 键的部分
     * @return 完整的缓存键
     */
    private String buildKey(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }

        return String.join(":", parts);
    }
}