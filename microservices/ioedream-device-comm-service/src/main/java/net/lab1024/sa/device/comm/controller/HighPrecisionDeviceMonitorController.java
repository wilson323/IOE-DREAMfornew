package net.lab1024.sa.device.comm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.device.comm.monitor.HighPrecisionDeviceMonitor;
import net.lab1024.sa.device.comm.service.HighPrecisionDeviceMonitorService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 高精度设备监控控制器
 * <p>
 * 严格遵循四层架构规范的Controller层实现：
 * - 位于Controller层，负责HTTP接口暴露和参数验证
 * - 使用@Resource注解进行依赖注入
 * - 使用@Valid进行参数验证
 * - 调用Service层处理业务逻辑
 * - 统一异常处理和响应封装
 * </p>
 * <p>
 * 提供高精度设备监控的RESTful API：
 * 1. 设备实时状态查询和监控
 * 2. 设备状态历史数据管理
 * 3. 设备性能统计分析
 * 4. 批量设备状态监控
 * 5. 监控配置动态管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/device/monitor/high-precision")
@Tag(name = "高精度设备监控", description = "高精度设备状态监控管理接口")
public class HighPrecisionDeviceMonitorController {

    @Resource
    private HighPrecisionDeviceMonitorService highPrecisionDeviceMonitorService;

