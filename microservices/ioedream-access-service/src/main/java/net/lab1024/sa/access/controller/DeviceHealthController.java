package net.lab1024.sa.access.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpMethod;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.access.service.DeviceHealthService;
import net.lab1024.sa.access.domain.form.DeviceMonitorRequest;
import net.lab1024.sa.access.domain.form.MaintenancePredictRequest;
import net.lab1024.sa.access.domain.vo.DeviceHealthVO;
import net.lab1024.sa.access.domain.vo.DevicePerformanceAnalyticsVO;
import net.lab1024.sa.access.domain.vo.MaintenancePredictionVO;

/**
 * 设备健康监控控制器
 * <p>
 * 提供设备健康状态监控、性能分析、预测性维护等企业级功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 统一使用@Resource依赖注入
 * - 完整的API文档注解
 * - 权限控制注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@PermissionCheck(value = "ACCESS_MANAGE", description = "设备健康监控权限")
@RequestMapping("/api/v1/device/health")
@Tag(name = "设备健康监控", description = "设备健康状态监控、性能分析和预测性维护")
public class DeviceHealthController {

    /**
     * 设备健康服务
     */
    @Resource
    private DeviceHealthService deviceHealthService;

    /**
     * 网关服务客户端
     */
    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 设备健康监控
     * <p>
     * 实时监控指定设备的健康状态，包括：
     * - 设备在线状态
     * - 网络连接质量
     * - 响应时间监控
     * - 错误率统计
     * </p>
     *
     * @param request 设备监控请求
     * @return 设备健康状态信息
     */
    @PostMapping("/monitor")
    @Operation(summary = "设备健康监控", description = "实时监控设备健康状态，包括在线状态、网络质量、响应时间等指标")
    @PermissionCheck(value = {"ACCESS_MANAGER", "DEVICE_MONITOR"}, description = "设备健康监控操作")
    public ResponseDTO<DeviceHealthVO> monitorDeviceHealth(
            @Valid @RequestBody DeviceMonitorRequest request) {
        log.info("[设备健康监控] 开始监控设备，deviceId={}", request.getDeviceId());

        try {
            DeviceHealthVO result = deviceHealthService.monitorDeviceHealth(request);
            log.info("[设备健康监控] 监控完成，deviceId={}, healthScore={}",
                request.getDeviceId(), result.getHealthScore());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[设备健康监控] 监控异常，deviceId={}, error={}",
                request.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("DEVICE_HEALTH_MONITOR_ERROR", "设备健康监控失败：" + e.getMessage());
        }
    }

    /**
     * 获取设备性能分析
     * <p>
     * 分析设备的历史性能数据和趋势，包括：
     * - 响应时间趋势
     * - 成功率分析
     * - 负载分析
     * - 性能优化建议
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备性能分析结果
     */
    @GetMapping("/analytics/{deviceId}")
    @Operation(summary = "设备性能分析", description = "分析设备性能指标和趋势，提供优化建议")
    @PermissionCheck(value = {"ACCESS_MANAGER", "DEVICE_ANALYTICS"}, description = "设备性能分析")
    public ResponseDTO<DevicePerformanceAnalyticsVO> getDevicePerformanceAnalytics(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId) {
        log.info("[设备性能分析] 开始分析设备性能，deviceId={}", deviceId);

        try {
            DevicePerformanceAnalyticsVO result = deviceHealthService.getDevicePerformanceAnalytics(deviceId);
            log.info("[设备性能分析] 分析完成，deviceId={}, avgResponseTime={}ms",
                deviceId, result.getAverageResponseTime());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[设备性能分析] 分析异常，deviceId={}, error={}",
                deviceId, e.getMessage(), e);
            return ResponseDTO.error("DEVICE_PERFORMANCE_ANALYTICS_ERROR", "设备性能分析失败：" + e.getMessage());
        }
    }

    /**
     * 预测性维护分析
     * <p>
     * 基于AI算法预测设备的维护需求，包括：
     * - 故障预测
     * - 维护时间建议
     * - 维护优先级评估
     * - 成本预估
     * </p>
     *
     * @param request 维护预测请求
     * @return 维护预测结果列表
     */
    @PostMapping("/maintenance/predict")
    @Operation(summary = "预测性维护", description = "基于AI算法预测设备维护需求，优化维护计划")
    @PermissionCheck(value = {"ACCESS_MANAGER", "MAINTENANCE_PLAN"}, description = "预测性维护计划")
    public ResponseDTO<List<MaintenancePredictionVO>> predictMaintenanceNeeds(
            @Valid @RequestBody MaintenancePredictRequest request) {
        log.info("[预测性维护] 开始分析维护需求，deviceId={}, predictionDays={}",
            request.getDeviceId(), request.getPredictionDays());

        try {
            List<MaintenancePredictionVO> result = deviceHealthService.predictMaintenanceNeeds(request);
            log.info("[预测性维护] 分析完成，deviceId={}, predictionCount={}",
                request.getDeviceId(), result.size());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[预测性维护] 分析异常，deviceId={}, error={}",
                request.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("MAINTENANCE_PREDICTION_ERROR", "预测性维护分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取设备健康统计
     * <p>
     * 获取所有设备的健康状态统计信息，包括：
     * - 健康设备数量
     * - 亚健康设备数量
     * - 故障设备数量
     * - 离线设备数量
     * </p>
     *
     * @return 设备健康统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "设备健康统计", description = "获取所有设备的健康状态统计信息")
    @PermissionCheck(value = {"ACCESS_MANAGER", "DEVICE_VIEW"}, description = "设备历史数据查看")
    public ResponseDTO<Object> getDeviceHealthStatistics() {
        log.info("[设备健康统计] 开始获取统计信息");

        try {
            Object result = deviceHealthService.getDeviceHealthStatistics();
            log.info("[设备健康统计] 统计完成");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[设备健康统计] 统计异常，error={}", e.getMessage(), e);
            return ResponseDTO.error("DEVICE_HEALTH_STATISTICS_ERROR", "设备健康统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取设备健康历史数据
     * <p>
     * 获取指定设备的历史健康数据，用于趋势分析
     * </p>
     *
     * @param deviceId 设备ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 历史健康数据
     */
    @GetMapping("/history/{deviceId}")
    @Operation(summary = "设备健康历史", description = "获取设备历史健康数据用于趋势分析")
    @PermissionCheck(value = {"ACCESS_MANAGER", "DEVICE_VIEW"}, description = "设备历史数据查看")
    public ResponseDTO<PageResult<Object>> getDeviceHealthHistory(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "开始时间")
            LocalDateTime startDate,
            @Parameter(description = "结束时间")
            LocalDateTime endDate) {
        log.info("[设备健康历史] 获取历史数据，deviceId={}, startDate={}, endDate={}",
            deviceId, startDate, endDate);

        try {
            PageResult<Object> result = deviceHealthService.getDeviceHealthHistory(deviceId, startDate, endDate);
            log.info("[设备健康历史] 获取完成，deviceId={}, recordCount={}",
                deviceId, result.getTotal());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[设备健康历史] 获取异常，deviceId={}, error={}",
                deviceId, e.getMessage(), e);
            return ResponseDTO.error("DEVICE_HEALTH_HISTORY_ERROR", "获取设备健康历史失败：" + e.getMessage());
        }
    }
}