package net.lab1024.sa.monitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_notification")
public class NotificationEntity {

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long notificationId;

    /**
     * 关联告警ID
     */
    private Long alertId;

    /**
     * 通知标题
     */
    private String notificationTitle;

    /**
     * 通知内容
     */
    private String notificationContent;

    /**
     * 通知类型 (EMAIL、SMS、WEBHOOK、WECHAT、DINGTALK)
     */
    private String notificationType;

    /**
     * 接收人
     */
    private String recipient;

    /**
     * 接收人类型 (USER、GROUP、ROLE)
     */
    private String recipientType;

    /**
     * 发送状态 (PENDING、SENT、FAILED、CANCELLED)
     */
    private String sendStatus;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 响应内容
     */
    private String responseContent;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 模板参数
     */
    private String templateParams;

    /**
     * 优先级 (LOW、NORMAL、HIGH、URGENT)
     */
    private String priority;

    /**
     * 标签
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 更新用户ID
     */
    private Long updateUserId;

    /**
     * 删除标记
     */
    private Integer deletedFlag;
}