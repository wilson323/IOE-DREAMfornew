package net.lab1024.sa.attendance.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.monitor.ApiPerformanceMonitor;
import net.lab1024.sa.attendance.monitor.ApiPerformanceMonitor.ApiStatistics;
import net.lab1024.sa.attendance.monitor.SlowQueryMonitor;
import net.lab1024.sa.attendance.monitor.SystemResourceMonitor;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 性能监控控制器
 * <p>
 * 提供性能监控数据查询接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/monitor")
@Tag(name = "性能监控")
public class PerformanceMonitorController {

    @Resource
    private ApiPerformanceMonitor performanceMonitor;

    @Resource
    private SlowQueryMonitor slowQueryMonitor;

    @Resource
    private SystemResourceMonitor systemResourceMonitor;

    /**
     * 获取API性能统计
     */
    @GetMapping("/api-performance")
    @Operation(summary = "获取API性能统计")
    public ResponseDTO<ConcurrentHashMap<String, ApiStatistics>> getApiPerformance() {
        log.info("[性能监控] 查询API性能统计");

        ConcurrentHashMap<String, ApiStatistics> statistics = performanceMonitor.getAllApiStatistics();

        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取慢查询统计
     */
    @GetMapping("/slow-queries")
    @Operation(summary = "获取慢查询统计")
    public ResponseDTO<ConcurrentHashMap<String, SlowQueryMonitor.SqlStatistics>> getSlowQueries() {
        log.info("[性能监控] 查询慢查询统计");

        ConcurrentHashMap<String, SlowQueryMonitor.SqlStatistics> statistics = slowQueryMonitor.getAllSqlStatistics();

        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取系统资源监控
     */
    @GetMapping("/system-resource")
    @Operation(summary = "获取系统资源监控")
    public ResponseDTO<Map<String, Object>> getSystemResource() {
        log.info("[性能监控] 查询系统资源监控");

        Map<String, Object> resourceInfo = systemResourceMonitor.getPerformanceSummary();

        return ResponseDTO.ok(resourceInfo);
    }

    /**
     * 获取内存信息
     */
    @GetMapping("/memory")
    @Operation(summary = "获取内存信息")
    public ResponseDTO<Map<String, Object>> getMemoryInfo() {
        log.info("[性能监控] 查询内存信息");

        Map<String, Object> memoryInfo = systemResourceMonitor.getMemoryInfo();

        return ResponseDTO.ok(memoryInfo);
    }

    /**
     * 获取线程信息
     */
    @GetMapping("/thread")
    @Operation(summary = "获取线程信息")
    public ResponseDTO<Map<String, Object>> getThreadInfo() {
        log.info("[性能监控] 查询线程信息");

        Map<String, Object> threadInfo = systemResourceMonitor.getThreadInfo();

        return ResponseDTO.ok(threadInfo);
    }

    /**
     * 获取JVM信息
     */
    @GetMapping("/jvm")
    @Operation(summary = "获取JVM信息")
    public ResponseDTO<Map<String, Object>> getJvmInfo() {
        log.info("[性能监控] 查询JVM信息");

        Map<String, Object> jvmInfo = systemResourceMonitor.getJvmInfo();

        return ResponseDTO.ok(jvmInfo);
    }

    /**
     * 获取性能监控概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取性能监控概览")
    public ResponseDTO<Map<String, Object>> getPerformanceOverview() {
        log.info("[性能监控] 查询性能监控概览");

        Map<String, Object> overview = new java.util.HashMap<>();
        overview.put("timestamp", java.time.LocalDateTime.now().toString());

        // API性能概览
        ConcurrentHashMap<String, ApiStatistics> apiStats = performanceMonitor.getAllApiStatistics();
        long totalRequests = apiStats.values().stream().mapToLong(ApiStatistics::getTotalRequests).sum();
        long totalErrors = apiStats.values().stream().mapToLong(ApiStatistics::getErrorRequests).sum();
        long totalSlowRequests = apiStats.values().stream().mapToLong(ApiStatistics::getSlowRequests).sum();

        Map<String, Object> apiOverview = new HashMap<>();
        apiOverview.put("totalRequests", totalRequests);
        apiOverview.put("totalErrors", totalErrors);
        apiOverview.put("totalSlowRequests", totalSlowRequests);
        apiOverview.put("errorRate", totalRequests > 0 ? String.format("%.2f%%", (double) totalErrors / totalRequests * 100) : "0%");
        apiOverview.put("slowRequestRate", totalRequests > 0 ? String.format("%.2f%%", (double) totalSlowRequests / totalRequests * 100) : "0%");
        overview.put("apiOverview", apiOverview);

        // 慢查询概览
        ConcurrentHashMap<String, SlowQueryMonitor.SqlStatistics> sqlStats = slowQueryMonitor.getAllSqlStatistics();
        long totalSqlExecutions = sqlStats.values().stream().mapToLong(SlowQueryMonitor.SqlStatistics::getExecutionCount).sum();
        long totalSlowQueries = sqlStats.values().stream().mapToLong(SlowQueryMonitor.SqlStatistics::getSlowQueryCount).sum();

        Map<String, Object> sqlOverview = new HashMap<>();
        sqlOverview.put("totalSqlExecutions", totalSqlExecutions);
        sqlOverview.put("totalSlowQueries", totalSlowQueries);
        sqlOverview.put("slowQueryRate", totalSqlExecutions > 0 ? String.format("%.2f%%", (double) totalSlowQueries / totalSqlExecutions * 100) : "0%");
        overview.put("sqlOverview", sqlOverview);

        // 系统资源概览
        overview.put("systemResource", systemResourceMonitor.getPerformanceSummary());

        return ResponseDTO.ok(overview);
    }
}
