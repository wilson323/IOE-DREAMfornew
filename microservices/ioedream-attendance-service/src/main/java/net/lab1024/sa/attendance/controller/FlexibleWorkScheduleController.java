package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.form.FlexibleWorkScheduleForm;
import net.lab1024.sa.attendance.domain.vo.FlexibleWorkScheduleDetailVO;
import net.lab1024.sa.attendance.domain.vo.FlexibleWorkScheduleVO;
import net.lab1024.sa.attendance.service.FlexibleWorkScheduleService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 弹性工作制管理控制器
 * <p>
 * 提供弹性工作制的完整管理API
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/flexible-schedule")
@Tag(name = "弹性工作制管理")
public class FlexibleWorkScheduleController {

    @Resource
    private FlexibleWorkScheduleService flexibleWorkScheduleService;

    // ==================== 弹性工作制配置管理 ====================

    /**
     * 创建弹性工作制配置
     *
     * @param form 弹性工作制配置表单
     * @return 配置ID
     */
    @PostMapping
    @Operation(summary = "创建弹性工作制配置", description = "创建新的弹性工作制配置")
    public ResponseDTO<Long> createFlexibleSchedule(@Valid @RequestBody FlexibleWorkScheduleForm form) {
        log.info("[弹性工作制] 创建配置: scheduleName={}, flexMode={}", form.getScheduleName(), form.getFlexMode());
        Long scheduleId = flexibleWorkScheduleService.createFlexibleSchedule(form);
        log.info("[弹性工作制] 创建配置成功: scheduleId={}", scheduleId);
        return ResponseDTO.ok(scheduleId);
    }

    /**
     * 更新弹性工作制配置
     *
     * @param scheduleId 配置ID
     * @param form 弹性工作制配置表单
     * @return 是否成功
     */
    @PutMapping("/{scheduleId}")
    @Operation(summary = "更新弹性工作制配置", description = "更新指定的弹性工作制配置")
    public ResponseDTO<Void> updateFlexibleSchedule(
            @PathVariable @Parameter(description = "配置ID", required = true) Long scheduleId,
            @Valid @RequestBody FlexibleWorkScheduleForm form) {
        log.info("[弹性工作制] 更新配置: scheduleId={}", scheduleId);
        Boolean result = flexibleWorkScheduleService.updateFlexibleSchedule(scheduleId, form);
        log.info("[弹性工作制] 更新配置成功: scheduleId={}, result={}", scheduleId, result);
        return ResponseDTO.ok();
    }

