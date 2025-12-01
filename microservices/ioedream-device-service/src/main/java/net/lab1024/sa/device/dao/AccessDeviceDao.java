package net.lab1024.sa.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.device.domain.entity.AccessDeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁设备DAO - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责门禁设备数据访问
 * - 提供缓存层需要的查询方法
 * - 支持批量操作和统计查询
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Mapper
public interface AccessDeviceDao extends BaseMapper<AccessDeviceEntity> {

    /**
     * 根据设备编码查询设备
     *
     * @param deviceCode 设备编码
     * @return 设备实体
     */
    @Select("SELECT * FROM t_access_device WHERE device_code = #{deviceCode} AND deleted_flag = 0")
    AccessDeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 查询指定区域的设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE area_id = #{areaId} AND deleted_flag = 0 ORDER BY create_time ASC")
    List<AccessDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询在线设备列表
     *
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE online_status = 1 AND enabled = 1 AND deleted_flag = 0 ORDER BY last_heartbeat_time DESC")
    List<AccessDeviceEntity> selectOnlineDevices();

    /**
     * 查询离线设备列表
     *
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE online_status = 0 AND enabled = 1 AND deleted_flag = 0 ORDER BY last_heartbeat_time ASC")
    List<AccessDeviceEntity> selectOfflineDevices();

    /**
     * 查询需要维护的设备列表
     *
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE work_mode = 2 AND enabled = 1 AND deleted_flag = 0 ORDER BY update_time DESC")
    List<AccessDeviceEntity> selectDevicesNeedingMaintenance();

    /**
     * 查询心跳超时的设备列表
     *
     * @param timeoutMinutes 超时阈值(分钟)
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE " +
            "last_heartbeat_time < DATE_SUB(NOW(), INTERVAL #{timeoutMinutes} MINUTE) " +
            "AND enabled = 1 AND deleted_flag = 0 ORDER BY last_heartbeat_time ASC")
    List<AccessDeviceEntity> selectHeartbeatTimeoutDevices(@Param("timeoutMinutes") Integer timeoutMinutes);

    /**
     * 根据设备类型查询设备列表
     *
     * @param accessDeviceType 门禁设备类型
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE access_device_type = #{accessDeviceType} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AccessDeviceEntity> selectByDeviceType(@Param("accessDeviceType") Integer accessDeviceType);

    /**
     * 根据工作模式查询设备列表
     *
     * @param workMode 工作模式
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE work_mode = #{workMode} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AccessDeviceEntity> selectByWorkMode(@Param("workMode") Integer workMode);

    /**
     * 根据厂商查询设备列表
     *
     * @param manufacturer 厂商名称
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE manufacturer = #{manufacturer} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AccessDeviceEntity> selectByManufacturer(@Param("manufacturer") String manufacturer);

    /**
     * 根据IP地址查询设备
     *
     * @param ipAddress IP地址
     * @return 设备实体
     */
    @Select("SELECT * FROM t_access_device WHERE ip_address = #{ipAddress} AND deleted_flag = 0 LIMIT 1")
    AccessDeviceEntity selectByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 批量更新设备在线状态
     *
     * @param deviceIds 设备ID列表
     * @param status    在线状态
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE t_access_device SET online_status = #{status}, update_time = NOW() " +
            "WHERE access_device_id IN " +
            "<foreach collection='deviceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    int batchUpdateOnlineStatus(@Param("deviceIds") List<Long> deviceIds, @Param("status") Integer status);

    /**
     * 批量更新设备工作模式
     *
     * @param deviceIds 设备ID列表
     * @param workMode  工作模式
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE t_access_device SET work_mode = #{workMode}, update_time = NOW() " +
            "WHERE access_device_id IN " +
            "<foreach collection='deviceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    int batchUpdateWorkMode(@Param("deviceIds") List<Long> deviceIds, @Param("workMode") Integer workMode);

    /**
     * 更新设备心跳时间
     *
     * @param accessDeviceId   设备ID
     * @param heartbeatTime 心跳时间
     * @return 影响行数
     */
    @Update("UPDATE t_access_device SET last_heartbeat_time = #{heartbeatTime}, " +
            "online_status = 1, update_time = NOW() " +
            "WHERE access_device_id = #{accessDeviceId} AND deleted_flag = 0")
    int updateHeartbeatTime(@Param("accessDeviceId") Long accessDeviceId, @Param("heartbeatTime") LocalDateTime heartbeatTime);

    /**
     * 统计总设备数量
     *
     * @return 设备总数
     */
    @Select("SELECT COUNT(*) FROM t_access_device WHERE deleted_flag = 0")
    Long countTotalDevices();

    /**
     * 统计在线设备数量
     *
     * @return 在线设备数
     */
    @Select("SELECT COUNT(*) FROM t_access_device WHERE online_status = 1 AND deleted_flag = 0")
    Long countOnlineDevices();

    /**
     * 统计指定区域的设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    @Select("SELECT COUNT(*) FROM t_access_device WHERE area_id = #{areaId} AND deleted_flag = 0")
    Long countDevicesByArea(@Param("areaId") Long areaId);

    /**
     * 根据设备类型统计设备数量
     *
     * @return 统计结果
     */
    @Select("SELECT access_device_type as type, COUNT(*) as count " +
            "FROM t_access_device WHERE deleted_flag = 0 " +
            "GROUP BY access_device_type ORDER BY count DESC")
    List<java.util.Map<String, Object>> countDevicesByType();

    /**
     * 根据工作模式统计设备数量
     *
     * @return 统计结果
     */
    @Select("SELECT work_mode as mode, COUNT(*) as count " +
            "FROM t_access_device WHERE deleted_flag = 0 " +
            "GROUP BY work_mode ORDER BY count DESC")
    List<java.util.Map<String, Object>> countDevicesByWorkMode();

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

    /**
     * 批量查询设备信息（性能优化）
     *
     * @param deviceIds 设备ID列表
     * @return 设备列表
     */
    @Select("<script>" +
            "SELECT * FROM t_access_device WHERE deleted_flag = 0 " +
            "AND access_device_id IN " +
            "<foreach collection='deviceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<AccessDeviceEntity> selectByDeviceIds(@Param("deviceIds") List<Long> deviceIds);

    /**
     * 统计启用设备数量
     *
     * @return 启用设备数
     */
    @Select("SELECT COUNT(*) FROM t_access_device WHERE enabled = 1 AND deleted_flag = 0")
    Long countEnabledDevices();

    /**
     * 查询紧急模式设备列表
     *
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE work_mode = 3 AND enabled = 1 AND deleted_flag = 0 ORDER BY update_time DESC")
    List<AccessDeviceEntity> selectEmergencyModeDevices();

    /**
     * 查询锁闭模式设备列表
     *
     * @return 设备列表
     */
    @Select("SELECT * FROM t_access_device WHERE work_mode = 4 AND enabled = 1 AND deleted_flag = 0 ORDER BY update_time DESC")
    List<AccessDeviceEntity> selectLockModeDevices();
}