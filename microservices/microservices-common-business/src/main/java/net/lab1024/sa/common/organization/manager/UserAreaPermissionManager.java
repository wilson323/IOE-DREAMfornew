package net.lab1024.sa.common.organization.manager;

import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.organization.dao.UserAreaPermissionDao;
import net.lab1024.sa.common.organization.entity.UserAreaPermissionEntity;

/**
 * 用户区域权限管理器（遗留）
 * <p>
 * 遗留管理器，容易与门禁设备权限混用，门禁侧请使用AccessUserPermissionManager。
 * 严格遵循CLAUDE.md规范：
 * - Manager类是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 处理复杂的业务逻辑编排
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Deprecated
public class UserAreaPermissionManager {

    private final UserAreaPermissionDao dao;

    /**
     * 构造函数
     *
     * @param dao 用户区域权限DAO
     */
    public UserAreaPermissionManager(UserAreaPermissionDao dao) {
        this.dao = dao;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<UserAreaPermissionEntity> getPermissionsByUserId(Long userId) {
        return dao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAreaPermissionEntity>()
                        .eq(UserAreaPermissionEntity::getUserId, userId)
                        .eq(UserAreaPermissionEntity::getDeleted, false));
    }

    /**
     * 根据区域ID查询权限
     *
     * @param areaId 区域ID
     * @return 权限列表
     */
    public List<UserAreaPermissionEntity> getPermissionsByAreaId(Long areaId) {
        return dao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAreaPermissionEntity>()
                        .eq(UserAreaPermissionEntity::getAreaId, areaId)
                        .eq(UserAreaPermissionEntity::getDeleted, false));
    }

    /**
     * 获取用户的有效权限
     * <p>
     * 查询指定用户在指定区域的有效权限，包括：
     * 1. 权限状态为启用（permission_status = 1）
     * 2. 权限在有效期内（start_time <= now <= end_time）
     * 3. 未删除（deleted = false）
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 有效权限实体，如果不存在则返回null
     */
    public UserAreaPermissionEntity getValidPermission(Long userId, Long areaId) {
        LocalDateTime now = LocalDateTime.now();

        List<UserAreaPermissionEntity> permissions = dao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAreaPermissionEntity>()
                        .eq(UserAreaPermissionEntity::getUserId, userId)
                        .eq(UserAreaPermissionEntity::getAreaId, areaId)
                        .eq(UserAreaPermissionEntity::getPermissionStatus, 1) // 启用状态
                        .eq(UserAreaPermissionEntity::getDeleted, false)
                        .and(wrapper -> wrapper
                                .isNull(UserAreaPermissionEntity::getStartTime)
                                .or()
                                .le(UserAreaPermissionEntity::getStartTime, now))
                        .and(wrapper -> wrapper
                                .isNull(UserAreaPermissionEntity::getEndTime)
                                .or()
                                .ge(UserAreaPermissionEntity::getEndTime, now))
                        .orderByDesc(UserAreaPermissionEntity::getInheritPriority)
                        .last("LIMIT 1"));

        return permissions.isEmpty() ? null : permissions.get(0);
    }
}
