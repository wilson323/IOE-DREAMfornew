/*
 * 邮件发送结果
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 邮件发送结果
 * 封装邮件发送的执行结果
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 服务器响应消息
     */
    private String serverResponse;

    /**
     * 邮件服务提供商
     */
    private String serviceProvider;

    /**
     * 发送耗时（毫秒）
     */
    private Long sendTimeMs;

    /**
     * 额外信息
     */
    private Object extraInfo;

    /**
     * 创建成功结果
     */
    public static EmailResult success(String messageId) {
        return EmailResult.builder()
                .success(true)
                .messageId(messageId)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功结果（带完整信息）
     */
    public static EmailResult success(String messageId, String serviceProvider, String serverResponse) {
        return EmailResult.builder()
                .success(true)
                .messageId(messageId)
                .serviceProvider(serviceProvider)
                .serverResponse(serverResponse)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static EmailResult failure(String errorCode, String errorMessage) {
        return EmailResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果（带详细错误）
     */
    public static EmailResult failure(String errorCode, String errorMessage, String serverResponse, String serviceProvider) {
        return EmailResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .serverResponse(serverResponse)
                .serviceProvider(serviceProvider)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 获取结果描述
     */
    public String getResultDescription() {
        if (success) {
            return String.format("邮件发送成功 - 消息ID: %s",
                    messageId != null ? messageId : "未知");
        } else {
            return String.format("邮件发送失败 [%s] - %s",
                    errorCode != null ? errorCode : "UNKNOWN_ERROR",
                    errorMessage != null ? errorMessage : "未知错误");
        }
    }

    /**
     * 检查是否需要重试
     */
    public boolean shouldRetry() {
        if (success) {
            return false;
        }

        // 以下错误类型可以重试
        return "NETWORK_TIMEOUT".equals(errorCode) ||
               "SERVICE_UNAVAILABLE".equals(errorCode) ||
               "RATE_LIMIT_EXCEEDED".equals(errorCode) ||
               "TEMPORARY_FAILURE".equals(errorCode);
    }

    /**
     * 获取重试等待时间（秒）
     */
    public long getRetryWaitSeconds() {
        if (!shouldRetry()) {
            return 0;
        }

        // 根据错误类型确定重试时间
        switch (errorCode) {
            case "RATE_LIMIT_EXCEEDED":
                return 300; // 5分钟
            case "SERVICE_UNAVAILABLE":
                return 60;  // 1分钟
            case "NETWORK_TIMEOUT":
                return 30;  // 30秒
            default:
                return 60;  // 默认1分钟
        }
    }

    /**
     * 获取详细错误信息
     */
    public String getDetailedErrorInfo() {
        if (success) {
            return "邮件发送成功";
        }

        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("邮件发送失败:\n");
        errorInfo.append(String.format("错误代码: %s\n", errorCode != null ? errorCode : "未知"));
        errorInfo.append(String.format("错误消息: %s\n", errorMessage != null ? errorMessage : "未知"));

        if (serviceProvider != null) {
            errorInfo.append(String.format("服务提供商: %s\n", serviceProvider));
        }

        if (serverResponse != null && !serverResponse.trim().isEmpty()) {
            errorInfo.append(String.format("服务器响应: %s\n", serverResponse));
        }

        if (shouldRetry()) {
            errorInfo.append(String.format("建议重试，等待时间: %d秒\n", getRetryWaitSeconds()));
        } else {
            errorInfo.append("不建议重试，请检查邮件地址和配置\n");
        }

        return errorInfo.toString();
    }
}


