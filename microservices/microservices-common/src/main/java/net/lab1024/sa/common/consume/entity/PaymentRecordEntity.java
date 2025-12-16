package net.lab1024.sa.common.consume.entity;

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
 * 支付记录实体类 - 完全兼容原始Entity
 * <p>
 * 企业级支付记录管理实体，支持多种支付方式和业务场景
 * 严格遵循CLAUDE.md全局架构规范
 * 包含原始Entity的所有字段，确保100%向后兼容
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
     * 支付记录ID（主键）- 兼容原始Entity
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 支付订单号（唯一）- 兼容原始Entity
     */
    @TableField("payment_id")
    private String paymentId;

    /**
     * 交易ID（关联消费交易）- 兼容原始Entity
     */
    @TableField("transaction_id")
    private String transactionId;

    /**
     * 用户ID - 兼容原始Entity
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 支付金额 - 兼容原始Entity
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付方式 - 兼容原始Entity
     * ALIPAY-支付宝
     * WECHAT-微信支付
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付状态 - 兼容原始Entity
     * PENDING-待支付
     * SUCCESS-支付成功
     * FAILED-支付失败
     * REFUNDED-已退款
     */
    @TableField("status")
    private String status;

    /**
     * 第三方交易号 - 兼容原始Entity
     */
    @TableField("third_party_transaction_id")
    private String thirdPartyTransactionId;

    /**
     * 支付时间 - 兼容原始Entity
     */
    @TableField("payment_time")
    private LocalDateTime paymentTime;

    /**
     * 回调时间 - 兼容原始Entity
     */
    @TableField("callback_time")
    private LocalDateTime callbackTime;

    /**
     * 回调数据（JSON格式） - 兼容原始Entity
     */
    @TableField("callback_data")
    private String callbackData;

    /**
     * 备注 - 兼容原始Entity
     */
    @TableField("remark")
    private String remark;

    /**
     * 工作流实例ID - 兼容原始Entity
     * 关联OA工作流模块的流程实例ID
     * 用于查询审批状态、审批历史等
     */
    @TableField("workflow_instance_id")
    private Long workflowInstanceId;

    // =====================================================
    // 扩展业务字段（企业级功能）
    // =====================================================

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 交易流水号
     */
    @TableField("transaction_no")
    private String transactionNo;

    /**
     * 账户ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 支付渠道：1-线上 2-线下 3-移动端 4-自助设备 5-POS机
     */
    @TableField("payment_channel")
    private Integer paymentChannel;

    /**
     * 优惠金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 业务类型：1-消费 2-充值 3-退款 4-转账 5-提现 6-补贴 7-罚款
     */
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
    @TableField("merchant_name")
    private String merchantName;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 设备编码
     */
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
    @TableField("area_name")
    private String areaName;

    /**
     * 支付状态（Integer版本）：1-待支付 2-支付中 3-支付成功 4-支付失败 5-已退款 6-部分退款 7-已取消
     */
    @TableField("payment_status")
    private Integer paymentStatus;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 第三方支付订单号
     */
    @TableField("third_party_order_no")
    private String thirdPartyOrderNo;

    /**
     * 第三方支付渠道：1-微信 2-支付宝 3-银联 4-京东支付 5-其他
     */
    @TableField("third_party_channel")
    private Integer thirdPartyChannel;

    /**
     * 支付手续费
     */
    @TableField("payment_fee")
    private BigDecimal paymentFee;

    /**
     * 结算金额
     */
    @TableField("settlement_amount")
    private BigDecimal settlementAmount;

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
    @TableField("refund_reason")
    private String refundReason;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

    /**
     * 客户端IP地址
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 用户代理
     */
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
    @TableField("audit_comment")
    private String auditComment;

    /**
     * 二维码ID（关联二维码支付）
     */
    @TableField("qr_code_id")
    private String qrCodeId;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}