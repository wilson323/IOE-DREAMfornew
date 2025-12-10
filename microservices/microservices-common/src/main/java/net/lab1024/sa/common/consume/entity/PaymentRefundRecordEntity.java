package net.lab1024.sa.common.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 * <p>
 * 企业级退款记录管理实体，支持多种退款场景和审核流程
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_payment_refund_record")
public class PaymentRefundRecordEntity extends BaseEntity {

    /**
     * 退款记录ID
     */
    @TableId(value = "refund_id", type = IdType.ASSIGN_ID)
    private String refundId;

    /**
     * 原支付记录ID
     */
    @NotNull(message = "原支付记录ID不能为空")
    @TableField("payment_id")
    private String paymentId;

    /**
     * 退款单号
     */
    @NotBlank(message = "退款单号不能为空")
    @Size(max = 100, message = "退款单号长度不能超过100个字符")
    @TableField("refund_no")
    private String refundNo;

    /**
     * 退款流水号
     */
    @NotBlank(message = "退款流水号不能为空")
    @Size(max = 100, message = "退款流水号长度不能超过100个字符")
    @TableField("refund_transaction_no")
    private String refundTransactionNo;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @TableField("user_id")
    private Long userId;

    /**
     * 原支付金额
     */
    @NotNull(message = "原支付金额不能为空")
    @TableField("original_amount")
    private java.math.BigDecimal originalAmount;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    @TableField("refund_amount")
    private java.math.BigDecimal refundAmount;

    /**
     * 退款手续费
     */
    @TableField("refund_fee")
    private java.math.BigDecimal refundFee;

    /**
     * 实退金额
     */
    @NotNull(message = "实退金额不能为空")
    @TableField("actual_refund_amount")
    private java.math.BigDecimal actualRefundAmount;

    /**
     * 退款方式：1-原路退回 2-余额退款 3-银行卡退款 4-现金退款 5-人工退款
     */
    @NotNull(message = "退款方式不能为空")
    @TableField("refund_method")
    private Integer refundMethod;

    /**
     * 退款类型：1-全额退款 2-部分退款 3-订单取消 4-商品退货 5-服务退款 6-其他
     */
    @NotNull(message = "退款类型不能为空")
    @TableField("refund_type")
    private Integer refundType;

    /**
     * 退款原因：1-用户申请 2-商户退款 3-系统异常 4-风控拦截 5-其他
     */
    @NotNull(message = "退款原因不能为空")
    @TableField("refund_reason_type")
    private Integer refundReasonType;

    /**
     * 退款原因描述
     */
    @NotBlank(message = "退款原因描述不能为空")
    @Size(max = 1000, message = "退款原因描述长度不能超过1000个字符")
    @TableField("refund_reason_desc")
    private String refundReasonDesc;

    /**
     * 申请人ID
     */
    @TableField("applicant_id")
    private Long applicantId;

    /**
     * 申请人类型：1-用户 2-商户 3-管理员 4-系统
     */
    @TableField("applicant_type")
    private Integer applicantType;

    /**
     * 退款状态：1-申请中 2-审核中 3-审核通过 4-审核拒绝 5-退款中 6-退款成功 7-退款失败 8-已取消
     */
    @NotNull(message = "退款状态不能为空")
    @TableField("refund_status")
    private Integer refundStatus;

    /**
     * 申请时间
     */
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /**
     * 审核人ID
     */
    @TableField("auditor_id")
    private Long auditorId;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    @Size(max = 1000, message = "审核意见长度不能超过1000个字符")
    @TableField("audit_comment")
    private String auditComment;

    /**
     * 处理人ID
     */
    @TableField("processor_id")
    private Long processorId;

    /**
     * 处理时间
     */
    @TableField("process_time")
    private LocalDateTime processTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 第三方退款单号
     */
    @Size(max = 200, message = "第三方退款单号长度不能超过200个字符")
    @TableField("third_party_refund_no")
    private String thirdPartyRefundNo;

    /**
     * 第三方退款渠道：1-微信 2-支付宝 3-银联 4-京东支付 5-其他
     */
    @TableField("third_party_refund_channel")
    private Integer thirdPartyRefundChannel;

    /**
     * 风险等级：1-低风险 2-中风险 3-高风险 4-极高风险
     */
    @TableField("risk_level")
    private Integer riskLevel;

    /**
     * 风险描述
     */
    @Size(max = 500, message = "风险描述长度不能超过500个字符")
    @TableField("risk_description")
    private String riskDescription;

    /**
     * 附件信息（JSON格式，存储相关凭证）
     */
    @TableField("attachment_info")
    private String attachmentInfo;

    /**
     * 备注信息
     */
    @Size(max = 1000, message = "备注信息长度不能超过1000个字符")
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}