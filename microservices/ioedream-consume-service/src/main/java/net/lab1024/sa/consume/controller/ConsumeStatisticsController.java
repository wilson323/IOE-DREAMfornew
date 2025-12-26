package net.lab1024.sa.consume.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.consume.service.ConsumeStatisticsService;

/**
 * 消费统计报表控制器
 * <p>
 * 提供消费数据的统计分析功能，包括：
 * 1. 消费金额统计
 * 2. 用户消费分析
 * 3. 商户销售统计
 * 4. 时间段分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@RestController
@PermissionCheck(value = "CONSUME_STATISTICS", description = "消费统计权限")
@RequestMapping("/api/v1/consume/statistics")
@Tag(name = "消费统计报表", description = "消费数据分析、统计报表等功能")
public class ConsumeStatisticsController {

    @Resource
    private ConsumeStatisticsService consumeStatisticsService;

    /**
     * 获取总体消费统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 总体统计
     */
    @GetMapping("/overview")
    @Operation(summary = "获取总体消费统计", description = "获取指定时间段的总体消费统计信息")
    public ResponseDTO<Map<String, Object>> getConsumeOverview(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> overview = consumeStatisticsService.getConsumeOverview(startDate, endDate);
        return ResponseDTO.ok(overview);
    }

    /**
     * 获取用户消费排行榜
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 用户排行榜
     */
    @GetMapping("/users/ranking")
    @Operation(summary = "用户消费排行榜", description = "获取用户消费金额排行榜")
    public ResponseDTO<List<Map<String, Object>>> getUserConsumeRanking(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> ranking = consumeStatisticsService.getUserConsumeRanking(startDate, endDate, limit);
        return ResponseDTO.ok(ranking);
    }

    /**
     * 获取商户销售排行榜
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 商户排行榜
     */
    @GetMapping("/merchants/ranking")
    @Operation(summary = "商户销售排行榜", description = "获取商户销售金额排行榜")
    public ResponseDTO<List<Map<String, Object>>> getMerchantSalesRanking(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> ranking = consumeStatisticsService.getMerchantSalesRanking(startDate, endDate, limit);
        return ResponseDTO.ok(ranking);
    }

    /**
     * 获取商品销售排行榜
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 商品排行榜
     */
    @GetMapping("/products/ranking")
    @Operation(summary = "商品销售排行榜", description = "获取商品销售数量排行榜")
    public ResponseDTO<List<Map<String, Object>>> getProductSalesRanking(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> ranking = consumeStatisticsService.getProductSalesRanking(startDate, endDate, limit);
        return ResponseDTO.ok(ranking);
    }

    /**
     * 获取时段消费分析
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 粒度（hour/day/week/month）
     * @return 时段分析
     */
    @GetMapping("/time-analysis")
    @Operation(summary = "时段消费分析", description = "分析不同时间段的消费情况")
    public ResponseDTO<Map<String, Object>> getTimePeriodAnalysis(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "时间粒度") @RequestParam(defaultValue = "day") String granularity) {
        Map<String, Object> analysis = consumeStatisticsService.getTimePeriodAnalysis(startDate, endDate, granularity);
        return ResponseDTO.ok(analysis);
    }

    /**
     * 获取消费方式统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 方式统计
     */
    @GetMapping("/payment-methods")
    @Operation(summary = "消费方式统计", description = "统计各种消费方式的使用情况")
    public ResponseDTO<Map<String, Object>> getPaymentMethodStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> statistics = consumeStatisticsService.getPaymentMethodStatistics(startDate, endDate);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取设备消费统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 设备统计
     */
    @GetMapping("/devices")
    @Operation(summary = "设备消费统计", description = "统计各设备的消费情况")
    public ResponseDTO<Map<String, Object>> getDeviceStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> statistics = consumeStatisticsService.getDeviceStatistics(startDate, endDate);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取区域消费统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 区域统计
     */
    @GetMapping("/areas")
    @Operation(summary = "区域消费统计", description = "统计各区域的消费情况")
    public ResponseDTO<Map<String, Object>> getAreaStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> statistics = consumeStatisticsService.getAreaStatistics(startDate, endDate);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取消费趋势数据
     *
     * @param type 趋势类型（amount/count/avg）
     * @param days 天数
     * @return 趋势数据
     */
    @GetMapping("/trends")
    @Operation(summary = "消费趋势数据", description = "获取消费趋势分析数据")
    public ResponseDTO<Map<String, Object>> getConsumeTrends(
            @Parameter(description = "趋势类型", required = false) @RequestParam(defaultValue = "amount") String type,
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        Map<String, Object> trends = consumeStatisticsService.getConsumeTrends(type, days);
        return ResponseDTO.ok(trends);
    }

    /**
     * 获取用户消费分析
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 用户分析
     */
    @GetMapping("/users/{userId}/analysis")
    @Operation(summary = "用户消费分析", description = "分析指定用户的消费行为")
    public ResponseDTO<Map<String, Object>> getUserConsumeAnalysis(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> analysis = consumeStatisticsService.getUserConsumeAnalysis(userId, startDate, endDate);
        return ResponseDTO.ok(analysis);
    }

    /**
     * 获取实时消费数据
     *
     * @return 实时数据
     */
    @GetMapping("/realtime")
    @Operation(summary = "实时消费数据", description = "获取实时的消费统计数据")
    public ResponseDTO<Map<String, Object>> getRealtimeStatistics() {
        Map<String, Object> realtime = consumeStatisticsService.getRealtimeStatistics();
        return ResponseDTO.ok(realtime);
    }

    /**
     * 获取对比数据
     *
     * @param currentPeriodStart  当前周期开始
     * @param currentPeriodEnd    当前周期结束
     * @param previousPeriodStart 对比周期开始
     * @param previousPeriodEnd   对比周期结束
     * @return 对比数据
     */
    @GetMapping("/comparison")
    @Operation(summary = "同比环比数据", description = "获取消费数据的同比环比分析")
    public ResponseDTO<Map<String, Object>> getComparisonData(
            @Parameter(description = "当前周期开始") @RequestParam LocalDateTime currentPeriodStart,
            @Parameter(description = "当前周期结束") @RequestParam LocalDateTime currentPeriodEnd,
            @Parameter(description = "对比周期开始") @RequestParam LocalDateTime previousPeriodStart,
            @Parameter(description = "对比周期结束") @RequestParam LocalDateTime previousPeriodEnd) {
        Map<String, Object> comparison = consumeStatisticsService.getComparisonData(
                currentPeriodStart, currentPeriodEnd, previousPeriodStart, previousPeriodEnd);
        return ResponseDTO.ok(comparison);
    }

    /**
     * 导出统计报表
     *
     * @param reportType 报表类型
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 导出结果
     */
    @GetMapping("/export")
    @Operation(summary = "导出统计报表", description = "导出各种统计报表到Excel文件")
    public ResponseDTO<String> exportStatisticsReport(
            @Parameter(description = "报表类型", required = true) @RequestParam String reportType,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        String downloadUrl = consumeStatisticsService.exportStatisticsReport(reportType, startDate, endDate);
        return ResponseDTO.ok(downloadUrl);
    }

    /**
     * 获取消费预测数据
     *
     * @param days 预测天数
     * @return 预测数据
     */
    @GetMapping("/prediction")
    @Operation(summary = "消费预测数据", description = "基于历史数据预测未来消费趋势")
    public ResponseDTO<Map<String, Object>> getConsumePrediction(
            @Parameter(description = "预测天数") @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> prediction = consumeStatisticsService.getConsumePrediction(days);
        return ResponseDTO.ok(prediction);
    }
}