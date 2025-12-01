package net.lab1024.sa.admin.module.device.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import net.lab1024.sa.base.common.cache.UnifiedCacheManager;

/**
 * 设备系统统一缓存服务
 * <p>
 * 基于UnifiedCacheManager实现的设备模块缓存服务
 * 遵循缓存架构统一化规范：
 * - 使用DEVICE命名空间
 * - 统一缓存键格式：iog:cache:DEVICE:{namespace}:{key}
 * - 标准TTL配置：默认30分钟
 * - 支持异步操作和批量操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public interface DeviceCacheService {

    // ========== 设备信息缓存 ==========

    /**
     * 获取设备信息
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    SmartDeviceEntity getDeviceInfo(Long deviceId);

    /**
     * 异步获取设备信息
     *
     * @param deviceId 设备ID
     * @return 异步结果
     */
    CompletableFuture<SmartDeviceEntity> getDeviceInfoAsync(Long deviceId);

    /**
     * 缓存设备信息
     *
     * @param device 设备信息
     */
    void cacheDeviceInfo(SmartDeviceEntity device);

    /**
     * 异步缓存设备信息
     *
     * @param device 设备信息
     * @return 异步结果
     */
    CompletableFuture<Void> cacheDeviceInfoAsync(SmartDeviceEntity device);

    /**
     * 批量获取设备信息
     *
     * @param deviceIds 设备ID列表
     * @return 设备信息映射
     */
    Map<Long, SmartDeviceEntity> batchGetDeviceInfo(List<Long> deviceIds);

    /**
     * 批量缓存设备信息
     *
     * @param devices 设备信息列表
     */
    void batchCacheDeviceInfo(List<SmartDeviceEntity> devices);

    // ========== 设备状态缓存 ==========

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    Integer getDeviceStatus(Long deviceId);

    /**
     * 设置设备状态
     *
     * @param deviceId 设备ID
     * @param status   设备状态
     */
    void setDeviceStatus(Long deviceId, Integer status);

    /**
     * 批量获取设备状态
     *
     * @param deviceIds 设备ID列表
     * @return 设备状态映射
     */
    Map<Long, Integer> batchGetDeviceStatus(List<Long> deviceIds);

    /**
     * 批量设置设备状态
     *
     * @param deviceStatusMap 设备状态映射
     */
    void batchSetDeviceStatus(Map<Long, Integer> deviceStatusMap);

    // ========== 设备列表缓存 ==========

    /**
     * 获取设备列表
     *
     * @param cacheKey 缓存键
     * @return 设备列表
     */
    List<SmartDeviceEntity> getDeviceList(String cacheKey);

    /**
     * 缓存设备列表
     *
     * @param cacheKey 缓存键
     * @param devices  设备列表
     */
    void cacheDeviceList(String cacheKey, List<SmartDeviceEntity> devices);

    /**
     * 获取按类型分组的设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    List<SmartDeviceEntity> getDevicesByType(String deviceType);

    /**
     * 缓存按类型分组的设备列表
     *
     * @param deviceType 设备类型
     * @param devices    设备列表
     */
    void cacheDevicesByType(String deviceType, List<SmartDeviceEntity> devices);

    /**
     * 获取按区域分组的设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    List<SmartDeviceEntity> getDevicesByArea(Long areaId);

    /**
     * 缓存按区域分组的设备列表
     *
     * @param areaId  区域ID
     * @param devices 设备列表
     */
    void cacheDevicesByArea(Long areaId, List<SmartDeviceEntity> devices);

    // ========== 设备统计缓存 ==========

    /**
     * 获取设备统计信息
     *
     * @param type 统计类型
     * @return 统计信息
     */
    Map<String, Object> getDeviceStatistics(String type);

    /**
     * 缓存设备统计信息
     *
     * @param type   统计类型
     * @param stats  统计信息
     */
    void cacheDeviceStatistics(String type, Map<String, Object> stats);

    /**
     * 获取设备类型统计
     *
     * @return 设备类型统计
     */
    Map<String, Object> getDeviceTypeStatistics();

    /**
     * 缓存设备类型统计
     *
     * @param stats 设备类型统计
     */
    void cacheDeviceTypeStatistics(Map<String, Object> stats);

    // ========== 缓存管理操作 ==========

    /**
     * 清除设备相关缓存
     *
     * @param deviceId 设备ID
     */
    void clearDeviceCache(Long deviceId);

    /**
     * 清除设备状态缓存
     *
     * @param deviceId 设备ID
     */
    void clearDeviceStatusCache(Long deviceId);

    /**
     * 清除设备列表缓存
     *
     * @param cacheKey 缓存键
     */
    void clearDeviceListCache(String cacheKey);

    /**
     * 清除类型相关缓存
     *
     * @param deviceType 设备类型
     */
    void clearDeviceTypeCache(String deviceType);

    /**
     * 清除区域相关缓存
     *
     * @param areaId 区域ID
     */
    void clearDeviceAreaCache(Long areaId);

    /**
     * 清除统计缓存
     *
     * @param type 统计类型
     */
    void clearDeviceStatisticsCache(String type);

    /**
     * 预热缓存
     */
    void warmupCache();

    /**
     * 预热设备缓存
     *
     * @param deviceId 设备ID
     */
    void warmupDeviceCache(Long deviceId);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    Map<String, Object> getCacheStatistics();

    /**
     * 清理DEVICE命名空间下的所有缓存
     */
    void clearAllDeviceCache();
}