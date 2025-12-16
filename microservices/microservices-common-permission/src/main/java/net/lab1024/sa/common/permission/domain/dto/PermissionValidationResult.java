package net.lab1024.sa.common.permission.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 权限验证结果
 * <p>
 * 统一的权限验证结果封装，支持：
 * - 验证状态
 * - 权限详情
 * - 验证路径
 * - 性能指标
 * - 扩展信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionValidationResult {

    /**
     * 验证是否通过
     */
    private boolean valid;

    /**
     * 验证状态码
     * <p>
     * 200-验证通过
     * 403-权限不足
     * 401-未认证
     * 404-权限不存在
     * 500-系统错误
     * </p>
     */
    private Integer statusCode;

    /**
     * 验证消息
     */
    private String message;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 角色标识
     */
    private String role;

    /**
     * 资源标识
     */
    private String resource;

    /**
     * 验证类型
     * <p>
     * PERMISSION-权限验证
     * ROLE-角色验证
     * DATA_SCOPE-数据权限验证
     * AREA-区域权限验证
     * DEVICE-设备权限验证
     * MODULE-模块权限验证
     * </p>
     */
    private String validationType;

    /**
     * 验证耗时（毫秒）
     */
    private Long duration;

    /**
     * 验证时间
     */
    private LocalDateTime validateTime;

    /**
     * 缓存命中
     */
    private Boolean cacheHit;

    /**
     * 验证路径
     * <p>
     * 记录权限验证的完整调用路径
     * 用于调试和审计
     * </p>
     */
    private String validatePath;

    /**
     * 用户权限列表
     * <p>
     * 当前用户拥有的权限列表
     * 用于调试和日志记录
     * </p>
     */
    private Set<String> userPermissions;

    /**
     * 用户角色列表
     * <p>
     * 当前用户拥有的角色列表
     * 用于调试和日志记录
     * </p>
     */
    private Set<String> userRoles;

    /**
     * 扩展信息
     * <p>
     * JSON格式的扩展信息
     * 用于存储特定的验证上下文
     * </p>
     */
    private Map<String, Object> extendedInfo;

    /**
     * 创建成功的验证结果
     */
    public static PermissionValidationResult success() {
        return PermissionValidationResult.builder()
                .valid(true)
                .statusCode(200)
                .message("权限验证通过")
                .validateTime(LocalDateTime.now())
                .cacheHit(false)
                .build();
    }

    /**
     * 创建成功的验证结果（带信息）
     */
    public static PermissionValidationResult success(String message) {
        return PermissionValidationResult.builder()
                .valid(true)
                .statusCode(200)
                .message(message)
                .validateTime(LocalDateTime.now())
                .cacheHit(false)
                .build();
    }

    /**
     * 创建失败的验证结果
     */
    public static PermissionValidationResult failure(String code, String message) {
        return PermissionValidationResult.builder()
                .valid(false)
                .statusCode(Integer.parseInt(code))
                .message(message)
                .validateTime(LocalDateTime.now())
                .cacheHit(false)
                .build();
    }

    /**
     * 创建权限不足的结果
     */
    public static PermissionValidationResult forbidden(String permission) {
        return PermissionValidationResult.builder()
                .valid(false)
                .statusCode(403)
                .message("权限不足: " + permission)
                .permission(permission)
                .validateTime(LocalDateTime.now())
                .cacheHit(false)
                .build();
    }

    /**
     * 创建未认证的结果
     */
    public static PermissionValidationResult unauthenticated() {
        return PermissionValidationResult.builder()
                .valid(false)
                .statusCode(401)
                .message("用户未认证")
                .validateTime(LocalDateTime.now())
                .cacheHit(false)
                .build();
    }

    /**
     * 创建权限不存在的结果
     */
    public static PermissionValidationResult permissionNotFound(String permission) {
        return PermissionValidationResult.builder()
                .valid(false)
                .statusCode(404)
                .message("权限不存在: " + permission)
                .permission(permission)
                .validateTime(LocalDateTime.now())
                .cacheHit(false)
                .build();
    }

    /**
     * 创建系统错误的结果
     */
    public static PermissionValidationResult systemError(String message) {
        return PermissionValidationResult.builder()
                .valid(false)
                .statusCode(500)
                .message("系统错误: " + message)
                .validateTime(LocalDateTime.now())
                .cacheHit(false)
                .build();
    }

    /**
     * 设置用户信息
     */
    public PermissionValidationResult withUser(Long userId, Set<String> permissions, Set<String> roles) {
        this.userId = userId;
        this.userPermissions = permissions;
        this.userRoles = roles;
        return this;
    }

    /**
     * 设置权限信息
     */
    public PermissionValidationResult withPermission(String permission, String resource) {
        this.permission = permission;
        this.resource = resource;
        return this;
    }

    /**
     * 设置角色信息
     */
    public PermissionValidationResult withRole(String role, String resource) {
        this.role = role;
        this.resource = resource;
        return this;
    }

    /**
     * 设置验证类型
     */
    public PermissionValidationResult withValidationType(String validationType) {
        this.validationType = validationType;
        return this;
    }

    /**
     * 设置性能指标
     */
    public PermissionValidationResult withPerformance(Long duration, Boolean cacheHit) {
        this.duration = duration;
        this.cacheHit = cacheHit;
        return this;
    }

    /**
     * 设置验证路径
     */
    public PermissionValidationResult withValidatePath(String validatePath) {
        this.validatePath = validatePath;
        return this;
    }

    /**
     * 设置扩展信息
     */
    public PermissionValidationResult withExtendedInfo(Map<String, Object> extendedInfo) {
        this.extendedInfo = extendedInfo;
        return this;
    }

    /**
     * 判断是否为缓存命中
     */
    public boolean isCacheHit() {
        return Boolean.TRUE.equals(this.cacheHit);
    }

    /**
     * 判断是否为权限不足
     */
    public boolean isForbidden() {
        return !valid && statusCode != null && statusCode == 403;
    }

    /**
     * 判断是否为未认证
     */
    public boolean isUnauthenticated() {
        return !valid && statusCode != null && statusCode == 401;
    }

    /**
     * 判断是否为系统错误
     */
    public boolean isSystemError() {
        return !valid && statusCode != null && statusCode == 500;
    }

    /**
     * 获取状态码描述
     */
    public String getStatusCodeDescription() {
        if (statusCode == null) {
            return "未知状态";
        }
        return switch (statusCode) {
            case 200 -> "验证通过";
            case 403 -> "权限不足";
            case 401 -> "未认证";
            case 404 -> "权限不存在";
            case 500 -> "系统错误";
            default -> "未知状态: " + statusCode;
        };
    }
}