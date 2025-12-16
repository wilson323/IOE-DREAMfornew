package net.lab1024.sa.common.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.video.entity.VideoBehaviorEntity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频行为检测DAO接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Mapper
public interface VideoBehaviorDao extends BaseMapper<VideoBehaviorEntity> {

    /**
     * 根据设备ID查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE device_id = #{deviceId} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据行为类型查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE behavior_type = #{behaviorType} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByBehaviorType(@Param("behaviorType") Integer behaviorType);

    /**
     * 根据人员ID查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE person_id = #{personId} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByPersonId(@Param("personId") Long personId);

    /**
     * 根据时间段查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据设备和时间段查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE device_id = #{deviceId} AND detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询触发了告警的行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE alarm_triggered = 1 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectAlarmTriggered();

    /**
     * 查询指定设备的告警记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE device_id = #{deviceId} AND alarm_triggered = 1 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectAlarmByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据处理状态查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE process_status = #{processStatus} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByProcessStatus(@Param("processStatus") Integer processStatus);

    /**
     * 查询未处理的告警记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE alarm_triggered = 1 AND process_status = 0 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectUnprocessedAlarms();

    /**
     * 查询需要人工确认的记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE need_manual_confirm = 1 AND process_status = 0 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectNeedingManualConfirm();

    /**
     * 根据严重程度查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE severity_level = #{severityLevel} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectBySeverityLevel(@Param("severityLevel") Integer severityLevel);

    /**
     * 根据告警级别查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE alarm_level = #{alarmLevel} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByAlarmLevel(@Param("alarmLevel") Integer alarmLevel);

    /**
     * 根据置信度范围查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE confidence_score >= #{minConfidence} AND confidence_score <= #{maxConfidence} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByConfidenceRange(@Param("minConfidence") Double minConfidence, @Param("maxConfidence") Double maxConfidence);

    /**
     * 根据检测算法查询行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE detection_algorithm = #{algorithm} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByDetectionAlgorithm(@Param("algorithm") Integer algorithm);

    /**
     * 统计各行为类型的数量
     */
    @Select("SELECT behavior_type, behavior_type_desc, COUNT(*) as behavior_count FROM t_video_behavior WHERE deleted_flag = 0 GROUP BY behavior_type, behavior_type_desc")
    List<Map<String, Object>> countByBehaviorType();

    /**
     * 统计各严重程度的数量
     */
    @Select("SELECT severity_level, COUNT(*) as severity_count FROM t_video_behavior WHERE deleted_flag = 0 GROUP BY severity_level")
    List<Map<String, Object>> countBySeverityLevel();

    /**
     * 统计各告警级别的数量
     */
    @Select("SELECT alarm_level, COUNT(*) as alarm_count FROM t_video_behavior WHERE alarm_triggered = 1 AND deleted_flag = 0 GROUP BY alarm_level")
    List<Map<String, Object>> countByAlarmLevel();

    /**
     * 统计各处理状态的数量
     */
    @Select("SELECT process_status, COUNT(*) as status_count FROM t_video_behavior WHERE deleted_flag = 0 GROUP BY process_status")
    List<Map<String, Object>> countByProcessStatus();

    /**
     * 统计设备行为数量
     */
    @Select("SELECT device_id, device_code, COUNT(*) as behavior_count FROM t_video_behavior WHERE deleted_flag = 0 GROUP BY device_id, device_code")
    List<Map<String, Object>> countByDevice();

    /**
     * 统计人员行为数量
     */
    @Select("SELECT person_id, person_code, person_name, COUNT(*) as behavior_count FROM t_video_behavior WHERE person_id IS NOT NULL AND deleted_flag = 0 GROUP BY person_id, person_code, person_name")
    List<Map<String, Object>> countByPerson();

