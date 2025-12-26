package net.lab1024.sa.common.monitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警规则视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "告警规则视图对象")
public class AlertRuleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "规则ID", example = "1001")
    private Long id;

    @Schema(description = "规则名称", example = "CPU使用率告警")
    private String ruleName;

    @Schema(description = "规则描述", example = "当CPU使用率超过80%时触发告警")
    private String ruleDescription;

    @Schema(description = "监控指标名称", example = "cpu.usage")
    private String metricName;

    @Schema(description = "监控类型", example = "METRIC")
    private String monitorType;

    @Schema(description = "条件操作符", example = "GT")
    private String conditionOperator;

    @Schema(description = "阈值", example = "80.0")
    private Double thresholdValue;

    @Schema(description = "告警级别", example = "CRITICAL")
    private String alertLevel;

    @Schema(description = "状态", example = "ENABLED")
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

    @Schema(description = "创建时间", example = "2025-12-20 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-20 10:00:00")
    private LocalDateTime updateTime;
}

