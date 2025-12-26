package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警规则VO
 * <p>
 * 用于返回告警规则信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "告警规则VO")
public class AlertRuleVO {

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "1")
    private Long ruleId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "设备离线告警")
    private String ruleName;

    /**
     * 规则编码
     */
    @Schema(description = "规则编码", example = "DEVICE_OFFLINE")
    private String ruleCode;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型", example = "DEVICE_OFFLINE")
    private String ruleType;

    /**
     * 规则类型名称
     */
    @Schema(description = "规则类型名称", example = "设备离线")
    private String ruleTypeName;

    /**
     * 条件类型
     * 1-简单条件 2-Aviator表达式 3-脚本条件
     */
    @Schema(description = "条件类型", example = "1")
    private Integer conditionType;

    /**
     * 条件类型名称
     */
    @Schema(description = "条件类型名称", example = "简单条件")
    private String conditionTypeName;

    /**
     * 触发条件表达式
     */
    @Schema(description = "触发条件表达式", example = "temperature > 80")
    private String conditionExpression;

    /**
     * 条件配置
     */
    @Schema(description = "条件配置")
    private String conditionConfig;

    /**
     * 告警级别
     * 1-低 2-中 3-高 4-紧急
     */
    @Schema(description = "告警级别", example = "3")
    private Integer alertLevel;

    /**
     * 告警级别名称
     */
    @Schema(description = "告警级别名称", example = "高")
    private String alertLevelName;

    /**
     * 告警标题模板
     */
    @Schema(description = "告警标题模板", example = "设备温度异常：{deviceName}")
    private String alertTitleTemplate;

    /**
     * 告警消息模板
     */
    @Schema(description = "告警消息模板")
    private String alertMessageTemplate;

    /**
     * 通知方式（逗号分隔）
     */
    @Schema(description = "通知方式", example = "SMS,EMAIL,PUSH")
    private String notificationMethods;

    /**
     * 通知接收人列表
     */
    @Schema(description = "通知接收人列表")
    private String notificationRecipients;

    /**
     * 通知内容模板
     */
    @Schema(description = "通知内容模板")
    private String notificationTemplate;

    /**
     * 是否启用告警聚合
     */
    @Schema(description = "是否启用告警聚合", example = "0")
    private Integer alertAggregationEnabled;

    /**
     * 聚合时间窗口（秒）
     */
    @Schema(description = "聚合时间窗口（秒）", example = "300")
    private Integer aggregationWindowSeconds;

    /**
     * 是否启用告警升级
     */
    @Schema(description = "是否启用告警升级", example = "0")
    private Integer alertEscalationEnabled;

    /**
     * 升级规则
     */
    @Schema(description = "升级规则")
    private String escalationRules;

    /**
     * 启用状态
     */
    @Schema(description = "启用状态", example = "1")
    private Integer enabled;

    /**
     * 优先级
     */
    @Schema(description = "优先级", example = "100")
    private Integer priority;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30T00:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-30T00:00:00")
    private LocalDateTime updateTime;
}
