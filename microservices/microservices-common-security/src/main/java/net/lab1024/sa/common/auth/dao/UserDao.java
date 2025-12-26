package net.lab1024.sa.common.auth.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.UserEntity;

/**
 * 用户数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    UserEntity selectByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    UserEntity selectById(@Param("userId") Long userId);

    /**
     * 查询用户权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> selectUserPermissions(@Param("userId") Long userId);

    /**
     * 查询用户角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<String> selectUserRoles(@Param("userId") Long userId);

    /**
     * 更新最后登录信息
     *
     * @param userId 用户ID
     * @param lastLoginTime 最后登录时间
     * @param loginIp 登录IP
     */
    void updateLastLogin(@Param("userId") Long userId, 
                         @Param("lastLoginTime") LocalDateTime lastLoginTime,
                         @Param("loginIp") String loginIp);
}

