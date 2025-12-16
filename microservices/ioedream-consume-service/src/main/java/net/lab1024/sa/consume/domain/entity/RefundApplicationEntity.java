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
 * 退款申请实体类
 * <p>
 * 用于记录退款申请信息，支持工作流审批
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
@TableName("refund_application")
public class RefundApplicationEntity extends BaseEntity {

    /**
     * 退款申请ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 退款申请编号（业务Key，唯一）
     */
    private String refundNo;

    /**
     * 支付记录ID
     */
    private Long paymentRecordId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

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




