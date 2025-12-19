package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.OfflineConsumeRecordDao;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.entity.OfflineConsumeRecordEntity;

/**
 * 离线同步管理器
 * <p>
 * 说明：
 * - 负责离线消费记录的落库、同步执行、冲突标记
 * -
 * 与{@link OfflineConsumeRecordDao}配合实现离线记录的状态机（PENDING/SUCCESS/FAILED/CONFLICT）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-19
 */
public class OfflineSyncManager {

    private static final int DEFAULT_BATCH_SIZE = 200;

    private final OfflineConsumeRecordDao offlineConsumeRecordDao;
    private final AccountDao accountDao;

    /**
     * 构造函数
     *
     * @param offlineConsumeRecordDao 离线消费记录DAO
     * @param accountDao              账户DAO
     */
    public OfflineSyncManager(OfflineConsumeRecordDao offlineConsumeRecordDao, AccountDao accountDao) {
        this.offlineConsumeRecordDao = offlineConsumeRecordDao;
        this.accountDao = accountDao;
    }

    /**
     * 保存离线记录
     * <p>
     * - 如果离线交易号重复，直接返回已存在记录ID（幂等）
     * - 如果为新记录，默认写入同步状态为PENDING、重试次数0
     * </p>
     *
     * @param record 离线记录
     * @return 记录ID
     */
    public Long saveOfflineRecord(OfflineConsumeRecordEntity record) {
        if (record == null || record.getOfflineTransNo() == null || record.getOfflineTransNo().trim().isEmpty()) {
            throw new IllegalArgumentException("离线交易号不能为空");
        }

        OfflineConsumeRecordEntity existing = offlineConsumeRecordDao
                .selectByOfflineTransNo(record.getOfflineTransNo());
        if (existing != null) {
            return existing.getId();
        }

        record.setSyncStatus("PENDING");
        record.setRetryCount(0);
        offlineConsumeRecordDao.insert(record);
        return record.getId();
    }

    /**
     * 批量同步待同步记录
     * <p>
     * 同步规则（最小可交付版本）：
     * - 余额不足：标记冲突（CONFLICT），不扣款
     * - 扣款成功：标记SUCCESS
     * - 其他异常：标记FAILED，并增加重试次数
     * </p>
     *
     * @return 同步结果
     */
    public SyncResult syncPendingRecords() {
        List<OfflineConsumeRecordEntity> pending = offlineConsumeRecordDao.selectPendingRecords(0, DEFAULT_BATCH_SIZE);
        if (pending == null || pending.isEmpty()) {
            return new SyncResult(0, 0, 0, 0);
        }

        int successCount = 0;
        int conflictCount = 0;
        int failCount = 0;

        for (OfflineConsumeRecordEntity record : pending) {
            try {
                if (record == null || record.getId() == null) {
                    failCount++;
                    continue;
                }
                if (record.getAccountId() == null) {
                    offlineConsumeRecordDao.markConflict(record.getId(), "ACCOUNT", "账户ID为空");
                    conflictCount++;
                    continue;
                }

                AccountEntity account = accountDao.selectById(record.getAccountId());
                if (account == null) {
                    offlineConsumeRecordDao.markConflict(record.getId(), "ACCOUNT", "账户不存在");
                    conflictCount++;
                    continue;
                }

                BigDecimal amount = record.getAmount() != null ? record.getAmount() : BigDecimal.ZERO;
                BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
                if (balance.compareTo(amount) < 0) {
                    offlineConsumeRecordDao.markConflict(record.getId(), "BALANCE", "余额不足");
                    conflictCount++;
                    continue;
                }

                // 扣款：最小实现，使用updateById（并发一致性由后续迭代引入版本/锁）
                account.setBalance(balance.subtract(amount));
                int updated = accountDao.updateById(account);
                if (updated <= 0) {
                    offlineConsumeRecordDao.updateSyncStatus(
                            record.getId(),
                            "FAILED",
                            LocalDateTime.now(),
                            "账户扣款更新失败",
                            nextRetryCount(record));
                    failCount++;
                    continue;
                }

                offlineConsumeRecordDao.updateSyncStatus(
                        record.getId(),
                        "SUCCESS",
                        LocalDateTime.now(),
                        "同步成功",
                        record.getRetryCount() != null ? record.getRetryCount() : 0);
                successCount++;

            } catch (Exception e) {
                offlineConsumeRecordDao.updateSyncStatus(
                        record.getId(),
                        "FAILED",
                        LocalDateTime.now(),
                        "同步异常: " + e.getMessage(),
                        nextRetryCount(record));
                failCount++;
            }
        }

        return new SyncResult(pending.size(), successCount, conflictCount, failCount);
    }

    /**
     * 计算下一次重试次数
     *
     * @param record 离线记录
     * @return 重试次数
     */
    private Integer nextRetryCount(OfflineConsumeRecordEntity record) {
        Integer current = record != null ? record.getRetryCount() : null;
        return current == null ? 1 : current + 1;
    }

    /**
     * 同步结果
     *
     * @param total         本次处理总数
     * @param successCount  成功数量
     * @param conflictCount 冲突数量
     * @param failCount     失败数量
     */
    public record SyncResult(int total, int successCount, int conflictCount, int failCount) {
    }
}
