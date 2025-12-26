package net.lab1024.sa.common.monitor.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 告警规则添加DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@Schema(description = "告警规则添加请求")
public class AlertRuleAddDTO {

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    @Schema(description = "规则名称", example = "CPU使用率告警", required = true)
    private String ruleName;

    @Schema(description = "规则描述", example = "当CPU使用率超过80%时触发告警")
    private String ruleDescription;

    @NotBlank(message = "监控指标不能为空")
    @Size(max = 100, message = "监控指标长度不能超过100个字符")
    @Schema(description = "监控指标名称", example = "cpu.usage", required = true)
    private String metricName;

    @Schema(description = "监控类型", example = "METRIC")
    private String monitorType;

    @Schema(description = "条件操作符", example = "GT", allowableValues = {"GT", "LT", "EQ", "GTE", "LTE"})
    private String conditionOperator;

    @NotNull(message = "阈值不能为空")
    @Schema(description = "阈值", example = "80.0", required = true)
    private Double thresholdValue;

    @Schema(description = "告警级别", example = "CRITICAL", allowableValues = {"CRITICAL", "ERROR", "WARNING", "INFO"})
    private String alertLevel;

    @Schema(description = "状态", example = "ENABLED", allowableValues = {"ENABLED", "DISABLED"})
    private String status;

    @Schema(description = "持续时间（分钟）", example = "5")
    private Integer durationMinutes;

    @Schema(description = "通知渠道列表")
    private List<String> notificationChannels;

    @Schema(description = "通知用户列表")
    private List<Long> notificationUsers;

    @Schema(description = "通知间隔（分钟）", example = "30")
    private Integer notificationInterval;

    @Schema(description = "抑制持续时间（分钟）", example = "60")
    private Integer suppressionDuration;

    @Schema(description = "规则表达式", example = "cpu.usage > 80")
    private String ruleExpression;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "适用服务列表")
    private List<String> applicableServices;

    @Schema(description = "适用环境列表")
    private List<String> applicableEnvironments;
}

