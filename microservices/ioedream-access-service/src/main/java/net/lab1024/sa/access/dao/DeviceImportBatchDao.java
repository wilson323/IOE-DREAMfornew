package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.DeviceImportBatchEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备导入批次DAO接口
 * <p>
 * 提供导入批次记录的数据访问操作：
 * - 基础CRUD（继承BaseMapper）
 * - 批次查询
 * - 状态统计
 * - 历史记录查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface DeviceImportBatchDao extends BaseMapper<DeviceImportBatchEntity> {

    /**
     * 根据操作人ID查询导入批次
     *
     * @param operatorId 操作人ID
     * @return 批次列表
     */
    List<DeviceImportBatchEntity> selectByOperatorId(@Param("operatorId") Long operatorId);

    /**
     * 根据导入状态查询批次
     *
     * @param importStatus 导入状态
     * @return 批次列表
     */
    List<DeviceImportBatchEntity> selectByStatus(@Param("importStatus") Integer importStatus);

    /**
     * 查询指定时间范围内的批次
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 批次列表
     */
    List<DeviceImportBatchEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近的批次记录（用于展示）
     *
     * @param limit 数量限制
     * @return 批次列表
     */
    List<DeviceImportBatchEntity> selectRecentBatches(@Param("limit") Integer limit);

    /**
     * 统计指定操作人的导入次数
     *
     * @param operatorId 操作人ID
     * @return 导入次数
     */
    Integer countByOperatorId(@Param("operatorId") Long operatorId);

    /**
     * 更新批次状态
     *
     * @param batchId      批次ID
     * @param importStatus 导入状态
     * @param statusMessage 状态消息
     * @return 更新行数
     */
    Integer updateStatus(@Param("batchId") Long batchId,
                         @Param("importStatus") Integer importStatus,
                         @Param("statusMessage") String statusMessage);

    /**
     * 更新批次统计信息
     *
     * @param batchId      批次ID
     * @param totalCount   总记录数
     * @param successCount 成功数量
     * @param failedCount  失败数量
     * @param skippedCount 跳过数量
     * @return 更新行数
     */
    Integer updateStatistics(@Param("batchId") Long batchId,
                            @Param("totalCount") Integer totalCount,
                            @Param("successCount") Integer successCount,
                            @Param("failedCount") Integer failedCount,
                            @Param("skippedCount") Integer skippedCount);

    /**
     * 完成批次（更新结束时间和状态）
     *
     * @param batchId      批次ID
     * @param importStatus 导入状态
     * @param statusMessage 状态消息
     * @param endTime      结束时间
     * @param durationMs   耗时（毫秒）
     * @return 更新行数
     */
    Integer completeBatch(@Param("batchId") Long batchId,
                          @Param("importStatus") Integer importStatus,
                          @Param("statusMessage") String statusMessage,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("durationMs") Long durationMs);
}
