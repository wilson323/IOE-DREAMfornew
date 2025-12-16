package net.lab1024.sa.consume.service.payment.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.service.payment.PaymentRecordService;
import net.lab1024.sa.consume.service.AccountService;
import net.lab1024.sa.consume.consume.entity.PaymentRecordEntity;
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
    @Observed(name = "consume.payment.getRecord", contextualName = "consume-payment-get-record")
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

            log.info("[支付记录] 查询支付记录成功，paymentId={}, status={}", paymentId, record != null ? record.getPaymentStatus() : null);
            return record;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付记录] 查询支付记录参数错误: paymentId={}, error={}", paymentId, e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.warn("[支付记录] 查询支付记录业务异常: paymentId={}, code={}, message={}", paymentId, e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.error("[支付记录] 查询支付记录系统异常: paymentId={}, code={}, message={}", paymentId, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("[支付记录] 查询支付记录未知异常: paymentId={}", paymentId, e);
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
    @Observed(name = "consume.payment.saveRecord", contextualName = "consume-payment-save-record")
    public PaymentRecordEntity savePaymentRecord(PaymentRecordEntity paymentRecord) {
        // 参数验证（必须在方法开始处进行，确保健壮性）
        if (paymentRecord == null) {
            throw new ParamException("PAYMENT_RECORD_NULL", "支付记录不能为空");
        }

        log.info("[支付记录] 保存支付记录，paymentId={}, amount={}",
                paymentRecord.getPaymentId(), paymentRecord.getPaymentAmount());

        try {

            if (paymentRecord.getPaymentId() == null || paymentRecord.getPaymentId().trim().isEmpty()) {
                throw new ParamException("PAYMENT_ID_NULL", "支付订单号不能为空");
            }

            if (paymentRecord.getPaymentAmount() == null || paymentRecord.getPaymentAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new ParamException("PAYMENT_AMOUNT_INVALID", "支付金额必须大于0");
            }

            // 设置默认状态
            if (paymentRecord.getPaymentStatus() == null) {
                paymentRecord.setPaymentStatus(1); // 1-待支付
            }

            // 设置创建时间
            if (paymentRecord.getCreateTime() == null) {
                paymentRecord.setCreateTime(LocalDateTime.now());
            }

            // 实现具体的保存逻辑
            int result = paymentRecordDao.insert(paymentRecord);

            log.info("[支付记录] 保存支付记录成功，paymentId={}, status={}, result={}",
                    paymentRecord.getPaymentId(), paymentRecord.getPaymentStatus(), result);

            return paymentRecord;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付记录] 保存支付记录参数错误: paymentId={}, error={}",
                    paymentRecord != null ? paymentRecord.getPaymentId() : "null", e.getMessage());
            throw new ParamException("SAVE_PAYMENT_RECORD_PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付记录] 保存支付记录业务异常: paymentId={}, code={}, message={}",
                    paymentRecord != null ? paymentRecord.getPaymentId() : "null", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[支付记录] 保存支付记录系统异常: paymentId={}, code={}, message={}",
                    paymentRecord != null ? paymentRecord.getPaymentId() : "null", e.getCode(), e.getMessage(), e);
            throw new SystemException("SAVE_PAYMENT_RECORD_SYSTEM_ERROR", "保存支付记录失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[支付记录] 保存支付记录未知异常: paymentId={}",
                    paymentRecord != null ? paymentRecord.getPaymentId() : "null", e);
            throw new SystemException("SAVE_PAYMENT_RECORD_SYSTEM_ERROR", "保存支付记录失败：" + e.getMessage(), e);
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
    @Observed(name = "consume.payment.updateStatus", contextualName = "consume-payment-update-status")
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
            //
            // 注意：这里使用 UpdateWrapper（字符串列名）避免 LambdaUpdateWrapper 在纯单元测试场景下触发
            // MyBatis-Plus 的 lambda cache 初始化异常（未加载 TableInfo 时可能抛出异常）。
            UpdateWrapper<PaymentRecordEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("payment_id", paymentId)
                    .set("status", status)
                    .set("update_time", LocalDateTime.now());

            if (thirdPartyTransactionId != null && !thirdPartyTransactionId.trim().isEmpty()) {
                updateWrapper.set("third_party_transaction_id", thirdPartyTransactionId);
            }

            if ("SUCCESS".equals(status)) {
                updateWrapper.set("payment_time", LocalDateTime.now());
            }

            int updated = paymentRecordDao.update(null, updateWrapper);

            log.info("[支付记录] 更新支付状态{}，paymentId={}, status={}, updated={}",
                    updated > 0 ? "成功" : "失败", paymentId, status, updated);

            return updated > 0;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付记录] 更新支付状态参数错误: paymentId={}, status={}, error={}", paymentId, status, e.getMessage());
            return false;
        } catch (BusinessException e) {
            log.warn("[支付记录] 更新支付状态业务异常: paymentId={}, status={}, code={}, message={}", paymentId, status, e.getCode(), e.getMessage());
            return false;
        } catch (SystemException e) {
            log.error("[支付记录] 更新支付状态系统异常: paymentId={}, status={}, code={}, message={}", paymentId, status, e.getCode(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[支付记录] 更新支付状态未知异常: paymentId={}, status={}", paymentId, status, e);
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
    @Observed(name = "consume.payment.handleSuccess", contextualName = "consume-payment-handle-success")
    public void handlePaymentSuccess(String paymentId, String transactionId) {
        log.info("[支付记录] 处理支付成功回调，paymentId={}, transactionId={}", paymentId, transactionId);

        try {
            // 参数验证
            if (paymentId == null || paymentId.trim().isEmpty()) {
                throw new ParamException("PAYMENT_ID_NULL", "支付订单号不能为空");
            }

            // 查询支付记录
            PaymentRecordEntity paymentRecord = getPaymentRecord(paymentId);
            if (paymentRecord == null) {
                log.error("[支付记录] 支付记录不存在，paymentId={}", paymentId);
                throw new BusinessException("PAYMENT_RECORD_NOT_FOUND", "支付记录不存在");
            }

            // 检查当前状态
            if (paymentRecord.getPaymentStatus() != null && paymentRecord.getPaymentStatus() == 3) { // 3-支付成功
                log.warn("[支付记录] 支付记录已经是成功状态，paymentId={}", paymentId);
                return;
            }

            if (paymentRecord.getPaymentStatus() != null && paymentRecord.getPaymentStatus() == 5) { // 5-已退款
                log.warn("[支付记录] 支付记录已退款，不能更新为成功状态，paymentId={}", paymentId);
                return;
            }

            // 更新状态为成功
            boolean updated = updatePaymentStatus(paymentId, "SUCCESS", transactionId);
            if (!updated) {
                throw new BusinessException("UPDATE_PAYMENT_STATUS_FAILED", "更新支付状态失败");
            }

            // 支付成功后的业务处理
            processPaymentSuccessBusiness(paymentRecord);

            log.info("[支付记录] 支付成功回调处理完成，paymentId={}, transactionId={}", paymentId, transactionId);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付记录] 支付成功回调处理参数错误: paymentId={}, transactionId={}, error={}",
                    paymentId, transactionId, e.getMessage());
            throw new ParamException("HANDLE_PAYMENT_SUCCESS_PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付记录] 支付成功回调处理业务异常: paymentId={}, transactionId={}, code={}, message={}",
                    paymentId, transactionId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[支付记录] 支付成功回调处理系统异常: paymentId={}, transactionId={}, code={}, message={}",
                    paymentId, transactionId, e.getCode(), e.getMessage(), e);
            throw new SystemException("HANDLE_PAYMENT_SUCCESS_SYSTEM_ERROR", "支付成功回调处理失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[支付记录] 支付成功回调处理未知异常: paymentId={}, transactionId={}",
                    paymentId, transactionId, e);
            throw new SystemException("HANDLE_PAYMENT_SUCCESS_SYSTEM_ERROR", "支付成功回调处理失败：" + e.getMessage(), e);
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
            if (paymentRecord.getUserId() != null && paymentRecord.getPaymentAmount() != null) {
                // 通过交易ID查找关联的账户ID
                // 注意：这里假设支付记录中的userId就是账户所属用户
                // 实际业务中可能需要通过transactionId查找对应的账户
                Long accountId = findAccountIdByUserId(paymentRecord.getUserId());
                if (accountId != null) {
                    boolean balanceUpdated = accountService.addBalance(
                            accountId,
                            paymentRecord.getPaymentAmount(),
                            "支付成功：" + paymentRecord.getPaymentId()
                    );
                    if (balanceUpdated) {
                        log.info("[支付记录] 账户余额更新成功，accountId={}, amount={}",
                                accountId, paymentRecord.getPaymentAmount());
                    } else {
                        log.warn("[支付记录] 账户余额更新失败，accountId={}, amount={}",
                                accountId, paymentRecord.getPaymentAmount());
                    }
                }
            }

            // 2. 发送支付成功通知
            if (paymentRecord.getUserId() != null) {
                sendPaymentSuccessNotification(
                        paymentRecord.getUserId(),
                        paymentRecord.getPaymentId(),
                        paymentRecord.getPaymentAmount()
                );
            }

        } catch (IllegalArgumentException | ParamException e) {
            // 业务处理失败不影响支付状态更新，记录日志即可
            log.warn("[支付记录] 支付成功业务处理参数错误: paymentId={}, error={}",
                    paymentRecord.getPaymentId(), e.getMessage());
        } catch (BusinessException e) {
            // 业务处理失败不影响支付状态更新，记录日志即可
            log.warn("[支付记录] 支付成功业务处理业务异常: paymentId={}, code={}, message={}",
                    paymentRecord.getPaymentId(), e.getCode(), e.getMessage());
        } catch (SystemException e) {
            // 业务处理失败不影响支付状态更新，记录日志即可
            log.error("[支付记录] 支付成功业务处理系统异常: paymentId={}, code={}, message={}",
                    paymentRecord.getPaymentId(), e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            // 业务处理失败不影响支付状态更新，记录日志即可
            log.error("[支付记录] 支付成功业务处理未知异常: paymentId={}",
                    paymentRecord.getPaymentId(), e);
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付记录] 查询账户ID参数错误: userId={}, error={}", userId, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[支付记录] 查询账户ID业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[支付记录] 查询账户ID系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.warn("[支付记录] 查询账户ID失败: userId={}, error={}", userId, e.getMessage());
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[支付记录] 支付成功通知发送参数错误: userId={}, paymentId={}, error={}", userId, paymentId, e.getMessage());
            // 通知发送失败不影响主业务流程
        } catch (BusinessException e) {
            log.warn("[支付记录] 支付成功通知发送业务异常: userId={}, paymentId={}, code={}, message={}", userId, paymentId, e.getCode(), e.getMessage());
            // 通知发送失败不影响主业务流程
        } catch (SystemException e) {
            log.error("[支付记录] 支付成功通知发送系统异常: userId={}, paymentId={}, code={}, message={}", userId, paymentId, e.getCode(), e.getMessage(), e);
            // 通知发送失败不影响主业务流程
        } catch (Exception e) {
            log.error("[支付记录] 支付成功通知发送未知异常: userId={}, paymentId={}", userId, paymentId, e);
            // 通知发送失败不影响主业务流程
        }
    }
}



