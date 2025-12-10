package net.lab1024.sa.common.workflow.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批配置实体
 * <p>
 * 用于配置业务类型与审批流程的映射关系，支持动态配置审批流程
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * </p>
 * <p>
 * 功能说明：
 * - 支持自定义业务类型与审批流程的映射
 * - 支持审批规则配置（金额阈值、天数阈值等）
 * - 支持审批后处理配置
 * - 支持多级审批配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_approval_config")
public class ApprovalConfigEntity extends BaseEntity {

    /**
     * 配置ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型（唯一标识）
     * <p>
     * 示例：ATTENDANCE_LEAVE、CONSUME_REFUND、CUSTOM_APPROVAL_001等
     * 支持自定义业务类型，不局限于枚举
     * </p>
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 业务类型名称
     * <p>
     * 用于显示，如：请假审批、退款审批、自定义审批等
     * </p>
     */
    @TableField("business_type_name")
    private String businessTypeName;

    /**
     * 所属模块
     * <p>
     * 示例：考勤模块、消费模块、自定义模块等
     * </p>
     */
    @TableField("module")
    private String module;

    /**
     * 流程定义ID
     * <p>
     * 关联t_common_workflow_definition表的id
     * 如果为null，表示使用默认流程
     * </p>
     */
    @TableField("definition_id")
    private Long definitionId;

    /**
     * 流程定义Key（备用）
     * <p>
     * 如果definitionId为空，可通过processKey查找流程定义
     * </p>
     */
    @TableField("process_key")
    private String processKey;

    /**
     * 审批规则配置（JSON格式）
     * <p>
     * 比钉钉更完善的审批规则配置
     * 示例：
     * {
     *   "amount_threshold": 1000,        // 金额阈值（超过此金额需要多级审批）
     *   "days_threshold": 3,             // 天数阈值（超过此天数需要多级审批）
     *   "auto_approve_conditions": {     // 自动审批条件
     *     "max_amount": 100,
     *     "max_days": 1,
     *     "conditions": [
     *       {
     *         "field": "amount",
     *         "operator": "<=",
     *         "value": 100
     *       }
     *     ]
     *   },
     *   "approval_levels": [             // 审批层级配置（已迁移到ApprovalNodeConfigEntity）
     *     {
     *       "level": 1,
     *       "approver_type": "direct_manager",
     *       "required": true
     *     }
     *   ],
     *   "form_validation": {              // 表单验证规则（比钉钉更完善）
     *     "rules": [
     *       {
     *         "field": "amount",
     *         "required": true,
     *         "min": 0,
     *         "max": 1000000,
     *         "pattern": "^\\d+(\\.\\d{1,2})?$"
     *       }
     *     ]
     *   },
     *   "print_template": {              // 打印模板配置（比钉钉更完善）
     *     "template_id": 1,
     *     "template_name": "审批单打印模板",
     *     "print_fields": ["amount", "reason", "date"]
     *   },
     *   "export_config": {               // 导出配置（比钉钉更完善）
     *     "export_formats": ["excel", "pdf"],
     *     "export_fields": ["all"],  // all-全部字段、custom-自定义字段
     *     "custom_fields": ["amount", "reason"]
     *   }
     * }
     * </p>
     */
    @TableField("approval_rules")
    private String approvalRules;

    /**
     * 审批后处理配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "on_approved": {                 // 审批通过后的处理
     *     "handler_type": "service_call", // 处理类型：service_call-服务调用、script-脚本执行
     *     "service_name": "attendance-service",
     *     "method": "processLeaveApproval",
     *     "params": {
     *       "employeeId": "${employeeId}",
     *       "leaveDays": "${leaveDays}"
     *     }
     *   },
     *   "on_rejected": {                 // 审批驳回后的处理
     *     "handler_type": "notification",
     *     "notify_channels": ["email", "sms"]
     *   }
     * }
     * </p>
     */
    @TableField("post_approval_handler")
    private String postApprovalHandler;

    /**
     * 超时配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "timeout_hours": 24,              // 超时时间（小时）
     *   "timeout_strategy": "escalate",   // 超时策略：escalate-升级、auto_approve-自动通过、notify-通知
     *   "escalate_to": "next_level"       // 升级到下一级
     *   "auto_approve_after_hours": 48    // 超过此时间自动通过
     * }
     * </p>
     */
    @TableField("timeout_config")
    private String timeoutConfig;

    /**
     * 通知配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "notify_applicant": true,         // 是否通知申请人
     *   "notify_approver": true,          // 是否通知审批人
     *   "notify_channels": ["email", "sms", "wechat"], // 通知渠道
     *   "notify_on_submit": true,         // 提交时通知
     *   "notify_on_approve": true,        // 审批时通知
     *   "notify_on_reject": true          // 驳回时通知
     * }
     * </p>
     */
    @TableField("notification_config")
    private String notificationConfig;

    /**
     * 适用范围配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "departments": [1, 2, 3],         // 适用部门ID列表
     *   "roles": ["manager", "employee"], // 适用角色列表
     *   "employee_levels": [1, 2, 3],    // 适用员工级别列表
     *   "exclude_departments": [10],      // 排除部门ID列表
     *   "exclude_roles": ["guest"]        // 排除角色列表
     * }
     * </p>
     */
    @TableField("applicable_scope")
    private String applicableScope;

    /**
     * 状态（ENABLED-启用 DISABLED-禁用）
     */
    @TableField("status")
    private String status;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间（null表示永久有效）
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 审批模板ID（外键）
     * <p>
     * 关联t_common_approval_template表的id
     * 如果从模板创建，记录模板ID
     * </p>
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 是否允许打印
     * <p>
     * 0-不允许
     * 1-允许
     * </p>
     */
    @TableField("allow_print")
    private Integer allowPrint;

    /**
     * 是否允许导出
     * <p>
     * 0-不允许
     * 1-允许
     * </p>
     */
    @TableField("allow_export")
    private Integer allowExport;

    /**
     * 是否启用统计
     * <p>
     * 0-不启用
     * 1-启用
     * </p>
     */
    @TableField("enable_statistics")
    private Integer enableStatistics;

    /**
     * 审批流程设计（JSON格式）
     * <p>
     * 比钉钉更完善的流程设计配置
     * 支持可视化流程设计器的配置存储
     * 示例：
     * {
     *   "nodes": [
     *     {
     *       "node_id": "node_1",
     *       "node_name": "部门经理审批",
     *       "node_type": "SERIAL",
     *       "x": 100,
     *       "y": 200,
     *       "width": 120,
     *       "height": 60
     *     }
     *   ],
     *   "edges": [
     *     {
     *       "source": "node_1",
     *       "target": "node_2",
     *       "condition": "amount > 1000"
     *     }
     *   ]
     * }
     * </p>
     */
    @TableField("process_design")
    private String processDesign;
}

