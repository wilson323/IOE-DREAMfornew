package net.lab1024.sa.device.comm.service;

import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.device.comm.monitor.HighPrecisionDeviceMonitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 高精度设备监控服务接口
 * <p>
 * 提供亚秒级精度的设备状态监控服务：
 * 1. 实时设备状态查询和监控
 * 2. 设备状态历史数据管理
 * 3. 设备性能统计分析
 * 4. 批量设备状态监控
 * 5. 监控配置动态管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Schema(description = "高精度设备监控服务接口")
public interface HighPrecisionDeviceMonitorService {

    /**
     * 获取设备实时状态
     *
     * @param deviceId 设备ID
     * @return 设备状态快照
     */
    HighPrecisionDeviceMonitor.DeviceStatusSnapshot getDeviceRealTimeStatus(String deviceId);

    /**
     * 异步监控设备状态
     *
     * @param deviceId 设备ID
     * @return 异步监控结果
     */
    CompletableFuture<HighPrecisionDeviceMonitor.DeviceStatusSnapshot> monitorDeviceAsync(String deviceId);

    /**
     * 获取设备状态历史
     *
     * @param deviceId 设备ID
     * @param count    获取数量
     * @return 历史状态列表
     */
    List<HighPrecisionDeviceMonitor.DeviceStatusSnapshot> getDeviceStatusHistory(String deviceId, int count);

    /**
     * 获取设备性能统计
     *
     * @return 性能统计信息
     */
    Map<String, Object> getPerformanceStatistics();

    /**
     * 批量监控设备
     *
     * @param deviceIds 设备ID列表
     * @return 监控结果映射
     */
    Map<String, HighPrecisionDeviceMonitor.DeviceStatusSnapshot> batchMonitorDevices(List<String> deviceIds);

    /**
     * 更新设备监控配置
     *
     * @param deviceId 设备ID
     * @param config   监控配置
     */
    void updateDeviceMonitorConfig(String deviceId, HighPrecisionDeviceMonitor.DeviceMonitorConfig config);

    /**
     * 获取所有设备状态概览
     *
     * @return 设备状态概览信息
     */
    Map<String, Object> getDeviceStatusOverview();

    /**
     * 获取需要关注的设备列表
     *
     * @return 问题设备列表
     */
    List<String> getProblematicDevices();

    /**
     * 启动高精度监控
     *
     * @param deviceIds 设备ID列表
     */
    void startHighPrecisionMonitoring(List<String> deviceIds);

    /**
     * 停止高精度监控
     *
     * @param deviceIds 设备ID列表
     */
    void stopHighPrecisionMonitoring(List<String> deviceIds);
}