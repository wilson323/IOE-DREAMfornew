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
     * 账户ID（用于与账户表关联）
     */
    @TableField("account_id")
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 支付方式
     */
    @TableField("payment_method")
    @Schema(description = "支付方式")
    private String paymentMethod;

    /**
     * 设备ID（数值型）
     */
    @TableField("device_id")
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 设备标识（兼容：部分测试/旧代码以字符串形式传入设备编号）
     */
    @TableField(exist = false)
    @Schema(description = "设备标识（非持久化，兼容字段）")
    private String deviceIdText;

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

    // ==================== 显式 Getter（避免 Lombok 在构建时未生效） ====================

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * 获取商户ID
     *
     * @return 商户ID
     */
    public Long getMerchantId() {
        return this.merchantId;
    }

    /**
     * 获取支付状态（整型兼容字段）
     *
     * @return 支付状态
     */
    public Integer getPaymentStatus() {
        return this.paymentStatus;
    }

    /**
     * 设置账户ID（兼容：long基本类型）
     *
     * @param accountId 账户ID
     */
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    /**
     * 设置账户ID
     *
     * @param accountId 账户ID
     */
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    /**
     * 设置设备ID（Long）
     *
     * @param deviceId 设备ID
     */
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * 设置设备ID（String兼容）
     * <p>
     * - 若为纯数字字符串，解析为Long写入deviceId
     * - 否则写入deviceIdText（仅用于兼容，不持久化）
     * </p>
     *
     * @param deviceId 设备ID字符串
     */
    public void setDeviceId(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            this.deviceId = null;
            this.deviceIdText = null;
            return;
        }
        String trimmed = deviceId.trim();
        try {
            this.deviceId = Long.parseLong(trimmed);
            this.deviceIdText = null;
        } catch (NumberFormatException ignore) {
            this.deviceId = null;
            this.deviceIdText = trimmed;
        }
    }

    /**
     * 设置支付方式（Integer兼容）
     * <p>
     * 兼容测试/旧代码：以数字编码设置支付方式
     * </p>
     *
     * @param paymentMethodCode 支付方式编码
     */
    public void setPaymentMethod(Integer paymentMethodCode) {
        if (paymentMethodCode == null) {
            this.paymentMethod = null;
            return;
        }
        switch (paymentMethodCode) {
            case 1:
                this.paymentMethod = "BALANCE";
                break;
            case 2:
                this.paymentMethod = "WECHAT";
                break;
            case 3:
                this.paymentMethod = "ALIPAY";
                break;
            case 4:
                this.paymentMethod = "BANK";
                break;
            case 5:
                this.paymentMethod = "CASH";
                break;
            default:
                this.paymentMethod = String.valueOf(paymentMethodCode);
                break;
        }
    }

    /**
     * 设置支付渠道（Integer兼容）
     * <p>
     * 兼容测试/旧代码：以数字编码设置支付渠道。
     * </p>
     *
     * @param paymentChannelCode 支付渠道编码
     */
    public void setPaymentChannel(Integer paymentChannelCode) {
        if (paymentChannelCode == null) {
            this.paymentChannel = null;
            return;
        }
        switch (paymentChannelCode) {
            case 1:
                this.paymentChannel = "WEB";
                break;
            case 2:
                this.paymentChannel = "POS";
                break;
            case 3:
                this.paymentChannel = "MOBILE";
                break;
            default:
                this.paymentChannel = String.valueOf(paymentChannelCode);
                break;
        }
    }

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
    @Schema(description = "业务类型")
    private Integer businessType;

    /**
     * 结算状态
     */
    @TableField("settlement_status")
    @Schema(description = "结算状态")
    private Integer settlementStatus;

    /**
     * 结算时间
     */
    @TableField("settlement_time")
    @Schema(description = "结算时间")
    private LocalDateTime settlementTime;

    /**
     * 获取业务类型
     * <p>
     * 根据chonggou.txt要求添加显式getter方法
     * </p>
     *
     * @return 业务类型
     */
    public Integer getBusinessType() {
        return this.businessType;
    }

    /**
     * 设置业务类型
     *
     * @param businessType 业务类型
     */
    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    /**
     * 获取结算状态
     *
     * @return 结算状态
     */
    public Integer getSettlementStatus() {
        return this.settlementStatus;
    }

    /**
     * 设置结算状态
     *
     * @param settlementStatus 结算状态
     */
    public void setSettlementStatus(Integer settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    /**
     * 获取结算时间
     *
     * @return 结算时间
     */
    public LocalDateTime getSettlementTime() {
        return this.settlementTime;
    }

    /**
     * 设置结算时间
     *
     * @param settlementTime 结算时间
     */
    public void setSettlementTime(LocalDateTime settlementTime) {
        this.settlementTime = settlementTime;
    }

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

    // 缺失字段 - 根据错误日志添加
    /**
     * 实际支付金额
     */
    @TableField("actual_amount")
    @Schema(description = "实际支付金额")
    private BigDecimal actualAmount;

    /**
     * 用户ID (String类型兼容)
     */
    @TableField("user_id_str")
    @Schema(description = "用户ID(String)")
    private String userIdStr;

    /**
     * 支付渠道
     */
    @TableField("payment_channel")
    @Schema(description = "支付渠道")
    private String paymentChannel;

    /**
     * 第三方交易号
     */
    @TableField("third_party_transaction_no")
    @Schema(description = "第三方交易号")
    private String thirdPartyTransactionNo;

    /**
     * 第三方订单号
     */
    @TableField("third_party_order_no")
    @Schema(description = "第三方订单号")
    private String thirdPartyOrderNo;

    /**
     * 退款状态
     */
    @TableField("refund_status")
    @Schema(description = "退款状态")
    private Integer refundStatus;

    /**
     * 支付描述
     */
    @TableField("payment_description")
    @Schema(description = "支付描述")
    private String paymentDescription;

    // 便捷方法
    public String getPaymentStatusStr() {
        return this.status;
    }

    public void setPaymentStatusStr(String paymentStatus) {
        this.status = paymentStatus;
    }

    // 缺失的getter/setter方法 - 根据错误日志添加
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getPaymentId() {
        return this.paymentId;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    public BigDecimal getActualAmount() {
        return this.actualAmount != null ? this.actualAmount : this.amount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getUserIdStr() {
        return this.userIdStr;
    }

    public void setUserIdStr(String userIdStr) {
        this.userIdStr = userIdStr;
    }

    public String getPaymentChannel() {
        return this.paymentChannel;
    }

    public void setPaymentChannel(String paymentChannel) {
        this.paymentChannel = paymentChannel;
    }

    public String getThirdPartyTransactionNo() {
        return this.thirdPartyTransactionNo;
    }

    public void setThirdPartyTransactionNo(String thirdPartyTransactionNo) {
        this.thirdPartyTransactionNo = thirdPartyTransactionNo;
    }

    public Integer getRefundStatus() {
        return this.refundStatus;
    }

    public void setRefundStatus(Integer refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getThirdPartyOrderNo() {
        return this.thirdPartyOrderNo;
    }

    public void setThirdPartyOrderNo(String thirdPartyOrderNo) {
        this.thirdPartyOrderNo = thirdPartyOrderNo;
    }

    public String getPaymentDescription() {
        return this.paymentDescription;
    }

    public void setPaymentDescription(String paymentDescription) {
        this.paymentDescription = paymentDescription;
    }

    /**
     * 手续费金额字段
     * <p>
     * 根据chonggou.txt和业务模块文档要求添加
     * </p>
     */
    @TableField("fee_amount")
    @Schema(description = "手续费金额")
    private BigDecimal feeAmount;

    /**
     * 获取支付手续费
     * <p>
     * 根据chonggou.txt要求添加
     * </p>
     *
     * @return 支付手续费
     */
    public BigDecimal getPaymentFee() {
        return this.feeAmount;
    }

    /**
     * 设置支付手续费
     *
     * @param feeAmount 手续费金额
     */
    public void setPaymentFee(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    /**
     * 获取结算金额
     * <p>
     * 根据chonggou.txt要求添加
     * 结算金额 = 支付金额 - 手续费 - 退款金额
     * </p>
     *
     * @return 结算金额
     */
    public BigDecimal getSettlementAmount() {
        // 结算金额 = 支付金额 - 手续费 - 退款金额
        BigDecimal settlement = this.amount != null ? this.amount : BigDecimal.ZERO;

        // 减去手续费
        if (this.feeAmount != null) {
            settlement = settlement.subtract(this.feeAmount);
        }

        // 减去退款金额
        if (this.refundAmount != null) {
            settlement = settlement.subtract(this.refundAmount);
        }

        return settlement;
    }
}
