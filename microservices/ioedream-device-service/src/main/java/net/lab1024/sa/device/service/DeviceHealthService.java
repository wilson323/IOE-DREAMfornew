package net.lab1024.sa.device.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.device.domain.entity.DeviceHealthEntity;
import net.lab1024.sa.device.domain.vo.DeviceHealthReportVO;
import net.lab1024.sa.device.domain.vo.DeviceHealthStatisticsVO;

/**
 * 设备健康服务接口
 *
 * 提供设备健康状态监控和管理功能：
 * - 设备健康状态查询
 * - 健康报告生成
 * - 健康统计分析
 * - 故障预测
 * - 维护建议
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
public interface DeviceHealthService {

    /**
     * 获取设备健康状态
     *
     * @param deviceId 设备ID
     * @return 设备健康实体
     */
    DeviceHealthEntity getDeviceHealth(Long deviceId);

    /**
     * 获取所有设备健康状态
     *
     * @return 设备健康列表
     */
    List<DeviceHealthEntity> getAllDevicesHealth();

    /**
     * 生成设备健康报告
     *
     * @param deviceId   设备ID
     * @param reportType 报告类型(daily/weekly/monthly)
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 健康报告VO
     */
    DeviceHealthReportVO generateHealthReport(Long deviceId, String reportType,
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取健康统计信息
     *
     * @param hours      统计时间范围(小时)
     * @param deviceType 设备类型(可选)
     * @return 健康统计VO
     */
    DeviceHealthStatisticsVO getHealthStatistics(Integer hours, String deviceType);

    /**
     * 获取故障设备列表
     *
     * @param healthLevel 健康状态等级(1-正常 2-警告 3-严重 4-故障)
     * @param deviceType  设备类型(可选)
     * @return 故障设备列表
     */
    List<DeviceHealthEntity> getFaultyDevices(Integer healthLevel, String deviceType);

    /**
     * 获取设备性能分析
     *
     * @param deviceId 设备ID
     * @param hours    分析时间范围(小时)
     * @return 性能分析结果
     */
    Map<String, Object> getDevicePerformanceAnalysis(Long deviceId, Integer hours);

    /**
     * 预测设备故障
     *
     * @param deviceId    设备ID
     * @param predictDays 预测天数
     * @return 故障预测结果
     */
    Map<String, Object> predictDeviceFailure(Long deviceId, Integer predictDays);

    /**
     * 执行健康检查
     *
     * @param deviceId  设备ID
     * @param checkType 检查类型(full/basic)
     * @return 健康检查结果
     */
    DeviceHealthEntity performHealthCheck(Long deviceId, String checkType);

    /**
     * 批量健康检查
     *
     * @param deviceIds 设备ID列表
     * @param checkType 检查类型
     * @return 健康检查结果列表
     */
    List<DeviceHealthEntity> batchHealthCheck(List<Long> deviceIds, String checkType);

    /**
     * 获取健康趋势
     *
     * @param deviceId 设备ID
     * @param days     时间范围天数
     * @return 健康趋势数据
     */
    Map<String, Object> getHealthTrend(Long deviceId, Integer days);

    /**
     * 获取维护建议
     *
     * @param deviceId 设备ID
     * @return 维护建议列表
     */
    List<Map<String, Object>> getMaintenanceSuggestions(Long deviceId);

    /**
     * 配置健康告警
     *
     * @param deviceId       设备ID
     * @param alertThreshold 告警阈值
     * @param alertType      告警类型
     * @return 配置结果
     */
    boolean configureHealthAlert(Long deviceId, Double alertThreshold, String alertType);

    /**
     * 获取健康历史数据
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     记录数量限制
     * @return 健康历史数据列表
     */
    List<DeviceHealthEntity> getHealthHistory(Long deviceId, LocalDateTime startTime,
            LocalDateTime endTime, Integer limit);

    /**
     * 获取健康指标
     *
     * @param deviceId   设备ID
     * @param metricType 指标类型(cpu/memory/network/storage)
     * @return 健康指标数据
     */
    Map<String, Object> getHealthMetrics(Long deviceId, String metricType);

    /**
     * 导出健康报告
     *
     * @param deviceId   设备ID
     * @param format     报告格式(pdf/excel/word)
     * @param reportType 报告类型
     * @return 下载URL
     */
    String exportHealthReport(Long deviceId, String format, String reportType);

    /**
     * 设置健康检查计划
     *
     * @param deviceId        设备ID
     * @param intervalMinutes 检查间隔(分钟)
     * @param checkType       检查类型
     * @return 设置结果
     */
    boolean setHealthCheckSchedule(Long deviceId, Integer intervalMinutes, String checkType);

    /**
     * 配置健康状态通知
     *
     * @param deviceId           设备ID
     * @param notificationMethod 通知方式(email/sms/webhook)
     * @param recipient          通知接收人
     * @return 配置结果
     */
    boolean configureHealthNotification(Long deviceId, String notificationMethod, String recipient);
}
