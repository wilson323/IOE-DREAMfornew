package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.FirmwareUpgradeTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 固件升级任务数据访问接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface FirmwareUpgradeTaskDao extends BaseMapper<FirmwareUpgradeTaskEntity> {

    /**
     * 更新任务统计信息
     *
     * @param taskId        任务ID
     * @param successCount   成功数量
     * @param failedCount    失败数量
     * @param pendingCount   待升级数量
     * @param upgradeProgress 升级进度
     * @return 更新行数
     */
    int updateTaskStatistics(@Param("taskId") Long taskId,
                             @Param("successCount") Integer successCount,
                             @Param("failedCount") Integer failedCount,
                             @Param("pendingCount") Integer pendingCount,
                             @Param("upgradeProgress") java.math.BigDecimal upgradeProgress);

    /**
     * 更新任务状态
     *
     * @param taskId     任务ID
     * @param taskStatus 任务状态
     * @return 更新行数
     */
    int updateTaskStatus(@Param("taskId") Long taskId, @Param("taskStatus") Integer taskStatus);

    /**
     * 查询待执行的任务
     * <p>
     * 条件：
     * 1. 任务状态为待执行（1）
     * 2. 定时升级时间已到（schedule_time <= NOW()）
     * </p>
     *
     * @return 待执行任务列表
     */
    List<FirmwareUpgradeTaskEntity> selectPendingTasks();
}
