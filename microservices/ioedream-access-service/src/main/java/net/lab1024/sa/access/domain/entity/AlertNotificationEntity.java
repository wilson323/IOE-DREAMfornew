package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 告警通知记录实体类
 * <p>
 * 用于记录告警通知的发送情况：
 * - 通知方式（短信/邮件/推送/WebSocket）
 * - 接收人信息（类型、ID、姓名、联系方式）
 * - 发送状态（待发送/发送中/成功/失败）
 * - 重试机制（重试次数、最大重试次数）
 * - 时间记录（发送、接收、阅读时间）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_alert_notification")
@Schema(description = "告警通知记录实体")
public class AlertNotificationEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "通知ID")
    private Long notificationId;

    /**
     * 告警ID
     */
    @Schema(description = "告警ID")
    private Long alertId;

    /**
     * 规则ID
     */
    @Schema(description = "规则ID")
    private Long ruleId;

    // ==================== 通知信息 ====================

    /**
     * 通知方式
     * SMS-短信
     * EMAIL-邮件
     * PUSH-应用推送
     * WEBSOCKET-WebSocket实时推送
     */
    @Schema(description = "通知方式：SMS-短信 EMAIL-邮件 PUSH-推送 WEBSOCKET-WebSocket")
    private String notificationMethod;

    /**
     * 接收人类型
     * USER-用户
     * ROLE-角色
     * DEPARTMENT-部门
     */
    @Schema(description = "接收人类型：USER-用户 ROLE-角色 DEPARTMENT-部门")
    private String recipientType;

    /**
     * 接收人ID
     */
    @Schema(description = "接收人ID")
    private Long recipientId;

    /**
     * 接收人姓名
     */
    @Schema(description = "接收人姓名")
    private String recipientName;

    /**
     * 接收人联系方式
     * 手机号/邮箱/设备ID等
     */
    @Schema(description = "接收人联系方式（手机号/邮箱/设备ID）")
    private String recipientContact;

    // ==================== 通知内容 ====================

    /**
     * 通知标题
     */
    @Schema(description = "通知标题")
    private String notificationTitle;

    /**
     * 通知内容
     */
    @Schema(description = "通知内容")
    private String notificationContent;

    // ==================== 发送状态 ====================

    /**
     * 通知状态
     * 0-待发送 1-发送中 2-发送成功 3-发送失败
     */
    @Schema(description = "通知状态：0-待发送 1-发送中 2-发送成功 3-发送失败")
    private Integer notificationStatus;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @Schema(description = "最大重试次数")
    private Integer maxRetry;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 错误代码
     */
    @Schema(description = "错误代码")
    private String errorCode;

    // ==================== 时间记录 ====================

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    /**
     * 接收时间
     */
    @Schema(description = "接收时间")
    private LocalDateTime receivedTime;

    /**
     * 阅读时间
     */
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;
}
