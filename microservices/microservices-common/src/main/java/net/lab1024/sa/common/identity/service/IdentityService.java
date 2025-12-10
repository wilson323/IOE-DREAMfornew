package net.lab1024.sa.common.identity.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.identity.domain.dto.UserCreateDTO;
import net.lab1024.sa.common.identity.domain.dto.UserUpdateDTO;
import net.lab1024.sa.common.identity.domain.dto.RoleCreateDTO;
import net.lab1024.sa.common.identity.domain.vo.UserDetailVO;
import net.lab1024.sa.common.identity.domain.vo.RoleDetailVO;
import net.lab1024.sa.common.identity.domain.vo.PermissionTreeVO;

import java.util.List;
import java.util.Set;

/**
 * 身份管理服务接口
 * 整合自ioedream-identity-service
 *
 * 功能职责：
 * - 用户管理（CRUD、状态管理）
 * - 角色管理（CRUD、权限分配）
 * - 权限管理（CRUD、树形结构）
 * - 用户角色关联
 * - 角色权限关联
 *
 * 企业级特性：
 * - RBAC权限模型
 * - 数据权限控制
 * - 权限缓存管理
 * - 权限变更通知
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自identity-service）
 */
public interface IdentityService {

    // ==================== 用户管理 ====================

    /**
     * 创建用户
     */
    ResponseDTO<Long> createUser(UserCreateDTO dto);

    /**
     * 更新用户
     */
    ResponseDTO<Void> updateUser(Long userId, UserUpdateDTO dto);

    /**
     * 删除用户
     */
    ResponseDTO<Void> deleteUser(Long userId);

    /**
     * 获取用户详情
     */
    ResponseDTO<UserDetailVO> getUserDetail(Long userId);

    /**
     * 启用/禁用用户
     */
    ResponseDTO<Void> updateUserStatus(Long userId, Integer status);

    /**
     * 重置用户密码
     */
    ResponseDTO<Void> resetPassword(Long userId, String newPassword);

    // ==================== 角色管理 ====================

    /**
     * 创建角色
     */
    ResponseDTO<Long> createRole(RoleCreateDTO dto);

    /**
     * 获取角色详情
     */
    ResponseDTO<RoleDetailVO> getRoleDetail(Long roleId);

    /**
     * 删除角色
     */
    ResponseDTO<Void> deleteRole(Long roleId);

    /**
     * 分配角色权限
     */
    ResponseDTO<Void> assignRolePermissions(Long roleId, List<Long> permissionIds);

    // ==================== 权限管理 ====================

    /**
     * 获取权限树
     */
    ResponseDTO<List<PermissionTreeVO>> getPermissionTree();

    /**
     * 分配用户角色
     */
    ResponseDTO<Void> assignUserRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色列表
     */
    Set<String> getUserRoles(Long userId);

    /**
     * 检查用户是否有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否有角色
     */
    boolean hasRole(Long userId, String roleCode);
}

