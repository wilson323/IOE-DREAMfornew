package net.lab1024.sa.consume.service.payment.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.service.payment.PaymentRecordService;
import net.lab1024.sa.consume.service.AccountService;
import net.lab1024.sa.consume.domain.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.dao.PaymentRecordDao;

/**
 * 支付记录服务实现类
 * <p>
 * 实现支付记录管理的核心业务功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识服务类
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 遵循四层架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 支付记录创建和查询
 * - 支付状态更新
 * - 支付回调处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class PaymentRecordServiceImpl implements PaymentRecordService {

    @Resource
    private PaymentRecordDao paymentRecordDao;

    @Resource
    private AccountService accountService;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 根据支付订单号获取支付记录
     *
     * @param paymentId 支付订单号
     * @return 支付记录
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentRecordEntity getPaymentRecord(String paymentId) {
        log.info("[支付记录] 查询支付记录，paymentId={}", paymentId);

        try {
            // 参数验证
            if (paymentId == null || paymentId.trim().isEmpty()) {
                log.warn("[支付记录] 支付订单号不能为空");
                return null;
            }

            // 实现具体的查询逻辑
            PaymentRecordEntity record = paymentRecordDao.selectByPaymentId(paymentId);

            log.info("[支付记录] 查询支付记录成功，paymentId={}, status={}", paymentId, record != null ? record.getStatus() : null);
            return record;

        } catch (Exception e) {
            log.error("[支付记录] 查询支付记录失败，paymentId={}", paymentId, e);
            return null;
        }
    }

    /**
     * 保存支付记录
     *
     * @param paymentRecord 支付记录
     * @return 保存的支付记录
     */
    @Override
    public PaymentRecordEntity savePaymentRecord(PaymentRecordEntity paymentRecord) {
        // 参数验证（必须在方法开始处进行，确保健壮性）
        if (paymentRecord == null) {
            throw new IllegalArgumentException("支付记录不能为空");
        }

        log.info("[支付记录] 保存支付记录，paymentId={}, amount={}",
                paymentRecord.getPaymentId(), paymentRecord.getAmount());

        try {

            if (paymentRecord.getPaymentId() == null || paymentRecord.getPaymentId().trim().isEmpty()) {
                throw new IllegalArgumentException("支付订单号不能为空");
            }

            if (paymentRecord.getAmount() == null || paymentRecord.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("支付金额必须大于0");
            }

            // 设置默认状态
            if (paymentRecord.getStatus() == null || paymentRecord.getStatus().trim().isEmpty()) {
                paymentRecord.setStatus("PENDING");
            }

            // 设置创建时间
            if (paymentRecord.getCreateTime() == null) {
                paymentRecord.setCreateTime(LocalDateTime.now());
            }

            // 实现具体的保存逻辑
            int result = paymentRecordDao.insert(paymentRecord);

            log.info("[支付记录] 保存支付记录成功，paymentId={}, status={}, result={}",
                    paymentRecord.getPaymentId(), paymentRecord.getStatus(), result);

            return paymentRecord;

        } catch (Exception e) {
            log.error("[支付记录] 保存支付记录失败，paymentId={}",
                    paymentRecord != null ? paymentRecord.getPaymentId() : "null", e);
            throw e;
        }
    }

    /**
     * 更新支付记录状态
     *
     * @param paymentId 支付订单号
     * @param status 支付状态
     * @param thirdPartyTransactionId 第三方交易号
     * @return 是否更新成功
     */
    @Override
    public boolean updatePaymentStatus(String paymentId, String status, String thirdPartyTransactionId) {
        log.info("[支付记录] 更新支付状态，paymentId={}, status={}, thirdPartyTransactionId={}",
                paymentId, status, thirdPartyTransactionId);

        try {
            // 参数验证
            if (paymentId == null || paymentId.trim().isEmpty()) {
                log.warn("[支付记录] 支付订单号不能为空");
                return false;
            }

            if (status == null || status.trim().isEmpty()) {
                log.warn("[支付记录] 支付状态不能为空");
                return false;
            }

            // 实现具体的更新逻辑
            LambdaUpdateWrapper<PaymentRecordEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PaymentRecordEntity::getPaymentId, paymentId)
                      .set(PaymentRecordEntity::getStatus, status)
                      .set(PaymentRecordEntity::getUpdateTime, LocalDateTime.now());

            if (thirdPartyTransactionId != null) {
                updateWrapper.set(PaymentRecordEntity::getThirdPartyTransactionId, thirdPartyTransactionId);
            }

            if (status.equals("SUCCESS")) {
                updateWrapper.set(PaymentRecordEntity::getPaymentTime, LocalDateTime.now());
            }

            int updated = paymentRecordDao.update(null, updateWrapper);

            log.info("[支付记录] 更新支付状态{}，paymentId={}, status={}, updated={}",
                    updated > 0 ? "成功" : "失败", paymentId, status, updated);

            return updated > 0;

        } catch (Exception e) {
            log.error("[支付记录] 更新支付状态异常，paymentId={}, status={}", paymentId, status, e);
            return false;
        }
    }

    /**
     * 处理支付成功回调
     *
     * @param paymentId 支付订单号
     * @param transactionId 交易号
     */
    @Override
    public void handlePaymentSuccess(String paymentId, String transactionId) {
        log.info("[支付记录] 处理支付成功回调，paymentId={}, transactionId={}", paymentId, transactionId);

        try {
            // 参数验证
            if (paymentId == null || paymentId.trim().isEmpty()) {
                throw new IllegalArgumentException("支付订单号不能为空");
            }

            // 查询支付记录
            PaymentRecordEntity paymentRecord = getPaymentRecord(paymentId);
            if (paymentRecord == null) {
                log.error("[支付记录] 支付记录不存在，paymentId={}", paymentId);
                throw new RuntimeException("支付记录不存在");
            }

            // 检查当前状态
            if ("SUCCESS".equals(paymentRecord.getStatus())) {
                log.warn("[支付记录] 支付记录已经是成功状态，paymentId={}", paymentId);
                return;
            }

            if ("REFUNDED".equals(paymentRecord.getStatus())) {
                log.warn("[支付记录] 支付记录已退款，不能更新为成功状态，paymentId={}", paymentId);
                return;
            }

            // 更新状态为成功
            boolean updated = updatePaymentStatus(paymentId, "SUCCESS", transactionId);
            if (!updated) {
                throw new RuntimeException("更新支付状态失败");
            }

            // 支付成功后的业务处理
            processPaymentSuccessBusiness(paymentRecord);

            log.info("[支付记录] 支付成功回调处理完成，paymentId={}, transactionId={}", paymentId, transactionId);

        } catch (Exception e) {
            log.error("[支付记录] 支付成功回调处理异常，paymentId={}, transactionId={}",
                    paymentId, transactionId, e);
            throw new RuntimeException("支付成功回调处理失败", e);
        }
    }

    /**
     * 处理支付成功后的业务逻辑
     * <p>
     * 业务场景：
     * 1. 更新账户余额（如果是充值类支付）
     * 2. 发送支付成功通知
     * 3. 记录审计日志
     * </p>
     *
     * @param paymentRecord 支付记录
     */
    private void processPaymentSuccessBusiness(PaymentRecordEntity paymentRecord) {
        try {
            // 1. 更新账户余额（如果支付记录关联了账户ID）
            if (paymentRecord.getUserId() != null && paymentRecord.getAmount() != null) {
                // 通过交易ID查找关联的账户ID
                // 注意：这里假设支付记录中的userId就是账户所属用户
                // 实际业务中可能需要通过transactionId查找对应的账户
                Long accountId = findAccountIdByUserId(paymentRecord.getUserId());
                if (accountId != null) {
                    boolean balanceUpdated = accountService.addBalance(
                            accountId,
                            paymentRecord.getAmount(),
                            "支付成功：" + paymentRecord.getPaymentId()
                    );
                    if (balanceUpdated) {
                        log.info("[支付记录] 账户余额更新成功，accountId={}, amount={}",
                                accountId, paymentRecord.getAmount());
                    } else {
                        log.warn("[支付记录] 账户余额更新失败，accountId={}, amount={}",
                                accountId, paymentRecord.getAmount());
                    }
                }
            }

            // 2. 发送支付成功通知
            if (paymentRecord.getUserId() != null) {
                sendPaymentSuccessNotification(
                        paymentRecord.getUserId(),
                        paymentRecord.getPaymentId(),
                        paymentRecord.getAmount()
                );
            }

        } catch (Exception e) {
            // 业务处理失败不影响支付状态更新，记录日志即可
            log.error("[支付记录] 支付成功业务处理异常，paymentId={}, error={}",
                    paymentRecord.getPaymentId(), e.getMessage(), e);
        }
    }

    /**
     * 根据用户ID查找账户ID
     *
     * @param userId 用户ID
     * @return 账户ID，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    private Long findAccountIdByUserId(Long userId) {
        try {
            // 通过GatewayServiceClient调用账户服务查询账户ID
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            ResponseDTO<Object> response = gatewayServiceClient.callConsumeService(
                    "/api/v1/account/getByUserId",
                    HttpMethod.GET,
                    params,
                    Object.class
            );

            if (response != null && response.getCode() == 200 && response.getData() != null) {
                Object dataObj = response.getData();
                if (dataObj instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    Object accountIdObj = data.get("accountId");
                    if (accountIdObj instanceof Number) {
                        return ((Number) accountIdObj).longValue();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[支付记录] 查询账户ID失败，userId={}, error={}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * 发送支付成功通知
     *
     * @param userId 用户ID
     * @param paymentId 支付订单号
     * @param amount 支付金额
     */
    private void sendPaymentSuccessNotification(Long userId, String paymentId, BigDecimal amount) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("recipientUserId", userId);
            notificationData.put("channel", 4); // 站内信
            notificationData.put("subject", "支付成功通知");
            notificationData.put("content", String.format("您的支付订单 %s 已成功支付，金额：%.2f 元", paymentId, amount));
            notificationData.put("businessType", "PAYMENT");
            notificationData.put("businessId", paymentId);
            notificationData.put("messageType", 2); // 业务通知
            notificationData.put("priority", 2); // 普通优先级

            // 异步发送通知，不关心返回值
            gatewayServiceClient.callCommonService(
                    "/api/v1/notification/send",
                    HttpMethod.POST,
                    notificationData,
                    Long.class
            );

            log.debug("[支付记录] 支付成功通知发送成功，userId={}, paymentId={}", userId, paymentId);
        } catch (Exception e) {
            log.error("[支付记录] 支付成功通知发送失败，userId={}, paymentId={}", userId, paymentId, e);
            // 通知发送失败不影响主业务流程
        }
    }
}
