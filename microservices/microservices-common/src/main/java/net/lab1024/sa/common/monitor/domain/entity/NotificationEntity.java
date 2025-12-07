package net.lab1024.sa.common.monitor.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 监控通知实体
 * <p>
 * 符合CLAUDE.md规范：
 * - 使用MyBatis-Plus注解
 * - 使用jakarta.validation进行参数验证
 * - 完整的业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_monitor_notification")
@Schema(description = "监控通知实体")
public class NotificationEntity {

    @TableId(value = "notification_id", type = IdType.ASSIGN_ID)
    @Schema(description = "通知ID")
    private Long notificationId;

    @NotBlank(message = "通知标题不能为空")
    @TableField("title")
    @Schema(description = "通知标题")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @TableField("content")
    @Schema(description = "通知内容")
    private String content;

    @NotNull(message = "通知类型不能为空")
    @TableField("notification_type")
    @Schema(description = "通知类型")
    private Integer notificationType;

    @NotNull(message = "接收者类型不能为空")
    @TableField("receiver_type")
    @Schema(description = "接收者类型")
    private Integer receiverType;

    @TableField("receiver_ids")
    @Schema(description = "接收者ID列表")
    private String receiverIds;

    @TableField("sender_id")
    @Schema(description = "发送者ID")
    private Long senderId;

    @TableField("sender_name")
    @Schema(description = "发送者姓名")
    private String senderName;

    @NotNull(message = "发送渠道不能为空")
    @TableField("channel")
    @Schema(description = "发送渠道")
    private Integer channel;

    @TableField("priority")
    @Schema(description = "优先级")
    private Integer priority;

    @TableField("status")
    @Schema(description = "发送状态")
    private Integer status;

    @TableField("send_time")
    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @TableField("error_message")
    @Schema(description = "错误消息")
    private String errorMessage;

    @TableField("retry_count")
    @Schema(description = "重试次数")
    private Integer retryCount;

    @TableField("max_retry_count")
    @Schema(description = "最大重试次数")
    private Integer maxRetryCount;

    @TableField("next_retry_time")
    @Schema(description = "下次重试时间")
    private LocalDateTime nextRetryTime;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
