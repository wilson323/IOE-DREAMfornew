package net.lab1024.sa.admin.module.video.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.admin.module.video.domain.entity.MonitorEventEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 监控事件DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface MonitorEventDao extends BaseMapper<MonitorEventEntity> {

    /**
     * 分页查询监控事件
     *
     * @param pageParam 分页参数
     * @param condition 查询条件
     * @return 分页结果
     */
    PageResult<MonitorEventEntity> selectPage(PageParam pageParam, MonitorEventEntity condition);

    /**
     * 统计今日事件数量
     *
     * @param deviceId 设备ID
     * @return 今日事件数量
     */
    Long getTodayEventCount(@Param("deviceId") Long deviceId);

    /**
     * 根据设备ID查询监控事件
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 监控事件列表
     */
    List<MonitorEventEntity> selectByDeviceId(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 根据事件类型查询监控事件
     *
     * @param eventType 事件类型
     * @param limit 限制数量
     * @return 监控事件列表
     */
    List<MonitorEventEntity> selectByEventType(@Param("eventType") String eventType, @Param("limit") Integer limit);

    /**
     * 根据事件级别查询监控事件
     *
     * @param eventLevel 事件级别
     * @param limit 限制数量
     * @return 监控事件列表
     */
    List<MonitorEventEntity> selectByEventLevel(@Param("eventLevel") String eventLevel, @Param("limit") Integer limit);

    /**
     * 根据事件状态查询监控事件
     *
     * @param eventStatus 事件状态
     * @param limit 限制数量
     * @return 监控事件列表
     */
    List<MonitorEventEntity> selectByEventStatus(@Param("eventStatus") String eventStatus, @Param("limit") Integer limit);

    /**
     * 根据时间范围查询监控事件
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 监控事件列表
     */
    List<MonitorEventEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             @Param("limit") Integer limit);

    /**
     * 查询紧急事件
     *
     * @param limit 限制数量
     * @return 紧急事件列表
     */
    List<MonitorEventEntity> selectUrgentEvents(@Param("limit") Integer limit);

    /**
     * 查询高优先级事件
     *
     * @param deviceId 设备ID（可选）
     * @param limit 限制数量
     * @return 高优先级事件列表
     */
    List<MonitorEventEntity> selectHighPriorityEvents(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 批量更新事件状态
     *
     * @param eventIds 事件ID列表
     * @param eventStatus 新状态
     * @return 更新行数
     */
    int batchUpdateEventStatus(@Param("eventIds") List<Long> eventIds,
                              @Param("eventStatus") String eventStatus);

    /**
     * 删除指定时间之前的事件
     *
     * @param beforeTime 时间点
     * @return 删除行数
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 统计各类型事件数量
     *
     * @param deviceId 设备ID（可选）
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> countEventsByType(@Param("deviceId") Long deviceId);
}
