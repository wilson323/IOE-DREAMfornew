package net.lab1024.sa.attendance.controller;

import io.github.resilience4j.annotation.CircuitBreaker;
import io.github.resilience4j.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.model.*;
import net.lab1024.sa.common.dto.ResponseDTO;

import org.springframework.format.annotation.DateTimeFormat;
import net.lab1024.sa.common.permission.PermissionCheck;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 考勤排班管理控制器
 * <p>
 * 内存优化设计：
 * - 使用异步处理，提高并发性能
 * - 合理的参数验证，避免内存溢出
 * - 熔断器保护，防止级联故障
 * - 分页参数限制，避免大数据量传输
 * </p>
 * <p>
 * 业务场景：
 * - 智能排班算法执行
 * - 排班冲突检测与解决
 * - 排班计划生成与管理
 * - 班次模板管理
 * - 临时排班调整
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/schedule")
@Tag(name = "考勤排班管理", description = "智能排班、冲突检测、计划生成、模板管理等API")
@Validated
@CircuitBreaker(name = "scheduleController")
@PermissionCheck(description = "考勤排班管理")
public class ScheduleController {

    @Resource
    private ScheduleEngine scheduleEngine;

    // ==================== 智能排班 ====================

    /**
     * 执行智能排班
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "执行智能排班",
        description = "基于AI算法执行智能排班，支持多种排班策略"
    )
    @PostMapping("/intelligent")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "执行智能排班")
    public CompletableFuture<ResponseDTO<ScheduleResult>> executeIntelligentSchedule(
            @Parameter(description = "排班请求", required = true)
            @Valid @RequestBody ScheduleRequest request
    ) {
        log.info("[智能排班] 执行智能排班, request={}", request);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.executeIntelligentSchedule(request)));
    }

    /**
     * 生成排班计划
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "生成排班计划",
        description = "为指定时间段生成完整的排班计划"
    )
    @PostMapping("/plan")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "生成排班计划")
    public CompletableFuture<ResponseDTO<SchedulePlan>> generateSchedulePlan(
            @Parameter(description = "计划ID", required = true, example = "1001")
            @RequestParam @NotNull Long planId,
            @Parameter(description = "开始日期", required = true, example = "2025-01-01")
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true, example = "2025-01-31")
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        log.info("[智能排班] 生成排班计划, planId={}, startDate={}, endDate={}", planId, startDate, endDate);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.generateSchedulePlan(planId, startDate, endDate)));
    }

    // ==================== 冲突检测 ====================

    /**
     * 验证排班冲突
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "验证排班冲突",
        description = "检测排班中的冲突问题，包括时间冲突、人员冲突等"
    )
    @PostMapping("/conflicts/validate")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR"}, description = "验证排班冲突")
    public CompletableFuture<ResponseDTO<ConflictDetectionResult>> validateScheduleConflicts(
            @Parameter(description = "排班数据", required = true)
            @Valid @RequestBody ScheduleData scheduleData
    ) {
        log.info("[智能排班] 验证排班冲突, scheduleData={}", scheduleData);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.validateScheduleConflicts(scheduleData)));
    }

    /**
     * 解决排班冲突
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "解决排班冲突",
        description = "自动解决排班中的冲突问题"
    )
    @PostMapping("/conflicts/resolve")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<ScheduleData>> resolveScheduleConflicts(
            @Parameter(description = "冲突列表", required = true)
            @RequestBody @NotNull List<ScheduleConflict> conflicts,
            @Parameter(description = "解决策略", example = "auto")
            @RequestParam(defaultValue = "auto") String strategy
    ) {
        log.info("[智能排班] 解决排班冲突, conflicts={}, strategy={}", conflicts.size(), strategy);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.resolveScheduleConflicts(conflicts, strategy)));
    }

    // ==================== 排班模板管理 ====================

    /**
     * 创建排班模板
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "创建排班模板",
        description = "创建可复用的排班模板"
    )
    @PostMapping("/template")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<ScheduleTemplate>> createScheduleTemplate(
            @Parameter(description = "模板数据", required = true)
            @Valid @RequestBody ScheduleTemplate template
    ) {
        log.info("[智能排班] 创建排班模板, template={}", template);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.createScheduleTemplate(template)));
    }

    /**
     * 获取排班模板列表
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "获取排班模板列表",
        description = "查询所有可用的排班模板"
    )
    @GetMapping("/templates")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR", "ATTENDANCE_VIEWER"}, description = "排班查看操作")
    public CompletableFuture<ResponseDTO<List<ScheduleTemplate>>> getScheduleTemplates() {
        log.info("[智能排班] 获取排班模板列表");
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.getScheduleTemplates()));
    }

    /**
     * 应用排班模板
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "应用排班模板",
        description = "将模板应用到指定日期范围"
    )
    @PostMapping("/template/{templateId}/apply")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<ScheduleResult>> applyScheduleTemplate(
            @Parameter(description = "模板ID", required = true, example = "1001")
            @PathVariable Long templateId,
            @Parameter(description = "开始日期", required = true)
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true)
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "目标人员ID列表")
            @RequestParam(required = false) List<Long> targetUserIds
    ) {
        log.info("[智能排班] 应用排班模板, templateId={}, startDate={}, endDate={}, targetUserIds={}",
                 templateId, startDate, endDate, targetUserIds);
        return CompletableFuture.completedFuture(ResponseDTO.ok(
                scheduleEngine.applyScheduleTemplate(templateId, startDate, endDate, targetUserIds)));
    }

    // ==================== 班次管理 ====================

    /**
     * 创建班次
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "创建班次",
        description = "创建新的班次定义"
    )
    @PostMapping("/shift")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<ShiftDefinition>> createShift(
            @Parameter(description = "班次定义", required = true)
            @Valid @RequestBody ShiftDefinition shift
    ) {
        log.info("[智能排班] 创建班次, shift={}", shift);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.createShift(shift)));
    }

    /**
     * 获取班次列表
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "获取班次列表",
        description = "查询所有可用的班次定义"
    )
    @GetMapping("/shifts")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR", "ATTENDANCE_VIEWER"}, description = "排班查看操作")
    public CompletableFuture<ResponseDTO<List<ShiftDefinition>>> getShifts() {
        log.info("[智能排班] 获取班次列表");
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.getShifts()));
    }

    /**
     * 更新班次
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "更新班次",
        description = "更新班次定义"
    )
    @PutMapping("/shift/{shiftId}")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<ShiftDefinition>> updateShift(
            @Parameter(description = "班次ID", required = true, example = "1001")
            @PathVariable Long shiftId,
            @Parameter(description = "班次定义", required = true)
            @Valid @RequestBody ShiftDefinition shift
    ) {
        log.info("[智能排班] 更新班次, shiftId={}, shift={}", shiftId, shift);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.updateShift(shiftId, shift)));
    }

    /**
     * 删除班次
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "删除班次",
        description = "删除指定的班次定义"
    )
    @DeleteMapping("/shift/{shiftId}")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<Void>> deleteShift(
            @Parameter(description = "班次ID", required = true, example = "1001")
            @PathVariable Long shiftId
    ) {
        log.info("[智能排班] 删除班次, shiftId={}", shiftId);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.deleteShift(shiftId)));
    }

    // ==================== 临时排班 ====================

    /**
     * 创建临时排班
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "创建临时排班",
        description = "为特殊情况创建临时排班调整"
    )
    @PostMapping("/temporary")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<ScheduleResult>> createTemporarySchedule(
            @Parameter(description = "临时排班请求", required = true)
            @Valid @RequestBody TemporaryScheduleRequest request
    ) {
        log.info("[智能排班] 创建临时排班, request={}", request);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.createTemporarySchedule(request)));
    }

    /**
     * 获取临时排班列表
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "获取临时排班列表",
        description = "查询所有临时排班调整"
    )
    @GetMapping("/temporary")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<List<TemporarySchedule>>> getTemporarySchedules(
            @Parameter(description = "开始日期", example = "2025-01-01")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2025-01-31")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        log.info("[智能排班] 获取临时排班列表, startDate={}, endDate={}", startDate, endDate);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.getTemporarySchedules(startDate, endDate)));
    }

    // ==================== 排班统计 ====================

    /**
     * 获取排班统计
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "获取排班统计",
        description = "获取排班相关的统计数据"
    )
    @GetMapping("/statistics")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<Map<String, Object>>> getScheduleStatistics(
            @Parameter(description = "开始日期", example = "2025-01-01")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2025-01-31")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "统计维度", example = "user")
            @RequestParam(defaultValue = "summary") String dimension
    ) {
        log.info("[智能排班] 获取排班统计, startDate={}, endDate={}, dimension={}", startDate, endDate, dimension);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.getScheduleStatistics(startDate, endDate, dimension)));
    }

    /**
     * 获取排班覆盖率
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "获取排班覆盖率",
        description = "分析排班覆盖率和缺勤情况"
    )
    @GetMapping("/coverage")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<Map<String, Object>>> getScheduleCoverage(
            @Parameter(description = "开始日期", required = true)
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true)
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "部门ID", example = "1001")
            @RequestParam(required = false) Long departmentId
    ) {
        log.info("[智能排班] 获取排班覆盖率, startDate={}, endDate={}, departmentId={}", startDate, endDate, departmentId);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.getScheduleCoverage(startDate, endDate, departmentId)));
    }

    // ==================== 排班导出 ====================

    /**
     * 导出排班计划
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "导出排班计划",
        description = "导出指定时间段的排班计划"
    )
    @PostMapping("/export")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<Map<String, Object>>> exportSchedulePlan(
            @Parameter(description = "开始日期", required = true)
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true)
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "导出格式", example = "excel")
            @RequestParam(defaultValue = "excel") String format,
            @Parameter(description = "部门ID", example = "1001")
            @RequestParam(required = false) Long departmentId
    ) {
        log.info("[智能排班] 导出排班计划, startDate={}, endDate={}, format={}, departmentId={}",
                 startDate, endDate, format, departmentId);
        return CompletableFuture.completedFuture(ResponseDTO.ok(
                scheduleEngine.exportSchedulePlan(startDate, endDate, format, departmentId)));
    }

    // ==================== 算法配置 ====================

    /**
     * 获取可用算法
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "获取可用算法",
        description = "查询所有可用的排班算法"
    )
    @GetMapping("/algorithms")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<Map<String, Object>>> getAvailableAlgorithms() {
        log.info("[智能排班] 获取可用算法");
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.getAvailableAlgorithms()));
    }

    /**
     * 配置算法参数
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "配置算法参数",
        description = "配置指定排班算法的参数"
    )
    @PostMapping("/algorithm/{algorithmName}/config")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "排班管理操作")
    public CompletableFuture<ResponseDTO<Void>> configureAlgorithm(
            @Parameter(description = "算法名称", required = true, example = "greedy")
            @PathVariable String algorithmName,
            @Parameter(description = "算法参数", required = true)
            @RequestBody @NotNull Map<String, Object> config
    ) {
        log.info("[智能排班] 配置算法参数, algorithmName={}, config={}", algorithmName, config);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleEngine.configureAlgorithm(algorithmName, config)));
    }
}
