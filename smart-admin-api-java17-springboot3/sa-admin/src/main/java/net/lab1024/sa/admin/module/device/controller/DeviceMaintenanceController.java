package net.lab1024.sa.admin.module.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.device.service.DeviceMaintenanceService;
import net.lab1024.sa.admin.module.device.domain.vo.DeviceMaintenancePlanVO;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备维护计划控制器
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@RestController
@RequestMapping("/api/device/maintenance")
@Tag(name = "设备维护计划", description = "设备维护计划管理相关接口")
@Validated
@Slf4j
public class DeviceMaintenanceController {

    @Resource
    private DeviceMaintenanceService deviceMaintenanceService;

    @Operation(summary = "获取维护计划详情", description = "根据计划ID获取维护计划的详细信息")
    @GetMapping("/{planId}")
    @SaCheckPermission("device:maintenance:query")
    public ResponseDTO<DeviceMaintenancePlanVO> getMaintenancePlan(
            @Parameter(description = "计划ID", required = true)
            @PathVariable @NotNull(message = "计划ID不能为空") Long planId) {

        return deviceMaintenanceService.getMaintenancePlan(planId);
    }

    @Operation(summary = "获取维护计划列表", description = "分页获取维护计划列表，支持多种筛选条件")
    @GetMapping("/list")
    @SaCheckPermission("device:maintenance:query")
    public ResponseDTO<PageResult<DeviceMaintenancePlanVO>> getMaintenancePlanList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") @Min(1) Integer pageSize,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "计划状态") @RequestParam(required = false) String planStatus,
            @Parameter(description = "指派人ID") @RequestParam(required = false) Long assignedTo) {

        return deviceMaintenanceService.getMaintenancePlanList(pageNum, pageSize, deviceId, planStatus, assignedTo);
    }

    @Operation(summary = "创建维护计划", description = "创建新的设备维护计划")
    @PostMapping
    @SaCheckPermission("device:maintenance:add")
    public ResponseDTO<Long> createMaintenancePlan(
            @Parameter(description = "维护计划信息", required = true)
            @RequestBody @Valid DeviceMaintenancePlanVO maintenancePlan) {

        return deviceMaintenanceService.createMaintenancePlan(maintenancePlan);
    }

    @Operation(summary = "更新维护计划", description = "更新现有的设备维护计划信息")
    @PutMapping("/{planId}")
    @SaCheckPermission("device:maintenance:update")
    public ResponseDTO<Void> updateMaintenancePlan(
            @Parameter(description = "计划ID", required = true)
            @PathVariable @NotNull(message = "计划ID不能为空") Long planId,
            @Parameter(description = "维护计划信息", required = true)
            @RequestBody @Valid DeviceMaintenancePlanVO maintenancePlan) {

        maintenancePlan.setPlanId(planId);
        return deviceMaintenanceService.updateMaintenancePlan(maintenancePlan);
    }

    @Operation(summary = "删除维护计划", description = "删除指定的维护计划（只能删除待处理或已取消状态的计划）")
    @DeleteMapping("/{planId}")
    @SaCheckPermission("device:maintenance:delete")
    public ResponseDTO<Void> deleteMaintenancePlan(
            @Parameter(description = "计划ID", required = true)
            @PathVariable @NotNull(message = "计划ID不能为空") Long planId) {

        return deviceMaintenanceService.deleteMaintenancePlan(planId);
    }

    @Operation(summary = "开始维护计划", description = "将维护计划状态更新为进行中")
    @PostMapping("/{planId}/start")
    @SaCheckPermission("device:maintenance:execute")
    public ResponseDTO<Void> startMaintenancePlan(
            @Parameter(description = "计划ID", required = true)
            @PathVariable @NotNull(message = "计划ID不能为空") Long planId) {

        return deviceMaintenanceService.startMaintenancePlan(planId);
    }

    @Operation(summary = "完成维护计划", description = "标记维护计划为已完成，并记录维护结果")
    @PostMapping("/{planId}/complete")
    @SaCheckPermission("device:maintenance:execute")
    public ResponseDTO<Void> completeMaintenancePlan(
            @Parameter(description = "计划ID", required = true)
            @PathVariable @NotNull(message = "计划ID不能为空") Long planId,
            @Parameter(description = "维护结果备注", required = true)
            @RequestParam @NotNull(message = "维护结果备注不能为空") String resultNote,
            @Parameter(description = "使用的零件") @RequestParam(required = false) String partsUsed,
            @Parameter(description = "维护费用") @RequestParam(required = false) Double costAmount) {

        return deviceMaintenanceService.completeMaintenancePlan(planId, resultNote, partsUsed, costAmount);
    }

    @Operation(summary = "取消维护计划", description = "取消指定的维护计划")
    @PostMapping("/{planId}/cancel")
    @SaCheckPermission("device:maintenance:execute")
    public ResponseDTO<Void> cancelMaintenancePlan(
            @Parameter(description = "计划ID", required = true)
            @PathVariable @NotNull(message = "计划ID不能为空") Long planId,
            @Parameter(description = "取消原因", required = true)
            @RequestParam @NotNull(message = "取消原因不能为空") String reason) {

        return deviceMaintenanceService.cancelMaintenancePlan(planId, reason);
    }

    @Operation(summary = "指派维护计划", description = "将维护计划指派给指定的维护人员")
    @PostMapping("/{planId}/assign")
    @SaCheckPermission("device:maintenance:assign")
    public ResponseDTO<Void> assignMaintenancePlan(
            @Parameter(description = "计划ID", required = true)
            @PathVariable @NotNull(message = "计划ID不能为空") Long planId,
            @Parameter(description = "指派人ID", required = true)
            @RequestParam @NotNull(message = "指派人ID不能为空") Long assignedTo) {

        return deviceMaintenanceService.assignMaintenancePlan(planId, assignedTo);
    }

    @Operation(summary = "获取我的维护任务", description = "获取当前用户的维护任务列表")
    @GetMapping("/my-tasks")
    @SaCheckPermission("device:maintenance:query")
    public ResponseDTO<List<DeviceMaintenancePlanVO>> getMyMaintenanceTasks(
            @Parameter(description = "计划状态筛选") @RequestParam(required = false) String planStatus) {

        // TODO: 从SecurityContext获取当前用户ID
        Long currentUserId = 1L;
        return deviceMaintenanceService.getMyMaintenanceTasks(currentUserId, planStatus);
    }

    @Operation(summary = "获取待处理维护任务", description = "获取所有待处理的维护任务列表")
    @GetMapping("/pending-tasks")
    @SaCheckPermission("device:maintenance:query")
    public ResponseDTO<List<DeviceMaintenancePlanVO>> getPendingMaintenanceTasks() {

        return deviceMaintenanceService.getPendingMaintenanceTasks();
    }

    @Operation(summary = "获取超期维护任务", description = "获取已超期的维护任务列表")
    @GetMapping("/overdue-tasks")
    @SaCheckPermission("device:maintenance:query")
    public ResponseDTO<List<DeviceMaintenancePlanVO>> getOverdueMaintenanceTasks() {

        return deviceMaintenanceService.getOverdueMaintenanceTasks();
    }

    @Operation(summary = "批量指派维护任务", description = "将多个维护任务批量指派给指定人员")
    @PostMapping("/batch-assign")
    @SaCheckPermission("device:maintenance:assign")
    public ResponseDTO<Integer> batchAssignMaintenanceTasks(
            @Parameter(description = "计划ID列表", required = true)
            @RequestBody @NotNull(message = "计划ID列表不能为空") List<Long> planIds,
            @Parameter(description = "指派人ID", required = true)
            @RequestParam @NotNull(message = "指派人ID不能为空") Long assignedTo) {

        if (planIds.isEmpty()) {
            return ResponseDTO.error("计划ID列表不能为空");
        }

        return deviceMaintenanceService.batchAssignMaintenanceTasks(planIds, assignedTo);
    }

    @Operation(summary = "获取维护统计信息", description = "获取指定时间范围内的维护统计信息")
    @GetMapping("/statistics")
    @SaCheckPermission("device:maintenance:query")
    public ResponseDTO<Map<String, Object>> getMaintenanceStatistics(
            @Parameter(description = "开始时间") @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam LocalDateTime endTime) {

        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            return ResponseDTO.error("结束时间不能早于开始时间");
        }

        return deviceMaintenanceService.getMaintenanceStatistics(startTime, endTime);
    }

    @Operation(summary = "获取维护人员工作负载", description = "获取维护人员在指定时间范围内的工作负载统计")
    @GetMapping("/workload")
    @SaCheckPermission("device:maintenance:query")
    public ResponseDTO<List<Map<String, Object>>> getMaintenanceWorkload(
            @Parameter(description = "开始时间") @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam LocalDateTime endTime) {

        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            return ResponseDTO.error("结束时间不能早于开始时间");
        }

        return deviceMaintenanceService.getMaintenanceWorkload(startTime, endTime);
    }

    @Operation(summary = "获取设备维护历史", description = "获取指定设备的维护历史记录")
    @GetMapping("/history/{deviceId}")
    @SaCheckPermission("device:maintenance:query")
    public ResponseDTO<List<DeviceMaintenancePlanVO>> getDeviceMaintenanceHistory(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "记录数量限制") @RequestParam(defaultValue = "10") @Min(1) Integer limit) {

        return deviceMaintenanceService.getDeviceMaintenanceHistory(deviceId, limit);
    }

    @Operation(summary = "导出维护报告", description = "导出维护报告为Excel文件")
    @PostMapping("/export")
    @SaCheckPermission("device:maintenance:export")
    public ResponseDTO<String> exportMaintenanceReport(
            @Parameter(description = "开始时间") @RequestParam @NotNull(message = "开始时间不能为空") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @NotNull(message = "结束时间不能为空") LocalDateTime endTime,
            @Parameter(description = "指派人筛选") @RequestParam(required = false) Long assignedTo,
            HttpServletResponse response) {

        if (endTime.isBefore(startTime)) {
            return ResponseDTO.error("结束时间不能早于开始时间");
        }

        ResponseDTO<String> exportResult = deviceMaintenanceService.exportMaintenanceReport(startTime, endTime, assignedTo);

        if (exportResult.isOk()) {
            // 设置下载响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=device_maintenance_report.xlsx");
        }

        return exportResult;
    }

    @Operation(summary = "获取维护建议", description = "获取AI生成的设备维护建议")
    @GetMapping("/suggestions/{deviceId}")
    @SaCheckPermission("device:maintenance:query")
    public ResponseDTO<List<String>> getMaintenanceSuggestions(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull(message = "设备ID不能为空") Long deviceId) {

        return deviceMaintenanceService.getMaintenanceSuggestions(deviceId);
    }
}