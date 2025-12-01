package net.lab1024.sa.device.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.device.domain.entity.DeviceEntity;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 设备管理服务接口
 * 基于现有SmartDeviceService重构，提供统一设备管理功能
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原SmartDeviceService重构)
 */
public interface DeviceService {

    /**
     * 分页查询设备列表（基于原queryDevicePage）
     */
    PageResult<DeviceEntity> queryDevicePage(PageParam pageParam, String deviceType,
            String deviceStatus, String deviceName, Long areaId);

    /**
     * 根据ID查询设备详情（基于原getDeviceById）
     */
    ResponseDTO<DeviceEntity> getDeviceById(Long deviceId);

    /**
     * 新增设备（基于原addDevice）
     */
    ResponseDTO<String> addDevice(DeviceEntity device);

    /**
     * 更新设备（基于原updateDevice）
     */
    ResponseDTO<String> updateDevice(DeviceEntity device);

    /**
     * 删除设备（基于原deleteDevice）
     */
    ResponseDTO<String> deleteDevice(Long deviceId);

    /**
     * 批量删除设备（基于原batchDeleteDevice）
     */
    ResponseDTO<String> batchDeleteDevice(List<Long> deviceIds);

    /**
     * 更新设备状态（基于原updateDeviceStatus）
     */
    ResponseDTO<String> updateDeviceStatus(Long deviceId, String deviceStatus);

    /**
     * 批量更新设备状态（基于原batchUpdateDeviceStatus）
     */
    ResponseDTO<String> batchUpdateDeviceStatus(List<Long> deviceIds, String deviceStatus);

    /**
     * 根据设备编码查询设备（基于原getDeviceByCode）
     */
    ResponseDTO<DeviceEntity> getDeviceByCode(String deviceCode);

    /**
     * 根据设备类型查询设备列表（基于原getDevicesByType）
     */
    ResponseDTO<List<DeviceEntity>> getDevicesByType(String deviceType);

    /**
     * 根据设备状态查询设备列表（基于原getDevicesByStatus）
     */
    ResponseDTO<List<DeviceEntity>> getDevicesByStatus(String deviceStatus);

    /**
     * 根据区域ID查询设备列表（基于区域管理扩展）
     */
    ResponseDTO<List<DeviceEntity>> getDevicesByAreaId(Long areaId);

    /**
     * 获取在线设备列表（基于原getOnlineDevices）
     */
    ResponseDTO<List<DeviceEntity>> getOnlineDevices();

    /**
     * 获取离线设备列表（基于原getOfflineDevices）
     */
    ResponseDTO<List<DeviceEntity>> getOfflineDevices();

    /**
     * 获取需要维护的设备列表（基于设备维护需求）
     */
    ResponseDTO<List<DeviceEntity>> getMaintenanceRequiredDevices();

    /**
     * 获取心跳超时的设备列表（基于AccessDeviceEntity心跳逻辑）
     */
    ResponseDTO<List<DeviceEntity>> getHeartbeatTimeoutDevices();

    /**
     * 设备状态统计（基于原getDeviceStatusStatistics）
     */
    ResponseDTO<Map<String, Object>> getDeviceStatusStatistics();

    /**
     * 设备类型统计（基于原getDeviceTypeStatistics）
     */
    ResponseDTO<Map<String, Object>> getDeviceTypeStatistics();

    /**
     * 设备健康状态统计（基于设备监控需求）
     */
    ResponseDTO<Map<String, Object>> getDeviceHealthStatistics();

    /**
     * 区域设备统计（基于区域管理扩展）
     */
    ResponseDTO<Map<String, Object>> getAreaDeviceStatistics();

    /**
     * 制造商统计（基于设备管理需求）
     */
    ResponseDTO<Map<String, Object>> getManufacturerStatistics();

    /**
     * 搜索设备（基于设备查询需求）
     */
    ResponseDTO<List<DeviceEntity>> searchDevices(String keyword, String deviceType,
            String deviceStatus, Long areaId);

    /**
     * 设备上线（基于设备状态管理）
     */
    ResponseDTO<String> deviceOnline(Long deviceId);

    /**
     * 设备下线（基于设备状态管理）
     */
    ResponseDTO<String> deviceOffline(Long deviceId);

    /**
     * 更新设备心跳（基于AccessDeviceEntity心跳机制）
     */
    ResponseDTO<String> updateHeartbeat(Long deviceId);

    /**
     * 设备故障报告（基于设备监控需求）
     */
    ResponseDTO<String> reportDeviceFault(Long deviceId, String faultMessage);

    /**
     * 设备恢复报告（基于设备监控需求）
     */
    ResponseDTO<String> reportDeviceRecovery(Long deviceId, String recoveryMessage);

    // 兼容性方法，保持与原有设备服务的兼容

    /**
     * 智能设备兼容接口（兼容性方法）
     */
    ResponseDTO<DeviceEntity> getSmartDeviceById(Long smartDeviceId);

    /**
     * 门禁设备兼容接口（兼容性方法）
     */
    ResponseDTO<DeviceEntity> getAccessDeviceById(Long accessDeviceId);

    /**
     * 消费设备兼容接口（兼容性方法）
     */
    ResponseDTO<DeviceEntity> getConsumeDeviceById(Long consumeDeviceId);

    /**
     * 考勤设备兼容接口（兼容性方法）
     */
    ResponseDTO<DeviceEntity> getAttendanceDeviceById(Long attendanceDeviceId);

    /**
     * 视频设备兼容接口（兼容性方法）
     */
    ResponseDTO<DeviceEntity> getVideoDeviceById(Long videoDeviceId);
}
