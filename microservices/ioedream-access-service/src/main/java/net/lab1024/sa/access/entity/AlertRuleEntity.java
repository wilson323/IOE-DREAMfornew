package net.lab1024.sa.access.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警规则实体
 *
 * 定义告警触发规则、条件、级别等
 * 支持多种规则类型：
 * - 阈值规则（THRESHOLD）：字段值达到阈值时触发
 * - 条件规则（CONDITION）：满足特定条件时触发
 * - 组合规则（COMPOSITE）：多个条件组合触发
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_alert_rule")
@Schema(description = "告警规则实体")
public class AlertRuleEntity {

    /**
     * 规则ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "规则ID", example = "1")
    private Long ruleId;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    @Schema(description = "规则名称", example = "设备离线告警")
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 规则编码（唯一标识）
     */
    @TableField("rule_code")
    @Schema(description = "规则编码", example = "DEVICE_OFFLINE")
    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;

    /**
     * 规则分类：DEVICE-设备 OFFLINE-离线 FAULT-故障 SECURITY-安全
     */
    @TableField("rule_category")
    @Schema(description = "规则分类", example = "OFFLINE")
    @NotBlank(message = "规则分类不能为空")
    private String ruleCategory;

    /**
     * 规则类型：THRESHOLD-阈值 CONDITION-条件 COMPOSITE-组合
     */
    @TableField("rule_type")
    @Schema(description = "规则类型", example = "THRESHOLD")
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    /**
     * 触发条件（JSON格式）
     * 示例：{"type":"THRESHOLD","field":"battery_level","operator":"<=","value":20}
     */
    @TableField("trigger_condition")
    @Schema(description = "触发条件（JSON）", example = "{\"type\":\"THRESHOLD\",\"field\":\"battery_level\",\"operator\":\"<=\",\"value\":20}")
    @NotBlank(message = "触发条件不能为空")
    private String triggerCondition;

    /**
     * 告警级别：1-提示 2-警告 3-严重 4-紧急
     */
    @TableField("alert_level")
    @Schema(description = "告警级别", example = "3")
    @NotNull(message = "告警级别不能为空")
    private Integer alertLevel;

    /**
     * 是否启用升级：0-否 1-是
     */
    @TableField("escalation_enabled")
    @Schema(description = "是否启用升级", example = "1")
    private Integer escalationEnabled;

    /**
     * 升级规则（JSON数组）
     * 示例：[{"level":3,"duration":300,"unit":"SECONDS"},{"level":4,"duration":600,"unit":"SECONDS"}]
     */
    @TableField("escalation_rules")
    @Schema(description = "升级规则（JSON）", example = "[{\"level\":3,\"duration\":300,\"unit\":\"SECONDS\"}]")
    private String escalationRules;

    /**
     * 是否启用聚合：0-否 1-是
     */
    @TableField("aggregation_enabled")
    @Schema(description = "是否启用聚合", example = "1")
    private Integer aggregationEnabled;

    /**
     * 聚合时间窗口（秒）
     */
    @TableField("aggregation_window")
    @Schema(description = "聚合时间窗口（秒）", example = "60")
    private Integer aggregationWindow;

    /**
     * 聚合阈值（窗口内相同告警超过此数量才发送）
     */
    @TableField("aggregation_threshold")
    @Schema(description = "聚合阈值", example = "5")
    private Integer aggregationThreshold;

    /**
     * 是否启用通知：0-否 1-是
     */
    @TableField("notification_enabled")
    @Schema(description = "是否启用通知", example = "1")
    private Integer notificationEnabled;

    /**
     * 通知方式（逗号分隔）：EMAIL,SMS,WEBSOCKET
     */
    @TableField("notification_methods")
    @Schema(description = "通知方式", example = "WEBSOCKET,SMS")
    private String notificationMethods;

    /**
     * 通知模板
     */
    @TableField("notification_template")
    @Schema(description = "通知模板", example = "设备{deviceName}触发{alertType}告警")
    private String notificationTemplate;

    /**
     * 设备范围：1-全部设备 2-指定设备类型 3-指定设备
     */
    @TableField("device_scope")
    @Schema(description = "设备范围", example = "1")
    private Integer deviceScope;

    /**
     * 设备范围值（JSON）
     */
    @TableField("device_scope_value")
    @Schema(description = "设备范围值（JSON）", example = "[\"DEV001\",\"DEV002\"]")
    private String deviceScopeValue;

    /**
     * 时间生效：1-全天 2-工作日 3-自定义时间段
     */
    @TableField("time_effective")
    @Schema(description = "时间生效", example = "1")
    private Integer timeEffective;

    /**
     * 时间生效值（JSON）
     */
    @TableField("time_effective_value")
    @Schema(description = "时间生效值（JSON）", example = "{\"startTime\":\"08:00\",\"endTime\":\"18:00\"}")
    private String timeEffectiveValue;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField("status")
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 优先级（1-100，数值越大优先级越高）
     */
    @TableField("priority")
    @Schema(description = "优先级", example = "50")
    private Integer priority;

    /**
     * 规则描述
     */
    @TableField("description")
    @Schema(description = "规则描述", example = "设备超过5分钟无心跳信号时触发告警")
    private String description;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注", example = "默认启用")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "2025-12-26T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2025-12-26T10:00:00")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Schema(description = "版本号", example = "0")
    private Integer version;

    // ==================== 便捷方法 ====================

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 判断是否启用升级
     */
    public boolean isEscalationEnabled() {
        return this.escalationEnabled != null && this.escalationEnabled == 1;
    }

    /**
     * 判断是否启用聚合
     */
    public boolean isAggregationEnabled() {
        return this.aggregationEnabled != null && this.aggregationEnabled == 1;
    }

    /**
     * 判断是否启用通知
     */
    public boolean isNotificationEnabled() {
        return this.notificationEnabled != null && this.notificationEnabled == 1;
    }

    /**
     * 判断是否阈值规则
     */
    public boolean isThresholdRule() {
        return "THRESHOLD".equals(this.ruleType);
    }

    /**
     * 判断是否条件规则
     */
    public boolean isConditionRule() {
        return "CONDITION".equals(this.ruleType);
    }

    /**
     * 判断是否组合规则
     */
    public boolean isCompositeRule() {
        return "COMPOSITE".equals(this.ruleType);
    }

    /**
     * 判断是否紧急告警
     */
    public boolean isEmergencyRule() {
        return this.alertLevel != null && this.alertLevel == 4;
    }

    /**
     * 判断是否严重告警
     */
    public boolean isSevereRule() {
        return this.alertLevel != null && this.alertLevel >= 3;
    }
}
