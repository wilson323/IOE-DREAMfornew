package net.lab1024.sa.common.permission.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.common.permission.service.UnifiedPermissionService;
import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 统一权限验证使用示例
 * <p>
 * 展示企业级统一权限验证机制的各种使用方式，包括：
 * - 声明式权限验证
 * - 编程式权限验证
 * - 复合条件验证
 * - 数据权限验证
 * - 区域和设备权限验证
 * - 权限管理和审计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/permission/example")
public class PermissionUsageExample {

    @Resource
    private UnifiedPermissionService unifiedPermissionService;

    // ==================== 声明式权限验证示例 ====================

    /**
     * 单权限验证示例
     */
    @PermissionCheck(value = "USER_VIEW", description = "查看用户信息")
    @GetMapping("/users/{userId}")
    public Map<String, Object> getUser(@PathVariable Long userId) {
        log.info("[权限示例] 查看用户信息, userId={}", userId);

        return Map.of(
            "userId", userId,
            "username", "user_" + userId,
            "email", "user" + userId + "@example.com",
            "status", "active"
        );
    }

    /**
     * 多权限验证示例（任一满足）
     */
    @PermissionCheck(value = {"USER_VIEW", "USER_MANAGE"}, mode = PermissionCheck.PermissionMode.ANY,
                         description = "查看或管理用户信息")
    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        log.info("[权限示例] 查看用户列表");

