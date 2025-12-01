/*
 * 安全通知记录
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
 * 安全通知记录
 * 表示一条通知的历史记录
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityNotificationRecord {

    /**
     * 通知ID
     */
    private Long notificationId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 通知渠道
     */
    private String channel;

    /**
     * 通知类型
     */
    private String notificationType;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 发送状态
     */
    private String status;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 额外数据
     */
    private String extraData;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 获取渠道描述
     */
    public String getChannelDescription() {
        switch (channel) {
            case "EMAIL":
                return "邮件";
            case "SMS":
                return "短信";
            case "PUSH":
                return "推送";
            case "WECHAT":
                return "微信";
            default:
                return channel;
        }
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        switch (status) {
            case "SUCCESS":
                return "发送成功";
            case "FAILED":
                return "发送失败";
            case "SCHEDULED":
                return "已计划";
            case "CANCELLED":
                return "已取消";
            case "RETRYING":
                return "重试中";
            default:
                return status;
        }
    }

    /**
     * 检查是否成功发送
     */
    public boolean isSuccessfullySent() {
        return "SUCCESS".equals(status);
    }

    /**
     * 检查是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return isFailed() && (retryCount == null || retryCount < 3);
    }

    /**
     * 获取处理时间
     */
    public long getProcessingTimeMs() {
        if (createTime != null && updateTime != null) {
            return java.time.Duration.between(createTime, updateTime).toMillis();
        }
        return 0;
    }

    /**
     * 获取记录摘要
     */
    public String getRecordSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("[%s] %s - %s\n", channel, notificationType, status));
        summary.append(String.format("时间: %s\n", createTime));
        summary.append(String.format("内容: %s\n", content.length() > 50 ? content.substring(0, 50) + "..." : content));

        if (isFailed()) {
            summary.append(String.format("错误: %s\n", errorMessage));
        }

        if (retryCount != null && retryCount > 0) {
            summary.append(String.format("重试次数: %d\n", retryCount));
        }

        return summary.toString();
    }
}