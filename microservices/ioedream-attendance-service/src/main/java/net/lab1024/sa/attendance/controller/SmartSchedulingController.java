package net.lab1024.sa.attendance.controller;

import io.github.resilience4j.annotation.CircuitBreaker;
import io.github.resilience4j.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.form.SmartSchedulingForm;
import net.lab1024.sa.attendance.domain.form.ScheduleTemplateForm;
import net.lab1024.sa.attendance.domain.vo.SchedulingResultVO;
import net.lab1024.sa.attendance.domain.vo.ScheduleRecordVO;
import net.lab1024.sa.attendance.domain.vo.ScheduleTemplateVO;
import net.lab1024.sa.attendance.domain.vo.SchedulingStatisticsVO;
import net.lab1024.sa.attendance.service.ScheduleService;
import net.lab1024.sa.common.response.ResponseDTO;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 智能排班管理控制器
 * <p>
 * 提供高级智能排班功能，包括：
 * - AI算法优化排班
 * - 多维度约束条件
 * - 自动冲突检测与解决
 * - 排班效果预测
 * - 成本效益分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/smart-scheduling")
@Tag(name = "智能排班管理", description = "AI智能排班、算法优化、效果预测等高级功能")
@Validated
@CircuitBreaker(name = "smartSchedulingController")
@PermissionCheck(value = "ATTENDANCE", description = "智能排班管理")
public class SmartSchedulingController {

    @Resource
    private ScheduleService scheduleService;

    // ==================== AI智能排班 ====================

