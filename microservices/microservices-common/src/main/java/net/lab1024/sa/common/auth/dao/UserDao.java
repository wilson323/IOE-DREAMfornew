package net.lab1024.sa.common.auth.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.security.entity.UserEntity;

/**
 * 用户DAO接口（Auth模块专用）
 * <p>
 * 符合CLAUDE.md规范：
 * - 使用@Mapper注解（禁止@Repository）
 * - 使用Dao后缀（禁止Repository后缀）
 * - 使用@Resource依赖注入
 * - 完整的事务管理
 * </p>
 * <p>
 * 注意：指定Bean名称为"authUserDao"以避免与security.dao.UserDao冲突
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM t_user WHERE username = #{username} AND deleted_flag = 0")
    UserEntity selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM t_user WHERE email = #{email} AND deleted_flag = 0")
    UserEntity selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND deleted_flag = 0")
    UserEntity selectByPhone(@Param("phone") String phone);

    /**
     * 更新最后登录信息
     */
    @Update("UPDATE t_user SET last_login_time = #{loginTime}, last_login_ip = #{loginIp}, update_time = NOW() WHERE user_id = #{userId}")
    int updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime,
            @Param("loginIp") String loginIp);

    /**
     * 增加登录失败次数
     */
    @Update("UPDATE t_user SET login_fail_count = login_fail_count + 1, update_time = NOW() WHERE user_id = #{userId}")
    int increaseLoginFailCount(@Param("userId") Long userId);

    /**
     * 重置登录失败次数
     */
    @Update("UPDATE t_user SET login_fail_count = 0, update_time = NOW() WHERE user_id = #{userId}")
    int resetLoginFailCount(@Param("userId") Long userId);

    /**
     * 锁定用户
     */
    @Update("UPDATE t_user SET account_locked = 1, lock_time = #{lockTime}, status = 3, update_time = NOW() WHERE user_id = #{userId}")
    int lockUser(@Param("userId") Long userId, @Param("lockTime") LocalDateTime lockTime);

    /**
     * 解锁用户
     */
    @Update("UPDATE t_user SET account_locked = 0, lock_time = NULL, unlock_time = #{unlockTime}, login_fail_count = 0, status = 1, update_time = NOW() WHERE user_id = #{userId}")
    int unlockUser(@Param("userId") Long userId, @Param("unlockTime") LocalDateTime unlockTime);

    /**
     * 查询用户权限列表
     */
    @Select("""
                SELECT DISTINCT p.permission_code
                FROM t_permission p
                INNER JOIN t_role_permission rp ON p.permission_id = rp.permission_id
                INNER JOIN t_user_role ur ON rp.role_id = ur.role_id
                WHERE ur.user_id = #{userId}
                  AND p.status = 1
                  AND p.deleted_flag = 0
            """)
    List<String> selectUserPermissions(@Param("userId") Long userId);

    /**
     * 查询用户角色列表
     */
    @Select("""
                SELECT DISTINCT r.role_code
                FROM t_role r
                INNER JOIN t_user_role ur ON r.role_id = ur.role_id
                WHERE ur.user_id = #{userId}
                  AND r.status = 1
                  AND r.deleted_flag = 0
            """)
    List<String> selectUserRoles(@Param("userId") Long userId);
}
