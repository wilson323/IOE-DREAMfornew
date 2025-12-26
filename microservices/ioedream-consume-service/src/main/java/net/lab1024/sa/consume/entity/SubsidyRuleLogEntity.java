package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 补贴规则执行日志实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_subsidy_rule_log")
@Schema(description = "补贴规则执行日志实体")
public class SubsidyRuleLogEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "日志UUID")
    private String logUuid;

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "消费记录ID")
    private Long consumeId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "消费金额")
    private BigDecimal consumeAmount;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    @Schema(description = "补贴金额")
    private BigDecimal subsidyAmount;

    @Schema(description = "补贴比例")
    private BigDecimal subsidyRate;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @Schema(description = "计算详情(JSON)")
    private String calculationDetail;

    @Schema(description = "执行状态 1-成功 2-失败")
    private Integer executionStatus;

    @Schema(description = "错误信息")
    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
