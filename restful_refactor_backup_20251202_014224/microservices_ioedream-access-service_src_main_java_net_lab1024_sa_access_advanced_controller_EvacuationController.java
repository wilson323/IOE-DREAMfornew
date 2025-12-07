package net.lab1024.sa.access.advanced.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.advanced.domain.entity.EvacuationPointEntity;
import net.lab1024.sa.access.advanced.domain.entity.EvacuationEventEntity;
import net.lab1024.sa.access.advanced.domain.entity.EvacuationRecordEntity;
import net.lab1024.sa.access.advanced.domain.dto.EvacuationTriggerDTO;
import net.lab1024.sa.access.advanced.service.EvacuationService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 疏散管理控制器
 * 提供疏散点管理、疏散事件处理和人员疏散追踪功能
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/access/evacuation")
@Tag(name = "疏散管理", description = "疏散点管理和人员疏散控制")
@Slf4j
public class EvacuationController {

    @Resource
    private EvacuationService evacuationService;

    // ========== 疏散点管理接口 ==========

    @Operation(summary = "获取所有激活的疏散点", description = "获取所有状态为激活的疏散点列表")
    @SaCheckPermission("evacuation:point:view")
    @GetMapping("/points/active")
    public ResponseDTO<List<EvacuationPointEntity>> getActiveEvacuationPoints() {
        List<EvacuationPointEntity> points = evacuationService.getActiveEvacuationPoints();
        return ResponseDTO.ok(points);
    }

    @Operation(summary = "根据区域获取疏散点", description = "根据指定区域ID获取疏散点列表")
    @SaCheckPermission("evacuation:point:view")
    @GetMapping("/points/area/{areaId}")
    public ResponseDTO<List<EvacuationPointEntity>> getEvacuationPointsByArea(@PathVariable Long areaId) {
        List<EvacuationPointEntity> points = evacuationService.getEvacuationPointsByArea(areaId);
        return ResponseDTO.ok(points);
    }

    @Operation(summary = "获取可用的疏散点", description = "获取容量未满的可用疏散点列表")
    @SaCheckPermission("evacuation:point:view")
    @GetMapping("/points/available")
    public ResponseDTO<List<EvacuationPointEntity>> getAvailableEvacuationPoints(
            @RequestParam(required = false) Long areaId) {
        List<EvacuationPointEntity> points = evacuationService.getAvailableEvacuationPoints(areaId);
        return ResponseDTO.ok(points);
    }

