package net.lab1024.sa.audit.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 审计日志VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class AuditLogVO {

    /**
     * 审计ID
     */
    private Long auditId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 资源类型
     */
    private String resource;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 操作结果
     */
    private String result;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应数据
     */
    private String responseData;

    /**
     * 操作时间
     */
    private LocalDateTime timestamp;

    /**
     * 执行时长(毫秒)
     */
    private Long executionTime;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 详细信息
     */
    private String details;

    /**
     * 风险等级
     */
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 租动端标识
     */
    private String mobileFlag;

    /**
     * 操作类型文本
     */
    private String operationTypeText;

    /**
     * 结果状态文本
     */
    private String resultStatusText;

    /**
     * 风险等级文本
     */
    private String riskLevelText;
}
