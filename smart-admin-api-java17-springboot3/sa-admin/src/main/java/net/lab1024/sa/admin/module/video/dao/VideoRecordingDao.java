package net.lab1024.sa.admin.module.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.video.domain.entity.VideoRecordingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频录制记录DAO
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
@Mapper
public interface VideoRecordingDao extends BaseMapper<VideoRecordingEntity> {

    /**
     * 根据设备ID和时间范围查询录制记录
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 录制记录列表
     */
    List<VideoRecordingEntity> selectByDeviceIdAndTimeRange(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 根据状态查询录制记录
     *
     * @param status 录制状态
     * @return 录制记录列表
     */
    List<VideoRecordingEntity> selectByStatus(@Param("status") String status);

    /**
     * 查询正在进行的录制
     *
     * @return 正在进行的录制列表
     */
    List<VideoRecordingEntity> selectActiveRecordings();

    /**
     * 批量更新录制状态
     *
     * @param recordingIds 录制ID列表
     * @param status 新状态
     * @return 更新数量
     */
    int updateStatusBatch(@Param("recordingIds") List<Long> recordingIds, @Param("status") String status);
}