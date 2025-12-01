/*
 * 系统通知配置实体
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 系统通知配置实体
 * 存储系统级别的通知服务配置
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@TableName("t_system_notification_config")
public class SystemNotificationConfigEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 是否启用邮件服务
     */
    private Boolean emailEnabled;

    /**
     * 是否启用短信服务
     */
    private Boolean smsEnabled;

    /**
     * 是否启用推送服务
     */
    private Boolean pushEnabled;

    /**
     * 是否启用微信服务
     */
    private Boolean wechatEnabled;

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
     * 邮件服务器配置（JSON格式）
     */
    private String emailServerConfig;

    /**
     * 短信服务配置（JSON格式）
     */
    private String smsServiceConfig;

    /**
     * 推送服务配置（JSON格式）
     */
    private String pushServiceConfig;

    /**
     * 微信服务配置（JSON格式）
     */
    private String wechatServiceConfig;

    /**
     * 是否启用频率限制
     */
    private Boolean rateLimitEnabled;

    /**
     * 是否启用异步发送
     */
    private Boolean asyncSendEnabled;

    /**
     * 异步线程池大小
     */
    private Integer asyncThreadPoolSize;

    /**
     * 通知内容模板配置（JSON格式）
     */
    private String notificationTemplates;

    /**
     * 是否启用通知日志
     */
    private Boolean loggingEnabled;

    /**
     * 日志保留天数
     */
    private Integer logRetentionDays;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 配置更新人
     */
    private String updatedBy;
}