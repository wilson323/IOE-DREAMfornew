package net.lab1024.sa.common.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批节点配置实体
 * <p>
 * 用于配置审批流程中的节点详细信息，比钉钉更完善的节点配置
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * </p>
 * <p>
 * 功能说明：
 * - 支持多种审批人设置方式（指定人员、部门负责人、角色、发起人自选、连续多级主管等）
 * - 支持多种审批节点类型（串行、并行、会签、或签）
 * - 支持条件分支配置
 * - 支持审批代理设置
 * - 支持审批抄送配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_approval_node_config")
public class ApprovalNodeConfigEntity extends BaseEntity {

    /**
     * 节点配置ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批配置ID（外键）
     * <p>
     * 关联t_common_approval_config表的id
     * </p>
     */
    @TableField("approval_config_id")
    private Long approvalConfigId;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 节点顺序（从1开始）
     */
    @TableField("node_order")
    private Integer nodeOrder;

    /**
     * 节点类型
     * <p>
     * SERIAL-串行审批（按顺序审批）
     * PARALLEL-并行审批（同时审批，任意一人通过即可）
     * COUNTERSIGN-会签审批（同时审批，所有人都需通过）
     * OR_SIGN-或签审批（任意一人通过即可，类似并行但语义不同）
     * CONDITION-条件分支节点
     * AUTO-自动审批节点
     * </p>
     */
    @TableField("node_type")
    private String nodeType;

    /**
     * 审批人设置方式
     * <p>
     * SPECIFIED_USER-指定人员（固定审批人）
     * DIRECT_MANAGER-直属上级
     * DEPARTMENT_MANAGER-部门负责人
     * ROLE-角色（指定角色的人员）
     * INITIATOR_SELECT-发起人自选
     * CONTINUOUS_MANAGERS-连续多级主管（向上逐级审批）
     * FORM_FIELD-表单字段（从表单中获取审批人）
     * DYNAMIC_RULE-动态规则（根据规则计算审批人）
     * </p>
     */
    @TableField("approver_type")
    private String approverType;

    /**
     * 审批人配置（JSON格式）
     * <p>
     * 根据approverType不同，配置内容不同：
     * 
     * SPECIFIED_USER:
     * {
     *   "user_ids": [1, 2, 3],
     *   "user_names": ["张三", "李四", "王五"]
     * }
     * 
     * ROLE:
     * {
     *   "role_codes": ["manager", "hr"],
     *   "role_names": ["经理", "HR"]
     * }
     * 
     * CONTINUOUS_MANAGERS:
     * {
     *   "levels": 3,  // 向上审批3级
     *   "stop_condition": "approve"  // 停止条件：approve-有人通过即停止、all-全部审批
     * }
     * 
     * FORM_FIELD:
     * {
     *   "field_name": "approver_id",
     *   "field_type": "user"
     * }
     * 
     * DYNAMIC_RULE:
     * {
     *   "rule_expression": "amount > 1000 ? 'finance_manager' : 'direct_manager'",
     *   "rule_type": "groovy"  // groovy、spel等
     * }
     * </p>
     */
    @TableField("approver_config")
    private String approverConfig;

    /**
     * 审批人数量限制
     * <p>
     * 对于INITIATOR_SELECT类型，限制发起人最多选择几个审批人
     * </p>
     */
    @TableField("approver_count_limit")
    private Integer approverCountLimit;

    /**
     * 审批通过条件
     * <p>
     * ALL-全部通过
     * ANY-任意一人通过
     * MAJORITY-多数通过（超过50%）
     * PERCENTAGE-按比例通过（需配合pass_percentage使用）
     * </p>
     */
    @TableField("pass_condition")
    private String passCondition;

    /**
     * 通过比例（0-100）
     * <p>
     * 当passCondition为PERCENTAGE时使用
     * </p>
     */
    @TableField("pass_percentage")
    private Integer passPercentage;

    /**
     * 条件分支配置（JSON格式）
     * <p>
     * 当nodeType为CONDITION时使用
     * 示例：
     * {
     *   "conditions": [
     *     {
     *       "condition_expression": "amount > 1000",
     *       "next_node_order": 3,
     *       "condition_name": "大额审批"
     *     },
     *     {
     *       "condition_expression": "amount <= 1000",
     *       "next_node_order": 2,
     *       "condition_name": "小额审批"
     *     }
     *   ],
     *   "default_next_node_order": 2  // 默认下一节点
     * }
     * </p>
     */
    @TableField("condition_config")
    private String conditionConfig;

