package net.lab1024.sa.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

/**
 * 支付上下文
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Accessors(chain = true)
public class PaymentContext {
    /**
     * 支付方式
     */
    private String paymentMethod;
    /**
     * 账户编号
     */
    private String accountNo;
    /**
     * 支付金额
     */
    private java.math.BigDecimal amount;
    /**
     * 订单号
     */
    private String orderNo;
    /**
     * 备注
     */
    private String remark;}
