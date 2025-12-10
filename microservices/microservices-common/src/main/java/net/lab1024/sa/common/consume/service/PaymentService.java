package net.lab1024.sa.common.consume.service;

import net.lab1024.sa.common.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity;
import net.lab1024.sa.common.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.common.consume.domain.form.RefundApplyForm;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 * <p>
 * 企业级支付服务接口，提供完整的支付、退款、对账等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
public interface PaymentService {

    /**
     * 处理支付
     *
     * @param form 支付处理表单
     * @return 支付结果
     */
    Map<String, Object> processPayment(PaymentProcessForm form);

    /**
     * 申请退款
     *
     * @param form 退款申请表单
     * @return 申请结果
     */
    Map<String, Object> applyRefund(RefundApplyForm form);

    /**
     * 审核退款
     *
     * @param refundId 退款记录ID
     * @param auditStatus 审核状态
     * @param auditComment 审核意见
     * @return 审核结果
     */
    Map<String, Object> auditRefund(String refundId, Integer auditStatus, String auditComment);

    /**
     * 执行退款
     *
     * @param refundId 退款记录ID
     * @return 执行结果
     */
    Map<String, Object> executeRefund(String refundId);

    /**
     * 查询支付记录
     *
     * @param paymentId 支付记录ID
     * @return 支付记录
     */
    PaymentRecordEntity getPaymentRecord(String paymentId);

    /**
     * 查询用户支付记录
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 支付记录列表
     */
    List<PaymentRecordEntity> getUserPaymentRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询退款记录
     *
     * @param refundId 退款记录ID
     * @return 退款记录
     */
    PaymentRefundRecordEntity getRefundRecord(String refundId);

    /**
     * 查询用户退款记录
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 退款记录列表
     */
    List<PaymentRefundRecordEntity> getUserRefundRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户支付统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getUserPaymentStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户退款统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getUserRefundStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 执行对账
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param merchantId 商户ID（可选）
     * @return 对账结果
     */
    Map<String, Object> performReconciliation(LocalDateTime startTime, LocalDateTime endTime, Long merchantId);

    /**
     * 获取商户结算统计
     *
     * @param merchantId 商户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 结算统计
     */
    Map<String, Object> getMerchantSettlementStatistics(Long merchantId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询待审核退款列表
     *
     * @return 待审核退款列表
     */
    List<PaymentRefundRecordEntity> getPendingAuditRefunds();

    /**
     * 查询待处理退款列表
     *
     * @return 待处理退款列表
     */
    List<PaymentRefundRecordEntity> getPendingProcessRefunds();

    /**
     * 查询高风险支付记录
     *
     * @param hours 小时数
     * @return 高风险支付记录列表
     */
    List<PaymentRecordEntity> getHighRiskPayments(Integer hours);

    /**
     * 查询高风险退款记录
     *
     * @param hours 小时数
     * @return 高风险退款记录列表
     */
    List<PaymentRefundRecordEntity> getHighRiskRefunds(Integer hours);

    /**
     * 查询异常支付记录
     *
     * @param hours 小时数
     * @return 异常支付记录列表
     */
    List<PaymentRecordEntity> getAbnormalPayments(Integer hours);

    /**
     * 创建微信支付订单
     *
     * @param orderId     订单ID（业务订单号）
     * @param amount      金额（单位：元）
     * @param description 商品描述
     * @param openId      用户OpenID（JSAPI支付时必需）
     * @param payType     支付类型（JSAPI/APP/H5/Native）
     * @return 支付参数
     */
    Map<String, Object> createWechatPayOrder(String orderId, BigDecimal amount, String description, String openId, String payType);

    /**
     * 处理微信支付回调通知
     *
     * @param notifyData 通知数据（JSON格式，微信支付平台发送的加密数据）
     * @return 处理结果
     */
    Map<String, Object> handleWechatPayNotify(String notifyData);

    /**
     * 创建支付宝支付订单
     *
     * @param orderId 订单ID
     * @param amount 金额（元）
     * @param subject 商品标题
     * @param payType 支付类型（APP/Web/Wap）
     * @return 支付参数
     */
    Map<String, Object> createAlipayOrder(String orderId, BigDecimal amount, String subject, String payType);

    /**
     * 处理支付宝支付回调通知
     *
     * @param params 通知参数
     * @return 处理结果（"success"或"fail"）
     */
    String handleAlipayNotify(Map<String, String> params);

    /**
     * 微信支付退款
     *
     * @param orderId 订单ID
     * @param refundId 退款单ID
     * @param totalAmount 订单总金额（分）
     * @param refundAmount 退款金额（分）
     * @return 退款结果
     */
    Map<String, Object> wechatRefund(String orderId, String refundId, Integer totalAmount, Integer refundAmount);

    /**
     * 支付宝退款
     *
     * @param orderId 订单ID
     * @param refundAmount 退款金额（元）
     * @param reason 退款原因（可选）
     * @return 退款结果
     */
    Map<String, Object> alipayRefund(String orderId, BigDecimal refundAmount, String reason);

    /**
     * 创建银行支付订单
     *
     * @param accountId 账户ID
     * @param amount 金额（元）
     * @param orderId 订单ID
     * @param description 商品描述
     * @param bankCardNo 银行卡号（可选）
     * @return 支付结果
     */
    Map<String, Object> createBankPaymentOrder(Long accountId, BigDecimal amount, String orderId, String description, String bankCardNo);

    /**
     * 处理信用额度支付
     *
     * @param accountId 账户ID
     * @param amount 金额（元）
     * @param orderId 订单ID
     * @param reason 扣除原因（可选）
     * @return 支付结果
     */
    Map<String, Object> processCreditLimitPayment(Long accountId, BigDecimal amount, String orderId, String reason);
}
