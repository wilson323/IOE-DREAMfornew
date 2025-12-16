package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 报销申请实体类
 * <p>
 * 用于记录报销申请信息，支持工作流审批
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reimbursement_application")
public class ReimbursementApplicationEntity extends BaseEntity {

    /**
     * 报销申请ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 报销申请编号（业务Key，唯一）
     */
    private String reimbursementNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 报销金额
     */
    private BigDecimal reimbursementAmount;

    /**
     * 报销类型
     * <p>
     * TRAVEL-差旅费
     * MEAL-餐费
     * TRANSPORT-交通费
     * OFFICE-办公费
     * OTHER-其他
     * </p>
     */
    private String reimbursementType;

    /**
     * 报销事由
     */
    private String reason;

    /**
     * 申请状态
     * <p>
     * PENDING-待审批
     * APPROVED-已通过
     * REJECTED-已驳回
     * CANCELLED-已取消
     * </p>
     */
    private String status;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 工作流实例ID
     * <p>
     * 关联OA工作流模块的流程实例ID
     * 用于查询审批状态、审批历史等
     * </p>
     */
    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}




