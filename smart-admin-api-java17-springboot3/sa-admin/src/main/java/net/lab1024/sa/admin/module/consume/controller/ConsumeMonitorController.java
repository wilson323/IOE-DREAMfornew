package net.lab1024.sa.admin.module.consume.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.stereotype.Controller;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import net.lab1024.sa.admin.module.consume.monitor.ConsumeMetricsCollector;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.annotation.SaCheckLogin;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;

import java.util.Map;

/**
 * 消费模块监控控制器
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Controller
@RestController
@RequestMapping("/api/consume/monitor")
@Tag(name = "消费监控", description = "消费模块监控和指标")
@Validated
public class ConsumeMonitorController extends SupportBaseController {

    @Resource
    private ConsumeMetricsCollector consumeMetricsCollector;

    @GetMapping("/business-metrics")
    @Operation(summary = "获取业务指标", description = "获取消费模块的业务指标数据")
    @SaCheckLogin
    @SaCheckPermission("consume:monitor:view")
    public ResponseDTO<Map<String, Object>> getBusinessMetrics() {
        Map<String, Object> metrics = consumeMetricsCollector.getBusinessMetrics();
        return ResponseDTO.ok(metrics);
    }

    @GetMapping("/technical-metrics")
    @Operation(summary = "获取技术指标", description = "获取消费模块的技术指标数据")
    @SaCheckLogin
    @SaCheckPermission("consume:monitor:view")
    public ResponseDTO<Map<String, Object>> getTechnicalMetrics() {
        Map<String, Object> metrics = consumeMetricsCollector.getTechnicalMetrics();
        return ResponseDTO.ok(metrics);
    }

    @GetMapping("/health")
    @Operation(summary = "获取健康状态", description = "获取消费模块的健康状态信息")
    @SaCheckLogin
    @SaCheckPermission("consume:monitor:view")
    public ResponseDTO<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = consumeMetricsCollector.getHealthStatus();
        return ResponseDTO.ok(health);
    }

    @PostMapping("/metrics/reset")
    @Operation(summary = "重置指标", description = "重置所有监控指标统计数据")
    @SaCheckLogin
    @SaCheckPermission("consume:monitor:reset")
    public ResponseDTO<String> resetMetrics(@RequestParam @NotNull String confirmToken) {
        // 简单的确认令牌验证，实际项目中应该使用更安全的方式
        if (!"RESET_CONFIRMED".equals(confirmToken)) {
            return ResponseDTO.error("确认令牌无效，指标重置已取消");
        }

        consumeMetricsCollector.resetStatistics();
        return ResponseDTO.ok("监控指标重置完成");
    }

    @GetMapping("/dashboard")
    @Operation(summary = "监控大屏数据", description = "获取监控大屏所需的综合数据")
    @SaCheckLogin
    @SaCheckPermission("consume:monitor:view")
    public ResponseDTO<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new java.util.HashMap<>();

        // 业务指标
        dashboard.put("business", consumeMetricsCollector.getBusinessMetrics());

        // 技术指标
        dashboard.put("technical", consumeMetricsCollector.getTechnicalMetrics());

        // 健康状态
        dashboard.put("health", consumeMetricsCollector.getHealthStatus());

        // 实时状态
        dashboard.put("status", Map.of(
            "timestamp", java.time.LocalDateTime.now().toString(),
            "collectorStatus", "RUNNING",
            "dataFreshness", "FRESH"
        ));

        return ResponseDTO.ok(dashboard);
    }

    @GetMapping("/alerts")
    @Operation(summary = "获取告警信息", description = "获取当前活跃的告警信息")
    @SaCheckLogin
    @SaCheckPermission("consume:monitor:view")
    public ResponseDTO<Map<String, Object>> getAlerts() {
        Map<String, Object> health = consumeMetricsCollector.getHealthStatus();
        Map<String, Object> alerts = new java.util.HashMap<>();

        // 从健康状态中提取告警信息
        Object alertsObj = health.get("alerts");
        Map<String, Boolean> alertStatus;
        if (alertsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> status = (Map<String, Boolean>) alertsObj;
            alertStatus = status;
        } else {
            alertStatus = Collections.emptyMap();
        }
        alerts.put("active", alertStatus);
        alerts.put("total", alertStatus.values().stream().mapToInt(v -> v ? 1 : 0).sum());

        // 按级别分类告警
        Map<String, Integer> alertLevels = new java.util.HashMap<>();
        alertStatus.forEach((key, active) -> {
            if (active) {
                String level = determineAlertLevel(key);
                alertLevels.put(level, alertLevels.getOrDefault(level, 0) + 1);
            }
        });
        alerts.put("levels", alertLevels);

        return ResponseDTO.ok(alerts);
    }

    @GetMapping("/summary")
    @Operation(summary = "获取监控摘要", description = "获取监控模块的摘要信息")
    @SaCheckLogin
    @SaCheckPermission("consume:monitor:view")
    public ResponseDTO<Map<String, Object>> getMonitorSummary() {
        Map<String, Object> summary = new java.util.HashMap<>();

        // 基本信息
        summary.put("moduleName", "consume");
        summary.put("collectorVersion", "1.0.0");
        summary.put("lastUpdate", java.time.LocalDateTime.now().toString());

        // 核心指标
        Map<String, Object> business = consumeMetricsCollector.getBusinessMetrics();
        Map<String, Object> technical = consumeMetricsCollector.getTechnicalMetrics();
        Map<String, Object> health = consumeMetricsCollector.getHealthStatus();

        // 获取告警数量
        Object healthAlertsObj = health.get("alerts");
        int activeAlertsCount = 0;
        if (healthAlertsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> healthAlerts = (Map<String, Boolean>) healthAlertsObj;
            activeAlertsCount = healthAlerts.values().stream().mapToInt(v -> v ? 1 : 0).sum();
        }

        summary.put("summary", Map.of(
            "totalOperations", business.get("successCount"),
            "successRate", business.get("successRate"),
            "cacheHitRate", technical.get("cacheHitRate"),
            "healthStatus", health.get("healthGrade"),
            "activeAlerts", activeAlertsCount
        ));

        return ResponseDTO.ok(summary);
    }

    /**
     * 确定告警级别
     */
    private String determineAlertLevel(String alertKey) {
        if (alertKey.contains("Low")) {
            return "WARNING";
        } else if (alertKey.contains("High")) {
            return "CRITICAL";
        } else {
            return "INFO";
        }
    }
}