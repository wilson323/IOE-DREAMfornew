package net.lab1024.sa.oa.workflow.entity;

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
     */
    @TableField("approval_rules")
    private String approvalRules;

    /**
     * 审批后处理配置（JSON格式）
     */
    @TableField("post_approval_handler")
    private String postApprovalHandler;

    /**
     * 超时配置（JSON格式）
     */
    @TableField("timeout_config")
    private String timeoutConfig;

    /**
     * 通知配置（JSON格式）
     */
    @TableField("notification_config")
    private String notificationConfig;

    /**
     * 适用范围配置（JSON格式）
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
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 是否允许打印
     */
    @TableField("allow_print")
    private Integer allowPrint;

    /**
     * 是否允许导出
     */
    @TableField("allow_export")
    private Integer allowExport;

    /**
     * 是否启用统计
     */
    @TableField("enable_statistics")
    private Integer enableStatistics;

    /**
     * 审批流程设计（JSON格式）
     */
    @TableField("process_design")
    private String processDesign;
}




