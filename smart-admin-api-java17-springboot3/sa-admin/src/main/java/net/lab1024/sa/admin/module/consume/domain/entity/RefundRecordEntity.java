package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Data
@TableName("t_consume_refund_record")
public class RefundRecordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 退款ID（业务唯一标识）
     */
    @TableField("refund_id")
    private String refundId;

    /**
     * 关联的支付ID
     */
    @TableField("payment_id")
    private String paymentId;

    /**
     * 关联的消费记录ID
     */
    @TableField("consume_record_id")
    private Long consumeRecordId;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 退款状态（PENDING, SUCCESS, FAILED, CANCELLED）
     */
    @TableField("status")
    private String status;

    /**
     * 第三方退款ID（微信refund_id, 支付宝refund_no等）
     */
    @TableField("third_party_refund_id")
    private String thirdPartyRefundId;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

    /**
     * 退款渠道（同支付渠道）
     */
    @TableField("refund_channel")
    private String refundChannel;

    /**
     * 退款方式（ORIGINAL, BALANCE, BANK_CARD等）
     */
    @TableField("refund_way")
    private String refundWay;

    /**
     * 退款手续费
     */
    @TableField("refund_fee")
    private BigDecimal refundFee;

    /**
     * 退款申请时间
     */
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /**
     * 退款处理时间
     */
    @TableField("process_time")
    private LocalDateTime processTime;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 扩展信息（JSON格式）
     */
    @TableField("extend_info")
    private String extendInfo;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;
}