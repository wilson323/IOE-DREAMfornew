package net.lab1024.sa.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.device.domain.entity.DeviceHealthEntity;
import net.lab1024.sa.device.domain.vo.DeviceHealthReportVO;
import net.lab1024.sa.device.domain.vo.DeviceHealthStatisticsVO;
import net.lab1024.sa.device.service.DeviceHealthService;

import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备健康监控控制器
 * <p>
 * 提供设备健康状态监控和管理功能，包括：
 * - 设备健康状态实时监控
 * - 设备故障预测和预警
 * - 设备性能分析
 * - 健康报告生成
 * - 维护建议
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
@RequestMapping("/device/health")
@Tag(name = "设备健康监控", description = "设备健康状态监控和管理API")
public class DeviceHealthController {

    @Resource
    private DeviceHealthService deviceHealthService;

    @Operation(summary = "获取设备健康状态", description = "获取指定设备的实时健康状态")
    @GetMapping("/{deviceId}")
    public ResponseDTO<DeviceHealthEntity> getDeviceHealth(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId) {

        DeviceHealthEntity health = deviceHealthService.getDeviceHealth(deviceId);
        return ResponseDTO.ok(health);
    }

    @Operation(summary = "获取所有设备健康状态", description = "获取所有设备的健康状态概览")
    @GetMapping("/overview")
    public ResponseDTO<List<DeviceHealthEntity>> getAllDevicesHealth() {
        List<DeviceHealthEntity> healthList = deviceHealthService.getAllDevicesHealth();
        return ResponseDTO.ok(healthList);
    }

