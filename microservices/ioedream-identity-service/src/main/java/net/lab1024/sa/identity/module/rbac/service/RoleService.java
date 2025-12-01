package net.lab1024.sa.identity.module.rbac.service;

import java.util.List;

import net.lab1024.sa.identity.module.rbac.domain.entity.RbacRoleEntity;

/**
 * 角色管理服务
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
public interface RoleService {

    /**
     * 获取角色列表
     *
     * @param status 状态筛选
     * @return 角色列表
     */
    List<RbacRoleEntity> getRoleList(Integer status);

    /**
     * 根据ID获取角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    RbacRoleEntity getRoleById(Long roleId);

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 角色ID
     */
    Long createRole(RbacRoleEntity role);

    /**
     * 更新角色
     *
     * @param roleId 角色ID
     * @param role   角色信息
     * @return 更新结果
     */
    boolean updateRole(Long roleId, RbacRoleEntity role);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 删除结果
     */
    boolean deleteRole(Long roleId);

    /**
     * 分配角色权限
     *
     * @param roleId       角色ID
     * @param resourceIds 资源ID列表
     * @return 分配结果
     */
    boolean assignRolePermissions(Long roleId, List<Long> resourceIds);

    /**
     * 获取角色权限
     *
     * @param roleId 角色ID
     * @return 权限资源列表
     */
    List<RbacRoleEntity> getRolePermissions(Long roleId);

    /**
     * 用户分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 分配结果
     */
    boolean assignUserRoles(Long userId, List<Long> roleIds);
}