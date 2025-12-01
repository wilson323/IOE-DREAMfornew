package net.lab1024.sa.video.manager;

import java.util.List;
import java.util.Map;

/**
 * 视频设备管理器接口
 *
 * <p>Manager层接口定义，负责处理复杂的业务逻辑，包括：</p>
 * <ul>
 *   <li>跨模块调用和第三方服务集成</li>
 *   <li>设备通信和协议适配</li>
 *   <li>实时数据流处理和事件驱动</li>
 *   <li>性能优化和资源管理</li>
 *   <li>复杂的业务流程编排</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
public interface VideoDeviceManagerInterface {

    /**
     * 云台控制
     *
     * @param deviceId 设备ID
     * @param command 控制命令（UP, DOWN, LEFT, RIGHT, ZOOM_IN, ZOOM_OUT）
     * @param speed 控制速度（1-10）
     * @return 控制结果
     * @throws Exception 业务异常
     */
    String ptzControl(Long deviceId, String command, Integer speed) throws Exception;

    /**
     * 获取设备实时状态
     *
     * @param deviceId 设备ID
     * @return 设备状态信息（JSON格式）
     * @throws Exception 业务异常
     */
    String getDeviceStatus(Long deviceId) throws Exception;

    /**
     * 启动设备录像
     *
     * @param deviceId 设备ID
     * @return 录像ID
     * @throws Exception 业务异常
     */
    Long startRecording(Long deviceId) throws Exception;

    /**
     * 停止设备录像
     *
     * @param recordId 录像ID
     * @return 操作结果
     * @throws Exception 业务异常
     */
    String stopRecording(Long recordId) throws Exception;

    
    /**
     * 处理设备监控事件
     *
     * @param deviceId 设备ID
     * @param eventType 事件类型（MOTION_DETECTED, PERSON_DETECTED, ALARM_TRIGGERED, DEVICE_OFFLINE等）
     * @param eventData 事件数据
     * @throws Exception 业务异常
     */
    void handleMonitorEvent(Long deviceId, String eventType, Map<String, Object> eventData) throws Exception;

    /**
     * 健康检查所有设备连接
     */
    void healthCheckAllDevices();
}