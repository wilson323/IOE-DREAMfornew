package net.lab1024.sa.access.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警通知记录实体
 *
 * 记录告警通知的发送状态和结果
 * 支持多种通知方式：
 * - EMAIL-邮件
 * - SMS-短信
 * - WEBSOCKET-WebSocket推送
 * - PUSH-APP推送
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_alert_notification")
@Schema(description = "告警通知记录实体")
public class AlertNotificationEntity {

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "通知ID", example = "1")
    private Long notificationId;

    /**
     * 告警ID
     */
    @TableField("alert_id")
    @Schema(description = "告警ID", example = "1")
    @NotNull(message = "告警ID不能为空")
    private Long alertId;

    /**
     * 规则ID
     */
    @TableField("rule_id")
    @Schema(description = "规则ID", example = "1")
    private Long ruleId;

    /**
     * 通知批次ID（批量通知时使用）
     */
    @TableField("notification_batch_id")
    @Schema(description = "通知批次ID", example = "BATCH_20251226_001")
    private String notificationBatchId;

    /**
     * 通知方式：EMAIL-邮件 SMS-短信 WEBSOCKET-WebSocket PUSH-推送
     */
    @TableField("notification_method")
    @Schema(description = "通知方式", example = "WEBSOCKET")
    @NotBlank(message = "通知方式不能为空")
    private String notificationMethod;

    /**
     * 通知目标（邮箱、手机号、用户ID等）
     */
    @TableField("notification_target")
    @Schema(description = "通知目标", example = "user@example.com")
    @NotBlank(message = "通知目标不能为空")
    private String notificationTarget;

    /**
     * 通知内容
     */
    @TableField("notification_content")
    @Schema(description = "通知内容", example = "设备DEV001触发离线告警")
    @NotBlank(message = "通知内容不能为空")
    private String notificationContent;

    /**
     * 发送状态：0-待发送 1-发送中 2-发送成功 3-发送失败
     */
    @TableField("send_status")
    @Schema(description = "发送状态", example = "0")
    private Integer sendStatus;

    /**
     * 发送时间
     */
    @TableField("send_time")
    @Schema(description = "发送时间", example = "2025-12-26T10:05:00")
    private LocalDateTime sendTime;

    /**
     * 发送结果
     */
    @TableField("send_result")
    @Schema(description = "发送结果", example = "发送成功")
    private String sendResult;

    /**
     * 错误信息
     */
    @TableField("error_message")
    @Schema(description = "错误信息", example = "邮件服务器连接超时")
    private String errorMessage;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField("max_retry_count")
    @Schema(description = "最大重试次数", example = "3")
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    @TableField("next_retry_time")
    @Schema(description = "下次重试时间", example = "2025-12-26T10:10:00")
    private LocalDateTime nextRetryTime;

    /**
     * 是否已读：0-未读 1-已读
     */
    @TableField("is_read")
    @Schema(description = "是否已读", example = "0")
    private Integer isRead;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    @Schema(description = "阅读时间", example = "2025-12-26T10:06:00")
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "2025-12-26T10:05:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2025-12-26T10:05:00")
    private LocalDateTime updateTime;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    // ==================== 便捷方法 ====================

    /**
     * 判断是否待发送
     */
    public boolean isPending() {
        return this.sendStatus != null && this.sendStatus == 0;
    }

    /**
     * 判断是否发送中
     */
    public boolean isSending() {
        return this.sendStatus != null && this.sendStatus == 1;
    }

    /**
     * 判断是否发送成功
     */
    public boolean isSendSuccess() {
        return this.sendStatus != null && this.sendStatus == 2;
    }

    /**
     * 判断是否发送失败
     */
    public boolean isSendFailed() {
        return this.sendStatus != null && this.sendStatus == 3;
    }

    /**
     * 判断是否已读
     */
    public boolean isRead() {
        return this.isRead != null && this.isRead == 1;
    }

    /**
     * 判断是否可以重试
     */
    public boolean canRetry() {
        return this.retryCount != null && this.maxRetryCount != null &&
                this.retryCount < this.maxRetryCount;
    }

    /**
     * 判断是否邮件通知
     */
    public boolean isEmailNotification() {
        return "EMAIL".equals(this.notificationMethod);
    }

    /**
     * 判断是否短信通知
     */
    public boolean isSmsNotification() {
        return "SMS".equals(this.notificationMethod);
    }

    /**
     * 判断是否WebSocket通知
     */
    public boolean isWebSocketNotification() {
        return "WEBSOCKET".equals(this.notificationMethod);
    }

    /**
     * 判断是否APP推送
     */
    public boolean isPushNotification() {
        return "PUSH".equals(this.notificationMethod);
    }
}
