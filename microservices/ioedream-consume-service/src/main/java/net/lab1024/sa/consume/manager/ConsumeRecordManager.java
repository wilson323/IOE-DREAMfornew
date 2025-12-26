package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeAccountDao;
import net.lab1024.sa.consume.dao.ConsumeAccountTransactionDao;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.entity.ConsumeAccountEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeAccountTransactionEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.exception.ConsumeAccountException;
import org.springframework.stereotype.Component;

/**
 * 消费记录管理器
 * <p>
 * 负责消费记录的业务逻辑编排
 * 包括在线消费、离线消费同步、退款处理等
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class ConsumeRecordManager {

    private final ConsumeRecordDao consumeRecordDao;
    private final ConsumeAccountDao consumeAccountDao;
    private final ConsumeAccountTransactionDao accountTransactionDao;
    private final ConsumeDistributedLockManager lockManager;

    /**
     * 构造函数注入依赖
     */
    public ConsumeRecordManager(ConsumeRecordDao consumeRecordDao,
                                 ConsumeAccountDao consumeAccountDao,
                                 ConsumeAccountTransactionDao accountTransactionDao,
                                 ConsumeDistributedLockManager lockManager) {
        this.consumeRecordDao = consumeRecordDao;
        this.consumeAccountDao = consumeAccountDao;
        this.accountTransactionDao = accountTransactionDao;
        this.lockManager = lockManager;
    }

    /**
     * 创建在线消费记录
     *
     * @param accountId 账户ID
     * @param userId 用户ID
     * @param userName 用户姓名
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param merchantId 商户ID
     * @param merchantName 商户名称
     * @param amount 消费金额
     * @param originalAmount 原始金额
     * @param discountAmount 优惠金额
     * @param consumeType 消费类型
     * @param consumeTypeName 消费类型名称
     * @param paymentMethod 支付方式
     * @param orderNo 订单号
     * @param transactionNo 交易流水号
     * @param consumeLocation 消费地点
     * @return 消费记录ID
     */
    public Long createOnlineRecord(Long accountId, Long userId, String userName,
                                   String deviceId, String deviceName,
                                   Long merchantId, String merchantName,
                                   BigDecimal amount, BigDecimal originalAmount,
                                   BigDecimal discountAmount, String consumeType,
                                   String consumeTypeName, String paymentMethod,
                                   String orderNo, String transactionNo,
                                   String consumeLocation) {
        log.info("[消费记录] 创建在线消费记录: accountId={}, userId={}, amount={}, orderNo={}",
                accountId, userId, amount, orderNo);

        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setAccountId(accountId);
        record.setUserId(userId);
        record.setUserName(userName);
        record.setDeviceId(deviceId);
        record.setDeviceName(deviceName);
        record.setMerchantId(merchantId);
        record.setMerchantName(merchantName);
        record.setAmount(amount);
        record.setOriginalAmount(originalAmount);
        record.setDiscountAmount(discountAmount);
        record.setConsumeType(consumeType);
        record.setConsumeTypeName(consumeTypeName);
        record.setPaymentMethod(paymentMethod);
        record.setOrderNo(orderNo);
        record.setTransactionNo(transactionNo);
        record.setTransactionStatus(1); // 成功
        record.setConsumeStatus(1); // 正常
        record.setConsumeTime(LocalDateTime.now());
        record.setConsumeLocation(consumeLocation);
        record.setRefundStatus(0); // 未退款
        record.setRefundAmount(BigDecimal.ZERO);
        record.setOfflineFlag(0); // 在线
        record.setSyncStatus(1); // 已同步

        consumeRecordDao.insert(record);

        log.info("[消费记录] 在线消费记录创建成功: recordId={}, orderNo={}", record.getRecordId(), orderNo);
        return record.getRecordId();
    }

    /**
     * 创建离线消费记录
     *
     * @param accountId 账户ID
     * @param userId 用户ID
     * @param userName 用户姓名
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param merchantId 商户ID
     * @param merchantName 商户名称
     * @param amount 消费金额
     * @param consumeType 消费类型
     * @param consumeTypeName 消费类型名称
     * @param paymentMethod 支付方式
     * @param orderNo 订单号
     * @param consumeTime 消费时间
     * @param consumeLocation 消费地点
     * @return 消费记录ID
     */
    public Long createOfflineRecord(Long accountId, Long userId, String userName,
                                    String deviceId, String deviceName,
                                    Long merchantId, String merchantName,
                                    BigDecimal amount, String consumeType,
                                    String consumeTypeName, String paymentMethod,
                                    String orderNo, LocalDateTime consumeTime,
                                    String consumeLocation) {
        log.info("[消费记录] 创建离线消费记录: accountId={}, userId={}, amount={}, orderNo={}",
                accountId, userId, amount, orderNo);

        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setAccountId(accountId);
        record.setUserId(userId);
        record.setUserName(userName);
        record.setDeviceId(deviceId);
        record.setDeviceName(deviceName);
        record.setMerchantId(merchantId);
        record.setMerchantName(merchantName);
        record.setAmount(amount);
        record.setDiscountAmount(BigDecimal.ZERO);
        record.setConsumeType(consumeType);
        record.setConsumeTypeName(consumeTypeName);
        record.setPaymentMethod(paymentMethod);
        record.setOrderNo(orderNo);
        record.setTransactionStatus(1); // 成功
        record.setConsumeStatus(1); // 正常
        record.setConsumeTime(consumeTime);
        record.setConsumeLocation(consumeLocation);
        record.setRefundStatus(0); // 未退款
        record.setRefundAmount(BigDecimal.ZERO);
        record.setOfflineFlag(1); // 离线
        record.setSyncStatus(0); // 未同步

        consumeRecordDao.insert(record);

        log.info("[消费记录] 离线消费记录创建成功: recordId={}, orderNo={}", record.getRecordId(), orderNo);
        return record.getRecordId();
    }

    /**
     * 处理退款
     *
     * @param recordId 消费记录ID
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 操作结果
     */
    public boolean processRefund(Long recordId, BigDecimal refundAmount, String refundReason) {
        log.info("[消费记录] 处理退款: recordId={}, refundAmount={}, reason={}",
                recordId, refundAmount, refundReason);

        return lockManager.executeWithAccountLock(recordId, () -> {
            try {
                // 1. 查询消费记录
                ConsumeRecordEntity record = consumeRecordDao.selectById(recordId);
                if (record == null) {
                    log.error("[消费记录] 退款失败，消费记录不存在: recordId={}", recordId);
                    return false;
                }

                // 2. 验证退款金额
                if (refundAmount.compareTo(record.getAmount()) > 0) {
                    log.error("[消费记录] 退款失败，退款金额超过消费金额: recordId={}, refundAmount={}, amount={}",
                            recordId, refundAmount, record.getAmount());
                    return false;
                }

                // 3. 判断退款类型
                Integer refundStatus = refundAmount.compareTo(record.getAmount()) == 0 ? 2 : 1; // 2-全额退款，1-部分退款

                // 4. 更新消费记录退款状态
                record.setRefundStatus(refundStatus);
                record.setRefundAmount(refundAmount);
                record.setRefundTime(LocalDateTime.now());
                record.setRefundReason(refundReason);

                // 如果是全额退款，更新消费状态
                if (refundStatus == 2) {
                    record.setConsumeStatus(2); // 已退款
                }

                consumeRecordDao.updateById(record);

                // 5. 增加账户余额（在Manager层处理）
                // 注意：这里只更新消费记录，账户余额的更新由Service层调用ConsumeAccountManager处理

                log.info("[消费记录] 退款处理成功: recordId={}, refundAmount={}, refundStatus={}",
                        recordId, refundAmount, refundStatus);
                return true;

            } catch (Exception e) {
                log.error("[消费记录] 退款处理异常: recordId={}, refundAmount={}", recordId, refundAmount, e);
                return false;
            }
        });
    }

    /**
     * 同步离线消费记录到服务器
     *
     * @param recordId 消费记录ID
     * @return 操作结果
     */
    public boolean syncOfflineRecord(Long recordId) {
        log.info("[消费记录] 同步离线消费记录: recordId={}", recordId);

        try {
            // 1. 查询离线消费记录
            ConsumeRecordEntity record = consumeRecordDao.selectById(recordId);
            if (record == null) {
                log.error("[消费记录] 同步失败，消费记录不存在: recordId={}", recordId);
                return false;
            }

            if (!record.isOffline()) {
                log.warn("[消费记录] 该记录不是离线记录: recordId={}", recordId);
                return false;
            }

            // 2. 标记为已同步
            record.setSyncStatus(1);
            record.setSyncTime(LocalDateTime.now());
            consumeRecordDao.updateById(record);

            log.info("[消费记录] 离线消费记录同步成功: recordId={}, orderNo={}", recordId, record.getOrderNo());
            return true;

        } catch (Exception e) {
            log.error("[消费记录] 同步离线消费记录异常: recordId={}", recordId, e);
            return false;
        }
    }

    /**
     * 批量同步离线消费记录
     *
     * @param limit 批量数量
     * @return 同步成功数量
     */
    public int batchSyncOfflineRecords(int limit) {
        log.info("[消费记录] 批量同步离线消费记录: limit={}", limit);

        // 1. 查询待同步的离线记录
        List<ConsumeRecordEntity> pendingRecords = consumeRecordDao.selectPendingSyncRecords(limit);

        if (pendingRecords.isEmpty()) {
            log.info("[消费记录] 没有待同步的离线记录");
            return 0;
        }

        log.info("[消费记录] 找到 {} 条待同步的离线记录", pendingRecords.size());

        int successCount = 0;
        for (ConsumeRecordEntity record : pendingRecords) {
            if (syncOfflineRecord(record.getRecordId())) {
                successCount++;
            }
        }

        log.info("[消费记录] 批量同步完成: total={}, success={}", pendingRecords.size(), successCount);
        return successCount;
    }

    /**
     * 记录账户变动
     *
     * @param accountId 账户ID
     * @param userId 用户ID
     * @param transactionType 交易类型
     * @param transactionNo 交易流水号
     * @param businessNo 业务编号
     * @param amount 变动金额
     * @param balanceBefore 变动前余额
     * @param balanceAfter 变动后余额
     * @param relatedRecordId 关联记录ID
     * @param relatedOrderNo 关联订单号
     * @return 交易ID
     */
    public Long recordTransaction(Long accountId, Long userId, String transactionType,
                                  String transactionNo, String businessNo,
                                  BigDecimal amount, BigDecimal balanceBefore,
                                  BigDecimal balanceAfter, Long relatedRecordId,
                                  String relatedOrderNo) {
        log.info("[消费记录] 记录账户变动: accountId={}, type={}, amount={}",
                accountId, transactionType, amount);

        ConsumeAccountTransactionEntity transaction = new ConsumeAccountTransactionEntity();
        transaction.setAccountId(accountId);
        transaction.setUserId(userId);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionNo(transactionNo);
        transaction.setBusinessNo(businessNo);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setFrozenAmountBefore(BigDecimal.ZERO);
        transaction.setFrozenAmountAfter(BigDecimal.ZERO);
        transaction.setRelatedRecordId(relatedRecordId);
        transaction.setRelatedOrderNo(relatedOrderNo);
        transaction.setTransactionStatus(1); // 成功
        transaction.setTransactionTime(LocalDateTime.now());

        accountTransactionDao.insert(transaction);

        log.info("[消费记录] 账户变动记录成功: transactionId={}, type={}, amount={}",
                transaction.getTransactionId(), transactionType, amount);
        return transaction.getTransactionId();
    }
}
