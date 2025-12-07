package net.lab1024.sa.identity.module.rbac.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.identity.module.rbac.annotation.RequireResource;
import net.lab1024.sa.identity.module.rbac.domain.entity.RbacRoleEntity;
import net.lab1024.sa.identity.module.rbac.service.RoleService;

/**
 * 角色管理控制器
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 获取角色列表
     *
     * @param status 状态筛选
     * @return 角色列表
     */
    @GetMapping
    @RequireResource(code = "ROLE_LIST", action = "READ", description = "获取角色列表")
    public ResponseDTO<List<RbacRoleEntity>> getRoleList(@RequestParam(required = false) Integer status) {
        try {
            List<RbacRoleEntity> roles = roleService.getRoleList(status);
            return ResponseDTO.ok(roles);
        } catch (Exception e) {
            log.error("获取角色列表异常", e);
            return ResponseDTO.error("获取角色列表失败");
        }
    }

    /**
     * 根据ID获取角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    @GetMapping("/{roleId}")
    @RequireResource(code = "ROLE_DETAIL", action = "READ", description = "获取角色详情")
    public ResponseDTO<RbacRoleEntity> getRoleById(@PathVariable Long roleId) {
        try {
            RbacRoleEntity role = roleService.getRoleById(roleId);
            if (role == null) {
                return ResponseDTO.userErrorParam("角色不存在");
            }
            return ResponseDTO.ok(role);
        } catch (Exception e) {
            log.error("获取角色详情异常", e);
            return ResponseDTO.error("获取角色详情失败");
        }
    }

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 创建结果
     */
    @PostMapping
    @RequireResource(code = "ROLE_CREATE", action = "WRITE", description = "创建角色")
    public ResponseDTO<Long> createRole(@RequestBody RbacRoleEntity role) {
        try {
            Long roleId = roleService.createRole(role);
            return ResponseDTO.ok(roleId);
        } catch (Exception e) {
            log.error("创建角色异常", e);
            return ResponseDTO.error("创建角色失败");
        }
    }

    /**
     * 更新角色
     *
     * @param roleId 角色ID
     * @param role   角色信息
     * @return 更新结果
     */
    @PutMapping("/{roleId}")
    @RequireResource(code = "ROLE_UPDATE", action = "WRITE", description = "更新角色")
    public ResponseDTO<Boolean> updateRole(@PathVariable Long roleId, @RequestBody RbacRoleEntity role) {
        try {
            boolean result = roleService.updateRole(roleId, role);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("更新角色异常", e);
            return ResponseDTO.error("更新角色失败");
        }
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{roleId}")
    @RequireResource(code = "ROLE_DELETE", action = "DELETE", description = "删除角色")
    public ResponseDTO<Boolean> deleteRole(@PathVariable Long roleId) {
        try {
            boolean result = roleService.deleteRole(roleId);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("删除角色异常", e);
            return ResponseDTO.error("删除角色失败");
        }
    }

    /**
     * 分配角色权限
     *
     * @param roleId      角色ID
     * @param resourceIds 资源ID列表
     * @return 分配结果
     */
    @PostMapping("/{roleId}/permissions")
    @RequireResource(code = "ROLE_ASSIGN_PERMISSIONS", action = "WRITE", description = "分配角色权限")
    public ResponseDTO<Boolean> assignRolePermissions(@PathVariable Long roleId,
            @RequestBody List<Long> resourceIds) {
        try {
            boolean result = roleService.assignRolePermissions(roleId, resourceIds);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分配角色权限异常", e);
            return ResponseDTO.error("分配角色权限失败");
        }
    }

    /**
     * 获取角色权限
     *
     * @param roleId 角色ID
     * @return 权限资源列表
     */
    @GetMapping("/{roleId}/permissions")
    @RequireResource(code = "ROLE_PERMISSIONS", action = "READ", description = "获取角色权限")
    public ResponseDTO<List<RbacRoleEntity>> getRolePermissions(@PathVariable Long roleId) {
        try {
            List<RbacRoleEntity> permissions = roleService.getRolePermissions(roleId);
            return ResponseDTO.ok(permissions);
        } catch (Exception e) {
            log.error("获取角色权限异常", e);
            return ResponseDTO.error("获取角色权限失败");
        }
    }

    /**
     * 用户分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 分配结果
     */
    @PostMapping("/users/{userId}/roles")
    @RequireResource(code = "USER_ASSIGN_ROLES", action = "WRITE", description = "用户分配角色")
    public ResponseDTO<Boolean> assignUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        try {
            boolean result = roleService.assignUserRoles(userId, roleIds);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("用户分配角色异常", e);
            return ResponseDTO.error("用户分配角色失败");
        }
    }
}
