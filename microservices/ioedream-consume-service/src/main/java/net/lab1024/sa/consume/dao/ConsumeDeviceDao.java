package net.lab1024.sa.consume.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.lab1024.sa.common.entity.consume.ConsumeDeviceEntity;

/**
 * 消费设备数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Mapper
public interface ConsumeDeviceDao extends BaseMapper<ConsumeDeviceEntity> {

    /**
     * 根据设备编码查询
     *
     * @param deviceCode 设备编码
     * @return 设备信息
     */
    ConsumeDeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据区域ID查询设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    List<ConsumeDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据状态查询设备列表
     *
     * @param deviceStatus 设备状态
     * @return 设备列表
     */
    List<ConsumeDeviceEntity> selectByStatus(@Param("deviceStatus") Integer deviceStatus);

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param deviceStatus 设备状态
     * @return 更新条数
     */
    int updateDeviceStatus(@Param("deviceId") Long deviceId, @Param("deviceStatus") Integer deviceStatus);

    /**
     * 批量更新设备状态
     *
     * @param deviceIds 设备ID列表
     * @param deviceStatus 设备状态
     * @return 更新条数
     */
    int batchUpdateDeviceStatus(@Param("deviceIds") List<Long> deviceIds, @Param("deviceStatus") Integer deviceStatus);

    /**
     * 统计设备数量
     *
     * @return 设备总数
     */
    Long countDevices();

    /**
     * 根据类型统计设备数量
     *
     * @param deviceType 设备类型
     * @return 设备数量
     */
    Long countDevicesByType(@Param("deviceType") Integer deviceType);

    /**
     * 检查设备编码是否存在
     *
     * @param deviceCode 设备编码
     * @param excludeDeviceId 排除的设备ID
     * @return 存在数量
     */
    int checkDeviceCodeExists(@Param("deviceCode") String deviceCode, @Param("excludeDeviceId") Long excludeDeviceId);

    // ==================== 设备管理相关方法 ====================

    /**
     * 统计设备编码数量（用于唯一性检查）
     *
     * @param deviceCode 设备编码
     * @param excludeId 排除的设备ID
     * @return 数量
     */
    int countByDeviceCode(@Param("deviceCode") String deviceCode, @Param("excludeId") Long excludeId);

    /**
     * 统计IP地址数量（用于唯一性检查）
     *
     * @param ipAddress IP地址
     * @param excludeId 排除的设备ID
     * @return 数量
     */
    int countByIpAddress(@Param("ipAddress") String ipAddress, @Param("excludeId") Long excludeId);

    /**
     * 统计MAC地址数量（用于唯一性检查）
     *
     * @param macAddress MAC地址
     * @param excludeId 排除的设备ID
     * @return 数量
     */
    int countByMacAddress(@Param("macAddress") String macAddress, @Param("excludeId") Long excludeId);

    /**
     * 更新最后通信时间
     *
     * @param deviceId 设备ID
     * @param communicationTime 通信时间
     * @return 更新条数
     */
    int updateLastCommunicationTime(@Param("deviceId") Long deviceId, @Param("communicationTime") LocalDateTime communicationTime);

    // ==================== 统计相关方法 ====================

    /**
     * 获取总体统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Map<String, Object> getOverallStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 按设备类型统计
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countByDeviceType();

    /**
     * 按设备状态统计
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countByDeviceStatus();

    /**
     * 按健康状态统计
     *
     * @return 统计结果
     */
    Map<String, Object> countByHealthStatus();

    /**
     * 按离线时长统计
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countByOfflineDuration();

    /**
     * 按通信模式统计
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countByCommunicationPattern();

    /**
     * 按位置统计
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countByLocation();
}