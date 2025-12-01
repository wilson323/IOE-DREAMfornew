package net.lab1024.sa.admin.module.consume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.admin.module.consume.service.ReportService;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.annotation.SaCheckLogin;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报表管理控制器
 *
 * @author OpenSpec Task 2.10 Implementation
 * @version 1.0
 * @since 2025-11-17
 */
@RestController
@RequestMapping("/api/consume/report")
@Tag(name = "报表管理", description = "消费统计报表相关接口")
@Validated
@RequiredArgsConstructor
public class ReportController extends SupportBaseController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "消费汇总", description = "获取消费数据汇总统计")
    @SaCheckLogin
    @SaCheckPermission("consume:report:summary")
    public ResponseDTO<Map<String, Object>> getConsumeSummary(
            @RequestParam(required = false) String timeDimension,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String consumeMode) {
        try {
            return ResponseDTO.ok(reportService.getConsumeSummary(timeDimension, startTime, endTime, deviceId, consumeMode));
        } catch (Exception e) {
            return ResponseDTO.error("获取消费汇总失败: " + e.getMessage());
        }
    }

    @GetMapping("/trend")
    @Operation(summary = "消费趋势", description = "获取消费趋势数据")
    @SaCheckLogin
    @SaCheckPermission("consume:report:trend")
    public ResponseDTO<List<Map<String, Object>>> getConsumeTrend(
            @RequestParam(required = false) String timeDimension,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String trendType,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String consumeMode) {
        try {
            return ResponseDTO.ok(reportService.getConsumeTrend(timeDimension, startTime, endTime, trendType, deviceId, consumeMode));
        } catch (Exception e) {
            return ResponseDTO.error("获取消费趋势失败: " + e.getMessage());
        }
    }

    @GetMapping("/mode-distribution")
    @Operation(summary = "消费模式分布", description = "获取各消费模式分布数据")
    @SaCheckLogin
    @SaCheckPermission("consume:report:mode")
    public ResponseDTO<List<Map<String, Object>>> getConsumeModeDistribution(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) Long deviceId) {
        try {
            return ResponseDTO.ok(reportService.getConsumeModeDistribution(startTime, endTime, deviceId));
        } catch (Exception e) {
            return ResponseDTO.error("获取消费模式分布失败: " + e.getMessage());
        }
    }

    @GetMapping("/device-ranking")
    @Operation(summary = "设备消费排行", description = "获取设备消费排行榜")
    @SaCheckLogin
    @SaCheckPermission("consume:report:device")
    public ResponseDTO<List<Map<String, Object>>> getDeviceRanking(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String rankingType,
            @RequestParam(required = false) Integer limit) {
        try {
            return ResponseDTO.ok(reportService.getDeviceRanking(startTime, endTime, rankingType, limit));
        } catch (Exception e) {
            return ResponseDTO.error("获取设备消费排行失败: " + e.getMessage());
        }
    }

    @GetMapping("/user-ranking")
    @Operation(summary = "用户消费排行", description = "获取用户消费排行榜")
    @SaCheckLogin
    @SaCheckPermission("consume:report:user")
    public ResponseDTO<List<Map<String, Object>>> getUserRanking(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String rankingType,
            @RequestParam(required = false) Integer limit) {
        try {
            return ResponseDTO.ok(reportService.getUserRanking(startTime, endTime, rankingType, limit));
        } catch (Exception e) {
            return ResponseDTO.error("获取用户消费排行失败: " + e.getMessage());
        }
    }

    @GetMapping("/hour-distribution")
    @Operation(summary = "时段分布", description = "获取24小时消费时段分布")
    @SaCheckLogin
    @SaCheckPermission("consume:report:hour")
    public ResponseDTO<List<Map<String, Object>>> getHourDistribution(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) Long deviceId) {
        try {
            return ResponseDTO.ok(reportService.getHourDistribution(startTime, endTime, deviceId));
        } catch (Exception e) {
            return ResponseDTO.error("获取时段分布失败: " + e.getMessage());
        }
    }

    @GetMapping("/region-distribution")
    @Operation(summary = "地区分布", description = "获取地区消费分布数据")
    @SaCheckLogin
    @SaCheckPermission("consume:report:region")
    public ResponseDTO<List<Map<String, Object>>> getRegionDistribution(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String regionMetric) {
        try {
            return ResponseDTO.ok(reportService.getRegionDistribution(startTime, endTime, regionMetric));
        } catch (Exception e) {
            return ResponseDTO.error("获取地区分布失败: " + e.getMessage());
        }
    }

    @GetMapping("/comparison")
    @Operation(summary = "同比环比", description = "获取同比环比数据")
    @SaCheckLogin
    @SaCheckPermission("consume:report:comparison")
    public ResponseDTO<Map<String, Object>> getComparisonData(
            @RequestParam @NotNull String comparisonType,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String consumeMode) {
        try {
            return ResponseDTO.ok(reportService.getComparisonData(comparisonType, startTime, endTime, deviceId, consumeMode));
        } catch (Exception e) {
            return ResponseDTO.error("获取同比环比数据失败: " + e.getMessage());
        }
    }

    @PostMapping("/export")
    @Operation(summary = "导出报表", description = "导出各类统计报表")
    @SaCheckLogin
    @SaCheckPermission("consume:report:export")
    public ResponseDTO<String> exportReport(
            @RequestParam @NotNull String reportType,
            @RequestParam(required = false) String timeDimension,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) Map<String, Object> exportParams) {
        try {
            return ResponseDTO.ok(reportService.exportReport(reportType, timeDimension, startTime, endTime, format, exportParams));
        } catch (Exception e) {
            return ResponseDTO.error("导出报表失败: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "仪表盘数据", description = "获取仪表盘展示数据")
    @SaCheckLogin
    @SaCheckPermission("consume:report:dashboard")
    public ResponseDTO<Map<String, Object>> getDashboardData(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        try {
            return ResponseDTO.ok(reportService.getDashboardData(startTime, endTime));
        } catch (Exception e) {
            return ResponseDTO.error("获取仪表盘数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/real-time")
    @Operation(summary = "实时统计", description = "获取实时消费统计数据")
    @SaCheckLogin
    @SaCheckPermission("consume:report:realtime")
    public ResponseDTO<Map<String, Object>> getRealTimeStatistics() {
        try {
            return ResponseDTO.ok(reportService.getRealTimeStatistics());
        } catch (Exception e) {
            return ResponseDTO.error("获取实时统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/anomaly-detection")
    @Operation(summary = "异常检测", description = "获取消费异常检测结果")
    @SaCheckLogin
    @SaCheckPermission("consume:report:anomaly")
    public ResponseDTO<List<Map<String, Object>>> getAnomalyDetection(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String detectionType) {
        try {
            return ResponseDTO.ok(reportService.getAnomalyDetection(startTime, endTime, detectionType));
        } catch (Exception e) {
            return ResponseDTO.error("获取异常检测结果失败: " + e.getMessage());
        }
    }

    @GetMapping("/forecast")
    @Operation(summary = "预测分析", description = "获取消费预测分析数据")
    @SaCheckLogin
    @SaCheckPermission("consume:report:forecast")
    public ResponseDTO<Map<String, Object>> getForecastAnalysis(
            @RequestParam @NotNull String forecastType,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) Integer forecastPeriod) {
        try {
            return ResponseDTO.ok(reportService.getForecastAnalysis(forecastType, startTime, endTime, forecastPeriod));
        } catch (Exception e) {
            return ResponseDTO.error("获取预测分析失败: " + e.getMessage());
        }
    }
}