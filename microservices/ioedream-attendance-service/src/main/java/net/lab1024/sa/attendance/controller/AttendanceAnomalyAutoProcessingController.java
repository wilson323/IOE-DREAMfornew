package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.attendance.domain.form.AttendanceAnomalyAutoProcessQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceAnomalyAutoProcessVO;
import net.lab1024.sa.attendance.service.AttendanceAnomalyAutoProcessingService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤异常自动处理控制器
 * <p>
 * 提供考勤异常自动处理相关API接口
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/anomaly-auto-process")
@Tag(name = "考勤异常自动处理")
public class AttendanceAnomalyAutoProcessingController {

    @Resource
    private AttendanceAnomalyAutoProcessingService autoProcessingService;

    /**
     * 智能分类异常
     *
     * @param anomalyId 异常ID
     * @return 分类结果
     */
    @Observed(name = "anomalyAutoProcess.categorize", contextualName = "categorize-anomaly")
    @GetMapping("/categorize/{anomalyId}")
    @Operation(summary = "智能分类异常", description = "根据多维度分析对异常进行智能分类")
    public ResponseDTO<String> categorizeAnomaly(
            @PathVariable @Parameter(description = "异常ID", required = true) Long anomalyId) {
        log.info("[异常自动处理] 智能分类异常: anomalyId={}", anomalyId);

        // TODO: 根据anomalyId查询异常实体
        // AttendanceAnomalyEntity anomaly = anomalyDao.selectById(anomalyId);
        // String category = autoProcessingService.categorizeAnomaly(anomaly);

        // 临时返回，待实现
        log.warn("[异常自动处理] 智能分类功能待完善");
        return ResponseDTO.ok("AUTO_APPROVE");
    }

    /**
     * 自动处理单个异常
     *
     * @param anomalyId 异常ID
     * @return 是否处理成功
     */
    @Observed(name = "anomalyAutoProcess.processSingle", contextualName = "process-single-anomaly")
    @PostMapping("/process/{anomalyId}")
    @Operation(summary = "自动处理单个异常", description = "对单个异常执行自动处理")
    public ResponseDTO<Boolean> autoProcessAnomaly(
            @PathVariable @Parameter(description = "异常ID", required = true) Long anomalyId) {
        log.info("[异常自动处理] 自动处理异常: anomalyId={}", anomalyId);

        Boolean result = autoProcessingService.autoProcessAnomaly(anomalyId);
        log.info("[异常自动处理] 自动处理完成: anomalyId={}, result={}", anomalyId, result);

        return ResponseDTO.ok(result);
    }

    /**
     * 批量自动处理异常
     *
     * @param attendanceDate 考勤日期
     * @return 处理的异常数量
     */
    @Observed(name = "anomalyAutoProcess.batchProcess", contextualName = "batch-process-anomalies")
    @PostMapping("/batch-process")
    @Operation(summary = "批量自动处理异常", description = "批量处理指定日期的待处理异常")
    public ResponseDTO<Integer> batchAutoProcess(
            @Parameter(description = "考勤日期", required = true) @RequestParam LocalDate attendanceDate) {
        log.info("[异常自动处理] 批量自动处理: date={}", attendanceDate);

        Integer count = autoProcessingService.batchAutoProcess(attendanceDate);
        log.info("[异常自动处理] 批量处理完成: date={}, count={}", attendanceDate, count);

        return ResponseDTO.ok(count);
    }

    /**
     * 分页查询自动处理记录
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @Observed(name = "anomalyAutoProcess.queryPage", contextualName = "query-auto-process-page")
    @PostMapping("/query")
    @Operation(summary = "分页查询自动处理记录", description = "分页查询自动处理记录列表")
    public ResponseDTO<PageResult<AttendanceAnomalyAutoProcessVO>> queryAutoProcessPage(
            @Valid @RequestBody AttendanceAnomalyAutoProcessQueryForm queryForm) {
        log.info("[异常自动处理] 分页查询自动处理记录: pageNum={}, pageSize={}",
                queryForm.getPageNum(), queryForm.getPageSize());

        PageResult<AttendanceAnomalyAutoProcessVO> pageResult = autoProcessingService.queryAutoProcessPage(queryForm);

        log.info("[异常自动处理] 分页查询成功: total={}", pageResult.getTotal());
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 获取自动处理规则配置
     *
     * @param anomalyType 异常类型（可选）
     * @return 规则配置列表
     */
    @Observed(name = "anomalyAutoProcess.getRules", contextualName = "get-auto-process-rules")
    @GetMapping("/rules")
    @Operation(summary = "获取自动处理规则配置", description = "查询自动处理规则配置列表")
    public ResponseDTO<List<Map<String, Object>>> getAutoProcessingRules(
            @Parameter(description = "异常类型（可选）", required = false) @RequestParam(required = false) String anomalyType) {
        log.info("[异常自动处理] 获取自动处理规则: anomalyType={}", anomalyType);

        List<Map<String, Object>> rules = autoProcessingService.getAutoProcessingRules(anomalyType);

        log.info("[异常自动处理] 获取规则成功: count={}", rules.size());
        return ResponseDTO.ok(rules);
    }

