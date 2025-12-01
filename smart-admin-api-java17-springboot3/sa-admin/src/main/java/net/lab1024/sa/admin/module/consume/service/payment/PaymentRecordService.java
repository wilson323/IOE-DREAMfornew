package net.lab1024.sa.admin.module.consume.service.payment;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.PaymentRecordDao;
import net.lab1024.sa.admin.module.consume.dao.RefundRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.PaymentRecordEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.RefundRecordEntity;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录服务
 * 管理所有支付交易记录，包括支付和退款记录
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Slf4j
@Service
public class PaymentRecordService {

    @Resource
    private PaymentRecordDao paymentRecordDao;

    @Resource
    private RefundRecordDao refundRecordDao;

    /**
     * 创建支付记录
     *
     * @param paymentId 支付ID
     * @param paymentRequest 支付请求参数
     * @param paymentType 支付类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPaymentRecord(String paymentId, WechatPaymentService.PaymentRequest paymentRequest,
                                  String paymentType) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setPaymentId(paymentId);
        entity.setPaymentType(paymentType);
        entity.setAmount(paymentRequest.getAmount());
        entity.setSubject("消费支付");
        entity.setBody(paymentRequest.getDescription());
        entity.setConsumeRecordId(paymentRequest.getConsumeRecordId());
        entity.setUserId(paymentRequest.getUserId());
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());

        paymentRecordDao.insert(entity);
        log.info("创建支付记录成功, paymentId: {}, amount: {}", paymentId, paymentRequest.getAmount());
    }

    /**
     * 创建支付宝支付记录
     *
     * @param paymentId 支付ID
     * @param paymentRequest 支付请求参数
     * @param paymentType 支付类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPaymentRecord(String paymentId, AlipayPaymentService.PaymentRequest paymentRequest,
                                  String paymentType) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setPaymentId(paymentId);
        entity.setPaymentType(paymentType);
        entity.setAmount(paymentRequest.getAmount());
        entity.setSubject(paymentRequest.getSubject());
        entity.setBody(paymentRequest.getBody());
        entity.setConsumeRecordId(paymentRequest.getConsumeRecordId());
        entity.setUserId(paymentRequest.getUserId());
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());

        paymentRecordDao.insert(entity);
        log.info("创建支付记录成功, paymentId: {}, amount: {}", paymentId, paymentRequest.getAmount());
    }

    /**
     * 更新支付预支付ID
     *
     * @param paymentId 支付ID
     * @param prepayId 预支付ID
     */
    public void updatePaymentPrepayId(String paymentId, String prepayId) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setPaymentId(paymentId);
        entity.setPrepayId(prepayId);
        entity.setUpdateTime(LocalDateTime.now());

