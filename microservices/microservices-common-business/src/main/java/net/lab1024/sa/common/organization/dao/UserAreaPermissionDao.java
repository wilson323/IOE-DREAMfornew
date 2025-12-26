package net.lab1024.sa.common.organization.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.UserAreaPermissionEntity;

/**
 * 用户区域权限DAO（遗留）
 * <p>
 * 对应数据库表: t_access_user_permission
 * 遗留DAO，容易与门禁设备权限混用，门禁侧请使用AccessUserPermissionDao。
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Deprecated
@Mapper
public interface UserAreaPermissionDao extends BaseMapper<UserAreaPermissionEntity> {

    /**
     * 根据用户ID和区域ID查询权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 权限实体，如果不存在则返回null
     */
    default UserAreaPermissionEntity selectByUserAndArea(Long userId, Long areaId) {
        return this.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAreaPermissionEntity>()
                        .eq(UserAreaPermissionEntity::getUserId, userId)
                        .eq(UserAreaPermissionEntity::getAreaId, areaId)
                        .eq(UserAreaPermissionEntity::getDeleted, false)
                        .last("LIMIT 1"));
    }

    /**
     * 根据区域ID查询权限列表
     *
     * @param areaId 区域ID
     * @return 权限列表
     */
    default List<UserAreaPermissionEntity> selectByAreaId(Long areaId) {
        return this.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAreaPermissionEntity>()
                        .eq(UserAreaPermissionEntity::getAreaId, areaId)
                        .eq(UserAreaPermissionEntity::getDeleted, false));
    }

    /**
     * 检查用户是否有区域权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    default boolean hasPermission(Long userId, Long areaId) {
        UserAreaPermissionEntity permission = selectByUserAndArea(userId, areaId);
        return permission != null && permission.getPermissionStatus() != null && permission.getPermissionStatus() == 1;
    }
}
