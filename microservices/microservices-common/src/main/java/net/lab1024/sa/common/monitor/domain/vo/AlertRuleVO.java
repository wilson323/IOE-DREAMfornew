package net.lab1024.sa.common.monitor.domain.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 告警规则VO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@JsonFormat格式化时间字段
 * - 完整的字段注释
 * </p>
 * 整合自ioedream-monitor-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自monitor-service）
 */
@Data
@Schema(description = "告警规则VO")
public class AlertRuleVO {

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则描述")
    private String ruleDescription;

    @Schema(description = "监控指标")
    private String metricName;

    @Schema(description = "监控类型")
    private String monitorType;

    @Schema(description = "告警条件 (GT、GTE、LT、LTE、EQ、NEQ)")
    private String conditionOperator;

    @Schema(description = "告警阈值")
    private Double thresholdValue;

    @Schema(description = "告警级别 (INFO、WARNING、ERROR、CRITICAL)")
    private String alertLevel;

    @Schema(description = "规则状态 (ENABLED、DISABLED)")
    private String status;

    @Schema(description = "持续时间（分钟）")
    private Integer durationMinutes;

    @Schema(description = "通知方式 (EMAIL、SMS、WEBHOOK、WECHAT)")
    private String notificationChannels;

    @Schema(description = "通知人员")
    private String notificationUsers;

    @Schema(description = "通知频率（分钟）")
    private Integer notificationInterval;

    @Schema(description = "抑制时间（分钟）")
    private Integer suppressionDuration;

    @Schema(description = "规则表达式")
    private String ruleExpression;

    @Schema(description = "规则优先级")
    private Integer priority;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "适用服务")
    private String applicableServices;

    @Schema(description = "适用环境")
    private String applicableEnvironments;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
