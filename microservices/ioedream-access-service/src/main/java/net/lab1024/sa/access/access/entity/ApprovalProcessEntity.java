package net.lab1024.sa.access.access.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批流程实体类
 * <p>
 * 用于管理门禁审批流程
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 门禁审批流程管理
 * - 审批状态跟踪
 * - 审批历史记录
 * </p>
 * <p>
 * 数据库表：access_approval_process（根据DAO中的SQL推断）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("access_approval_process")
@SuppressWarnings("PMD.ShortVariable")
public class ApprovalProcessEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列process_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "process_id", type = IdType.AUTO)
    private Long id;

    /**
     * 流程编号
     */
    @TableField("process_no")
    private String processNo;

    /**
     * 申请人ID
     */
    @TableField("applicant_id")
    private Long applicantId;

    /**
     * 申请人姓名
     */
    @TableField("applicant_name")
    private String applicantName;

    /**
     * 申请类型
     */
    @TableField("apply_type")
    private String applyType;

    /**
     * 申请内容
     */
    @TableField("apply_content")
    private String applyContent;

    /**
     * 当前步骤
     */
    @TableField("current_step")
    private Integer currentStep;

    /**
     * 状态（PENDING-待审批，IN_PROGRESS-审批中，APPROVED-已通过，REJECTED-已拒绝）
     */
    @TableField("status")
    private String status;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 审批意见
     */
    @TableField("approval_comment")
    private String approvalComment;

    /**
     * 最终审批人ID
     */
    @TableField("final_approver_id")
    private Long finalApproverId;

    /**
     * 最终审批人姓名
     */
    @TableField("final_approver_name")
    private String finalApproverName;

    /**
     * 审批时间
     */
    @TableField("approval_time")
    private LocalDateTime approvalTime;

    /**
     * 创建时间（created_time）
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}


