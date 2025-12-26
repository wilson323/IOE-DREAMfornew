package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 门禁联动规则实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_access_linkage_rule")
@Schema(description = "门禁联动规则实体")
public class AccessLinkageRuleEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "联动规则ID")
    private Long ruleId;

    @TableField("rule_name")
    @Schema(description = "规则名称")
    private String ruleName;

    @TableField("rule_code")
    @Schema(description = "规则编码")
    private String ruleCode;

    @TableField("rule_type")
    @Schema(description = "联动类型(DOOR_LINKAGE-门联动, AREA_LINKAGE-区域联动)")
    private String ruleType;

    @TableField("trigger_type")
    @Schema(description = "触发类型(OPEN_DOOR-开门, CLOSE_DOOR-关门, ALARM-告警)")
    private String triggerType;

    @TableField("trigger_device_id")
    @Schema(description = "触发设备ID")
    private Long triggerDeviceId;

    @TableField("trigger_door_id")
    @Schema(description = "触发门ID")
    private Long triggerDoorId;

    @TableField("action_type")
    @Schema(description = "执行动作(OPEN_DOOR-开门, CLOSE_DOOR-关门, LOCK-锁定, UNLOCK-解锁)")
    private String actionType;

    @TableField("target_device_id")
    @Schema(description = "目标设备ID")
    private Long targetDeviceId;

    @TableField("target_door_id")
    @Schema(description = "目标门ID")
    private Long targetDoorId;

    @TableField("delay_seconds")
    @Schema(description = "延迟执行秒数")
    private Integer delaySeconds;

    @TableField("condition_expression")
    @Schema(description = "条件表达式(JSON格式)")
    private String conditionExpression;

    @TableField("priority")
    @Schema(description = "优先级(1-100, 数字越小优先级越高)")
    private Integer priority;

    @TableField("enabled")
    @Schema(description = "启用状态(0-禁用 1-启用)")
    private Integer enabled;

    @TableField("valid_from")
    @Schema(description = "生效开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    @TableField("valid_until")
    @Schema(description = "生效结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntil;

    @TableField("description")
    @Schema(description = "规则描述")
    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField("create_user_id")
    @Schema(description = "创建人ID")
    private Long createUserId;

    @TableField("update_user_id")
    @Schema(description = "更新人ID")
    private Long updateUserId;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记(0-未删除 1-已删除)")
    private Integer deletedFlag;
}
