package net.lab1024.sa.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.OfflineConsumeRecordDao;
import net.lab1024.sa.common.consume.entity.AccountEntity;
import net.lab1024.sa.common.consume.entity.OfflineConsumeRecordEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 离线同步管理器
 * <p>
 * 负责离线消费记录的同步处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
public class OfflineSyncManager {

    private final OfflineConsumeRecordDao offlineConsumeRecordDao;
    private final AccountDao accountDao;

    private static final int MAX_RETRY = 3;
    private static final int BATCH_SIZE = 100;

    public OfflineSyncManager(OfflineConsumeRecordDao offlineConsumeRecordDao,
                              AccountDao accountDao) {
        this.offlineConsumeRecordDao = offlineConsumeRecordDao;
        this.accountDao = accountDao;
    }

    /**
     * 保存离线消费记录
     */
    public Long saveOfflineRecord(OfflineConsumeRecordEntity record) {
        log.info("[离线同步] 保存离线消费记录，offlineTransNo={}, deviceId={}, amount={}",
                record.getOfflineTransNo(), record.getDeviceId(), record.getAmount());

        // 检查是否重复
        OfflineConsumeRecordEntity existing = offlineConsumeRecordDao.selectByOfflineTransNo(record.getOfflineTransNo());
        if (existing != null) {
            log.warn("[离线同步] 离线记录已存在，offlineTransNo={}", record.getOfflineTransNo());
            return existing.getId();
        }

        record.setSyncStatus("PENDING");
        record.setRetryCount(0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        offlineConsumeRecordDao.insert(record);
        log.info("[离线同步] 离线记录保存成功，id={}", record.getId());
        return record.getId();
    }

    /**
     * 批量同步离线记录
     */
    public SyncResult syncPendingRecords() {
        log.info("[离线同步] 开始批量同步离线记录");

        List<OfflineConsumeRecordEntity> pendingRecords = offlineConsumeRecordDao.selectPendingRecords(MAX_RETRY, BATCH_SIZE);
        if (pendingRecords.isEmpty()) {
            log.info("[离线同步] 没有待同步的记录");
            return new SyncResult(0, 0, 0);
        }

        int successCount = 0;
        int failCount = 0;
        int conflictCount = 0;

        for (OfflineConsumeRecordEntity record : pendingRecords) {
            try {
                SyncItemResult result = syncSingleRecord(record);
                switch (result) {
                    case SUCCESS -> successCount++;
                    case CONFLICT -> conflictCount++;
                    case FAILED -> failCount++;
                }
            } catch (Exception e) {
                log.error("[离线同步] 同步记录异常，id={}", record.getId(), e);
                offlineConsumeRecordDao.incrementRetryCount(record.getId());
                failCount++;
            }
        }

        log.info("[离线同步] 批量同步完成，success={}, conflict={}, failed={}",
                successCount, conflictCount, failCount);
        return new SyncResult(successCount, conflictCount, failCount);
    }

    /**
     * 同步单条记录
     */
    private SyncItemResult syncSingleRecord(OfflineConsumeRecordEntity record) {
        log.info("[离线同步] 同步单条记录，id={}, offlineTransNo={}", record.getId(), record.getOfflineTransNo());

        // 1. 检查账户状态
        AccountEntity account = accountDao.selectById(record.getAccountId());
        if (account == null) {
            offlineConsumeRecordDao.markConflict(record.getId(), "OTHER", "账户不存在");
            return SyncItemResult.CONFLICT;
        }

        // 2. 检查余额一致性
        BigDecimal actualBalance = account.getBalance();

        // 允许一定的误差（考虑到可能有其他离线记录）
        if (actualBalance.compareTo(record.getAmount()) < 0) {
            // 余额不足
            offlineConsumeRecordDao.markConflict(record.getId(), "BALANCE",
                    String.format("余额不足，当前余额=%s，消费金额=%s", actualBalance, record.getAmount()));
            return SyncItemResult.CONFLICT;
        }

        // 3. 检查重复消费
        // 这里简化处理，实际应该检查是否已有相同的在线记录

        // 4. 执行同步（扣减余额）
        try {
            // 直接扣减余额
            BigDecimal newBalance = actualBalance.subtract(record.getAmount());
            account.setBalance(newBalance);
            account.setUpdateTime(LocalDateTime.now());
            accountDao.updateById(account);

            // 更新同步状态
            offlineConsumeRecordDao.updateSyncStatus(record.getId(), "SYNCED",
                    LocalDateTime.now(), "同步成功", null);

            log.info("[离线同步] 记录同步成功，id={}, newBalance={}", record.getId(), newBalance);
            return SyncItemResult.SUCCESS;

        } catch (Exception e) {
            log.error("[离线同步] 同步执行失败，id={}", record.getId(), e);
            offlineConsumeRecordDao.incrementRetryCount(record.getId());
            return SyncItemResult.FAILED;
        }
    }

    /**
     * 处理冲突记录
     */
    public int resolveConflicts() {
        log.info("[离线同步] 开始处理冲突记录");

        List<OfflineConsumeRecordEntity> conflictRecords = offlineConsumeRecordDao.selectConflictRecords();
        int resolvedCount = 0;

        for (OfflineConsumeRecordEntity record : conflictRecords) {
            // 简化处理：标记为已忽略
            // 实际应该根据冲突类型进行不同处理
            offlineConsumeRecordDao.updateSyncStatus(record.getId(), "CONFLICT",
                    LocalDateTime.now(), "冲突已标记，需人工处理", null);
            resolvedCount++;
        }

        log.info("[离线同步] 冲突处理完成，处理数量={}", resolvedCount);
        return resolvedCount;
    }

    /**
     * 同步结果
     */
    public record SyncResult(int successCount, int conflictCount, int failCount) {
        public int total() {
            return successCount + conflictCount + failCount;
        }
    }

    /**
     * 单条同步结果
     */
    private enum SyncItemResult {
        SUCCESS, CONFLICT, FAILED
    }
}
