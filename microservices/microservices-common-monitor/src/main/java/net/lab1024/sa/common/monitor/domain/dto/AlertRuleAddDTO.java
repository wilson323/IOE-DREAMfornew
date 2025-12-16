package net.lab1024.sa.common.monitor.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 告警规则添加DTO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@NotBlank/@NotNull验证必填字段
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class AlertRuleAddDTO {

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 规则描述
     */
    private String ruleDescription;

    /**
     * 监控指标
     */
    @NotBlank(message = "监控指标不能为空")
    private String metricName;

    /**
     * 监控类型
     */
    private String monitorType;

    /**
     * 告警条件
     */
    @NotBlank(message = "告警条件不能为空")
    private String conditionOperator;

    /**
     * 告警阈值
     */
    @NotNull(message = "告警阈值不能为空")
    private Double thresholdValue;

    /**
     * 告警级别
     */
    @NotBlank(message = "告警级别不能为空")
    private String alertLevel;

    /**
     * 适用服务
     */
    private String applicableServices;

    /**
     * 适用环境
     */
    private String applicableEnvironments;

    /**
     * 规则状态
     */
    private String status;

    /**
     * 持续时间（分钟）
     */
    private Integer durationMinutes;

    /**
     * 通知方式
     */
    private String notificationChannels;

    /**
     * 通知人员
     */
    private String notificationUsers;

    /**
     * 通知频率（分钟）
     */
    private Integer notificationInterval;

    /**
     * 抑制时间（分钟）
     */
    private Integer suppressionDuration;

    /**
     * 规则表达式
     */
    private String ruleExpression;

    /**
     * 规则优先级
     */
    private Integer priority;

    /**
     * 标签
     */
    private String tags;
}

