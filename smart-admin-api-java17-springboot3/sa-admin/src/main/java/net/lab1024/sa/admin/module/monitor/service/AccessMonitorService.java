package net.lab1024.sa.admin.module.monitor.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.admin.module.access.domain.vo.AlertEventVO;
import net.lab1024.sa.admin.module.access.domain.vo.VideoLinkageVO;
import net.lab1024.sa.admin.module.access.domain.vo.PersonTrackingVO;
import net.lab1024.sa.admin.module.access.domain.vo.MonitorStatisticsVO;
import net.lab1024.sa.admin.module.access.domain.vo.AccessDeviceStatusVO;

/**
 * 门禁实时监控服务接口（增强版）
 * <p>
 * 严格遵循repowiki规范：
 * - 接口定义清晰，职责单一
 * - 方法命名遵循RESTful规范
 * - 完整的业务逻辑封装
 * - 实时数据处理和推送
 * - 支持高并发监控（≥1000台设备）
 * - 低延迟响应（≤5秒状态更新）
 * - 视频联动和人员追踪增强功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 * @version 2.0 Enhanced for OpenSpec Task 2.3
 */
public interface AccessMonitorService {

    /**
     * 获取监控仪表板概览
     *
     * @return 仪表板概览数据
     */
    Map<String, Object> getDashboardOverview();

    /**
     * 获取实时统计数据
     *
     * @return 实时统计数据
     */
    Map<String, Object> getRealtimeStatistics();

    /**
     * 获取设备状态统计
     *
     * @return 设备状态统计信息
     */
    Map<String, Object> getDeviceStatistics();

    /**
     * 获取区域访问统计
     *
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param areaId 区域ID（可选）
     * @return 区域访问统计
     */
    Map<String, Object> getAreaStatistics(String startTime, String endTime, Long areaId);

    /**
     * 获取访问趋势数据
     *
     * @param timeRange 时间范围（hour/day/week/month）
     * @param timeSpan 时间跨度
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据
     */
    Map<String, Object> getAccessTrend(String timeRange, Integer timeSpan, String startTime, String endTime);

    /**
     * 获取实时告警列表
     *
     * @param alertLevel 告警级别
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 告警列表
     */
    Map<String, Object> getRealtimeAlerts(String alertLevel, Integer pageNum, Integer pageSize);

    /**
     * 获取WebSocket连接统计
     *
     * @return WebSocket连接统计
     */
    Map<String, Object> getWebSocketStats();

    /**
     * 获取系统健康状态
     *
     * @return 系统健康状态
     */
    Map<String, Object> getHealthStatus();

    /**
     * 刷新统计数据
     */
    void refreshStatistics();

    /**
     * 清除历史数据
     *
     * @param dataType 数据类型
     * @param retainDays 保留天数
     */
    void clearHistoryData(String dataType, Integer retainDays);

    /**
     * 导出监控报告
     *
     * @param reportType 报告类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param format 导出格式
     * @return 报告数据
     */
    Map<String, Object> exportReport(String reportType, String startTime, String endTime, String format);

    /**
     * 发送WebSocket测试消息
     */
    void sendWebSocketTestMessage();

    // ==================== 增强功能 - OpenSpec Task 2.3 ====================

    /**
     * 获取设备详细监控状态（增强版）
     * 支持高并发访问，低延迟响应（≤5秒）
     *
     * @param deviceId 设备ID
     * @return 设备监控状态
     */
    ResponseDTO<AccessDeviceStatusVO> getDeviceMonitorStatus(Long deviceId);

    /**
     * 批量获取设备监控状态（支持≥1000台设备并发监控）
     *
     * @param deviceIds 设备ID列表
     * @return 设备监控状态列表
     */
    CompletableFuture<ResponseDTO<List<AccessDeviceStatusVO>>> getBatchDeviceMonitorStatus(List<Long> deviceIds);

    /**
     * 触发视频联动（增强版）
     * 当发生门禁告警时自动触发相关摄像头录像和监控
     *
     * @param deviceId 门禁设备ID
     * @param alertLevel 告警级别
     * @param triggerType 触发类型
     * @return 视频联动结果
     */
    ResponseDTO<VideoLinkageVO> triggerVideoLinkage(Long deviceId, String alertLevel, String triggerType);

    /**
     * 处理告警事件（增强版）
     * 支持多级告警处理和自动分派
     *
     * @param alertEventVO 告警事件
     * @return 处理结果
     */
    ResponseDTO<String> handleAlertEvent(AlertEventVO alertEventVO);

    /**
     * 获取实时人员轨迹追踪
     * 基于门禁记录和位置信息进行轨迹分析
     *
     * @param personId 人员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 人员轨迹数据
     */
    ResponseDTO<PersonTrackingVO> getPersonTracking(Long personId, String startTime, String endTime);

    /**
     * 获取综合监控统计信息（增强版）
     * 包含设备、告警、访问、区域、人员、性能等全面统计
     *
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param areaId 区域ID（可选）
     * @return 综合监控统计
     */
    ResponseDTO<MonitorStatisticsVO> getComprehensiveStatistics(String startTime, String endTime, Long areaId);

    /**
     * 实时告警级别升级检查
     * 根据预设规则自动升级告警级别
     *
     * @param alertId 告警ID
     * @return 升级后的告警信息
     */
    ResponseDTO<AlertEventVO> escalateAlertLevel(Long alertId);

    /**
     * 获取区域实时人员密度监控
     *
     * @param areaId 区域ID
     * @return 人员密度信息
     */
    ResponseDTO<Map<String, Object>> getAreaPeopleDensity(Long areaId);

    /**
     * 异步批量处理监控数据（支持高并发）
     *
     * @param monitorDataList 监控数据列表
     * @return 处理结果
     */
    CompletableFuture<ResponseDTO<Boolean>> batchProcessMonitorData(List<Map<String, Object>> monitorDataList);

    /**
     * 获取设备性能监控指标
     *
     * @param deviceId 设备ID
     * @param timeRange 时间范围
     * @return 性能指标
     */
    ResponseDTO<Map<String, Object>> getDevicePerformanceMetrics(Long deviceId, String timeRange);

    /**
     * 启动实时监控流（WebSocket增强版）
     * 支持≥1000并发连接的实时数据推送
     *
     * @param deviceId 设备ID
     * @return 监控流ID
     */
    ResponseDTO<String> startRealtimeMonitorStream(Long deviceId);

    /**
     * 停止实时监控流
     *
     * @param streamId 监控流ID
     * @return 停止结果
     */
    ResponseDTO<Boolean> stopRealtimeMonitorStream(String streamId);
}