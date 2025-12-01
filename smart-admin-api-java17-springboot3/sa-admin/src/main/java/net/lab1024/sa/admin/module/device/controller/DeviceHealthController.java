package net.lab1024.sa.admin.module.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.device.service.DeviceHealthService;
import net.lab1024.sa.admin.module.device.domain.vo.DeviceHealthOverviewVO;
import net.lab1024.sa.admin.module.device.domain.vo.DeviceHealthTrendVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.annotation.SaCheckPermission;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备健康诊断控制器
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@RestController
@RequestMapping("/api/device/health")
@Tag(name = "设备健康诊断", description = "设备健康监控、评分和维护建议相关接口")
@Validated
@Slf4j
public class DeviceHealthController {

    @Resource
    private DeviceHealthService deviceHealthService;

    @Operation(summary = "获取设备健康总览", description = "获取指定设备的健康状态总览信息")
    @GetMapping("/overview/{deviceId}")
    @SaCheckPermission("device:health:query")
    public ResponseDTO<DeviceHealthOverviewVO> getHealthOverview(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId) {

        return deviceHealthService.getHealthOverview(deviceId);
    }

    @Operation(summary = "获取设备健康总览列表", description = "分页获取所有设备的健康状态总览")
    @GetMapping("/overview/list")
    @SaCheckPermission("device:health:query")
    public ResponseDTO<PageResult<DeviceHealthOverviewVO>> getHealthOverviewList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") @Min(1) Integer pageSize,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "健康等级筛选") @RequestParam(required = false) String healthLevel) {

