package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.lab1024.sa.common.entity.video.VideoRecordingTaskEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频录像任务DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Mapper
public interface VideoRecordingTaskDao extends BaseMapper<VideoRecordingTaskEntity> {

    /**
     * 查询设备当前运行中的录像任务
     *
     * @param deviceId 设备ID
     * @return 录像任务
     */
    VideoRecordingTaskEntity selectRunningTaskByDevice(@Param("deviceId") String deviceId);

    /**
     * 查询指定计划的录像任务列表
     *
     * @param planId 计划ID
     * @return 录像任务列表
     */
    List<VideoRecordingTaskEntity> selectTasksByPlan(@Param("planId") Long planId);

    /**
     * 查询指定时间范围内的录像任务
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 录像任务列表
     */
    List<VideoRecordingTaskEntity> selectTasksByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定状态的录像任务
     *
     * @param status 任务状态
     * @param limit 限制数量
     * @return 录像任务列表
     */
    List<VideoRecordingTaskEntity> selectTasksByStatus(@Param("status") Integer status,
                                                       @Param("limit") Integer limit);

    /**
     * 查询失败且可重试的录像任务
     *
     * @param limit 限制数量
     * @return 录像任务列表
     */
    List<VideoRecordingTaskEntity> selectRetryableTasks(@Param("limit") Integer limit);

    /**
     * 更新录像任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态
     * @return 影响行数
     */
    Integer updateTaskStatus(@Param("taskId") Long taskId, @Param("status") Integer status);

    /**
     * 统计设备的录像任务数量（按状态）
     *
     * @param deviceId 设备ID
     * @param status 任务状态
     * @return 任务数量
     */
    Integer countTasksByDeviceAndStatus(@Param("deviceId") String deviceId,
                                        @Param("status") Integer status);

    /**
     * 删除指定日期之前的已完成录像任务
     *
     * @param beforeDate 日期
     * @return 删除行数
     */
    Integer deleteCompletedTasksBefore(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 统计录像文件总大小
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 文件总大小（字节）
     */
    Long sumFileSizeByTimeRange(@Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    /**
     * 查询设备最近的录像任务
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 录像任务列表
     */
    List<VideoRecordingTaskEntity> selectRecentTasksByDevice(@Param("deviceId") String deviceId,
                                                             @Param("limit") Integer limit);
}
