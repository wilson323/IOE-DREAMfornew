package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.access.domain.entity.BiometricDeviceEntity;
import net.lab1024.sa.access.domain.entity.BiometricLogEntity;
import net.lab1024.sa.access.domain.query.BiometricQueryForm;
import net.lab1024.sa.access.domain.vo.BiometricAlertVO;
import net.lab1024.sa.access.domain.vo.BiometricStatusVO;
import net.lab1024.sa.common.domain.PageResult;

/**
 * 生物识别监控服务接口
 * <p>
 * 提供生物识别设备和系统的监控功能，包括：
 * - 设备状态监控
 * - 识别性能监控
 * - 异常告警管理
 * - 系统健康检查
 * - 统计分析报告
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
public interface BiometricMonitorService {

    /**
     * 获取所有生物识别设备的实时状态信息
     *
     * @return 设备状态列表
     */
    List<BiometricStatusVO> getAllDeviceStatus();

    /**
     * 根据设备ID获取设备的详细信息和状态
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    BiometricDeviceEntity getDeviceDetail(Long deviceId);

    /**
     * 获取指定设备的健康状态和性能指标
     *
     * @param deviceId 设备ID
     * @return 健康状态信息
     */
    Map<String, Object> getDeviceHealth(Long deviceId);

    /**
     * 获取设备在一定时间内的性能统计数据
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 性能统计数据
     */
    Map<String, Object> getDevicePerformance(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询生物识别操作日志
     *
     * @param pageForm 分页查询表单
     * @return 日志分页结果
     */
    PageResult<BiometricLogEntity> getBiometricLogs(BiometricQueryForm queryForm);

    /**
     * 获取今日的生物识别统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getTodayStatistics();

    /**
     * 获取指定时间范围的历史识别统计数据
     *
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @param statisticsType 统计类型 day/week/month
     * @return 统计数据
     */
    Map<String, Object> getHistoryStatistics(LocalDateTime startTime, LocalDateTime endTime, String statisticsType);

    /**
     * 分析各设备的识别成功率情况
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 成功率分析结果
     */
    Map<String, Object> getSuccessRateAnalysis(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分析各设备的识别响应时间分布
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 响应时间分析结果
     */
    Map<String, Object> getResponseTimeAnalysis(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取生物识别系统的异常告警信息
     *
     * @param alertLevel  告警级别
     * @param alertStatus 告警状态
     * @param days        查询天数
     * @return 告警列表
     */
    List<BiometricAlertVO> getBiometricAlerts(Integer alertLevel, Integer alertStatus, Integer days);

    /**
     * 标记告警为已处理状态
     *
     * @param alertId      告警ID
     * @param handleRemark 处理说明
     * @return 是否处理成功
     */
    boolean handleAlert(Long alertId, String handleRemark);

    /**
     * 获取整个生物识别系统的健康状态
     *
     * @return 系统健康状态
     */
    Map<String, Object> getSystemHealth();

    /**
     * 获取系统的实时负载数据
     *
     * @return 系统负载数据
     */
    Map<String, Object> getSystemLoad();

    /**
     * 检测并报告离线的生物识别设备
     *
     * @return 离线设备告警列表
     */
    List<BiometricAlertVO> checkOfflineDevices();

    /**
     * 检测设备性能异常并生成告警
     *
     * @return 性能异常告警列表
     */
    List<BiometricAlertVO> checkPerformanceAbnormal();

    /**
     * 监控各设备的识别准确率变化
     *
     * @param hours 监控时间范围(小时)
     * @return 准确率监控数据
     */
    Map<String, Object> getAccuracyMonitor(Integer hours);

    /**
     * 统计用户使用生物识别的活跃度
     *
     * @param days   统计天数
     * @param userId 用户ID（可选）
     * @return 用户活跃度数据
     */
    Map<String, Object> getUserActivity(Integer days, Long userId);

    /**
     * 获取设备维护和保养提醒信息
     *
     * @return 维护提醒列表
     */
    List<Map<String, Object>> getMaintenanceReminders();

    /**
     * 生成指定时间段的监控分析报告
     *
     * @param reportType 报告类型 daily/weekly/monthly
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 监控报告
     */
    Map<String, Object> generateMonitorReport(String reportType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 导出监控数据到Excel文件
     *
     * @param exportType 导出类型 logs/alerts/performance
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 下载URL
     */
    String exportMonitorData(String exportType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取监控仪表板展示的核心数据
     *
     * @return 仪表板数据
     */
    Map<String, Object> getDashboardData();
}
