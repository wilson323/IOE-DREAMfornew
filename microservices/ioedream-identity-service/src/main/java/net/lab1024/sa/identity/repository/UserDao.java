package net.lab1024.sa.identity.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.identity.domain.entity.UserEntity;

/**
 * 注意：BaseMapper已经提供了基础的CRUD方法
 * - selectById(ID) - 根据ID查找
 * - insert(T) - 新增
 * - updateById(T) - 根据ID更新
 * - deleteById(ID) - 根据ID删除
 */

/**
 * 用户数据访问接口
 * 基于现有EmployeeDao重构，扩展用户权限相关功能
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原EmployeeDao重构)
 */
@Mapper
public interface UserRepository extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查找用户（基于原EmployeeDao模式扩展）
     */
    @Select("SELECT * FROM t_employee WHERE deleted_flag = 0 AND username = #{username}")
    UserEntity findByUsername(@Param("username") String username);

    /**
     * 获取用户权限列表（基于原有权限模式扩展）
     */
    @Select("SELECT DISTINCT p.permission_code " +
            "FROM t_employee e " +
            "LEFT JOIN t_user_role ur ON e.employee_id = ur.user_id " +
            "LEFT JOIN t_role_permission rp ON ur.role_id = rp.role_id " +
            "LEFT JOIN t_permission p ON rp.permission_id = p.permission_id " +
            "WHERE e.employee_id = #{userId} AND e.deleted_flag = 0 AND p.status = 1")
    List<String> findUserPermissions(@Param("userId") Long userId);

    /**
     * 获取用户角色列表（基于原有角色模式扩展）
     */
    @Select("SELECT r.role_code " +
            "FROM t_employee e " +
            "LEFT JOIN t_user_role ur ON e.employee_id = ur.user_id " +
            "LEFT JOIN t_role r ON ur.role_id = r.role_id " +
            "WHERE e.employee_id = #{userId} AND e.deleted_flag = 0 AND r.status = 1")
    List<String> findUserRoles(@Param("userId") Long userId);

    // 兼容性方法，保持与原有EmployeeDao的兼容性
    /**
     * 根据员工ID查找（兼容性方法）
     */
    default UserEntity findByEmployeeId(Long employeeId) {
        return this.selectById(employeeId);
    }

    /**
     * 检查用户名是否存在（新增功能）
     */
    @Select("SELECT COUNT(1) FROM t_employee WHERE deleted_flag = 0 AND username = #{username} AND employee_id != #{excludeId}")
    int countByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    /**
     * 根据部门ID获取用户列表（基于原有部门查询扩展）
     */
    @Select("SELECT * FROM t_employee WHERE deleted_flag = 0 AND department_id = #{departmentId}")
    List<UserEntity> findByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(1) FROM t_employee WHERE deleted_flag = 0 AND email = #{email}")
    int existsByEmail(@Param("email") String email);

    /**
     * 保存用户（兼容JPA方法）
     */
    default UserEntity save(UserEntity user) {
        if (user.getUserId() == null) {
            insert(user);
        } else {
            updateById(user);
        }
        return user;
    }
}
