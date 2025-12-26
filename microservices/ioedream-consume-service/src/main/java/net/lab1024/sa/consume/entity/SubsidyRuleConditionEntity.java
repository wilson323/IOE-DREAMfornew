package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 补贴规则条件实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_subsidy_rule_condition")
@Schema(description = "补贴规则条件实体")
public class SubsidyRuleConditionEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "条件ID")
    private Long id;

    @Schema(description = "条件UUID")
    private String conditionUuid;

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "条件类型 user_group/department/area/device/consume_amount")
    private String conditionType;

    @Schema(description = "操作符 eq/in/gt/lt/between")
    private String conditionOperator;

    @Schema(description = "条件值(JSON数组)")
    private String conditionValue;

    @Schema(description = "逻辑关系 AND/OR/NOT")
    private String logicOperator;

    @Schema(description = "状态 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "条件描述")
    private String description;

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