    /**
     * 更新自动处理规则
     *
     * @param ruleId     规则ID
     * @param ruleConfig 规则配置（JSON格式）
     * @param enabled    是否启用
     * @return 是否更新成功
     */
    @Observed(name = "anomalyAutoProcess.updateRule", contextualName = "update-auto-process-rule")
    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "更新自动处理规则", description = "更新指定的自动处理规则配置")
    public ResponseDTO<Boolean> updateAutoProcessingRule(
            @PathVariable @Parameter(description = "规则ID", required = true) Long ruleId,
            @Parameter(description = "规则配置（JSON格式）", required = true) @RequestParam String ruleConfig,
            @Parameter(description = "是否启用", required = true) @RequestParam Boolean enabled) {
        log.info("[异常自动处理] 更新自动处理规则: ruleId={}, enabled={}", ruleId, enabled);

        Boolean result = autoProcessingService.updateAutoProcessingRule(ruleId, ruleConfig, enabled);

        log.info("[异常自动处理] 更新规则成功: ruleId={}, result={}", ruleId, result);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取异常的自动处理建议
     *
     * @param anomalyId 异常ID
     * @return 建议信息
     */
    @Observed(name = "anomalyAutoProcess.getSuggestion", contextualName = "get-auto-process-suggestion")
    @GetMapping("/suggestion/{anomalyId}")
    @Operation(summary = "获取自动处理建议", description = "为管理员提供自动处理的建议和理由")
    public ResponseDTO<Map<String, Object>> getAutoProcessSuggestion(
            @PathVariable @Parameter(description = "异常ID", required = true) Long anomalyId) {
        log.info("[异常自动处理] 获取自动处理建议: anomalyId={}", anomalyId);

        Map<String, Object> suggestion = autoProcessingService.getAutoProcessSuggestion(anomalyId);

        log.info("[异常自动处理] 获取建议成功: anomalyId={}", anomalyId);
        return ResponseDTO.ok(suggestion);
    }

    /**
     * 手动覆盖自动处理决定
     *
     * @param anomalyId   异常ID
     * @param newDecision 新的决定
     * @param comment     处理意见
     * @return 是否覆盖成功
     */
    @Observed(name = "anomalyAutoProcess.override", contextualName = "override-auto-process-decision")
    @PutMapping("/override/{anomalyId}")
    @Operation(summary = "手动覆盖自动处理决定", description = "管理员手动修改自动处理的决定")
    public ResponseDTO<Boolean> overrideAutoProcessDecision(
            @PathVariable @Parameter(description = "异常ID", required = true) Long anomalyId,
            @Parameter(description = "新的决定（APPROVE/REJECT/ESCALATE）", required = true) @RequestParam String newDecision,
            @Parameter(description = "处理意见", required = true) @RequestParam String comment) {
        log.info("[异常自动处理] 手动覆盖自动处理决定: anomalyId={}, newDecision={}", anomalyId, newDecision);

        Boolean result = autoProcessingService.overrideAutoProcessDecision(anomalyId, newDecision, comment);

        log.info("[异常自动处理] 手动覆盖完成: anomalyId={}, result={}", anomalyId, result);
        return ResponseDTO.ok(result);
    }

    /**
     * 统计自动处理效果
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    @Observed(name = "anomalyAutoProcess.statistics", contextualName = "statistics-auto-process-effect")
    @GetMapping("/statistics")
    @Operation(summary = "统计自动处理效果", description = "统计指定时间段的自动处理效果数据")
    public ResponseDTO<Map<String, Object>> statisticsAutoProcessEffect(
            @Parameter(description = "开始日期", required = true) @RequestParam LocalDate startDate,
            @Parameter(description = "结束日期", required = true) @RequestParam LocalDate endDate) {
        log.info("[异常自动处理] 统计自动处理效果: startDate={}, endDate={}", startDate, endDate);

        Map<String, Object> statistics = autoProcessingService.statisticsAutoProcessEffect(startDate, endDate);

        log.info("[异常自动处理] 统计完成: startDate={}, endDate={}", startDate, endDate);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 分析员工的异常历史模式
     *
     * @param userId 用户ID
     * @param months 统计月数
     * @return 历史模式分析结果
     */
    @Observed(name = "anomalyAutoProcess.analyzePattern", contextualName = "analyze-employee-anomaly-pattern")
    @GetMapping("/pattern/{userId}")
    @Operation(summary = "分析员工异常历史模式", description = "分析员工的历史异常记录，识别模式")
    public ResponseDTO<Map<String, Object>> analyzeEmployeeAnomalyPattern(
            @PathVariable @Parameter(description = "用户ID", required = true) Long userId,
            @Parameter(description = "统计月数", required = false) @RequestParam(required = false, defaultValue = "3") Integer months) {
        log.info("[异常自动处理] 分析员工异常模式: userId={}, months={}", userId, months);

        Map<String, Object> pattern = autoProcessingService.analyzeEmployeeAnomalyPattern(userId, months);

        log.info("[异常自动处理] 模式分析完成: userId={}, riskLevel={}",
                userId, pattern.get("riskLevel"));
        return ResponseDTO.ok(pattern);
    }
}
