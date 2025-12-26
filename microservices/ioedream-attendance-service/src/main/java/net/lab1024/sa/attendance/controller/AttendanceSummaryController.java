package net.lab1024.sa.attendance.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.service.AttendanceSummaryService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 考勤汇总管理控制器
 * <p>
 * 提供考勤汇总数据的生成和查询接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/summary")
@Tag(name = "考勤汇总管理")
public class AttendanceSummaryController {

    @Resource
    private AttendanceSummaryService attendanceSummaryService;

    /**
     * 生成个人月度汇总
     */
    @PostMapping("/personal/generate")
    @Operation(summary = "生成个人月度汇总")
    public ResponseDTO<Void> generatePersonalSummary(
            @Parameter(description = "员工ID", required = true)
            @RequestParam Long employeeId,
            @Parameter(description = "年份", required = true)
            @RequestParam Integer year,
            @Parameter(description = "月份", required = true)
            @RequestParam Integer month
    ) {
        log.info("[汇总管理] 生成个人月度汇总: employeeId={}, year={}, month={}", employeeId, year, month);

        boolean success = attendanceSummaryService.generatePersonalSummary(employeeId, year, month);

        return success ?
                ResponseDTO.ok() :
                ResponseDTO.error("GENERATE_FAILED", "生成汇总失败");
    }

    /**
     * 批量生成个人月度汇总
     */
    @PostMapping("/personal/batch-generate")
    @Operation(summary = "批量生成个人月度汇总")
    public ResponseDTO<String> batchGeneratePersonalSummaries(
            @Parameter(description = "年份", required = true)
            @RequestParam Integer year,
            @Parameter(description = "月份", required = true)
            @RequestParam Integer month
    ) {
        log.info("[汇总管理] 批量生成个人月度汇总: year={}, month={}", year, month);

        int count = attendanceSummaryService.batchGeneratePersonalSummaries(year, month);

        String message = String.format("成功生成 %d 条个人汇总记录", count);
        return ResponseDTO.ok(message);
    }

    /**
     * 生成部门月度统计
     */
    @PostMapping("/department/generate")
    @Operation(summary = "生成部门月度统计")
    public ResponseDTO<Void> generateDepartmentStatistics(
            @Parameter(description = "部门ID", required = true)
            @RequestParam Long departmentId,
            @Parameter(description = "年份", required = true)
            @RequestParam Integer year,
            @Parameter(description = "月份", required = true)
            @RequestParam Integer month
    ) {
        log.info("[汇总管理] 生成部门月度统计: departmentId={}, year={}, month={}", departmentId, year, month);

        boolean success = attendanceSummaryService.generateDepartmentStatistics(departmentId, year, month);

        return success ?
                ResponseDTO.ok() :
                ResponseDTO.error("GENERATE_FAILED", "生成统计失败");
    }

    /**
     * 批量生成部门月度统计
     */
    @PostMapping("/department/batch-generate")
    @Operation(summary = "批量生成部门月度统计")
    public ResponseDTO<String> batchGenerateDepartmentStatistics(
            @Parameter(description = "年份", required = true)
            @RequestParam Integer year,
            @Parameter(description = "月份", required = true)
            @RequestParam Integer month
    ) {
        log.info("[汇总管理] 批量生成部门月度统计: year={}, month={}", year, month);

        int count = attendanceSummaryService.batchGenerateDepartmentStatistics(year, month);

        String message = String.format("成功生成 %d 条部门统计记录", count);
        return ResponseDTO.ok(message);
    }

    /**
     * 触发全量汇总生成（个人+部门）
     */
    @PostMapping("/full-generate")
    @Operation(summary = "触发全量汇总生成")
    public ResponseDTO<String> triggerFullSummaryGeneration(
            @Parameter(description = "年份", required = true)
            @RequestParam Integer year,
            @Parameter(description = "月份", required = true)
            @RequestParam Integer month
    ) {
        log.info("[汇总管理] 触发全量汇总生成: year={}, month={}", year, month);

        String result = attendanceSummaryService.triggerFullSummaryGeneration(year, month);

        return ResponseDTO.ok(result);
    }
}
