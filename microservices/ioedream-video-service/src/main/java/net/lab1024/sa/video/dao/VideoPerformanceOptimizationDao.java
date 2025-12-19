package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.video.entity.VideoDeviceEntity;
import net.lab1024.sa.video.entity.VideoStreamEntity;
import net.lab1024.sa.video.entity.VideoRecordEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频服务性能优化DAO
 * <p>
 * 提供数据库性能优化的专用查询方法：
 * 1. 批量操作优化
 * 2. 索引优化查询
 * 3. 游标分页查询
 * 4. 统计分析查询
 * 5. 缓存友好查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Mapper
public interface VideoPerformanceOptimizationDao extends BaseMapper<VideoDeviceEntity> {

    // ==================== 批量操作优化 ====================

    /**
     * 批量更新设备状态
     * 使用IN语句避免逐条更新
     */
    @Update({
        "<script>",
        "UPDATE t_video_device",
        "SET device_status = #{status},",
        "    update_time = NOW()",
        "WHERE device_id IN",
        "<foreach collection='deviceIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateDeviceStatus(@Param("deviceIds") List<Long> deviceIds, @Param("status") Integer status);

    /**
     * 批量插入设备状态日志
     * 使用VALUES语法进行批量插入
     */
    @Update({
        "<script>",
        "INSERT INTO t_video_device_status_log (device_id, old_status, new_status, change_time, change_reason) VALUES",
        "<foreach collection='statusLogs' item='log' separator=','>",
        "(#{log.deviceId}, #{log.oldStatus}, #{log.newStatus}, #{log.changeTime}, #{log.changeReason})",
        "</foreach>",
        "</script>"
    })
    @Transactional(rollbackFor = Exception.class)
    int batchInsertDeviceStatusLogs(@Param("statusLogs") List<Map<String, Object>> statusLogs);

    /**
     * 批量删除过期录像记录
     * 使用复合索引优化删除性能
     */
    @Update("DELETE FROM t_video_recording WHERE create_time < #{cutoffTime} AND deleted_flag = 0 LIMIT #{limit}")
    @Transactional(rollbackFor = Exception.class)
    int batchDeleteExpiredRecordings(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("limit") int limit);

    // ==================== 索引优化查询 ====================

    /**
     * 使用游标分页查询设备列表（避免深度分页问题）
     */
    @Select({
        "SELECT * FROM t_video_device",
        "WHERE deleted_flag = 0",
        "  AND (#{cursorDeviceId} IS NULL OR device_id < #{cursorDeviceId})",
        "ORDER BY device_id DESC",
        "LIMIT #{limit}"
    })
    List<VideoDeviceEntity> getDevicesByCursor(
            @Param("cursorDeviceId") Long cursorDeviceId,
            @Param("limit") int limit
    );

    /**
     * 查询指定状态的设备（使用状态索引）
     */
    @Select({
        "SELECT * FROM t_video_device",
        "WHERE deleted_flag = 0 AND device_status = #{status}",
        "ORDER BY create_time DESC",
        "LIMIT #{limit}"
    })
    List<VideoDeviceEntity> getDevicesByStatus(@Param("status") Integer status, @Param("limit") int limit);

    /**
     * 查询指定区域的设备（使用区域索引）
     */
    @Select({
        "SELECT * FROM t_video_device",
        "WHERE deleted_flag = 0 AND area_id = #{areaId}",
        "ORDER BY device_code ASC",
        "LIMIT #{limit}"
    })
    List<VideoDeviceEntity> getDevicesByArea(@Param("areaId") Long areaId, @Param("limit") int limit);

    /**
     * 查询活跃的视频流（使用流状态索引）
     */
    @Select({
        "SELECT * FROM t_video_stream",
        "WHERE deleted_flag = 0 AND stream_status = 1",
        "  AND last_update_time > #{sinceTime}",
        "ORDER BY last_update_time DESC"
    })
    List<VideoStreamEntity> getActiveStreams(@Param("sinceTime") LocalDateTime sinceTime);

    // ==================== 统计分析查询 ====================

    /**
     * 获取设备状态统计（使用索引优化）
     */
    @Select({
        "SELECT device_status, COUNT(*) as count",
        "FROM t_video_device",
        "WHERE deleted_flag = 0",
        "GROUP BY device_status"
    })
    List<Map<String, Object>> getDeviceStatusStatistics();

    /**
     * 获取按区域分组的设备统计
     */
    @Select({
        "SELECT area_id, COUNT(*) as device_count,",
        "       SUM(CASE WHEN device_status = 1 THEN 1 ELSE 0 END) as online_count",
        "FROM t_video_device",
        "WHERE deleted_flag = 0",
        "GROUP BY area_id"
    })
    List<Map<String, Object>> getDeviceStatisticsByArea();

    /**
     * 获取录像存储统计
     */
    @Select({
        "SELECT DATE(create_time) as date,",
        "       COUNT(*) as recording_count,",
        "       SUM(file_size) as total_size",
        "FROM t_video_recording",
        "WHERE deleted_flag = 0",
        "  AND create_time >= #{startDate}",
        "  AND create_time < #{endDate}",
        "GROUP BY DATE(create_time)",
        "ORDER BY date DESC"
    })
    List<Map<String, Object>> getRecordingStorageStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 获取视频流质量统计
     */
    @Select({
        "SELECT device_id,",
        "       AVG(framerate) as avg_framerate,",
        "       AVG(bitrate) as avg_bitrate,",
        "       COUNT(*) as sample_count",
        "FROM t_video_stream_quality",
        "WHERE sample_time >= #{startTime}",
        "  AND sample_time < #{endTime}",
        "GROUP BY device_id",
        "HAVING COUNT(*) >= #{minSamples}"
    })
    List<Map<String, Object>> getStreamQualityStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("minSamples") int minSamples
    );

    // ==================== 性能监控查询 ====================

    /**
     * 获取设备连接性能统计
     */
    @Select({
        "SELECT device_id,",
        "       AVG(connection_time) as avg_connection_time,",
        "       MIN(connection_time) as min_connection_time,",
        "       MAX(connection_time) as max_connection_time,",
        "       COUNT(*) as connection_count",
        "FROM t_video_device_connection_log",
        "WHERE connect_time >= #{startTime}",
        "GROUP BY device_id"
    })
    List<Map<String, Object>> getDeviceConnectionStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取API调用性能统计
     */
    @Select({
        "SELECT api_path,",
        "       COUNT(*) as call_count,",
        "       AVG(response_time) as avg_response_time,",
        "       MIN(response_time) as min_response_time,",
        "       MAX(response_time) as max_response_time,",
        "       SUM(CASE WHEN success_flag = 1 THEN 1 ELSE 0 END) as success_count",
        "FROM t_video_api_call_log",
        "WHERE call_time >= #{startTime}",
        "  AND call_time < #{endTime}",
        "GROUP BY api_path",
        "ORDER BY avg_response_time DESC"
    })
    List<Map<String, Object>> getApiPerformanceStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // ==================== 缓存友好查询 ====================

    /**
     * 获取设备基本信息（缓存友好）
     */
    @Select({
        "SELECT device_id, device_code, device_name, device_type, device_status,",
        "       ip_address, port, area_id",
        "FROM t_video_device",
        "WHERE device_id = #{deviceId} AND deleted_flag = 0"
    })
    Map<String, Object> getDeviceInfoForCache(@Param("deviceId") Long deviceId);

    /**
     * 获取设备配置信息（缓存友好）
     */
    @Select({
        "SELECT device_id, config_type, config_value, update_time",
        "FROM t_video_device_config",
        "WHERE device_id = #{deviceId} AND enabled_flag = 1"
    })
    List<Map<String, Object>> getDeviceConfigForCache(@Param("deviceId") Long deviceId);

    /**
     * 获取用户设备权限（缓存友好）
     */
    @Select({
        "SELECT user_id, device_id, permission_level",
        "FROM t_video_device_permission",
        "WHERE user_id = #{userId} AND permission_level > 0",
        "  AND start_time <= NOW() AND (end_time IS NULL OR end_time > NOW())"
    })
    List<Map<String, Object>> getUserDevicePermissionsForCache(@Param("userId") Long userId);

    // ==================== 数据清理查询 ====================

    /**
     * 清理过期的日志数据
     */
    @Update("DELETE FROM t_video_operation_log WHERE create_time < #{cutoffTime} LIMIT #{limit}")
    int cleanupExpiredOperationLogs(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("limit") int limit);

    /**
     * 清理过期的会话数据
     */
    @Update("DELETE FROM t_video_stream_session WHERE end_time < #{cutoffTime} AND end_time IS NOT NULL LIMIT #{limit}")
    int cleanupExpiredStreamSessions(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("limit") int limit);

    /**
     * 清理过期的临时文件记录
     */
    @Update("DELETE FROM t_video_temp_file WHERE create_time < #{cutoffTime} LIMIT #{limit}")
    int cleanupExpiredTempFiles(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("limit") int limit);

    // ==================== 优化索引建议查询 ====================

    /**
     * 查询慢查询记录
     */
    @Select({
        "SELECT query_hash, query_template, execution_count,",
        "       AVG(execution_time) as avg_time,",
        "       MAX(execution_time) as max_time,",
        "       SUM(execution_time) as total_time",
        "FROM t_video_slow_query_log",
        "WHERE record_time >= #{startTime}",
        "GROUP BY query_hash, query_template",
        "HAVING execution_count >= #{minExecutions}",
        "ORDER BY avg_time DESC"
    })
    List<Map<String, Object>> getSlowQueryStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("minExecutions") int minExecutions
    );

    /**
     * 分析表数据分布
     */
    @Select({
        "SELECT",
        "  (SELECT COUNT(*) FROM t_video_device WHERE deleted_flag = 0) as total_devices,",
        "  (SELECT COUNT(*) FROM t_video_stream WHERE deleted_flag = 0) as total_streams,",
        "  (SELECT COUNT(*) FROM t_video_recording WHERE deleted_flag = 0) as total_recordings,",
        "  (SELECT COUNT(*) FROM t_video_device_connection_log WHERE connect_time >= DATE_SUB(NOW(), INTERVAL 1 DAY)) as daily_connections",
        "  (SELECT COUNT(*) FROM t_video_operation_log WHERE create_time >= DATE_SUB(NOW(), INTERVAL 1 DAY)) as daily_operations"
    })
    Map<String, Object> getTableDistributionStatistics();
}
