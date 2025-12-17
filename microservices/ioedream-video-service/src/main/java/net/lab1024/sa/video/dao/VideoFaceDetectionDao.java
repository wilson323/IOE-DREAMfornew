package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.video.entity.VideoFaceDetectionEntity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 人脸检测记录DAO接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Mapper
public interface VideoFaceDetectionDao extends BaseMapper<VideoFaceDetectionEntity> {

    /**
     * 根据设备ID查询检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE device_id = #{deviceId} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据流ID查询检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE stream_id = #{streamId} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByStreamId(@Param("streamId") Long streamId);

    /**
     * 根据匹配人员ID查询检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE matched_person_id = #{personId} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByMatchedPersonId(@Param("personId") Long personId);

    /**
     * 根据时间段查询检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND device_id = #{deviceId} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询触发了告警的检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE alarm_triggered = 1 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectAlarmTriggered();

    /**
     * 查询指定设备的告警记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE device_id = #{deviceId} AND alarm_triggered = 1 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectAlarmByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据处理状态查询检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE process_status = #{processStatus} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByProcessStatus(@Param("processStatus") Integer processStatus);

    /**
     * 查询未处理的告警记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE alarm_triggered = 1 AND process_status = 0 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectUnprocessedAlarms();

    /**
     * 查询匹配到人员ID的检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE matched_face_id IS NOT NULL AND matched_face_id > 0 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectWithMatchedFace();

    /**
     * 查询未知人员检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE matched_face_id IS NULL OR matched_face_id = 0 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectUnknownFaces();

    /**
     * 根据相似度范围查询检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE max_similarity >= #{minSimilarity} AND max_similarity <= #{maxSimilarity} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectBySimilarityRange(@Param("minSimilarity") Double minSimilarity, @Param("maxSimilarity") Double maxSimilarity);

    /**
     * 查询高质量人脸检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE face_quality_score >= #{qualityThreshold} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectHighQualityDetections(@Param("qualityThreshold") Double qualityThreshold);

    /**
     * 根据活体检测结果查询
     */
    @Select("SELECT * FROM t_video_face_detection WHERE liveness_result = #{livenessResult} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByLivenessResult(@Param("livenessResult") Integer livenessResult);

    /**
     * 查询通过活体检测的记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE liveness_result = 1 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectLivenessPassed();

    /**
     * 根据检测算法查询
     */
    @Select("SELECT * FROM t_video_face_detection WHERE detection_algorithm = #{algorithm} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByDetectionAlgorithm(@Param("algorithm") Integer algorithm);

    /**
     * 根据识别性别查询
     */
    @Select("SELECT * FROM t_video_face_detection WHERE recognized_gender = #{gender} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByRecognizedGender(@Param("gender") Integer gender);

    /**
     * 根据识别情绪查询
     */
    @Select("SELECT * FROM t_video_face_detection WHERE recognized_emotion = #{emotion} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoFaceDetectionEntity> selectByRecognizedEmotion(@Param("emotion") Integer emotion);

    /**
     * 统计设备检测次数
     */
    @Select("SELECT device_id, device_code, COUNT(*) as detection_count, COUNT(DISTINCT matched_person_id) as unique_person_count FROM t_video_face_detection WHERE deleted_flag = 0 GROUP BY device_id, device_code")
    List<Map<String, Object>> countByDevice();

    /**
     * 统计人员检测次数
     */
    @Select("SELECT matched_person_id, matched_person_name, COUNT(*) as detection_count FROM t_video_face_detection WHERE matched_person_id IS NOT NULL AND deleted_flag = 0 GROUP BY matched_person_id, matched_person_name ORDER BY detection_count DESC")
    List<Map<String, Object>> countByPerson();

    /**
     * 统计时间段内检测次数
     */
    @Select("SELECT DATE(detection_time) as detection_date, COUNT(*) as detection_count, COUNT(DISTINCT matched_person_id) as unique_person_count FROM t_video_face_detection WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY DATE(detection_time)")
    List<Map<String, Object>> countByDate(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计告警次数
     */
    @Select("SELECT alarm_types, COUNT(*) as alarm_count FROM t_video_face_detection WHERE alarm_triggered = 1 AND deleted_flag = 0 GROUP BY alarm_types")
    List<Map<String, Object>> countAlarmTypes();

    /**
     * 统计处理状态分布
     */
    @Select("SELECT process_status, COUNT(*) as status_count FROM t_video_face_detection WHERE deleted_flag = 0 GROUP BY process_status")
    List<Map<String, Object>> countByProcessStatus();

    /**
     * 统计活体检测结果分布
     */
    @Select("SELECT liveness_result, COUNT(*) as result_count FROM t_video_face_detection WHERE deleted_flag = 0 GROUP BY liveness_result")
    List<Map<String, Object>> countByLivenessResult();

    /**
     * 统计识别情绪分布
     */
    @Select("SELECT recognized_emotion, COUNT(*) as emotion_count FROM t_video_face_detection WHERE recognized_emotion IS NOT NULL AND deleted_flag = 0 GROUP BY recognized_emotion")
    List<Map<String, Object>> countByEmotion();

    /**
     * 查询最近检测记录
     */
    @Select("SELECT * FROM t_video_face_detection WHERE deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoFaceDetectionEntity> selectRecentDetections(@Param("limit") Integer limit);

    /**
     * 更新处理状态
     */
    @Update("UPDATE t_video_face_detection SET process_status = #{processStatus}, process_user_id = #{userId}, process_time = NOW(), process_remark = #{remark} WHERE detection_id = #{detectionId}")
    int updateProcessStatus(@Param("detectionId") Long detectionId, @Param("processStatus") Integer processStatus,
                           @Param("userId") Long userId, @Param("remark") String remark);

    /**
     * 批量更新处理状态
     */
    @Update("UPDATE t_video_face_detection SET process_status = #{processStatus}, process_user_id = #{userId}, process_time = NOW() WHERE detection_id IN (${detectionIds})")
    int batchUpdateProcessStatus(@Param("detectionIds") String detectionIds, @Param("processStatus") Integer processStatus, @Param("userId") Long userId);

    /**
     * 清理历史检测记录
     */
    @Update("UPDATE t_video_face_detection SET deleted_flag = 1 WHERE detection_time < #{cutoffTime} AND deleted_flag = 0")
    int cleanOldDetections(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查询设备检测趋势数据
     */
    @Select("SELECT device_id, DATE_FORMAT(detection_time, '%Y-%m-%d %H:00:00') as hour_bucket, COUNT(*) as detection_count FROM t_video_face_detection WHERE device_id IN (${deviceIds}) AND detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY device_id, hour_bucket ORDER BY hour_bucket")
    List<Map<String, Object>> selectDetectionTrend(@Param("deviceIds") String deviceIds, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询人员活跃度统计
     */
    @Select("SELECT matched_person_id, matched_person_name, device_id, COUNT(*) as visit_count, MAX(detection_time) as last_seen FROM t_video_face_detection WHERE matched_person_id IN (${personIds}) AND detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY matched_person_id, matched_person_name, device_id")
    List<Map<String, Object>> selectPersonActivity(@Param("personIds") String personIds, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
