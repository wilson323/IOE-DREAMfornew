package net.lab1024.sa.admin.module.system.device.manager;

import net.lab1024.sa.admin.module.system.device.domain.entity.UnifiedDeviceEntity;
import net.lab1024.sa.base.common.domain.ResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 统一设备管理器
 * <p>
 * 严格遵循repowiki四层架构规范：
 * - Manager层负责复杂的业务逻辑封装
 * - 处理跨模块调用和外部系统集成
 * - 事件发布和订阅处理
 * - 缓存管理和性能优化
 * - 设备控制和状态管理的核心逻辑
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface UnifiedDeviceManager {

    /**
     * 获取设备状态统计信息
     *
     * @param deviceType 设备类型（可选）
     * @return 统计信息
     */
    Map<String, Object> getDeviceStatusStatistics(String deviceType);

    /**
     * 获取设备类型统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getDeviceTypeStatistics();

    /**
     * 远程控制设备（核心业务逻辑）
     *
     * @param deviceId 设备ID
     * @param command 控制命令
     * @param params 参数
     * @return 操作结果
     */
    ResponseDTO<String> remoteControlDevice(Long deviceId, String command, Map<String, Object> params);

    /**
     * 获取设备配置（含缓存）
     *
     * @param deviceId 设备ID
     * @return 设备配置
     */
    ResponseDTO<Map<String, Object>> getDeviceConfig(Long deviceId);

    /**
     * 将配置转换为JSON字符串
     *
     * @param config 配置对象
     * @return JSON字符串
     */
    String convertConfigToJson(Map<String, Object> config);

    /**
     * 从JSON字符串解析配置
     *
     * @param jsonConfig JSON配置字符串
     * @return 配置对象
     */
    Map<String, Object> parseConfigFromJson(String jsonConfig);

    // ========== 门禁设备专用方法 ==========

    /**
     * 远程开门（门禁设备专用）
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> remoteOpenDoor(Long deviceId);

    /**
     * 重启设备（门禁设备专用）
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> restartDevice(Long deviceId);

    /**
     * 同步设备时间（门禁设备专用）
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<String> syncDeviceTime(Long deviceId);

    // ========== 视频设备专用方法 ==========

    /**
     * 云台控制（视频设备专用）
     *
     * @param deviceId 设备ID
     * @param command 控制命令
     * @param speed 控制速度
     * @return 控制结果
     */
    ResponseDTO<String> ptzControl(Long deviceId, String command, Integer speed);

    /**
     * 启动设备录像（视频设备专用）
     *
     * @param deviceId 设备ID
     * @return 录像ID
     */
    ResponseDTO<Long> startRecording(Long deviceId);

    /**
     * 停止设备录像（视频设备专用）
     *
     * @param recordId 录像ID
     * @return 操作结果
     */
    ResponseDTO<String> stopRecording(Long recordId);

    /**
     * 获取实时视频流（视频设备专用）
     *
     * @param deviceId 设备ID
     * @return 视频流地址
     */
    ResponseDTO<String> getLiveStream(Long deviceId);

    // ========== 高级功能方法 ==========

    /**
     * 获取设备健康状态（含性能指标）
     *
     * @param deviceId 设备ID
     * @return 健康状态
     */
    ResponseDTO<Map<String, Object>> getDeviceHealthStatus(Long deviceId);

    /**
     * 获取需要维护的设备列表
     *
     * @param deviceType 设备类型（可选）
     * @return 需要维护的设备列表
     */
    List<UnifiedDeviceEntity> getDevicesNeedingMaintenance(String deviceType);

    /**
     * 获取心跳超时的设备列表
     *
     * @param deviceType 设备类型（可选）
     * @return 心跳超时设备列表
     */
    List<UnifiedDeviceEntity> getHeartbeatTimeoutDevices(String deviceType);

    /**
     * 检查设备是否需要维护
     *
     * @param device 设备信息
     * @return 是否需要维护
     */
    boolean isDeviceNeedingMaintenance(UnifiedDeviceEntity device);

    /**
     * 检查设备是否心跳超时
     *
     * @param device 设备信息
     * @return 是否心跳超时
     */
    boolean isDeviceHeartbeatTimeout(UnifiedDeviceEntity device);

    // ========== 事件驱动架构支持 ==========

    /**
     * 发布设备状态变更事件
     *
     * @param deviceId 设备ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    void publishDeviceStatusChangeEvent(Long deviceId, String oldStatus, String newStatus);

    /**
     * 发布设备配置变更事件
     *
     * @param deviceId 设备ID
     * @param config 配置信息
     */
    void publishDeviceConfigChangeEvent(Long deviceId, Map<String, Object> config);

    /**
     * 发布设备心跳事件
     *
     * @param deviceId 设备ID
     * @param heartbeatData 心跳数据
     */
    void publishDeviceHeartbeatEvent(Long deviceId, Map<String, Object> heartbeatData);

    /**
     * 发布设备故障事件
     *
     * @param deviceId 设备ID
     * @param faultInfo 故障信息
     */
    void publishDeviceFaultEvent(Long deviceId, Map<String, Object> faultInfo);

    // ========== 缓存管理 ==========

    /**
     * 清除设备缓存
     *
     * @param deviceId 设备ID
     */
    void clearDeviceCache(Long deviceId);

    /**
     * 清除所有设备缓存
     */
    void clearAllDeviceCache();

    /**
     * 预热设备缓存
     *
     * @param deviceIds 设备ID列表
     */
    void warmUpDeviceCache(List<Long> deviceIds);

    // ========== 外部系统集成 ==========

    /**
     * 与设备通信（通用方法）
     *
     * @param deviceId 设备ID
     * @param request 请求数据
     * @return 响应数据
     */
    Map<String, Object> communicateWithDevice(Long deviceId, Map<String, Object> request);

    /**
     * 批量设备通信
     *
     * @param deviceIds 设备ID列表
     * @param request 请求数据
     * @return 响应结果列表
     */
    List<Map<String, Object>> batchCommunicateWithDevices(List<Long> deviceIds, Map<String, Object> request);

    /**
     * 获取设备通信日志
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 通信日志列表
     */
    List<Map<String, Object>> getDeviceCommunicationLogs(Long deviceId, String startTime, String endTime);
}