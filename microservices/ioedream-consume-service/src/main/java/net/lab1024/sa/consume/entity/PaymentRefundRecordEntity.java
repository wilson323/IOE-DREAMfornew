package net.lab1024.sa.consume.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 支付退款记录实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment_refund_record")
@Schema(description = "支付退款记录实体")
public class PaymentRefundRecordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 退款ID
     */
    @TableField("refund_id")
    @Schema(description = "退款ID")
    private Long refundId;
    /**
     * 退款编号
     */
    @TableField("refund_no")
    @Schema(description = "退款编号")
    private String refundNo;
    /**
     * 原支付ID
     */
    @TableField("payment_id")
    @Schema(description = "原支付ID")
    private Long paymentId;
    /**
     * 退款金额
     */
    @TableField("refund_amount")
    @Schema(description = "退款金额")
    private java.math.BigDecimal refundAmount;
    /**
     * 退款时间
     */
    @TableField("refund_time")
    @Schema(description = "退款时间")
    private java.time.LocalDateTime refundTime;
    /**
     * 退款状态
     */
    @TableField("status")
    @Schema(description = "退款状态")
    private String status;

    /**
     * 申请人ID
     */
    @TableField("applicant_id")
    @Schema(description = "申请人ID")
    private Long applicantId;

    /**
     * 退款状态（详细状态）
     */
    @TableField("refund_status")
    @Schema(description = "退款状态（详细状态）")
    private Integer refundStatus;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    @Schema(description = "完成时间")
    private java.time.LocalDateTime completeTime;

    /**
     * 审核人ID
     */
    @TableField("auditor_id")
    @Schema(description = "审核人ID")
    private Long auditorId;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    @Schema(description = "审核时间")
    private java.time.LocalDateTime auditTime;

    /**
     * 审核意见
     */
    @TableField("audit_comment")
    @Schema(description = "审核意见")
    private String auditComment;

    /**
     * 处理人ID
     */
    @TableField("processor_id")
    @Schema(description = "处理人ID")
    private Long processorId;

    /**
     * 处理时间
     */
    @TableField("process_time")
    @Schema(description = "处理时间")
    private java.time.LocalDateTime processTime;

    /**
     * 第三方退款单号
     */
    @TableField("third_party_refund_no")
    @Schema(description = "第三方退款单号")
    private String thirdPartyRefundNo;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    /**
     * 退款交易流水号
     * <p>
     * 根据chonggou.txt和业务模块文档要求添加
     * </p>
     */
    @TableField("refund_transaction_no")
    @Schema(description = "退款交易流水号")
    private String refundTransactionNo;

    /**
     * 申请时间
     * <p>
     * 根据chonggou.txt要求添加
     * </p>
     */
    @TableField("apply_time")
    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    /**
     * 实际退款金额
     * <p>
     * 根据chonggou.txt要求添加
     * 可能与refundAmount不同（如部分退款、手续费扣除等）
     * </p>
     */
    @TableField("actual_refund_amount")
    @Schema(description = "实际退款金额")
    private BigDecimal actualRefundAmount;

    /**
     * 退款类型
     * <p>
     * 根据chonggou.txt要求添加
     * 1-全额退款 2-部分退款 3-自动退款 4-手动退款
     * </p>
     */
    @TableField("refund_type")
    @Schema(description = "退款类型")
    private Integer refundType;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
