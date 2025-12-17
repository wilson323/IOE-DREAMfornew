package net.lab1024.sa.common.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.video.entity.VideoObjectDetectionEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频目标检测DAO接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Mapper
public interface VideoObjectDetectionDao extends BaseMapper<VideoObjectDetectionEntity> {

    /**
     * 根据设备ID查询检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE device_id = #{deviceId} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据目标类型查询检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE object_type = #{objectType} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectByObjectType(@Param("objectType") Integer objectType);

    /**
     * 根据时间段查询检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据设备和时间段查询检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE device_id = #{deviceId} AND detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询触发了告警的检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE alert_triggered = 1 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectAlertTriggered();

    /**
     * 查询指定设备的告警记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE device_id = #{deviceId} AND alert_triggered = 1 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectAlertByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据告警级别查询检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE alert_level = #{alertLevel} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectByAlertLevel(@Param("alertLevel") Integer alertLevel);

    /**
     * 查询在禁区内的检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE in_restricted_area = 1 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectInRestrictedArea();

    /**
     * 根据目标ID查询检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE object_id = #{objectId} AND deleted_flag = 0 ORDER BY detection_time ASC")
    List<VideoObjectDetectionEntity> selectByObjectId(@Param("objectId") String objectId);

    /**
     * 根据置信度范围查询检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE confidence_score >= #{minConfidence} AND confidence_score <= #{maxConfidence} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectByConfidenceRange(@Param("minConfidence") Double minConfidence, @Param("maxConfidence") Double maxConfidence);

    /**
     * 查询高置信度检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE confidence_score >= #{minConfidence} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectHighConfidence(@Param("minConfidence") Double minConfidence);

    /**
     * 根据区域ID查询检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE area_id = #{areaId} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据处理状态查询检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE process_status = #{processStatus} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectByProcessStatus(@Param("processStatus") Integer processStatus);

    /**
     * 查询未处理的告警记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE alert_triggered = 1 AND process_status = 0 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectUnprocessedAlerts();

    /**
     * 统计各目标类型的数量
     */
    @Select("SELECT object_type, object_type_desc, COUNT(*) as object_count FROM t_video_object_detection WHERE deleted_flag = 0 GROUP BY object_type, object_type_desc")
    List<Map<String, Object>> countByObjectType();

    /**
     * 统计各告警级别的数量
     */
    @Select("SELECT alert_level, COUNT(*) as alert_count FROM t_video_object_detection WHERE alert_triggered = 1 AND deleted_flag = 0 GROUP BY alert_level")
    List<Map<String, Object>> countByAlertLevel();

    /**
     * 统计各设备的目标检测数量
     */
    @Select("SELECT device_id, device_code, COUNT(*) as detection_count FROM t_video_object_detection WHERE deleted_flag = 0 GROUP BY device_id, device_code")
    List<Map<String, Object>> countByDevice();

    /**
     * 统计各区域的检测数量
     */
    @Select("SELECT area_id, area_name, COUNT(*) as detection_count FROM t_video_object_detection WHERE area_id IS NOT NULL AND deleted_flag = 0 GROUP BY area_id, area_name")
    List<Map<String, Object>> countByArea();

