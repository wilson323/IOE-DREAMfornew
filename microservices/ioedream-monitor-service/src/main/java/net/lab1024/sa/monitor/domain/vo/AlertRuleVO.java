package net.lab1024.sa.monitor.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 告警规则VO
 *
 * 用于展示告警规则的详细信息，包括规则配置、状态、通知设置等
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class AlertRuleVO {

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则描述
     */
    private String ruleDescription;

    /**
     * 监控指标
     */
    private String metricName;

    /**
     * 监控类型
     */
    private String monitorType;

    /**
     * 告警条件 (GT、GTE、LT、LTE、EQ、NEQ)
     */
    private String conditionOperator;

    /**
     * 告警阈值
     */
    private Double thresholdValue;

    /**
     * 告警级别 (INFO、WARNING、ERROR、CRITICAL)
     */
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
     * 规则状态 (ENABLED、DISABLED)
     */
    private String status;

    /**
     * 持续时间（分钟）
     */
    private Integer durationMinutes;

    /**
     * 通知方式 (EMAIL、SMS、WEBHOOK、WECHAT)
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 更新用户ID
     */
    private Long updateUserId;

    /**
     * 创建用户姓名
     */
    private String createUserName;

    /**
     * 更新用户姓名
     */
    private String updateUserName;
}
