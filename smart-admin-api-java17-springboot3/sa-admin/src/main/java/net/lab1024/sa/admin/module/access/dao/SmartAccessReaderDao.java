package net.lab1024.sa.admin.module.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.admin.module.access.domain.entity.SmartAccessReaderEntity;

/**
 * 门禁读头DAO
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责门禁读头数据访问
 * - 提供缓存层需要的查询方法
 * - 关联基础设备和门表查询
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Mapper
public interface SmartAccessReaderDao extends BaseMapper<SmartAccessReaderEntity> {

    /**
     * 根据读头ID查询详细信息
     * 关联查询基础设备信息和门信息，确保数据一致性
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.reader_id = #{readerId} AND sd.deleted_flag = 0")
    SmartAccessReaderEntity selectByIdWithDetails(@Param("readerId") Long readerId);

    /**
     * 根据设备ID查询关联的读头列表
     * 关联基础设备表和门表，查询指定设备下的所有读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.device_id = #{deviceId} AND sd.deleted_flag = 0 " +
            "ORDER BY r.create_time ASC")
    List<SmartAccessReaderEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据门ID查询关联的读头列表
     * 关联基础设备表和门表，查询指定门下的所有读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.door_id = #{doorId} AND sd.deleted_flag = 0 " +
            "ORDER BY r.create_time ASC")
    List<SmartAccessReaderEntity> selectByDoorId(@Param("doorId") Long doorId);

    /**
     * 查询指定读头类型的读头列表
     * 关联基础设备表和门表，按读头类型过滤
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.reader_type = #{readerType} AND sd.deleted_flag = 0 " +
            "ORDER BY r.create_time ASC")
    List<SmartAccessReaderEntity> selectByReaderType(@Param("readerType") String readerType);

    /**
     * 查询指定读头品牌的读头列表
     * 关联基础设备表和门表，按读头品牌过滤
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.reader_brand = #{readerBrand} AND sd.deleted_flag = 0 " +
            "ORDER BY r.create_time ASC")
    List<SmartAccessReaderEntity> selectByReaderBrand(@Param("readerBrand") String readerBrand);

    /**
     * 查询指定通信协议的读头列表
     * 关联基础设备表和门表，按通信协议过滤
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.communication_protocol = #{protocol} AND sd.deleted_flag = 0 " +
            "ORDER BY r.create_time ASC")
    List<SmartAccessReaderEntity> selectByCommunicationProtocol(@Param("protocol") String protocol);

    /**
     * 查询在线的读头列表
     * 关联基础设备表和门表，过滤在线状态的读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.reader_status = 'ONLINE' AND sd.deleted_flag = 0 " +
            "ORDER BY r.last_read_time DESC")
    List<SmartAccessReaderEntity> selectOnlineReaders();

    /**
     * 查询离线的读头列表
     * 关联基础设备表和门表，过滤离线状态的读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.reader_status = 'OFFLINE' AND sd.deleted_flag = 0 " +
            "ORDER BY r.last_read_time ASC")
    List<SmartAccessReaderEntity> selectOfflineReaders();

    /**
     * 查询故障的读头列表
     * 关联基础设备表和门表，过滤故障状态的读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.reader_status = 'FAULT' OR r.maintenance_status = 'MALFUNCTION' " +
            "AND sd.deleted_flag = 0 " +
            "ORDER BY r.create_time ASC")
    List<SmartAccessReaderEntity> selectFaultReaders();

    /**
     * 查询支持多卡检测的读头列表
     * 关联基础设备表和门表，过滤支持多卡检测的读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.multi_card_detection = 1 AND sd.deleted_flag = 0 " +
            "ORDER BY r.create_time ASC")
    List<SmartAccessReaderEntity> selectMultiCardDetectionReaders();

    /**
     * 查询支持防冲突的读头列表
     * 关联基础设备表和门表，过滤支持防冲突的读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.anti_collision = 1 AND sd.deleted_flag = 0 " +
            "ORDER BY r.create_time ASC")
    List<SmartAccessReaderEntity> selectAntiCollisionReaders();

    /**
     * 查询支持生物特征的读头列表
     * 关联基础设备表和门表，过滤支持生物特征的读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.reader_type = 'BIOMETRIC' AND sd.deleted_flag = 0 " +
            "ORDER BY r.create_time ASC")
    List<SmartAccessReaderEntity> selectBiometricReaders();

    /**
     * 统计读头数量
     * 关联基础设备表和门表，统计有效读头的数量
     */
    @Select("SELECT COUNT(*) FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "WHERE sd.deleted_flag = 0")
    Long countTotalReaders();

    /**
     * 统计在线读头数量
     * 关联基础设备表和门表，统计在线状态的读头数量
     */
    @Select("SELECT COUNT(*) FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "WHERE r.reader_status = 'ONLINE' AND sd.deleted_flag = 0")
    Long countOnlineReaders();

    /**
     * 统计故障读头数量
     * 关联基础设备表和门表，统计故障状态的读头数量
     */
    @Select("SELECT COUNT(*) FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "WHERE (r.reader_status = 'FAULT' OR r.maintenance_status = 'MALFUNCTION') " +
            "AND sd.deleted_flag = 0")
    Long countFaultReaders();

    /**
     * 根据读头类型统计读头数量
     * 关联基础设备表和门表，按读头类型分组统计
     */
    @Select("SELECT r.reader_type, COUNT(*) as count " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "WHERE sd.deleted_flag = 0 " +
            "GROUP BY r.reader_type ORDER BY count DESC")
    List<Object> countReadersByType();

    /**
     * 根据读头品牌统计读头数量
     * 关联基础设备表和门表，按读头品牌分组统计
     */
    @Select("SELECT r.reader_brand, COUNT(*) as count " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "WHERE sd.deleted_flag = 0 " +
            "GROUP BY r.reader_brand ORDER BY count DESC")
    List<Object> countReadersByBrand();

    /**
     * 查询今日有读卡记录的读头列表
     * 关联基础设备表和门表，过滤今日有活动的读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.today_read_count > 0 AND sd.deleted_flag = 0 " +
            "ORDER BY r.today_read_count DESC")
    List<SmartAccessReaderEntity> selectActiveReadersToday();

    /**
     * 查询异常读卡次数较多的读头列表
     * 关联基础设备表和门表，过滤异常读卡次数高的读头
     */
    @Select("SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE r.abnormal_read_count > #{threshold} AND sd.deleted_flag = 0 " +
            "ORDER BY r.abnormal_read_count DESC")
    List<SmartAccessReaderEntity> selectHighAbnormalReadCountReaders(@Param("threshold") Integer threshold);

    /**
     * 批量查询多个设备的读头列表
     * 性能优化：使用IN查询避免N+1问题
     *
     * @param deviceIds 设备ID列表
     * @return 读头列表
     */
    @Select("<script>" +
            "SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE sd.deleted_flag = 0 " +
            "AND r.device_id IN " +
            "<foreach collection='deviceIds' item='deviceId' open='(' separator=',' close=')'>" +
            "#{deviceId}" +
            "</foreach>" +
            " ORDER BY r.create_time ASC" +
            "</script>")
    List<SmartAccessReaderEntity> selectByDeviceIds(@Param("deviceIds") List<Long> deviceIds);

    /**
     * 批量查询多个门的读头列表
     * 性能优化：使用IN查询避免N+1问题
     *
     * @param doorIds 门ID列表
     * @return 读头列表
     */
    @Select("<script>" +
            "SELECT r.*, " +
            "sd.device_name as device_name, sd.device_code as device_code, sd.device_type as device_type, " +
            "d.door_name as door_name, d.door_number as door_number, d.door_type as door_type " +
            "FROM t_smart_access_reader r " +
            "INNER JOIN t_smart_device sd ON r.device_id = sd.device_id " +
            "INNER JOIN t_smart_access_door d ON r.door_id = d.door_id " +
            "WHERE sd.deleted_flag = 0 " +
            "AND r.door_id IN " +
            "<foreach collection='doorIds' item='doorId' open='(' separator=',' close=')'>" +
            "#{doorId}" +
            "</foreach>" +
            " ORDER BY r.create_time ASC" +
            "</script>")
    List<SmartAccessReaderEntity> selectByDoorIds(@Param("doorIds") List<Long> doorIds);
}