    /**
     * 统计时间段内行为数量
     */
    @Select("SELECT DATE(detection_time) as behavior_date, COUNT(*) as behavior_count FROM t_video_behavior WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY DATE(detection_time)")
    List<Map<String, Object>> countByDate(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计告警类型分布
     */
    @Select("SELECT alarm_types, COUNT(*) as alarm_count FROM t_video_behavior WHERE alarm_triggered = 1 AND deleted_flag = 0 GROUP BY alarm_types")
    List<Map<String, Object>> countAlarmTypes();

    /**
     * 查询最近行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoBehaviorEntity> selectRecentBehaviors(@Param("limit") Integer limit);

    /**
     * 查询高风险行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE severity_level >= 3 AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectHighRiskBehaviors();

    /**
     * 查询长持续时间行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE duration_seconds >= #{minDuration} AND deleted_flag = 0 ORDER BY duration_seconds DESC")
    List<VideoBehaviorEntity> selectLongDurationBehaviors(@Param("minDuration") Long minDuration);

    /**
     * 更新处理状态
     */
    @Update("UPDATE t_video_behavior SET process_status = #{processStatus}, process_time = NOW(), process_user_id = #{userId}, process_user_name = #{userName}, process_remark = #{remark} WHERE behavior_id = #{behaviorId}")
    int updateProcessStatus(@Param("behaviorId") Long behaviorId, @Param("processStatus") Integer processStatus,
                           @Param("userId") Long userId, @Param("userName") String userName, @Param("remark") String remark);

    /**
     * 批量更新处理状态
     */
    @Update("UPDATE t_video_behavior SET process_status = #{processStatus}, process_time = NOW(), process_user_id = #{userId}, process_user_name = #{userName} WHERE behavior_id IN (${behaviorIds})")
    int batchUpdateProcessStatus(@Param("behaviorIds") String behaviorIds, @Param("processStatus") Integer processStatus,
                                 @Param("userId") Long userId, @Param("userName") String userName);

    /**
     * 清理历史行为记录
     */
    @Update("UPDATE t_video_behavior SET deleted_flag = 1 WHERE detection_time < #{cutoffTime} AND deleted_flag = 0")
    int cleanOldBehaviors(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查询设备行为趋势数据
     */
    @Select("SELECT device_id, DATE_FORMAT(detection_time, '%Y-%m-%d %H:00:00') as hour_bucket, behavior_type, COUNT(*) as behavior_count FROM t_video_behavior WHERE device_id IN (${deviceIds}) AND detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY device_id, hour_bucket, behavior_type ORDER BY hour_bucket")
    List<Map<String, Object>> selectBehaviorTrend(@Param("deviceIds") String deviceIds, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询人员行为活跃度统计
     */
    @Select("SELECT person_id, person_code, person_name, device_id, COUNT(*) as behavior_count, MAX(detection_time) as last_seen FROM t_video_behavior WHERE person_id IN (${personIds}) AND detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0 GROUP BY person_id, person_code, person_name, device_id")
    List<Map<String, Object>> selectPersonActivity(@Param("personIds") String personIds, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询异常行为模式
     */
    @Select("SELECT behavior_type, behavior_type_desc, COUNT(*) as abnormal_count, AVG(confidence_score) as avg_confidence FROM t_video_behavior WHERE severity_level >= 3 AND deleted_flag = 0 GROUP BY behavior_type, behavior_type_desc ORDER BY abnormal_count DESC")
    List<Map<String, Object>> selectAbnormalBehaviorPatterns();

    /**
     * 查询行为发生频率统计
     */
    @Select("SELECT behavior_type_desc, COUNT(*) as occurrence_count, COUNT(*) * 100.0 / (SELECT COUNT(*) FROM t_video_behavior WHERE deleted_flag = 0) as frequency_percentage FROM t_video_behavior WHERE deleted_flag = 0 GROUP BY behavior_type_desc ORDER BY occurrence_count DESC")
    List<Map<String, Object>> selectBehaviorFrequency();

    /**
     * 查询处理时效分析
     */
    @Select("SELECT process_status, AVG(TIMESTAMPDIFF(SECOND, detection_time, process_time)) as avg_process_minutes, COUNT(*) as status_count FROM t_video_behavior WHERE process_status > 0 AND process_time IS NOT NULL AND deleted_flag = 0 GROUP BY process_status")
    List<Map<String, Object>> selectProcessEfficiency();

    // ==================== 统计和维护相关方法 ====================

    /**
     * 更新行为统计数据
     */
    @Update("CALL sp_update_behavior_statistics(#{statDate}, #{statType})")
    int updateBehaviorStatistics(@Param("statDate") LocalDateTime statDate, @Param("statType") String statType);

    /**
     * 同步行为检测索引
     */
    @Insert("INSERT INTO t_video_behavior_index (behavior_id, device_id, detection_time, behavior_type, severity_level, confidence_score, alarm_triggered, process_status, person_id, time_bucket, type_severity_bucket, alarm_status_bucket) " +
            "SELECT behavior_id, device_id, detection_time, behavior_type, severity_level, confidence_score, alarm_triggered, process_status, person_id, " +
            "DATE_FORMAT(detection_time, '%Y%m%d%H') as time_bucket, " +
            "CONCAT(behavior_type, '_', severity_level) as type_severity_bucket, " +
            "CONCAT(alarm_triggered, '_', process_status) as alarm_status_bucket " +
            "FROM t_video_behavior " +
            "WHERE detection_time >= #{startTime} AND deleted_flag = 0 " +
            "ON DUPLICATE KEY UPDATE device_id = VALUES(device_id), detection_time = VALUES(detection_time), severity_level = VALUES(severity_level), confidence_score = VALUES(confidence_score), alarm_triggered = VALUES(alarm_triggered), process_status = VALUES(process_status), person_id = VALUES(person_id), time_bucket = VALUES(time_bucket), type_severity_bucket = VALUES(type_severity_bucket), alarm_status_bucket = VALUES(alarm_status_bucket)")
    int syncBehaviorIndex(@Param("startTime") LocalDateTime startTime);

    /**
     * 清理历史行为记录
     */
    @Update("UPDATE t_video_behavior SET deleted_flag = 1, update_time = NOW() WHERE detection_time < #{cutoffTime} AND deleted_flag = 0")
    int cleanOldBehaviors(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 获取行为检测记录总数
     */
    @Select("SELECT COUNT(*) FROM t_video_behavior WHERE deleted_flag = 0")
    long countTotalBehaviors();

    /**
     * 获取指定时间范围内的行为数量
     */
    @Select("SELECT COUNT(*) FROM t_video_behavior WHERE detection_time >= #{startTime} AND detection_time <= #{endTime} AND deleted_flag = 0")
    long countBehaviorsByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取设备的行为数量
     */
    @Select("SELECT device_id, COUNT(*) as behavior_count FROM t_video_behavior WHERE deleted_flag = 0 GROUP BY device_id ORDER BY behavior_count DESC")
    List<Map<String, Object>> countBehaviorByDevice();

    /**
     * 获取人员的行为数量
     */
    @Select("SELECT person_id, COUNT(*) as behavior_count FROM t_video_behavior WHERE person_id IS NOT NULL AND deleted_flag = 0 GROUP BY person_id ORDER BY behavior_count DESC")
    List<Map<String, Object>> countBehaviorByPerson();

    /**
     * 获取指定人员的行为记录
     */
    @Select("SELECT * FROM t_video_behavior WHERE person_id = #{personId} AND deleted_flag = 0 ORDER BY detection_time DESC")
    List<VideoBehaviorEntity> selectByPersonId(@Param("personId") Long personId);

    /**
     * 批量更新处理状态
     */
    @Update("UPDATE t_video_behavior SET process_status = #{processStatus}, process_time = NOW(), process_user_id = #{userId}, process_user_name = #{userName} WHERE FIND_IN_SET(behavior_id, #{behaviorIds}) AND deleted_flag = 0")
    int batchUpdateProcessStatus(@Param("behaviorIds") String behaviorIds, @Param("processStatus") Integer processStatus, @Param("userId") Long userId, @Param("userName") String userName);

    /**
     * 获取最近的异常行为
     */
    @Select("SELECT * FROM t_video_behavior WHERE behavior_type = 5 AND deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoBehaviorEntity> selectRecentAbnormalBehaviors(@Param("limit") Integer limit);

    /**
     * 获取高置信度行为
     */
    @Select("SELECT * FROM t_video_behavior WHERE confidence_score >= #{minConfidence} AND deleted_flag = 0 ORDER BY detection_time DESC LIMIT #{limit}")
    List<VideoBehaviorEntity> selectHighConfidenceBehaviors(@Param("minConfidence") BigDecimal minConfidence, @Param("limit") Integer limit);

    /**
     * 获取需要告警的行为
     */
    @Select("SELECT * FROM t_video_behavior WHERE alarm_triggered = 1 AND process_status = 0 AND deleted_flag = 0 ORDER BY detection_time ASC")
    List<VideoBehaviorEntity> selectAlarmNeedingProcess();

    /**
     * 更新行为告警状态
     */
    @Update("UPDATE t_video_behavior SET alarm_triggered = #{alarmTriggered}, alarm_level = #{alarmLevel}, alarm_types = #{alarmTypes}, update_time = NOW() WHERE behavior_id = #{behaviorId}")
    int updateAlarmStatus(@Param("behaviorId") Long behaviorId, @Param("alarmTriggered") Integer alarmTriggered, @Param("alarmLevel") Integer alarmLevel, @Param("alarmTypes") String alarmTypes);

    /**
     * 按行为类型统计数量
     */
    @Select("SELECT behavior_type, COUNT(*) as count FROM t_video_behavior WHERE deleted_flag = 0 GROUP BY behavior_type")
    List<Map<String, Object>> countByBehaviorType();

    /**
     * 按严重程度统计数量
     */
    @Select("SELECT severity_level, COUNT(*) as count FROM t_video_behavior WHERE deleted_flag = 0 GROUP BY severity_level")
    List<Map<String, Object>> countBySeverityLevel();

    /**
     * 按告警级别统计数量
     */
    @Select("SELECT alarm_level, COUNT(*) as count FROM t_video_behavior WHERE alarm_triggered = 1 AND deleted_flag = 0 GROUP BY alarm_level")
    List<Map<String, Object>> countByAlarmLevel();

    /**
     * 按处理状态统计数量
     */
    @Select("SELECT process_status, COUNT(*) as count FROM t_video_behavior WHERE deleted_flag = 0 GROUP BY process_status")
    List<Map<String, Object>> countByProcessStatus();

    /**
     * 获取平均置信度
     */
    @Select("SELECT AVG(confidence_score) as avg_confidence, MAX(confidence_score) as max_confidence, MIN(confidence_score) as min_confidence FROM t_video_behavior WHERE deleted_flag = 0")
    Map<String, Object> getConfidenceStatistics();

    /**
     * 获取平均处理时间
     */
    @Select("SELECT AVG(TIMESTAMPDIFF(MINUTE, detection_time, process_time)) as avg_process_minutes, MAX(TIMESTAMPDIFF(MINUTE, detection_time, process_time)) as max_process_minutes FROM t_video_behavior WHERE process_status = 2 AND process_time IS NOT NULL AND deleted_flag = 0")
    Map<String, Object> getProcessingTimeStatistics();

    /**
     * 获取每日行为统计
     */
    @Select("SELECT DATE(detection_time) as stat_date, COUNT(*) as total_count, SUM(CASE WHEN alarm_triggered = 1 THEN 1 ELSE 0 END) as alarm_count FROM t_video_behavior WHERE deleted_flag = 0 AND detection_time >= #{startDate} GROUP BY DATE(detection_time) ORDER BY stat_date DESC")
    List<Map<String, Object>> getDailyStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * 获取行为趋势数据
     */
    @Select("SELECT DATE(detection_time) as stat_date, behavior_type, COUNT(*) as count FROM t_video_behavior WHERE deleted_flag = 0 AND detection_time >= #{startDate} AND detection_time <= #{endDate} GROUP BY DATE(detection_time), behavior_type ORDER BY stat_date, behavior_type")
    List<Map<String, Object>> getBehaviorTrend(@Param("startDate") LocalDateTime startDate, @Param("endTime") LocalDateTime endTime);
}