    @Operation(summary = "获取设备实时状态", description = "获取指定设备的实时状态快照")
    @GetMapping("/device/{deviceId}/realtime-status")
    public ResponseDTO<HighPrecisionDeviceMonitor.DeviceStatusSnapshot> getDeviceRealTimeStatus(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {
        try {
            HighPrecisionDeviceMonitor.DeviceStatusSnapshot status =
                    highPrecisionDeviceMonitorService.getDeviceRealTimeStatus(deviceId);
            return ResponseDTO.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error("PARAM_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("[高精度监控控制器] 获取设备实时状态失败, deviceId={}", deviceId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取设备实时状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "异步监控设备状态", description = "异步监控指定设备的状态")
    @PostMapping("/device/{deviceId}/monitor-async")
    public ResponseDTO<String> monitorDeviceAsync(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {
        try {
            CompletableFuture<HighPrecisionDeviceMonitor.DeviceStatusSnapshot> future =
                    highPrecisionDeviceMonitorService.monitorDeviceAsync(deviceId);

            // 异步处理，立即返回
            return ResponseDTO.ok("异步监控已启动，设备ID: " + deviceId);
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error("PARAM_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("[高精度监控控制器] 启动异步监控失败, deviceId={}", deviceId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "启动异步监控失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备状态历史", description = "获取指定设备的状态历史记录")
    @GetMapping("/device/{deviceId}/history")
    public ResponseDTO<List<HighPrecisionDeviceMonitor.DeviceStatusSnapshot>> getDeviceStatusHistory(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "获取数量", example = "100") @RequestParam(defaultValue = "100") int count) {
        try {
            List<HighPrecisionDeviceMonitor.DeviceStatusSnapshot> history =
                    highPrecisionDeviceMonitorService.getDeviceStatusHistory(deviceId, count);
            return ResponseDTO.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error("PARAM_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("[高精度监控控制器] 获取设备状态历史失败, deviceId={}, count={}", deviceId, count, e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取设备状态历史失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取性能统计", description = "获取高精度监控的性能统计信息")
    @GetMapping("/performance-statistics")
    public ResponseDTO<Map<String, Object>> getPerformanceStatistics() {
        try {
            Map<String, Object> statistics = highPrecisionDeviceMonitorService.getPerformanceStatistics();
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[高精度监控控制器] 获取性能统计失败", e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取性能统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量监控设备", description = "批量监控多个设备的状态")
    @PostMapping("/devices/batch-monitor")
    public ResponseDTO<Map<String, HighPrecisionDeviceMonitor.DeviceStatusSnapshot>> batchMonitorDevices(
            @Parameter(description = "批量监控请求", required = true)
            @RequestBody @Valid BatchMonitorRequest request) {
        try {
            Map<String, HighPrecisionDeviceMonitor.DeviceStatusSnapshot> results =
                    highPrecisionDeviceMonitorService.batchMonitorDevices(request.getDeviceIds());
            return ResponseDTO.ok(results);
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error("PARAM_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("[高精度监控控制器] 批量监控设备失败, deviceCount={}",
                    request.getDeviceIds() != null ? request.getDeviceIds().size() : 0, e);
            return ResponseDTO.error("SYSTEM_ERROR", "批量监控设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新设备监控配置", description = "更新指定设备的监控配置")
    @PostMapping("/device/{deviceId}/config")
    public ResponseDTO<Void> updateDeviceMonitorConfig(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "监控配置", required = true)
            @RequestBody @Valid HighPrecisionDeviceMonitor.DeviceMonitorConfig config) {
        try {
            highPrecisionDeviceMonitorService.updateDeviceMonitorConfig(deviceId, config);
            return ResponseDTO.ok();
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error("PARAM_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("[高精度监控控制器] 更新设备监控配置失败, deviceId={}", deviceId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "更新设备监控配置失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备状态概览", description = "获取所有设备的监控状态概览")
    @GetMapping("/status-overview")
    public ResponseDTO<Map<String, Object>> getDeviceStatusOverview() {
        try {
            Map<String, Object> overview = highPrecisionDeviceMonitorService.getDeviceStatusOverview();
            return ResponseDTO.ok(overview);
        } catch (Exception e) {
            log.error("[高精度监控控制器] 获取设备状态概览失败", e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取设备状态概览失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取问题设备列表", description = "获取需要关注的设备列表")
    @GetMapping("/problematic-devices")
    public ResponseDTO<List<String>> getProblematicDevices() {
        try {
            List<String> problematicDevices = highPrecisionDeviceMonitorService.getProblematicDevices();
            return ResponseDTO.ok(problematicDevices);
        } catch (Exception e) {
            log.error("[高精度监控控制器] 获取问题设备列表失败", e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取问题设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "启动高精度监控", description = "为指定设备启动高精度监控")
    @PostMapping("/devices/start-high-precision")
    public ResponseDTO<Void> startHighPrecisionMonitoring(
            @Parameter(description = "启动高精度监控请求", required = true)
            @RequestBody @Valid StartHighPrecisionRequest request) {
        try {
            highPrecisionDeviceMonitorService.startHighPrecisionMonitoring(request.getDeviceIds());
            return ResponseDTO.ok();
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error("PARAM_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("[高精度监控控制器] 启动高精度监控失败, deviceCount={}",
                    request.getDeviceIds() != null ? request.getDeviceIds().size() : 0, e);
            return ResponseDTO.error("SYSTEM_ERROR", "启动高精度监控失败: " + e.getMessage());
        }
    }

    @Operation(summary = "停止高精度监控", description = "为指定设备停止高精度监控")
    @PostMapping("/devices/stop-high-precision")
    public ResponseDTO<Void> stopHighPrecisionMonitoring(
            @Parameter(description = "停止高精度监控请求", required = true)
            @RequestBody @Valid StopHighPrecisionRequest request) {
        try {
            highPrecisionDeviceMonitorService.stopHighPrecisionMonitoring(request.getDeviceIds());
            return ResponseDTO.ok();
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error("PARAM_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("[高精度监控控制器] 停止高精度监控失败, deviceCount={}",
                    request.getDeviceIds() != null ? request.getDeviceIds().size() : 0, e);
            return ResponseDTO.error("SYSTEM_ERROR", "停止高精度监控失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备实时指标", description = "获取指定设备的实时性能指标")
    @GetMapping("/device/{deviceId}/realtime-metrics")
    public ResponseDTO<Map<String, Object>> getDeviceRealTimeMetrics(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {
        try {
            HighPrecisionDeviceMonitor.DeviceStatusSnapshot snapshot =
                    highPrecisionDeviceMonitorService.getDeviceRealTimeStatus(deviceId);

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("deviceId", deviceId);
            metrics.put("timestamp", snapshot.getTimestamp());
            metrics.put("status", snapshot.getStatus());
            metrics.put("healthLevel", snapshot.getHealthLevel());
            metrics.put("responseTimeMs", snapshot.getResponseTimeMs());
            metrics.put("cpuUsage", snapshot.getCpuUsage());
            metrics.put("memoryUsage", snapshot.getMemoryUsage());
            metrics.put("networkLatency", snapshot.getNetworkLatency());
            metrics.put("connectionCount", snapshot.getConnectionCount());
            metrics.put("extendedAttributes", snapshot.getExtendedAttributes());

            return ResponseDTO.ok(metrics);
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error("PARAM_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("[高精度监控控制器] 获取设备实时指标失败, deviceId={}", deviceId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取设备实时指标失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取监控健康检查", description = "检查高精度监控系统的健康状态")
    @GetMapping("/health-check")
    public ResponseDTO<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> healthStatus = new HashMap<>();

            // 获取性能统计
            Map<String, Object> statistics = highPrecisionDeviceMonitorService.getPerformanceStatistics();
            healthStatus.put("performanceStatistics", statistics);

            // 检查系统状态
            long totalMonitors = (Long) statistics.get("totalMonitors");
            long successMonitors = (Long) statistics.get("successMonitors");
            double successRate = (Double) statistics.get("successRate");

            String systemStatus = "HEALTHY";
            if (successRate < 90.0) {
                systemStatus = "WARNING";
            } else if (successRate < 70.0) {
                systemStatus = "ERROR";
            }

            healthStatus.put("systemStatus", systemStatus);
            healthStatus.put("checkTime", new java.util.Date());
            healthStatus.put("uptime", System.currentTimeMillis());

            return ResponseDTO.ok(healthStatus);
        } catch (Exception e) {
            log.error("[高精度监控控制器] 健康检查失败", e);

            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("systemStatus", "ERROR");
            errorStatus.put("errorMessage", e.getMessage());
            errorStatus.put("checkTime", new java.util.Date());

            return ResponseDTO.error("HEALTH_CHECK_ERROR", "健康检查失败", errorStatus);
        }
    }

    // ==================== 内部类 ====================

    /**
     * 批量监控请求
     */
    @Schema(description = "批量监控请求")
    public static class BatchMonitorRequest {
        @Schema(description = "设备ID列表", required = true)
        private List<String> deviceIds;

        // getters and setters
        public List<String> getDeviceIds() { return deviceIds; }
        public void setDeviceIds(List<String> deviceIds) { this.deviceIds = deviceIds; }
    }

    /**
     * 启动高精度监控请求
     */
    @Schema(description = "启动高精度监控请求")
    public static class StartHighPrecisionRequest {
        @Schema(description = "设备ID列表", required = true)
        private List<String> deviceIds;

        // getters and setters
        public List<String> getDeviceIds() { return deviceIds; }
        public void setDeviceIds(List<String> deviceIds) { this.deviceIds = deviceIds; }
    }

    /**
     * 停止高精度监控请求
     */
    @Schema(description = "停止高精度监控请求")
    public static class StopHighPrecisionRequest {
        @Schema(description = "设备ID列表", required = true)
        private List<String> deviceIds;

        // getters and setters
        public List<String> getDeviceIds() { return deviceIds; }
        public void setDeviceIds(List<String> deviceIds) { this.deviceIds = deviceIds; }
    }
}