    @Operation(summary = "创建疏散点", description = "创建新的疏散点")
    @SaCheckPermission("evacuation:point:add")
    @PostMapping("/points")
    public ResponseDTO<Map<String, Object>> createEvacuationPoint(@Valid @RequestBody EvacuationPointEntity point) {
        Map<String, Object> result = evacuationService.createEvacuationPoint(point);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "更新疏散点", description = "更新疏散点信息")
    @SaCheckPermission("evacuation:point:edit")
    @PutMapping("/points/{pointId}")
    public ResponseDTO<Map<String, Object>> updateEvacuationPoint(
            @PathVariable Long pointId, @Valid @RequestBody EvacuationPointEntity point) {
        point.setPointId(pointId);
        Map<String, Object> result = evacuationService.updateEvacuationPoint(point);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "更新疏散点当前人数", description = "更新疏散点的当前人员数量")
    @SaCheckPermission("evacuation:point:edit")
    @PutMapping("/points/{pointId}/count")
    public ResponseDTO<Map<String, Object>> updateCurrentCount(
            @PathVariable Long pointId, @RequestParam Integer currentCount) {
        Map<String, Object> result = evacuationService.updateCurrentCount(pointId, currentCount);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取疏散点统计", description = "获取疏散点的统计信息")
    @SaCheckPermission("evacuation:point:view")
    @GetMapping("/points/statistics")
    public ResponseDTO<Map<String, Object>> getEvacuationPointStatistics(
            @RequestParam(required = false) Long pointId) {
        Map<String, Object> statistics = evacuationService.getEvacuationPointStatistics(pointId);
        return ResponseDTO.ok(statistics);
    }

    @Operation(summary = "检查容量告警", description = "检查所有疏散点的容量使用情况并返回告警信息")
    @SaCheckPermission("evacuation:point:view")
    @GetMapping("/points/capacity-alerts")
    public ResponseDTO<List<Map<String, Object>>> checkCapacityAlerts() {
        List<Map<String, Object>> alerts = evacuationService.checkCapacityAlerts();
        return ResponseDTO.ok(alerts);
    }

    // ========== 疏散事件管理接口 ==========

    @Operation(summary = "触发疏散事件", description = "根据触发条件启动疏散事件")
    @SaCheckPermission("evacuation:event:trigger")
    @PostMapping("/events/trigger")
    public ResponseDTO<Map<String, Object>> triggerEvacuation(@Valid @RequestBody EvacuationTriggerDTO triggerDTO) {
        log.info("[EvacuationController] 触发疏散事件: {}", triggerDTO.getEventTitle());
        Map<String, Object> result = evacuationService.triggerEvacuation(triggerDTO);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取当前活跃疏散事件", description = "获取所有正在进行的疏散事件")
    @SaCheckPermission("evacuation:event:view")
    @GetMapping("/events/active")
    public ResponseDTO<List<EvacuationEventEntity>> getActiveEvacuationEvents() {
        List<EvacuationEventEntity> events = evacuationService.getActiveEvacuationEvents();
        return ResponseDTO.ok(events);
    }

    @Operation(summary = "获取疏散事件详情", description = "根据事件ID获取疏散事件详细信息")
    @SaCheckPermission("evacuation:event:view")
    @GetMapping("/events/{eventId}")
    public ResponseDTO<EvacuationEventEntity> getEvacuationEventDetail(@PathVariable Long eventId) {
        EvacuationEventEntity event = evacuationService.getEvacuationEventDetail(eventId);
        return ResponseDTO.ok(event);
    }

    @Operation(summary = "更新疏散事件状态", description = "更新疏散事件的状态")
    @SaCheckPermission("evacuation:event:edit")
    @PutMapping("/events/{eventId}/status")
    public ResponseDTO<Map<String, Object>> updateEventStatus(
            @PathVariable Long eventId, @RequestParam String status) {
        Map<String, Object> result = evacuationService.updateEventStatus(eventId, status);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "结束疏散事件", description = "结束指定的疏散事件")
    @SaCheckPermission("evacuation:event:complete")
    @PostMapping("/events/{eventId}/complete")
    public ResponseDTO<Map<String, Object>> completeEvacuationEvent(
            @PathVariable Long eventId, @RequestParam String endReason) {
        Map<String, Object> result = evacuationService.completeEvacuationEvent(eventId, endReason);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取疏散事件统计", description = "获取疏散事件的统计信息")
    @SaCheckPermission("evacuation:event:view")
    @GetMapping("/events/statistics")
    public ResponseDTO<Map<String, Object>> getEvacuationEventStatistics(
            @RequestParam(required = false) Long eventId) {
        Map<String, Object> statistics = evacuationService.getEvacuationEventStatistics(eventId);
        return ResponseDTO.ok(statistics);
    }

    // ========== 人员疏散管理接口 ==========

    @Operation(summary = "记录人员疏散", description = "记录人员疏散的详细信息")
    @SaCheckPermission("evacuation:record:add")
    @PostMapping("/records")
    public ResponseDTO<Map<String, Object>> recordPersonEvacuation(@Valid @RequestBody EvacuationRecordEntity record) {
        Map<String, Object> result = evacuationService.recordPersonEvacuation(record);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "更新人员到达状态", description = "更新人员到达安全点的状态")
    @SaCheckPermission("evacuation:record:edit")
    @PutMapping("/records/{recordId}/arrival")
    public ResponseDTO<Map<String, Object>> updateArrivalStatus(
            @PathVariable Long recordId, @RequestParam String arrivalTime) {
        Map<String, Object> result = evacuationService.updateArrivalStatus(recordId, arrivalTime);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取疏散记录", description = "根据疏散事件ID获取所有疏散记录")
    @SaCheckPermission("evacuation:record:view")
    @GetMapping("/records/evacuation/{evacuationId}")
    public ResponseDTO<List<EvacuationRecordEntity>> getEvacuationRecords(@PathVariable String evacuationId) {
        List<EvacuationRecordEntity> records = evacuationService.getEvacuationRecords(evacuationId);
        return ResponseDTO.ok(records);
    }

    @Operation(summary = "获取未到达人员", description = "获取指定疏散事件中未到达安全点的人员")
    @SaCheckPermission("evacuation:record:view")
    @GetMapping("/records/missing/{evacuationId}")
    public ResponseDTO<List<Map<String, Object>>> getMissingPeople(@PathVariable String evacuationId) {
        List<Map<String, Object>> missingPeople = evacuationService.getMissingPeople(evacuationId);
        return ResponseDTO.ok(missingPeople);
    }

    @Operation(summary = "获取受伤人员", description = "获取需要医疗救助的人员列表")
    @SaCheckPermission("evacuation:record:view")
    @GetMapping("/records/injured/{evacuationId}")
    public ResponseDTO<List<Map<String, Object>>> getInjuredPeople(@PathVariable String evacuationId) {
        List<Map<String, Object>> injuredPeople = evacuationService.getInjuredPeople(evacuationId);
        return ResponseDTO.ok(injuredPeople);
    }

    @Operation(summary = "批量更新人员状态", description = "批量更新多个人员的疏散状态")
    @SaCheckPermission("evacuation:record:edit")
    @PutMapping("/records/batch-status")
    public ResponseDTO<Map<String, Object>> batchUpdatePersonStatus(
            @RequestParam List<Long> recordIds, @RequestParam String status) {
        Map<String, Object> result = evacuationService.batchUpdatePersonStatus(recordIds, status);
        return ResponseDTO.ok(result);
    }

    // ========== 疏散监控和报告接口 ==========

    @Operation(summary = "获取实时监控数据", description = "获取疏散系统的实时监控数据")
    @SaCheckPermission("evacuation:monitor:view")
    @GetMapping("/monitoring/realtime")
    public ResponseDTO<Map<String, Object>> getRealTimeMonitoringData() {
        Map<String, Object> monitoringData = evacuationService.getRealTimeMonitoringData();
        return ResponseDTO.ok(monitoringData);
    }

    @Operation(summary = "生成疏散报告", description = "生成指定疏散事件的详细报告")
    @SaCheckPermission("evacuation:report:view")
    @GetMapping("/reports/{eventId}")
    public ResponseDTO<Map<String, Object>> generateEvacuationReport(@PathVariable Long eventId) {
        Map<String, Object> report = evacuationService.generateEvacuationReport(eventId);
        return ResponseDTO.ok(report);
    }

    @Operation(summary = "疏散效率分析", description = "分析指定时间段内的疏散效率")
    @SaCheckPermission("evacuation:analysis:view")
    @GetMapping("/analysis/efficiency")
    public ResponseDTO<Map<String, Object>> analyzeEvacuationEfficiency(
            @RequestParam(defaultValue = "30") Integer days) {
        Map<String, Object> analysis = evacuationService.analyzeEvacuationEfficiency(days);
        return ResponseDTO.ok(analysis);
    }

    @Operation(summary = "疏散点利用率报告", description = "获取指定时间段内疏散点的利用率报告")
    @SaCheckPermission("evacuation:report:view")
    @GetMapping("/reports/utilization")
    public ResponseDTO<Map<String, Object>> getPointUtilizationReport(
            @RequestParam String startDate, @RequestParam String endDate) {
        Map<String, Object> report = evacuationService.getPointUtilizationReport(startDate, endDate);
        return ResponseDTO.ok(report);
    }

    @Operation(summary = "模拟疏散演练", description = "执行疏散演练模拟")
    @SaCheckPermission("evacuation:drill:execute")
    @PostMapping("/drills/simulate")
    public ResponseDTO<Map<String, Object>> simulateEvacuationDrill(@RequestBody Map<String, Object> simulationConfig) {
        Map<String, Object> result = evacuationService.simulateEvacuationDrill(simulationConfig);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "检查疏散点健康状态", description = "检查指定疏散点的设备健康状态")
    @SaCheckPermission("evacuation:point:view")
    @GetMapping("/points/{pointId}/health")
    public ResponseDTO<Map<String, Object>> checkPointHealth(@PathVariable Long pointId) {
        Map<String, Object> health = evacuationService.checkPointHealth(pointId);
        return ResponseDTO.ok(health);
    }

    @Operation(summary = "导出疏散数据", description = "导出指定类型的疏散数据")
    @SaCheckPermission("evacuation:export:view")
    @PostMapping("/export")
    public ResponseDTO<Map<String, Object>> exportEvacuationData(
            @RequestParam String exportType, @RequestBody Map<String, Object> queryParams) {
        Map<String, Object> exportData = evacuationService.exportEvacuationData(exportType, queryParams);
        return ResponseDTO.ok(exportData);
    }
}