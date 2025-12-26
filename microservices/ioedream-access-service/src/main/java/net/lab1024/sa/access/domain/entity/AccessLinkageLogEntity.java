package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 联动执行日志实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_access_linkage_log")
@Schema(description = "联动执行日志实体")
public class AccessLinkageLogEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long logId;

    @TableField("rule_id")
    @Schema(description = "联动规则ID")
    private Long ruleId;

    @TableField("trigger_device_id")
    @Schema(description = "触发设备ID")
    private Long triggerDeviceId;

    @TableField("trigger_door_id")
    @Schema(description = "触发门ID")
    private Long triggerDoorId;

    @TableField("trigger_event")
    @Schema(description = "触发事件")
    private String triggerEvent;

    @TableField("target_device_id")
    @Schema(description = "目标设备ID")
    private Long targetDeviceId;

    @TableField("target_door_id")
    @Schema(description = "目标门ID")
    private Long targetDoorId;

    @TableField("action_type")
    @Schema(description = "执行动作")
    private String actionType;

    @TableField("execution_status")
    @Schema(description = "执行状态(SUCCESS-成功, FAILED-失败, PENDING-待执行)")
    private String executionStatus;

    @TableField("execution_time")
    @Schema(description = "执行时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executionTime;

    @TableField("error_message")
    @Schema(description = "错误信息")
    private String errorMessage;

    @TableField("execution_result")
    @Schema(description = "执行结果(JSON格式)")
    private String executionResult;

    @TableField("trigger_data")
    @Schema(description = "触发数据(JSON格式)")
    private String triggerData;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
