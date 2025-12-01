package net.lab1024.sa.admin.module.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.admin.module.access.domain.entity.SmartDeviceAccessExtensionEntity;

/**
 * 门禁设备扩展DAO
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责设备扩展数据访问
 * - 提供缓存层需要的查询方法
 * - 关联基础设备表查询
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Mapper
public interface SmartDeviceAccessExtensionDao extends BaseMapper<SmartDeviceAccessExtensionEntity> {

    /**
     * 根据设备ID查询门禁扩展信息
     * 关联查询基础设备信息，确保数据一致性
     */
    @Select("SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE e.device_id = #{deviceId} AND d.deleted_flag = 0")
    SmartDeviceAccessExtensionEntity selectByDeviceIdWithBaseInfo(@Param("deviceId") Long deviceId);

    /**
     * 查询指定类型的门禁设备列表
     * 关联基础设备表，支持按门禁设备类型过滤
     */
    @Select("SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE e.access_device_type = #{accessDeviceType} AND d.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartDeviceAccessExtensionEntity> selectByAccessDeviceType(@Param("accessDeviceType") String accessDeviceType);

    /**
     * 查询在线的门禁设备列表
     * 关联基础设备表，过滤在线状态的门禁设备
     */
    @Select("SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE d.online_status = 1 AND d.enabled = 1 AND d.deleted_flag = 0 " +
            "ORDER BY e.last_heartbeat_time DESC")
    List<SmartDeviceAccessExtensionEntity> selectOnlineDevices();

    /**
     * 查询离线的门禁设备列表
     * 关联基础设备表，过滤离线状态的门禁设备
     */
    @Select("SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE d.online_status = 0 AND d.enabled = 1 AND d.deleted_flag = 0 " +
            "ORDER BY e.last_heartbeat_time ASC")
    List<SmartDeviceAccessExtensionEntity> selectOfflineDevices();

    /**
     * 查询心跳超时的门禁设备列表
     * 关联基础设备表，检查心跳超时的门禁设备
     */
    @Select("SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE e.last_heartbeat_time < DATE_SUB(NOW(), INTERVAL 120 SECOND) " +
            "AND d.enabled = 1 AND d.deleted_flag = 0 " +
            "ORDER BY e.last_heartbeat_time ASC")
    List<SmartDeviceAccessExtensionEntity> selectHeartbeatTimeoutDevices();

    /**
     * 查询需要维护的门禁设备列表
     * 关联基础设备表，检查需要维护的设备
     */
    @Select("SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE e.lock_status = 'FAULT' OR e.door_sensor_status = 'FAULT' " +
            "AND d.enabled = 1 AND d.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartDeviceAccessExtensionEntity> selectDevicesNeedingMaintenance();

    /**
     * 统计门禁设备数量
     * 关联基础设备表，统计启用状态的门禁设备
     */
    @Select("SELECT COUNT(*) FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE d.deleted_flag = 0")
    Long countTotalAccessDevices();

    /**
     * 统计在线门禁设备数量
     * 关联基础设备表，统计在线状态的门禁设备
     */
    @Select("SELECT COUNT(*) FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE d.online_status = 1 AND d.enabled = 1 AND d.deleted_flag = 0")
    Long countOnlineAccessDevices();

    /**
     * 根据门禁设备类型统计设备数量
     * 关联基础设备表，按设备类型分组统计
     */
    @Select("SELECT e.access_device_type, COUNT(*) as count " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE d.deleted_flag = 0 " +
            "GROUP BY e.access_device_type ORDER BY count DESC")
    List<Object> countAccessDevicesByType();

    /**
     * 查询支持远程开门的设备列表
     * 关联基础设备表，过滤支持远程开门的设备
     */
    @Select("SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE e.remote_open_enabled = 1 AND d.enabled = 1 AND d.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartDeviceAccessExtensionEntity> selectRemoteOpenEnabledDevices();

    /**
     * 查询支持胁迫报警的设备列表
     * 关联基础设备表，过滤支持胁迫报警的设备
     */
    @Select("SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE e.duress_alarm_enabled = 1 AND d.enabled = 1 AND d.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartDeviceAccessExtensionEntity> selectDuressAlarmEnabledDevices();

    /**
     * 根据开门方式查询设备列表
     * 关联基础设备表，按开门方式过滤设备
     */
    @Select("SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE e.open_method = #{openMethod} AND d.enabled = 1 AND d.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartDeviceAccessExtensionEntity> selectByOpenMethod(@Param("openMethod") String openMethod);

    /**
     * 批量查询多个设备的扩展信息
     * 性能优化：使用IN查询避免N+1问题
     *
     * @param deviceIds 设备ID列表
     * @return 设备扩展信息列表
     */
    @Select("<script>" +
            "SELECT e.*, d.device_name, d.device_code, d.device_type, d.status as device_status " +
            "FROM t_smart_device_access_extension e " +
            "INNER JOIN t_smart_device d ON e.device_id = d.device_id " +
            "WHERE d.deleted_flag = 0 " +
            "AND e.device_id IN " +
            "<foreach collection='deviceIds' item='deviceId' open='(' separator=',' close=')'>" +
            "#{deviceId}" +
            "</foreach>" +
            " ORDER BY d.create_time ASC" +
            "</script>")
    List<SmartDeviceAccessExtensionEntity> selectByDeviceIds(@Param("deviceIds") List<Long> deviceIds);
}