package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.video.VideoObjectTrackingEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频目标跟踪DAO接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Mapper
public interface VideoObjectTrackingDao extends BaseMapper<VideoObjectTrackingEntity> {

    /**
     * 根据目标ID查询跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE object_id = #{objectId} AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectByObjectId(@Param("objectId") String objectId);

    /**
     * 根据设备ID查询跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE device_id = #{deviceId} AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据目标类型查询跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE object_type = #{objectType} AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectByObjectType(@Param("objectType") Integer objectType);

    /**
     * 根据时间段查询跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE tracking_start_time >= #{startTime} AND tracking_start_time <= #{endTime} AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据跟踪状态查询记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE tracking_status = #{trackingStatus} AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectByTrackingStatus(@Param("trackingStatus") Integer trackingStatus);

    /**
     * 查询活跃的跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE tracking_status = 1 AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectActiveTracking();

    /**
     * 查询已完成的跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE tracking_status = 3 AND deleted_flag = 0 ORDER BY tracking_end_time DESC")
    List<VideoObjectTrackingEntity> selectCompletedTracking();

    /**
     * 查询丢失的跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE tracking_status = 2 AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectLostTracking();

    /**
     * 查询长时间跟踪记录（超过指定时长）
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE tracking_duration >= #{minDuration} AND deleted_flag = 0 ORDER BY tracking_duration DESC")
    List<VideoObjectTrackingEntity> selectLongDurationTracking(@Param("minDuration") Long minDuration);

    /**
     * 查询高速度跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE average_speed >= #{minSpeed} AND deleted_flag = 0 ORDER BY average_speed DESC")
    List<VideoObjectTrackingEntity> selectHighSpeedTracking(@Param("minSpeed") Double minSpeed);

    /**
     * 查询长距离跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE total_distance >= #{minDistance} AND deleted_flag = 0 ORDER BY total_distance DESC")
    List<VideoObjectTrackingEntity> selectLongDistanceTracking(@Param("minDistance") Double minDistance);

    /**
     * 查询异常轨迹记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE anomaly_flag > 0 AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectAnomalousTracking();

    /**
     * 查询区域违规记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE area_violations > 0 AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectAreaViolationTracking();

    /**
     * 查询有停留行为的记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE loitering_duration > 0 AND deleted_flag = 0 ORDER BY loitering_duration DESC")
    List<VideoObjectTrackingEntity> selectLoiteringTracking();

    /**
     * 查询关联人脸的跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE associated_face_id IS NOT NULL AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectWithAssociatedFace();

    /**
     * 查询关联人员的跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE associated_person_id IS NOT NULL AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectWithAssociatedPerson();

    /**
     * 查询关联车辆的跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE associated_plate_number IS NOT NULL AND deleted_flag = 0 ORDER BY tracking_start_time DESC")
    List<VideoObjectTrackingEntity> selectWithAssociatedVehicle();

    /**
     * 统计各目标类型的跟踪数量
     */
    @Select("SELECT object_type, object_type_desc, COUNT(*) as tracking_count, AVG(tracking_duration) as avg_duration FROM t_video_object_tracking WHERE deleted_flag = 0 GROUP BY object_type, object_type_desc")
    List<Map<String, Object>> countByObjectType();

    /**
     * 统计各跟踪状态的数量
     */
    @Select("SELECT tracking_status, COUNT(*) as status_count FROM t_video_object_tracking WHERE deleted_flag = 0 GROUP BY tracking_status")
    List<Map<String, Object>> countByTrackingStatus();

    /**
     * 统计各设备的跟踪数量
     */
    @Select("SELECT device_id, COUNT(*) as tracking_count FROM t_video_object_tracking WHERE deleted_flag = 0 GROUP BY device_id")
    List<Map<String, Object>> countByDevice();

    /**
     * 统计跟踪质量分布
     */
    @Select("SELECT CASE WHEN tracking_quality_score >= 0.8 THEN 'excellent' WHEN tracking_quality_score >= 0.6 THEN 'good' WHEN tracking_quality_score >= 0.4 THEN 'fair' ELSE 'poor' END as quality_level, COUNT(*) as count FROM t_video_object_tracking WHERE deleted_flag = 0 GROUP BY CASE WHEN tracking_quality_score >= 0.8 THEN 'excellent' WHEN tracking_quality_score >= 0.6 THEN 'good' WHEN tracking_quality_score >= 0.4 THEN 'fair' ELSE 'poor' END")
    List<Map<String, Object>> countByQualityLevel();

