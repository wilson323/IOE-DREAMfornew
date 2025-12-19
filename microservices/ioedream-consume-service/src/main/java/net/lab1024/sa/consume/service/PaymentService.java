package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.domain.form.RefundApplyForm;
import net.lab1024.sa.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.entity.PaymentRefundRecordEntity;

/**
 * 支付服务接口
 * <p>
 * 根据chonggou.txt和业务模块文档要求，修复方法签名不匹配问题
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
public interface PaymentService {
    /**
     * 处理支付（旧方法，保留向后兼容）
     * String accountNo, BigDecimal amount, String paymentMethod
     * 
     * @return Boolean
     */
    Boolean processPayment(String accountNo, BigDecimal amount, String paymentMethod);

    /**
     * 处理支付（表单方式，返回详细结果）
     * <p>
     * 根据chonggou.txt要求修复：返回Map<String, Object>而非Boolean
     * </p>
     * 
     * @param form 支付处理表单
     * @return 支付结果Map，包含status、paymentId、transactionNo等信息
     */
    Map<String, Object> processPayment(PaymentProcessForm form);

    /**
     * 退款处理
     * Long paymentId, String reason
     * 
     * @return Boolean
     */
    Boolean refundPayment(Long paymentId, String reason);

    /**
     * 申请退款
     * 
     * @param form 退款申请表单
     * @return 退款结果Map，包含status、refundId、refundNo等信息
     */
    Map<String, Object> applyRefund(RefundApplyForm form);

    /**
     * 审核退款
     * 
     * @param refundNo 退款编号
     * @param status   审核状态
     * @param comment  审核意见
     * @return 审核结果Map
     */
    Map<String, Object> auditRefund(String refundNo, Integer status, String comment);

    /**
     * 执行退款
     * 
     * @param refundNo 退款编号
     * @return 执行结果Map
     */
    Map<String, Object> executeRefund(String refundNo);

    /**
     * 获取支付记录
     * 
     * @param paymentId 支付ID（String类型）
     * @return 支付记录实体
     */
    PaymentRecordEntity getPaymentRecord(String paymentId);

    /**
     * 获取用户支付记录列表（分页）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 包含分页信息的支付记录Map {list: List<PaymentRecordEntity>, total: Long, pageNum: Integer, pageSize: Integer}
     */
    Map<String, Object> getUserPaymentRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取退款记录
     *
     * @param refundId 退款ID（String格式）
     * @return 包含退款记录信息的Map {record: PaymentRefundRecordEntity}
     */
    Map<String, Object> getRefundRecord(String refundId);

    /**
     * 获取用户退款记录列表（分页）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 包含分页信息的退款记录Map {list: List<PaymentRefundRecordEntity>, total: Long, pageNum: Integer, pageSize: Integer}
     */
    Map<String, Object> getUserRefundRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户支付统计
     * 
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 支付统计数据
     */
    Map<String, Object> getUserPaymentStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户退款统计
     * 
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 退款统计数据
     */
    Map<String, Object> getUserRefundStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 执行对账
     * 
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param merchantId 商户ID（可选）
     * @return 对账结果
     */
    Map<String, Object> performReconciliation(LocalDateTime startTime, LocalDateTime endTime, Long merchantId);

    /**
     * 获取商户结算统计
     * 
     * @param merchantId 商户ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 结算统计数据
     */
    Map<String, Object> getMerchantSettlementStatistics(Long merchantId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 获取待审核退款列表
     * 
     * @return 待审核退款列表
     */
    Map<String, Object> getPendingAuditRefunds();

    /**
     * 获取待处理退款列表
     * 
     * @return 待处理退款列表
     */
    Map<String, Object> getPendingProcessRefunds();

    /**
     * 获取高风险支付记录
     * 
     * @param riskLevel 风险等级阈值
     * @return 高风险支付记录列表
     */
    Map<String, Object> getHighRiskPayments(Integer riskLevel);

    /**
     * 获取高风险退款记录
     * 
     * @param riskLevel 风险等级阈值
     * @return 高风险退款记录列表
     */
    Map<String, Object> getHighRiskRefunds(Integer riskLevel);

    /**
     * 获取异常支付记录
     * 
     * @param threshold 异常阈值
     * @return 异常支付记录列表
     */
    Map<String, Object> getAbnormalPayments(Integer threshold);

    /**
     * 创建微信支付订单
     * 
     * @param accountNo   账户编号
     * @param amount      支付金额
     * @param description 支付描述
     * @param openId      用户openId
     * @param notifyUrl   回调地址
     * @return 支付结果
     */
    Map<String, Object> createWechatPayOrder(String accountNo, BigDecimal amount, String description, String openId,
            String notifyUrl);

    /**
     * 处理微信支付回调
     * 
     * @param notifyData 回调数据
     * @return 处理结果
     */
    Map<String, Object> handleWechatPayNotify(String notifyData);

    /**
     * 创建支付宝支付订单
     * 
     * @param accountNo 账户编号
     * @param amount    支付金额
     * @param subject   订单主题
     * @param returnUrl 返回地址
     * @return 支付结果
     */
    Map<String, Object> createAlipayOrder(String accountNo, BigDecimal amount, String subject, String returnUrl);

    /**
     * 处理支付宝回调
     * 
     * @param notifyParams 回调参数
     * @return 处理结果
     */
    Map<String, Object> handleAlipayNotify(Map<String, String> notifyParams);

    /**
     * 微信退款
     * 
     * @param paymentNo    支付单号
     * @param refundNo     退款单号
     * @param refundAmount 退款金额（分）
     * @param totalAmount  原订单金额（分）
     * @return 退款结果
     */
    Map<String, Object> wechatRefund(String paymentNo, String refundNo, Integer refundAmount, Integer totalAmount);

    /**
     * 支付宝退款
     * 
     * @param paymentNo    支付单号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果
     */
    Map<String, Object> alipayRefund(String paymentNo, BigDecimal refundAmount, String refundReason);

    /**
     * 创建银行卡支付订单
     * 
     * @param accountNo     账户编号
     * @param amount        支付金额
     * @param bankCardNo    银行卡号
     * @param bankName      银行名称
     * @param transactionId 交易ID
     * @return 支付结果
     */
    Map<String, Object> createBankPaymentOrder(Long accountNo, BigDecimal amount, String bankCardNo, String bankName,
            String transactionId);

    /**
     * 处理信用额度支付
     * 
     * @param accountNo   账户编号
     * @param amount      支付金额
     * @param description 支付描述
     * @param creditLimit 信用额度
     * @return 支付结果
     */
    Map<String, Object> processCreditLimitPayment(Long accountNo, BigDecimal amount, String description,
            String creditLimit);
}
