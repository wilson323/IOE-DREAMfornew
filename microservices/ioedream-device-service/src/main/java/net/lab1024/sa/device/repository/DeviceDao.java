package net.lab1024.sa.device.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.device.domain.entity.DeviceEntity;

/**
 * 设备数据访问层
 * 基于现有SmartDeviceDao重构，扩展为统一设备管理
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原SmartDeviceDao重构)
 */
@Mapper
public interface DeviceRepository extends BaseMapper<DeviceEntity> {

    /**
     * 根据设备编码查询设备（基于原selectByDeviceCode）
     */
    @Select("SELECT * FROM t_smart_device WHERE deleted_flag = 0 AND device_code = #{deviceCode}")
    DeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据设备类型查询设备列表（基于原selectByDeviceType）
     */
    @Select("SELECT * FROM t_smart_device WHERE deleted_flag = 0 AND device_type = #{deviceType} ORDER BY create_time DESC")
    List<DeviceEntity> selectByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 根据设备状态查询设备列表（基于原selectByDeviceStatus）
     */
    @Select("SELECT * FROM t_smart_device WHERE deleted_flag = 0 AND device_status = #{deviceStatus} ORDER BY create_time DESC")
    List<DeviceEntity> selectByDeviceStatus(@Param("deviceStatus") String deviceStatus);

    /**
     * 根据分组ID查询设备列表（基于原selectByGroupId）
     */
    @Select("SELECT * FROM t_smart_device WHERE deleted_flag = 0 AND group_id = #{groupId} ORDER BY create_time DESC")
    List<DeviceEntity> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据区域ID查询设备列表（基于AccessDeviceEntity.areaId）
     */
    @Select("SELECT * FROM t_smart_device WHERE deleted_flag = 0 AND area_id = #{areaId} ORDER BY create_time DESC")
    List<DeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询在线设备列表（基于原selectOnlineDevices）
     */
    @Select("SELECT * FROM t_smart_device WHERE deleted_flag = 0 AND device_status = 'ONLINE' ORDER BY last_online_time DESC")
    List<DeviceEntity> selectOnlineDevices();

    /**
     * 查询离线设备列表（基于原selectOfflineDevices）
     */
    @Select("SELECT * FROM t_smart_device WHERE deleted_flag = 0 AND device_status = 'OFFLINE' ORDER BY last_online_time DESC")
    List<DeviceEntity> selectOfflineDevices();

    /**
     * 查询心跳超时的设备（基于AccessDeviceEntity心跳逻辑）
     */
    @Select("SELECT * FROM t_smart_device WHERE deleted_flag = 0 AND " +
            "(last_heartbeat_time IS NULL OR " +
            "last_heartbeat_time < DATE_SUB(NOW(), INTERVAL COALESCE(heartbeat_interval * 3, 5) MINUTE)) " +
            "ORDER BY last_heartbeat_time DESC")
    List<DeviceEntity> selectHeartbeatTimeoutDevices();

    /**
     * 查询需要维护的设备（基于AccessDeviceEntity.needsMaintenance逻辑）
     */
    @Select("SELECT * FROM t_smart_device WHERE deleted_flag = 0 AND " +
            "(device_status = 'FAULT' OR " +
            "(device_status = 'OFFLINE' AND last_heartbeat_time IS NOT NULL AND " +
            "last_heartbeat_time < DATE_SUB(NOW(), INTERVAL 24 HOUR))) " +
            "ORDER BY last_heartbeat_time DESC")
    List<DeviceEntity> selectMaintenanceRequiredDevices();

    /**
     * 更新设备状态（基于原updateDeviceStatus）
     */
    @Update("UPDATE t_smart_device SET device_status = #{deviceStatus}, update_time = NOW() WHERE device_id = #{deviceId}")
    int updateDeviceStatus(@Param("deviceId") Long deviceId, @Param("deviceStatus") String deviceStatus);

    /**
     * 更新设备最后在线时间（基于原updateLastOnlineTime）
     */
    @Update("UPDATE t_smart_device SET last_online_time = #{lastOnlineTime}, update_time = NOW() WHERE device_id = #{deviceId}")
    int updateLastOnlineTime(@Param("deviceId") Long deviceId, @Param("lastOnlineTime") LocalDateTime lastOnlineTime);

    /**
     * 更新设备最后心跳时间（基于AccessDeviceEntity心跳机制）
     */
    @Update("UPDATE t_smart_device SET last_heartbeat_time = #{lastHeartbeatTime}, update_time = NOW() WHERE device_id = #{deviceId}")
    int updateLastHeartbeatTime(@Param("deviceId") Long deviceId,
            @Param("lastHeartbeatTime") LocalDateTime lastHeartbeatTime);

