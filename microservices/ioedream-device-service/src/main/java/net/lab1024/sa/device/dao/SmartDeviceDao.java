package net.lab1024.sa.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.device.domain.entity.SmartDeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能设备 DAO 接口 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责智能设备数据访问
 * - 提供缓存层需要的查询方法
 * - 支持批量操作和统计查询
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Mapper
public interface SmartDeviceDao extends BaseMapper<SmartDeviceEntity> {

    /**
     * 根据设备编码查询设备
     *
     * @param deviceCode 设备编码
     * @return 设备实体
     */
    @Select("SELECT * FROM t_smart_device WHERE device_code = #{deviceCode} AND deleted_flag = 0")
    SmartDeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据设备类型查询设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    @Select("SELECT * FROM t_smart_device WHERE device_type = #{deviceType} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<SmartDeviceEntity> selectByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 根据设备状态查询设备列表
     *
     * @param deviceStatus 设备状态
     * @return 设备列表
     */
    @Select("SELECT * FROM t_smart_device WHERE device_status = #{deviceStatus} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<SmartDeviceEntity> selectByDeviceStatus(@Param("deviceStatus") String deviceStatus);

    /**
     * 根据分组ID查询设备列表
     *
     * @param groupId 分组ID
     * @return 设备列表
     */
    @Select("SELECT * FROM t_smart_device WHERE group_id = #{groupId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<SmartDeviceEntity> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询在线设备列表
     *
     * @param minutes 在线时间阈值(分钟)
     * @return 设备列表
     */
    @Select("SELECT * FROM t_smart_device WHERE device_status = 'ONLINE' AND " +
            "last_online_time >= DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE) AND deleted_flag = 0 " +
            "ORDER BY last_online_time DESC")
    List<SmartDeviceEntity> selectOnlineDevices(@Param("minutes") Integer minutes);

    /**
     * 查询离线设备列表
     *
     * @param minutes 离线时间阈值(分钟)
     * @return 设备列表
     */
    @Select("SELECT * FROM t_smart_device WHERE device_status = 'OFFLINE' AND " +
            "(last_online_time IS NULL OR last_online_time <= DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)) AND deleted_flag = 0 " +
            "ORDER BY last_online_time ASC")
    List<SmartDeviceEntity> selectOfflineDevices(@Param("minutes") Integer minutes);

    /**
     * 更新设备状态
     *
     * @param deviceId     设备ID
     * @param deviceStatus 设备状态
     * @return 影响行数
     */
    @Update("UPDATE t_smart_device SET device_status = #{deviceStatus}, update_time = NOW() WHERE device_id = #{deviceId} AND deleted_flag = 0")
    int updateDeviceStatus(@Param("deviceId") Long deviceId, @Param("deviceStatus") String deviceStatus);

    /**
     * 更新设备最后在线时间
     *
     * @param deviceId        设备ID
     * @param lastOnlineTime 最后在线时间
     * @return 影响行数
     */
    @Update("UPDATE t_smart_device SET last_online_time = #{lastOnlineTime}, device_status = 'ONLINE', update_time = NOW() WHERE device_id = #{deviceId} AND deleted_flag = 0")
    int updateLastOnlineTime(@Param("deviceId") Long deviceId, @Param("lastOnlineTime") LocalDateTime lastOnlineTime);

    /**
     * 批量更新设备状态
     *
     * @param deviceIds    设备ID列表
     * @param deviceStatus 设备状态
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE t_smart_device SET device_status = #{deviceStatus}, update_time = NOW() " +
            "WHERE device_id IN " +
            "<foreach collection='deviceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    int batchUpdateDeviceStatus(@Param("deviceIds") List<Long> deviceIds, @Param("deviceStatus") String deviceStatus);

    /**
     * 统计设备数量按类型
     *
     * @return 统计结果
     */
    @Select("SELECT device_type as type, COUNT(*) as count " +
            "FROM t_smart_device WHERE deleted_flag = 0 " +
            "GROUP BY device_type ORDER BY count DESC")
    List<java.util.Map<String, Object>> countByDeviceType();

    /**
     * 统计设备数量按状态
     *
     * @return 统计结果
     */
    @Select("SELECT device_status as status, COUNT(*) as count " +
            "FROM t_smart_device WHERE deleted_flag = 0 " +
            "GROUP BY device_status ORDER BY count DESC")
    List<java.util.Map<String, Object>> countByDeviceStatus();

    /**
     * 统计总设备数量
     *
     * @return 设备总数
     */
    @Select("SELECT COUNT(*) FROM t_smart_device WHERE deleted_flag = 0")
    Long countTotalDevices();

    /**
     * 统计启用设备数量
     *
     * @return 启用设备数
     */
    @Select("SELECT COUNT(*) FROM t_smart_device WHERE enabled_flag = 1 AND deleted_flag = 0")
    Long countEnabledDevices();

    /**
     * 根据厂商查询设备列表
     *
     * @param manufacturer 厂商名称
     * @return 设备列表
     */
    @Select("SELECT * FROM t_smart_device WHERE manufacturer = #{manufacturer} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<SmartDeviceEntity> selectByManufacturer(@Param("manufacturer") String manufacturer);

    /**
     * 根据IP地址查询设备
     *
     * @param ipAddress IP地址
     * @return 设备实体
     */
    @Select("SELECT * FROM t_smart_device WHERE ip_address = #{ipAddress} AND deleted_flag = 0 LIMIT 1")
    SmartDeviceEntity selectByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 查询需要维护的设备列表
     *
     * @return 设备列表
     */
    @Select("SELECT * FROM t_smart_device WHERE device_status = 'MAINTAIN' AND deleted_flag = 0 ORDER BY update_time DESC")
    List<SmartDeviceEntity> selectMaintenanceDevices();

    /**
     * 查询故障设备列表
     *
     * @return 设备列表
     */
    @Select("SELECT * FROM t_smart_device WHERE device_status = 'FAULT' AND deleted_flag = 0 ORDER BY update_time DESC")
    List<SmartDeviceEntity> selectFaultDevices();

    /**
     * 批量查询设备信息（性能优化）
     *
     * @param deviceIds 设备ID列表
     * @return 设备列表
     */
    @Select("<script>" +
            "SELECT * FROM t_smart_device WHERE deleted_flag = 0 " +
            "AND device_id IN " +
            "<foreach collection='deviceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<SmartDeviceEntity> selectByDeviceIds(@Param("deviceIds") List<Long> deviceIds);
}