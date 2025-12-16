package net.lab1024.sa.consume.scheduler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.manager.OfflineSyncManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 离线同步定时任务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Component
public class OfflineSyncScheduler {

    @Resource
    private OfflineSyncManager offlineSyncManager;

    /**
     * 每5分钟执行一次离线记录同步
     */
    @Scheduled(fixedRate = 300000)
    public void syncOfflineRecords() {
        log.info("[定时任务] 开始同步离线消费记录");
        try {
            OfflineSyncManager.SyncResult result = offlineSyncManager.syncPendingRecords();
            log.info("[定时任务] 离线同步完成，success={}, conflict={}, failed={}",
                    result.successCount(), result.conflictCount(), result.failCount());
        } catch (Exception e) {
            log.error("[定时任务] 离线同步异常", e);
        }
    }

    /**
     * 每小时处理一次冲突记录
     */
    @Scheduled(fixedRate = 3600000)
    public void resolveConflicts() {
        log.info("[定时任务] 开始处理冲突记录");
        try {
            int count = offlineSyncManager.resolveConflicts();
            log.info("[定时任务] 冲突处理完成，处理数量={}", count);
        } catch (Exception e) {
            log.error("[定时任务] 冲突处理异常", e);
        }
    }
}
