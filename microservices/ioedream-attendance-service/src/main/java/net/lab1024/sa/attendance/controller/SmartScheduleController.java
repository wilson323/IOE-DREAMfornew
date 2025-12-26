package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.form.smartSchedule.SmartSchedulePlanAddForm;
import net.lab1024.sa.attendance.domain.form.smartSchedule.SmartSchedulePlanQueryForm;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartSchedulePlanDetailVO;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartSchedulePlanVO;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartScheduleResultVO;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;
import net.lab1024.sa.attendance.service.SmartScheduleService;
import net.lab1024.sa.common.controller.SupportBaseController;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * 智能排班Controller
 * <p>
 * 提供智能排班相关的REST API：
 * - 排班计划管理
 * - 排班优化执行
 * - 排班结果查询
 * - 排班方案导出
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/schedule")
@Tag(name = "智能排班", description = "智能排班管理接口")
public class SmartScheduleController extends SupportBaseController {

    @Resource
    private SmartScheduleService smartScheduleService;

    @Operation(summary = "创建排班计划")
    @PostMapping("/plan")
    public ResponseDTO<Long> createPlan(@RequestBody SmartSchedulePlanAddForm form) {
        log.info("[智能排班] 创建排班计划: name={}", form.getPlanName());
        Long planId = smartScheduleService.createPlan(form);
        return ResponseDTO.ok(planId);
    }

    @Operation(summary = "执行排班优化")
    @PostMapping("/plan/{planId}/execute")
    public ResponseDTO<OptimizationResult> executeOptimization(@PathVariable Long planId) {
        log.info("[智能排班] 执行排班优化: planId={}", planId);
        OptimizationResult result = smartScheduleService.executeOptimization(planId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "查询排班计划列表")
    @GetMapping("/plan")
    public ResponseDTO<PageResult<SmartSchedulePlanVO>> queryPlanPage(SmartSchedulePlanQueryForm form) {
        log.info("[智能排班] 查询排班计划列表: {}", form);
        PageResult<SmartSchedulePlanVO> pageResult = smartScheduleService.queryPlanPage(form);
        return ResponseDTO.ok(pageResult);
    }

    @Operation(summary = "查询排班计划详情")
    @GetMapping("/plan/{planId}")
    public ResponseDTO<SmartSchedulePlanDetailVO> getPlanDetail(@PathVariable Long planId) {
        log.info("[智能排班] 查询排班计划详情: planId={}", planId);
        SmartSchedulePlanDetailVO detail = smartScheduleService.getPlanDetail(planId);
        return ResponseDTO.ok(detail);
    }

    @Operation(summary = "查询排班结果列表（分页）")
    @GetMapping("/plan/{planId}/results")
    public ResponseDTO<PageResult<SmartScheduleResultVO>> queryResultPage(
            @PathVariable Long planId,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        log.info("[智能排班] 查询排班结果: planId={}, pageNum={}, pageSize={}", planId, pageNum, pageSize);
        PageResult<SmartScheduleResultVO> pageResult = smartScheduleService.queryResultPage(
                planId, pageNum, pageSize, employeeId, startDate, endDate);
        return ResponseDTO.ok(pageResult);
    }

    @Operation(summary = "查询排班结果列表（不分页）")
    @GetMapping("/plan/{planId}/results/all")
    public ResponseDTO<List<SmartScheduleResultVO>> queryResultList(@PathVariable Long planId) {
        log.info("[智能排班] 查询所有排班结果: planId={}", planId);
        List<SmartScheduleResultVO> resultList = smartScheduleService.queryResultList(planId);
        return ResponseDTO.ok(resultList);
    }

    @Operation(summary = "删除排班计划")
    @DeleteMapping("/plan/{planId}")
    public ResponseDTO<Void> deletePlan(@PathVariable Long planId) {
        log.info("[智能排班] 删除排班计划: planId={}", planId);
        smartScheduleService.deletePlan(planId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "批量删除排班计划")
    @DeleteMapping("/plan/batch")
    public ResponseDTO<Void> batchDeletePlan(@RequestBody List<Long> planIds) {
        log.info("[智能排班] 批量删除排班计划: count={}", planIds.size());
        smartScheduleService.batchDeletePlan(planIds);
        return ResponseDTO.ok();
    }

    @Operation(summary = "确认排班计划")
    @PostMapping("/plan/{planId}/confirm")
    public ResponseDTO<Void> confirmPlan(@PathVariable Long planId) {
        log.info("[智能排班] 确认排班计划: planId={}", planId);
        smartScheduleService.confirmPlan(planId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "取消排班计划")
    @PostMapping("/plan/{planId}/cancel")
    public ResponseDTO<Void> cancelPlan(
            @PathVariable Long planId,
            @RequestParam String reason) {
        log.info("[智能排班] 取消排班计划: planId={}, reason={}", planId, reason);
        smartScheduleService.cancelPlan(planId, reason);
        return ResponseDTO.ok();
    }

    @Operation(summary = "导出排班结果")
    @GetMapping("/plan/{planId}/export")
    public void exportScheduleResult(
            @PathVariable Long planId,
            HttpServletResponse response) throws IOException {
        log.info("[智能排班] 导出排班结果: planId={}", planId);

        // 生成Excel文件
        byte[] excelBytes = smartScheduleService.exportScheduleResult(planId);

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=schedule_result_" + planId + ".xlsx");

        // 写入响应流
        try (OutputStream os = response.getOutputStream()) {
            os.write(excelBytes);
            os.flush();
        }

        log.info("[智能排班] 导出完成: planId={}, size={}bytes", planId, excelBytes.length);
    }

    @Operation(summary = "验证优化配置")
    @PostMapping("/config/validate")
    public ResponseDTO<Object> validateConfig(@RequestBody OptimizationConfig config) {
        log.info("[智能排班] 验证优化配置");
        // TODO: 实现配置验证功能
        return ResponseDTO.ok();
    }

    @Operation(summary = "获取算法推荐")
    @GetMapping("/algorithm/recommendation")
    public ResponseDTO<String> getAlgorithmRecommendation(
            @RequestParam Integer employeeCount,
            @RequestParam Integer periodDays) {
        log.info("[智能排班] 获取算法推荐: employees={}, days={}", employeeCount, periodDays);
        // TODO: 实现算法推荐功能
        return ResponseDTO.ok();
    }
}
