package net.lab1024.sa.identity.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.identity.domain.entity.UserEntity;

/**
 * 用户数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND delete_flag = 0")
    UserEntity findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM user WHERE email = #{email} AND delete_flag = 0")
    UserEntity findByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(1) FROM user WHERE username = #{username} AND delete_flag = 0")
    int countByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(1) FROM user WHERE email = #{email} AND delete_flag = 0")
    int countByEmail(@Param("email") String email);

    /**
     * 获取用户权限
     */
    @Select("SELECT DISTINCT p.permission_code FROM user u " +
            "LEFT JOIN user_role ur ON u.user_id = ur.user_id " +
            "LEFT JOIN role r ON ur.role_id = r.role_id " +
            "LEFT JOIN role_resource rr ON r.role_id = rr.role_id " +
            "LEFT JOIN resource p ON rr.resource_id = p.resource_id " +
            "WHERE u.user_id = #{userId} AND u.delete_flag = 0 " +
            "AND ur.delete_flag = 0 AND r.delete_flag = 0 " +
            "AND rr.delete_flag = 0 AND p.delete_flag = 0 " +
            "AND p.status = 1")
    List<String> findUserPermissions(@Param("userId") Long userId);

    /**
     * 搜索用户
     */
    @Select("SELECT * FROM user WHERE (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND delete_flag = 0 " +
            "AND (#{status} IS NULL OR status = #{status})")
    List<UserEntity> searchUsers(@Param("keyword") String keyword, @Param("status") Integer status);
}
