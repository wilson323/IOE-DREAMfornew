package net.lab1024.sa.base.module.support.rbac.service;

import net.lab1024.sa.base.module.support.rbac.domain.entity.AreaPersonEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 区域权限验证服务接口
 * <p>
 * 提供集中的区域权限验证和管理功能，支持区域层次结构、时间范围、数据域等高级权限控制
 *
 * @author SmartAdmin Team
 * @date 2025/11/25
 */
public interface AreaPermissionService {

    // ==================== 权限验证核心方法 ====================

    /**
     * 检查用户是否具有指定区域权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否具有权限
     */
    boolean hasAreaPermission(Long userId, Long areaId);

    /**
     * 检查用户是否具有指定区域路径权限
     *
     * @param userId 用户ID
     * @param areaPath 区域路径
     * @return 是否具有权限
     */
    boolean hasAreaPathPermission(Long userId, String areaPath);

    /**
     * 获取用户所有授权的区域ID列表
     *
     * @param userId 用户ID
     * @return 授权区域ID列表
     */
    List<Long> getUserAuthorizedAreaIds(Long userId);

    /**
     * 获取用户在指定数据域范围内的区域权限
     *
     * @param userId 用户ID
     * @param dataScope 数据域
     * @return 区域ID列表
     */
    List<Long> getUserAreaIdsByDataScope(Long userId, String dataScope);

    // ==================== 批量权限管理方法 ====================

    /**
     * 批量授权用户区域权限
     *
     * @param userId 用户ID
     * @param areaIds 区域ID列表
     * @param dataScope 数据域
     * @param effectiveTime 生效时间
     * @param expireTime 失效时间
     * @return 授权结果
     */
    boolean batchGrantAreaPermissions(Long userId, List<Long> areaIds, String dataScope,
                                     LocalDateTime effectiveTime, LocalDateTime expireTime);

    /**
     * 批量撤销用户区域权限
     *
     * @param userId 用户ID
     * @param areaIds 区域ID列表
     * @return 撤销结果
     */
    boolean batchRevokeAreaPermissions(Long userId, List<Long> areaIds);

    /**
     * 批量更新用户区域权限状态
     *
     * @param userId 用户ID
     * @param areaIds 区域ID列表
     * @param status 状态
     * @return 更新结果
     */
    boolean batchUpdatePermissionStatus(Long userId, List<Long> areaIds, Integer status);

    // ==================== 区域层次结构方法 ====================

    /**
     * 获取用户所有授权区域（包括子区域）
     *
     * @param userId 用户ID
     * @return 所有授权区域ID列表（包含子区域）
     */
    List<Long> getAllAuthorizedAreaIds(Long userId);

    /**
     * 获取用户直接授权的区域（不包含子区域）
     *
     * @param userId 用户ID
     * @return 直接授权区域ID列表
     */
    List<Long> getDirectAuthorizedAreaIds(Long userId);

    /**
     * 根据区域路径前缀获取用户权限
     *
     * @param userId 用户ID
     * @param areaPathPrefix 区域路径前缀
     * @return 匹配的区域ID列表
     */
    List<Long> getAreaIdsByPathPrefix(Long userId, String areaPathPrefix);

    /**
     * 获取用户授权的区域路径列表
     *
     * @param userId 用户ID
     * @return 区域路径列表
     */
    List<String> getUserAuthorizedAreaPaths(Long userId);

    // ==================== 时间范围权限管理 ====================

    /**
     * 获取用户在指定时间范围内的有效权限
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 有效权限列表
     */
    List<AreaPersonEntity> getEffectivePermissionsByTimeRange(Long userId,
                                                              LocalDateTime startTime,
                                                              LocalDateTime endTime);

    /**
     * 检查用户在指定时间是否具有区域权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param checkTime 检查时间
     * @return 是否具有权限
     */
    boolean hasAreaPermissionAtTime(Long userId, Long areaId, LocalDateTime checkTime);

    /**
     * 获取即将过期的权限（指定天数内）
     *
     * @param days 天数
     * @return 即将过期的权限列表
     */
    List<AreaPersonEntity> getExpiringPermissions(Integer days);

    /**
     * 清理过期权限
     *
     * @return 清理的权限数量
     */
    int cleanExpiredPermissions();

    // ==================== 统计和分析方法 ====================

    /**
     * 统计用户的区域权限数量
     *
     * @param userId 用户ID
     * @return 权限数量
     */
    int countUserAreaPermissions(Long userId);

