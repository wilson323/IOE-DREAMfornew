package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.consume.entity.OfflineSyncLogEntity;

/**
 * 离线同步日志Service接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
public interface OfflineSyncLogService extends IService<OfflineSyncLogEntity> {

    /**
     * 记录同步日志
     *
     * @param syncBatchNo 同步批次号
     * @param totalCount 总记录数
     * @param successCount 成功数量
     * @param failedCount 失败数量
     * @param conflictCount 冲突数量
     * @param durationMs 耗时（毫秒）
     */
    void recordSyncLog(String syncBatchNo, Integer totalCount, Integer successCount,
                       Integer failedCount, Integer conflictCount, Long durationMs);

    /**
     * 查询最近的同步日志
     *
     * @param limit 查询数量
     * @return 同步日志列表
     */
    java.util.List<OfflineSyncLogEntity> getRecentLogs(Integer limit);
}
