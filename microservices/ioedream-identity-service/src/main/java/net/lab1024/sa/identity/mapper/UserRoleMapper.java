package net.lab1024.sa.identity.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.identity.domain.entity.RoleEntity;
import net.lab1024.sa.identity.domain.entity.UserRoleEntity;

/**
 * 用户角色关系Mapper
 * 基于现有角色管理模式重构
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原RbacUserRoleEntity模式重构)
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {

    /**
     * 根据用户ID查询角色列表
     */
    @Select("SELECT r.* FROM t_rbac_role r " +
            "INNER JOIN t_user_role ur ON r.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND ur.status = 1 AND r.status = 1 " +
            "ORDER BY r.role_id")
    List<RoleEntity> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM t_rbac_permission p " +
            "INNER JOIN t_rbac_role_permission rp ON p.permission_id = rp.permission_id " +
            "INNER JOIN t_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND ur.status = 1 AND p.status = 1 " +
            "ORDER BY p.permission_code")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色代码查询角色（基于原有查询模式）
     */
    @Select("SELECT * FROM t_rbac_role WHERE role_code = #{roleCode} AND status = 1 LIMIT 1")
    RoleEntity selectRoleByCode(@Param("roleCode") String roleCode);

    /**
     * 删除用户所有角色
     */
    @Delete("DELETE FROM t_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 检查用户角色关联是否存在
     */
    @Select("SELECT COUNT(1) FROM t_user_role WHERE user_id = #{userId} AND role_id = #{roleId} AND status = 1")
    int countByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据角色ID查询用户数量
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM t_user_role WHERE role_id = #{roleId} AND status = 1")
    int countUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询过期的用户角色
     */
    @Select("SELECT * FROM t_user_role WHERE expire_time < NOW() AND status = 1")
    List<UserRoleEntity> selectExpiredRoles();

    /**
     * 批量更新过期角色状态
     */
    @Select("UPDATE t_user_role SET status = 0 WHERE expire_time < NOW() AND status = 1")
    int updateExpiredRoles();

    // 兼容性方法，保持与原有模式的兼容

    /**
     * 根据员工ID查询角色（兼容性方法）
     */
    default List<RoleEntity> selectRolesByEmployeeId(Long employeeId) {
        return selectRolesByUserId(employeeId);
    }

    /**
     * 根据员工ID查询权限（兼容性方法）
     */
    default List<String> selectPermissionsByEmployeeId(Long employeeId) {
        return selectPermissionsByUserId(employeeId);
    }
}