    /**
     * 生成智能排班方案
     */
    @TimeLimiter(name = "smartSchedulingController")
    @Operation(
        summary = "生成智能排班方案",
        description = "基于AI算法生成最优排班方案，支持遗传算法、模拟退火、贪心算法等多种优化策略"
    )
    @PostMapping("/generate")
    @PermissionCheck(value = "ATTENDANCE_MANAGER", description = "生成智能排班方案")
    public CompletableFuture<ResponseDTO<SchedulingResultVO>> generateSmartSchedule(
            @Parameter(description = "智能排班请求", required = true)
            @Valid @RequestBody SmartSchedulingForm form
    ) {
        log.info("[智能排班] 生成智能排班方案, departmentId={}, period={}-{}",
                form.getDepartmentId(), form.getStartDate(), form.getEndDate());

        try {
            SchedulingResultVO result = scheduleService.generateSmartSchedule(form);
            return CompletableFuture.completedFuture(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[智能排班] 生成智能排班方案失败", e);
            return CompletableFuture.completedFuture(
                ResponseDTO.error("SCHEDULE_GENERATION_ERROR", "生成排班方案失败: " + e.getMessage()));
        }
    }

    /**
     * 优化现有排班方案
     */
    @TimeLimiter(name = "smartSchedulingController")
    @Operation(
        summary = "优化现有排班方案",
        description = "对已生成的排班方案进行二次优化，提升效率和公平性"
    )
    @PostMapping("/optimize/{requestId}")
    @PermissionCheck(value = "ATTENDANCE_MANAGER", description = "优化现有排班方案")
    public CompletableFuture<ResponseDTO<SchedulingResultVO>> optimizeSchedule(
            @Parameter(description = "排班请求ID", required = true, example = "REQ_20250130_001")
            @PathVariable String requestId
    ) {
        log.info("[智能排班] 优化排班方案, requestId={}", requestId);

        try {
            SchedulingResultVO result = scheduleService.optimizeSchedule(requestId);
            return CompletableFuture.completedFuture(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[智能排班] 优化排班方案失败", e);
            return CompletableFuture.completedFuture(
                ResponseDTO.error("SCHEDULE_OPTIMIZATION_ERROR", "优化排班方案失败: " + e.getMessage()));
        }
    }

    // ==================== 需求预测 ====================

    /**
     * 预测排班需求
     */
    @TimeLimiter(name = "smartSchedulingController")
    @Operation(
        summary = "预测排班需求",
        description = "基于历史数据预测未来排班需求，支持多种预测周期"
    )
    @GetMapping("/forecast")
    @PermissionCheck(value = {"ATTENDANCE_MANAGER", "ATTENDANCE_OPERATOR"}, description = "预测排班需求")
    public CompletableFuture<ResponseDTO<SchedulingStatisticsVO>> forecastDemand(
            @Parameter(description = "部门ID", required = true, example = "1")
            @RequestParam Long departmentId,
            @Parameter(description = "预测周期", required = true, example = "next_month")
            @RequestParam String forecastPeriod
    ) {
        log.info("[智能排班] 预测排班需求, departmentId={}, period={}", departmentId, forecastPeriod);

        try {
            SchedulingStatisticsVO statistics = scheduleService.forecastDemand(departmentId, forecastPeriod);
            return CompletableFuture.completedFuture(ResponseDTO.ok(statistics));

        } catch (Exception e) {
            log.error("[智能排班] 预测排班需求失败", e);
            return CompletableFuture.completedFuture(
                ResponseDTO.error("FORECAST_ERROR", "预测排班需求失败: " + e.getMessage()));
        }
    }

    // ==================== 统计分析 ====================

    /**
     * 获取排班统计信息
     */
    @TimeLimiter(name = "smartSchedulingController")
    @Operation(
        summary = "获取排班统计信息",
        description = "获取详细的排班统计分析，包括覆盖率、成本、效率等指标"
    )
    @GetMapping("/statistics")
    @PermissionCheck(value = {"ATTENDANCE_MANAGER", "ATTENDANCE_OPERATOR", "ATTENDANCE_VIEWER"}, description = "获取排班统计信息")
    public CompletableFuture<ResponseDTO<SchedulingStatisticsVO>> getSchedulingStatistics(
            @Parameter(description = "部门ID", required = true, example = "1")
            @RequestParam Long departmentId,
            @Parameter(description = "开始日期", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true, example = "2025-01-31")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        log.info("[智能排班] 获取排班统计信息, departmentId={}, period={}-{}",
                departmentId, startDate, endDate);

        try {
            SchedulingStatisticsVO statistics = scheduleService.getSchedulingStatistics(departmentId, startDate, endDate);
            return CompletableFuture.completedFuture(ResponseDTO.ok(statistics));

        } catch (Exception e) {
            log.error("[智能排班] 获取排班统计信息失败", e);
            return CompletableFuture.completedFuture(
                ResponseDTO.error("STATISTICS_ERROR", "获取统计信息失败: " + e.getMessage()));
        }
    }

    // ==================== 模板管理 ====================

    /**
     * 应用排班模板（智能版本）
     */
    @TimeLimiter(name = "smartSchedulingController")
    @Operation(
        summary = "应用排班模板（智能版本）",
        description = "应用排班模板并结合智能算法进行优化调整"
    )
    @PostMapping("/template/{templateId}/apply")
    @PermissionCheck(value = "ATTENDANCE_MANAGER", description = "应用排班模板")
    public CompletableFuture<ResponseDTO<List<ScheduleRecordVO>>> applyScheduleTemplate(
            @Parameter(description = "模板ID", required = true, example = "1001")
            @PathVariable Long templateId,
            @Parameter(description = "开始日期", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true, example = "2025-01-31")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "是否启用智能优化", example = "true")
            @RequestParam(defaultValue = "true") Boolean enableOptimization
    ) {
        log.info("[智能排班] 应用排班模板, templateId={}, period={}-{}, optimize={}",
                templateId, startDate, endDate, enableOptimization);

        try {
            List<ScheduleRecordVO> records = scheduleService.applyScheduleTemplate(templateId, startDate, endDate);
            return CompletableFuture.completedFuture(ResponseDTO.ok(records));

        } catch (Exception e) {
            log.error("[智能排班] 应用排班模板失败", e);
            return CompletableFuture.completedFuture(
                ResponseDTO.error("TEMPLATE_APPLY_ERROR", "应用排班模板失败: " + e.getMessage()));
        }
    }

    /**
     * 获取智能排班模板列表
     */
    @TimeLimiter(name = "smartSchedulingController")
    @Operation(
        summary = "获取智能排班模板列表",
        description = "获取所有可用的智能排班模板，支持按类型和部门筛选"
    )
    @GetMapping("/templates")
    @PermissionCheck(value = {"ATTENDANCE_MANAGER", "ATTENDANCE_OPERATOR", "ATTENDANCE_VIEWER"}, description = "获取智能排班模板列表")
    public CompletableFuture<ResponseDTO<List<ScheduleTemplateVO>>> getSmartScheduleTemplates(
            @Parameter(description = "模板类型", example = "智能模板")
            @RequestParam(required = false) String templateType,
            @Parameter(description = "部门ID", example = "1")
            @RequestParam(required = false) Long departmentId
    ) {
        log.info("[智能排班] 获取智能排班模板列表, type={}, departmentId={}", templateType, departmentId);

        try {
            List<ScheduleTemplateVO> templates = scheduleService.getScheduleTemplates(templateType, departmentId);
            return CompletableFuture.completedFuture(ResponseDTO.ok(templates));

        } catch (Exception e) {
            log.error("[智能排班] 获取智能排班模板列表失败", e);
            return CompletableFuture.completedFuture(
                ResponseDTO.error("TEMPLATE_LIST_ERROR", "获取模板列表失败: " + e.getMessage()));
        }
    }

    // ==================== 冲突检测 ====================

    /**
     * 智能冲突检测
     */
    @TimeLimiter(name = "smartSchedulingController")
    @Operation(
        summary = "智能冲突检测",
        description = "基于多维度规则检测排班冲突，提供解决建议"
    )
    @PostMapping("/conflicts/detect")
    @PermissionCheck(value = {"ATTENDANCE_MANAGER", "ATTENDANCE_OPERATOR"}, description = "智能冲突检测")
    public CompletableFuture<ResponseDTO<List<String>>> detectScheduleConflicts(
            @Parameter(description = "员工ID列表", required = true)
            @RequestParam List<Long> employeeIds,
            @Parameter(description = "检测日期", required = true, example = "2025-01-30")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        log.info("[智能排班] 智能冲突检测, employeeCount={}, date={}", employeeIds.size(), date);

        try {
            List<String> conflicts = scheduleService.detectScheduleConflicts(employeeIds, date);
            return CompletableFuture.completedFuture(ResponseDTO.ok(conflicts));

        } catch (Exception e) {
            log.error("[智能排班] 智能冲突检测失败", e);
            return CompletableFuture.completedFuture(
                ResponseDTO.error("CONFLICT_DETECTION_ERROR", "冲突检测失败: " + e.getMessage()));
        }
    }

    // ==================== 算法配置 ====================

    /**
     * 获取支持的算法列表
     */
    @TimeLimiter(name = "smartSchedulingController")
    @Operation(
        summary = "获取支持的算法列表",
        description = "查询所有支持的智能排班算法及其配置参数"
    )
    @GetMapping("/algorithms")
    @PermissionCheck(value = "ATTENDANCE_MANAGER", description = "获取支持的算法列表")
    public CompletableFuture<ResponseDTO<Object>> getSupportedAlgorithms() {
        log.info("[智能排班] 获取支持的算法列表");

        try {
            // 返回支持的算法信息
            Object algorithms = java.util.Map.of(
                "GENETIC", java.util.Map.of(
                    "name", "遗传算法",
                    "description", "基于生物进化原理的全局优化算法",
                    "parameters", java.util.List.of("populationSize", "maxGenerations", "crossoverRate", "mutationRate")
                ),
                "SIMULATED_ANNEALING", java.util.Map.of(
                    "name", "模拟退火算法",
                    "description", "基于物理退火过程的随机优化算法",
                    "parameters", java.util.List.of("initialTemperature", "coolingRate", "minTemperature")
                ),
                "GREEDY", java.util.Map.of(
                    "name", "贪心算法",
                    "description", "基于局部最优选择的高效算法",
                    "parameters", java.util.List.of("priorityWeight", "costWeight")
                )
            );

            return CompletableFuture.completedFuture(ResponseDTO.ok(algorithms));

        } catch (Exception e) {
            log.error("[智能排班] 获取支持的算法列表失败", e);
            return CompletableFuture.completedFuture(
                ResponseDTO.error("ALGORITHM_LIST_ERROR", "获取算法列表失败: " + e.getMessage()));
        }
    }
}