        List<Map<String, Object>> users = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            users.add(Map.of(
                "userId", i,
                "username", "user_" + i,
                "email", "user" + i + "@example.com",
                "status", "active"
            ));
        }

        return users;
    }

    /**
     * 多权限验证示例（全部满足）
     */
    @PermissionCheck(value = {"USER_MANAGE", "USER_EDIT"}, mode = PermissionCheck.PermissionMode.ALL,
                         description = "管理并编辑用户信息")
    @PutMapping("/users/{userId}")
    public Map<String, Object> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> userData) {
        log.info("[权限示例] 更新用户信息, userId={}, userData={}", userId, userData);

        return Map.of(
            "userId", userId,
            "updated", true,
            "updateTime", System.currentTimeMillis()
        );
    }

    /**
     * 角色权限验证示例
     */
    @PermissionCheck(roles = {"ADMIN", "MANAGER"}, description = "管理员或经理权限")
    @DeleteMapping("/users/{userId}")
    public Map<String, Object> deleteUser(@PathVariable Long userId) {
        log.info("[权限示例] 删除用户, userId={}", userId);

        return Map.of(
            "userId", userId,
            "deleted", true,
            "deleteTime", System.currentTimeMillis()
        );
    }

    /**
     * 权限和角色组合验证示例
     */
    @PermissionCheck(value = "USER_MANAGE", roles = {"ADMIN"},
                         operator = PermissionCheck.LogicOperator.OR,
                         description = "用户管理权限或管理员角色")
    @PostMapping("/users/{userId}/activate")
    public Map<String, Object> activateUser(@PathVariable Long userId) {
        log.info("[权限示例] 激活用户, userId={}", userId);

        return Map.of(
            "userId", userId,
            "activated", true,
            "activateTime", System.currentTimeMillis()
        );
    }

    // ==================== 数据权限验证示例 ====================

    /**
     * 部门数据权限验证示例
     */
    @PermissionCheck(value = "DEPARTMENT_VIEW",
                         dataScope = PermissionCheck.DataScopeType.DEPARTMENT,
                         dataScopeParam = "departmentId",
                         description = "查看部门信息（数据权限）")
    @GetMapping("/departments/{departmentId}/users")
    public List<Map<String, Object>> getDepartmentUsers(@PathVariable Long departmentId) {
        log.info("[权限示例] 查看部门用户, departmentId={}", departmentId);

        return List.of(
            Map.of("userId", 1, "username", "user1", "departmentId", departmentId),
            Map.of("userId", 2, "username", "user2", "departmentId", departmentId),
            Map.of("userId", 3, "username", "user3", "departmentId", departmentId)
        );
    }

    /**
     * 区域权限验证示例
     */
    @PermissionCheck(value = "AREA_ACCESS",
                         areaParam = "areaId",
                         description = "区域访问权限")
    @GetMapping("/areas/{areaId}/devices")
    public List<Map<String, Object>> getAreaDevices(@PathVariable Long areaId) {
        log.info("[权限示例] 查看区域设备, areaId={}", areaId);

        return List.of(
            Map.of("deviceId", "DEV001", "deviceName", "门禁设备1", "areaId", areaId),
            Map.of("deviceId", "DEV002", "deviceName", "门禁设备2", "areaId", areaId)
        );
    }

    /**
     * 设备权限验证示例
     */
    @PermissionCheck(value = "DEVICE_CONTROL",
                         deviceParam = "deviceId",
                         description = "设备控制权限")
    @PostMapping("/devices/{deviceId}/control")
    public Map<String, Object> controlDevice(@PathVariable String deviceId, @RequestBody Map<String, Object> command) {
        log.info("[权限示例] 控制设备, deviceId={}, command={}", deviceId, command);

        return Map.of(
            "deviceId", deviceId,
            "command", command,
            "executed", true,
            "executeTime", System.currentTimeMillis()
        );
    }

    // ==================== 编程式权限验证示例 ====================

    /**
     * 编程式权限验证示例
     */
    @GetMapping("/users/{userId}/profile")
    public Map<String, Object> getUserProfile(@PathVariable Long userId) {
        log.info("[权限示例] 查看用户资料, userId={}", userId);

        // 编程式权限验证
        PermissionValidationResult result = unifiedPermissionService.validatePermission(
            getCurrentUserId(), "USER_VIEW", "user:" + userId);

        if (!result.isValid()) {
            throw new RuntimeException("权限不足: " + result.getMessage());
        }

        return Map.of(
            "userId", userId,
            "username", "user_" + userId,
            "profile", "用户详细资料信息",
            "permissions", unifiedPermissionService.getUserPermissions(getCurrentUserId()),
            "roles", unifiedPermissionService.getUserRoles(getCurrentUserId())
        );
    }

    /**
     * 复合条件验证示例
     */
    @PostMapping("/reports/generate")
    public Map<String, Object> generateReport(@RequestBody Map<String, Object> reportParams) {
        log.info("[权限示例] 生成报告, params={}", reportParams);

        Long userId = getCurrentUserId();

        // 构建复合权限条件
        PermissionCondition[] conditions = new PermissionCondition[] {
            PermissionCondition.ofPermission("REPORT_VIEW", "report"),
            PermissionCondition.ofModulePermission("REPORT", "GENERATE")
        };

        // 验证复合条件（AND操作）
        PermissionValidationResult result = unifiedPermissionService.validateConditions(
            userId, conditions, LogicOperator.AND);

        if (!result.isValid()) {
            throw new RuntimeException("权限不足: " + result.getMessage());
        }

        return Map.of(
            "reportId", UUID.randomUUID().toString(),
            "generated", true,
            "generateTime", System.currentTimeMillis(),
            "parameters", reportParams
        );
    }

    /**
     * 权限统计信息示例
     */
    @GetMapping("/permissions/stats")
    public Map<String, Object> getPermissionStats() {
        log.info("[权限示例] 获取权限统计信息");

        return Map.of(
            "validationStats", unifiedPermissionService.getValidationStats(),
            "cacheStats", "缓存统计信息（需要实现）",
            "auditStats", "审计统计信息（需要实现）"
        );
    }

    // ==================== 权限管理示例 ====================

    /**
     * 刷新用户权限缓存示例
     */
    @PostMapping("/permissions/cache/refresh/{userId}")
    public Map<String, Object> refreshUserPermissionCache(@PathVariable Long userId) {
        log.info("[权限示例] 刷新用户权限缓存, userId={}", userId);

        unifiedPermissionService.refreshUserPermissionCache(userId);

        return Map.of(
            "userId", userId,
            "refreshed", true,
            "refreshTime", System.currentTimeMillis()
        );
    }

    /**
     * 预加载用户权限示例
     */
    @PostMapping("/permissions/cache/preload/{userId}")
    public Map<String, Object> preloadUserPermissions(@PathVariable Long userId) {
        log.info("[权限示例] 预加载用户权限, userId={}", userId);

        unifiedPermissionService.preloadUserPermissions(userId);

        return Map.of(
            "userId", userId,
            "preloaded", true,
            "preloadTime", System.currentTimeMillis()
        );
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // 这里应该从请求上下文获取当前用户ID
        // 简化实现，返回固定值
        return 1L;
    }
}