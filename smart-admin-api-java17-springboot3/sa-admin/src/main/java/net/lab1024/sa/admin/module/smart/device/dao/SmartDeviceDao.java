package net.lab1024.sa.admin.module.smart.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.smart.device.domain.entity.SmartDeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能设备 DAO 接口
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@Mapper
public interface SmartDeviceDao extends BaseMapper<SmartDeviceEntity> {

    /**
     * 根据设备编码查询设备
     *
     * @param deviceCode 设备编码
     * @return 设备实体
     */
    SmartDeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据设备类型查询设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    List<SmartDeviceEntity> selectByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 根据设备状态查询设备列表
     *
     * @param deviceStatus 设备状态
     * @return 设备列表
     */
    List<SmartDeviceEntity> selectByDeviceStatus(@Param("deviceStatus") String deviceStatus);

    /**
     * 根据分组ID查询设备列表
     *
     * @param groupId 分组ID
     * @return 设备列表
     */
    List<SmartDeviceEntity> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询在线设备列表
     *
     * @param minutes 在线时间阈值(分钟)
     * @return 设备列表
     */
    List<SmartDeviceEntity> selectOnlineDevices(@Param("minutes") Integer minutes);

    /**
     * 查询离线设备列表
     *
     * @param minutes 离线时间阈值(分钟)
     * @return 设备列表
     */
    List<SmartDeviceEntity> selectOfflineDevices(@Param("minutes") Integer minutes);

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param deviceStatus 设备状态
     * @return 影响行数
     */
    int updateDeviceStatus(@Param("deviceId") Long deviceId, @Param("deviceStatus") String deviceStatus);

    /**
     * 更新设备最后在线时间
     *
     * @param deviceId 设备ID
     * @param lastOnlineTime 最后在线时间
     * @return 影响行数
     */
    int updateLastOnlineTime(@Param("deviceId") Long deviceId, @Param("lastOnlineTime") LocalDateTime lastOnlineTime);

    /**
     * 批量更新设备状态
     *
     * @param deviceIds 设备ID列表
     * @param deviceStatus 设备状态
     * @return 影响行数
     */
    int batchUpdateDeviceStatus(@Param("deviceIds") List<Long> deviceIds, @Param("deviceStatus") String deviceStatus);

    /**
     * 统计设备数量按类型
     *
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> countByDeviceType();

    /**
     * 统计设备数量按状态
     *
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> countByDeviceStatus();
}