        paymentRecordDao.updateByPaymentId(entity);
        log.info("更新支付预支付ID成功, paymentId: {}, prepayId: {}", paymentId, prepayId);
    }

    /**
     * 更新支付二维码
     *
     * @param paymentId 支付ID
     * @param qrCode 二维码URL
     */
    public void updatePaymentQrCode(String paymentId, String qrCode) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setPaymentId(paymentId);
        entity.setQrCode(qrCode);
        entity.setUpdateTime(LocalDateTime.now());

        paymentRecordDao.updateByPaymentId(entity);
        log.info("更新支付二维码成功, paymentId: {}", paymentId);
    }

    /**
     * 更新支付表单数据
     *
     * @param paymentId 支付ID
     * @param formData 表单数据
     */
    public void updatePaymentFormData(String paymentId, String formData) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setPaymentId(paymentId);
        entity.setFormData(formData);
        entity.setUpdateTime(LocalDateTime.now());

        paymentRecordDao.updateByPaymentId(entity);
        log.info("更新支付表单数据成功, paymentId: {}", paymentId);
    }

    /**
     * 更新支付订单字符串
     *
     * @param paymentId 支付ID
     * @param orderString 订单字符串
     */
    public void updatePaymentOrderString(String paymentId, String orderString) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setPaymentId(paymentId);
        entity.setOrderString(orderString);
        entity.setUpdateTime(LocalDateTime.now());

        paymentRecordDao.updateByPaymentId(entity);
        log.info("更新支付订单字符串成功, paymentId: {}", paymentId);
    }

    /**
     * 更新支付状态
     *
     * @param paymentId 支付ID
     * @param status 状态
     * @param thirdPartyTransactionId 第三方交易ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatus(String paymentId, String status, String thirdPartyTransactionId) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setPaymentId(paymentId);
        entity.setStatus(status);
        entity.setThirdPartyTransactionId(thirdPartyTransactionId);
        entity.setUpdateTime(LocalDateTime.now());

        // 如果支付成功，记录支付时间
        if ("SUCCESS".equals(status) || "TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status)) {
            entity.setPaymentTime(LocalDateTime.now());
        }

        paymentRecordDao.updateByPaymentId(entity);
        log.info("更新支付状态成功, paymentId: {}, status: {}", paymentId, status);
    }

    /**
     * 处理支付成功
     *
     * @param paymentId 支付ID
     * @param thirdPartyTransactionId 第三方交易ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentSuccess(String paymentId, String thirdPartyTransactionId) {
        // 更新支付记录
        updatePaymentStatus(paymentId, "SUCCESS", thirdPartyTransactionId);

        // 更新消费记录状态
        PaymentRecordEntity paymentRecord = paymentRecordDao.selectByPaymentId(paymentId);
        if (paymentRecord != null && paymentRecord.getConsumeRecordId() != null) {
            // 这里需要调用消费记录服务更新状态
            // consumeRecordService.updatePaymentStatus(paymentRecord.getConsumeRecordId(), "PAID");
            log.info("更新消费记录支付状态, consumeRecordId: {}", paymentRecord.getConsumeRecordId());
        }

        log.info("处理支付成功完成, paymentId: {}", paymentId);
    }

    /**
     * 创建退款记录
     *
     * @param refundId 退款ID
     * @param refundRequest 退款请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void createRefundRecord(String refundId, WechatPaymentService.RefundRequest refundRequest) {
        RefundRecordEntity entity = new RefundRecordEntity();
        entity.setRefundId(refundId);
        entity.setPaymentId(refundRequest.getPaymentId());
        entity.setRefundAmount(refundRequest.getRefundAmount());
        entity.setReason(refundRequest.getReason());
        entity.setConsumeRecordId(refundRequest.getConsumeRecordId());
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());

        refundRecordDao.insert(entity);
        log.info("创建退款记录成功, refundId: {}, amount: {}", refundId, refundRequest.getRefundAmount());
    }

    /**
     * 创建支付宝退款记录
     *
     * @param refundId 退款ID
     * @param refundRequest 退款请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void createRefundRecord(String refundId, AlipayPaymentService.RefundRequest refundRequest) {
        RefundRecordEntity entity = new RefundRecordEntity();
        entity.setRefundId(refundId);
        entity.setPaymentId(refundRequest.getPaymentId());
        entity.setRefundAmount(refundRequest.getRefundAmount());
        entity.setReason(refundRequest.getReason());
        entity.setConsumeRecordId(refundRequest.getConsumeRecordId());
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());

        refundRecordDao.insert(entity);
        log.info("创建退款记录成功, refundId: {}, amount: {}", refundId, refundRequest.getRefundAmount());
    }

    /**
     * 更新退款状态
     *
     * @param refundId 退款ID
     * @param status 状态
     * @param thirdPartyRefundId 第三方退款ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRefundStatus(String refundId, String status, String thirdPartyRefundId) {
        RefundRecordEntity entity = new RefundRecordEntity();
        entity.setRefundId(refundId);
        entity.setStatus(status);
        entity.setThirdPartyRefundId(thirdPartyRefundId);
        entity.setUpdateTime(LocalDateTime.now());

        // 如果退款成功，记录退款时间
        if ("SUCCESS".equals(status)) {
            entity.setRefundTime(LocalDateTime.now());
        }

        refundRecordDao.updateByRefundId(entity);
        log.info("更新退款状态成功, refundId: {}, status: {}", refundId, status);
    }

    /**
     * 根据支付ID查询支付记录
     *
     * @param paymentId 支付ID
     * @return 支付记录
     */
    public PaymentRecordEntity getPaymentRecord(String paymentId) {
        return paymentRecordDao.selectByPaymentId(paymentId);
    }

    /**
     * 根据消费记录ID查询支付记录
     *
     * @param consumeRecordId 消费记录ID
     * @return 支付记录
     */
    public PaymentRecordEntity getPaymentRecordByConsumeRecordId(Long consumeRecordId) {
        return paymentRecordDao.selectByConsumeRecordId(consumeRecordId);
    }

    /**
     * 根据退款ID查询退款记录
     *
     * @param refundId 退款ID
     * @return 退款记录
     */
    public RefundRecordEntity getRefundRecord(String refundId) {
        return refundRecordDao.selectByRefundId(refundId);
    }
}