    @Operation(summary = "获取设备健康报告", description = "生成指定设备的健康分析报告")
    @GetMapping("/report/{deviceId}")
    public ResponseDTO<DeviceHealthReportVO> getDeviceHealthReport(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "报告类型 daily/weekly/monthly")
            @RequestParam(defaultValue = "daily") String reportType,
            @Parameter(description = "开始时间")
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间")
            @RequestParam(required = false) LocalDateTime endTime) {

        DeviceHealthReportVO report = deviceHealthService.generateHealthReport(
            deviceId, reportType, startTime, endTime);
        return ResponseDTO.ok(report);
    }

    @Operation(summary = "设备健康统计", description = "获取设备健康状态统计数据")
    @GetMapping("/statistics")
    public ResponseDTO<DeviceHealthStatisticsVO> getHealthStatistics(
            @Parameter(description = "统计时间范围")
            @RequestParam(defaultValue = "24") Integer hours,
            @Parameter(description = "设备类型")
            @RequestParam(required = false) String deviceType) {

        DeviceHealthStatisticsVO statistics = deviceHealthService.getHealthStatistics(hours, deviceType);
        return ResponseDTO.ok(statistics);
    }

    @Operation(summary = "故障设备列表", description = "获取有故障或异常的设备列表")
    @GetMapping("/faulty")
    public ResponseDTO<List<DeviceHealthEntity>> getFaultyDevices(
            @Parameter(description = "健康状态等级 1-正常 2-警告 3-严重 4-故障")
            @RequestParam(required = false) Integer healthLevel,
            @Parameter(description = "设备类型")
            @RequestParam(required = false) String deviceType) {

        List<DeviceHealthEntity> faultyDevices = deviceHealthService.getFaultyDevices(healthLevel, deviceType);
        return ResponseDTO.ok(faultyDevices);
    }

    @Operation(summary = "设备性能分析", description = "分析设备性能指标和趋势")
    @GetMapping("/performance/{deviceId}")
    public ResponseDTO<Map<String, Object>> getDevicePerformanceAnalysis(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "分析时间范围(小时)")
            @RequestParam(defaultValue = "24") Integer hours) {

        Map<String, Object> performance = deviceHealthService.getDevicePerformanceAnalysis(deviceId, hours);
        return ResponseDTO.ok(performance);
    }

    @Operation(summary = "故障预测", description = "基于历史数据预测设备可能的故障")
    @GetMapping("/prediction/{deviceId}")
    public ResponseDTO<Map<String, Object>> predictDeviceFailure(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "预测天数")
            @RequestParam(defaultValue = "7") Integer predictDays) {

        Map<String, Object> prediction = deviceHealthService.predictDeviceFailure(deviceId, predictDays);
        return ResponseDTO.ok(prediction);
    }

    @Operation(summary = "健康检查", description = "手动触发设备健康检查")
    @PostMapping("/check/{deviceId}")
    public ResponseDTO<DeviceHealthEntity> performHealthCheck(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "检查类型 full/basic")
            @RequestParam(defaultValue = "basic") String checkType) {

        DeviceHealthEntity health = deviceHealthService.performHealthCheck(deviceId, checkType);
        return ResponseDTO.ok(health);
    }

    @Operation(summary = "批量健康检查", description = "批量检查多个设备的健康状态")
    @PostMapping("/batch-check")
    public ResponseDTO<List<DeviceHealthEntity>> batchHealthCheck(
            @Parameter(description = "设备ID列表", required = true)
            @RequestParam List<Long> deviceIds,
            @Parameter(description = "检查类型")
            @RequestParam(defaultValue = "basic") String checkType) {

        List<DeviceHealthEntity> healthResults = deviceHealthService.batchHealthCheck(deviceIds, checkType);
        return ResponseDTO.ok(healthResults);
    }

    @Operation(summary = "获取健康趋势", description = "获取设备健康状态变化趋势")
    @GetMapping("/trend/{deviceId}")
    public ResponseDTO<Map<String, Object>> getHealthTrend(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "时间范围天数")
            @RequestParam(defaultValue = "7") Integer days) {

        Map<String, Object> trend = deviceHealthService.getHealthTrend(deviceId, days);
        return ResponseDTO.ok(trend);
    }

    @Operation(summary = "维护建议", description = "基于健康状态提供设备维护建议")
    @GetMapping("/maintenance-suggestion/{deviceId}")
    public ResponseDTO<List<Map<String, Object>>> getMaintenanceSuggestions(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId) {

        List<Map<String, Object>> suggestions = deviceHealthService.getMaintenanceSuggestions(deviceId);
        return ResponseDTO.ok(suggestions);
    }

    @Operation(summary = "健康告警配置", description = "配置设备健康状态告警规则")
    @PostMapping("/alert/config")
    public ResponseDTO<Boolean> configureHealthAlert(
            @Parameter(description = "设备ID", required = true)
            @RequestParam Long deviceId,
            @Parameter(description = "告警阈值", required = true)
            @RequestParam Double alertThreshold,
            @Parameter(description = "告警类型", required = true)
            @RequestParam String alertType) {

        boolean result = deviceHealthService.configureHealthAlert(deviceId, alertThreshold, alertType);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "健康历史数据", description = "获取设备健康状态历史数据")
    @GetMapping("/history/{deviceId}")
    public ResponseDTO<List<DeviceHealthEntity>> getHealthHistory(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "开始时间")
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间")
            @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "记录数量限制")
            @RequestParam(defaultValue = "100") Integer limit) {

        List<DeviceHealthEntity> history = deviceHealthService.getHealthHistory(
            deviceId, startTime, endTime, limit);
        return ResponseDTO.ok(history);
    }

    @Operation(summary = "健康指标分析", description = "分析设备各项健康指标")
    @GetMapping("/metrics/{deviceId}")
    public ResponseDTO<Map<String, Object>> getHealthMetrics(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "指标类型 cpu/memory/network/storage")
            @RequestParam(required = false) String metricType) {

        Map<String, Object> metrics = deviceHealthService.getHealthMetrics(deviceId, metricType);
        return ResponseDTO.ok(metrics);
    }

    @Operation(summary = "导出健康报告", description = "导出设备健康分析报告")
    @PostMapping("/export/{deviceId}")
    public ResponseDTO<String> exportHealthReport(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "报告格式 pdf/excel/word")
            @RequestParam(defaultValue = "pdf") String format,
            @Parameter(description = "报告类型")
            @RequestParam(defaultValue = "comprehensive") String reportType) {

        String downloadUrl = deviceHealthService.exportHealthReport(deviceId, format, reportType);
        return ResponseDTO.ok(downloadUrl);
    }

    @Operation(summary = "设置健康检查计划", description = "设置设备定期健康检查计划")
    @PostMapping("/schedule/{deviceId}")
    public ResponseDTO<Boolean> setHealthCheckSchedule(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "检查间隔(分钟)", required = true)
            @RequestParam Integer intervalMinutes,
            @Parameter(description = "检查类型")
            @RequestParam(defaultValue = "basic") String checkType) {

        boolean result = deviceHealthService.setHealthCheckSchedule(deviceId, intervalMinutes, checkType);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "健康状态通知", description = "配置设备健康状态变化通知")
    @PostMapping("/notification/config")
    public ResponseDTO<Boolean> configureHealthNotification(
            @Parameter(description = "设备ID", required = true)
            @RequestParam Long deviceId,
            @Parameter(description = "通知方式 email/sms/webhook")
            @RequestParam String notificationMethod,
            @Parameter(description = "通知接收人")
            @RequestParam String recipient) {

        boolean result = deviceHealthService.configureHealthNotification(
            deviceId, notificationMethod, recipient);
        return ResponseDTO.ok(result);
    }
}