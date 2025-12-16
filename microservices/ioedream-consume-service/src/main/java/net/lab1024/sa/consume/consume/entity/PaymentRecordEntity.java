package net.lab1024.sa.consume.consume.entity;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 * <p>
 * 企业级支付记录管理实体，支持多种支付方式和业务场景
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_payment_record")
public class PaymentRecordEntity extends BaseEntity {

    /**
     * 支付记录ID
     */
    @TableId(value = "payment_id", type = IdType.ASSIGN_ID)
    private String paymentId;

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    @Size(max = 100, message = "订单号长度不能超过100个字符")
    @TableField("order_no")
    private String orderNo;

    /**
     * 交易流水号
     */
    @NotBlank(message = "交易流水号不能为空")
    @Size(max = 100, message = "交易流水号长度不能超过100个字符")
    @TableField("transaction_no")
    private String transactionNo;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @TableField("user_id")
    private Long userId;

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    @TableField("account_id")
    private Long accountId;

    /**
     * 支付方式：1-余额支付 2-微信支付 3-支付宝 4-银行卡 5-现金 6-二维码 7- NFC 8-生物识别
     */
    @NotNull(message = "支付方式不能为空")
    @TableField("payment_method")
    private Integer paymentMethod;

    /**
     * 支付渠道：1-线上 2-线下 3-移动端 4-自助设备 5-POS机
     */
    @NotNull(message = "支付渠道不能为空")
    @TableField("payment_channel")
    private Integer paymentChannel;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @TableField("payment_amount")
    private java.math.BigDecimal paymentAmount;

    /**
     * 优惠金额
     */
    @TableField("discount_amount")
    private java.math.BigDecimal discountAmount;

    /**
     * 实付金额
     */
    @NotNull(message = "实付金额不能为空")
    @TableField("actual_amount")
    private java.math.BigDecimal actualAmount;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private java.math.BigDecimal refundAmount;

    /**
     * 业务类型：1-消费 2-充值 3-退款 4-转账 5-提现 6-补贴 7-罚款
     */
    @NotNull(message = "业务类型不能为空")
    @TableField("business_type")
    private Integer businessType;

    /**
     * 商户ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 商户名称
     */
    @Size(max = 200, message = "商户名称长度不能超过200个字符")
    @TableField("merchant_name")
    private String merchantName;

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @Size(max = 100, message = "设备ID长度不能超过100个字符")
    @TableField("device_id")
    private String deviceId;

    /**
     * 设备编码
     */
    @Size(max = 100, message = "设备编码长度不能超过100个字符")
    @TableField("device_code")
    private String deviceCode;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 区域名称
     */
    @Size(max = 200, message = "区域名称长度不能超过200个字符")
    @TableField("area_name")
    private String areaName;

    /**
     * 支付状态：1-待支付 2-支付中 3-支付成功 4-支付失败 5-已退款 6-部分退款 7-已取消
     */
    @NotNull(message = "支付状态不能为空")
    @TableField("payment_status")
    private Integer paymentStatus;

    /**
     * 支付时间
     */
    @TableField("payment_time")
    private LocalDateTime paymentTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 第三方支付订单号
     */
    @Size(max = 200, message = "第三方支付订单号长度不能超过200个字符")
    @TableField("third_party_order_no")
    private String thirdPartyOrderNo;

    /**
     * 第三方交易号
     */
    @Size(max = 200, message = "第三方交易号长度不能超过200个字符")
    @TableField("third_party_transaction_no")
    private String thirdPartyTransactionNo;

    /**
     * 第三方支付渠道：1-微信 2-支付宝 3-银联 4-京东支付 5-其他
     */
    @TableField("third_party_channel")
    private Integer thirdPartyChannel;

    /**
     * 支付手续费
     */
    @TableField("payment_fee")
    private java.math.BigDecimal paymentFee;

    /**
     * 结算金额
     */
    @TableField("settlement_amount")
    private java.math.BigDecimal settlementAmount;

    /**
     * 结算状态：1-未结算 2-已结算 3-结算失败 4-部分结算
     */
    @TableField("settlement_status")
    private Integer settlementStatus;

    /**
     * 结算时间
     */
    @TableField("settlement_time")
    private LocalDateTime settlementTime;

    /**
     * 退款原因
     */
    @Size(max = 500, message = "退款原因长度不能超过500个字符")
    @TableField("refund_reason")
    private String refundReason;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

