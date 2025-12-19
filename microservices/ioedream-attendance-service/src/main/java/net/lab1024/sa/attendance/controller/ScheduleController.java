package net.lab1024.sa.attendance.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.service.ScheduleService;
import net.lab1024.sa.attendance.engine.model.ConflictDetectionResult;
import net.lab1024.sa.attendance.engine.model.ConflictResolution;
import net.lab1024.sa.attendance.engine.model.OptimizedSchedule;
import net.lab1024.sa.attendance.engine.model.ScheduleConflict;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.SchedulePlan;
import net.lab1024.sa.attendance.engine.model.SchedulePrediction;
import net.lab1024.sa.attendance.engine.model.ScheduleRequest;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;
import net.lab1024.sa.attendance.engine.model.ScheduleStatistics;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 考勤排班管理控制器（引擎能力）
 * <p>
 * 职责边界（SOLID/SRP）：
 * - 仅暴露排班引擎能力：排班执行、冲突检测/解决、优化、预测、统计
 * - 模板/班次/导出等应用用例由 {@code SmartSchedulingController} + {@code ScheduleService} 承担
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/schedule")
@Tag(name = "考勤排班管理", description = "智能排班引擎能力：排班执行、冲突、优化、预测、统计")
@Validated
@CircuitBreaker(name = "scheduleController")
@PermissionCheck(description = "考勤排班管理（引擎能力）")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    /**
     * 执行智能排班
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "执行智能排班",
        description = "基于排班引擎执行智能排班"
    )
    @PostMapping("/intelligent")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "执行智能排班")
    public CompletableFuture<ResponseDTO<ScheduleResult>> executeIntelligentSchedule(
        @Parameter(description = "排班请求", required = true)
        @Valid @RequestBody ScheduleRequest request
    ) {
        log.info("[智能排班] 执行智能排班, request={}", request);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleService.executeIntelligentSchedule(request)));
    }

    /**
     * 生成排班计划
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "生成排班计划",
        description = "为指定时间段生成排班计划"
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
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleService.generateSchedulePlan(planId, startDate, endDate)));
    }

    /**
     * 验证排班冲突
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "验证排班冲突",
        description = "检测排班中的冲突问题"
    )
    @PostMapping("/conflicts/validate")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR"}, description = "验证排班冲突")
    public CompletableFuture<ResponseDTO<ConflictDetectionResult>> validateScheduleConflicts(
        @Parameter(description = "排班数据", required = true)
        @Valid @RequestBody ScheduleData scheduleData
    ) {
        log.info("[智能排班] 验证排班冲突, scheduleData={}", scheduleData);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleService.validateScheduleConflicts(scheduleData)));
    }

    /**
     * 解决排班冲突
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "解决排班冲突",
        description = "基于指定策略对冲突进行处理并返回解决方案"
    )
    @PostMapping("/conflicts/resolve")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE"}, description = "解决排班冲突")
    public CompletableFuture<ResponseDTO<ConflictResolution>> resolveScheduleConflicts(
        @Parameter(description = "冲突解决请求", required = true)
        @Valid @RequestBody ResolveConflictsRequest request
    ) {
        List<ScheduleConflict> conflicts = request.getConflicts();
        String strategy = request.getResolutionStrategy();
        log.info("[智能排班] 解决排班冲突, conflictsSize={}, strategy={}",
            conflicts == null ? 0 : conflicts.size(), strategy);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleService.resolveScheduleConflicts(conflicts, strategy)));
    }

    /**
     * 优化排班结果
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "优化排班结果",
        description = "按指定目标优化当前排班结果"
    )
    @PostMapping("/optimize")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR"}, description = "优化排班结果")
    public CompletableFuture<ResponseDTO<OptimizedSchedule>> optimizeSchedule(
        @Parameter(description = "排班优化请求", required = true)
        @Valid @RequestBody OptimizeScheduleRequest request
    ) {
        log.info("[智能排班] 优化排班结果, target={}", request.getOptimizationTarget());
        return CompletableFuture.completedFuture(ResponseDTO.ok(
            scheduleService.optimizeSchedule(request.getScheduleData(), request.getOptimizationTarget())));
    }

    /**
     * 预测排班效果
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "预测排班效果",
        description = "对当前排班数据进行效果预测"
    )
    @PostMapping("/predict")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR", "ATTENDANCE_VIEWER"}, description = "预测排班效果")
    public CompletableFuture<ResponseDTO<SchedulePrediction>> predictScheduleEffect(
        @Parameter(description = "排班数据", required = true)
        @Valid @RequestBody ScheduleData scheduleData
    ) {
        log.info("[智能排班] 预测排班效果, planId={}", scheduleData == null ? null : scheduleData.getPlanId());
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleService.predictScheduleEffect(scheduleData)));
    }

    /**
     * 获取排班统计信息
     */
    @TimeLimiter(name = "scheduleController")
    @Operation(
        summary = "获取排班统计信息",
        description = "获取指定计划的统计信息"
    )
    @GetMapping("/statistics")
    @PermissionCheck(value = {"ATTENDANCE_MANAGE", "ATTENDANCE_OPERATOR", "ATTENDANCE_VIEWER"}, description = "获取排班统计信息")
    public CompletableFuture<ResponseDTO<ScheduleStatistics>> getScheduleStatistics(
        @Parameter(description = "计划ID", required = true, example = "1001")
        @RequestParam @NotNull Long planId
    ) {
        log.info("[智能排班] 获取排班统计信息, planId={}", planId);
        return CompletableFuture.completedFuture(ResponseDTO.ok(scheduleService.getScheduleStatistics(planId)));
    }

    @Data
    public static class ResolveConflictsRequest {
        @NotNull
        private List<ScheduleConflict> conflicts;

        private String resolutionStrategy = "AUTO";
    }

    @Data
    public static class OptimizeScheduleRequest {
        @NotNull
        private ScheduleData scheduleData;

        @NotNull
        private String optimizationTarget;
    }
}

