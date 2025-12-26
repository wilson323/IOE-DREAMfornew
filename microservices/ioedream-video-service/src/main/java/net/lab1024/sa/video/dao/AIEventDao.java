package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.video.domain.entity.AIEventEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI事件数据访问层
 * <p>
 * 提供AI智能分析事件的数据访问操作：
 * 1. 基础CRUD操作
 * 2. 复杂条件查询
 * 3. 统计分析查询
 * 4. 批量操作优化
 * 5. 性能优化查询
 * 6. 数据清理操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Mapper
public interface AIEventDao extends BaseMapper<AIEventEntity> {

    // ==================== 复杂查询方法 ====================

    /**
     * 根据设备ID和时间范围查询事件
     */
    @Select({
        "SELECT * FROM t_video_ai_event",
        "WHERE device_id = #{deviceId}",
        "  AND create_time >= #{startTime}",
        "  AND create_time <= #{endTime}",
        "  AND deleted_flag = 0",
        "ORDER BY create_time DESC"
    })
    List<AIEventEntity> selectEventsByDeviceAndTime(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 根据事件类型和优先级查询事件
     */
    @Select({
        "SELECT * FROM t_video_ai_event",
        "WHERE event_type = #{eventType}",
        "  AND priority >= #{minPriority}",
        "  AND event_status = #{status}",
        "  AND deleted_flag = 0",
        "ORDER BY priority DESC, create_time DESC",
        "LIMIT #{limit}"
    })
    List<AIEventEntity> selectEventsByTypeAndPriority(
            @Param("eventType") String eventType,
            @Param("minPriority") Integer minPriority,
            @Param("status") Integer status,
            @Param("limit") Integer limit
    );

    /**
     * 查询需要处理的AI事件（高优先级）
     */
    @Select({
        "SELECT * FROM t_video_ai_event",
        "WHERE event_status = 1",  // 待处理
        "  AND priority >= #{minPriority}",
        "  AND deleted_flag = 0",
        "ORDER BY priority DESC, create_time ASC",
        "LIMIT #{limit}"
    })
    List<AIEventEntity> selectPendingHighPriorityEvents(
            @Param("minPriority") Integer minPriority,
            @Param("limit") Integer limit
    );

    /**
     * 根据位置查询事件
     */
    @Select({
        "SELECT * FROM t_video_ai_event",
        "WHERE location LIKE CONCAT('%', #{location}, '%')",
        "  AND create_time >= #{startTime}",
        "  AND deleted_flag = 0",
        "ORDER BY create_time DESC",
        "LIMIT #{limit}"
    })
    List<AIEventEntity> selectEventsByLocation(
            @Param("location") String location,
            @Param("startTime") LocalDateTime startTime,
            @Param("limit") Integer limit
    );

    // ==================== 统计分析查询 ====================

    /**
     * 按事件类型统计事件数量
     */
    @Select({
        "SELECT event_type, COUNT(*) as count",
        "FROM t_video_ai_event",
        "WHERE create_time >= #{startTime}",
        "  AND create_time <= #{endTime}",
        "  AND deleted_flag = 0",
        "GROUP BY event_type"
    })
    List<Map<String, Object>> selectEventCountByType(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按设备统计事件数量
     */
    @Select({
        "SELECT device_id, COUNT(*) as event_count",
        "FROM t_video_ai_event",
        "WHERE create_time >= #{startTime}",
        "  AND create_time <= #{endTime}",
        "  AND deleted_flag = 0",
        "GROUP BY device_id",
        "HAVING COUNT(*) >= #{minCount}",
        "ORDER BY event_count DESC"
    })
    List<Map<String, Object>> selectEventCountByDevice(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("minCount") Integer minCount
    );

    /**
     * 按优先级统计事件数量
     */
    @Select({
        "SELECT priority, COUNT(*) as count",
        "FROM t_video_ai_event",
        "WHERE create_time >= #{startTime}",
        "  AND create_time <= #{endTime}",
        "  AND deleted_flag = 0",
        "GROUP BY priority",
        "ORDER BY priority DESC"
    })
    List<Map<String, Object>> selectEventCountByPriority(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按处理状态统计事件数量
     */
    @Select({
        "SELECT event_status, COUNT(*) as count",
        "FROM t_video_ai_event",
        "WHERE create_time >= #{startTime}",
        "  AND create_time <= #{endTime}",
        "  AND deleted_flag = 0",
        "GROUP BY event_status"
    })
    List<Map<String, Object>> selectEventCountByStatus(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按小时统计事件趋势
     */
    @Select({
        "SELECT",
        "  HOUR(create_time) as hour,",
        "  COUNT(*) as count",
        "FROM t_video_ai_event",
        "WHERE create_time >= #{startTime}",
        "  AND create_time <= #{endTime}",
        "  AND deleted_flag = 0",
        "GROUP BY HOUR(create_time)",
        "ORDER BY hour"
    })
    List<Map<String, Object>> selectEventTrendByHour(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 获取设备事件统计详情
     */
    @Select({
        "SELECT",
        "  device_id,",
        "  COUNT(*) as total_events,",
        "  SUM(CASE WHEN event_status = 1 THEN 1 ELSE 0 END) as pending_events,",
        "  SUM(CASE WHEN event_status = 2 THEN 1 ELSE 0 END) as processed_events,",
        "  AVG(priority) as avg_priority,",
        "  MAX(priority) as max_priority,",
        "  MAX(create_time) as last_event_time",
        "FROM t_video_ai_event",
        "WHERE device_id = #{deviceId}",
        "  AND create_time >= #{startTime}",
        "  AND create_time <= #{endTime}",
        "  AND deleted_flag = 0",
        "GROUP BY device_id"
    })
    Map<String, Object> selectDeviceEventStatistics(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // ==================== 批量操作方法 ====================

    /**
     * 批量更新事件状态
     */
    @Update({
        "<script>",
        "UPDATE t_video_ai_event",
        "SET event_status = #{status},",
        "    process_time = NOW(),",
        "    update_time = NOW()",
        "WHERE id IN",
        "<foreach collection='eventIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "  AND deleted_flag = 0",
        "</script>"
    })
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateEventStatus(@Param("eventIds") List<Long> eventIds, @Param("status") Integer status);

    /**
     * 批量更新事件优先级
     */
    @Update({
        "<script>",
        "UPDATE t_video_ai_event",
        "SET priority = #{priority},",
        "    update_time = NOW()",
        "WHERE id IN",
        "<foreach collection='eventIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "  AND deleted_flag = 0",
        "</script>"
    })
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateEventPriority(@Param("eventIds") List<Long> eventIds, @Param("priority") Integer priority);

    /**
     * 批量设置处理结果
     */
    @Update({
        "<script>",
        "UPDATE t_video_ai_event",
        "SET event_status = 2,",
        "    process_result = #{processResult},",
        "    process_time = NOW(),",
        "    process_times = process_times + 1,",
        "    update_time = NOW()",
        "WHERE id IN",
        "<foreach collection='eventIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "  AND deleted_flag = 0",
        "</script>"
    })
    @Transactional(rollbackFor = Exception.class)
    int batchProcessEvent(@Param("eventIds") List<Long> eventIds, @Param("processResult") String processResult);

    // ==================== 数据清理方法 ====================

    /**
     * 清理过期的已处理事件
     */
    @Update({
        "DELETE FROM t_video_ai_event",
        "WHERE create_time < #{cutoffTime}",
        "  AND event_status = 2",  // 已处理
        "  AND deleted_flag = 0",
        "LIMIT #{limit}"
    })
    @Transactional(rollbackFor = Exception.class)
    int deleteExpiredProcessedEvents(
            @Param("cutoffTime") LocalDateTime cutoffTime,
            @Param("limit") Integer limit
    );

    /**
     * 清理旧的低优先级事件
     */
    @Update({
        "DELETE FROM t_video_ai_event",
        "WHERE create_time < #{cutoffTime}",
        "  AND priority <= #{maxPriority}",
        "  AND event_status = 1",  // 待处理（可能是遗漏的事件）
        "  AND deleted_flag = 0",
        "LIMIT #{limit}"
    })
    @Transactional(rollbackFor = Exception.class)
    int deleteOldLowPriorityEvents(
            @Param("cutoffTime") LocalDateTime cutoffTime,
            @Param("maxPriority") Integer maxPriority,
            @Param("limit") Integer limit
    );

    /**
     * 软删除指定时间之前的事件
     */
    @Update({
        "UPDATE t_video_ai_event",
        "SET deleted_flag = 1,",
        "    update_time = NOW()",
        "WHERE create_time < #{cutoffTime}",
        "  AND deleted_flag = 0",
        "LIMIT #{limit}"
    })
    @Transactional(rollbackFor = Exception.class)
    int softDeleteOldEvents(
            @Param("cutoffTime") LocalDateTime cutoffTime,
            @Param("limit") Integer limit
    );

    // ==================== 性能优化查询 ====================

    /**
     * 分页查询未处理事件（使用索引优化）
     */
    @Select({
        "SELECT * FROM t_video_ai_event",
        "WHERE event_status = 1",
        "  AND create_time < #{beforeTime}",
        "  AND deleted_flag = 0",
        "ORDER BY priority DESC, create_time ASC",
        "LIMIT #{limit}"
    })
    List<AIEventEntity> selectPendingEventsPage(
            @Param("beforeTime") LocalDateTime beforeTime,
            @Param("limit") Integer limit
    );

    /**
     * 查询事件处理效率统计
     */
    @Select({
        "SELECT",
        "  DATE(create_time) as date,",
        "  COUNT(*) as total_events,",
        "  SUM(CASE WHEN event_status = 2 THEN 1 ELSE 0 END) as processed_events,",
        "  AVG(TIMESTAMPDIFF(SECOND, create_time, process_time)) as avg_process_seconds",
        "  COUNT(DISTINCT device_id) as active_devices",
        "FROM t_video_ai_event",
        "WHERE create_time >= #{startTime}",
        "  AND create_time <= #{endTime}",
        "  AND deleted_flag = 0",
        "GROUP BY DATE(create_time)",
        "ORDER BY date DESC"
    })
    List<Map<String, Object>> selectEventProcessEfficiency(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询AI模型准确率统计
     */
    @Select({
        "SELECT",
        "  event_type,",
        "  AVG(confidence) as avg_confidence,",
        "  COUNT(*) as total_events,",
        "  SUM(CASE WHEN priority >= 8 THEN 1 ELSE 0 END) as high_priority_count,",
        "  SUM(CASE WHEN event_status = 2 THEN 1 ELSE 0 END) as processed_count",
        "FROM t_video_ai_event",
        "WHERE create_time >= #{startTime}",
        "  AND create_time <= #{endTime}",
        "  AND deleted_flag = 0",
        "GROUP BY event_type"
    })
    List<Map<String, Object>> selectAIModelAccuracy(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // ==================== 实时数据查询 ====================

    /**
     * 获取最近的实时事件
     */
    @Select({
        "SELECT * FROM t_video_ai_event",
        "WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)",
        "  AND deleted_flag = 0",
        "ORDER BY create_time DESC",
        "LIMIT #{limit}"
    })
    List<AIEventEntity> selectRecentRealtimeEvents(
            @Param("minutes") Integer minutes,
            @Param("limit") Integer limit
    );

    /**
     * 获取指定类型的最新事件
     */
    @Select({
        "SELECT * FROM t_video_ai_event",
        "WHERE event_type = #{eventType}",
        "  AND deleted_flag = 0",
        "ORDER BY create_time DESC",
        "LIMIT 1"
    })
    AIEventEntity selectLatestEventByType(@Param("eventType") String eventType);

    /**
     * 统计未处理事件数量（快速查询）
     */
    @Select({
        "SELECT COUNT(*) FROM t_video_ai_event",
        "WHERE event_status = 1",
        "  AND deleted_flag = 0"
    })
    long countUnprocessedEvents();

    /**
     * 统计高优先级未处理事件数量
     */
    @Select({
        "SELECT COUNT(*) FROM t_video_ai_event",
        "WHERE event_status = 1",
        "  AND priority >= #{minPriority}",
        "  AND deleted_flag = 0"
    })
    long countHighPriorityUnprocessedEvents(@Param("minPriority") Integer minPriority);
}
