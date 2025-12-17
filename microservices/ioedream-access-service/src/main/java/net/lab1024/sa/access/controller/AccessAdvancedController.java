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
 * 门禁高级功能控制器
 * <p>
 * 提供门禁微服务的高级功能API入口：
 * - 蓝牙门禁管理与控制
 * - 离线模式支持与数据同步
 * - AI智能分析与异常检测
 * - 视频联动监控与控制
 * - 监控告警体系管理
 * - 综合数据分析与报表
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/api/v1/access/advanced")
@Tag(name = "门禁高级功能", description = "门禁系统高级功能管理")
@Slf4j
public class AccessAdvancedController {

    // 依赖注入 - 严格按照规范使用@Resource注解
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

    // ==================== 蓝牙门禁管理 ====================

    @Operation(summary = "扫描蓝牙设备", description = "扫描附近可用的蓝牙门禁设备")
    @PostMapping("/bluetooth/scan")
    @Timed(value = "bluetooth.scan", description = "扫描蓝牙设备耗时")
    @SaCheckPermission("access:bluetooth:scan")
    public ResponseDTO<List<BluetoothDeviceVO>> scanBluetoothDevices(
            @RequestParam @Parameter(description = "扫描时长(秒)") Integer scanDuration) {

        log.info("[蓝牙扫描] 开始扫描蓝牙设备, scanDuration={}秒", scanDuration);

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

        log.info("[蓝牙连接] 连接蓝牙设备, deviceAddress={}, deviceName={}",
                request.getDeviceAddress(), request.getDeviceName());

        return bluetoothAccessService.connectBluetoothDevice(request);
    }

    @Operation(summary = "执行蓝牙门禁验证", description = "通过蓝牙执行门禁权限验证")
    @PostMapping("/bluetooth/verify")
    @Timed(value = "bluetooth.verify", description = "蓝牙验证耗时")
    @SaCheckPermission("access:bluetooth:verify")
    public ResponseDTO<BluetoothVerificationResult> verifyBluetoothAccess(@Valid @RequestBody BluetoothVerificationRequest request) {

        log.info("[蓝牙验证] 执行蓝牙门禁验证, userId={}, deviceAddress={}",
                request.getUserId(), request.getDeviceAddress());

        return bluetoothAccessService.verifyBluetoothAccess(request);
    }

    @Operation(summary = "断开蓝牙连接", description = "断开已连接的蓝牙设备")
    @PostMapping("/bluetooth/disconnect")
    public ResponseDTO<Void> disconnectBluetoothDevice(
            @RequestParam @Parameter(description = "设备地址") String deviceAddress) {

        log.info("[蓝牙断开] 断开蓝牙设备, deviceAddress={}", deviceAddress);
        return bluetoothAccessService.disconnectBluetoothDevice(deviceAddress);
    }

    @Operation(summary = "获取蓝牙设备状态", description = "获取蓝牙设备的连接状态和健康信息")
    @GetMapping("/bluetooth/device/status")
    public ResponseDTO<List<BluetoothDeviceStatusVO>> getBluetoothDeviceStatus() {

        log.info("[蓝牙状态] 获取蓝牙设备状态");
        return bluetoothAccessService.getBluetoothDeviceStatus();
    }

    // ==================== 离线模式管理 ====================

    @Operation(summary = "同步离线数据", description = "同步离线模式所需的权限数据")
    @PostMapping("/offline/sync")
    @Timed(value = "offline.sync", description = "离线数据同步耗时")
    public ResponseDTO<OfflineSyncResult> syncOfflineData(@Valid @RequestBody OfflineSyncRequest request) {

        log.info("[离线同步] 开始同步离线数据, userId={}, lastSyncTime={}",
                request.getUserId(), request.getLastSyncTime());

        return offlineModeService.syncOfflineData(request);
    }

