package net.lab1024.sa.common.organization.manager;

import net.lab1024.sa.common.organization.dao.UserAreaPermissionDao;
import net.lab1024.sa.common.organization.entity.UserAreaPermissionEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户区域权限管理类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在配置类中注册为Bean
 * </p>
 * <p>
 * 核心职责：
 * - 用户区域权限的复杂业务逻辑编排
 * - 权限验证和查询
 * - 权限有效期管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public class UserAreaPermissionManager {

    private final UserAreaPermissionDao userAreaPermissionDao;

    /**
     * 构造函数注入依赖
     *
     * @param userAreaPermissionDao 用户区域权限DAO
     */
    public UserAreaPermissionManager(UserAreaPermissionDao userAreaPermissionDao) {
        this.userAreaPermissionDao = userAreaPermissionDao;
    }

    /**
     * 根据用户ID和区域ID查询权限
     * <p>
     * 对应文档中的permissionDao.selectByUserAndArea()方法
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 权限实体，不存在返回null
     */
    public UserAreaPermissionEntity getPermissionByUserAndArea(Long userId, Long areaId) {
        return userAreaPermissionDao.selectByUserAndArea(userId, areaId);
    }

    /**
     * 验证用户是否有区域权限
     * <p>
     * 检查权限是否存在、是否有效、是否在有效期内
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    public boolean hasAreaPermission(Long userId, Long areaId) {
        // 使用DAO的查询方法，自动处理有效期检查
        return userAreaPermissionDao.hasPermission(userId, areaId);
    }

    /**
     * 验证用户是否有区域权限（带时间检查）
     * <p>
     * 检查权限是否存在、是否有效、是否在有效期内
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param currentTime 当前时间
     * @return 是否有权限
     */
    public boolean hasAreaPermissionAtTime(Long userId, Long areaId, LocalDateTime currentTime) {
        UserAreaPermissionEntity permission = userAreaPermissionDao.selectValidPermission(userId, areaId, currentTime);
        return permission != null;
    }

    /**
     * 获取用户的所有区域权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<UserAreaPermissionEntity> getUserPermissions(Long userId) {
        return userAreaPermissionDao.selectByUserId(userId);
    }

    /**
     * 获取区域的所有用户权限
     *
     * @param areaId 区域ID
     * @return 权限列表
     */
    public List<UserAreaPermissionEntity> getAreaPermissions(Long areaId) {
        return userAreaPermissionDao.selectByAreaId(areaId);
    }

    /**
     * 获取有效的权限（未过期）
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 权限实体，不存在或已过期返回null
     */
    public UserAreaPermissionEntity getValidPermission(Long userId, Long areaId) {
        return userAreaPermissionDao.selectValidPermission(userId, areaId, LocalDateTime.now());
    }

    /**
     * 检查权限是否过期
     *
     * @param permission 权限实体
     * @return 是否过期
     */
    public boolean isPermissionExpired(UserAreaPermissionEntity permission) {
        if (permission == null) {
            return true;
        }

        // 永久权限不过期
        if (UserAreaPermissionEntity.PermissionType.ALWAYS.equals(permission.getPermissionType())) {
            return false;
        }

        // 限时权限检查结束时间
        if (permission.getEndTime() != null) {
            return LocalDateTime.now().isAfter(permission.getEndTime());
        }

        return false;
    }

    /**
     * 批量失效过期权限
     *
     * @return 失效的权限数量
     */
    public int expireInvalidPermissions() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<UserAreaPermissionEntity> expiredPermissions = userAreaPermissionDao.selectExpiredPermissions(currentTime);

        if (expiredPermissions.isEmpty()) {
            return 0;
        }

        List<Long> permissionIds = expiredPermissions.stream()
                .map(UserAreaPermissionEntity::getId)
                .toList();

        return userAreaPermissionDao.batchUpdateStatus(permissionIds, UserAreaPermissionEntity.Status.INVALID);
    }
}
