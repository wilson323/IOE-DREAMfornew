/*
 * 安全通知结果
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
 * 安全通知结果
 * 封装通知发送的执行结果
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityNotificationResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果代码
     */
    private String resultCode;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 通知渠道
     */
    private String channel;

    /**
     * 通知类型
     */
    private String notificationType;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 额外数据
     */
    private Object extraData;

    /**
     * 创建成功结果
     */
    public static SecurityNotificationResult success(String message) {
        return SecurityNotificationResult.builder()
                .success(true)
                .resultCode("SUCCESS")
                .message(message)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功结果（带消息ID）
     */
    public static SecurityNotificationResult success(String message, String messageId) {
        return SecurityNotificationResult.builder()
                .success(true)
                .resultCode("SUCCESS")
                .message(message)
                .messageId(messageId)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static SecurityNotificationResult failure(String resultCode, String message) {
        return SecurityNotificationResult.builder()
                .success(false)
                .resultCode(resultCode)
                .message(message)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建部分成功结果
     */
    public static SecurityNotificationResult partial(String message, String detail) {
        return SecurityNotificationResult.builder()
                .success(true)
                .resultCode("PARTIAL_SUCCESS")
                .message(message)
                .extraData(detail)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 检查是否为成功状态
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 检查是否为失败状态
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * 检查是否需要重试
     */
    public boolean shouldRetry() {
        if (success) {
            return false;
        }

        // 网络相关错误可重试
        return "NETWORK_ERROR".equals(resultCode) ||
               "TIMEOUT_ERROR".equals(resultCode) ||
               "SERVICE_UNAVAILABLE".equals(resultCode);
    }

    /**
     * 获取重试等待时间（秒）
     */
    public long getRetryWaitTime() {
        if (retryCount == null || retryCount == 0) {
            return 5; // 首次重试等待5秒
        }

        // 指数退避：5, 10, 20, 40...
        return Math.min(5L * (1L << retryCount), 300); // 最大5分钟
    }

    /**
     * 获取结果描述
     */
    public String getResultDescription() {
        if (success) {
            return String.format("通知发送成功 - %s", message);
        } else {
            return String.format("通知发送失败 [%s] - %s", resultCode, message);
        }
    }

    /**
     * 获取详细信息
     */
    public String getDetailInfo() {
        StringBuilder detail = new StringBuilder();
        detail.append("通知发送结果: ").append(success ? "成功" : "失败");
        detail.append(", 结果代码: ").append(resultCode);
        detail.append(", 消息: ").append(message);

        if (messageId != null) {
            detail.append(", 消息ID: ").append(messageId);
        }

        if (channel != null) {
            detail.append(", 渠道: ").append(channel);
        }

        if (retryCount != null && retryCount > 0) {
            detail.append(", 重试次数: ").append(retryCount);
        }

        if (sendTime != null) {
            detail.append(", 发送时间: ").append(sendTime);
        }

        return detail.toString();
    }
}