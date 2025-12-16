package net.lab1024.sa.common.permission.service;

import net.lab1024.sa.common.permission.domain.vo.*;

import java.util.List;
import java.util.Set;

/**
 * 权限数据服务接口
 * <p>
 * 提供前后端权限一致性保障的核心服务：
 * - 用户权限数据的统一管理和查询
 * - 菜单权限树的构建和缓存
 * - 权限数据变更的实时检测和通知
 * - 权限数据的版本控制和增量同步
 * - 权限缓存的统一管理和失效控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface PermissionDataService {

    /**
     * 获取用户完整权限数据
     *
     * @param userId 用户ID，为空则获取当前用户
     * @return 用户权限数据VO，包含权限列表、菜单树、webPerms等
     */
    UserPermissionVO getUserPermissions(Long userId);

    /**
     * 获取菜单权限树结构
     *
     * @param userId 用户ID，为空则获取当前用户
     * @return 菜单权限树结构，包含详细权限信息
     */
    List<MenuPermissionVO> getMenuPermissions(Long userId);

    /**
     * 批量获取用户权限数据
     *
     * @param userIds 用户ID列表
     * @return 用户权限数据列表
     */
    List<UserPermissionVO> getBatchUserPermissions(List<Long> userIds);

    /**
     * 获取权限数据变更通知
     *
     * @param lastSyncTime 最后同步时间戳（毫秒）
     * @return 权限变更通知列表
     */
    List<PermissionDataVO> getPermissionChanges(Long lastSyncTime);

    /**
     * 确认权限数据同步
     *
     * @param userId 用户ID
     * @param dataVersion 数据版本号
     * @param syncType 同步类型（FULL/INCREMENTAL）
     */
    void confirmPermissionSync(Long userId, String dataVersion, String syncType);

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     */
    void clearUserPermissionCache(Long userId);

    /**
     * 批量清除权限缓存
     *
     * @param userIds 用户ID列表
     */
    void clearBatchPermissionCache(List<Long> userIds);

    /**
     * 获取权限统计数据
     *
     * @return 权限统计信息VO
     */
    PermissionStatsVO getPermissionStats();

    /**
     * 构建用户权限VO（核心方法）
     *
     * @param userId 用户ID
     * @param userPermissions 用户权限集合
     * @param userRoles 用户角色集合
     * @return 用户权限数据VO
     */
    UserPermissionVO buildUserPermissionVO(Long userId, Set<String> userPermissions, Set<String> userRoles);

    /**
     * 构建菜单权限树（核心方法）
     *
     * @param userId 用户ID
     * @param userPermissions 用户权限集合
     * @param userRoles 用户角色集合
     * @return 菜单权限树结构
     */
    List<MenuPermissionVO> buildMenuPermissionTree(Long userId, Set<String> userPermissions, Set<String> userRoles);

    /**
     * 生成webPerms权限标识
     *
     * @param permissionId 权限ID
     * @return webPerms权限标识列表
     */
    Set<String> generateWebPerms(String permissionId);

    /**
     * 检查用户是否有权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 检查用户是否有角色
     *
     * @param userId 用户ID
     * @param role 角色标识
     * @return 是否有角色
     */
    boolean hasRole(Long userId, String role);

    /**
     * 获取用户所有权限
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户所有角色
     *
     * @param userId 用户ID
     * @return 角色集合
     */
    Set<String> getUserRoles(Long userId);

    /**
     * 通知权限数据变更
     *
     * @param changeType 变更类型
     * @param targetId 目标ID（用户ID或角色ID）
     * @param affectedData 受影响的数据
     */
    void notifyPermissionChange(String changeType, Long targetId, Object affectedData);
}