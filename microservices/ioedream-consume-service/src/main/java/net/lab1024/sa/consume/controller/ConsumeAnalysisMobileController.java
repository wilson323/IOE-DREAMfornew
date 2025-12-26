package net.lab1024.sa.consume.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumptionAnalysisQueryForm;
import net.lab1024.sa.consume.domain.vo.*;
import net.lab1024.sa.consume.service.ConsumeAnalysisService;
import net.lab1024.sa.consume.service.ConsumeExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 移动端消费分析控制器
 * <p>
 * 提供消费数据分析相关的移动端API
 * 包括消费总览、趋势分析、分类统计、习惯分析、智能推荐等
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/v1/consume/mobile/analysis")
@Tag(name = "移动端消费分析", description = "消费数据分析、趋势、统计与推荐接口")
@Slf4j
public class ConsumeAnalysisMobileController {

    @Resource
    private ConsumeAnalysisService consumeAnalysisService;

    @Resource
    private ConsumeExportService exportService;

    @Operation(summary = "获取消费数据分析", description = "获取用户的消费分析数据，包括总览、趋势、分类、习惯等")
    @GetMapping("/consumption")
    public ResponseDTO<ConsumptionAnalysisVO> getConsumptionAnalysis(
            @Valid ConsumptionAnalysisQueryForm queryForm) {
        log.info("[消费分析] 查询消费分析: userId={}, period={}", queryForm.getUserId(), queryForm.getPeriod());
        ConsumptionAnalysisVO result = consumeAnalysisService.getConsumptionAnalysis(queryForm);
        log.info("[消费分析] 查询完成: totalAmount={}, totalCount={}",
                result.getTotalAmount(), result.getTotalCount());
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取消费趋势数据", description = "获取用户指定时间段的消费趋势数据")
    @GetMapping("/trend")
    public ResponseDTO<List<ConsumptionTrendVO>> getConsumptionTrend(
            @Valid ConsumptionAnalysisQueryForm queryForm) {
        log.info("[消费分析] 查询消费趋势: userId={}, period={}", queryForm.getUserId(), queryForm.getPeriod());
        List<ConsumptionTrendVO> result = consumeAnalysisService.getConsumptionTrend(queryForm);
        log.info("[消费分析] 趋势查询完成: size={}", result.size());
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取消费分类统计", description = "获取用户指定时间段的分类消费统计")
    @GetMapping("/category")
    public ResponseDTO<List<CategoryStatsVO>> getCategoryStats(
            @Valid ConsumptionAnalysisQueryForm queryForm) {
        log.info("[消费分析] 查询分类统计: userId={}, period={}", queryForm.getUserId(), queryForm.getPeriod());
        List<CategoryStatsVO> result = consumeAnalysisService.getCategoryStats(queryForm);
        log.info("[消费分析] 分类统计查询完成: size={}", result.size());
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取消费习惯分析", description = "分析用户的消费习惯，包括最常时段、最喜欢品类等")
    @GetMapping("/habits/{userId}")
    public ResponseDTO<ConsumptionHabitsVO> getConsumptionHabits(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "时间周期: week/month/quarter", example = "week")
            @RequestParam(defaultValue = "week") String period) {
        log.info("[消费分析] 查询消费习惯: userId={}, period={}", userId, period);
        ConsumptionHabitsVO result = consumeAnalysisService.getConsumptionHabits(userId, period);
        log.info("[消费分析] 习惯分析完成: mostFrequentTime={}, favoriteCategory={}",
                result.getMostFrequentTime(), result.getFavoriteCategory());
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取智能推荐", description = "基于用户消费习惯生成个性化推荐")
    @GetMapping("/recommendations/{userId}")
    public ResponseDTO<List<SmartRecommendationVO>> getSmartRecommendations(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "时间周期: week/month/quarter", example = "week")
            @RequestParam(defaultValue = "week") String period) {
        log.info("[消费分析] 生成智能推荐: userId={}, period={}", userId, period);
        List<SmartRecommendationVO> result = consumeAnalysisService.getSmartRecommendations(userId, period);
        log.info("[消费分析] 智能推荐生成完成: size={}", result.size());
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "导出PDF报告", description = "导出消费分析PDF报告")
    @GetMapping("/export/pdf")
    public void exportPdf(
            @Valid ConsumptionAnalysisQueryForm queryForm,
            HttpServletResponse response) throws IOException {
        log.info("[消费分析] 导出PDF报告: userId={}, period={}",
                queryForm.getUserId(), queryForm.getPeriod());
        exportService.exportAnalysisPdf(queryForm, response);
    }

    @Operation(summary = "导出Excel报告", description = "导出消费分析Excel报告")
    @GetMapping("/export/excel")
    public void exportExcel(
            @Valid ConsumptionAnalysisQueryForm queryForm,
            HttpServletResponse response) throws IOException {
        log.info("[消费分析] 导出Excel报告: userId={}, period={}",
                queryForm.getUserId(), queryForm.getPeriod());
        exportService.exportAnalysisExcel(queryForm, response);
    }

    @Operation(summary = "导出消费记录Excel", description = "导出消费记录Excel明细")
    @GetMapping("/export/records/{userId}")
    public void exportRecordsExcel(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "时间周期: week/month/quarter", example = "week")
            @RequestParam(defaultValue = "week") String period,
            HttpServletResponse response) throws IOException {
        log.info("[消费分析] 导出消费记录Excel: userId={}, period={}", userId, period);
        exportService.exportRecordsExcel(userId, period, response);
    }
}
