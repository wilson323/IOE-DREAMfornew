package net.lab1024.sa.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.device.domain.entity.PhysicalDeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物理设备数据访问层
 *
 * @author IOE-DREAM Team
 */
@Mapper
public interface PhysicalDeviceDao extends BaseMapper<PhysicalDeviceEntity> {

    /**
     * 根据设备编号查询设备
     *
     * @param deviceCode 设备编号
     * @return 设备实体
     */
    PhysicalDeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 查询在线设备列表
     *
     * @param deviceType 设备类型（可选）
     * @return 在线设备列表
     */
    List<PhysicalDeviceEntity> selectOnlineDevices(@Param("deviceType") String deviceType);

    /**
     * 查询离线设备列表
     *
     * @param deviceType 设备类型（可选）
     * @return 离线设备列表
     */
    List<PhysicalDeviceEntity> selectOfflineDevices(@Param("deviceType") String deviceType);

    /**
     * 更新设备状态
     *
     * @param deviceId     设备ID
     * @param deviceStatus 设备状态
     * @return 影响行数
     */
    int updateDeviceStatus(@Param("deviceId") Long deviceId, @Param("deviceStatus") String deviceStatus);

    /**
     * 更新设备最后心跳时间
     *
     * @param deviceId          设备ID
     * @param lastHeartbeatTime 最后心跳时间
     * @return 影响行数
     */
    int updateLastHeartbeatTime(@Param("deviceId") Long deviceId, @Param("lastHeartbeatTime") LocalDateTime lastHeartbeatTime);

    /**
     * 查询心跳超时的设备
     *
     * @param cutoffTime 截止时间
     * @return 超时设备列表
     */
    List<PhysicalDeviceEntity> selectHeartbeatTimeoutDevices(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 批量更新设备状态
     *
     * @param deviceIds    设备ID列表
     * @param deviceStatus 设备状态
     * @return 影响行数
     */
    int batchUpdateDeviceStatus(@Param("deviceIds") List<Long> deviceIds, @Param("deviceStatus") String deviceStatus);

    /**
     * 统计设备数量按类型
     *
     * @return 设备统计结果
     */
    List<java.util.Map<String, Object>> countDevicesByType();

    /**
     * 统计设备数量按状态
     *
     * @return 设备统计结果
     */
    List<java.util.Map<String, Object>> countDevicesByStatus();
}