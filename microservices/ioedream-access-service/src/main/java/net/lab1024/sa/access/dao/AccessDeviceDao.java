package net.lab1024.sa.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 门禁设备DAO
 * <p>
 * 严格遵循DAO架构规范：
 * - 统一DAO模式，使用Dao命名
 * - 使用@Mapper注解，禁止使用@Repository
 * - 查询方法使用@Transactional(readOnly = true)
 * - 继承BaseMapper使用MyBatis-Plus
 * - 职责单一：只负责门禁设备数据访问
 * - 使用公共DeviceEntity代替AccessDeviceEntity
 * </p>
 * <p>
 * 数据库表：t_common_device
 * 设备类型：ACCESS（门禁设备）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AccessDeviceDao extends BaseMapper<DeviceEntity> {

    /**
     * 查询指定区域的门禁设备列表
     * <p>
     * 使用公共设备表：t_common_device
     * </p>
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE area_id = #{areaId} AND device_type = 'ACCESS' AND deleted_flag = 0 ORDER BY create_time ASC")
    List<DeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询在线门禁设备列表
     *
     * @return 在线设备列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_type = 'ACCESS' AND device_status = 1 AND enabled = 1 AND deleted_flag = 0 ORDER BY last_online_time DESC")
    List<DeviceEntity> selectOnlineDevices();

    /**
     * 查询离线门禁设备列表
     *
     * @return 离线设备列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_type = 'ACCESS' AND device_status = 2 AND enabled = 1 AND deleted_flag = 0 ORDER BY last_online_time ASC")
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
     * 根据设备编码查询设备
     * <p>
     * 用于根据设备编码查找设备信息
     * </p>
     *
     * @param deviceCode 设备编码
     * @return 设备实体，如果不存在则返回null
     */
    @Select("SELECT * FROM t_common_device WHERE device_code = #{deviceCode} AND device_type = 'ACCESS' AND deleted_flag = 0 LIMIT 1")
    @Transactional(readOnly = true)
    DeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 统计门禁设备数量
     *
     * @return 设备总数
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_common_device WHERE device_type = 'ACCESS' AND deleted_flag = 0")
    Long countTotalDevices();

    /**
     * 统计在线门禁设备数量
     *
     * @return 在线设备数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_common_device WHERE device_type = 'ACCESS' AND device_status = 1 AND deleted_flag = 0")
    Long countOnlineDevices();

    /**
     * 统计指定区域的门禁设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_common_device WHERE area_id = #{areaId} AND device_type = 'ACCESS' AND deleted_flag = 0")
    Long countDevicesByArea(@Param("areaId") Long areaId);
}
