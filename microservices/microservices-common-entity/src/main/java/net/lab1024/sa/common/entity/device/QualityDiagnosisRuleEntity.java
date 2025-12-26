package net.lab1024.sa.common.entity.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * 质量诊断规则实体
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_quality_diagnosis_rule")
@Schema(description = "质量诊断规则实体")
public class QualityDiagnosisRuleEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "设备类型(1-门禁 2-考勤 3-消费 4-视频 5-访客)")
    private Integer deviceType;

    @Schema(description = "指标类型")
    private String metricType;

    @Schema(description = "规则表达式(eq/gt/lt/gte/lte)")
    private String ruleExpression;

    @Schema(description = "阈值")
    private BigDecimal thresholdValue;

    @Schema(description = "告警级别(1-低 2-中 3-高 4-紧急)")
    private Integer alarmLevel;

    @Schema(description = "规则状态(1-启用 0-禁用)")
    private Integer ruleStatus;

    @Schema(description = "规则描述")
    private String ruleDescription;
}
