package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.AccessAlarmEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁报警数据访问接口
 * <p>
 * 提供报警数据的CRUD操作和复杂查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AccessAlarmDao extends BaseMapper<AccessAlarmEntity> {

    /**
     * 统计指定时间范围内的报警数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 报警数量
     */
    @Select("SELECT COUNT(*) FROM t_access_alarm WHERE alarm_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0")
    long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内未处理的报警数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 未处理报警数量
     */
    @Select("SELECT COUNT(*) FROM t_access_alarm WHERE alarm_time BETWEEN #{startTime} AND #{endTime} AND processed = 0 AND deleted_flag = 0")
    long countUnhandledByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内按报警级别分组的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果列表，每项包含 alarmLevel 和 count
     */
    @Select("SELECT alarm_level, COUNT(*) as count FROM t_access_alarm " +
            "WHERE alarm_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0 " +
            "GROUP BY alarm_level")
    List<Map<String, Object>> countByAlarmLevel(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内按报警类型分组的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果列表，每项包含 alarmType 和 count
     */
    @Select("SELECT alarm_type, COUNT(*) as count FROM t_access_alarm " +
            "WHERE alarm_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0 " +
            "GROUP BY alarm_type")
    List<Map<String, Object>> countByAlarmType(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的严重报警
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param minLevel   最小报警级别
     * @return 严重报警列表
     */
    List<AccessAlarmEntity> selectCriticalAlarms(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("minLevel") Integer minLevel);

    /**
     * 查询指定设备的活跃报警
     *
     * @param deviceId 设备ID
     * @return 活跃报警列表
     */
    @Select("SELECT * FROM t_access_alarm " +
            "WHERE device_id = #{deviceId} AND alarm_status = 'ACTIVE' AND processed = 0 AND deleted_flag = 0 " +
            "ORDER BY alarm_time DESC")
    List<AccessAlarmEntity> selectActiveAlarmsByDevice(@Param("deviceId") Long deviceId);

    /**
     * 查询重复报警（同一设备、同一类型、短时间内）
     *
     * @param deviceId      设备ID
     * @param alarmType     报警类型
     * @param timeWindowMin 时间窗口（分钟）
     * @return 重复报警列表
     */
    @Select("SELECT * FROM t_access_alarm " +
            "WHERE device_id = #{deviceId} AND alarm_type = #{alarmType} " +
            "AND alarm_time >= DATE_SUB(NOW(), INTERVAL #{timeWindowMin} MINUTE) " +
            "AND deleted_flag = 0 " +
            "ORDER BY alarm_time DESC")
    List<AccessAlarmEntity> selectRepeatAlarms(@Param("deviceId") Long deviceId,
                                              @Param("alarmType") String alarmType,
                                              @Param("timeWindowMin") Integer timeWindowMin);

    /**
     * 查询今日报警统计
     *
     * @return 统计结果，包含 total, handled, unhandled, critical
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN processed = 1 THEN 1 ELSE 0 END) as handled, " +
            "SUM(CASE WHEN processed = 0 THEN 1 ELSE 0 END) as unhandled, " +
            "SUM(CASE WHEN alarm_level >= 4 THEN 1 ELSE 0 END) as critical " +
            "FROM t_access_alarm " +
            "WHERE DATE(alarm_time) = CURDATE() AND deleted_flag = 0")
    Map<String, Object> selectTodayStatistics();

    /**
     * 查询最近的未处理报警
     *
     * @param limit 限制数量
     * @return 未处理报警列表
     */
    @Select("SELECT * FROM t_access_alarm " +
            "WHERE processed = 0 AND deleted_flag = 0 " +
            "ORDER BY alarm_level DESC, alarm_time DESC " +
            "LIMIT #{limit}")
    List<AccessAlarmEntity> selectRecentUnhandled(@Param("limit") int limit);

    /**
     * 统计各设备报警数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果列表，每项包含 deviceId, deviceName, count
     */
    @Select("SELECT a.device_id, a.device_name, COUNT(*) as count " +
            "FROM t_access_alarm a " +
            "WHERE a.alarm_time BETWEEN #{startTime} AND #{endTime} AND a.deleted_flag = 0 " +
            "GROUP BY a.device_id, a.device_name " +
            "ORDER BY count DESC")
    List<Map<String, Object>> countByDevice(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}
