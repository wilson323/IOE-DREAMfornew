package net.lab1024.sa.consume.service.integration.payment;

/**
 * 支付处理器接口
 * 定义支付处理的标准接口
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface PaymentProcessor {

    /**
     * 获取支持的支付方式
     *
     * @return 支付方式
     */
    PaymentMethod getSupportedPaymentMethod();

    /**
     * 处理支付
     *
     * @param context 支付上下文
     * @return 支付结果
     */
    PaymentResult processPayment(PaymentContext context);

    /**
     * 处理退款
     *
     * @param context 退款上下文
     * @return 退款结果
     */
    RefundResult processRefund(RefundContext context);

    /**
     * 查询支付状态
     *
     * @param transactionId 交易ID
     * @return 支付状态
     */
    PaymentStatus queryPaymentStatus(String transactionId);

    /**
     * 验证支付
     *
     * @param transactionId 交易ID
     * @param amount 金额
     * @return 验证结果
     */
    boolean verifyPayment(String transactionId, java.math.BigDecimal amount);

    /**
     * 获取支付详情
     *
     * @param transactionId 交易ID
     * @return 支付详情
     */
    PaymentDetails getPaymentDetails(String transactionId);
}