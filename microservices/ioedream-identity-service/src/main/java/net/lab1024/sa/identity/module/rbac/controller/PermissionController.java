package net.lab1024.sa.identity.module.rbac.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.identity.module.rbac.annotation.RequireResource;
import net.lab1024.sa.identity.module.rbac.service.PermissionService;

/**
 * 权限管理控制器
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    /**
     * 检查用户权限
     *
     * @param userId       用户ID
     * @param resourceCode 资源编码
     * @param action       操作动作
     * @return 检查结果
     */
    @GetMapping("/check")
    @RequireResource(code = "PERMISSION_CHECK", action = "READ", description = "检查用户权限")
    public ResponseDTO<Boolean> checkPermission(@RequestParam Long userId,
            @RequestParam String resourceCode,
            @RequestParam(defaultValue = "READ") String action) {
        try {
            boolean hasPermission = permissionService.hasPermission(userId, resourceCode, action);
            return ResponseDTO.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查权限异常", e);
            return ResponseDTO.userErrorParam("检查权限失败");
        }
    }

    /**
     * 检查用户是否有任意权限
     *
     * @param userId       用户ID
     * @param resourceCode 资源编码
     * @return 检查结果
     */
    @GetMapping("/check-any/{userId}")
    @RequireResource(code = "PERMISSION_CHECK_ANY", action = "READ", description = "检查用户任意权限")
    public ResponseDTO<Boolean> checkAnyPermission(@PathVariable Long userId,
            @RequestParam String resourceCode) {
        try {
            boolean hasPermission = permissionService.hasAnyPermission(userId, resourceCode);
            return ResponseDTO.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查任意权限异常", e);
            return ResponseDTO.userErrorParam("检查权限失败");
        }
    }

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限编码集合
     */
    @GetMapping("/user/{userId}")
    @RequireResource(code = "USER_PERMISSIONS", action = "READ", description = "获取用户权限列表")
    public ResponseDTO<Set<String>> getUserPermissions(@PathVariable Long userId) {
        try {
            Set<String> permissions = permissionService.getUserPermissions(userId);
            return ResponseDTO.ok(permissions);
        } catch (Exception e) {
            log.error("获取用户权限异常", e);
            return ResponseDTO.userErrorParam("获取权限失败");
        }
    }

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色编码集合
     */
    @GetMapping("/user/{userId}/roles")
    @RequireResource(code = "USER_ROLES", action = "READ", description = "获取用户角色列表")
    public ResponseDTO<Set<String>> getUserRoles(@PathVariable Long userId) {
        try {
            Set<String> roles = permissionService.getUserRoles(userId);
            return ResponseDTO.ok(roles);
        } catch (Exception e) {
            log.error("获取用户角色异常", e);
            return ResponseDTO.userErrorParam("获取角色失败");
        }
    }

    /**
     * 获取用户区域权限
     *
     * @param userId 用户ID
     * @return 区域ID集合
     */
    @GetMapping("/user/{userId}/areas")
    @RequireResource(code = "USER_AREAS", action = "READ", description = "获取用户区域权限")
    public ResponseDTO<Set<Long>> getUserAreaPermissions(@PathVariable Long userId) {
        try {
            Set<Long> areas = permissionService.getUserAreaPermissions(userId);
            return ResponseDTO.ok(areas);
        } catch (Exception e) {
            log.error("获取用户区域权限异常", e);
            return ResponseDTO.userErrorParam("获取区域权限失败");
        }
    }

    /**
     * 获取用户部门权限
     *
     * @param userId 用户ID
     * @return 部门ID集合
     */
    @GetMapping("/user/{userId}/depts")
    @RequireResource(code = "USER_DEPTS", action = "READ", description = "获取用户部门权限")
    public ResponseDTO<Set<Long>> getUserDeptPermissions(@PathVariable Long userId) {
        try {
            Set<Long> depts = permissionService.getUserDeptPermissions(userId);
            return ResponseDTO.ok(depts);
        } catch (Exception e) {
            log.error("获取用户部门权限异常", e);
            return ResponseDTO.userErrorParam("获取部门权限失败");
        }
    }

    /**
     * 批量检查权限
     *
     * @param userId          用户ID
     * @param resourceActions 资源-动作映射
     * @return 检查结果
     */
    @PostMapping("/batch-check/{userId}")
    @RequireResource(code = "BATCH_PERMISSION_CHECK", action = "READ", description = "批量检查权限")
    public ResponseDTO<Map<String, Boolean>> batchCheckPermissions(@PathVariable Long userId,
            @RequestBody Map<String, String> resourceActions) {
        try {
            Map<String, Boolean> results = permissionService.batchCheckPermissions(userId, resourceActions);
            return ResponseDTO.ok(results);
        } catch (Exception e) {
            log.error("批量检查权限异常", e);
            return ResponseDTO.userErrorParam("批量检查权限失败");
        }
    }

    /**
     * 获取权限统计信息
     *
     * @param userId 用户ID
     * @return 权限统计信息
     */
    @GetMapping("/statistics/{userId}")
    @RequireResource(code = "PERMISSION_STATISTICS", action = "READ", description = "获取权限统计信息")
    public ResponseDTO<Map<String, Object>> getPermissionStatistics(@PathVariable Long userId) {
        try {
            Map<String, Object> statistics = permissionService.getPermissionStatistics(userId);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取权限统计异常", e);
            return ResponseDTO.userErrorParam("获取权限统计失败");
        }
    }

    /**
     * 检查是否为超级管理员
     *
     * @param userId 用户ID
     * @return 是否为超级管理员
     */
    @GetMapping("/is-super-admin/{userId}")
    @RequireResource(code = "SUPER_ADMIN_CHECK", action = "READ", description = "检查超级管理员")
    public ResponseDTO<Boolean> isSuperAdmin(@PathVariable Long userId) {
        try {
            boolean isSuperAdmin = permissionService.isSuperAdmin(userId);
            return ResponseDTO.ok(isSuperAdmin);
        } catch (Exception e) {
            log.error("检查超级管理员异常", e);
            return ResponseDTO.userErrorParam("检查超级管理员失败");
        }
    }
}