    /**
     * 统计异常跟踪类型
     */
    @Select("SELECT anomaly_flag, COUNT(*) as count FROM t_video_object_tracking WHERE anomaly_flag > 0 AND deleted_flag = 0 GROUP BY anomaly_flag")
    List<Map<String, Object>> countByAnomalyType();

    /**
     * 查询最近跟踪记录
     */
    @Select("SELECT * FROM t_video_object_tracking WHERE deleted_flag = 0 ORDER BY tracking_start_time DESC LIMIT #{limit}")
    List<VideoObjectTrackingEntity> selectRecentTracking(@Param("limit") Integer limit);

    /**
     * 更新跟踪状态
     */
    @Update("UPDATE t_video_object_tracking SET tracking_status = #{trackingStatus}, tracking_end_time = #{endTime}, update_time = NOW() WHERE tracking_id = #{trackingId}")
    int updateTrackingStatus(@Param("trackingId") Long trackingId, @Param("trackingStatus") Integer trackingStatus, @Param("endTime") LocalDateTime endTime);

    /**
     * 更新跟踪结束信息
     */
    @Update("UPDATE t_video_object_tracking SET tracking_end_time = #{endTime}, tracking_duration = #{duration}, final_x = #{finalX}, final_y = #{finalY}, total_distance = #{distance}, average_speed = #{avgSpeed}, update_time = NOW() WHERE tracking_id = #{trackingId}")
    int updateTrackingEnd(@Param("trackingId") Long trackingId, @Param("endTime") LocalDateTime endTime, @Param("duration") Long duration, @Param("finalX") Double finalX, @Param("finalY") Double finalY, @Param("distance") Double distance, @Param("avgSpeed") Double avgSpeed);

    /**
     * 更新关联信息
     */
    @Update("UPDATE t_video_object_tracking SET associated_face_id = #{faceId}, associated_person_id = #{personId}, associated_plate_number = #{plateNumber}, update_time = NOW() WHERE tracking_id = #{trackingId}")
    int updateAssociatedInfo(@Param("trackingId") Long trackingId, @Param("faceId") String faceId, @Param("personId") Long personId, @Param("plateNumber") String plateNumber);

    /**
     * 更新轨迹分析结果
     */
    @Update("UPDATE t_video_object_tracking SET trajectory_analysis = #{analysis}, behavior_prediction = #{prediction}, anomaly_flag = #{anomalyFlag}, anomaly_description = #{anomalyDesc}, update_time = NOW() WHERE tracking_id = #{trackingId}")
    int updateTrajectoryAnalysis(@Param("trackingId") Long trackingId, @Param("analysis") String analysis, @Param("prediction") String prediction, @Param("anomalyFlag") Integer anomalyFlag, @Param("anomalyDesc") String anomalyDesc);