    @Operation(summary = "获取离线权限", description = "获取用户在离线模式下的访问权限")
    @GetMapping("/offline/permissions")
    public ResponseDTO<OfflinePermissionsVO> getOfflinePermissions(
            @RequestParam @Parameter(description = "用户ID") Long userId,
            @RequestParam @Parameter(description = "上次同步时间") String lastSyncTime) {

        log.info("[离线权限] 获取离线权限, userId={}, lastSyncTime={}", userId, lastSyncTime);
        return offlineModeService.getOfflinePermissions(userId, lastSyncTime);
    }

    @Operation(summary = "上报离线记录", description = "上报离线模式下产生的访问记录")
    @PostMapping("/offline/records/report")
    @Timed(value = "offline.report", description = "离线记录上报耗时")
    public ResponseDTO<OfflineReportResult> reportOfflineRecords(@Valid @RequestBody OfflineRecordsReportRequest request) {

        log.info("[离线上报] 上报离线记录, userId={}, recordCount={}",
                request.getUserId(), request.getRecords().size());

        return offlineModeService.reportOfflineRecords(request);
    }

    @Operation(summary = "验证离线访问权限", description = "在离线模式下验证用户访问权限")
    @PostMapping("/offline/validate")
    public ResponseDTO<OfflineAccessValidationResult> validateOfflineAccess(
            @RequestParam @Parameter(description = "用户ID") Long userId,
            @RequestParam @Parameter(description = "设备ID") Long deviceId,
            @RequestParam @Parameter(description = "访问类型") String accessType,
            @RequestParam @Parameter(description = "验证数据") String verificationData) {

        log.info("[离线验证] 验证离线访问权限, userId={}, deviceId={}, accessType={}",
                userId, deviceId, accessType);

        return offlineModeService.validateOfflineAccess(userId, deviceId, accessType, verificationData);
    }

    @Operation(summary = "获取离线模式状态", description = "查询用户的离线模式运行状态")
    @GetMapping("/offline/status")
    @SaCheckPermission("access:offline:query")
    public ResponseDTO<OfflineModeStatusVO> getOfflineModeStatus(
            @RequestParam @Parameter(description = "用户ID") Long userId) {

        log.info("[离线状态] 获取离线模式状态, userId={}", userId);
        return offlineModeService.getOfflineModeStatus(userId);
    }

    // ==================== AI智能分析 ====================

    @Operation(summary = "用户行为模式分析", description = "分析用户的门禁访问行为模式")
    @PostMapping("/ai/analysis/behavior")
    @Timed(value = "ai.behavior.analysis", description = "行为模式分析耗时")
    @SaCheckPermission("access:ai:analyze")
    public ResponseDTO<UserBehaviorPatternVO> analyzeUserBehaviorPattern(
            @RequestParam @Parameter(description = "用户ID") Long userId,
            @RequestParam @Parameter(description = "分析天数") Integer analysisDays) {

        log.info("[AI分析] 开始用户行为模式分析, userId={}, analysisDays={}", userId, analysisDays);

        return aiAnalysisService.analyzeUserBehaviorPattern(userId, analysisDays);
    }

    @Operation(summary = "异常访问行为检测", description = "实时检测用户和设备的异常访问行为")
    @PostMapping("/ai/analysis/anomaly")
    @Timed(value = "ai.anomaly.detection", description = "异常行为检测耗时")
    public ResponseDTO<AnomalyDetectionResult> detectAnomalousAccessBehavior(@Valid @RequestBody AnomalyDetectionRequest request) {

        log.info("[AI检测] 开始异常访问行为检测, userId={}, deviceId={}",
                request.getUserId(), request.getDeviceId());

        return aiAnalysisService.detectAnomalousAccessBehavior(request);
    }

    @Operation(summary = "设备预测性维护分析", description = "基于设备数据进行预测性维护分析")
    @PostMapping("/ai/maintenance/predictive")
    @Timed(value = "ai.maintenance.prediction", description = "预测性维护分析耗时")
    public ResponseDTO<PredictiveMaintenanceResult> performPredictiveMaintenanceAnalysis(
            @RequestParam @Parameter(description = "设备ID") Long deviceId,
            @RequestParam @Parameter(description = "预测天数") Integer predictionDays) {

        log.info("[AI维护] 开始预测性维护分析, deviceId={}, predictionDays={}", deviceId, predictionDays);

        return aiAnalysisService.performPredictiveMaintenanceAnalysis(deviceId, predictionDays);
    }

