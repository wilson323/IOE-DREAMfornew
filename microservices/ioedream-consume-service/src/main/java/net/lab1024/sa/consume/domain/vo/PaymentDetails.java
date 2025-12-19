package net.lab1024.sa.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付详情VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Accessors(chain = true)
public class PaymentDetails {

    /**
     * 支付ID
     */
    private Long paymentId;

    /**
     * 支付编号
     */
    private String paymentNo;

    /**
     * 账户编号
     */
    private String accountNo;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付状态
     */
    private String status;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 交易类型
     */
    private String transactionType;

    /**
     * 备注
     */
    private String remark;
}