    /**
     * 清理历史跟踪记录
     */
    @Update("UPDATE t_video_object_tracking SET deleted_flag = 1 WHERE tracking_start_time < #{cutoffTime} AND deleted_flag = 0")
    int cleanOldTracking(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查询目标轨迹统计
     */
    @Select("SELECT object_id, object_type, COUNT(*) as tracking_count, SUM(tracking_duration) as total_duration, AVG(tracking_quality_score) as avg_quality FROM t_video_object_tracking WHERE object_id IS NOT NULL AND tracking_start_time >= #{startTime} AND tracking_start_time <= #{endTime} AND deleted_flag = 0 GROUP BY object_id, object_type")
    List<Map<String, Object>> selectObjectTrajectoryStats(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询设备跟踪性能统计
     */
    @Select("SELECT device_id, COUNT(*) as total_tracking, AVG(tracking_duration) as avg_duration, AVG(tracking_quality_score) as avg_quality, SUM(CASE WHEN tracking_status = 2 THEN 1 ELSE 0 END) as lost_count FROM t_video_object_tracking WHERE tracking_start_time >= #{startTime} AND deleted_flag = 0 GROUP BY device_id")
    List<Map<String, Object>> selectDeviceTrackingStats(@Param("startTime") LocalDateTime startTime);

    /**
     * 查询跟踪趋势数据
     */
    @Select("SELECT DATE(tracking_start_time) as tracking_date, object_type, COUNT(*) as tracking_count FROM t_video_object_tracking WHERE tracking_start_time >= #{startTime} AND tracking_start_time <= #{endTime} AND deleted_flag = 0 GROUP BY DATE(tracking_start_time), object_type ORDER BY tracking_date")
    List<Map<String, Object>> selectTrackingTrend(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询跟踪失败分析
     */
    @Select("SELECT lost_reason, COUNT(*) as failure_count, AVG(tracking_duration) as avg_duration_before_loss FROM t_video_object_tracking WHERE tracking_status = 2 AND lost_reason IS NOT NULL AND deleted_flag = 0 GROUP BY lost_reason")
    List<Map<String, Object>> selectTrackingFailureAnalysis();

    /**
     * 查询区域进入/离开统计
     */
    @Select("SELECT device_id, entered_areas, exited_areas, COUNT(*) as tracking_count FROM t_video_object_tracking WHERE (entered_areas IS NOT NULL OR exited_areas IS NOT NULL) AND deleted_flag = 0 GROUP BY device_id, entered_areas, exited_areas")
    List<Map<String, Object>> selectAreaTransitionStats();

    /**
     * 查询停留行为分析
     */
    @Select("SELECT loitering_area_id, COUNT(*) as loitering_count, AVG(loitering_duration) as avg_duration, MAX(loitering_duration) as max_duration FROM t_video_object_tracking WHERE loitering_duration > 0 AND deleted_flag = 0 GROUP BY loitering_area_id")
    List<Map<String, Object>> selectLoiteringAnalysis();

    /**
     * 查询跟踪质量分布
     */
    @Select("SELECT tracking_algorithm_version, COUNT(*) as count, AVG(tracking_quality_score) as avg_quality FROM t_video_object_tracking WHERE tracking_algorithm_version IS NOT NULL AND deleted_flag = 0 GROUP BY tracking_algorithm_version")
    List<Map<String, Object>> selectQualityByAlgorithm();

    /**
     * 获取跟踪记录总数
     */
    @Select("SELECT COUNT(*) FROM t_video_object_tracking WHERE deleted_flag = 0")
    long countTotalTracking();

    /**
     * 获取指定时间范围内的跟踪数量
     */
    @Select("SELECT COUNT(*) FROM t_video_object_tracking WHERE tracking_start_time >= #{startTime} AND tracking_start_time <= #{endTime} AND deleted_flag = 0")
    long countTrackingByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取平均跟踪质量
     */
    @Select("SELECT AVG(tracking_quality_score) as avg_quality, MAX(tracking_quality_score) as max_quality, MIN(tracking_quality_score) as min_quality FROM t_video_object_tracking WHERE deleted_flag = 0")
    Map<String, Object> getQualityStatistics();

    /**
     * 获取平均跟踪持续时间
     */
    @Select("SELECT AVG(tracking_duration) as avg_duration, MAX(tracking_duration) as max_duration FROM t_video_object_tracking WHERE tracking_duration IS NOT NULL AND deleted_flag = 0")
    Map<String, Object> getDurationStatistics();

    /**
     * 获取每日跟踪统计
     */
    @Select("SELECT DATE(tracking_start_time) as stat_date, COUNT(*) as total_count, SUM(CASE WHEN tracking_status = 3 THEN 1 ELSE 0 END) as completed_count FROM t_video_object_tracking WHERE deleted_flag = 0 AND tracking_start_time >= #{startDate} GROUP BY DATE(tracking_start_time) ORDER BY stat_date DESC")
    List<Map<String, Object>> getDailyStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * 获取跟踪趋势数据
     */
    @Select("SELECT DATE(tracking_start_time) as stat_date, object_type, COUNT(*) as count FROM t_video_object_tracking WHERE deleted_flag = 0 AND tracking_start_time >= #{startDate} AND tracking_start_time <= #{endDate} GROUP BY DATE(tracking_start_time), object_type ORDER BY stat_date, object_type")
    List<Map<String, Object>> getTrackingTrend(@Param("startDate") LocalDateTime startDate, @Param("endTime") LocalDateTime endTime);
}

