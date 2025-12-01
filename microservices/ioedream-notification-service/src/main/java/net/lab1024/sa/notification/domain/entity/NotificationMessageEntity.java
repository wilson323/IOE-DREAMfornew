package net.lab1024.sa.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 通知消息实体
 * <p>
 * 统一管理所有渠道的通知消息
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 继承BaseEntity获取公共字段
 * - 完整的通知属性和状态管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_notification_message")
public class NotificationMessageEntity extends BaseEntity {

    /**
     * 消息ID
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    /**
     * 消息主题
     */
    private String subject;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1-系统通知 2-业务通知 3-告警通知 4-营销通知 5-验证码
     */
    private Integer messageType;

    /**
     * 发送渠道：1-邮件 2-短信 3-微信 4-站内信 5-推送 6-语音
     */
    private Integer channel;

    /**
     * 发送状态：0-待发送 1-发送中 2-发送成功 3-发送失败 4-已撤销
     */
    private Integer sendStatus;

    /**
     * 优先级：1-低 2-普通 3-高 4-紧急
     */
    private Integer priority;

    /**
     * 接收用户ID
     */
    private Long recipientUserId;

    /**
     * 接收人标识（邮箱、手机号、微信号等）
     */
    private String recipientIdentifier;

    /**
     * 接收人姓名
     */
    private String recipientName;

    /**
     * 发送用户ID
     */
    private Long senderUserId;

    /**
     * 发送人姓名
     */
    private String senderName;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 模板参数（JSON格式）
     */
    private String templateParams;

    /**
     * 附件信息（JSON格式）
     */
    private String attachments;

    /**
     * 计划发送时间
     */
    private LocalDateTime scheduleTime;

    /**
     * 实际发送时间
     */
    private LocalDateTime sentTime;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 外部消息ID（渠道返回的ID）
     */
    private String externalMessageId;

    /**
     * 发送耗时（毫秒）
     */
    private Long sendDuration;

    /**
     * 已读状态（仅站内信有效）：0-未读 1-已读
     */
    private Integer readStatus = 0;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 扩展属性（JSON格式）
     */
    private String extensions;

    /**
     * 获取消息类型描述
     */
    public String getMessageTypeDesc() {
        switch (messageType) {
            case 1:
                return "系统通知";
            case 2:
                return "业务通知";
            case 3:
                return "告警通知";
            case 4:
                return "营销通知";
            case 5:
                return "验证码";
            default:
                return "未知类型";
        }
    }

    /**
     * 获取发送渠道描述
     */
    public String getChannelDesc() {
        switch (channel) {
            case 1:
                return "邮件";
            case 2:
                return "短信";
            case 3:
                return "微信";
            case 4:
                return "站内信";
            case 5:
                return "推送";
            case 6:
                return "语音";
            default:
                return "未知渠道";
        }
    }

    /**
     * 获取发送状态描述
     */
    public String getSendStatusDesc() {
        switch (sendStatus) {
            case 0:
                return "待发送";
            case 1:
                return "发送中";
            case 2:
                return "发送成功";
            case 3:
                return "发送失败";
            case 4:
                return "已撤销";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取优先级描述
     */
    public String getPriorityDesc() {
        switch (priority) {
            case 1:
                return "低";
            case 2:
                return "普通";
            case 3:
                return "高";
            case 4:
                return "紧急";
            default:
                return "普通";
        }
    }

    /**
     * 是否为高优先级消息
     */
    public boolean isHighPriority() {
        return priority != null && priority >= 3;
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return sendStatus != null && sendStatus == 3 && retryCount < maxRetryCount;
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return scheduleTime != null && scheduleTime.isBefore(LocalDateTime.now().minusDays(1));
    }

    /**
     * 是否为验证码消息
     */
    public boolean isVerificationCode() {
        return messageType != null && messageType == 5;
    }

    /**
     * 是否为站内信
     */
    public boolean isInternalMessage() {
        return channel != null && channel == 4;
    }

    /**
     * 设置为发送成功
     */
    public void markAsSent(Long sendDuration, String externalMessageId) {
        this.sendStatus = 2;
        this.sentTime = LocalDateTime.now();
        this.sendDuration = sendDuration;
        this.externalMessageId = externalMessageId;
    }

    /**
     * 设置为发送失败
     */
    public void markAsFailed(String failureReason) {
        this.sendStatus = 3;
        this.failureReason = failureReason;
        this.retryCount++;
    }

    /**
     * 设置为已读（仅站内信有效）
     */
    public void markAsRead() {
        if (isInternalMessage()) {
            this.readStatus = 1;
            this.readTime = LocalDateTime.now();
        }
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }
}