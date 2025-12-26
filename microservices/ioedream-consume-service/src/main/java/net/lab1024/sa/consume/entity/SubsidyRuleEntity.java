package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 补贴规则实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_subsidy_rule")
@Schema(description = "补贴规则实体")
public class SubsidyRuleEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则类型 1-固定金额 2-比例补贴 3-阶梯补贴 4-限时补贴")
    private Integer ruleType;

    @Schema(description = "补贴类型 1-餐补 2-交通补 3-其他")
    private Integer subsidyType;

    @Schema(description = "补贴金额(固定金额模式)")
    private BigDecimal subsidyAmount;

    @Schema(description = "补贴比例(比例模式 0-1)")
    private BigDecimal subsidyRate;

    @Schema(description = "最高补贴限额")
    private BigDecimal maxSubsidyAmount;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @Schema(description = "阶梯配置(JSON)")
    private String tierConfig;

    @Schema(description = "适用时间 1-全部 2-工作日 3-周末 4-自定义")
    private Integer applyTimeType;

    @Schema(description = "开始时间")
    private LocalTime applyStartTime;

    @Schema(description = "结束时间")
    private LocalTime applyEndTime;

    @Schema(description = "适用日期(1,2,3,4,5,6,7 代表周一到周日)")
    private String applyDays;

    @Schema(description = "适用餐别(1-早餐 2-午餐 3-晚餐)")
    private String applyMealTypes;

    @Schema(description = "优先级(数字越大优先级越高)")
    private Integer priority;

    @Schema(description = "状态 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveDate;

    @Schema(description = "失效时间")
    private LocalDateTime expireDate;

    @Schema(description = "规则描述")
    private String description;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @Schema(description = "扩展属性(JSON)")
    private String extendedAttributes;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "更新人ID")
    private Long updateUserId;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;

    @Version
    @Schema(description = "版本号")
    private Integer version;
}
