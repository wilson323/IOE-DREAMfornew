package net.lab1024.sa.admin.module.access.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.lab1024.sa.admin.module.access.domain.entity.AccessRecordEntity;
import net.lab1024.sa.admin.module.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.admin.module.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import net.lab1024.sa.base.common.cache.UnifiedCacheManager;

/**
 * 门禁系统统一缓存服务
 * <p>
 * 基于UnifiedCacheManager实现的门禁模块缓存服务，提供统一的缓存接口
 * 遵循缓存架构统一化规范：
 * - 使用ACCESS命名空间
 * - 统一缓存键格式：iog:cache:ACCESS:{namespace}:{key}
 * - 标准TTL配置：默认10分钟
 * - 支持异步操作和批量操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-23
 */
public interface AccessCacheService {

    // ========== 门禁记录缓存 ==========

    /**
     * 获取门禁记录
     *
     * @param recordId 记录ID
     * @return 门禁记录
     */
    AccessRecordEntity getAccessRecord(Long recordId);

    /**
     * 异步获取门禁记录
     *
     * @param recordId 记录ID
     * @return 异步结果
     */
    CompletableFuture<AccessRecordEntity> getAccessRecordAsync(Long recordId);

    /**
     * 缓存门禁记录
     *
     * @param record 门禁记录
     */
    void cacheAccessRecord(AccessRecordEntity record);

    /**
     * 异步缓存门禁记录
     *
     * @param record 门禁记录
     * @return 异步结果
     */
    CompletableFuture<Void> cacheAccessRecordAsync(AccessRecordEntity record);

    /**
     * 获取用户当日访问记录
     *
     * @param userId 用户ID
     * @param date   日期 yyyy-MM-dd
     * @return 访问记录列表
     */
    List<AccessRecordEntity> getUserTodayAccess(Long userId, String date);

    /**
     * 缓存用户当日访问记录
     *
     * @param userId  用户ID
     * @param date    日期
     * @param records 访问记录列表
     */
    void cacheUserTodayAccess(Long userId, String date, List<AccessRecordEntity> records);

    /**
     * 获取设备当日访问记录
     *
     * @param deviceId 设备ID
     * @param date     日期 yyyy-MM-dd
     * @return 访问记录列表
     */
    List<AccessRecordEntity> getDeviceTodayAccess(Long deviceId, String date);

    /**
     * 缓存设备当日访问记录
     *
     * @param deviceId 设备ID
     * @param date     日期
     * @param records  访问记录列表
     */
    void cacheDeviceTodayAccess(Long deviceId, String date, List<AccessRecordEntity> records);

    // ========== 区域信息缓存 ==========

    /**
     * 获取区域信息
     *
     * @param areaId 区域ID
     * @return 区域信息
     */
    AccessAreaEntity getArea(Long areaId);

    /**
     * 缓存区域信息
     *
     * @param area 区域信息
     */
    void cacheArea(AccessAreaEntity area);

    /**
     * 批量获取区域信息
     *
     * @param areaIds 区域ID列表
     * @return 区域信息映射
     */
    Map<Long, AccessAreaEntity> batchGetAreas(List<Long> areaIds);

    /**
     * 批量缓存区域信息
     *
     * @param areas 区域信息列表
     */
    void batchCacheAreas(List<AccessAreaEntity> areas);

    /**
     * 获取区域树形结构
     *
     * @param parentId      父区域ID
     * @param includeChildren 是否包含子区域
     * @return 区域树形结构
     */
    List<AccessAreaTreeVO> getAreaTree(Long parentId, Boolean includeChildren);

    /**
     * 缓存区域树形结构
     *
     * @param parentId      父区域ID
     * @param includeChildren 是否包含子区域
     * @param treeList      区域树形结构
     */
    void cacheAreaTree(Long parentId, Boolean includeChildren, List<AccessAreaTreeVO> treeList);

    /**
     * 获取子区域ID列表
     *
     * @param parentId 父区域ID
     * @return 子区域ID列表
     */
    List<Long> getChildrenIds(Long parentId);

    /**
     * 缓存子区域ID列表
     *
     * @param parentId    父区域ID
     * @param childrenIds 子区域ID列表
     */
    void cacheChildrenIds(Long parentId, List<Long> childrenIds);

    // ========== 统计数据缓存 ==========

    /**
     * 获取访问统计数据
     *
     * @param date 日期 yyyy-MM-dd
     * @return 统计信息
     */
    Map<String, Object> getAccessStats(String date);

    /**
     * 缓存访问统计数据
     *
     * @param date  日期
     * @param stats 统计信息
     */
    void cacheAccessStats(String date, Map<String, Object> stats);

    /**
     * 获取实时访问统计
     *
     * @return 实时统计信息
     */
    Map<String, Object> getRealTimeStats();

    /**
     * 更新实时统计
     *
     * @param stats 统计信息
     */
    void updateRealTimeStats(Map<String, Object> stats);

    /**
     * 获取区域统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    Map<String, Object> getAreaStatistics(Long areaId);

    /**
     * 缓存区域统计信息
     *
     * @param areaId 区域ID
     * @param stats  统计信息
     */
    void cacheAreaStatistics(Long areaId, Map<String, Object> stats);

    // ========== 权限缓存 ==========

    /**
     * 检查用户访问权限
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     * @return 权限检查结果
     */
    Map<String, Object> checkUserPermission(Long userId, Long deviceId);

    /**
     * 缓存用户权限信息
     *
     * @param userId 用户ID
     * @param permission 权限信息
     */
    void cacheUserPermission(Long userId, Map<String, Object> permission);

    // ========== 缓存管理操作 ==========

    /**
     * 清除用户相关缓存
     *
     * @param userId 用户ID
     */
    void clearUserCache(Long userId);

    /**
     * 清除设备相关缓存
     *
     * @param deviceId 设备ID
     */
    void clearDeviceCache(Long deviceId);

    /**
     * 清除区域相关缓存
     *
     * @param areaId 区域ID
     */
    void clearAreaCache(Long areaId);

    /**
     * 清除区域树相关缓存
     */
    void clearAreaTreeCache();

    /**
     * 清除日期相关缓存
     *
     * @param date 日期
     */
    void clearDateCache(String date);

    /**
     * 清除实时统计缓存
     */
    void clearRealTimeStatsCache();

    /**
     * 清除区域统计信息缓存
     *
     * @param areaId 区域ID
     */
    void clearAreaStatisticsCache(Long areaId);

    /**
     * 预热缓存
     */
    void warmupCache();

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    Map<String, Object> getCacheStatistics();

    /**
     * 清理ACCESS命名空间下的所有缓存
     */
    void clearAllAccessCache();

    /**
     * 移除指定缓存项
     *
     * @param cacheKey 缓存键
     */
    void removeCache(String cacheKey);
}