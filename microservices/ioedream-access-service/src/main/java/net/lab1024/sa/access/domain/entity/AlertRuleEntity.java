package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 告警规则实体类
 * <p>
 * 用于定义各类告警的触发条件和处理规则：
 * - 规则名称和编码
 * - 触发条件（简单条件/Aviator表达式/脚本条件）
 * - 告警配置（级别、标题模板、消息模板）
 * - 通知配置（方式、接收人、模板）
 * - 高级功能（告警聚合、升级规则）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_alert_rule")
@Schema(description = "告警规则实体")
public class AlertRuleEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "规则ID")
    private Long ruleId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 规则编码（唯一）
     */
    @Schema(description = "规则编码")
    private String ruleCode;

    /**
     * 规则类型
     * DEVICE_OFFLINE-设备离线
     * DEVICE_FAULT-设备故障
     * TEMP_HIGH-温度过高
     * TEMP_LOW-温度过低
     * NETWORK_ERROR-网络异常
     * STORAGE_LOW-存储空间不足
     * POWER_LOW-电量不足
     * AUTH_FAILED-认证失败
     * DEVICE_BLOCKED-设备被阻挡
     * OTHER-其他
     */
    @Schema(description = "规则类型")
    private String ruleType;

    // ==================== 触发条件 ====================

    /**
     * 条件类型
     * 1-简单条件 2-Aviator表达式 3-脚本条件
     */
    @Schema(description = "条件类型：1-简单条件 2-Aviator表达式 3-脚本条件")
    private Integer conditionType;

    /**
     * 触发条件表达式（Aviator格式）
     * 示例：temperature > 80
     * 示例：storage_used_percent > 90
     */
    @Schema(description = "触发条件表达式")
    private String conditionExpression;

    /**
     * 条件配置（JSON格式）
     * 用于存储复杂条件的配置参数
     * 示例：{"threshold": 80, "duration": 300}
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @Schema(description = "条件配置（JSON格式）")
    private String conditionConfig;

    // ==================== 告警配置 ====================

    /**
     * 告警级别
     * 1-低 2-中 3-高 4-紧急
     */
    @Schema(description = "告警级别：1-低 2-中 3-高 4-紧急")
    private Integer alertLevel;

    /**
     * 告警标题模板
     * 支持变量替换：{deviceName}, {alertType}, {temperature}等
     */
    @Schema(description = "告警标题模板")
    private String alertTitleTemplate;

    /**
     * 告警消息模板
     * 支持变量替换和富文本格式
     */
    @Schema(description = "告警消息模板")
    private String alertMessageTemplate;

    // ==================== 通知配置 ====================

    /**
     * 通知方式（逗号分隔）
     * SMS-短信
     * EMAIL-邮件
     * PUSH-推送
     * WEBSOCKET-WebSocket
     */
    @Schema(description = "通知方式（逗号分隔）")
    private String notificationMethods;

    /**
     * 通知接收人列表（JSON数组）
     * 示例：[{"userId": 1, "userName": "张三"}, {"roleId": 2, "roleName": "管理员"}]
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @Schema(description = "通知接收人列表（JSON格式）")
    private String notificationRecipients;

    /**
     * 通知内容模板
     */
    @Schema(description = "通知内容模板")
    private String notificationTemplate;

    // ==================== 高级配置 ====================

    /**
     * 是否启用告警聚合
     * 0-否 1-是
     */
    @Schema(description = "是否启用告警聚合")
    private Integer alertAggregationEnabled;

    /**
     * 聚合时间窗口（秒）
     * 在此时间窗口内的相同类型告警会被聚合为一条
     */
    @Schema(description = "聚合时间窗口（秒）")
    private Integer aggregationWindowSeconds;

    /**
     * 是否启用告警升级
     * 0-否 1-是
     */
    @Schema(description = "是否启用告警升级")
    private Integer alertEscalationEnabled;

    /**
     * 升级规则（JSON格式）
     * 示例：[{"level": 2, "delay": 300, "action": "notify_manager"}]
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @Schema(description = "升级规则（JSON格式）")
    private String escalationRules;

    // ==================== 状态管理 ====================

    /**
     * 启用状态
     * 0-禁用 1-启用
     */
    @Schema(description = "启用状态：0-禁用 1-启用")
    private Integer enabled;

    /**
     * 优先级
     * 数字越大优先级越高，用于规则匹配顺序
     */
    @Schema(description = "优先级")
    private Integer priority;

    // ==================== 审计字段 ====================
    // 注意：createTime, updateTime, createUserId, updateUserId, deleted 从 BaseEntity 继承
}
