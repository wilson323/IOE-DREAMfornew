package net.lab1024.sa.admin.module.smart.monitor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.annotation.SaCheckLogin;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;

import java.util.HashMap;
import net.lab1024.sa.admin.module.smart.monitor.service.AccessMonitorService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 门禁实时监控控制器
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - Controller只做参数验证和调用Service
 * - 统一异常处理和响应格式
 * - 完整的权限控制和参数验证
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/api/smart/monitor")
@Tag(name = "门禁实时监控", description = "门禁实时监控相关接口")
@Validated
public class AccessMonitorController {

    @Resource
    private AccessMonitorService accessMonitorService;

    /**
     * 获取监控仪表板概览
     */
    @GetMapping("/dashboard/overview")
    @Operation(summary = "获取监控仪表板概览", description = "获取门禁系统实时监控仪表板的总体概览信息")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:dashboard")
    public ResponseDTO<Map<String, Object>> getDashboardOverview() {
        log.info("获取监控仪表板概览");

        try {
            Map<String, Object> overview = accessMonitorService.getDashboardOverview();
            return SmartResponseUtil.success(overview);

        } catch (Exception e) {
            log.error("获取监控仪表板概览失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取监控仪表板概览失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 获取实时统计数据
     */
    @GetMapping("/statistics/realtime")
    @Operation(summary = "获取实时统计数据", description = "获取门禁系统的实时统计数据")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:statistics")
    public ResponseDTO<Map<String, Object>> getRealtimeStatistics() {
        log.info("获取实时统计数据");

        try {
            Map<String, Object> statistics = accessMonitorService.getRealtimeStatistics();
            return SmartResponseUtil.success(statistics);

        } catch (Exception e) {
            log.error("获取实时统计数据失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取实时统计数据失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 获取设备状态统计
     */
    @GetMapping("/statistics/devices")
    @Operation(summary = "获取设备状态统计", description = "获取门禁设备的状态分布统计信息")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:devices")
    public ResponseDTO<Map<String, Object>> getDeviceStatistics() {
        log.info("获取设备状态统计");

        try {
            Map<String, Object> statistics = accessMonitorService.getDeviceStatistics();
            return SmartResponseUtil.success(statistics);

        } catch (Exception e) {
            log.error("获取设备状态统计失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取设备状态统计失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 获取区域访问统计
     */
    @GetMapping("/statistics/areas")
    @Operation(summary = "获取区域访问统计", description = "获取各区域的访问次数统计信息")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:areas")
    public ResponseDTO<Map<String, Object>> getAreaStatistics(
            @Parameter(description = "统计开始时间", example = "2025-11-16 00:00:00")
            @RequestParam(required = false) String startTime,

            @Parameter(description = "统计结束时间", example = "2025-11-16 23:59:59")
            @RequestParam(required = false) String endTime,

            @Parameter(description = "区域ID", example = "1")
            @RequestParam(required = false) Long areaId) {
        log.info("获取区域访问统计，startTime: {}, endTime: {}, areaId: {}", startTime, endTime, areaId);

        try {
            Map<String, Object> statistics = accessMonitorService.getAreaStatistics(startTime, endTime, areaId);
            return SmartResponseUtil.success(statistics);

        } catch (Exception e) {
            log.error("获取区域访问统计失败，startTime: {}, endTime: {}, areaId: {}", startTime, endTime, areaId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取区域访问统计失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 获取访问趋势数据
     */
    @GetMapping("/trends/access")
    @Operation(summary = "获取访问趋势数据", description = "获取指定时间范围内的访问趋势统计")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:trends")
    public ResponseDTO<Map<String, Object>> getAccessTrend(
            @Parameter(description = "统计时间范围：hour/day/week/month", example = "day")
            @RequestParam(defaultValue = "day") String timeRange,

            @Parameter(description = "时间跨度（数量）", example = "7")
            @RequestParam(defaultValue = "7") Integer timeSpan,

            @Parameter(description = "开始时间", example = "2025-11-16 00:00:00")
            @RequestParam(required = false) String startTime,

            @Parameter(description = "结束时间", example = "2025-11-16 23:59:59")
            @RequestParam(required = false) String endTime) {
        log.info("获取访问趋势数据，timeRange: {}, timeSpan: {}, startTime: {}, endTime: {}",
                timeRange, timeSpan, startTime, endTime);

        try {
            Map<String, Object> trendData = accessMonitorService.getAccessTrend(timeRange, timeSpan, startTime, endTime);
            return SmartResponseUtil.success(trendData);

        } catch (Exception e) {
            log.error("获取访问趋势数据失败，timeRange: {}, timeSpan: {}", timeRange, timeSpan, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取访问趋势数据失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 获取实时告警列表
     */
    @GetMapping("/alerts/realtime")
    @Operation(summary = "获取实时告警列表", description = "获取当前活跃的系统告警列表")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:alerts")
    public ResponseDTO<Map<String, Object>> getRealtimeAlerts(
            @Parameter(description = "告警级别", example = "warning")
            @RequestParam(required = false) String alertLevel,

            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,

            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取实时告警列表，alertLevel: {}, pageNum: {}, pageSize: {}", alertLevel, pageNum, pageSize);

        try {
            Map<String, Object> alerts = accessMonitorService.getRealtimeAlerts(alertLevel, pageNum, pageSize);
            return SmartResponseUtil.success(alerts);

        } catch (Exception e) {
            log.error("获取实时告警列表失败，alertLevel: {}, pageNum: {}, pageSize: {}", alertLevel, pageNum, pageSize, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取实时告警列表失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 获取WebSocket连接统计
     */
    @GetMapping("/websocket/stats")
    @Operation(summary = "获取WebSocket连接统计", description = "获取当前WebSocket连接的统计信息")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:websocket")
    public ResponseDTO<Map<String, Object>> getWebSocketStats() {
        log.info("获取WebSocket连接统计");

        try {
            Map<String, Object> stats = accessMonitorService.getWebSocketStats();
            return SmartResponseUtil.success(stats);

        } catch (Exception e) {
            log.error("获取WebSocket连接统计失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取WebSocket连接统计失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health/status")
    @Operation(summary = "获取系统健康状态", description = "获取门禁监控系统的整体健康状态")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:health")
    public ResponseDTO<Map<String, Object>> getHealthStatus() {
        log.info("获取系统健康状态");

        try {
            Map<String, Object> healthStatus = accessMonitorService.getHealthStatus();
            return SmartResponseUtil.success(healthStatus);

        } catch (Exception e) {
            log.error("获取系统健康状态失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "获取系统健康状态失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 手动刷新统计数据
     */
    @PostMapping("/statistics/refresh")
    @Operation(summary = "手动刷新统计数据", description = "手动触发统计数据刷新")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:refresh")
    public ResponseDTO<Map<String, Object>> refreshStatistics() {
        log.info("手动刷新统计数据");

        try {
            accessMonitorService.refreshStatistics();
            Map<String, Object> successResult = new HashMap<>();
            successResult.put("message", "统计数据刷新成功");
            successResult.put("success", true);
            return SmartResponseUtil.success(successResult);

        } catch (Exception e) {
            log.error("手动刷新统计数据失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "手动刷新统计数据失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 清除历史数据
     */
    @PostMapping("/data/clear")
    @Operation(summary = "清除历史数据", description = "清除指定时间范围的历史监控数据")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:clear")
    public ResponseDTO<Map<String, Object>> clearHistoryData(
            @Parameter(description = "数据类型", example = "events")
            @RequestParam String dataType,

            @Parameter(description = "保留天数", example = "30")
            @RequestParam(defaultValue = "30") Integer retainDays) {
        log.info("清除历史数据，dataType: {}, retainDays: {}", dataType, retainDays);

        try {
            accessMonitorService.clearHistoryData(dataType, retainDays);
            Map<String, Object> successResult = new HashMap<>();
            successResult.put("message", "历史数据清除成功");
            successResult.put("success", true);
            return SmartResponseUtil.success(successResult);

        } catch (Exception e) {
            log.error("清除历史数据失败，dataType: {}, retainDays: {}", dataType, retainDays, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "清除历史数据失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 导出监控报告
     */
    @PostMapping("/report/export")
    @Operation(summary = "导出监控报告", description = "导出门禁监控数据报告")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:export")
    public ResponseDTO<Map<String, Object>> exportReport(
            @Parameter(description = "报告类型", example = "daily")
            @RequestParam String reportType,

            @Parameter(description = "开始时间", example = "2025-11-16 00:00:00")
            @RequestParam String startTime,

            @Parameter(description = "结束时间", example = "2025-11-16 23:59:59")
            @RequestParam String endTime,

            @Parameter(description = "导出格式", example = "excel")
            @RequestParam(defaultValue = "excel") String format) {
        log.info("导出监控报告，reportType: {}, startTime: {}, endTime: {}, format: {}",
                reportType, startTime, endTime, format);

        try {
            Map<String, Object> reportData = accessMonitorService.exportReport(reportType, startTime, endTime, format);
            return SmartResponseUtil.success(reportData);

        } catch (Exception e) {
            log.error("导出监控报告失败，reportType: {}, startTime: {}, endTime: {}, format: {}",
                    reportType, startTime, endTime, format, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "导出监控报告失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }

    /**
     * 测试WebSocket连接
     */
    @PostMapping("/websocket/test")
    @Operation(summary = "测试WebSocket连接", description = "向所有连接的WebSocket客户端发送测试消息")
    @SaCheckLogin
    @SaCheckPermission("smart:monitor:test")
    public ResponseDTO<Map<String, Object>> testWebSocket() {
        log.info("测试WebSocket连接");

        try {
            accessMonitorService.sendWebSocketTestMessage();
            Map<String, Object> successResult = new HashMap<>();
            successResult.put("message", "WebSocket测试消息已发送");
            successResult.put("success", true);
            return SmartResponseUtil.success(successResult);

        } catch (Exception e) {
            log.error("测试WebSocket连接失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "测试WebSocket连接失败: " + e.getMessage());
            errorResult.put("success", false);
            return SmartResponseUtil.success(errorResult);
        }
    }
}