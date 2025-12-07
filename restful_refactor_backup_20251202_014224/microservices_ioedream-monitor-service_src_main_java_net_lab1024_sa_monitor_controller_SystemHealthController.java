package net.lab1024.sa.monitor.controller;

import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.monitor.domain.vo.SystemHealthVO;
import net.lab1024.sa.monitor.service.SystemHealthService;

/**
 * 系统健康监控控制器
 * 负责系统运行状态监控、健康检查、性能指标监控等功能
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/health")
@Tag(name = "系统健康监控", description = "系统运行状态监控、健康检查、性能指标监控等功能")
@Validated
public class SystemHealthController {

    @Resource
    private SystemHealthService systemHealthService;

    @GetMapping("/overview")
    @Operation(summary = "系统健康概览", description = "获取系统整体健康状况概览")
    public ResponseDTO<SystemHealthVO> getSystemHealthOverview() {
        log.info("获取系统健康概览");

        SystemHealthVO healthOverview = systemHealthService.getSystemHealthOverview();

        log.info("系统健康概览获取完成，健康状态：{}", healthOverview.getOverallStatus());

        return ResponseDTO.ok(healthOverview);
    }

    @GetMapping("/components")
    @Operation(summary = "组件健康状态", description = "获取各个系统组件的健康状态")
    public ResponseDTO<List<Map<String, Object>>> getComponentHealthStatus() {
        log.info("获取组件健康状态");

        List<Map<String, Object>> componentStatus = systemHealthService.getComponentHealthStatus();

        log.info("组件健康状态获取完成，组件数量：{}", componentStatus.size());

        return ResponseDTO.ok(componentStatus);
    }

    @GetMapping("/metrics")
    @Operation(summary = "系统性能指标", description = "获取系统关键性能指标")
    public ResponseDTO<Map<String, Object>> getSystemMetrics() {
        log.info("获取系统性能指标");

        Map<String, Object> metrics = systemHealthService.getSystemMetrics();

        log.info("系统性能指标获取完成");

        return ResponseDTO.ok(metrics);
    }

    @GetMapping("/resource/usage")
    @Operation(summary = "资源使用情况", description = "获取CPU、内存、磁盘等资源使用情况")
    public ResponseDTO<Map<String, Object>> getResourceUsage() {
        log.info("获取系统资源使用情况");

        Map<String, Object> resourceUsage = systemHealthService.getResourceUsage();

        log.info("系统资源使用情况获取完成");

        return ResponseDTO.ok(resourceUsage);
    }

    @GetMapping("/services/status")
    @Operation(summary = "微服务状态", description = "获取所有微服务的运行状态")
    public ResponseDTO<List<Map<String, Object>>> getMicroservicesStatus() {
        log.info("获取微服务状态");

        List<Map<String, Object>> servicesStatus = systemHealthService.getMicroservicesStatus();

        log.info("微服务状态获取完成，服务数量：{}", servicesStatus.size());

        return ResponseDTO.ok(servicesStatus);
    }

    @GetMapping("/database/status")
    @Operation(summary = "数据库状态", description = "获取数据库连接状态和性能指标")
    public ResponseDTO<Map<String, Object>> getDatabaseStatus() {
        log.info("获取数据库状态");

        Map<String, Object> databaseStatus = systemHealthService.getDatabaseStatus();

        log.info("数据库状态获取完成");

        return ResponseDTO.ok(databaseStatus);
    }

    @GetMapping("/cache/status")
    @Operation(summary = "缓存状态", description = "获取Redis缓存状态和性能指标")
    public ResponseDTO<Map<String, Object>> getCacheStatus() {
        log.info("获取缓存状态");

        Map<String, Object> cacheStatus = systemHealthService.getCacheStatus();

        log.info("缓存状态获取完成");

        return ResponseDTO.ok(cacheStatus);
    }

    @PostMapping("/check/{component}")
    @Operation(summary = "手动健康检查", description = "对指定组件执行健康检查")
    public ResponseDTO<Map<String, Object>> performHealthCheck(@PathVariable String component) {
        log.info("执行手动健康检查，组件：{}", component);

        Map<String, Object> checkResult = systemHealthService.performHealthCheck(component);

        log.info("健康检查完成，组件：{}，状态：{}", component, checkResult.get("status"));

        return ResponseDTO.ok(checkResult);
    }

    @GetMapping("/alerts/active")
    @Operation(summary = "活跃告警", description = "获取当前活跃的告警信息")
    public ResponseDTO<List<Map<String, Object>>> getActiveAlerts() {
        log.info("获取活跃告警信息");

        List<Map<String, Object>> activeAlerts = systemHealthService.getActiveAlerts();

        log.info("活跃告警信息获取完成，告警数量：{}", activeAlerts.size());

        return ResponseDTO.ok(activeAlerts);
    }

    @GetMapping("/uptime")
    @Operation(summary = "系统运行时间", description = "获取系统和各服务的运行时间统计")
    public ResponseDTO<Map<String, Object>> getSystemUptime() {
        log.info("获取系统运行时间统计");

        Map<String, Object> uptime = systemHealthService.getSystemUptime();

        log.info("系统运行时间统计获取完成");

        return ResponseDTO.ok(uptime);
    }

    @GetMapping("/history/trends")
    @Operation(summary = "健康趋势", description = "获取系统健康状态的历史趋势数据")
    public ResponseDTO<List<Map<String, Object>>> getHealthTrends(@RequestParam(defaultValue = "24") Integer hours) {
        log.info("获取系统健康趋势，时间范围：{}小时", hours);

        List<Map<String, Object>> trends = systemHealthService.getHealthTrends(hours);

        log.info("系统健康趋势获取完成，数据点数：{}", trends.size());

        return ResponseDTO.ok(trends);
    }

    @PostMapping("/alert/resolve/{alertId}")
    @Operation(summary = "解决告警", description = "手动标记告警为已解决")
    public ResponseDTO<Void> resolveAlert(@PathVariable Long alertId,
            @RequestParam(required = false) String resolution) {
        log.info("解决告警，告警ID：{}，解决说明：{}", alertId, resolution);

        systemHealthService.resolveAlert(alertId, resolution);

        log.info("告警解决完成");

        return ResponseDTO.ok();
    }
}