        return deviceHealthService.getHealthOverviewList(pageNum, pageSize, areaId, healthLevel);
    }

    @Operation(summary = "获取设备健康趋势", description = "获取设备在指定时间范围内的健康趋势数据")
    @GetMapping("/trend/{deviceId}")
    @SaCheckPermission("device:health:query")
    public ResponseDTO<DeviceHealthTrendVO> getHealthTrend(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "开始时间") @RequestParam @NotNull(message = "开始时间不能为空") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @NotNull(message = "结束时间不能为空") LocalDateTime endTime) {

        if (endTime.isBefore(startTime)) {
            return ResponseDTO.error("结束时间不能早于开始时间");
        }

        return deviceHealthService.getHealthTrend(deviceId, startTime, endTime);
    }

    @Operation(summary = "计算设备健康评分", description = "重新计算并更新设备的健康评分")
    @PostMapping("/calculate/{deviceId}")
    @SaCheckPermission("device:health:calculate")
    public ResponseDTO<BigDecimal> calculateHealthScore(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId) {

        return deviceHealthService.calculateHealthScore(deviceId);
    }

    @Operation(summary = "创建设备健康快照", description = "为指定设备创建健康快照")
    @PostMapping("/snapshot/{deviceId}")
    @SaCheckPermission("device:health:create")
    public ResponseDTO<Void> createHealthSnapshot(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId) {

        return deviceHealthService.createHealthSnapshot(deviceId);
    }

    @Operation(summary = "批量创建健康快照", description = "为多个设备批量创建健康快照")
    @PostMapping("/snapshot/batch")
    @SaCheckPermission("device:health:create")
    public ResponseDTO<Integer> batchCreateHealthSnapshots(
            @Parameter(description = "设备ID列表", required = true)
            @RequestBody @NotNull(message = "设备ID列表不能为空") List<Long> deviceIds) {

        if (deviceIds.isEmpty()) {
            return ResponseDTO.error("设备ID列表不能为空");
        }

        return deviceHealthService.batchCreateHealthSnapshots(deviceIds);
    }

    @Operation(summary = "获取健康评分统计", description = "获取指定时间范围内的健康评分统计信息")
    @GetMapping("/statistics/score")
    @SaCheckPermission("device:health:query")
    public ResponseDTO<Map<String, Object>> getHealthScoreStatistics(
            @Parameter(description = "开始时间") @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam LocalDateTime endTime) {

        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            return ResponseDTO.error("结束时间不能早于开始时间");
        }

        return deviceHealthService.getHealthScoreStatistics(startTime, endTime);
    }

    @Operation(summary = "获取健康等级分布", description = "获取当前所有设备的健康等级分布统计")
    @GetMapping("/statistics/distribution")
    @SaCheckPermission("device:health:query")
    public ResponseDTO<Map<String, Object>> getHealthLevelDistribution() {

        return deviceHealthService.getHealthLevelDistribution();
    }

    @Operation(summary = "获取需要关注的设备", description = "获取健康评分低于指定阈值的设备列表")
    @GetMapping("/attention")
    @SaCheckPermission("device:health:query")
    public ResponseDTO<List<DeviceHealthOverviewVO>> getAttentionDevices(
            @Parameter(description = "评分阈值") @RequestParam(defaultValue = "60") BigDecimal scoreThreshold) {

        if (scoreThreshold.compareTo(BigDecimal.ZERO) < 0 || scoreThreshold.compareTo(BigDecimal.valueOf(100)) > 0) {
            return ResponseDTO.error("评分阈值必须在0-100之间");
        }

        return deviceHealthService.getAttentionDevices(scoreThreshold);
    }

    @Operation(summary = "预测维护时间", description = "根据健康趋势预测设备下次需要维护的时间")
    @GetMapping("/predict-maintenance/{deviceId}")
    @SaCheckPermission("device:health:query")
    public ResponseDTO<LocalDateTime> predictMaintenanceTime(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId) {

        return deviceHealthService.predictMaintenanceTime(deviceId);
    }

    @Operation(summary = "获取设备健康配置", description = "获取设备的健康评分配置信息")
    @GetMapping("/config/{deviceId}")
    @SaCheckPermission("device:health:config")
    public ResponseDTO<Map<String, Object>> getHealthConfig(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId) {

        return deviceHealthService.getHealthConfig(deviceId);
    }

    @Operation(summary = "更新设备健康配置", description = "更新设备的健康评分配置")
    @PutMapping("/config/{deviceId}")
    @SaCheckPermission("device:health:config")
    public ResponseDTO<Void> updateHealthConfig(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "配置信息", required = true)
            @RequestBody @NotNull(message = "配置信息不能为空") Map<String, Object> config) {

        return deviceHealthService.updateHealthConfig(deviceId, config);
    }

    @Operation(summary = "导出健康报告", description = "导出指定设备的健康报告")
    @PostMapping("/export")
    @SaCheckPermission("device:health:export")
    public ResponseDTO<String> exportHealthReport(
            @Parameter(description = "设备ID列表", required = true)
            @RequestBody @NotNull(message = "设备ID列表不能为空") List<Long> deviceIds,
            @Parameter(description = "开始时间") @RequestParam @NotNull(message = "开始时间不能为空") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @NotNull(message = "结束时间不能为空") LocalDateTime endTime,
            HttpServletResponse response) {

        if (deviceIds.isEmpty()) {
            return ResponseDTO.error("设备ID列表不能为空");
        }

        if (endTime.isBefore(startTime)) {
            return ResponseDTO.error("结束时间不能早于开始时间");
        }

        ResponseDTO<String> exportResult = deviceHealthService.exportHealthReport(deviceIds, startTime, endTime);

        if (exportResult.isOk()) {
            // 设置下载响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=device_health_report.xlsx");
        }

        return exportResult;
    }

    @Operation(summary = "触发健康检查", description = "手动触发全量设备的健康检查任务")
    @PostMapping("/trigger-check")
    @SaCheckPermission("device:health:manage")
    public ResponseDTO<Void> triggerHealthCheck() {

        return deviceHealthService.triggerHealthCheck();
    }

    @Operation(summary = "获取健康数据采集状态", description = "获取健康数据采集任务的运行状态")
    @GetMapping("/collection-status")
    @SaCheckPermission("device:health:query")
    public ResponseDTO<Map<String, Object>> getHealthCollectionStatus() {

        return deviceHealthService.getHealthCollectionStatus();
    }

    @Operation(summary = "手动采集设备指标", description = "手动采集指定设备的各项健康指标数据")
    @PostMapping("/collect-metrics/{deviceId}")
    @SaCheckPermission("device:health:collect")
    public ResponseDTO<Void> collectDeviceMetrics(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId) {

        return deviceHealthService.collectDeviceMetrics(deviceId);
    }

    @Operation(summary = "获取健康诊断建议", description = "获取AI生成的设备健康诊断建议")
    @GetMapping("/suggestions/{deviceId}")
    @SaCheckPermission("device:health:query")
    public ResponseDTO<List<String>> getHealthDiagnosticSuggestions(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId) {

        return deviceHealthService.getHealthDiagnosticSuggestions(deviceId);
    }

    @Operation(summary = "清理历史数据", description = "清理指定天数之前的健康历史数据")
    @DeleteMapping("/cleanup")
    @SaCheckPermission("device:health:manage")
    public ResponseDTO<Integer> cleanHealthHistoryData(
            @Parameter(description = "保留天数", required = true)
            @RequestParam @Min(1) Integer retentionDays) {

        return deviceHealthService.cleanHealthHistoryData(retentionDays);
    }
}