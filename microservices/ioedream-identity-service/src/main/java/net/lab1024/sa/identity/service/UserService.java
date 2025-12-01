package net.lab1024.sa.identity.service;

import java.util.List;
import java.util.Set;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.identity.domain.entity.UserEntity;

/**
 * 用户服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
public interface UserService {

    /**
     * 根据ID获取用户
     */
    ResponseDTO<UserEntity> getUserById(Long userId);

    /**
     * 根据用户名获取用户
     */
    UserEntity getUserByUsername(String username);

    /**
     * 分页查询用户
     */
    PageResult<UserEntity> getUserPage(PageParam pageParam, String keyword, Integer status);

    /**
     * 创建用户
     */
    ResponseDTO<String> createUser(UserEntity user, List<Long> roleIds);

    /**
     * 更新用户
     */
    ResponseDTO<String> updateUser(UserEntity user, List<Long> roleIds);

    /**
     * 删除用户
     */
    ResponseDTO<String> deleteUser(Long userId);

    /**
     * 启用/禁用用户
     */
    ResponseDTO<String> updateUserStatus(Long userId, Integer status);

    /**
     * 重置密码
     */
    ResponseDTO<String> resetPassword(Long userId, String newPassword);

    /**
     * 获取用户权限
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色
     */
    Set<String> getUserRoles(Long userId);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}
