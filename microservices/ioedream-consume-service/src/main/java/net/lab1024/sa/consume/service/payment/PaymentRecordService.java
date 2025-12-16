package net.lab1024.sa.consume.service.payment;

import net.lab1024.sa.consume.consume.entity.PaymentRecordEntity;

/**
 * 支付记录服务接口
 * <p>
 * 提供支付记录相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface PaymentRecordService {

    /**
     * 根据支付订单号获取支付记录
     *
     * @param paymentId 支付订单号
     * @return 支付记录
     */
    PaymentRecordEntity getPaymentRecord(String paymentId);

    /**
     * 保存支付记录
     *
     * @param paymentRecord 支付记录
     * @return 保存的支付记录
     */
    PaymentRecordEntity savePaymentRecord(PaymentRecordEntity paymentRecord);

    /**
     * 更新支付记录状态
     *
     * @param paymentId 支付订单号
     * @param status 支付状态
     * @param thirdPartyTransactionId 第三方交易号
     * @return 是否更新成功
     */
    boolean updatePaymentStatus(String paymentId, String status, String thirdPartyTransactionId);

    /**
     * 处理支付成功回调
     *
     * @param paymentId 支付订单号
     * @param transactionId 交易号
     */
    void handlePaymentSuccess(String paymentId, String transactionId);
}



