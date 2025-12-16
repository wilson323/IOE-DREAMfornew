package net.lab1024.sa.common.organization.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 设备DAO
 * <p>
 * 统一设备数据访问接口，用于所有业务模块的设备数据访问
 * 严格遵循DAO架构规范：
 * - 统一DAO模式，使用Dao命名
 * - 使用@Mapper注解，禁止使用@Repository
 * - 查询方法使用@Transactional(readOnly = true)
 * - 继承BaseMapper使用MyBatis-Plus
 * - 职责单一：只负责设备数据访问
 * </p>
 * <p>
 * 数据库表：t_common_device
 * 支持的设备类型：
 * - CAMERA - 摄像头
 * - ACCESS - 门禁设备
 * - CONSUME - 消费机
 * - ATTENDANCE - 考勤机
 * - BIOMETRIC - 生物识别设备
 * - INTERCOM - 对讲机
 * - ALARM - 报警器
 * - SENSOR - 传感器
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface DeviceDao extends BaseMapper<DeviceEntity> {

    /**
     * 查询指定区域的设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询在线设备列表
     *
     * @return 在线设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectOnlineDevices();

    /**
     * 查询离线设备列表
     *
     * @return 离线设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectOfflineDevices();

    /**
     * 查询指定类型的设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 查询需要维护的设备列表
     *
     * @return 需要维护的设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectDevicesNeedingMaintenance();

    /**
     * 查询心跳超时的设备列表
     *
     * @param timeoutMinutes 超时分钟数
     * @return 心跳超时的设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectHeartbeatTimeoutDevices(@Param("timeoutMinutes") Integer timeoutMinutes);

    /**
     * 统计设备数量
     *
     * @return 设备总数
     */
    @Transactional(readOnly = true)
    Long countTotalDevices();

    /**
     * 统计在线设备数量
     *
     * @return 在线设备数量
     */
    @Transactional(readOnly = true)
    Long countOnlineDevices();

    /**
     * 统计指定区域的设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    @Transactional(readOnly = true)
    Long countDevicesByArea(@Param("areaId") Long areaId);

    /**
     * 根据设备类型统计设备数量
     *
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    List<Object> countDevicesByType();

    /**
     * 批量查询多个区域的设备列表（性能优化：使用IN查询避免N+1问题）
     *
     * @param areaIds 区域ID列表
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectByAreaIds(@Param("areaIds") List<Long> areaIds);

    /**
     * 根据设备状态统计设备数量
     *
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    List<Object> countDevicesByStatus();

    /**
     * 根据设备编码查询设备
     * <p>
     * 用于根据设备序列号（SN）或设备编码查找设备ID
     * </p>
     *
     * @param deviceCode 设备编码（设备序列号SN）
     * @return 设备实体，如果不存在则返回null
     */
    @Select("SELECT * FROM t_common_device WHERE device_code = #{deviceCode} AND deleted_flag = 0 LIMIT 1")
    @Transactional(readOnly = true)
    DeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);
}
