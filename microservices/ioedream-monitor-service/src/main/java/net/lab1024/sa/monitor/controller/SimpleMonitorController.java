package net.lab1024.sa.monitor.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 简化监控控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@Validated
public class SimpleMonitorController {

    /**
     * 系统健康检查
     */
    @GetMapping("/health")
    public ResponseDTO<Map<String, Object>> systemHealth() {
        log.info("获取系统健康状态");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("uptime", System.currentTimeMillis());
        health.put("cpuUsage", 45.2);
        health.put("memoryUsage", 67.8);
        health.put("diskUsage", 32.1);

        return ResponseDTO.ok(health);
    }

    /**
     * 系统指标
     */
    @GetMapping("/metrics")
    public ResponseDTO<Map<String, Object>> systemMetrics() {
        log.info("获取系统指标");

        Map<String, Object> metrics = new HashMap<>();

        // 系统资源
        metrics.put("cpu", Map.of(
                "usage", 45.2,
                "cores", 8,
                "load", 3.2));

        metrics.put("memory", Map.of(
                "total", 16384,
                "used", 11091,
                "free", 5293,
                "usagePercent", 67.8));

        metrics.put("disk", Map.of(
                "total", 500000,
                "used", 160500,
                "free", 339500,
                "usagePercent", 32.1));

        // 应用指标
        metrics.put("applications", Map.of(
                "totalServices", 12,
                "activeServices", 11,
                "failedServices", 1,
                "responseTime", 125.5));

        metrics.put("database", Map.of(
                "connections", 15,
                "maxConnections", 100,
                "usagePercent", 15.0,
                "status", "HEALTHY"));

        metrics.put("cache", Map.of(
                "redis", Map.of("status", "UP", "hitRate", 85.2),
                "localCache", Map.of("hitRate", 92.1)));

        return ResponseDTO.ok(metrics);
    }

    /**
     * 服务状态
     */
    @GetMapping("/services")
    public ResponseDTO<List<Map<String, Object>>> serviceStatus() {
        log.info("获取服务状态");

        List<Map<String, Object>> services = new ArrayList<>();

        String[] serviceNames = {
                "ioedream-auth-service",
                "ioedream-identity-service",
                "ioedream-device-service",
                "ioedream-access-service",
                "ioedream-consume-service",
                "ioedream-attendance-service",
                "ioedream-video-service",
                "ioedream-visitor-service",
                "smart-gateway"
        };

        for (String serviceName : serviceNames) {
            Map<String, Object> service = new HashMap<>();
            service.put("serviceName", serviceName);
            service.put("status", Math.random() > 0.1 ? "UP" : "DOWN");
            service.put("port", 8080 + (int) (Math.random() * 100));
            service.put("responseTime", 50 + (int) (Math.random() * 200));
            service.put("lastCheck", LocalDateTime.now());
            service.put("uptime", (long) (Math.random() * 86400));
            services.add(service);
        }

        return ResponseDTO.ok(services);
    }

    /**
     * 告警信息
     */
    @GetMapping("/alerts")
    public ResponseDTO<List<Map<String, Object>>> alerts() {
        log.info("获取告警信息");

        List<Map<String, Object>> alertList = new ArrayList<>();

        // 模拟一些告警
        String[] alertTypes = { "CPU高使用率", "内存警告", "磁盘空间不足", "服务响应慢", "数据库连接异常" };
        String[] severities = { "CRITICAL", "WARNING", "INFO", "WARNING", "CRITICAL" };

        for (int i = 0; i < 5; i++) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", (long) (i + 1));
            alert.put("type", alertTypes[i]);
            alert.put("severity", severities[i]);
            alert.put("message", alertTypes[i] + " - 需要关注");
            alert.put("source", "System");
            alert.put("timestamp", LocalDateTime.now().minusMinutes(i * 10));
            alert.put("status", Math.random() > 0.5 ? "ACTIVE" : "RESOLVED");
            alertList.add(alert);
        }

        return ResponseDTO.ok(alertList);
    }

    /**
     * 系统日志
     */
    @GetMapping("/logs")
    public ResponseDTO<List<Map<String, Object>>> systemLogs(
            @RequestParam(defaultValue = "INFO") String level,
            @RequestParam(defaultValue = "50") Integer limit) {

        log.info("获取系统日志，级别: {}, 限制: {}", level, limit);

        List<Map<String, Object>> logs = new ArrayList<>();

        String[] logLevels = { "INFO", "WARN", "ERROR", "DEBUG" };
        String[] messages = {
                "服务启动成功",
                "数据库连接建立",
                "缓存初始化完成",
                "系统健康检查通过",
                "用户认证成功"
        };

        for (int i = 0; i < Math.min(limit, 20); i++) {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("id", (long) (i + 1));
            logEntry.put("level", logLevels[i % logLevels.length]);
            logEntry.put("message", messages[i % messages.length]);
            logEntry.put("timestamp", LocalDateTime.now().minusMinutes(i * 5));
            logEntry.put("source", "System");
            logEntry.put("thread", "main-" + (i + 1));
            logs.add(logEntry);
        }

        return ResponseDTO.ok(logs);
    }

    /**
     * 监控服务健康检查
     */
    @GetMapping("/monitor/health")
    public ResponseDTO<Map<String, Object>> monitorHealth() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "ioedream-monitor-service",
                "port", 8097,
                "timestamp", LocalDateTime.now(),
                "apiCount", 6);

        return ResponseDTO.ok(health);
    }
}
