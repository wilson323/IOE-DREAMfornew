package net.lab1024.sa.consume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.OfflineSyncLogDao;
import net.lab1024.sa.common.entity.consume.OfflineSyncLogEntity;
import net.lab1024.sa.consume.service.OfflineSyncLogService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 离线同步日志Service实现
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Service
@Slf4j
public class OfflineSyncLogServiceImpl extends ServiceImpl<OfflineSyncLogDao, OfflineSyncLogEntity>
        implements OfflineSyncLogService {

    @Override
    public void recordSyncLog(String syncBatchNo, Integer totalCount, Integer successCount,
                               Integer failedCount, Integer conflictCount, Long durationMs) {
        log.debug("[离线同步日志] 记录同步日志: batchNo={}, total={}, success={}, failed={}, conflict={}",
                syncBatchNo, totalCount, successCount, failedCount, conflictCount);

        OfflineSyncLogEntity syncLog = new OfflineSyncLogEntity();
        syncLog.setSyncBatchNo(syncBatchNo);
        syncLog.setTotalCount(totalCount);
        syncLog.setSuccessCount(successCount);
        syncLog.setFailedCount(failedCount);
        syncLog.setConflictCount(conflictCount);
        syncLog.setDurationMs(durationMs);
        syncLog.setStartTime(LocalDateTime.now());

        this.save(syncLog);
        log.debug("[离线同步日志] 同步日志记录成功: logId={}", syncLog.getLogId());
    }

    @Override
    public java.util.List<OfflineSyncLogEntity> getRecentLogs(Integer limit) {
        log.debug("[离线同步日志] 查询最近同步日志: limit={}", limit);
        LambdaQueryWrapper<OfflineSyncLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(OfflineSyncLogEntity::getStartTime)
                .last("LIMIT " + (limit != null && limit > 0 ? limit : 10));
        return baseMapper.selectList(queryWrapper);
    }
}
