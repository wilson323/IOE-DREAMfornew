/*
 * 用户通知偏好
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
import java.util.Arrays;
import java.util.List;

/**
 * 用户通知偏好
 * 定义用户的通知接收偏好设置
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreference {

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 是否启用邮件通知
     */
    private boolean emailEnabled;

    /**
     * 是否启用短信通知
     */
    private boolean smsEnabled;

    /**
     * 是否启用推送通知
     */
    private boolean pushEnabled;

    /**
     * 是否启用微信通知
     */
    private boolean wechatEnabled;

    /**
     * 通知类型列表
     */
    private List<String> notificationTypes;

    /**
     * 免打扰时间段开始（小时，0-23）
     */
    private Integer doNotDisturbStartHour;

    /**
     * 免打扰时间段结束（小时，0-23）
     */
    private Integer doNotDisturbEndHour;

    /**
     * 是否启用免打扰
     */
    private boolean doNotDisturbEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 检查通知类型是否启用
     */
    public boolean isNotificationEnabled(String notificationType) {
        if (notificationTypes == null || notificationTypes.isEmpty()) {
            return true; // 未配置则默认启用
        }

        return notificationTypes.contains(notificationType);
    }

    /**
     * 检查当前时间是否在免打扰时间段内
     */
    public boolean isDoNotDisturbPeriod() {
        if (!doNotDisturbEnabled || doNotDisturbStartHour == null || doNotDisturbEndHour == null) {
            return false;
        }

        int currentHour = LocalDateTime.now().getHour();

        if (doNotDisturbStartHour <= doNotDisturbEndHour) {
            // 正常时间段，如 22:00 - 08:00
            return currentHour >= doNotDisturbStartHour && currentHour < doNotDisturbEndHour;
        } else {
            // 跨天时间段，如 22:00 - 次日08:00
            return currentHour >= doNotDisturbStartHour || currentHour < doNotDisturbEndHour;
        }
    }

    /**
     * 获取启用的通知渠道
     */
    public List<String> getEnabledChannels() {
        return Arrays.asList(
                emailEnabled ? "EMAIL" : null,
                smsEnabled ? "SMS" : null,
                pushEnabled ? "PUSH" : null,
                wechatEnabled ? "WECHAT" : null
        ).stream().filter(channel -> channel != null).toList();
    }

    /**
     * 检查是否有任何通知渠道启用
     */
    public boolean hasAnyChannelEnabled() {
        return emailEnabled || smsEnabled || pushEnabled || wechatEnabled;
    }

    /**
     * 检查是否为安全敏感通知类型
     */
    public boolean isSecuritySensitiveNotification(String notificationType) {
        if (notificationType == null) {
            return false;
        }

        String type = notificationType.toUpperCase();
        return type.equals("SECURITY_ALERT") ||
               type.equals("ACCOUNT_STATUS_CHANGE") ||
               type.equals("TRANSACTION_ANOMALY") ||
               type.equals("LOGIN_ANOMALY") ||
               type.equals("PASSWORD_RESET");
    }

    /**
     * 检查安全敏感通知是否允许免打扰
     */
    public boolean allowDoNotDisturbForNotification(String notificationType) {
        // 安全敏感通知不受免打扰限制
        return !isSecuritySensitiveNotification(notificationType) || !isDoNotDisturbPeriod();
    }

    /**
     * 获取优先级最高的通知渠道
     */
    public String getHighestPriorityChannel() {
        if (pushEnabled) return "PUSH";
        if (smsEnabled) return "SMS";
        if (emailEnabled) return "EMAIL";
        if (wechatEnabled) return "WECHAT";
        return null;
    }

    /**
     * 添加通知类型
     */
    public void addNotificationType(String notificationType) {
        if (notificationType != null && !isNotificationEnabled(notificationType)) {
            notificationTypes.add(notificationType);
        }
    }

    /**
     * 移除通知类型
     */
    public void removeNotificationType(String notificationType) {
        if (notificationType != null && notificationTypes != null) {
            notificationTypes.remove(notificationType);
        }
    }

    /**
     * 设置默认偏好
     */
    public void setDefaultPreferences() {
        this.emailEnabled = true;
        this.smsEnabled = true;
        this.pushEnabled = true;
        this.wechatEnabled = false;
        this.doNotDisturbEnabled = false;
        this.doNotDisturbStartHour = 22;
        this.doNotDisturbEndHour = 8;
        this.notificationTypes = Arrays.asList(
                "SECURITY_ALERT",
                "ACCOUNT_STATUS_CHANGE",
                "TRANSACTION_ANOMALY",
                "LIMIT_REMINDER"
        );
    }

    /**
     * 创建默认偏好
     */
    public static UserNotificationPreference createDefault(Long personId) {
        return UserNotificationPreference.builder()
                .personId(personId)
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .wechatEnabled(false)
                .doNotDisturbEnabled(false)
                .doNotDisturbStartHour(22)
                .doNotDisturbEndHour(8)
                .notificationTypes(Arrays.asList(
                        "SECURITY_ALERT",
                        "ACCOUNT_STATUS_CHANGE",
                        "TRANSACTION_ANOMALY",
                        "LIMIT_REMINDER",
                        "LOGIN_ANOMALY",
                        "PASSWORD_RESET"
                ))
                .build();
    }

    /**
     * 创建仅关键通知偏好
     */
    public static UserNotificationPreference createCriticalOnly(Long personId) {
        return UserNotificationPreference.builder()
                .personId(personId)
                .emailEnabled(true)
                .smsEnabled(false)
                .pushEnabled(false)
                .wechatEnabled(false)
                .doNotDisturbEnabled(false)
                .notificationTypes(Arrays.asList(
                        "SECURITY_ALERT",
                        "ACCOUNT_STATUS_CHANGE",
                        "TRANSACTION_ANOMALY"
                ))
                .build();
    }

    /**
     * 获取偏好摘要
     */
    public String getPreferenceSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("启用的通知渠道: ");

        if (emailEnabled) summary.append("邮件 ");
        if (smsEnabled) summary.append("短信 ");
        if (pushEnabled) summary.append("推送 ");
        if (wechatEnabled) summary.append("微信 ");

        summary.append(String.format("\n通知类型数量: %d",
                notificationTypes != null ? notificationTypes.size() : 0));

        if (doNotDisturbEnabled) {
            summary.append(String.format("\n免打扰时段: %02d:00 - %02d:00",
                    doNotDisturbStartHour != null ? doNotDisturbStartHour : 0,
                    doNotDisturbEndHour != null ? doNotDisturbEndHour : 0));
        }

        return summary.toString();
    }
}