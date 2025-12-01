/*
 * 系统通知配置
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
import java.util.Map;

/**
 * 系统通知配置
 * 系统级别的通知服务配置
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotificationConfig {

    /**
     * 是否启用邮件服务
     */
    private boolean emailEnabled;

    /**
     * 是否启用短信服务
     */
    private boolean smsEnabled;

    /**
     * 是否启用推送服务
     */
    private boolean pushEnabled;

    /**
     * 是否启用微信服务
     */
    private boolean wechatEnabled;

    /**
     * 每小时最大邮件发送数量
     */
    private Integer maxEmailPerHour;

    /**
     * 每小时最大短信发送数量
     */
    private Integer maxSmsPerHour;

    /**
     * 每小时最大推送发送数量
     */
    private Integer maxPushPerHour;

    /**
     * 每小时最大微信发送数量
     */
    private Integer maxWechatPerHour;

    /**
     * 重试尝试次数
     */
    private Integer retryAttempts;

    /**
     * 重试间隔（分钟）
     */
    private Integer retryInterval;

    /**
     * 邮件服务器配置
     */
    private Map<String, Object> emailServerConfig;

    /**
     * 短信服务配置
     */
    private Map<String, Object> smsServiceConfig;

    /**
     * 推送服务配置
     */
    private Map<String, Object> pushServiceConfig;

    /**
     * 微信服务配置
     */
    private Map<String, Object> wechatServiceConfig;

    /**
     * 是否启用频率限制
     */
    private boolean rateLimitEnabled;

    /**
     * 是否启用异步发送
     */
    private boolean asyncSendEnabled;

    /**
     * 异步线程池大小
     */
    private Integer asyncThreadPoolSize;

    /**
     * 通知内容模板配置
     */
    private Map<String, String> notificationTemplates;

    /**
     * 是否启用通知日志
     */
    private boolean loggingEnabled;

    /**
     * 日志保留天数
     */
    private Integer logRetentionDays;

    /**
     * 配置更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 配置更新人
     */
    private String updatedBy;

    /**
     * 检查配置是否有效
     */
    public boolean isValid() {
        // 检查基本参数有效性
        if (maxEmailPerHour != null && maxEmailPerHour <= 0) return false;
        if (maxSmsPerHour != null && maxSmsPerHour <= 0) return false;
        if (maxPushPerHour != null && maxPushPerHour <= 0) return false;
        if (maxWechatPerHour != null && maxWechatPerHour <= 0) return false;

        if (retryAttempts != null && retryAttempts < 0) return false;
        if (retryInterval != null && retryInterval <= 0) return false;
        if (asyncThreadPoolSize != null && asyncThreadPoolSize <= 0) return false;
        if (logRetentionDays != null && logRetentionDays <= 0) return false;

        return true;
    }

    /**
     * 获取指定渠道的每小时最大发送数量
     */
    public Integer getMaxSendCountPerHour(String channel) {
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return maxEmailPerHour != null ? maxEmailPerHour : 10;
            case "SMS":
                return maxSmsPerHour != null ? maxSmsPerHour : 20;
            case "PUSH":
                return maxPushPerHour != null ? maxPushPerHour : 50;
            case "WECHAT":
                return maxWechatPerHour != null ? maxWechatPerHour : 30;
            default:
                return 20;
        }
    }

    /**
     * 检查渠道是否启用
     */
    public boolean isChannelEnabled(String channel) {
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return emailEnabled;
            case "SMS":
                return smsEnabled;
            case "PUSH":
                return pushEnabled;
            case "WECHAT":
                return wechatEnabled;
            default:
                return false;
        }
    }

    /**
     * 获取渠道配置
     */
    public Map<String, Object> getChannelConfig(String channel) {
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return emailServerConfig;
            case "SMS":
                return smsServiceConfig;
            case "PUSH":
                return pushServiceConfig;
            case "WECHAT":
                return wechatServiceConfig;
            default:
                return null;
        }
    }

    /**
     * 获取通知模板
     */
    public String getNotificationTemplate(String templateKey) {
        if (notificationTemplates == null) {
            return null;
        }
        return notificationTemplates.get(templateKey);
    }

    /**
     * 创建默认配置
     */
    public static SystemNotificationConfig createDefault() {
        return SystemNotificationConfig.builder()
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .wechatEnabled(false)
                .maxEmailPerHour(10)
                .maxSmsPerHour(20)
                .maxPushPerHour(50)
                .maxWechatPerHour(30)
                .retryAttempts(3)
                .retryInterval(5)
                .rateLimitEnabled(true)
                .asyncSendEnabled(true)
                .asyncThreadPoolSize(5)
                .loggingEnabled(true)
                .logRetentionDays(30)
                .build();
    }

    /**
     * 创建测试环境配置
     */
    public static SystemNotificationConfig createTest() {
        return SystemNotificationConfig.builder()
                .emailEnabled(false) // 测试环境通常不启用邮件
                .smsEnabled(false)  // 测试环境通常不启用短信
                .pushEnabled(true)
                .wechatEnabled(false)
                .maxEmailPerHour(1)
                .maxSmsPerHour(1)
                .maxPushPerHour(10)
                .maxWechatPerHour(1)
                .retryAttempts(1) // 测试环境减少重试
                .retryInterval(1)
                .rateLimitEnabled(true)
                .asyncSendEnabled(false) // 测试环境使用同步发送便于调试
                .asyncThreadPoolSize(1)
                .loggingEnabled(true)
                .logRetentionDays(7) // 测试环境日志保留时间短
                .build();
    }

    /**
     * 创建生产环境配置
     */
    public static SystemNotificationConfig createProduction() {
        return SystemNotificationConfig.builder()
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .wechatEnabled(true) // 生产环境启用所有渠道
                .maxEmailPerHour(100)
                .maxSmsPerHour(200)
                .maxPushPerHour(500)
                .maxWechatPerHour(150)
                .retryAttempts(5) // 生产环境增加重试
                .retryInterval(3)
                .rateLimitEnabled(true)
                .asyncSendEnabled(true)
                .asyncThreadPoolSize(10) // 生产环境增加线程池
                .loggingEnabled(true)
                .logRetentionDays(90) // 生产环境日志保留时间长
                .build();
    }

    /**
     * 获取配置摘要
     */
    public String getConfigSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("系统通知配置:\n");

        summary.append("启用的通知渠道: ");
        if (emailEnabled) summary.append("邮件 ");
        if (smsEnabled) summary.append("短信 ");
        if (pushEnabled) summary.append("推送 ");
        if (wechatEnabled) summary.append("微信 ");
        summary.append("\n");

        summary.append("每小时发送限制:\n");
        summary.append(String.format("  邮件: %d\n", maxEmailPerHour != null ? maxEmailPerHour : 0));
        summary.append(String.format("  短信: %d\n", maxSmsPerHour != null ? maxSmsPerHour : 0));
        summary.append(String.format("  推送: %d\n", maxPushPerHour != null ? maxPushPerHour : 0));
        summary.append(String.format("  微信: %d\n", maxWechatPerHour != null ? maxWechatPerHour : 0));

        summary.append(String.format("重试配置: %d次，间隔%d分钟\n",
                retryAttempts != null ? retryAttempts : 0,
                retryInterval != null ? retryInterval : 0));

        summary.append(String.format("异步发送: %s", asyncSendEnabled ? "启用" : "禁用"));
        if (asyncSendEnabled) {
            summary.append(String.format("，线程池大小: %d", asyncThreadPoolSize != null ? asyncThreadPoolSize : 0));
        }
        summary.append("\n");

        summary.append(String.format("通知日志: %s", loggingEnabled ? "启用" : "禁用"));
        if (loggingEnabled) {
            summary.append(String.format("，保留天数: %d", logRetentionDays != null ? logRetentionDays : 0));
        }

        return summary.toString();
    }

    /**
     * 验证配置完整性
     */
    public String validateConfig() {
        StringBuilder errors = new StringBuilder();

        if (emailEnabled && (maxEmailPerHour == null || maxEmailPerHour <= 0)) {
            errors.append("邮件服务已启用但未配置每小时最大发送数量\n");
        }

        if (smsEnabled && (maxSmsPerHour == null || maxSmsPerHour <= 0)) {
            errors.append("短信服务已启用但未配置每小时最大发送数量\n");
        }

        if (pushEnabled && (maxPushPerHour == null || maxPushPerHour <= 0)) {
            errors.append("推送服务已启用但未配置每小时最大发送数量\n");
        }

        if (wechatEnabled && (maxWechatPerHour == null || maxWechatPerHour <= 0)) {
            errors.append("微信服务已启用但未配置每小时最大发送数量\n");
        }

        if (retryAttempts == null || retryAttempts < 0) {
            errors.append("重试次数配置无效\n");
        }

        if (retryInterval == null || retryInterval <= 0) {
            errors.append("重试间隔配置无效\n");
        }

        if (asyncSendEnabled && (asyncThreadPoolSize == null || asyncThreadPoolSize <= 0)) {
            errors.append("异步发送已启用但未配置线程池大小\n");
        }

        if (loggingEnabled && (logRetentionDays == null || logRetentionDays <= 0)) {
            errors.append("通知日志已启用但未配置保留天数\n");
        }

        return errors.length() > 0 ? errors.toString() : null;
    }
}