    /**
     * 批量更新设备状态（基于原batchUpdateDeviceStatus）
     */
    @Update("<script>" +
            "UPDATE t_smart_device SET device_status = #{deviceStatus}, update_time = NOW() WHERE device_id IN " +
            "<foreach collection='deviceIds' item='deviceId' open='(' separator=',' close=')'>" +
            "#{deviceId}" +
            "</foreach>" +
            "</script>")
    int batchUpdateDeviceStatus(@Param("deviceIds") List<Long> deviceIds, @Param("deviceStatus") String deviceStatus);

    /**
     * 统计设备数量按类型（基于原countByDeviceType）
     */
    @Select("SELECT device_type as deviceType, COUNT(*) as count FROM t_smart_device " +
            "WHERE deleted_flag = 0 GROUP BY device_type ORDER BY count DESC")
    List<Map<String, Object>> countByDeviceType();

    /**
     * 统计设备数量按状态（基于原countByDeviceStatus）
     */
    @Select("SELECT device_status as deviceStatus, COUNT(*) as count FROM t_smart_device " +
            "WHERE deleted_flag = 0 GROUP BY device_status ORDER BY count DESC")
    List<Map<String, Object>> countByDeviceStatus();

    /**
     * 统计设备数量按区域（基于区域管理扩展）
     */
    @Select("SELECT area_id as areaId, COUNT(*) as count FROM t_smart_device " +
            "WHERE deleted_flag = 0 AND area_id IS NOT NULL GROUP BY area_id ORDER BY count DESC")
    List<Map<String, Object>> countByAreaId();

    /**
     * 查询设备健康状态统计（新增，基于设备监控需求）
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN device_status = 'ONLINE' THEN 1 ELSE 0 END) as onlineCount, " +
            "SUM(CASE WHEN device_status = 'OFFLINE' THEN 1 ELSE 0 END) as offlineCount, " +
            "SUM(CASE WHEN device_status = 'FAULT' THEN 1 ELSE 0 END) as faultCount, " +
            "SUM(CASE WHEN device_status = 'MAINTAIN' THEN 1 ELSE 0 END) as maintainCount " +
            "FROM t_smart_device WHERE deleted_flag = 0")
    Map<String, Object> getDeviceHealthStatistics();

    /**
     * 查询制造商分布统计（新增，基于设备管理需求）
     */
    @Select("SELECT manufacturer, COUNT(*) as count FROM t_smart_device " +
            "WHERE deleted_flag = 0 AND manufacturer IS NOT NULL " +
            "GROUP BY manufacturer ORDER BY count DESC")
    List<Map<String, Object>> countByManufacturer();

    /**
     * 查询固件版本分布（新增，基于设备维护需求）
     */
    @Select("SELECT firmware_version, COUNT(*) as count FROM t_smart_device " +
            "WHERE deleted_flag = 0 AND firmware_version IS NOT NULL " +
            "GROUP BY firmware_version ORDER BY count DESC")
    List<Map<String, Object>> countByFirmwareVersion();

    /**
     * 搜索设备（基于设备查询需求）
     */
    @Select("<script>" +
            "SELECT * FROM t_smart_device WHERE deleted_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (device_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR device_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR manufacturer LIKE CONCAT('%', #{keyword}, '%') " +
            "OR device_model LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='deviceType != null and deviceType != \"\"'>" +
            "AND device_type = #{deviceType} " +
            "</if>" +
            "<if test='deviceStatus != null and deviceStatus != \"\"'>" +
            "AND device_status = #{deviceStatus} " +
            "</if>" +
            "<if test='areaId != null'>" +
            "AND area_id = #{areaId} " +
            "</if>" +
            "ORDER BY create_time DESC " +
            "</script>")
    List<DeviceEntity> searchDevices(@Param("keyword") String keyword,
            @Param("deviceType") String deviceType,
            @Param("deviceStatus") String deviceStatus,
            @Param("areaId") Long areaId);

    // 兼容性方法，保持与原有设备DAO的兼容

    /**
     * 根据智能设备ID查找（兼容性方法）
     */
    default DeviceEntity selectBySmartDeviceId(Long smartDeviceId) {
        return this.selectById(smartDeviceId);
    }

    /**
     * 根据门禁设备ID查找（兼容性方法）
     */
    default DeviceEntity selectByAccessDeviceId(Long accessDeviceId) {
        return this.selectById(accessDeviceId);
    }

    /**
     * 根据消费设备ID查找（兼容性方法）
     */
    default DeviceEntity selectByConsumeDeviceId(Long consumeDeviceId) {
        return this.selectById(consumeDeviceId);
    }

    /**
     * 根据考勤设备ID查找（兼容性方法）
     */
    default DeviceEntity selectByAttendanceDeviceId(Long attendanceDeviceId) {
        return this.selectById(attendanceDeviceId);
    }

    /**
     * 根据视频设备ID查找（兼容性方法）
     */
    default DeviceEntity selectByVideoDeviceId(Long videoDeviceId) {
        return this.selectById(videoDeviceId);
    }
}
