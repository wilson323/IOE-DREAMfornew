package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 告警规则表单
 * <p>
 * 用于创建或更新告警规则
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
@Schema(description = "告警规则表单")
public class AlertRuleForm {

    /**
     * 规则ID（更新时必填）
     */
    @Schema(description = "规则ID", example = "1")
    private Long ruleId;

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    @Schema(description = "规则名称", example = "设备离线告警")
    private String ruleName;

    /**
     * 规则编码
     */
    @NotBlank(message = "规则编码不能为空")
    @Size(max = 50, message = "规则编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z_]+$", message = "规则编码只能包含大写字母和下划线")
    @Schema(description = "规则编码", example = "DEVICE_OFFLINE")
    private String ruleCode;

    /**
     * 规则类型
     */
    @NotBlank(message = "规则类型不能为空")
    @Schema(description = "规则类型", example = "DEVICE_OFFLINE")
    private String ruleType;

    /**
     * 条件类型
     * 1-简单条件 2-Aviator表达式 3-脚本条件
     */
    @NotNull(message = "条件类型不能为空")
    @Min(value = 1, message = "条件类型值不正确")
    @Max(value = 3, message = "条件类型值不正确")
    @Schema(description = "条件类型", example = "1")
    private Integer conditionType;

    /**
     * 触发条件表达式
     */
    @Schema(description = "触发条件表达式", example = "temperature > 80")
    private String conditionExpression;

    /**
     * 条件配置（JSON格式）
     */
    @Schema(description = "条件配置（JSON格式）", example = "{\"threshold\": 80, \"duration\": 300}")
    private String conditionConfig;

    /**
     * 告警级别
     * 1-低 2-中 3-高 4-紧急
     */
    @NotNull(message = "告警级别不能为空")
    @Min(value = 1, message = "告警级别值不正确")
    @Max(value = 4, message = "告警级别值不正确")
    @Schema(description = "告警级别", example = "3")
    private Integer alertLevel;

    /**
     * 告警标题模板
     */
    @Size(max = 200, message = "告警标题模板长度不能超过200个字符")
    @Schema(description = "告警标题模板", example = "设备温度异常：{deviceName}温度为{temperature}℃")
    private String alertTitleTemplate;

    /**
     * 告警消息模板
     */
    @Schema(description = "告警消息模板")
    private String alertMessageTemplate;

    /**
     * 通知方式（逗号分隔）
     * SMS, EMAIL, PUSH, WEBSOCKET
     */
    @Schema(description = "通知方式", example = "SMS,EMAIL,PUSH")
    private String notificationMethods;

    /**
     * 通知接收人列表（JSON数组）
     */
    @Schema(description = "通知接收人列表（JSON格式）")
    private String notificationRecipients;

    /**
     * 通知内容模板
     */
    @Schema(description = "通知内容模板")
    private String notificationTemplate;

    /**
     * 是否启用告警聚合
     * 0-否 1-是
     */
    @Schema(description = "是否启用告警聚合", example = "0")
    private Integer alertAggregationEnabled;

    /**
     * 聚合时间窗口（秒）
     */
    @Min(value = 60, message = "聚合时间窗口不能少于60秒")
    @Max(value = 3600, message = "聚合时间窗口不能超过3600秒")
    @Schema(description = "聚合时间窗口（秒）", example = "300")
    private Integer aggregationWindowSeconds;

    /**
     * 是否启用告警升级
     * 0-否 1-是
     */
    @Schema(description = "是否启用告警升级", example = "0")
    private Integer alertEscalationEnabled;

    /**
     * 升级规则（JSON格式）
     */
    @Schema(description = "升级规则（JSON格式）")
    private String escalationRules;

    /**
     * 启用状态
     * 0-禁用 1-启用
     */
    @Schema(description = "启用状态", example = "1")
    private Integer enabled;

    /**
     * 优先级
     */
    @Min(value = 0, message = "优先级不能为负数")
    @Max(value = 999, message = "优先级不能超过999")
    @Schema(description = "优先级", example = "100")
    private Integer priority;
}
