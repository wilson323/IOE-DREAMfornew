package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.video.DeviceHealthEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备健康检查数据访问对象
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface DeviceHealthDao extends BaseMapper<DeviceHealthEntity> {

    /**
     * 查询指定设备的最新健康记录
     *
     * @param deviceId 设备ID
     * @return 最新健康记录
     */
    @Select("SELECT * FROM t_video_device_health " +
            "WHERE device_id = #{deviceId} " +
            "ORDER BY check_time DESC " +
            "LIMIT 1")
    DeviceHealthEntity selectLatestByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 查询指定设备的健康历史
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 健康记录列表
     */
    @Select("SELECT * FROM t_video_device_health " +
            "WHERE device_id = #{deviceId} " +
            "ORDER BY check_time DESC " +
            "LIMIT #{limit}")
    List<DeviceHealthEntity> selectHealthHistory(@Param("deviceId") Long deviceId,
                                                   @Param("limit") int limit);

    /**
     * 查询指定时间范围内的健康记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 健康记录列表
     */
    @Select("SELECT * FROM t_video_device_health " +
            "WHERE check_time >= #{startTime} AND check_time <= #{endTime} " +
            "ORDER BY check_time DESC")
    List<DeviceHealthEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 查询需要告警的健康记录
     *
     * @param alarmLevel 告警级别（1-提示 2-警告 3-严重）
     * @return 健康记录列表
     */
    @Select("SELECT * FROM t_video_device_health " +
            "WHERE alarm_level >= #{alarmLevel} " +
            "ORDER BY check_time DESC")
    List<DeviceHealthEntity> selectAlarmRecords(@Param("alarmLevel") int alarmLevel);

    /**
     * 统计各健康状态的设备数量
     *
     * @return 统计结果列表
     */
    @Select("SELECT health_status, COUNT(*) as count FROM t_video_device_health " +
            "WHERE check_time = ( " +
            "  SELECT MAX(check_time) FROM t_video_device_health " +
            ") " +
            "GROUP BY health_status")
    List<HealthStatusCount> selectHealthStatusStatistics();

    /**
     * 健康状态统计结果
     */
    class HealthStatusCount {
        public Integer healthStatus;
        public Long count;
    }
}
