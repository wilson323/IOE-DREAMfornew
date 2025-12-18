package net.lab1024.sa.access.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckPermission;
import net.lab1024.sa.access.service.AIAnalysisService;
import net.lab1024.sa.access.service.BluetoothAccessService;
import net.lab1024.sa.access.service.MonitorAlertService;
import net.lab1024.sa.access.service.OfflineModeService;
import net.lab1024.sa.access.service.VideoLinkageMonitorService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.service.AIAnalysisService.*;
import net.lab1024.sa.access.service.BluetoothAccessService.*;
import net.lab1024.sa.access.service.MonitorAlertService.*;
import net.lab1024.sa.access.service.OfflineModeService.*;
import net.lab1024.sa.access.service.VideoLinkageMonitorService.*;

/**
 * 门禁高级功能服务控制器
 * <p>
 * 提供门禁访问服务高级功能API接口，包括：
 * - 蓝牙门禁设备连接管理
 * - 离线模式数据处理
 * - AI智能分析服务监控告警
 * - 视频监控联动处理
 * - 设备健康状态监控
 * - 数据库性能优化服务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/api/v1/access/advanced")
@Tag(name = "门禁高级功能", description = "门禁高级功能服务接口")
@Slf4j
public class AccessAdvancedController {

    // 服务注入 - 严格遵循CLAUDE.md规范使用@Resource注解
    @Resource
    private BluetoothAccessService bluetoothAccessService;

    @Resource
    private OfflineModeService offlineModeService;

    @Resource
    private AIAnalysisService aiAnalysisService;

    @Resource
    private VideoLinkageMonitorService videoLinkageMonitorService;

    @Resource
    private MonitorAlertService monitorAlertService;

    // ==================== 蓝牙门禁设备 ====================

    @Operation(summary = "扫描蓝牙设备", description = "扫描附近的蓝牙门禁设备并返回设备列表")
    @PostMapping("/bluetooth/scan")
    @Timed(value = "bluetooth.scan", description = "扫描蓝牙设备耗时")
    @SaCheckPermission("access:bluetooth:scan")
    public ResponseDTO<List<BluetoothDeviceVO>> scanBluetoothDevices(
            @RequestParam @Parameter(description = "扫描持续时间(秒)") Integer scanDuration) {

        log.info("[蓝牙扫描] 开始扫描附近设备，扫描时长={}秒", scanDuration);

        BluetoothScanRequest request = BluetoothScanRequest.builder()
                .scanDuration(scanDuration != null ? scanDuration : 10)
                .scanRadius(10.0)
                .deviceTypes(Arrays.asList("ACCESS_CONTROL", "SMART_LOCK"))
                .build();

        return bluetoothAccessService.scanBluetoothDevices(request);
    }

    @Operation(summary = "连接蓝牙设备", description = "连接指定的蓝牙门禁设备")
    @PostMapping("/bluetooth/connect")
    @SaCheckPermission("access:bluetooth:connect")
    public ResponseDTO<BluetoothConnectionResult> connectBluetoothDevice(@Valid @RequestBody BluetoothConnectRequest request) {
        log.info("[蓝牙连接] 连接蓝牙设备: {}", request.getDeviceId());

        return bluetoothAccessService.connectDevice(request);
    }

    @Operation(summary = "断开蓝牙设备", description = "断开已连接的蓝牙门禁设备")
    @PostMapping("/bluetooth/disconnect")
    @SaCheckPermission("access:bluetooth:disconnect")
    public ResponseDTO<Void> disconnectBluetoothDevice(@RequestParam String deviceId) {
        log.info("[蓝牙断开] 断开蓝牙设备: {}", deviceId);

        bluetoothAccessService.disconnectDevice(deviceId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "获取蓝牙设备列表", description = "获取已配对的蓝牙设备列表")
    @GetMapping("/bluetooth/devices")
    @SaCheckPermission("access:bluetooth:list")
    public ResponseDTO<List<BluetoothDeviceVO>> getPairedBluetoothDevices() {
        log.debug("[蓝牙设备] 获取已配对设备列表");

        return bluetoothAccessService.getPairedDevices();
    }

    // ==================== 离线模式服务 ====================

    @Operation(summary = "启用离线模式", description = "启用门禁系统的离线工作模式")
    @PostMapping("/offline/enable")
    @SaCheckPermission("access:offline:manage")
    public ResponseDTO<Void> enableOfflineMode() {
        log.info("[离线模式] 启用离线模式");

        offlineModeService.enableOfflineMode();
        return ResponseDTO.ok();
    }

    @Operation(summary = "禁用离线模式", description = "禁用门禁系统的离线工作模式")
    @PostMapping("/offline/disable")
    @SaCheckPermission("access:offline:manage")
    public ResponseDTO<Void> disableOfflineMode() {
        log.info("[离线模式] 禁用离线模式");

        offlineModeService.disableOfflineMode();
        return ResponseDTO.ok();
    }

    @Operation(summary = "同步离线数据", description = "同步离线期间产生的门禁记录数据")
    @PostMapping("/offline/sync")
    @SaCheckPermission("access:offline:sync")
    public ResponseDTO<OfflineSyncResult> syncOfflineData() {
        log.info("[离线模式] 开始同步离线数据");

        return offlineModeService.syncOfflineData();
    }

    @Operation(summary = "获取离线模式状态", description = "查询当前离线模式的工作状态")
    @GetMapping("/offline/status")
    @SaCheckPermission("access:offline:status")
    public ResponseDTO<OfflineModeStatus> getOfflineModeStatus() {
        log.debug("[离线模式] 获取离线模式状态");

        return ResponseDTO.ok(offlineModeService.getOfflineModeStatus());
    }

    // ==================== AI分析服务 ====================

    @Operation(summary = "执行行为分析", description = "对门禁访问记录进行AI行为分析")
    @PostMapping("/ai/behavior-analysis")
    @Timed(value = "ai.behavior-analysis", description = "AI行为分析耗时")
    @SaCheckPermission("access:ai:analyze")
    public ResponseDTO<BehaviorAnalysisResult> performBehaviorAnalysis(
            @Valid @RequestBody BehaviorAnalysisRequest request) {
        log.info("[AI分析] 开始执行行为分析: userId={}, timeRange={}~{}",
                request.getUserId(), request.getStartTime(), request.getEndTime());

        return aiAnalysisService.analyzeBehavior(request);
    }

    @Operation(summary = "异常检测", description = "检测门禁访问中的异常行为模式")
    @PostMapping("/ai/anomaly-detection")
    @SaCheckPermission("access:ai:detect")
    public ResponseDTO<AnomalyDetectionResult> detectAnomalies(
            @Valid @RequestBody AnomalyDetectionRequest request) {
        log.info("[AI检测] 开始异常检测: userId={}", request.getUserId());

        return aiAnalysisService.detectAnomalies(request);
    }

    @Operation(summary = "风险预警", description = "基于历史数据进行风险预警分析")
    @PostMapping("/ai/risk-prediction")
    @SaCheckPermission("access:ai:predict")
    public ResponseDTO<RiskPredictionResult> predictRisk(
            @Valid @RequestBody RiskPredictionRequest request) {
        log.info("[AI预测] 开始风险预测: userId={}", request.getUserId());

        return aiAnalysisService.predictRisk(request);
    }

    // ==================== 视频监控联动 ====================

    @Operation(summary = "开始视频监控", description = "启动与门禁事件联动的视频监控")
    @PostMapping("/video/start")
    @SaCheckPermission("access:video:control")
    public ResponseDTO<Void> startVideoMonitoring(
            @RequestParam String accessPointId,
            @RequestParam String cameraId) {
        log.info("[视频监控] 开始监控: 访问点={}, 摄像头={}", accessPointId, cameraId);

        videoLinkageMonitorService.startMonitoring(accessPointId, cameraId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "停止视频监控", description = "停止指定的视频监控会话")
    @PostMapping("/video/stop")
    @SaCheckPermission("access:video:control")
    public ResponseDTO<Void> stopVideoMonitoring(
            @RequestParam String accessPointId,
            @RequestParam String cameraId) {
        log.info("[视频监控] 停止监控: 访问点={}, 摄像头={}", accessPointId, cameraId);

        videoLinkageMonitorService.stopMonitoring(accessPointId, cameraId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "抓拍照片", description = "抓拍指定访问点的照片")
    @PostMapping("/video/capture")
    @SaCheckPermission("access:video:capture")
    public ResponseDTO<VideoCaptureResult> capturePhoto(
            @RequestParam String accessPointId,
            @RequestParam String cameraId) {
        log.info("[视频抓拍] 抓拍照片: 访问点={}, 摄像头={}", accessPointId, cameraId);

        return videoLinkageMonitorService.capturePhoto(accessPointId, cameraId);
    }

    // ==================== 监控告警 ====================

    @Operation(summary = "获取告警列表", description = "获取系统产生的监控告警信息")
    @GetMapping("/alerts/list")
    @SaCheckPermission("access:alerts:list")
    public ResponseDTO<List<AlertInfo>> getAlertList(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.debug("[监控告警] 获取告警列表: severity={}, status={}", severity, status);

        return ResponseDTO.ok(monitorAlertService.getAlertList(severity, status, page, size));
    }

    @Operation(summary = "处理告警", description = "处理指定的监控告警")
    @PostMapping("/alerts/process")
    @SaCheckPermission("access:alerts:process")
    public ResponseDTO<Void> processAlert(
            @RequestParam String alertId,
            @RequestParam String processResult,
            @RequestParam(required = false) String processNotes) {
        log.info("[监控告警] 处理告警: alertId={}, result={}", alertId, processResult);

        monitorAlertService.processAlert(alertId, processResult, processNotes);
        return ResponseDTO.ok();
    }

    @Operation(summary = "创建告警规则", description = "创建自定义的监控告警规则")
    @PostMapping("/alerts/rules/create")
    @SaCheckPermission("access:alerts:manage")
    public ResponseDTO<AlertRule> createAlertRule(@Valid @RequestBody AlertRuleCreateRequest request) {
        log.info("[监控告警] 创建告警规则: {}", request.getRuleName());

        return ResponseDTO.ok(monitorAlertService.createAlertRule(request));
    }

    // ==================== 综合状态查询 ====================

    @Operation(summary = "获取系统概览", description = "获取门禁系统的综合运行状态概览")
    @GetMapping("/overview")
    @SaCheckPermission("access:overview:read")
    public ResponseDTO<SystemOverview> getSystemOverview() {
        log.debug("[系统概览] 获取系统概览状态");

        // 收集各服务的运行状态
        Map<String, Object> systemStatus = Map.of(
            "timestamp", LocalDateTime.now(),
            "bluetoothStatus", bluetoothAccessService.getSystemStatus(),
            "offlineModeStatus", offlineModeService.getOfflineModeStatus(),
            "aiServiceStatus", aiAnalysisService.getServiceStatus(),
            "videoMonitoringCount", videoLinkageMonitorService.getActiveMonitoringCount(),
            "activeAlerts", monitorAlertService.getActiveAlertsCount()
        );

        return ResponseDTO.ok(SystemOverview.builder()
                .systemStatus("RUNNING")
                .details(systemStatus)
                .build());
    }

    @Operation(summary = "健康检查", description = "执行系统健康状态检查")
    @GetMapping("/health")
    @Timed(value = "system.health-check", description = "健康检查耗时")
    @SaCheckPermission("access:health:check")
    public ResponseDTO<SystemHealth> performHealthCheck() {
        log.info("[健康检查] 开始执行系统健康检查");

        try {
            // 检查各个服务的健康状态
            boolean bluetoothHealthy = bluetoothAccessService.isHealthy();
            boolean aiServiceHealthy = aiAnalysisService.isHealthy();
            boolean videoServiceHealthy = videoLinkageMonitorService.isHealthy();

            SystemHealth health = SystemHealth.builder()
                    .overallStatus(bluetoothHealthy && aiServiceHealthy && videoServiceHealthy ? "HEALTHY" : "DEGRADED")
                    .checkTime(LocalDateTime.now())
                    .serviceStatus(Map.of(
                        "bluetooth", bluetoothHealthy ? "UP" : "DOWN",
                        "aiService", aiServiceHealthy ? "UP" : "DOWN",
                        "videoMonitoring", videoServiceHealthy ? "UP" : "DOWN"
                    ))
                    .build();

            return ResponseDTO.ok(health);

        } catch (Exception e) {
            log.error("[健康检查] 健康检查异常", e);
            return ResponseDTO.error("HEALTH_CHECK_ERROR", "健康检查执行异常: " + e.getMessage());
        }
    }
}