package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Data
@TableName("t_consume_payment_record")
public class PaymentRecordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 支付ID（业务唯一标识）
     */
    @TableField("payment_id")
    private String paymentId;

    /**
     * 支付类型（WECHAT, ALIPAY, ALIPAY_APP, WECHAT_NATIVE等）
     */
    @TableField("payment_type")
    private String paymentType;

    /**
     * 支付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付主题/商品名称
     */
    @TableField("subject")
    private String subject;

    /**
     * 支付描述/商品详情
     */
    @TableField("body")
    private String body;

    /**
     * 关联的消费记录ID
     */
    @TableField("consume_record_id")
    private Long consumeRecordId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 支付状态（PENDING, SUCCESS, FAILED, CLOSED, REFUNDED）
     */
    @TableField("status")
    private String status;

    /**
     * 第三方交易ID（微信trade_no, 支付宝trade_no等）
     */
    @TableField("third_party_transaction_id")
    private String thirdPartyTransactionId;

    /**
     * 预支付ID（微信prepay_id）
     */
    @TableField("prepay_id")
    private String prepayId;

    /**
     * 二维码URL（Native支付）
     */
    @TableField("qr_code")
    private String qrCode;

    /**
     * 支付表单数据（网页支付）
     */
    @TableField("form_data")
    private String formData;

    /**
     * 订单字符串（APP支付）
     */
    @TableField("order_string")
    private String orderString;

    /**
     * 支付时间
     */
    @TableField("payment_time")
    private LocalDateTime paymentTime;

    /**
     * 支付渠道（WEB, APP, SCAN_CODE等）
     */
    @TableField("payment_channel")
    private String paymentChannel;

    /**
     * 支付IP地址
     */
    @TableField("payment_ip")
    private String paymentIp;

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