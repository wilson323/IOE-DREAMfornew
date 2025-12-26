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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 离线消费同步管理器
 * <p>
 * 负责离线消费记录的自动同步和账户余额补扣
 * 定时扫描待同步记录并自动处理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class ConsumeOfflineSyncManager {

    private final ConsumeRecordDao consumeRecordDao;
    private final ConsumeAccountDao consumeAccountDao;
    private final ConsumeAccountTransactionDao accountTransactionDao;
    private final ConsumeRecordManager recordManager;
    private final ConsumeDistributedLockManager lockManager;

    /**
     * 构造函数注入依赖
     */
    public ConsumeOfflineSyncManager(ConsumeRecordDao consumeRecordDao,
                                      ConsumeAccountDao consumeAccountDao,
                                      ConsumeAccountTransactionDao accountTransactionDao,
                                      ConsumeRecordManager recordManager,
                                      ConsumeDistributedLockManager lockManager) {
        this.consumeRecordDao = consumeRecordDao;
        this.consumeAccountDao = consumeAccountDao;
        this.accountTransactionDao = accountTransactionDao;
        this.recordManager = recordManager;
        this.lockManager = lockManager;
    }

    /**
     * 定时任务：每分钟扫描待同步的离线消费记录
     */
    @Scheduled(cron = "0 * * * * ?")
    public void syncPendingOfflineRecords() {
        try {
            log.debug("[离线同步] 开始扫描待同步的离线消费记录");

            // 1. 查询待同步的离线记录
            List<ConsumeRecordEntity> pendingRecords = consumeRecordDao.selectPendingSyncRecords(100);

            if (pendingRecords.isEmpty()) {
                log.debug("[离线同步] 没有待同步的离线消费记录");
                return;
            }

            log.info("[离线同步] 找到 {} 条待同步的离线消费记录", pendingRecords.size());

            int successCount = 0;
            int failCount = 0;

            for (ConsumeRecordEntity record : pendingRecords) {
                try {
                    boolean success = syncOfflineRecord(record);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("[离线同步] 同步离线记录异常: recordId={}, orderNo={}",
                            record.getRecordId(), record.getOrderNo(), e);
                    failCount++;
                }
            }

            log.info("[离线同步] 扫描完成: total={}, success={}, fail={}",
                    pendingRecords.size(), successCount, failCount);

        } catch (Exception e) {
            log.error("[离线同步] 定时任务执行异常", e);
        }
    }

    /**
     * 同步单条离线消费记录
     *
     * @param record 离线消费记录
     * @return 同步结果
     */
    private boolean syncOfflineRecord(ConsumeRecordEntity record) {
        log.info("[离线同步] 开始同步离线记录: recordId={}, orderNo={}, amount={}",
                record.getRecordId(), record.getOrderNo(), record.getAmount());

        return lockManager.executeWithAccountLock(record.getAccountId(), () -> {
            try {
                // 1. 查询账户信息
                ConsumeAccountEntity account = consumeAccountDao.selectById(record.getAccountId());
                if (account == null) {
                    log.error("[离线同步] 同步失败，账户不存在: accountId={}", record.getAccountId());
                    return false;
                }

                // 2. 验证账户状态
                if (!account.isActive()) {
                    log.error("[离线同步] 同步失败，账户状态异常: accountId={}, status={}",
                            record.getAccountId(), account.getAccountStatus());
                    return false;
                }

                // 3. 验证余额
                if (!account.hasSufficientBalance(record.getAmount())) {
                    log.warn("[离线同步] 同步失败，余额不足: accountId={}, balance={}, amount={}",
                            record.getAccountId(), account.getBalance(), record.getAmount());
                    // 标记为同步失败但不删除，等待人工处理
                    record.setSyncStatus(0);
                    consumeRecordDao.updateById(record);
                    return false;
                }

                // 4. 扣减账户余额
                BigDecimal balanceBefore = account.getBalance();
                account.decreaseBalance(record.getAmount());
                BigDecimal balanceAfter = account.getBalance();

                consumeAccountDao.updateById(account);

                // 5. 记录账户变动
                String transactionNo = generateTransactionNo("DEDUCT", record.getAccountId());
                accountTransactionDao.insert(createTransactionEntity(
                        record.getAccountId(),
                        account.getUserId(),
                        "DEDUCT",
                        transactionNo,
                        record.getOrderNo(),
                        record.getAmount().negate(), // 扣减是负数
                        balanceBefore,
                        balanceAfter,
                        record.getRecordId(),
                        record.getOrderNo()
                ));

                // 6. 标记为已同步
                record.setSyncStatus(1);
                record.setSyncTime(LocalDateTime.now());
                // 生成交易流水号
                record.setTransactionNo(transactionNo);
                consumeRecordDao.updateById(record);

                log.info("[离线同步] 离线记录同步成功: recordId={}, orderNo={}, balanceBefore={}, balanceAfter={}",
                        record.getRecordId(), record.getOrderNo(), balanceBefore, balanceAfter);
                return true;

            } catch (Exception e) {
                log.error("[离线同步] 同步离线记录异常: recordId={}, orderNo={}",
                        record.getRecordId(), record.getOrderNo(), e);
                return false;
            }
        });
    }

    /**
     * 定时任务：每5分钟统计待同步记录数量
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void reportPendingSyncRecords() {
        try {
            List<ConsumeRecordEntity> pendingRecords = consumeRecordDao.selectPendingSyncRecords(10000);
            int pendingCount = pendingRecords.size();

            if (pendingCount == 0) {
                log.debug("[离线同步] 待同步记录统计: 0 条");
            } else {
                log.info("[离线同步] 待同步记录统计: {} 条", pendingCount);

                // 如果超过1000条，触发告警
                if (pendingCount > 1000) {
                    log.error("[离线同步] 告警：待同步离线记录过多: {} 条，请检查系统状态", pendingCount);
                    // TODO: 发送告警通知（邮件、短信、钉钉等）
                }
            }

        } catch (Exception e) {
            log.error("[离线同步] 统计待同步记录异常", e);
        }
    }

    /**
     * 生成交易流水号
     *
     * @param prefix 前缀
     * @param accountId 账户ID
     * @return 交易流水号
     */
    private String generateTransactionNo(String prefix, Long accountId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("%s_%d_%s", prefix, accountId, timestamp);
    }

    /**
     * 创建账户变动记录实体
     */
    private ConsumeAccountTransactionEntity createTransactionEntity(
            Long accountId, Long userId, String transactionType,
            String transactionNo, String businessNo, BigDecimal amount,
            BigDecimal balanceBefore, BigDecimal balanceAfter,
            Long relatedRecordId, String relatedOrderNo) {

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

        return transaction;
    }
}
