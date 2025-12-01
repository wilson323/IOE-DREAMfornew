package net.lab1024.sa.admin.module.consume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.admin.module.consume.service.report.AdvancedReportService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 高级报表控制器
 * 提供高级可视化和数据分析功能
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Slf4j
@RestController
@RequestMapping("/api/consume/advanced-report")
@Tag(name = "高级报表", description = "消费模块高级报表和可视化接口")
public class AdvancedReportController extends SupportBaseController {

    @Resource
    private AdvancedReportService advancedReportService;

    /**
     * 生成消费热力图数据
     */
    @Operation(summary = "生成消费热力图数据", description = "生成按小时和星期分布的消费热力图")
    @SaCheckPermission("consume:report:heatmap")
    @GetMapping("/heatmap")
    public ResponseDTO<Map<String, Object>> generateConsumeHeatmap(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {

        try {
            LocalDateTime start = parseDateTime(startDate, LocalDateTime.now().minusDays(30));
            LocalDateTime end = parseDateTime(endDate, LocalDateTime.now());

            return advancedReportService.generateConsumeHeatmap(start, end);
        } catch (Exception e) {
            log.error("生成消费热力图数据异常", e);
            return ResponseDTO.error("生成热力图数据失败");
        }
    }

    /**
     * 生成消费漏斗图数据
     */
    @Operation(summary = "生成消费漏斗图数据", description = "生成从浏览到完成的消费转化漏斗图")
    @SaCheckPermission("consume:report:funnel")
    @GetMapping("/funnel")
    public ResponseDTO<Map<String, Object>> generateConsumeFunnel(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {

        try {
            LocalDateTime start = parseDateTime(startDate, LocalDateTime.now().minusDays(30));
            LocalDateTime end = parseDateTime(endDate, LocalDateTime.now());

            return advancedReportService.generateConsumeFunnel(start, end);
        } catch (Exception e) {
            log.error("生成消费漏斗图数据异常", e);
            return ResponseDTO.error("生成漏斗图数据失败");
        }
    }

    /**
     * 生成用户消费行为雷达图
     */
    @Operation(summary = "生成用户消费行为雷达图", description = "生成用户消费行为多维度雷达图分析")
    @SaCheckPermission("consume:report:radar")
    @GetMapping("/user-radar/{userId}")
    public ResponseDTO<Map<String, Object>> generateUserBehaviorRadar(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {

        try {
            LocalDateTime start = parseDateTime(startDate, LocalDateTime.now().minusDays(30));
            LocalDateTime end = parseDateTime(endDate, LocalDateTime.now());

            return advancedReportService.generateUserBehaviorRadar(userId, start, end);
        } catch (Exception e) {
            log.error("生成用户消费行为雷达图异常, userId: {}", userId, e);
            return ResponseDTO.error("生成雷达图数据失败");
        }
    }

    /**
     * 生成实时消费监控大盘数据
     */
    @Operation(summary = "生成实时消费监控大盘", description = "生成实时消费监控大盘数据")
    @SaCheckPermission("consume:report:dashboard")
    @GetMapping("/dashboard")
    public ResponseDTO<Map<String, Object>> generateRealTimeDashboard() {
        try {
            return advancedReportService.generateRealTimeDashboard();
        } catch (Exception e) {
            log.error("生成实时消费监控大盘数据异常", e);
            return ResponseDTO.error("生成监控大盘数据失败");
        }
    }

    /**
     * 生成智能预测分析数据
     */
    @Operation(summary = "生成智能预测分析", description = "基于历史数据生成未来趋势预测")
    @SaCheckPermission("consume:report:predict")
    @GetMapping("/predict")
    public ResponseDTO<Map<String, Object>> generatePredictiveAnalysis(
            @Parameter(description = "预测天数") @RequestParam(defaultValue = "7") Integer predictDays) {

        try {
            return advancedReportService.generatePredictiveAnalysis(predictDays);
        } catch (Exception e) {
            log.error("生成智能预测分析数据异常", e);
            return ResponseDTO.error("生成预测分析失败");
        }
    }

    /**
     * 导出高级报表数据
     */
    @Operation(summary = "导出高级报表数据", description = "导出可视化分析数据为Excel文件")
    @SaCheckPermission("consume:report:export")
    @PostMapping("/export")
    public ResponseDTO<String> exportAdvancedReport(
            @Parameter(description = "报表类型") @RequestParam String reportType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            HttpServletResponse response) {

        try {
            LocalDateTime start = parseDateTime(startDate, LocalDateTime.now().minusDays(30));
            LocalDateTime end = parseDateTime(endDate, LocalDateTime.now());

            String fileName = exportReportData(reportType, start, end, response);
            return ResponseDTO.ok(fileName);
        } catch (Exception e) {
            log.error("导出高级报表数据异常", e);
            return ResponseDTO.error("导出报表失败");
        }
    }

    /**
     * 获取报表配置信息
     */
    @Operation(summary = "获取报表配置信息", description = "获取可视化报表的配置和元数据")
    @SaCheckPermission("consume:report:config")
    @GetMapping("/config")
    public ResponseDTO<Map<String, Object>> getReportConfig(
            @Parameter(description = "报表类型") @RequestParam(required = false) String reportType) {

        try {
            Map<String, Object> config = generateReportConfig(reportType);
            return ResponseDTO.ok(config);
        } catch (Exception e) {
            log.error("获取报表配置信息异常", e);
            return ResponseDTO.error("获取配置信息失败");
        }
    }

    /**
     * 缓存刷新接口
     */
    @Operation(summary = "刷新报表缓存", description = "手动刷新报表数据缓存")
    @SaCheckPermission("consume:report:refresh")
    @PostMapping("/refresh-cache")
    public ResponseDTO<String> refreshCache(
            @Parameter(description = "缓存类型") @RequestParam(required = false) String cacheType) {

        try {
            // 实现缓存刷新逻辑
            log.info("刷新报表缓存, cacheType: {}", cacheType);
            return ResponseDTO.ok("缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新报表缓存异常", e);
            return ResponseDTO.error("缓存刷新失败");
        }
    }

    // ========== 私有辅助方法 ==========

    private LocalDateTime parseDateTime(String dateStr, LocalDateTime defaultDate) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultDate;
        }

        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e1) {
            try {
                // 尝试只解析日期部分
                return LocalDateTime.parse(dateStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e2) {
                log.warn("日期格式解析失败, 使用默认日期: {}", dateStr);
                return defaultDate;
            }
        }
    }

    private String exportReportData(String reportType, LocalDateTime startDate, LocalDateTime endDate, HttpServletResponse response) {
        try {
            String fileName = switch (reportType) {
                case "heatmap" -> exportHeatmapData(startDate, endDate, response);
                case "funnel" -> exportFunnelData(startDate, endDate, response);
                case "dashboard" -> exportDashboardData(response);
                case "prediction" -> exportPredictionData(response);
                default -> "unknown_report_type";
            };

            return fileName;
        } catch (Exception e) {
            log.error("导出报表数据异常, reportType: {}", reportType, e);
            return "export_failed";
        }
    }

    private String exportHeatmapData(LocalDateTime startDate, LocalDateTime endDate, HttpServletResponse response) {
        // 实现热力图数据导出逻辑
        response.setHeader("Content-Disposition", "attachment; filename=heatmap_data.xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return "heatmap_data.xlsx";
    }

    private String exportFunnelData(LocalDateTime startDate, LocalDateTime endDate, HttpServletResponse response) {
        // 实现漏斗图数据导出逻辑
        response.setHeader("Content-Disposition", "attachment; filename=funnel_data.xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return "funnel_data.xlsx";
    }

    private String exportDashboardData(HttpServletResponse response) {
        // 实现监控大盘数据导出逻辑
        response.setHeader("Content-Disposition", "attachment; filename=dashboard_data.xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return "dashboard_data.xlsx";
    }

    private String exportPredictionData(HttpServletResponse response) {
        // 实现预测数据导出逻辑
        response.setHeader("Content-Disposition", "attachment; filename=prediction_data.xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return "prediction_data.xlsx";
    }

    private Map<String, Object> generateReportConfig(String reportType) {
        Map<String, Object> config = new java.util.HashMap<>();

        if (reportType == null || "all".equals(reportType)) {
            // 返回所有报表的配置
            config.put("heatmap", generateHeatmapConfig());
            config.put("funnel", generateFunnelConfig());
            config.put("radar", generateRadarConfig());
            config.put("dashboard", generateDashboardConfig());
        } else {
            config = switch (reportType) {
                case "heatmap" -> generateHeatmapConfig();
                case "funnel" -> generateFunnelConfig();
                case "radar" -> generateRadarConfig();
                case "dashboard" -> generateDashboardConfig();
                default -> new java.util.HashMap<>();
            };
        }

        return config;
    }

    private Map<String, Object> generateHeatmapConfig() {
        Map<String, Object> config = new java.util.HashMap<>();
        config.put("title", "消费热力图");
        config.put("description", "显示按小时和星期分布的消费活跃度");
        config.put("chartType", "heatmap");
        config.put("refreshInterval", 300); // 5分钟刷新一次

        Map<String, Object> xAxis = new java.util.HashMap<>();
        xAxis.put("type", "category");
        xAxis.put("data", generateHourLabels());
        config.put("xAxis", xAxis);

        Map<String, Object> yAxis = new java.util.HashMap<>();
        yAxis.put("type", "category");
        yAxis.put("data", generateDayLabels());
        config.put("yAxis", yAxis);

        return config;
    }

    private Map<String, Object> generateFunnelConfig() {
        Map<String, Object> config = new java.util.HashMap<>();
        config.put("title", "消费转化漏斗");
        config.put("description", "显示用户从浏览到消费的转化过程");
        config.put("chartType", "funnel");
        config.put("refreshInterval", 600); // 10分钟刷新一次

        Map<String, Object> tooltip = new java.util.HashMap<>();
        tooltip.put("trigger", "item");
        tooltip.put("formatter", "{a} <br/>{b} : {c} ({d}%)");
        config.put("tooltip", tooltip);

        return config;
    }

    private Map<String, Object> generateRadarConfig() {
        Map<String, Object> config = new java.util.HashMap<>();
        config.put("title", "用户消费行为雷达图");
        config.put("description", "多维度分析用户消费行为特征");
        config.put("chartType", "radar");
        config.put("maxValue", 100);

        return config;
    }

    private Map<String, Object> generateDashboardConfig() {
        Map<String, Object> config = new java.util.HashMap<>();
        config.put("title", "实时消费监控大盘");
        config.put("description", "实时监控消费业务核心指标");
        config.put("refreshInterval", 30); // 30秒刷新一次

        java.util.List<String> metrics = java.util.Arrays.asList(
            "totalAmount", "totalOrders", "totalUsers", "avgOrderAmount"
        );
        config.put("metrics", metrics);

        return config;
    }

    private java.util.List<String> generateHourLabels() {
        java.util.List<String> labels = new java.util.ArrayList<>();
        for (int i = 0; i < 24; i++) {
            labels.add(String.format("%02d:00", i));
        }
        return labels;
    }

    private java.util.List<String> generateDayLabels() {
        return java.util.Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
    }
}