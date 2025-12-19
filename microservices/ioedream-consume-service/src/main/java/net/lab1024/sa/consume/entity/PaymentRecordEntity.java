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
 * 支付记录实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_payment_record")
@Schema(description = "支付记录实体")
public class PaymentRecordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 支付ID (业务主键)
     */
    @TableField("payment_id")
    @Schema(description = "支付ID")
    private Long paymentId;

    /**
     * 支付编号
     */
    @TableField("payment_no")
    @Schema(description = "支付编号")
    private String paymentNo;

    /**
     * 账户编号
     */
    @TableField("account_no")
    @Schema(description = "账户编号")
    private String accountNo;

    /**
     * 支付方式
     */
    @TableField("payment_method")
    @Schema(description = "支付方式")
    private String paymentMethod;

    /**
     * 支付金额 (实体字段)
     */
    @TableField("amount")
    @Schema(description = "支付金额")
    private java.math.BigDecimal amount;

    /**
     * 支付金额 (DB列名兼容)
     */
    @TableField("payment_amount")
    @Schema(description = "支付金额(兼容)")
    private java.math.BigDecimal paymentAmount;

    /**
     * 支付时间
     */
    @TableField("payment_time")
    @Schema(description = "支付时间")
    private java.time.LocalDateTime paymentTime;

    /**
     * 支付状态
     */
    @TableField("status")
    @Schema(description = "支付状态")
    private String status;

    /**
     * 支付状态 (Int兼容)
     */
    @TableField("payment_status")
    private Integer paymentStatus;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 商户ID
     */
    @TableField("merchant_id")
    @Schema(description = "商户ID")
    private Long merchantId;

    /**
     * 订单号
     */
    @TableField("order_no")
    @Schema(description = "订单号")
    private String orderNo;

    /**
     * 交易流水号
     */
    @TableField("transaction_no")
    private String transactionNo;

    /**
     * 交易类型
     */
    @TableField("transaction_type")
    @Schema(description = "交易类型")
    private String transactionType;

    /**
     * 业务类型
     */
    @TableField("business_type")
    private Integer businessType;

    /**
     * 结算状态
     */
    @TableField("settlement_status")
    private Integer settlementStatus;

    /**
     * 结算时间
     */
    @TableField("settlement_time")
    private LocalDateTime settlementTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 风险等级
     */
    @TableField("risk_level")
    private Integer riskLevel;

    /**
     * 审计状态
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 备注信息
     */
    @TableField("remark")
    @Schema(description = "备注信息")
    private String remark;

    // 便捷方法
    public String getPaymentStatusStr() {
        return this.status;
    }

    public void setPaymentStatusStr(String paymentStatus) {
        this.status = paymentStatus;
    }
}
