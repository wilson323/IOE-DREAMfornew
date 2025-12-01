package net.lab1024.sa.identity.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.identity.domain.entity.UserEntity;
import net.lab1024.sa.identity.service.UserService;

/**
 * 用户管理控制器
 * 基于现有EmployeeController重构，扩展为身份权限服务核心控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原EmployeeController重构)
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    @PreAuthorize("hasAuthority('user:list')")
    public ResponseDTO<PageResult<UserEntity>> getUserList(@Valid PageParam pageParam,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long departmentId) {
        return ResponseDTO.ok(userService.getUserPage(pageParam, realName, status));
    }

    @GetMapping("/detail/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取详细信息")
    @PreAuthorize("hasAuthority('user:detail')")
    public ResponseDTO<UserEntity> getUserDetail(@PathVariable @NotNull Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping("/add")
    @Operation(summary = "新增用户", description = "新增用户信息")
    @PreAuthorize("hasAuthority('user:add')")
    public ResponseDTO<String> addUser(@RequestBody @Valid UserEntity user) {
        return userService.createUser(user, null);
    }

    @PostMapping("/update")
    @Operation(summary = "更新用户", description = "更新用户信息")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseDTO<String> updateUser(@RequestBody @Valid UserEntity user) {
        return userService.updateUser(user, null);
    }

    @PostMapping("/delete/{userId}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseDTO<String> deleteUser(@PathVariable @NotNull Long userId) {
        return userService.deleteUser(userId);
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改登录密码")
    @PreAuthorize("hasAuthority('user:change-password')")
    public ResponseDTO<Void> changePassword(@RequestParam @NotNull Long userId,
            @RequestParam @NotNull String oldPassword,
            @RequestParam @NotNull String newPassword,
            @RequestParam @NotNull String confirmPassword) {
        // 验证新密码和确认密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            return ResponseDTO.error("新密码和确认密码不一致");
        }

        ResponseDTO<String> result = userService.resetPassword(userId, newPassword);
        return Boolean.TRUE.equals(result.getOk()) ? ResponseDTO.ok() : ResponseDTO.error(result.getMsg());
    }

    @PostMapping("/status/{userId}")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PreAuthorize("hasAuthority('user:update-status')")
    public ResponseDTO<Void> updateUserStatus(@PathVariable @NotNull Long userId,
            @RequestParam @NotNull Integer status) {
        ResponseDTO<String> result = userService.updateUserStatus(userId, status);
        return Boolean.TRUE.equals(result.getOk()) ? ResponseDTO.ok() : ResponseDTO.error(result.getMsg());
    }

    @GetMapping("/roles/{userId}")
    @Operation(summary = "获取用户角色", description = "获取指定用户的所有角色")
    @PreAuthorize("hasAuthority('user:detail')")
    public ResponseDTO<Set<String>> getUserRoles(@PathVariable @NotNull Long userId) {
        return ResponseDTO.ok(userService.getUserRoles(userId));
    }

    @GetMapping("/permissions/{userId}")
    @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限")
    @PreAuthorize("hasAuthority('user:detail')")
    public ResponseDTO<Set<String>> getUserPermissions(@PathVariable @NotNull Long userId) {
        return ResponseDTO.ok(userService.getUserPermissions(userId));
    }

    @GetMapping("/check-permission")
    @Operation(summary = "检查权限", description = "检查用户是否具有指定权限")
    public ResponseDTO<Map<String, Object>> checkPermission(@RequestParam @NotNull Long userId,
            @RequestParam @NotNull String permission) {
        Set<String> permissions = userService.getUserPermissions(userId);
        boolean hasPermission = permissions.contains(permission);

        Map<String, Object> result = new HashMap<>();
        result.put("hasPermission", hasPermission);
        result.put("permission", permission);

        return ResponseDTO.ok(result);
    }

    @GetMapping("/check-role")
    @Operation(summary = "检查角色", description = "检查用户是否具有指定角色")
    public ResponseDTO<Map<String, Object>> checkRole(@RequestParam @NotNull Long userId,
            @RequestParam @NotNull String roleCode) {
        Set<String> roles = userService.getUserRoles(userId);
        boolean hasRole = roles.contains(roleCode);

        Map<String, Object> result = new HashMap<>();
        result.put("hasRole", hasRole);
        result.put("role", roleCode);

        return ResponseDTO.ok(result);
    }

    // 兼容性接口，保持与原EmployeeController的兼容性
    /**
     * 获取员工列表（兼容性接口）
     */
    @GetMapping("/employee/list")
    @Operation(summary = "获取员工列表", description = "分页获取员工列表（兼容性接口）")
    public ResponseDTO<PageResult<UserEntity>> getEmployeeList(@Valid PageParam pageParam,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long departmentId) {
        return getUserList(pageParam, employeeName, status, departmentId);
    }

    /**
     * 获取员工详情（兼容性接口）
     */
    @GetMapping("/employee/detail/{employeeId}")
    @Operation(summary = "获取员工详情", description = "根据员工ID获取详细信息（兼容性接口）")
    public ResponseDTO<UserEntity> getEmployeeDetail(@PathVariable @NotNull Long employeeId) {
        return getUserDetail(employeeId);
    }

    /**
     * 新增员工（兼容性接口）
     */
    @PostMapping("/employee/add")
    @Operation(summary = "新增员工", description = "新增员工信息（兼容性接口）")
    public ResponseDTO<String> addEmployee(@RequestBody @Valid UserEntity employee) {
        return addUser(employee);
    }

    /**
     * 更新员工（兼容性接口）
     */
    @PostMapping("/employee/update")
    @Operation(summary = "更新员工", description = "更新员工信息（兼容性接口）")
    public ResponseDTO<String> updateEmployee(@RequestBody @Valid UserEntity employee) {
        return updateUser(employee);
    }

    /**
     * 删除员工（兼容性接口）
     */
    @PostMapping("/employee/delete/{employeeId}")
    @Operation(summary = "删除员工", description = "根据员工ID删除员工（兼容性接口）")
    public ResponseDTO<String> deleteEmployee(@PathVariable @NotNull Long employeeId) {
        return deleteUser(employeeId);
    }

    /**
     * 部门下拉（兼容性接口）
     */
    @GetMapping("/departments")
    @Operation(summary = "部门下拉", description = "返回部门树列表（占位，后续接入系统部门模块）")
    public ResponseDTO<List<?>> getDepartments() {
        return ResponseDTO.ok(List.of());
    }
}
