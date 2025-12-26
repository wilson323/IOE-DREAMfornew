package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.DeviceAlertEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备告警DAO接口
 * <p>
 * 提供设备告警数据的访问操作：
 * - 基础CRUD（继承BaseMapper）
 * - 按设备查询告警
 * - 按告警级别查询
 * - 按告警状态查询
 * - 统计查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface DeviceAlertDao extends BaseMapper<DeviceAlertEntity> {

    /**
     * 根据设备ID查询未确认的告警列表
     *
     * @param deviceId 设备ID
     * @return 未确认告警列表
     */
    @Select("SELECT * FROM t_device_alert WHERE device_id = #{deviceId} AND alert_status = 0 ORDER BY alert_occurred_time DESC")
    List<DeviceAlertEntity> selectUnconfirmedByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据告警级别和状态查询告警列表
     *
     * @param alertLevel 告警级别
     * @param alertStatus 告警状态
     * @return 告警列表
     */
    @Select("SELECT * FROM t_device_alert WHERE alert_level = #{alertLevel} AND alert_status = #{alertStatus} ORDER BY alert_occurred_time DESC")
    List<DeviceAlertEntity> selectByLevelAndStatus(@Param("alertLevel") Integer alertLevel,
                                                    @Param("alertStatus") Integer alertStatus);

    /**
     * 查询指定时间范围内的告警列表
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 告警列表
     */
    @Select("SELECT * FROM t_device_alert WHERE alert_occurred_time BETWEEN #{startTime} AND #{endTime} ORDER BY alert_occurred_time DESC")
    List<DeviceAlertEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各告警级别的数量
     *
     * @return 统计结果 [{"alertLevel": 1, "count": 10}, ...]
     */
    @Select("SELECT alert_level, COUNT(*) as count FROM t_device_alert WHERE deleted_flag = 0 GROUP BY alert_level")
    List<Object> countByAlertLevel();

    /**
     * 统计各告警状态的数量
     *
     * @return 统计结果 [{"alertStatus": 0, "count": 5}, ...]
     */
    @Select("SELECT alert_status, COUNT(*) as count FROM t_device_alert WHERE deleted_flag = 0 GROUP BY alert_status")
    List<Object> countByAlertStatus();

    /**
     * 查询最近N条紧急告警
     *
     * @param limit 数量限制
     * @return 紧急告警列表
     */
    @Select("SELECT * FROM t_device_alert WHERE alert_level = 4 AND alert_status IN (0,1) ORDER BY alert_occurred_time DESC LIMIT #{limit}")
    List<DeviceAlertEntity> selectRecentCriticalAlerts(@Param("limit") Integer limit);

    /**
     * 统计指定设备在指定时间范围内的告警次数
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 告警次数
     */
    @Select("SELECT COUNT(*) FROM t_device_alert WHERE device_id = #{deviceId} AND alert_occurred_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0")
    Integer countAlertsByDeviceAndTimeRange(@Param("deviceId") Long deviceId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}