    /**
     * 删除弹性工作制配置
     *
     * @param scheduleId 配置ID
     * @return 是否成功
     */
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "删除弹性工作制配置", description = "删除指定的弹性工作制配置")
    public ResponseDTO<Void> deleteFlexibleSchedule(
            @PathVariable @Parameter(description = "配置ID", required = true) Long scheduleId) {
        log.info("[弹性工作制] 删除配置: scheduleId={}", scheduleId);
        Boolean result = flexibleWorkScheduleService.deleteFlexibleSchedule(scheduleId);
        log.info("[弹性工作制] 删除配置成功: scheduleId={}, result={}", scheduleId, result);
        return ResponseDTO.ok();
    }

    /**
     * 获取弹性工作制配置详情
     *
     * @param scheduleId 配置ID
     * @return 配置详情
     */
    @GetMapping("/{scheduleId}")
    @Operation(summary = "获取弹性工作制配置详情", description = "查询指定弹性工作制配置的详细信息")
    public ResponseDTO<FlexibleWorkScheduleDetailVO> getFlexibleScheduleDetail(
            @PathVariable @Parameter(description = "配置ID", required = true) Long scheduleId) {
        log.info("[弹性工作制] 查询配置详情: scheduleId={}", scheduleId);
        FlexibleWorkScheduleDetailVO detailVO = flexibleWorkScheduleService.getFlexibleScheduleDetail(scheduleId);
        log.info("[弹性工作制] 查询配置详情成功: scheduleId={}", scheduleId);
        return ResponseDTO.ok(detailVO);
    }

    /**
     * 分页查询弹性工作制配置
     *
     * @param form 查询表单
     * @return 分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询弹性工作制配置", description = "分页查询弹性工作制配置列表")
    public ResponseDTO<PageResult<FlexibleWorkScheduleVO>> queryFlexibleSchedules(@RequestBody FlexibleWorkScheduleForm form) {
        log.info("[弹性工作制] 分页查询配置: scheduleName={}", form.getScheduleName());
        PageResult<FlexibleWorkScheduleVO> pageResult = flexibleWorkScheduleService.queryFlexibleSchedules(form);
        log.info("[弹性工作制] 分页查询配置成功: total={}", pageResult.getTotal());
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 获取所有启用的弹性工作制配置
     *
     * @return 配置列表
     */
    @GetMapping("/list/active")
    @Operation(summary = "获取所有启用的配置", description = "查询所有启用的弹性工作制配置列表")
    public ResponseDTO<List<FlexibleWorkScheduleVO>> getAllActiveSchedules() {
        log.info("[弹性工作制] 查询所有启用的配置");
        List<FlexibleWorkScheduleVO> list = flexibleWorkScheduleService.getAllActiveSchedules();
        log.info("[弹性工作制] 查询所有启用的配置成功: count={}", list.size());
        return ResponseDTO.ok(list);
    }

    // ==================== 弹性时间计算 ====================

    /**
     * 计算弹性工作制考勤状态
     *
     * @param scheduleId 配置ID
     * @param employeeId 员工ID
     * @param attendanceDate 考勤日期
     * @param checkInTime 实际签到时间
     * @param checkOutTime 实际签退时间
     * @return 考勤结果
     */
    @PostMapping("/calculate-status")
    @Operation(summary = "计算考勤状态", description = "根据弹性工作制计算考勤状态")
    public ResponseDTO<String> calculateAttendanceStatus(
            @Parameter(description = "配置ID", required = true) @RequestParam Long scheduleId,
            @Parameter(description = "员工ID", required = true) @RequestParam Long employeeId,
            @Parameter(description = "考勤日期", required = true) @RequestParam LocalDateTime attendanceDate,
            @Parameter(description = "签到时间", required = true) @RequestParam LocalDateTime checkInTime,
            @Parameter(description = "签退时间", required = true) @RequestParam LocalDateTime checkOutTime) {
        log.info("[弹性工作制] 计算考勤状态: scheduleId={}, employeeId={}, date={}", scheduleId, employeeId, attendanceDate);
        String status = flexibleWorkScheduleService.calculateAttendanceStatus(scheduleId, employeeId, attendanceDate, checkInTime, checkOutTime);
        log.info("[弹性工作制] 计算考勤状态成功: scheduleId={}, employeeId={}, status={}", scheduleId, employeeId, status);
        return ResponseDTO.ok(status);
    }

    /**
     * 验证弹性工作时间是否合规
     *
     * @param scheduleId 配置ID
     * @param checkInTime 签到时间
     * @param checkOutTime 签退时间
     * @return 验证结果
     */
    @PostMapping("/validate-time")
    @Operation(summary = "验证弹性工作时间", description = "验证签到签退时间是否符合弹性工作制要求")
    public ResponseDTO<Boolean> validateFlexibleTime(
            @Parameter(description = "配置ID", required = true) @RequestParam Long scheduleId,
            @Parameter(description = "签到时间", required = true) @RequestParam LocalDateTime checkInTime,
            @Parameter(description = "签退时间", required = true) @RequestParam LocalDateTime checkOutTime) {
        log.info("[弹性工作制] 验证弹性时间: scheduleId={}, checkIn={}, checkOut={}", scheduleId, checkInTime, checkOutTime);
        Boolean isValid = flexibleWorkScheduleService.validateFlexibleTime(scheduleId, checkInTime, checkOutTime);
        log.info("[弹性工作制] 验证弹性时间成功: scheduleId={}, isValid={}", scheduleId, isValid);
        return ResponseDTO.ok(isValid);
    }

    /**
     * 计算弹性工作时长
     *
     * @param scheduleId 配置ID
     * @param checkInTime 签到时间
     * @param checkOutTime 签退时间
     * @return 工作时长（分钟）
     */
    @PostMapping("/calculate-duration")
    @Operation(summary = "计算弹性工作时长", description = "根据弹性工作制计算实际工作时长")
    public ResponseDTO<Integer> calculateWorkDuration(
            @Parameter(description = "配置ID", required = true) @RequestParam Long scheduleId,
            @Parameter(description = "签到时间", required = true) @RequestParam LocalDateTime checkInTime,
            @Parameter(description = "签退时间", required = true) @RequestParam LocalDateTime checkOutTime) {
        log.info("[弹性工作制] 计算工作时长: scheduleId={}, checkIn={}, checkOut={}", scheduleId, checkInTime, checkOutTime);
        Integer duration = flexibleWorkScheduleService.calculateWorkDuration(scheduleId, checkInTime, checkOutTime);
        log.info("[弹性工作制] 计算工作时长成功: scheduleId={}, duration={}minutes", scheduleId, duration);
        return ResponseDTO.ok(duration);
    }

    // ==================== 弹性模式管理 ====================

    /**
     * 切换弹性工作模式
     *
     * @param scheduleId 配置ID
     * @param flexMode 目标弹性模式
     * @return 是否成功
     */
    @PutMapping("/{scheduleId}/flex-mode")
    @Operation(summary = "切换弹性工作模式", description = "切换弹性工作制的模式（STANDARD/FLEXIBLE/HYBRID）")
    public ResponseDTO<Void> switchFlexMode(
            @PathVariable @Parameter(description = "配置ID", required = true) Long scheduleId,
            @Parameter(description = "弹性模式（STANDARD/FLEXIBLE/HYBRID）", required = true) @RequestParam String flexMode) {
        log.info("[弹性工作制] 切换弹性模式: scheduleId={}, flexMode={}", scheduleId, flexMode);
        Boolean result = flexibleWorkScheduleService.switchFlexMode(scheduleId, flexMode);
        log.info("[弹性工作制] 切换弹性模式成功: scheduleId={}, flexMode={}, result={}", scheduleId, flexMode, result);
        return ResponseDTO.ok();
    }

    /**
     * 启用弹性工作制
     *
     * @param scheduleId 配置ID
     * @return 是否成功
     */
    @PutMapping("/{scheduleId}/enable")
    @Operation(summary = "启用弹性工作制", description = "启用指定的弹性工作制配置")
    public ResponseDTO<Void> enableFlexibleSchedule(
            @PathVariable @Parameter(description = "配置ID", required = true) Long scheduleId) {
        log.info("[弹性工作制] 启用配置: scheduleId={}", scheduleId);
        Boolean result = flexibleWorkScheduleService.enableFlexibleSchedule(scheduleId);
        log.info("[弹性工作制] 启用配置成功: scheduleId={}, result={}", scheduleId, result);
        return ResponseDTO.ok();
    }

    /**
     * 禁用弹性工作制
     *
     * @param scheduleId 配置ID
     * @return 是否成功
     */
    @PutMapping("/{scheduleId}/disable")
    @Operation(summary = "禁用弹性工作制", description = "禁用指定的弹性工作制配置")
    public ResponseDTO<Void> disableFlexibleSchedule(
            @PathVariable @Parameter(description = "配置ID", required = true) Long scheduleId) {
        log.info("[弹性工作制] 禁用配置: scheduleId={}", scheduleId);
        Boolean result = flexibleWorkScheduleService.disableFlexibleSchedule(scheduleId);
        log.info("[弹性工作制] 禁用配置成功: scheduleId={}, result={}", scheduleId, result);
        return ResponseDTO.ok();
    }

    // ==================== 员工弹性工作制分配 ====================

    /**
     * 为员工分配弹性工作制
     *
     * @param scheduleId 配置ID
     * @param employeeIds 员工ID列表
     * @param effectiveTime 生效时间
     * @return 分配的员工数量
     */
    @PostMapping("/{scheduleId}/assign")
    @Operation(summary = "为员工分配弹性工作制", description = "批量为员工分配弹性工作制配置")
    public ResponseDTO<Integer> assignToEmployees(
            @PathVariable @Parameter(description = "配置ID", required = true) Long scheduleId,
            @Parameter(description = "员工ID列表", required = true) @RequestParam List<Long> employeeIds,
            @Parameter(description = "生效时间", required = true) @RequestParam LocalDateTime effectiveTime) {
        log.info("[弹性工作制] 为员工分配配置: scheduleId={}, employeeCount={}, effectiveTime={}", scheduleId, employeeIds.size(), effectiveTime);
        Integer count = flexibleWorkScheduleService.assignToEmployees(scheduleId, employeeIds, effectiveTime);
        log.info("[弹性工作制] 为员工分配配置成功: scheduleId={}, assignedCount={}", scheduleId, count);
        return ResponseDTO.ok(count);
    }

    /**
     * 批量移除员工的弹性工作制
     *
     * @param scheduleId 配置ID
     * @param employeeIds 员工ID列表
     * @return 移除的员工数量
     */
    @DeleteMapping("/{scheduleId}/employees")
    @Operation(summary = "移除员工弹性工作制", description = "批量移除员工的弹性工作制配置")
    public ResponseDTO<Integer> removeFromEmployees(
            @PathVariable @Parameter(description = "配置ID", required = true) Long scheduleId,
            @Parameter(description = "员工ID列表", required = true) @RequestParam List<Long> employeeIds) {
        log.info("[弹性工作制] 移除员工配置: scheduleId={}, employeeCount={}", scheduleId, employeeIds.size());
        Integer count = flexibleWorkScheduleService.removeFromEmployees(scheduleId, employeeIds);
        log.info("[弹性工作制] 移除员工配置成功: scheduleId={}, removedCount={}", scheduleId, count);
        return ResponseDTO.ok(count);
    }

    /**
     * 获取员工的弹性工作制配置
     *
     * @param employeeId 员工ID
     * @param effectiveDate 生效日期
     * @return 弹性工作制配置
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "获取员工弹性工作制", description = "查询员工的弹性工作制配置")
    public ResponseDTO<FlexibleWorkScheduleVO> getEmployeeSchedule(
            @PathVariable @Parameter(description = "员工ID", required = true) Long employeeId,
            @Parameter(description = "生效日期", required = false) @RequestParam(required = false) LocalDateTime effectiveDate) {
        log.info("[弹性工作制] 查询员工配置: employeeId={}, effectiveDate={}", employeeId, effectiveDate);
        if (effectiveDate == null) {
            effectiveDate = LocalDateTime.now();
        }
        FlexibleWorkScheduleVO scheduleVO = flexibleWorkScheduleService.getEmployeeSchedule(employeeId, effectiveDate);
        log.info("[弹性工作制] 查询员工配置成功: employeeId={}", employeeId);
        return ResponseDTO.ok(scheduleVO);
    }

    // ==================== 统计分析 ====================

    /**
     * 获取弹性工作制使用统计
     *
     * @param scheduleId 配置ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    @GetMapping("/{scheduleId}/statistics")
    @Operation(summary = "获取弹性工作制统计", description = "获取弹性工作制的使用统计数据")
    public ResponseDTO<String> getScheduleStatistics(
            @PathVariable @Parameter(description = "配置ID", required = true) Long scheduleId,
            @Parameter(description = "开始日期", required = true) @RequestParam LocalDateTime startDate,
            @Parameter(description = "结束日期", required = true) @RequestParam LocalDateTime endDate) {
        log.info("[弹性工作制] 获取统计: scheduleId={}, startDate={}, endDate={}", scheduleId, startDate, endDate);
        String statistics = flexibleWorkScheduleService.getScheduleStatistics(scheduleId, startDate, endDate);
        log.info("[弹性工作制] 获取统计成功: scheduleId={}", scheduleId);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取弹性工作制对比分析
     *
     * @param scheduleIds 配置ID列表
     * @return 对比分析数据
     */
    @PostMapping("/compare")
    @Operation(summary = "对比弹性工作制", description = "对比多个弹性工作制配置的差异")
    public ResponseDTO<String> compareSchedules(
            @Parameter(description = "配置ID列表", required = true) @RequestParam List<Long> scheduleIds) {
        log.info("[弹性工作制] 对比配置: scheduleIds={}", scheduleIds);
        String comparison = flexibleWorkScheduleService.compareSchedules(scheduleIds);
        log.info("[弹性工作制] 对比配置成功: count={}", scheduleIds.size());
        return ResponseDTO.ok(comparison);
    }
}
