package net.lab1024.sa.common.monitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 告警规则实体
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 继承BaseEntity获取公共字段
 * - 使用@TableName指定数据库表名
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_alert_rule")
public class AlertRuleEntity extends BaseEntity {

    /**
     * 规则ID
     */
    @TableId(type = IdType.AUTO)
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
}
