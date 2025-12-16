package net.lab1024.sa.common.permission.service;

import java.util.Set;

/**
 * 统一权限验证服务接口
 * <p>
 * 提供企业级权限管理能力，支持：
 * - 权限验证
 * - 角色验证
 * - 数据权限验证
 * - 区域权限验证
 * - 设备权限验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface UnifiedPermissionService {

    /**
     * 验证用户权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @param resource 资源标识（可选）
     * @return 权限验证结果
     */
    PermissionValidationResult validatePermission(Long userId, String permission, String resource);

    /**
     * 验证用户角色
     *
     * @param userId 用户ID
     * @param role 角色标识
     * @param resource 资源标识（可选）
     * @return 权限验证结果
     */
    PermissionValidationResult validateRole(Long userId, String role, String resource);

    /**
     * 验证用户数据权限
     *
     * @param userId 用户ID
     * @param dataType 数据类型（如：DEPARTMENT、AREA等）
     * @param resourceId 资源ID
     * @return 权限验证结果
     */
    PermissionValidationResult validateDataScope(Long userId, String dataType, Object resourceId);

    /**
     * 验证复合权限条件
     * 支持AND/OR逻辑组合
     *
     * @param userId 用户ID
     * @param conditions 权限条件列表
     * @param logicOperator 逻辑操作符
     * @return 权限验证结果
     */
    PermissionValidationResult validateConditions(Long userId, PermissionCondition[] conditions, LogicOperator logicOperator);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限标识集合
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色标识集合
     */
    Set<String> getUserRoles(Long userId);

    /**
     * 检查用户是否有区域权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasAreaPermission(Long userId, Long areaId, String permission);

    /**
     * 检查用户是否有设备权限
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasDevicePermission(Long userId, String deviceId, String permission);

    /**
     * 检查用户是否有模块权限
     *
     * @param userId 用户ID
     * @param moduleCode 模块编码
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasModulePermission(Long userId, String moduleCode, String permission);

    /**
     * 刷新用户权限缓存
     *
     * @param userId 用户ID
     */
    void refreshUserPermissionCache(Long userId);

    /**
     * 刷新用户权限缓存（批量）
     *
     * @param userIds 用户ID列表
     */
    void refreshUserPermissionCache(Set<Long> userIds);

    /**
     * 预加载用户权限
     *
     * @param userId 用户ID
     */
    void preloadUserPermissions(Long userId);

    /**
     * 获取权限验证统计信息
     *
     * @return 权限验证统计
     */
    PermissionValidationStats getValidationStats();
}