    /**
     * 备注信息
     */
    @Size(max = 1000, message = "备注信息长度不能超过1000个字符")
    @TableField("remark")
    private String remark;

    /**
     * 客户端IP地址
     */
    @Size(max = 45, message = "客户端IP地址长度不能超过45个字符")
    @TableField("client_ip")
    private String clientIp;

    /**
     * 用户代理
     */
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @TableField("user_agent")
    private String userAgent;

    /**
     * 地理位置（纬度）
     */
    @TableField("latitude")
    private Double latitude;

    /**
     * 地理位置（经度）
     */
    @TableField("longitude")
    private Double longitude;

    /**
     * 位置描述
     */
    @Size(max = 200, message = "位置描述长度不能超过200个字符")
    @TableField("location_description")
    private String locationDescription;

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
     * 审核状态：0-无需审核 1-待审核 2-审核通过 3-审核拒绝
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    @TableField("audit_user_id")
    private Long auditUserId;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    @Size(max = 500, message = "审核意见长度不能超过500个字符")
    @TableField("audit_comment")
    private String auditComment;

    /**
     * 二维码ID（关联二维码支付）
     */
    @Size(max = 100, message = "二维码ID长度不能超过100个字符")
    @TableField("qr_code_id")
    private String qrCodeId;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    // =====================================================
    // 兼容方法：为了兼容domain.entity.PaymentRecordEntity的方法名
    // =====================================================

    /**
     * 获取支付金额（兼容方法）
     * <p>
     * 兼容domain.entity.PaymentRecordEntity的getAmount()方法
     * 实际返回paymentAmount字段的值
     * </p>
     *
     * @return 支付金额
     */
    public BigDecimal getAmount() {
        return this.paymentAmount;
    }

    /**
     * 设置支付金额（兼容方法）
     * <p>
     * 兼容domain.entity.PaymentRecordEntity的setAmount()方法
     * 实际设置paymentAmount字段的值
     * </p>
     *
     * @param amount 支付金额
     */
    public void setAmount(BigDecimal amount) {
        this.paymentAmount = amount;
    }

    /**
     * 获取回调时间（兼容方法）
     * <p>
     * 兼容domain.entity.PaymentRecordEntity的getCallbackTime()方法
     * 实际返回completeTime字段的值
     * </p>
     *
     * @return 完成时间（作为回调时间）
     */
    public LocalDateTime getCallbackTime() {
        return this.completeTime;
    }

    /**
     * 设置回调时间（兼容方法）
     * <p>
     * 兼容domain.entity.PaymentRecordEntity的setCallbackTime()方法
     * 实际设置completeTime字段的值
     * </p>
     *
     * @param callbackTime 回调时间
     */
    public void setCallbackTime(LocalDateTime callbackTime) {
        this.completeTime = callbackTime;
    }

    /**
     * 获取支付状态（兼容方法）
     * <p>
     * 兼容domain.entity.PaymentRecordEntity的getStatus()方法
     * 将Integer类型的paymentStatus转换为String类型
     * </p>
     *
     * @return 支付状态字符串
     */
    public String getStatus() {
        if (this.paymentStatus == null) {
            return null;
        }
        // 将Integer状态转换为String
        switch (this.paymentStatus) {
            case 1:
                return "PENDING";
            case 2:
                return "PROCESSING";
            case 3:
                return "SUCCESS";
            case 4:
                return "FAILED";
            case 5:
                return "REFUNDED";
            case 6:
                return "PARTIAL_REFUND";
            case 7:
                return "CANCELLED";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 设置支付状态（兼容方法）
     * <p>
     * 兼容domain.entity.PaymentRecordEntity的setStatus()方法
     * 将String类型的status转换为Integer类型的paymentStatus
     * </p>
     *
     * @param status 支付状态字符串
     */
    public void setStatus(String status) {
        if (status == null) {
            this.paymentStatus = null;
            return;
        }
        // 将String状态转换为Integer
        switch (status.toUpperCase()) {
            case "PENDING":
                this.paymentStatus = 1;
                break;
            case "PROCESSING":
                this.paymentStatus = 2;
                break;
            case "SUCCESS":
                this.paymentStatus = 3;
                break;
            case "FAILED":
                this.paymentStatus = 4;
                break;
            case "REFUNDED":
                this.paymentStatus = 5;
                break;
            case "PARTIAL_REFUND":
                this.paymentStatus = 6;
                break;
            case "CANCELLED":
                this.paymentStatus = 7;
                break;
            default:
                this.paymentStatus = null;
        }
    }
}



