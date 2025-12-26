package net.lab1024.sa.common.monitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 监控通知实体
 * <p>
 * 对应数据库表: t_monitor_notification
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_monitor_notification")
public class NotificationEntity extends BaseEntity {

    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;

    @TableField("notification_type")
    private String notificationType;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("receiver_id")
    private Long receiverId;

    @TableField("receiver_type")
    private String receiverType;

    @TableField("channel")
    private String channel;

    @TableField("send_status")
    private Integer sendStatus;

    @TableField("send_time")
    private LocalDateTime sendTime;

    @TableField("read_status")
    private Integer readStatus;

    @TableField("read_time")
    private LocalDateTime readTime;
}

