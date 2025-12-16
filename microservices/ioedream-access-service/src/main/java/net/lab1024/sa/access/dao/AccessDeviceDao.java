package net.lab1024.sa.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 门禁设备DAO
 * <p>
 * 严格遵循DAO架构规范：
 * - 统一DAO模式，使用Dao命名
 * - 使用@Mapper注解，禁止使用@Mapper
 * - 查询方法使用@Transactional(readOnly = true)
 * - 继承BaseMapper使用MyBatis-Plus
 * - 职责单一：只负责设备数据访问
 * - 提供缓存层需要的查询方法
 * - 使用公共DeviceEntity替代AccessDeviceEntity
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 * @updated 2025-12-02 使用公共DeviceEntity，遵循repowiki规范
 */
@Mapper
public interface AccessDeviceDao extends BaseMapper<DeviceEntity> {

    /**
     * 查询指定区域的设备列表
     * 使用公共设备表：t_common_device
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE area_id = #{areaId} AND deleted_flag = 0 ORDER BY create_time ASC")
    List<DeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询在线设备列表
     *
     * @return 在线设备列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_status = 'ONLINE' AND enabled_flag = 1 AND deleted_flag = 0 ORDER BY last_online_time DESC")
    List<DeviceEntity> selectOnlineDevices();

    /**
     * 查询离线设备列表
     *
     * @return 离线设备列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_status = 'OFFLINE' AND enabled_flag = 1 AND deleted_flag = 0 ORDER BY last_online_time ASC")
    List<DeviceEntity> selectOfflineDevices();

    /**
     * 查询门禁类型设备列表
     *
     * @return 门禁设备列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_type = 'ACCESS' AND deleted_flag = 0 ORDER BY create_time ASC")
    List<DeviceEntity> selectAccessDevices();

    /**
     * 查询需要维护的设备列表
     *
     * @return 需要维护的设备列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_status = 'MAINTAIN' AND enabled_flag = 1 AND deleted_flag = 0 ORDER BY update_time ASC")
    List<DeviceEntity> selectDevicesNeedingMaintenance();

    /**
     * 查询心跳超时的设备列表
     *
     * @return 心跳超时的设备列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE " +
            "last_online_time < DATE_SUB(NOW(), INTERVAL 10 MINUTE) " +
            "AND enabled_flag = 1 AND deleted_flag = 0 ORDER BY last_online_time ASC")
    List<DeviceEntity> selectHeartbeatTimeoutDevices();

    /**
     * 批量更新设备在线状态
     *
     * @param deviceIds 设备ID列表
     * @param status    状态
     * @return 更新行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("<script>" +
            "UPDATE t_common_device SET device_status = #{status}, update_time = NOW() " +
            "WHERE device_id IN " +
            "<foreach collection='deviceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateOnlineStatus(@Param("deviceIds") List<Long> deviceIds, @Param("status") String status);

    /**
     * 统计设备数量
     *
     * @return 设备总数
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_common_device WHERE deleted_flag = 0")
    Long countTotalDevices();

    /**
     * 统计在线设备数量
     *
     * @return 在线设备数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_common_device WHERE device_status = 'ONLINE' AND deleted_flag = 0")
    Long countOnlineDevices();

    /**
     * 统计指定区域的设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_common_device WHERE area_id = #{areaId} AND deleted_flag = 0")
    Long countDevicesByArea(@Param("areaId") Long areaId);

    /**
     * 根据设备类型统计设备数量
     *
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    @Select("SELECT device_type, COUNT(*) as count " +
            "FROM t_common_device WHERE deleted_flag = 0 " +
            "GROUP BY device_type ORDER BY count DESC")
    List<Object> countDevicesByType();

    /**
     * 批量查询多个区域的设备列表（性能优化：使用IN查询避免N+1问题）
     *
     * @param areaIds 区域ID列表
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT * FROM t_common_device WHERE deleted_flag = 0 " +
            "AND area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            " ORDER BY create_time ASC" +
            "</script>")
    List<DeviceEntity> selectByAreaIds(@Param("areaIds") List<Long> areaIds);

    /**
     * 根据设备状态统计设备数量
     *
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    @Select("SELECT device_status, COUNT(*) as count " +
            "FROM t_common_device WHERE deleted_flag = 0 " +
            "GROUP BY device_status ORDER BY count DESC")
    List<Object> countDevicesByStatus();
}

