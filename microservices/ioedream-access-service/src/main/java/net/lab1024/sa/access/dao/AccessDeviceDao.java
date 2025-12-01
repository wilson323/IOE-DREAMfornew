package net.lab1024.sa.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.access.domain.entity.AccessDeviceEntity;

/**
 * 门禁设备DAO
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责设备数据访问
 * - 提供缓存层需要的查询方法
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface AccessDeviceDao extends BaseMapper<AccessDeviceEntity> {

    /**
     * 查询指定区域的设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE area_id = #{areaId} AND deleted_flag = 0 ORDER BY create_time ASC")
    List<AccessDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询在线设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE online_status = 1 AND enabled = 1 AND deleted_flag = 0 ORDER BY last_heartbeat_time DESC")
    List<AccessDeviceEntity> selectOnlineDevices();

    /**
     * 查询离线设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE online_status = 0 AND enabled = 1 AND deleted_flag = 0 ORDER BY last_comm_time ASC")
    List<AccessDeviceEntity> selectOfflineDevices();

    /**
     * 查询需要维护的设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE next_maintenance_time < NOW() AND enabled = 1 AND deleted_flag = 0 ORDER BY next_maintenance_time ASC")
    List<AccessDeviceEntity> selectDevicesNeedingMaintenance();

    /**
     * 查询心跳超时的设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE " +
            "last_heartbeat_time < DATE_SUB(NOW(), INTERVAL (heartbeat_interval * 2) SECOND) " +
            "AND enabled = 1 AND deleted_flag = 0 ORDER BY last_heartbeat_time ASC")
    List<AccessDeviceEntity> selectHeartbeatTimeoutDevices();

    /**
     * 批量更新设备在线状态
     */
    @Select("<script>" +
            "UPDATE t_access_device SET online_status = #{status}, update_time = NOW() " +
            "WHERE access_device_id IN " +
            "<foreach collection='deviceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateOnlineStatus(@Param("deviceIds") List<Long> deviceIds, @Param("status") Integer status);

    /**
     * 统计设备数量
     */
    @Select("SELECT COUNT(*) FROM t_access_device WHERE deleted_flag = 0")
    Long countTotalDevices();

    /**
     * 统计在线设备数量
     */
    @Select("SELECT COUNT(*) FROM t_access_device WHERE online_status = 1 AND deleted_flag = 0")
    Long countOnlineDevices();

    /**
     * 统计指定区域的设备数量
     */
    @Select("SELECT COUNT(*) FROM t_access_device WHERE area_id = #{areaId} AND deleted_flag = 0")
    Long countDevicesByArea(@Param("areaId") Long areaId);

    /**
     * 根据设备类型统计设备数量
     */
    @Select("SELECT access_device_type, COUNT(*) as count " +
            "FROM t_access_device WHERE deleted_flag = 0 " +
            "GROUP BY access_device_type ORDER BY count DESC")
    List<Object> countDevicesByType();

    /**
     * 批量查询多个区域的设备列表（性能优化：使用IN查询避免N+1问题）
     *
     * @param areaIds 区域ID列表
     * @return 设备列表
     */
    @Select("<script>" +
            "SELECT * FROM t_access_device WHERE deleted_flag = 0 " +
            "AND area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            " ORDER BY create_time ASC" +
            "</script>")
    List<AccessDeviceEntity> selectByAreaIds(@Param("areaIds") List<Long> areaIds);
}