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
 * 通知发送记录实体
 * <p>
 * 记录所有通知的发送历史，用于统计和审计
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 继承BaseEntity获取公共字段
 * - 完整的发送过程记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_notification_record")
public class NotificationRecordEntity extends BaseEntity {

    /**
     * 记录ID
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 批次ID（用于批量发送的跟踪）
     */
    private String batchId;

    /**
     * 发送渠道：1-邮件 2-短信 3-微信 4-站内信 5-推送 6-语音
     */
    private Integer channel;

    /**
     * 消息类型：1-系统通知 2-业务通知 3-告警通知 4-营销通知 5-验证码
     */
    private Integer messageType;

    /**
     * 发送状态：1-成功 2-失败 3-超时 4-撤销
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
     * 接收人标识
     */
    private String recipientIdentifier;

    /**
     * 发送用户ID
     */
    private Long senderUserId;

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
     * 发送开始时间
     */
    private LocalDateTime sendStartTime;

    /**
     * 发送结束时间
     */
    private LocalDateTime sendEndTime;

    /**
     * 发送耗时（毫秒）
     */
    private Long sendDuration;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 外部消息ID
     */
    private String externalMessageId;

    /**
     * 响应码
     */
    private String responseCode;

    /**
     * 响应消息
     */
    private String responseMessage;

    /**
     * 错误详情
     */
    private String errorDetail;

    /**
     * 发送内容（实际发送的内容）
     */
    private String sendContent;

    /**
     * 发送主题
     */
    private String sendSubject;

    /**
     * 发送服务商（如阿里云、腾讯云等）
     */
    private String serviceProvider;

    /**
     * 发送成本（分）
     */
    private Integer sendCost;

    /**
     * 配送状态（仅推送和邮件有效）：1-已送达 2-已读 3-已点击 4-已退订
     */
    private Integer deliveryStatus;

    /**
     * 配送时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 最后跟踪时间
     */
    private LocalDateTime lastTrackTime;

    /**
     * 用户行为（点击、打开等）
     */
    private String userAction;

    /**
     * 用户行为时间
     */
    private LocalDateTime userActionTime;

    /**
     * 地理位置信息
     */
    private String locationInfo;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 扩展数据（JSON格式）
     */
    private String extensions;

    /**
     * 获取渠道描述
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
     * 获取发送状态描述
     */
    public String getSendStatusDesc() {
        switch (sendStatus) {
            case 1:
                return "成功";
            case 2:
                return "失败";
            case 3:
                return "超时";
            case 4:
                return "撤销";
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
     * 获取配送状态描述
     */
    public String getDeliveryStatusDesc() {
        if (deliveryStatus == null) {
            return "未知";
        }
        switch (deliveryStatus) {
            case 1:
                return "已送达";
            case 2:
                return "已读";
            case 3:
                return "已点击";
            case 4:
                return "已退订";
            default:
                return "未知";
        }
    }

    /**
     * 是否发送成功
     */
    public boolean isSendSuccess() {
        return sendStatus != null && sendStatus == 1;
    }

    /**
     * 是否发送失败
     */
    public boolean isSendFailed() {
        return sendStatus != null && sendStatus == 2;
    }

    /**
     * 是否已送达
     */
    public boolean isDelivered() {
        return deliveryStatus != null && deliveryStatus >= 1;
    }

    /**
     * 是否已读
     */
    public boolean isRead() {
        return deliveryStatus != null && deliveryStatus >= 2;
    }

    /**
     * 是否已点击
     */
    public boolean isClicked() {
        return deliveryStatus != null && deliveryStatus == 3;
    }

    /**
     * 计算实际发送时长
     */
    public Long calculateSendDuration() {
        if (sendStartTime != null && sendEndTime != null) {
            return java.time.Duration.between(sendStartTime, sendEndTime).toMillis();
        }
        return sendDuration;
    }

    /**
     * 设置发送成功
     */
    public void markAsSuccess(String externalMessageId, Long duration) {
        this.sendStatus = 1;
        this.externalMessageId = externalMessageId;
        this.sendEndTime = LocalDateTime.now();
        this.sendDuration = duration != null ? duration : calculateSendDuration();
    }

    /**
     * 设置发送失败
     */
    public void markAsFailed(String responseCode, String responseMessage, String errorDetail) {
        this.sendStatus = 2;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.errorDetail = errorDetail;
        this.sendEndTime = LocalDateTime.now();
        this.sendDuration = calculateSendDuration();
    }

    /**
     * 设置超时
     */
    public void markAsTimeout() {
        this.sendStatus = 3;
        this.responseMessage = "发送超时";
        this.sendEndTime = LocalDateTime.now();
        this.sendDuration = calculateSendDuration();
    }

    /**
     * 更新配送状态
     */
    public void updateDeliveryStatus(Integer newDeliveryStatus, String userAction) {
        this.deliveryStatus = newDeliveryStatus;
        this.userAction = userAction;
        this.deliveryTime = LocalDateTime.now();
        this.userActionTime = LocalDateTime.now();
        this.lastTrackTime = LocalDateTime.now();
    }

    /**
     * 更新最后跟踪时间
     */
    public void updateLastTrackTime() {
        this.lastTrackTime = LocalDateTime.now();
    }
}