package net.lab1024.sa.consume.service;

import java.math.BigDecimal;

/**
 * 支付服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
public interface PaymentService {
    /**
     * 处理支付
     * String accountNo, BigDecimal amount, String paymentMethod
     * @return Boolean
     */
    Boolean processPayment(String accountNo, BigDecimal amount, String paymentMethod);
    /**
     * 退款处理
     * Long paymentId, String reason
     * @return Boolean
     */
    Boolean refundPayment(Long paymentId, String reason);}