    /**
     * 审批代理配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "enable_proxy": true,
     *   "proxy_rules": [
     *     {
     *       "condition": "approver_absent",  // 审批人不在
     *       "proxy_type": "auto",  // auto-自动代理、manual-手动代理
     *       "proxy_to": "deputy_manager"  // 代理给副经理
     *     },
     *     {
     *       "condition": "approver_busy",  // 审批人忙碌
     *       "proxy_type": "auto",
     *       "proxy_to": "backup_approver"
     *     }
     *   ],
     *   "proxy_notification": true  // 是否通知原审批人
     * }
     * </p>
     */
    @TableField("proxy_config")
    private String proxyConfig;

    /**
     * 审批抄送配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "enable_cc": true,
     *   "cc_users": [10, 20, 30],  // 固定抄送人
     *   "cc_roles": ["hr", "finance"],  // 抄送角色
     *   "cc_departments": [1, 2],  // 抄送部门
     *   "cc_timing": "on_submit",  // 抄送时机：on_submit-提交时、on_approve-审批时、on_complete-完成时
     *   "cc_notification": true  // 是否发送通知
     * }
     * </p>
     */
    @TableField("cc_config")
    private String ccConfig;

    /**
     * 超时配置（JSON格式）
     * <p>
     * 节点级别的超时配置，覆盖全局超时配置
     * 示例：
     * {
     *   "timeout_hours": 24,
     *   "timeout_strategy": "escalate",  // escalate-升级、auto_approve-自动通过、notify-通知
     *   "escalate_to": "next_level",
     *   "reminder_hours": [12, 18]  // 提醒时间点（小时）
     * }
     * </p>
     */
    @TableField("timeout_config")
    private String timeoutConfig;

    /**
     * 审批意见要求
     * <p>
     * NONE-不要求
     * OPTIONAL-可选
     * REQUIRED-必填
     * REQUIRED_ON_REJECT-驳回时必填
     * </p>
     */
    @TableField("comment_required")
    private String commentRequired;

    /**
     * 附件要求
     * <p>
     * NONE-不要求
     * OPTIONAL-可选
     * REQUIRED-必填
     * </p>
     */
    @TableField("attachment_required")
    private String attachmentRequired;

    /**
     * 附件配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "max_file_size": 10485760,  // 最大文件大小（字节），10MB
     *   "allowed_file_types": ["pdf", "doc", "docx", "xls", "xlsx", "jpg", "png"],
     *   "max_file_count": 5  // 最多上传文件数
     * }
     * </p>
     */
    @TableField("attachment_config")
    private String attachmentConfig;

    /**
     * 数据权限配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "visible_fields": ["amount", "reason", "date"],  // 可见字段
     *   "editable_fields": ["reason"],  // 可编辑字段
     *   "hidden_fields": ["salary"],  // 隐藏字段
     *   "field_permissions": {
     *     "amount": {
     *       "visible": true,
     *       "editable": false
     *     }
     *   }
     * }
     * </p>
     */
    @TableField("data_permission_config")
    private String dataPermissionConfig;

    /**
     * 是否允许撤回
     * <p>
     * 0-不允许
     * 1-允许
     * </p>
     */
    @TableField("allow_withdraw")
    private Integer allowWithdraw;

    /**
     * 撤回条件（JSON格式）
     * <p>
     * 示例：
     * {
     *   "withdraw_condition": "not_approved",  // 撤回条件：not_approved-未审批、any_time-任意时间
     *   "withdraw_deadline_hours": 24  // 撤回截止时间（小时）
     * }
     * </p>
     */
    @TableField("withdraw_config")
    private String withdrawConfig;

    /**
     * 是否允许催办
     * <p>
     * 0-不允许
     * 1-允许
     * </p>
     */
    @TableField("allow_urge")
    private Integer allowUrge;

    /**
     * 催办配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "urge_interval_hours": 4,  // 催办间隔（小时）
     *   "max_urge_count": 3,  // 最多催办次数
     *   "urge_notification": true  // 是否发送催办通知
     * }
     * </p>
     */
    @TableField("urge_config")
    private String urgeConfig;

    /**
     * 是否允许评论
     * <p>
     * 0-不允许
     * 1-允许
     * </p>
     */
    @TableField("allow_comment")
    private Integer allowComment;

    /**
     * 评论配置（JSON格式）
     * <p>
     * 示例：
     * {
     *   "comment_permission": "all",  // 评论权限：all-所有人、approver-仅审批人、applicant-仅申请人
     *   "comment_visible": true  // 评论是否可见
     * }
     * </p>
     */
    @TableField("comment_config")
    private String commentConfig;

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
}