    /**
     * 统计指定区域的授权用户数量
     *
     * @param areaId 区域ID
     * @return 授权用户数量
     */
    int countUserPermissionsByArea(Long areaId);

    /**
     * 获取区域权限统计信息
     *
     * @return 权限统计结果
     */
    List<AreaPersonEntity> getAreaPermissionStatistics();

    // ==================== 权限继承和传播方法 ====================

    /**
     * 检查用户是否具有父区域权限（用于权限继承）
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否具有父区域权限
     */
    boolean hasParentAreaPermission(Long userId, Long areaId);

    /**
     * 获取用户的所有父区域权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 父区域ID列表
     */
    List<Long> getParentAreaPermissions(Long userId, Long areaId);

    /**
     * 传播权限到子区域
     *
     * @param userId 用户ID
     * @param parentAreaId 父区域ID
     * @param includeChildren 是否包含子区域
     * @return 传播结果
     */
    boolean propagatePermissionToChildren(Long userId, Long parentAreaId, boolean includeChildren);

    /**
     * 检查区域层次结构是否存在循环引用
     *
     * @param areaId 区域ID
     * @param newParentId 新的父区域ID
     * @return 是否存在循环引用
     */
    boolean checkCircularReference(Long areaId, Long newParentId);

    /**
     * 获取区域的完整层次路径
     *
     * @param areaId 区域ID
     * @return 层次路径ID列表
     */
    List<Long> getAreaHierarchyPath(Long areaId);

    /**
     * 获取区域的所有子区域ID（递归）
     *
     * @param parentAreaId 父区域ID
     * @return 所有子区域ID列表
     */
    List<Long> getAllChildAreaIds(Long parentAreaId);

    // ==================== 缓存管理方法 ====================

    /**
     * 清除用户区域权限缓存
     *
     * @param userId 用户ID
     */
    void clearUserPermissionCache(Long userId);

    /**
     * 清除指定区域的权限缓存
     *
     * @param areaId 区域ID
     */
    void clearAreaPermissionCache(Long areaId);

    /**
     * 清除所有权限缓存
     */
    void clearAllPermissionCache();

    /**
     * 预热用户权限缓存
     *
     * @param userId 用户ID
     */
    void warmupUserPermissionCache(Long userId);

    // ==================== 批量查询优化方法 ====================

    /**
     * 批量检查多个用户的区域权限
     *
     * @param userAreaIds 用户区域ID映射 Map<UserId, List<AreaId>>
     * @return 权限检查结果 Map<UserId, Map<AreaId, Boolean>>
     */
    java.util.Map<Long, java.util.Map<Long, Boolean>> batchCheckAreaPermissions(
            java.util.Map<Long, List<Long>> userAreaIds);

    /**
     * 批量获取用户所有授权区域ID
     *
     * @param userIds 用户ID列表
     * @return 用户授权区域映射 Map<UserId, List<AreaId>>
     */
    java.util.Map<Long, List<Long>> batchGetUserAuthorizedAreaIds(List<Long> userIds);

    /**
     * 批量检查用户区域权限状态
     *
     * @param userIds 用户ID列表
     * @param areaIds 区域ID列表
     * @return 权限状态列表
     */
    List<AreaPersonEntity> batchGetPermissionStatus(List<Long> userIds, List<Long> areaIds);

    /**
     * 批量获取用户区域权限数量
     *
     * @param userIds 用户ID列表
     * @return 权限数量映射 Map<UserId, Integer>
     */
    java.util.Map<Long, Integer> batchCountUserAreaPermissions(List<Long> userIds);

    /**
     * 批量更新用户权限缓存状态
     *
     * @param userIds 用户ID列表
     * @return 更新结果
     */
    boolean batchRefreshUserPermissionCache(List<Long> userIds);

    /**
     * 获取指定区域的所有子区域ID（批量）
     *
     * @param parentAreaIds 父区域ID列表
     * @return 子区域映射 Map<ParentAreaId, List<ChildAreaId>>
     */
    java.util.Map<Long, List<Long>> batchGetAllChildAreaIds(List<Long> parentAreaIds);

    /**
     * 批量获取区域层次路径
     *
     * @param areaIds 区域ID列表
     * @return 层次路径映射 Map<AreaId, List<PathAreaId>>
     */
    java.util.Map<Long, List<Long>> batchGetAreaHierarchyPaths(List<Long> areaIds);
}