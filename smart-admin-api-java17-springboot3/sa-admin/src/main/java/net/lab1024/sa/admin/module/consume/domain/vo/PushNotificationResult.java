/*
 * 推送通知结果
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
 * 推送通知结果
 * 封装App推送的执行结果
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationResult {

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
     * 成功送达的设备数量
     */
    private Integer successDeviceCount;

    /**
     * 失败送达的设备数量
     */
    private Integer failureDeviceCount;

    /**
     * 服务提供商
     */
    private String serviceProvider;

    /**
     * 推送平台（ANDROID/IOS）
     */
    private String platform;

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
    public static PushNotificationResult success(String messageId) {
        return PushNotificationResult.builder()
                .success(true)
                .messageId(messageId)
                .successDeviceCount(1)
                .failureDeviceCount(0)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功结果（带设备统计）
     */
    public static PushNotificationResult success(String messageId, Integer successCount, Integer failureCount) {
        return PushNotificationResult.builder()
                .success(true)
                .messageId(messageId)
                .successDeviceCount(successCount != null ? successCount : 0)
                .failureDeviceCount(failureCount != null ? failureCount : 0)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功结果（带完整信息）
     */
    public static PushNotificationResult success(String messageId, String serviceProvider, String platform,
                                                 Integer successCount, Integer failureCount) {
        return PushNotificationResult.builder()
                .success(true)
                .messageId(messageId)
                .serviceProvider(serviceProvider)
                .platform(platform)
                .successDeviceCount(successCount != null ? successCount : 0)
                .failureDeviceCount(failureCount != null ? failureCount : 0)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static PushNotificationResult failure(String errorCode, String errorMessage) {
        return PushNotificationResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果（带详细错误）
     */
    public static PushNotificationResult failure(String errorCode, String errorMessage, String serviceProvider) {
        return PushNotificationResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .serviceProvider(serviceProvider)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 获取结果描述
     */
    public String getResultDescription() {
        if (success) {
            if (successDeviceCount != null && failureDeviceCount != null) {
                return String.format("推送发送成功 - 消息ID: %s, 成功: %d, 失败: %d",
                        messageId != null ? messageId : "未知",
                        successDeviceCount,
                        failureDeviceCount);
            } else {
                return String.format("推送发送成功 - 消息ID: %s",
                        messageId != null ? messageId : "未知");
            }
        } else {
            return String.format("推送发送失败 [%s] - %s",
                    errorCode != null ? errorCode : "UNKNOWN_ERROR",
                    errorMessage != null ? errorMessage : "未知错误");
        }
    }

    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (successDeviceCount == null || failureDeviceCount == null) {
            return success ? 100.0 : 0.0;
        }

        int total = successDeviceCount + failureDeviceCount;
        return total > 0 ? (double) successDeviceCount / total * 100 : 0.0;
    }

    /**
     * 检查是否需要重试
     */
    public boolean shouldRetry() {
        if (success) {
            // 如果有部分失败，可以重试失败的设备
            return failureDeviceCount != null && failureDeviceCount > 0;
        }

        // 以下错误类型可以重试
        return "NETWORK_TIMEOUT".equals(errorCode) ||
               "SERVICE_UNAVAILABLE".equals(errorCode) ||
               "RATE_LIMIT_EXCEEDED".equals(errorCode) ||
               "TEMPORARY_FAILURE".equals(errorCode) ||
               "DEVICE_OFFLINE".equals(errorCode);
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
                return 120; // 2分钟
            case "DEVICE_OFFLINE":
                return 600; // 10分钟
            case "NETWORK_TIMEOUT":
                return 30;  // 30秒
            default:
                return 60;  // 默认1分钟
        }
    }

    /**
     * 获取平台描述
     */
    public String getPlatformDescription() {
        if (platform == null) {
            return "所有平台";
        }

        switch (platform.toUpperCase()) {
            case "ANDROID":
                return "Android";
            case "IOS":
                return "iOS";
            case "ALL":
                return "所有平台";
            default:
                return platform;
        }
    }

    /**
     * 获取详细错误信息
     */
    public String getDetailedErrorInfo() {
        if (success) {
            if (failureDeviceCount != null && failureDeviceCount > 0) {
                return String.format("推送部分成功 - 成功: %d, 失败: %d, 成功率: %.1f%%",
                        successDeviceCount != null ? successDeviceCount : 0,
                        failureDeviceCount,
                        getSuccessRate());
            } else {
                return "推送全部成功";
            }
        }

        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("推送发送失败:\n");
        errorInfo.append(String.format("错误代码: %s\n", errorCode != null ? errorCode : "未知"));
        errorInfo.append(String.format("错误消息: %s\n", errorMessage != null ? errorMessage : "未知"));

        if (serviceProvider != null) {
            errorInfo.append(String.format("服务提供商: %s\n", serviceProvider));
        }

        if (platform != null) {
            errorInfo.append(String.format("推送平台: %s\n", getPlatformDescription()));
        }

        if (shouldRetry()) {
            errorInfo.append(String.format("建议重试，等待时间: %d秒\n", getRetryWaitSeconds()));
        } else {
            errorInfo.append("不建议重试，请检查配置和设备状态\n");
        }

        return errorInfo.toString();
    }

    /**
     * 检查是否为批量推送
     */
    public boolean isBatchPush() {
        Integer totalCount = null;
        if (successDeviceCount != null && failureDeviceCount != null) {
            totalCount = successDeviceCount + failureDeviceCount;
        }
        return totalCount != null && totalCount > 1;
    }

    /**
     * 获取发送状态描述
     */
    public String getStatusDescription() {
        if (success) {
            if (isBatchPush()) {
                return "批量推送成功";
            } else {
                return "单设备推送成功";
            }
        } else {
            return "推送发送失败";
        }
    }
}