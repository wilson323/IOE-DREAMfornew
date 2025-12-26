package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 统一设备数据访问接口
 * <p>
 * 严格遵循DAO架构规范：
 * - 统一DAO模式，使用Dao命名
 * - 使用@Mapper注解，禁止使用@Repository
 * - 查询方法使用@Transactional(readOnly = true)
 * - 继承BaseMapper使用MyBatis-Plus
 * - 职责单一：只负责设备数据访问
 * </p>
 * <p>
 * 数据库表：t_common_device
 * 支持所有设备类型的统一管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface DeviceDao extends BaseMapper<DeviceEntity> {

    /**
     * 根据设备编码查询设备
     *
     * @param deviceCode 设备编码
     * @return 设备实体，如果不存在则返回null
     */
    @Transactional(readOnly = true)
    DeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据序列号查询设备
     *
     * @param serialNumber 序列号
     * @return 设备实体，如果不存在则返回null
     */
    @Transactional(readOnly = true)
    DeviceEntity selectBySerialNumber(@Param("serialNumber") String serialNumber);

    /**
     * 根据设备类型查询设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectByDeviceType(@Param("deviceType") Integer deviceType);

    /**
     * 根据区域ID查询设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据区域ID和设备类型查询设备列表
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectByAreaIdAndDeviceType(@Param("areaId") Long areaId, @Param("deviceType") Integer deviceType);

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
     * 根据设备状态查询设备列表
     *
     * @param deviceStatus 设备状态
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    List<DeviceEntity> selectByDeviceStatus(@Param("deviceStatus") Integer deviceStatus);

    /**
     * 根据IP地址查询设备
     *
     * @param ipAddress IP地址
     * @return 设备实体，如果不存在则返回null
     */
    @Transactional(readOnly = true)
    DeviceEntity selectByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 统计指定类型的设备数量
     *
     * @param deviceType 设备类型
     * @return 设备数量
     */
    @Transactional(readOnly = true)
    Long countByDeviceType(@Param("deviceType") Integer deviceType);

    /**
     * 统计指定区域的设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    @Transactional(readOnly = true)
    Long countByAreaId(@Param("areaId") Long areaId);

    /**
     * 统计在线设备数量
     *
     * @return 在线设备数量
     */
    @Transactional(readOnly = true)
    Long countOnlineDevices();

    /**
     * 统计离线设备数量
     *
     * @return 离线设备数量
     */
    @Transactional(readOnly = true)
    Long countOfflineDevices();

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param deviceStatus 设备状态
     * @return 更新行数
     */
    @Transactional(rollbackFor = Exception.class)
    int updateDeviceStatus(@Param("deviceId") Long deviceId, @Param("deviceStatus") Integer deviceStatus);

    /**
     * 更新设备最后在线时间
     *
     * @param deviceId 设备ID
     * @return 更新行数
     */
    @Transactional(rollbackFor = Exception.class)
    int updateLastOnlineTime(@Param("deviceId") Long deviceId);

    /**
     * 更新设备最后离线时间
     *
     * @param deviceId 设备ID
     * @return 更新行数
     */
    @Transactional(rollbackFor = Exception.class)
    int updateLastOfflineTime(@Param("deviceId") Long deviceId);
}

