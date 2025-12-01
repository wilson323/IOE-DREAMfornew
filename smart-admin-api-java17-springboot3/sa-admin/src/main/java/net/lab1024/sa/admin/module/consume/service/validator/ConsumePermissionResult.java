package net.lab1024.sa.admin.module.consume.service.validator;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消费权限验证结果
 *
 * @author OpenSpec Task 1.2 Implementation
 * @version 1.0
 * @since 2025-11-17
 */
@Data
public class ConsumePermissionResult {

    /**
     * 是否验证成功
     */
    private boolean success;

    /**
     * 验证错误消息
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 验证时间
     */
    private LocalDateTime validateTime;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 消费金额
     */
    private java.math.BigDecimal amount;

    /**
     * 权限检查详情
     */
    private Map<String, Object> permissionDetails;

    /**
     * 权限级别
     */
    private String permissionLevel;

    /**
     * 验证规则
     */
    private String validateRule;

    /**
     * 创建成功的验证结果
     */
    public static ConsumePermissionResult success() {
        ConsumePermissionResult result = new ConsumePermissionResult();
        result.setSuccess(true);
        result.setValidateTime(LocalDateTime.now());
        return result;
    }

    /**
     * 创建成功的验证结果（带详情）
     */
    public static ConsumePermissionResult success(Long personId, String deviceId, java.math.BigDecimal amount) {
        ConsumePermissionResult result = success();
        result.setPersonId(personId);
        result.setDeviceId(deviceId);
        result.setAmount(amount);
        return result;
    }

    /**
     * 创建失败的验证结果
     */
    public static ConsumePermissionResult fail(String errorMessage, String errorCode) {
        ConsumePermissionResult result = new ConsumePermissionResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setErrorCode(errorCode);
        result.setValidateTime(LocalDateTime.now());
        return result;
    }

    /**
     * 创建失败的验证结果（带详情）
     */
    public static ConsumePermissionResult fail(String errorMessage, String errorCode, Long personId, String deviceId) {
        ConsumePermissionResult result = fail(errorMessage, errorCode);
        result.setPersonId(personId);
        result.setDeviceId(deviceId);
        return result;
    }

    /**
     * 检查是否有权限错误
     */
    public boolean hasPermissionError() {
        return !success && (errorCode != null && errorCode.startsWith("PERMISSION_"));
    }

    /**
     * 检查是否是系统错误
     */
    public boolean hasSystemError() {
        return !success && (errorCode != null && errorCode.startsWith("SYSTEM_"));
    }

    /**
     * 获取错误摘要
     */
    public String getErrorSummary() {
        if (success) {
            return "验证通过";
        }
        return String.format("验证失败: %s (错误码: %s)",
            errorMessage != null ? errorMessage : "未知错误",
            errorCode != null ? errorCode : "UNKNOWN");
    }

    /**
     * 构造函数
     */
    public ConsumePermissionResult() {
        this.validateTime = LocalDateTime.now();
    }
}