package net.lab1024.sa.oa.workflow.entity;

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
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_approval_node_config")
public class ApprovalNodeConfigEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("approval_config_id")
    private Long approvalConfigId;

    @TableField("node_name")
    private String nodeName;

    @TableField("node_order")
    private Integer nodeOrder;

    @TableField("node_type")
    private String nodeType;

    @TableField("approver_type")
    private String approverType;

    @TableField("approver_config")
    private String approverConfig;

    @TableField("approver_count_limit")
    private Integer approverCountLimit;

    @TableField("pass_condition")
    private String passCondition;

    @TableField("pass_percentage")
    private Integer passPercentage;

    @TableField("condition_config")
    private String conditionConfig;

    @TableField("proxy_config")
    private String proxyConfig;

    @TableField("cc_config")
    private String ccConfig;

    @TableField("timeout_config")
    private String timeoutConfig;

    @TableField("comment_required")
    private String commentRequired;

    @TableField("attachment_required")
    private String attachmentRequired;

    @TableField("attachment_config")
    private String attachmentConfig;

    @TableField("data_permission_config")
    private String dataPermissionConfig;

    @TableField("allow_withdraw")
    private Integer allowWithdraw;

    @TableField("withdraw_config")
    private String withdrawConfig;

    @TableField("allow_urge")
    private Integer allowUrge;

    @TableField("urge_config")
    private String urgeConfig;

    @TableField("allow_comment")
    private Integer allowComment;

    @TableField("comment_config")
    private String commentConfig;

    @TableField("status")
    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("remark")
    private String remark;
}




