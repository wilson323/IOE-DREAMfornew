package net.lab1024.sa.admin.module.smart.monitor.service;

import java.util.Map;

/**
 * 门禁实时监控服务接口
 * <p>
 * 严格遵循repowiki规范：
 * - 接口定义清晰，职责单一
 * - 方法命名遵循RESTful规范
 * - 完整的业务逻辑封装
 * - 实时数据处理和推送
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
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
}