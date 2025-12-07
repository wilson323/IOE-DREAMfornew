package net.lab1024.sa.access.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.access.domain.entity.BiometricDeviceEntity;
import net.lab1024.sa.access.domain.entity.BiometricLogEntity;
import net.lab1024.sa.access.domain.query.BiometricQueryForm;
import net.lab1024.sa.access.domain.vo.BiometricAlertVO;
import net.lab1024.sa.access.domain.vo.BiometricStatusVO;
import net.lab1024.sa.access.service.BiometricMonitorService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 生物识别监控控制器
 * <p>
 * 提供生物识别设备和系统的监控功能，包括：
 * - 设备状态监控
 * - 识别性能监控
 * - 异常告警管理
 * - 系统健康检查
 * - 统计分析报告
 * </p>
 * 严格遵循repowiki编码规范：
 * - 使用jakarta包名
 * - 使用@Resource依赖注入
 * - RESTful API设计
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@RestController
@RequestMapping("/biometric/monitor")
@Tag(name = "生物识别监控", description = "生物识别设备和系统监控API")
public class BiometricMonitorController {

    @Resource
    private BiometricMonitorService biometricMonitorService;

    @Operation(summary = "获取所有设备状态", description = "获取所有生物识别设备的实时状态信息")
    @GetMapping("/devices/status")
    public ResponseDTO<List<BiometricStatusVO>> getAllDeviceStatus() {
        List<BiometricStatusVO> deviceStatus = biometricMonitorService.getAllDeviceStatus();
        return ResponseDTO.ok(deviceStatus);
    }

    @Operation(summary = "获取设备详细信息", description = "根据设备ID获取设备的详细信息和状态")
    @GetMapping("/device/{deviceId}")
    public ResponseDTO<BiometricDeviceEntity> getDeviceDetail(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {

        BiometricDeviceEntity device = biometricMonitorService.getDeviceDetail(deviceId);
        return ResponseDTO.ok(device);
    }

    @Operation(summary = "获取设备健康状态", description = "获取指定设备的健康状态和性能指标")
    @GetMapping("/device/{deviceId}/health")
    public ResponseDTO<Map<String, Object>> getDeviceHealth(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {

        Map<String, Object> healthInfo = biometricMonitorService.getDeviceHealth(deviceId);
        return ResponseDTO.ok(healthInfo);
    }

    @Operation(summary = "设备性能统计", description = "获取设备在一定时间内的性能统计数据")
    @GetMapping("/device/{deviceId}/performance")
    public ResponseDTO<Map<String, Object>> getDevicePerformance(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        Map<String, Object> performance = biometricMonitorService.getDevicePerformance(
                deviceId, startTime, endTime);
        return ResponseDTO.ok(performance);
    }

    @Operation(summary = "获取识别日志", description = "分页查询生物识别操作日志")
    @PostMapping("/logs/page")
    public ResponseDTO<PageResult<BiometricLogEntity>> getBiometricLogs(
            @Valid @RequestBody BiometricQueryForm queryForm) {

        PageResult<BiometricLogEntity> logs = biometricMonitorService.getBiometricLogs(queryForm);
        return ResponseDTO.ok(logs);
    }

    @Operation(summary = "获取今日识别统计", description = "获取今日的生物识别统计数据")
    @GetMapping("/statistics/today")
    public ResponseDTO<Map<String, Object>> getTodayStatistics() {
        Map<String, Object> statistics = biometricMonitorService.getTodayStatistics();
        return ResponseDTO.ok(statistics);
    }

    @Operation(summary = "获取历史识别统计", description = "获取指定时间范围的历史识别统计数据")
    @GetMapping("/statistics/history")
    public ResponseDTO<Map<String, Object>> getHistoryStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "统计类型 day/week/month") @RequestParam(defaultValue = "day") String statisticsType) {

        Map<String, Object> statistics = biometricMonitorService.getHistoryStatistics(
                startTime, endTime, statisticsType);
        return ResponseDTO.ok(statistics);
    }

    @Operation(summary = "识别成功率分析", description = "分析各设备的识别成功率情况")
    @GetMapping("/success-rate")
    public ResponseDTO<Map<String, Object>> getSuccessRateAnalysis(
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        Map<String, Object> successRate = biometricMonitorService.getSuccessRateAnalysis(startTime, endTime);
        return ResponseDTO.ok(successRate);
    }

    @Operation(summary = "识别响应时间分析", description = "分析各设备的识别响应时间分布")
    @GetMapping("/response-time")
    public ResponseDTO<Map<String, Object>> getResponseTimeAnalysis(
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        Map<String, Object> responseTime = biometricMonitorService.getResponseTimeAnalysis(startTime, endTime);
        return ResponseDTO.ok(responseTime);
    }

