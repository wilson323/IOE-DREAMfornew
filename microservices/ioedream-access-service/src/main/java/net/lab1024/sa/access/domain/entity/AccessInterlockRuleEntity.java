package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁互锁规则实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_access_interlock_rule")
@Schema(description = "门禁互锁规则实体")
public class AccessInterlockRuleEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "互锁规则ID")
    private Long ruleId;

    @TableField("rule_name")
    @Schema(description = "规则名称")
    private String ruleName;

    @TableField("rule_code")
    @Schema(description = "规则编码")
    private String ruleCode;

    @TableField("rule_type")
    @Schema(description = "互锁类型(AREA_INTERLOCK-区域互锁, DOOR_INTERLOCK-门互锁)")
    private String ruleType;

    @TableField("area_a_id")
    @Schema(description = "区域A ID")
    private Long areaAId;

    @TableField("area_a_name")
    @Schema(description = "区域A名称")
    private String areaAName;

    @TableField("door_a_id")
    @Schema(description = "门A ID")
    private Long doorAId;

    @TableField("area_b_id")
    @Schema(description = "区域B ID")
    private Long areaBId;

    @TableField("area_b_name")
    @Schema(description = "区域B名称")
    private String areaBName;

    @TableField("door_b_id")
    @Schema(description = "门B ID")
    private Long doorBId;

    @TableField("area_a_door_ids")
    @Schema(description = "区域A关联门ID列表(JSON数组)")
    private String areaADoorIds;

    @TableField("area_b_door_ids")
    @Schema(description = "区域B关联门ID列表(JSON数组)")
    private String areaBDoorIds;

    @TableField("interlock_mode")
    @Schema(description = "互锁模式(BIDIRECTIONAL-双向, UNIDIRECTIONAL-单向)")
    private String interlockMode;

    @TableField("unlock_condition")
    @Schema(description = "解锁条件(MANUAL-手动, TIMER-定时, AUTO-自动)")
    private String unlockCondition;

    @TableField("unlock_delay_seconds")
    @Schema(description = "自动解锁延迟秒数")
    private Integer unlockDelaySeconds;

    @TableField("enabled")
    @Schema(description = "启用状态(0-禁用 1-启用)")
    private Integer enabled;

    @TableField("priority")
    @Schema(description = "优先级(1-100, 数字越小优先级越高)")
    private Integer priority;

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
