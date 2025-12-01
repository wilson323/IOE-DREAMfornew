package net.lab1024.sa.admin.module.system.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.system.device.domain.entity.UnifiedDeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统一设备DAO接口
 * <p>
 * 严格遵循repowiki四层架构规范：
 * - DAO层负责数据访问和SQL操作
 * - 禁止在DAO层编写业务逻辑
 * - 使用MyBatis Plus简化数据库操作
 * - 复杂查询使用XML映射文件
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface UnifiedDeviceDao extends BaseMapper<UnifiedDeviceEntity> {

    /**
     * 根据状态统计设备数量
     *
     * @param deviceType 设备类型（可选）
     * @param onlineStatus 在线状态（可选）
     * @param deviceStatus 设备状态（可选）
     * @return 设备数量
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "<if test='deviceType != null and deviceType != \"\"'>",
        "  AND device_type = #{deviceType}",
        "</if>",
        "<if test='onlineStatus != null'>",
        "  AND online_status = #{onlineStatus}",
        "</if>",
        "<if test='deviceStatus != null and deviceStatus != \"\"'>",
        "  AND device_status = #{deviceStatus}",
        "</if>",
        "</script>"
    })
    long countByStatus(@Param("deviceType") String deviceType,
                      @Param("onlineStatus") Integer onlineStatus,
                      @Param("deviceStatus") String deviceStatus);

    /**
     * 根据设备类型统计设备数量
     *
     * @param deviceType 设备类型
     * @return 设备数量
     */
    @Select({
        "SELECT COUNT(*) FROM t_unified_device",
        "WHERE deleted_flag = 0 AND device_type = #{deviceType}"
    })
    long countByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 查询需要维护的设备列表
     *
     * @param deviceType 设备类型（可选）
     * @return 需要维护的设备列表
     */
    @Select({
        "<script>",
        "SELECT * FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "  AND maintenance_cycle > 0",
        "  AND enabled = 1",
        "  AND (",
        "    (last_maintenance_time IS NULL AND install_time IS NOT NULL AND DATE_ADD(install_time, INTERVAL maintenance_cycle DAY) &lt; NOW())",
        "    OR (last_maintenance_time IS NOT NULL AND DATE_ADD(last_maintenance_time, INTERVAL maintenance_cycle DAY) &lt; NOW())",
        "  )",
        "<if test='deviceType != null and deviceType != \"\"'>",
        "  AND device_type = #{deviceType}",
        "</if>",
        "ORDER BY last_maintenance_time ASC, install_time ASC",
        "</script>"
    })
    List<UnifiedDeviceEntity> selectDevicesNeedingMaintenance(@Param("deviceType") String deviceType);

    /**
     * 查询心跳超时的设备列表
     *
     * @param deviceType 设备类型（可选）
     * @return 心跳超时的设备列表
     */
    @Select({
        "<script>",
        "SELECT * FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "  AND heartbeat_interval > 0",
        "  AND enabled = 1",
        "  AND (",
        "    last_heartbeat_time IS NULL",
        "    OR DATE_ADD(last_heartbeat_time, INTERVAL (heartbeat_interval * 3) SECOND) &lt; NOW()",
        "  )",
        "<if test='deviceType != null and deviceType != \"\"'>",
        "  AND device_type = #{deviceType}",
        "</if>",
        "ORDER BY last_heartbeat_time ASC",
        "</script>"
    })
    List<UnifiedDeviceEntity> selectHeartbeatTimeoutDevices(@Param("deviceType") String deviceType);

    /**
     * 根据设备编号查询设备
     *
     * @param deviceCode 设备编号
     * @return 设备信息
     */
    @Select({
        "SELECT * FROM t_unified_device",
        "WHERE deleted_flag = 0 AND device_code = #{deviceCode}"
    })
    UnifiedDeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据MAC地址查询设备
     *
     * @param macAddress MAC地址
     * @return 设备信息
     */
    @Select({
        "SELECT * FROM t_unified_device",
        "WHERE deleted_flag = 0 AND mac_address = #{macAddress}"
    })
    UnifiedDeviceEntity selectByMacAddress(@Param("macAddress") String macAddress);

    /**
     * 根据IP地址和端口查询设备
     *
     * @param ipAddress IP地址
     * @param port 端口号
     * @return 设备信息
     */
    @Select({
        "SELECT * FROM t_unified_device",
        "WHERE deleted_flag = 0 AND ip_address = #{ipAddress} AND port = #{port}"
    })
    UnifiedDeviceEntity selectByIpAddressAndPort(@Param("ipAddress") String ipAddress, @Param("port") Integer port);

    /**
     * 根据区域查询设备列表
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型（可选）
     * @return 设备列表
     */
    @Select({
        "<script>",
        "SELECT * FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "  AND area_id = #{areaId}",
        "  AND enabled = 1",
        "<if test='deviceType != null and deviceType != \"\"'>",
        "  AND device_type = #{deviceType}",
        "</if>",
        "ORDER BY device_name ASC",
        "</script>"
    })
    List<UnifiedDeviceEntity> selectDevicesByAreaId(@Param("areaId") Long areaId, @Param("deviceType") String deviceType);

    /**
     * 查询在线设备列表
     *
     * @param deviceType 设备类型（可选）
     * @param limit 限制数量
     * @return 在线设备列表
     */
    @Select({
        "<script>",
        "SELECT * FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "  AND online_status = 1",
        "  AND enabled = 1",
        "<if test='deviceType != null and deviceType != \"\"'>",
        "  AND device_type = #{deviceType}",
        "</if>",
        "ORDER BY last_heartbeat_time DESC",
        "<if test='limit != null and limit > 0'>",
        "  LIMIT #{limit}",
        "</if>",
        "</script>"
    })
    List<UnifiedDeviceEntity> selectOnlineDevices(@Param("deviceType") String deviceType, @Param("limit") Integer limit);

    /**
     * 查询离线设备列表
     *
     * @param deviceType 设备类型（可选）
     * @param limit 限制数量
     * @return 离线设备列表
     */
    @Select({
        "<script>",
        "SELECT * FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "  AND online_status = 0",
        "  AND enabled = 1",
        "<if test='deviceType != null and deviceType != \"\"'>",
        "  AND device_type = #{deviceType}",
        "</if>",
        "ORDER BY last_heartbeat_time DESC",
        "<if test='limit != null and limit > 0'>",
        "  LIMIT #{limit}",
        "</if>",
        "</script>"
    })
    List<UnifiedDeviceEntity> selectOfflineDevices(@Param("deviceType") String deviceType, @Param("limit") Integer limit);

    /**
     * 查询故障设备列表
     *
     * @param deviceType 设备类型（可选）
     * @param limit 限制数量
     * @return 故障设备列表
     */
    @Select({
        "<script>",
        "SELECT * FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "  AND device_status = 'FAULT'",
        "  AND enabled = 1",
        "<if test='deviceType != null and deviceType != \"\"'>",
        "  AND device_type = #{deviceType}",
        "</if>",
        "ORDER BY update_time DESC",
        "<if test='limit != null and limit > 0'>",
        "  LIMIT #{limit}",
        "</if>",
        "</script>"
    })
    List<UnifiedDeviceEntity> selectFaultDevices(@Param("deviceType") String deviceType, @Param("limit") Integer limit);

    /**
     * 批量更新设备在线状态
     *
     * @param deviceIds 设备ID列表
     * @param onlineStatus 在线状态
     * @return 更新行数
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "  AND device_id IN",
        "  <foreach item='id' collection='deviceIds' open='(' separator=',' close=')'>",
        "    #{id}",
        "  </foreach>",
        "</script>"
    })
    long batchUpdateOnlineStatus(@Param("deviceIds") List<Long> deviceIds, @Param("onlineStatus") Integer onlineStatus);

    /**
     * 批量更新设备状态
     *
     * @param deviceIds 设备ID列表
     * @param deviceStatus 设备状态
     * @return 更新行数
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "  AND device_id IN",
        "  <foreach item='id' collection='deviceIds' open='(' separator=',' close=')'>",
        "    #{id}",
        "  </foreach>",
        "</script>"
    })
    long batchUpdateDeviceStatus(@Param("deviceIds") List<Long> deviceIds, @Param("deviceStatus") String deviceStatus);

    /**
     * 查询设备使用统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    @Select({
        "SELECT",
        "  device_type,",
        "  COUNT(*) as total_count,",
        "  SUM(CASE WHEN online_status = 1 THEN 1 ELSE 0 END) as online_count,",
        "  SUM(CASE WHEN device_status = 'FAULT' THEN 1 ELSE 0 END) as fault_count,",
        "  SUM(CASE WHEN device_status = 'MAINTENANCE' THEN 1 ELSE 0 END) as maintenance_count",
        "FROM t_unified_device",
        "WHERE deleted_flag = 0",
        "  AND create_time >= #{startDate}",
        "  AND create_time &lt;= #{endDate}",
        "GROUP BY device_type",
        "ORDER BY total_count DESC"
    })
    List<Map<String, Object>> selectDeviceUsageStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 查询设备状态变更历史
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 变更历史列表
     */
    @Select({
        "SELECT",
        "  device_id,",
        "  old_status,",
        "  new_status,",
        "  change_time,",
        "  change_reason",
        "FROM t_device_status_history",
        "WHERE device_id = #{deviceId}",
        "ORDER BY change_time DESC",
        "LIMIT #{limit}"
    })
    List<Map<String, Object>> selectDeviceStatusHistory(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 查询设备通信记录
     *
     * @param deviceId 设备ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param limit 限制数量
     * @return 通信记录列表
     */
    @Select({
        "<script>",
        "SELECT",
        "  device_id,",
        "  command,",
        "  request_data,",
        "  response_data,",
        "  communication_status,",
        "  communication_time,",
        "  response_time",
        "FROM t_device_communication_log",
        "WHERE device_id = #{deviceId}",
        "<if test='startDate != null and startDate != \"\"'>",
        "  AND communication_time >= #{startDate}",
        "</if>",
        "<if test='endDate != null and endDate != \"\"'>",
        "  AND communication_time &lt;= #{endDate}",
        "</if>",
        "ORDER BY communication_time DESC",
        "<if test='limit != null and limit > 0'>",
        "  LIMIT #{limit}",
        "</if>",
        "</script>"
    })
    List<Map<String, Object>> selectDeviceCommunicationLogs(@Param("deviceId") Long deviceId,
                                                           @Param("startDate") String startDate,
                                                           @Param("endDate") String endDate,
                                                           @Param("limit") Integer limit);
}