    @Operation(summary = "获取异常告警", description = "获取生物识别系统的异常告警信息")
    @GetMapping("/alerts")
    public ResponseDTO<List<BiometricAlertVO>> getBiometricAlerts(
            @Parameter(description = "告警级别 1-严重 2-重要 3-一般 4-提示") @RequestParam(required = false) Integer alertLevel,
            @Parameter(description = "告警状态 0-未处理 1-处理中 2-已处理") @RequestParam(required = false) Integer alertStatus,
            @Parameter(description = "查询天数") @RequestParam(defaultValue = "7") Integer days) {

        List<BiometricAlertVO> alerts = biometricMonitorService.getBiometricAlerts(
                alertLevel, alertStatus, days);
        return ResponseDTO.ok(alerts);
    }

    @Operation(summary = "处理告警", description = "标记告警为已处理状态")
    @PutMapping("/alert/{alertId}/handle")
    public ResponseDTO<Boolean> handleAlert(
            @Parameter(description = "告警ID", required = true) @PathVariable Long alertId,
            @Parameter(description = "处理说明") @RequestParam String handleRemark) {

        boolean result = biometricMonitorService.handleAlert(alertId, handleRemark);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取系统健康状态", description = "获取整个生物识别系统的健康状态")
    @GetMapping("/system/health")
    public ResponseDTO<Map<String, Object>> getSystemHealth() {
        Map<String, Object> systemHealth = biometricMonitorService.getSystemHealth();
        return ResponseDTO.ok(systemHealth);
    }

    @Operation(summary = "系统负载监控", description = "获取系统的实时负载数据")
    @GetMapping("/system/load")
    public ResponseDTO<Map<String, Object>> getSystemLoad() {
        Map<String, Object> systemLoad = biometricMonitorService.getSystemLoad();
        return ResponseDTO.ok(systemLoad);
    }

    @Operation(summary = "设备离线告警", description = "检测并报告离线的生物识别设备")
    @PostMapping("/device/offline/check")
    public ResponseDTO<List<BiometricAlertVO>> checkOfflineDevices() {
        List<BiometricAlertVO> offlineAlerts = biometricMonitorService.checkOfflineDevices();
        return ResponseDTO.ok(offlineAlerts);
    }

    @Operation(summary = "性能异常检测", description = "检测设备性能异常并生成告警")
    @PostMapping("/performance/abnormal/check")
    public ResponseDTO<List<BiometricAlertVO>> checkPerformanceAbnormal() {
        List<BiometricAlertVO> performanceAlerts = biometricMonitorService.checkPerformanceAbnormal();
        return ResponseDTO.ok(performanceAlerts);
    }

    @Operation(summary = "识别准确率监控", description = "监控各设备的识别准确率变化")
    @GetMapping("/accuracy/monitor")
    public ResponseDTO<Map<String, Object>> getAccuracyMonitor(
            @Parameter(description = "监控时间范围(小时)") @RequestParam(defaultValue = "24") Integer hours) {

        Map<String, Object> accuracyMonitor = biometricMonitorService.getAccuracyMonitor(hours);
        return ResponseDTO.ok(accuracyMonitor);
    }

    @Operation(summary = "用户识别活跃度", description = "统计用户使用生物识别的活跃度")
    @GetMapping("/user/activity")
    public ResponseDTO<Map<String, Object>> getUserActivity(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {

        Map<String, Object> userActivity = biometricMonitorService.getUserActivity(days, userId);
        return ResponseDTO.ok(userActivity);
    }

    @Operation(summary = "设备维护提醒", description = "获取设备维护和保养提醒信息")
    @GetMapping("/device/maintenance")
    public ResponseDTO<List<Map<String, Object>>> getMaintenanceReminders() {
        List<Map<String, Object>> maintenance = biometricMonitorService.getMaintenanceReminders();
        return ResponseDTO.ok(maintenance);
    }

    @Operation(summary = "生成监控报告", description = "生成指定时间段的监控分析报告")
    @PostMapping("/report/generate")
    public ResponseDTO<Map<String, Object>> generateMonitorReport(
            @Parameter(description = "报告类型 daily/weekly/monthly") @RequestParam String reportType,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        Map<String, Object> report = biometricMonitorService.generateMonitorReport(
                reportType, startTime, endTime);
        return ResponseDTO.ok(report);
    }

    @Operation(summary = "导出监控数据", description = "导出监控数据到Excel文件")
    @PostMapping("/data/export")
    public ResponseDTO<String> exportMonitorData(
            @Parameter(description = "导出类型 logs/alerts/performance") @RequestParam String exportType,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        String downloadUrl = biometricMonitorService.exportMonitorData(
                exportType, startTime, endTime);
        return ResponseDTO.ok(downloadUrl);
    }

    @Operation(summary = "监控仪表板数据", description = "获取监控仪表板展示的核心数据")
    @GetMapping("/dashboard")
    public ResponseDTO<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboardData = biometricMonitorService.getDashboardData();
        return ResponseDTO.ok(dashboardData);
    }
}