    /**
     * 统计时间段内检测数量
     */
    @Select("SELECT DATE(detection_time) as detection_date, COUNT(*) as detection_count FROM t_video_object_detection WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY DATE(detection_time)")
    List<Map<String, Object>> countByDate(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoObjectDetectionEntity> selectRecentDetections(@Param("limit") Integer limit);

    /**
     * 查询大目标检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE relative_size >= #{minSize} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectLargeObjects(@Param("minSize") Double minSize);

    /**
     * 查询快速运动目标
     */
    @Select("SELECT * FROM t_video_object_detection WHERE movement_speed >= #{minSpeed} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectFastMovingObjects(@Param("minSpeed") Double minSpeed);

    /**
     * 更新处理状态
     */
    @Update("UPDATE t_video_object_detection SET process_status = #{processStatus}, update_time = NOW() WHERE detection_id = #{detectionId}")
    int updateProcessStatus(@Param("detectionId") Long detectionId, @Param("processStatus") Integer processStatus);

    /**
     * 批量更新处理状态
     */
    @Update("UPDATE t_video_object_detection SET process_status = #{processStatus}, update_time = NOW() WHERE detection_id IN (${detectionIds})")
    int batchUpdateProcessStatus(@Param("detectionIds") String detectionIds, @Param("processStatus") Integer processStatus);

    /**
     * 更新验证结果
     */
    @Update("UPDATE t_video_object_detection SET verification_result = #{verificationResult}, verified_by = #{verifiedBy}, verification_time = #{verificationTime}, verification_note = #{verificationNote}, update_time = NOW() WHERE detection_id = #{detectionId}")
    int updateVerificationResult(@Param("detectionId") Long detectionId, @Param("verificationResult") Integer verificationResult, @Param("verifiedBy") Long verifiedBy, @Param("verificationTime") LocalDateTime verificationTime, @Param("verificationNote") String verificationNote);

    /**
     * 清理历史检测记录
     */
    @Update("UPDATE t_video_object_detection SET deleted_flag = 1 WHERE detection_time < #{cutoffTime} AND deleted_flag = 0")
    int cleanOldDetections(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查询目标检测趋势数据
     */
    @Select("SELECT device_id, DATE_FORMAT(detection_time, '%Y-%m-%d %H:00:00') as hour_bucket, object_type, COUNT(*) as detection_count FROM t_video_object_detection WHERE device_id IN (${deviceIds}) AND detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY device_id, hour_bucket, object_type ORDER BY hour_bucket")
    List<Map<String, Object>> selectDetectionTrend(@Param("deviceIds") String deviceIds, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询对象运动统计
     */
    @Select("SELECT object_id, object_type, COUNT(*) as detection_count, AVG(confidence_score) as avg_confidence, MAX(detection_time) as last_seen, MIN(detection_time) as first_seen FROM t_video_object_detection WHERE object_id IS NOT NULL AND detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY object_id, object_type")
    List<Map<String, Object>> selectObjectMovementStats(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询区域入侵检测
     */
    @Select("SELECT * FROM t_video_object_detection WHERE in_restricted_area = 1 AND alert_triggered = 1 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectAreaIntrusionDetections();

    /**
     * 查询目标出现频率统计
     */
    @Select("SELECT object_type_desc, COUNT(*) as occurrence_count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM t_video_object_detection WHERE deleted_flag = 0) as frequency_percentage FROM t_video_object_detection WHERE deleted_flag = 0 GROUP BY object_type_desc ORDER BY occurrence_count DESC")
    List<Map<String, Object>> selectObjectFrequency();

    /**
     * 查询检测性能统计
     */
    @Select("SELECT AVG(processing_time) as avg_processing_time, MAX(processing_time) as max_processing_time, AVG(confidence_score) as avg_confidence, COUNT(*) as total_detections FROM t_video_object_detection WHERE detection_time >= #{startTime} AND deleted_flag = 0")
    Map<String, Object> selectDetectionPerformance(@Param("startTime") LocalDateTime startTime);

    /**
     * 查询目标密度分析
     */
    @Select("SELECT device_id, area_id, COUNT(*) as object_count, density_level FROM t_video_object_detection WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY device_id, area_id, density_level")
    List<Map<String, Object>> selectObjectDensityAnalysis(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询关联人脸的检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE face_id IS NOT NULL AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectWithAssociatedFace();

    /**
     * 查询关联车辆的检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE plate_number IS NOT NULL AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectWithAssociatedVehicle();

    /**
     * 查询目标属性分析
     */
    @Select("SELECT object_type, object_attributes, COUNT(*) as count FROM t_video_object_detection WHERE object_attributes IS NOT NULL AND deleted_flag = 0 GROUP BY object_type, object_attributes")
    List<Map<String, Object>> selectObjectAttributesAnalysis();

    /**
     * 查询异常检测记录
     */
    @Select("SELECT * FROM t_video_object_detection WHERE alert_level >= 4 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoObjectDetectionEntity> selectAnomalousDetections();

    /**
     * 获取检测记录总数
     */
    @Select("SELECT COUNT(*) FROM t_video_object_detection WHERE deleted_flag = 0")
    long countTotalDetections();

    /**
     * 获取指定时间范围内的检测数量
     */
    @Select("SELECT COUNT(*) FROM t_video_object_detection WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0")
    long countDetectionsByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取设备的检测数量
     */
    @Select("SELECT device_id, COUNT(*) as detection_count FROM t_video_object_detection WHERE deleted_flag = 0 GROUP BY device_id ORDER BY detection_count DESC")
    List<Map<String, Object>> countDetectionByDevice();

    /**
     * 按处理状态统计数量
     */
    @Select("SELECT process_status, COUNT(*) as count FROM t_video_object_detection WHERE deleted_flag = 0 GROUP BY process_status")
    List<Map<String, Object>> countByProcessStatus();

    /**
     * 获取平均置信度
     */
    @Select("SELECT AVG(confidence_score) as avg_confidence, MAX(confidence_score) as max_confidence, MIN(confidence_score) as min_confidence FROM t_video_object_detection WHERE deleted_flag = 0")
    Map<String, Object> getConfidenceStatistics();

    /**
     * 获取平均处理时间
     */
    @Select("SELECT AVG(processing_time) as avg_processing_time, MAX(processing_time) as max_processing_time FROM t_video_object_detection WHERE processing_time IS NOT NULL AND deleted_flag = 0")
    Map<String, Object> getProcessingTimeStatistics();

    /**
     * 获取每日检测统计
     */
    @Select("SELECT DATE(detection_time) as stat_date, COUNT(*) as total_count, SUM(CASE WHEN alert_triggered = 1 THEN 1 ELSE 0 END) as alert_count FROM t_video_object_detection WHERE deleted_flag = 0 AND detection_time >= #{startDate} GROUP BY DATE(detection_time) ORDER BY stat_date DESC")
    List<Map<String, Object>> getDailyStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * 获取检测趋势数据
     */
    @Select("SELECT DATE(detection_time) as stat_date, object_type, COUNT(*) as count FROM t_video_object_detection WHERE deleted_flag = 0 AND detection_time >= #{startDate} AND detection_time <= #{endDate} GROUP BY DATE(detection_time), object_type ORDER BY stat_date, object_type")
    List<Map<String, Object>> getDetectionTrend(@Param("startDate") LocalDateTime startDate, @Param("endTime") LocalDateTime endTime);
}
