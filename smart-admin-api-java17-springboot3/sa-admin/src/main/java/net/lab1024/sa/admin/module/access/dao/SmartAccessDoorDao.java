package net.lab1024.sa.admin.module.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.admin.module.access.domain.entity.SmartAccessDoorEntity;

/**
 * 门禁门DAO
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责门禁门数据访问
 * - 提供缓存层需要的查询方法
 * - 关联基础设备和区域表查询
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Mapper
public interface SmartAccessDoorDao extends BaseMapper<SmartAccessDoorEntity> {

    /**
     * 根据门ID查询详细信息
     * 关联查询基础设备信息和区域信息，确保数据一致性
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.door_id = #{doorId} AND sd.deleted_flag = 0 AND a.deleted_flag = 0")
    SmartAccessDoorEntity selectByIdWithDetails(@Param("doorId") Long doorId);

    /**
     * 根据设备ID查询关联的门列表
     * 关联基础设备表和区域表，查询指定设备下的所有门
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.device_id = #{deviceId} AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartAccessDoorEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据区域ID查询关联的门列表
     * 关联基础设备表和区域表，查询指定区域下的所有门
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.area_id = #{areaId} AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartAccessDoorEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询指定门类型的门列表
     * 关联基础设备表和区域表，按门类型过滤
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.door_type = #{doorType} AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartAccessDoorEntity> selectByDoorType(@Param("doorType") String doorType);

    /**
     * 查询指定门方向的门列表
     * 关联基础设备表和区域表，按门方向过滤
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.door_direction = #{doorDirection} AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartAccessDoorEntity> selectByDoorDirection(@Param("doorDirection") String doorDirection);

    /**
     * 查询当前打开的门列表
     * 关联基础设备表和区域表，过滤当前状态为打开的门
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.door_status = 'OPEN' AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.last_open_time DESC")
    List<SmartAccessDoorEntity> selectOpenDoors();

    /**
     * 查询锁定的门列表
     * 关联基础设备表和区域表，过滤锁定状态的门
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.lock_status = 'LOCKED' AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartAccessDoorEntity> selectLockedDoors();

    /**
     * 查询故障的门列表
     * 关联基础设备表和区域表，过滤故障状态的门
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.door_status = 'FAULT' OR d.lock_status = 'ERROR' OR d.door_sensor_status = 'FAULT' " +
            "AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartAccessDoorEntity> selectFaultDoors();

    /**
     * 查询支持远程开门的门列表
     * 关联基础设备表和区域表，过滤支持远程开门的门
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.remote_open_enabled = 1 AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartAccessDoorEntity> selectRemoteOpenEnabledDoors();

    /**
     * 查询支持胁迫开门的门列表
     * 关联基础设备表和区域表，过滤支持胁迫开门的门
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.duress_open_enabled = 1 AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartAccessDoorEntity> selectDuressOpenEnabledDoors();

    /**
     * 查询启用反潜保护的门列表
     * 关联基础设备表和区域表，过滤启用反潜保护的门
     */
    @Select("SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.anti_passback_enabled = 1 AND sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "ORDER BY d.create_time ASC")
    List<SmartAccessDoorEntity> selectAntiPassbackDoors();

    /**
     * 统计门数量
     * 关联基础设备表和区域表，统计有效门的数量
     */
    @Select("SELECT COUNT(*) FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE sd.deleted_flag = 0 AND a.deleted_flag = 0")
    Long countTotalDoors();

    /**
     * 统计当前打开的门数量
     * 关联基础设备表和区域表，统计当前打开的门数量
     */
    @Select("SELECT COUNT(*) FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE d.door_status = 'OPEN' AND sd.deleted_flag = 0 AND a.deleted_flag = 0")
    Long countOpenDoors();

    /**
     * 统计故障的门数量
     * 关联基础设备表和区域表，统计故障门的数量
     */
    @Select("SELECT COUNT(*) FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE (d.door_status = 'FAULT' OR d.lock_status = 'ERROR' OR d.door_sensor_status = 'FAULT') " +
            "AND sd.deleted_flag = 0 AND a.deleted_flag = 0")
    Long countFaultDoors();

    /**
     * 根据门类型统计门数量
     * 关联基础设备表和区域表，按门类型分组统计
     */
    @Select("SELECT d.door_type, COUNT(*) as count " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "GROUP BY d.door_type ORDER BY count DESC")
    List<Object> countDoorsByType();

    /**
     * 根据开门方式统计门数量
     * 关联基础设备表和区域表，按开门方式分组统计
     */
    @Select("SELECT d.open_method, COUNT(*) as count " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "GROUP BY d.open_method ORDER BY count DESC")
    List<Object> countDoorsByOpenMethod();

    /**
     * 批量查询多个设备的门列表
     * 性能优化：使用IN查询避免N+1问题
     *
     * @param deviceIds 设备ID列表
     * @return 门列表
     */
    @Select("<script>" +
            "SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "AND d.device_id IN " +
            "<foreach collection='deviceIds' item='deviceId' open='(' separator=',' close=')'>" +
            "#{deviceId}" +
            "</foreach>" +
            " ORDER BY d.create_time ASC" +
            "</script>")
    List<SmartAccessDoorEntity> selectByDeviceIds(@Param("deviceIds") List<Long> deviceIds);

    /**
     * 批量查询多个区域的门列表
     * 性能优化：使用IN查询避免N+1问题
     *
     * @param areaIds 区域ID列表
     * @return 门列表
     */
    @Select("<script>" +
            "SELECT d.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "a.area_name as area_name, a.area_code as area_code, a.area_type as area_type " +
            "FROM t_smart_access_door d " +
            "INNER JOIN t_smart_device sd ON d.device_id = sd.device_id " +
            "INNER JOIN t_area a ON d.area_id = a.area_id " +
            "WHERE sd.deleted_flag = 0 AND a.deleted_flag = 0 " +
            "AND d.area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            " ORDER BY d.create_time ASC" +
            "</script>")
    List<SmartAccessDoorEntity> selectByAreaIds(@Param("areaIds") List<Long> areaIds);
}