package net.lab1024.sa.monitor.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 告警规则添加表单
 *
 * 用于创建新的告警监控规则
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class AlertRuleAddForm {

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    private String ruleName;

    /**
     * 规则描述
     */
    @Size(max = 500, message = "规则描述长度不能超过500个字符")
    private String ruleDescription;

    /**
     * 监控指标
     */
    @NotBlank(message = "监控指标不能为空")
    @Size(max = 50, message = "监控指标长度不能超过50个字符")
    private String metricName;

    /**
     * 监控类型
     */
    @NotBlank(message = "监控类型不能为空")
    @Size(max = 50, message = "监控类型长度不能超过50个字符")
    private String monitorType;

    /**
     * 告警条件 (GT、GTE、LT、LTE、EQ、NEQ)
     */
    @NotBlank(message = "告警条件不能为空")
    @Size(max = 10, message = "告警条件长度不能超过10个字符")
    private String conditionOperator;

    /**
     * 告警阈值
     */
    @NotNull(message = "告警阈值不能为空")
    private Double thresholdValue;

    /**
     * 告警级别 (INFO、WARNING、ERROR、CRITICAL)
     */
    @NotBlank(message = "告警级别不能为空")
    @Size(max = 20, message = "告警级别长度不能超过20个字符")
    private String alertLevel;

    /**
     * 适用服务
     */
    @Size(max = 200, message = "适用服务长度不能超过200个字符")
    private String applicableServices;

    /**
     * 适用环境
     */
    @Size(max = 100, message = "适用环境长度不能超过100个字符")
    private String applicableEnvironments;

    /**
     * 规则状态 (ENABLED、DISABLED)
     */
    @Size(max = 20, message = "规则状态长度不能超过20个字符")
    private String status;

    /**
     * 持续时间（分钟）
     */
    private Integer durationMinutes;

    /**
     * 通知方式 (EMAIL、SMS、WEBHOOK、WECHAT)
     */
    @Size(max = 100, message = "通知方式长度不能超过100个字符")
    private String notificationChannels;

    /**
     * 通知人员
     */
    @Size(max = 500, message = "通知人员长度不能超过500个字符")
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
    @Size(max = 500, message = "规则表达式长度不能超过500个字符")
    private String ruleExpression;

    /**
     * 规则优先级
     */
    private Integer priority;

    /**
     * 标签
     */
    @Size(max = 200, message = "标签长度不能超过200个字符")
    private String tags;
}
