package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.lab1024.sa.common.entity.video.VideoRecordingPlanEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频录像计划DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Mapper
public interface VideoRecordingPlanDao extends BaseMapper<VideoRecordingPlanEntity> {

    /**
     * 查询设备的所有启用的录像计划
     *
     * @param deviceId 设备ID
     * @return 录像计划列表
     */
    List<VideoRecordingPlanEntity> selectEnabledPlansByDevice(@Param("deviceId") String deviceId);

    /**
     * 查询指定时间范围内的录像计划
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 录像计划列表
     */
    List<VideoRecordingPlanEntity> selectPlansByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定星期生效的录像计划
     *
     * @param weekday 星期（1-7，1=星期一）
     * @return 录像计划列表
     */
    List<VideoRecordingPlanEntity> selectPlansByWeekday(@Param("weekday") Integer weekday);

    /**
     * 查询设备在指定时间的优先级最高的录像计划
     *
     * @param deviceId 设备ID
     * @param currentTime 当前时间
     * @return 录像计划
     */
    VideoRecordingPlanEntity selectHighestPriorityPlan(@Param("deviceId") String deviceId,
                                                       @Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计设备的录像计划数量
     *
     * @param deviceId 设备ID
     * @return 计划数量
     */
    Integer countByDevice(@Param("deviceId") String deviceId);

    /**
     * 检查设备是否存在启用的录像计划
     *
     * @param deviceId 设备ID
     * @return 是否存在
     */
    Boolean existsEnabledPlan(@Param("deviceId") String deviceId);
}