    @Operation(summary = "安全风险评估", description = "综合评估门禁系统的安全风险")
    @PostMapping("/ai/security/risk-assessment")
    @Timed(value = "ai.security.risk.assessment", description = "安全风险评估耗时")
    public ResponseDTO<SecurityRiskAssessmentVO> assessSecurityRisk(@Valid @RequestBody SecurityRiskAssessmentRequest request) {

        log.info("[AI安全] 开始安全风险评估, userId={}, deviceId={}, areaId={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId());

        return aiAnalysisService.assessSecurityRisk(request);
    }

    @Operation(summary = "访问趋势预测", description = "基于历史数据预测访问趋势")
    @PostMapping("/ai/trend/prediction")
    @Timed(value = "ai.trend.prediction", description = "访问趋势预测耗时")
    public ResponseDTO<AccessTrendPredictionVO> predictAccessTrends(@Valid @RequestBody AccessTrendPredictionRequest request) {

        log.info("[AI趋势] 开始访问趋势预测, predictionPeriod={}, deviceId={}",
                request.getPredictionPeriod(), request.getDeviceIds());

        return aiAnalysisService.predictAccessTrends(request);
    }

    @Operation(summary = "优化访问控制", description = "基于AI分析结果优化访问控制策略")
    @PostMapping("/ai/access-control/optimize")
    public ResponseDTO<AccessControlOptimizationVO> optimizeAccessControl(
            @RequestParam @Parameter(description = "用户ID") Long userId) {

        log.info("[AI优化] 开始访问控制优化, userId={}", userId);
        return aiAnalysisService.optimizeAccessControl(userId);
    }

    @Operation(summary = "设备利用率分析", description = "分析设备的使用效率和利用率")
    @PostMapping("/ai/device/utilization")
    public ResponseDTO<DeviceUtilizationAnalysisVO> analyzeDeviceUtilization(
            @RequestParam @Parameter(description = "设备ID") Long deviceId,
            @RequestParam @Parameter(description = "分析周期(天)") Integer analysisPeriod) {

        log.info("[AI设备] 开始设备利用率分析, deviceId={}, analysisPeriod={}", deviceId, analysisPeriod);
        return aiAnalysisService.analyzeDeviceUtilization(deviceId, analysisPeriod);
    }

    @Operation(summary = "获取AI分析报告", description = "生成综合的AI分析报告")
    @PostMapping("/ai/report/generate")
    @Timed(value = "ai.report.generation", description = "AI报告生成耗时")
    public ResponseDTO<AIAnalysisReportVO> generateAIAnalysisReport(@Valid @RequestBody AIAnalysisReportRequest request) {

        log.info("[AI报告] 开始生成AI分析报告, reportType={}, period={}",
                request.getReportType(), request.getReportPeriod());

        return aiAnalysisService.generateAIAnalysisReport(request);
    }

    // ==================== 视频联动监控 ====================

    @Operation(summary = "触发视频联动", description = "门禁事件触发视频监控联动")
    @PostMapping("/video/linkage/trigger")
    @Timed(value = "video.linkage.trigger", description = "视频联动触发耗时")
    public ResponseDTO<VideoLinkageResult> triggerVideoLinkage(@Valid @RequestBody VideoLinkageRequest request) {

        log.info("[视频联动] 触发视频联动, accessEventId={}, deviceId={}, userId={}",
                request.getAccessEventId(), request.getDeviceId(), request.getUserId());

        return videoLinkageMonitorService.triggerVideoLinkage(request);
    }

    @Operation(summary = "获取实时视频流", description = "获取指定摄像头的实时视频流")
    @PostMapping("/video/stream/realtime")
    @Timed(value = "video.stream.get", description = "获取视频流耗时")
    public ResponseDTO<VideoStreamResult> getRealTimeStream(@Valid @RequestBody VideoStreamRequest request) {

        log.info("[视频流] 获取实时视频流, cameraId={}, streamType={}",
                request.getCameraId(), request.getStreamType());

        return videoLinkageMonitorService.getRealTimeStream(request);
    }

    @Operation(summary = "人脸识别验证", description = "通过视频流进行实时人脸识别验证")
    @PostMapping("/video/face/recognition")
    @Timed(value = "video.face.recognition", description = "人脸识别验证耗时")
    public ResponseDTO<FaceRecognitionResult> performFaceRecognition(@Valid @RequestBody FaceRecognitionRequest request) {

        log.info("[人脸识别] 开始人脸识别验证, streamId={}, userId={}",
                request.getStreamId(), request.getUserId());

        return videoLinkageMonitorService.performFaceRecognition(request);
    }

    @Operation(summary = "异常行为检测", description = "基于视频流检测异常行为")
    @PostMapping("/video/behavior/detection")
    @Timed(value = "video.behavior.detection", description = "异常行为检测耗时")
    public ResponseDTO<AbnormalBehaviorResult> detectAbnormalBehavior(@Valid @RequestBody AbnormalBehaviorRequest request) {

        log.info("[行为检测] 开始异常行为检测, streamId={}, areaId={}",
                request.getStreamId(), request.getAreaId());

        return videoLinkageMonitorService.detectAbnormalBehavior(request);
    }

    @Operation(summary = "管理视频录制", description = "管理事件相关的视频片段录制")
    @PostMapping("/video/recording/manage")
    @Timed(value = "video.recording.manage", description = "视频录制管理耗时")
    public ResponseDTO<VideoRecordingResult> manageVideoRecording(@Valid @RequestBody VideoRecordingRequest request) {

        log.info("[视频录制] 管理视频录制, linkageId={}, cameraCount={}",
                request.getLinkageId(), request.getCameraIds().size());

        return videoLinkageMonitorService.manageVideoRecording(request);
    }

    @Operation(summary = "获取多画面视图", description = "获取多画面实时监控展示")
    @PostMapping("/video/multiscreen/view")
    @Timed(value = "video.multiscreen.get", description = "多画面视图获取耗时")
    public ResponseDTO<MultiScreenResult> getMultiScreenView(@Valid @RequestBody MultiScreenRequest request) {

        log.info("[多画面] 获取多画面视图, cameraCount={}, layout={}",
                request.getCameraIds().size(), request.getLayoutType());

        return videoLinkageMonitorService.getMultiScreenView(request);
    }

    @Operation(summary = "PTZ云台控制", description = "远程控制摄像头的云台操作")
    @PostMapping("/video/ptz/control")
    @Timed(value = "video.ptz.control", description = "PTZ控制耗时")
    public ResponseDTO<PTZControlResult> controlPTZCamera(@Valid @RequestBody PTZControlRequest request) {

        log.info("[PTZ控制] 开始PTZ云台控制, cameraId={}, action={}",
                request.getCameraId(), request.getAction());

        return videoLinkageMonitorService.controlPTZCamera(request);
    }

    @Operation(summary = "历史视频回放", description = "提供历史视频的回放功能")
    @PostMapping("/video/playback/historical")
    @Timed(value = "video.playback.historical", description = "历史视频回放耗时")
    public ResponseDTO<VideoPlaybackResult> playbackHistoricalVideo(@Valid @RequestBody VideoPlaybackRequest request) {

        log.info("[视频回放] 开始历史视频回放, cameraId={}, startTime={}, endTime={}",
                request.getCameraId(), request.getStartTime(), request.getEndTime());

        return videoLinkMonitorService.playbackHistoricalVideo(request);
    }

    @Operation(summary = "获取联动事件列表", description = "查询视频联动事件的历史记录")
    @GetMapping("/video/linkage/events")
    public ResponseDTO<List<VideoLinkageEventVO>> getLinkageEvents(
            @RequestParam(required = false) @Parameter(description = "用户ID") String userId,
            @RequestParam(required = false) @Parameter(description = "设备ID") Long deviceId,
            @RequestParam(required = false) @Parameter(description = "区域ID") String areaId,
            @RequestParam(required = false) @Parameter(description = "事件类型") String eventType,
            @RequestParam(required = false) @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(required = false) @Parameter(description = "联动状态") Integer linkageStatus,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer pageNum,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer pageSize) {

        log.info("[联动事件] 查询视频联动事件");

        VideoLinkageEventQueryRequest request = VideoLinkageEventQueryRequest.builder()
                .userId(userId)
                .deviceId(deviceId)
                .areaId(areaId)
                .eventType(eventType)
                .startTime(startTime)
                .endTime(endTime)
                .linkageStatus(linkageStatus)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();

        return videoLinkageMonitorService.getLinkageEvents(request);
    }

    @Operation(summary = "获取监控统计报告", description = "生成视频监控系统的统计分析报告")
    @PostMapping("/video/statistics/report")
    @Timed(value = "video.statistics.report", description = "监控统计报告生成耗时")
    public ResponseDTO<MonitorStatisticsVO> getMonitorStatistics(@Valid @RequestBody MonitorStatisticsRequest request) {

        log.info("[监控统计] 生成监控统计报告, cameraCount={}, statisticsType={}",
                request.getCameraIds().size(), request.getStatisticsType());

        return videoLinkageMonitorService.getMonitorStatistics(request);
    }

    // ==================== 监控告警管理 ====================

    @Operation(summary = "创建监控告警", description = "创建一个新的监控告警事件")
    @PostMapping("/alert/create")
    @Timed(value = "monitor.alert.create", description = "创建监控告警耗时")
    public ResponseDTO<MonitorAlertResult> createMonitorAlert(@Valid @RequestBody CreateMonitorAlertRequest request) {

        log.info("[监控告警] 创建告警, title={}, type={}, severity={}",
                request.getAlertTitle(), request.getAlertType(), request.getSeverityLevel());

        return monitorAlertService.createMonitorAlert(request);
    }

    @Operation(summary = "告警级别评估", description = "基于多维度指标进行智能告警分级")
    @PostMapping("/alert/level/assessment")
    @Timed(value = "alert.level.assessment", description = "告警级别评估耗时")
    public ResponseDTO<AlertLevelAssessmentResult> assessAlertLevel(@Valid @RequestBody AlertLevelAssessmentRequest request) {

        log.info("[告警分级] 开始级别评估, type={}, source={}, impact={}",
                request.getAlertType(), request.getSourceSystem(), request.getBusinessImpact());

        return monitorAlertService.assessAlertLevel(request);
    }

    @Operation(summary = "发送告警通知", description = "根据告警级别和配置规则推送告警通知")
    @PostMapping("/alert/notification/send")
    @Timed(value = "alert.notification.send", description = "告警通知推送耗时")
    public ResponseDTO<AlertNotificationResult> sendAlertNotification(@Valid @RequestBody AlertNotificationRequest request) {

        log.info("[告警通知] 发送通知, alertId={}, channels={}, recipients={}",
                request.getAlertId(), request.getNotificationChannels(), request.getRecipients().size());

        return monitorAlertService.sendAlertNotification(request);
    }

    @Operation(summary = "获取告警列表", description = "查询监控系统告警列表")
    @GetMapping("/alert/list")
    @Timed(value = "monitor.alert.query", description = "查询告警列表耗时")
    public ResponseDTO<List<MonitorAlertVO>> getMonitorAlertList(
            @RequestParam(required = false) @Parameter(description = "告警ID") String alertId,
            @RequestParam(required = false) @Parameter(description = "告警类型") String alertType,
            @RequestParam(required = false) @Parameter(description = "严重等级") String severityLevel,
            @RequestParam(required = false) @Parameter(description = "状态") String status,
            @RequestParam(required = false) @Parameter(description = "来源系统") String sourceSystem,
            @RequestParam(required = false) @Parameter(description = "分配给") String assignedTo,
            @RequestParam(required = false) @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer pageNum,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer pageSize,
            @RequestParam(required = false) @Parameter(description = "排序字段") String sortBy,
            @RequestParam(required = false) @Parameter(description = "排序方向") String sortOrder) {

        log.info("[告警查询] 查询告警列表");

        MonitorAlertQueryRequest request = MonitorAlertQueryRequest.builder()
                .alertId(alertId)
                .alertType(alertType)
                .severityLevel(severityLevel)
                .status(status)
                .sourceSystem(sourceSystem)
                .assignedTo(assignedTo)
                .startTime(startTime)
                .endTime(endTime)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .build();

        return monitorAlertService.getMonitorAlertList(request);
    }

    @Operation(summary = "处理告警事件", description = "处理监控告警事件")
    @PostMapping("/alert/handle")
    @Timed(value = "monitor.alert.handle", description = "处理告警事件耗时")
    public ResponseDTO<AlertHandleResult> handleAlert(@Valid @RequestBody AlertHandleRequest request) {

        log.info("[告警处理] 处理告警事件, alertId={}, action={}, assignedTo={}",
                request.getAlertId(), request.getHandleAction(), request.getAssignedTo());

        return monitorAlertService.handleAlert(request);
    }

    @Operation(summary = "生成告警统计", description = "生成监控告警的统计分析报告")
    @PostMapping("/alert/statistics/generate")
    @Timed(value = "monitor.alert.statistics.generate", description = "生成告警统计耗时")
    public ResponseDTO<AlertStatisticsReport> generateAlertStatistics(@Valid @RequestBody AlertStatisticsRequest request) {

        log.info("[告警统计] 生成统计报告, type={}, period={}, startTime={}, endTime={}",
                request.getStatisticsType(), request.getStatisticsPeriod(),
                request.getStartTime(), request.getEndTime());

        return monitorAlertService.generateAlertStatistics(request);
    }

    @Operation(summary = "配置告警规则", description = "管理监控告警的规则配置")
    @PostMapping("/alert/rule/configure")
    @Timed(value = "monitor.alert.rule.configure", description = "配置告警规则耗时")
    public ResponseDTO<AlertRuleResult> configureAlertRule(@Valid @RequestBody AlertRuleConfigureRequest request) {

        log.info("[告警规则] 配置告警规则, ruleId={}, ruleName={}, enabled={}",
                request.getRuleId(), request.getRuleName(), request.getEnabled());

        return monitorAlertService.configureAlertRule(request);
    }

    @Operation(summary = "获取告警规则列表", description = "查询系统配置的告警规则列表")
    @GetMapping("/alert/rule/list")
    @Timed(value = "monitor.alert.rule.query", description = "查询告警规则列表耗时")
    public ResponseDTO<List<AlertRuleVO>> getAlertRuleList(
            @RequestParam(required = false) @Parameter(description = "规则ID") String ruleId,
            @RequestParam(required = false) @Parameter(description = "规则名称") String ruleName,
            @RequestParam(required = false) @Parameter(description = "规则类型") String ruleType,
            @RequestParam(required = false) @Parameter(description = "是否启用") Boolean enabled,
            @RequestParam(required = false) @Parameter(description = "状态") String status,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer pageNum,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer pageSize) {

        log.info("[告警规则] 查询告警规则列表");

        AlertRuleQueryRequest request = AlertRuleQueryRequest.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .ruleType(ruleType)
                .enabled(enabled)
                .status(status)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();

        return monitorAlertService.getAlertRuleList(request);
    }

    @Operation(summary = "系统健康检查", description = "执行系统全面的健康检查")
    @PostMapping("/health/check")
    @Timed(value = "monitor.health.check", description = "系统健康检查耗时")
    public ResponseDTO<SystemHealthCheckResult> performSystemHealthCheck(@Valid @RequestBody SystemHealthCheckRequest request) {

        log.info("[健康检查] 执行系统健康检查, categories={}", request.getCheckCategories());

        return monitorAlertService.performSystemHealthCheck(request);
    }

    @Operation(summary = "故障自愈处理", description = "执行系统故障的自动恢复处理")
    @PostMapping("/healing/self-healing")
    @Timed(value = "monitor.self.healing", description = "故障自愈处理耗时")
    public ResponseDTO<SelfHealingResult> performSelfHealing(@Valid @RequestBody SelfHealingRequest request) {

        log.info("[故障自愈] 开始自愈处理, incidentId={}, failureType={}, strategy={}",
                request.getIncidentId(), request.getFailureType(), request.getSelfHealingStrategy());

        return monitorAlertService.performSelfHealing(request);
    }

    @Operation(summary = "告警趋势预测", description = "基于历史数据预测告警趋势")
    @PostMapping("/alert/trend/prediction")
    @Timed(value = "alert.trend.prediction", description = "告警趋势预测耗时")
    public ResponseDTO<AlertTrendPredictionResult> predictAlertTrend(@Valid @RequestBody AlertTrendPredictionRequest request) {

        log.info("[趋势预测] 开始告警趋势预测, model={}, period={}, days={}",
                request.getPredictionModel(), request.getPredictionPeriod(), request.getPredictionDays());

        return monitorAlertService.predictAlertTrend(request);
    }

    // ==================== 综合分析接口 ====================

    @Operation(summary = "系统综合分析", description = "对门禁系统进行综合分析")
    @PostMapping("/analysis/comprehensive")
    @Timed(value = "analysis.comprehensive", description = "综合分析耗时")
    public ResponseDTO<ComprehensiveAnalysisResult> performComprehensiveAnalysis(
            @RequestParam @Parameter(description = "分析类型") String analysisType,
            @RequestParam(required = false) @Parameter(description = "用户ID") Long userId,
            @RequestParam(required = false) @Parameter(description = "设备ID") Long deviceId,
            @RequestParam(required = false) @Parameter(description = "分析周期") String analysisPeriod,
            @RequestParam(required = false) @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @Parameter(description = "结束时间") LocalDateTime endTime) {

        log.info("[综合分析] 开始系统综合分析, type={}, userId={}, deviceId={}", analysisType, userId, deviceId);

        // 模拟综合分析
        ComprehensiveAnalysisResult result = performMockComprehensiveAnalysis(
                analysisType, userId, deviceId, analysisPeriod, startTime, endTime);

        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取系统概览", description = "获取门禁系统的整体运行概览")
    @GetMapping("/overview/system")
    @Timed(value = "overview.system", description = "获取系统概览耗时")
    public ResponseDTO<SystemOverviewVO> getSystemOverview() {

        log.info("[系统概览] 获取门禁系统运行概览");

        // 模拟系统概览数据
        SystemOverviewVO overview = SystemOverviewVO.builder()
                .systemName("IOE-DREAM智慧门禁系统")
                .systemVersion("1.0.0")
                .status("RUNNING")
                .uptime(99.8)
                .totalUsers(10000)
                .activeUsers(8500)
                .totalDevices(500)
                .onlineDevices(495)
                .todayAccessCount(12500)
                .todayAlertCount(15)
                .systemHealth("GOOD")
                .lastUpdateTime(LocalDateTime.now())
                .build();

        return ResponseDTO.ok(overview);
    }

    @Operation(summary = "批量数据导出", description = "批量导出各类业务数据")
    @PostMapping("/export/batch")
    @Timed(value = "export.batch", description = "批量导出耗时")
    public ResponseEntity<byte[]> exportBatchData(
            @RequestParam @Parameter(description = "导出类型") String exportType,
            @RequestParam(required = false) @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam @Parameter(description = "导出格式") String format) {

        log.info("[批量导出] 开始批量数据导出, type={}, format={}", exportType, format);

        // 模拟批量导出
        byte[] exportData = generateMockExportData(exportType, startTime, endTime, format);

        String filename = String.format("access_data_%s_%s.%s",
                exportType,
                LocalDateTime.now().toString().replace(":", "-"),
                format.toLowerCase());

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(getContentType(format))
                .body(exportData);
    }

    @Operation(summary = "异步任务状态查询", description = "查询异步任务的执行状态")
    @GetMapping("/async/status")
    public ResponseDTO<Map<String, Object>> getAsyncTaskStatus(
            @RequestParam @Parameter(description = "任务ID") String taskId) {

        log.info("[异步状态] 查询异步任务状态, taskId={}", taskId);

        // 模拟异步任务状态查询
        Map<String, Object> status = Map.of(
                "taskId", taskId,
                "status", "RUNNING",
                "progress", 75,
                "message", "正在处理中...",
                "createTime", LocalDateTime.now().minusMinutes(5),
                "estimatedCompletion", LocalDateTime.now().plusMinutes(10)
        );

        return ResponseDTO.ok(status);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 模拟综合分析
     */
    private ComprehensiveAnalysisResult performMockComprehensiveAnalysis(String analysisType, Long userId, Long deviceId,
                                                              String analysisPeriod, LocalDateTime startTime, LocalDateTime endTime) {
        return ComprehensiveAnalysisResult.builder()
                .analysisId("ANALYSIS-" + System.currentTimeMillis())
                .analysisType(analysisType)
                .analysisTime(LocalDateTime.now())
                .analysisPeriod(analysisPeriod)
                .userId(userId)
                .deviceId(deviceId)
                .summary("系统运行良好，各项指标正常")
                .keyFindings(Arrays.asList(
                        "门禁访问量呈上升趋势",
                        "设备在线率保持在99%以上",
                        "AI分析准确率提升到85%"
                ))
                .recommendations(Arrays.asList(
                        "建议增加监控覆盖范围",
                        "优化设备巡检策略",
                        "加强AI模型训练"
                ))
                .riskAssessment("LOW")
                .performanceScore(92.5)
                .securityScore(88.0)
                .availabilityScore(99.8)
                .build();
    }

    /**
     * 生成模拟导出数据
     */
    private byte[] generateMockExportData(String exportType, LocalDateTime startTime, LocalDateTime endTime, String format) {
        // 根据导出类型和格式生成模拟数据
        String csvContent = "导出类型: " + exportType + "\n" +
                "导出时间: " + LocalDateTime.now() + "\n" +
                "时间范围: " + startTime + " 至 " + endTime + "\n" +
                "格式: " + format + "\n" +
                "记录ID,用户ID,设备ID,访问时间,访问类型,验证结果,响应时间\n" +
                "1,1001,2001,2025-01-30 10:00:00,BIOMETRIC,成功,150\n" +
                "2,1002,2002,2025-01-30 10:01:00,CARD,成功,200\n" +
                "3,1003,2003,2025-01-30 10:02:00,BLUETOOTH,成功,180";

        return csvContent.getBytes();
    }

    /**
     * 获取内容类型
     */
    private String getContentType(String format) {
        Map<String, String> contentTypes = Map.of(
                "JSON", "application/json",
                "XML", "application/xml",
                "CSV", "text/csv",
                "XLSX", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "PDF", "application/pdf"
        );
        return contentTypes.getOrDefault(format.toUpperCase(), "application/json");
    }

    /**
     * 系统概览视图对象
     */
    public static class SystemOverviewVO {
        private String systemName;
        private String systemVersion;
        private String status;
        private Double uptime;
        private Long totalUsers;
        private Long activeUsers;
        private Long totalDevices;
        private Long onlineDevices;
        private Long todayAccessCount;
        private Long todayAlertCount;
        private String systemHealth;
        private LocalDateTime lastUpdateTime;
    }

    /**
     * 综合分析结果
     */
    public static class ComprehensiveAnalysisResult {
        private String analysisId;
        private String analysisType;
        private LocalDateTime analysisTime;
        private String analysisPeriod;
        private Long userId;
        private Long deviceId;
        private String summary;
        private List<String> keyFindings;
        private List<String> recommendations;
        private String riskAssessment;
        private Double performanceScore;
        private Double securityScore;
        private Double availabilityScore;
    }
}
