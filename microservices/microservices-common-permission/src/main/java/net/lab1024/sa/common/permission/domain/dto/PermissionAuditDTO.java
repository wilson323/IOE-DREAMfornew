package net.lab1024.sa.common.permission.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 权限审计日志DTO
 * <p>
 * 统一的权限验证审计日志数据传输对象，用于记录和传递权限验证过程中的关键信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionAuditDTO {

    /**
     * 审计ID
     */
    private String auditId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 资源标识
     */
    private String resource;

    /**
     * 验证结果
     * GRANTED-通过
     * DENIED-拒绝
     */
    private String result;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 角色标识
     */
    private String role;

    /**
     * 是否敏感操作
     */
    private boolean sensitive;

    /**
     * 审计时间
     */
    private LocalDateTime auditTime;

    /**
     * 验证耗时(毫秒)
     */
    private Long duration;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 失败原因（别名）
     */
    private String failureReason;

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 是否启用增强日志
     */
    private Boolean enhancedLogging;

    /**
     * 扩展信息
     */
    private Map<String, Object> metadata;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 验证时间
     */
    private LocalDateTime validationTime;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法参数
     */
    private List<Object> methodParameters;

    /**
     * 描述
     */
    private String description;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 创建成功审计日志
     */
    public static PermissionAuditDTO success(Long userId, String operation, String resource) {
        return PermissionAuditDTO.builder()
                .userId(userId)
                .operation(operation)
                .resource(resource)
                .result("GRANTED")
                .auditTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败审计日志
     */
    public static PermissionAuditDTO denied(Long userId, String operation, String resource, String reason) {
        return PermissionAuditDTO.builder()
                .userId(userId)
                .operation(operation)
                .resource(resource)
                .result("DENIED")
                .reason(reason)
                .auditTime(LocalDateTime.now())
                .build();
    }
}
