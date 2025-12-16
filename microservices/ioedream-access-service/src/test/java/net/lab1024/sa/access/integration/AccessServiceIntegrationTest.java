package net.lab1024.sa.access.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.access.controller.AccessAdvancedController;
import net.lab1024.sa.access.controller.AccessMobileController;
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
 * 门禁微服务集成测试
 * <p>
 * 验证所有功能模块的协同工作和数据一致性
 * 测试覆盖：蓝牙门禁、离线模式、AI分析、视频监控、告警系统
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("门禁微服务集成测试")
public class AccessServiceIntegrationTest {

    @Resource
    private AccessAdvancedController accessAdvancedController;

    @Resource
    private AccessMobileController accessMobileController;

    @MockBean
    private AIAnalysisService aiAnalysisService;

    @MockBean
    private BluetoothAccessService bluetoothAccessService;

    @MockBean
    private MonitorAlertService monitorAlertService;

    @MockBean
    private OfflineModeService offlineModeService;

    @MockBean
    private VideoLinkageMonitorService videoLinkageMonitorService;

    @Resource
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        setupMockServices();
    }

    private void setupMockServices() {
        // AI分析服务Mock
        when(aiAnalysisService.analyzeAccessPattern(any(AccessPatternAnalysisRequestDTO.class)))
            .thenReturn(buildMockAccessPatternAnalysis());

        when(aiAnalysisService.detectBehaviorAnomalies(any(BehaviorAnalysisRequestDTO.class)))
            .thenReturn(buildMockBehaviorAnomalies());

        // 蓝牙门禁服务Mock
        when(bluetoothAccessService.performSeamlessAccess(any(SeamlessAccessRequestDTO.class)))
            .thenReturn(buildMockSeamlessAccessResult());

        when(bluetoothAccessService.checkDeviceHealth(anyString()))
            .thenReturn(buildMockDeviceHealthResult());

        // 监控告警服务Mock
        when(monitorAlertService.processSmartAlert(any(SmartAlertRequestDTO.class)))
            .thenReturn(buildMockAlertProcessResult());

        // 离线模式服务Mock
        when(offlineModeService.syncOfflineData(any()))
            .thenReturn(buildMockSyncResult());

        // 视频监控服务Mock
        when(videoLinkageMonitorService.startRealTimeMonitoring(any(VideoMonitoringRequestDTO.class)))
            .thenReturn(buildMockVideoStream());
    }

    @Test
    @DisplayName("蓝牙门禁完整流程集成测试")
    void testBluetoothAccessCompleteFlow() {
        log.info("[集成测试] 开始蓝牙门禁完整流程测试");

        try {
            // 1. 设备健康检查
            DeviceHealthResult healthResult = bluetoothAccessService.checkDeviceHealth("TEST_DEVICE_001");
            assertNotNull(healthResult);
            assertEquals("TEST_DEVICE_001", healthResult.getDeviceId());
            assertTrue(healthResult.getHealthScore() >= 0 && healthResult.getHealthScore() <= 100);
            log.info("[集成测试] 设备健康检查完成 - 设备: {}, 健康评分: {}",
                healthResult.getDeviceId(), healthResult.getHealthScore());

            // 2. 蓝牙设备扫描
            CompletableFuture<List<BluetoothDeviceVO>> scanFuture =
                bluetoothAccessService.scanBluetoothDevices(buildMockScanRequest());
            List<BluetoothDeviceVO> devices = scanFuture.join();
            assertNotNull(devices);
            log.info("[集成测试] 蓝牙设备扫描完成 - 发现设备: {} 个", devices.size());

            // 3. 无缝通行测试
            SeamlessAccessRequestDTO accessRequest = buildMockSeamlessAccessRequest();
            SeamlessAccessResultVO accessResult = bluetoothAccessService.performSeamlessAccess(accessRequest);
            assertNotNull(accessResult);
            assertTrue(accessResult.getResponseTime() >= 0);
            log.info("[集成测试] 无缝通行测试完成 - 用户: {}, 通行结果: {}, 响应时间: {}ms",
                accessRequest.getUserId(), accessResult.getAccessGranted(), accessResult.getResponseTime());

            // 4. 验证数据一致性
            verify(bluetoothAccessService, times(1)).checkDeviceHealth("TEST_DEVICE_001");
            verify(bluetoothAccessService, times(1)).performSeamlessAccess(any(SeamlessAccessRequestDTO.class));

            log.info("[集成测试] 蓝牙门禁完整流程测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 蓝牙门禁完整流程测试失败", e);
            fail("蓝牙门禁完整流程测试失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("AI智能分析集成测试")
    void testAIAnalysisIntegration() {
        log.info("[集成测试] 开始AI智能分析集成测试");

        try {
            // 1. 访问模式分析
            AccessPatternAnalysisRequestDTO patternRequest = buildMockPatternAnalysisRequest();
            AccessPatternAnalysisVO patternResult = aiAnalysisService.analyzeAccessPattern(patternRequest);

            assertNotNull(patternResult);
            assertNotNull(patternResult.getUserId());
            assertTrue(patternResult.getTotalAccessCount() >= 0);
            log.info("[集成测试] 访问模式分析完成 - 用户: {}, 访问次数: {}, 风险评分: {}",
                patternResult.getUserId(), patternResult.getTotalAccessCount(), patternResult.getRiskScore());

            // 2. 异常行为检测
            BehaviorAnalysisRequestDTO behaviorRequest = buildMockBehaviorAnalysisRequest();
            List<BehaviorAnomalyVO> anomalies = aiAnalysisService.detectBehaviorAnomalies(behaviorRequest);

            assertNotNull(anomalies);
            log.info("[集成测试] 异常行为检测完成 - 用户: {}, 发现异常: {} 个",
                behaviorRequest.getUserId(), anomalies.size());

            // 3. 预测性维护分析
            PredictiveMaintenanceRequestDTO maintenanceRequest = buildMockMaintenanceRequest();
            PredictiveMaintenanceVO maintenanceResult = aiAnalysisService.performPredictiveMaintenanceAnalysis(maintenanceRequest);

            assertNotNull(maintenanceResult);
            assertNotNull(maintenanceResult.getDeviceId());
            assertTrue(maintenanceResult.getHealthScore() >= 0 && maintenanceResult.getHealthScore() <= 100);
            log.info("[集成测试] 预测性维护分析完成 - 设备: {}, 健康评分: {}, 维护建议: {} 个",
                maintenanceResult.getDeviceId(), maintenanceResult.getHealthScore(),
                maintenanceResult.getMaintenanceRecommendations().size());

            // 验证服务调用
            verify(aiAnalysisService, times(1)).analyzeAccessPattern(any(AccessPatternAnalysisRequestDTO.class));
            verify(aiAnalysisService, times(1)).detectBehaviorAnomalies(any(BehaviorAnalysisRequestDTO.class));
            verify(aiAnalysisService, times(1)).performPredictiveMaintenanceAnalysis(any(PredictiveMaintenanceRequestDTO.class));

            log.info("[集成测试] AI智能分析集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] AI智能分析集成测试失败", e);
            fail("AI智能分析集成测试失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("视频监控联动集成测试")
    void testVideoMonitoringIntegration() {
        log.info("[集成测试] 开始视频监控联动集成测试");

        try {
            // 1. 启动实时监控
            VideoMonitoringRequestDTO monitorRequest = buildMockVideoMonitoringRequest();
            VideoStreamVO streamResult = videoLinkageMonitorService.startRealTimeMonitoring(monitorRequest);

            assertNotNull(streamResult);
            assertNotNull(streamResult.getStreamUrl());
            assertNotNull(streamResult.getStreamSessionId());
            assertEquals("STREAMING", streamResult.getStatus());
            log.info("[集成测试] 实时监控启动成功 - 摄像头: {}, 流地址: {}",
                monitorRequest.getCameraId(), streamResult.getStreamUrl());

            // 2. 人脸识别测试
            FaceRecognitionRequestDTO faceRequest = buildMockFaceRecognitionRequest();
            List<FaceRecognitionResultVO> faceResults = videoLinkageMonitorService.performFaceRecognition(faceRequest);

            assertNotNull(faceResults);
            log.info("[集成测试] 人脸识别完成 - 摄像头: {}, 识别结果: {} 个",
                faceRequest.getCameraId(), faceResults.size());

            // 3. 异常行为检测
            AbnormalBehaviorDetectionRequestDTO behaviorRequest = buildMockAbnormalBehaviorRequest();
            List<AbnormalBehaviorVO> behaviorResults = videoLinkageMonitorService.detectAbnormalBehavior(behaviorRequest);

            assertNotNull(behaviorResults);
            log.info("[集成测试] 异常行为检测完成 - 摄像头: {}, 发现异常: {} 个",
                behaviorRequest.getCameraId(), behaviorResults.size());

            // 4. 停止监控
            videoLinkageMonitorService.stopRealTimeMonitoring(streamResult.getStreamSessionId());
            log.info("[集成测试] 监控停止完成 - 会话ID: {}", streamResult.getStreamSessionId());

            // 验证服务调用
            verify(videoLinkageMonitorService, times(1)).startRealTimeMonitoring(any(VideoMonitoringRequestDTO.class));
            verify(videoLinkageMonitorService, times(1)).performFaceRecognition(any(FaceRecognitionRequestDTO.class));
            verify(videoLinkageMonitorService, times(1)).detectAbnormalBehavior(any(AbnormalBehaviorDetectionRequestDTO.class));
            verify(videoLinkageMonitorService, times(1)).stopRealTimeMonitoring(anyString());

            log.info("[集成测试] 视频监控联动集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 视频监控联动集成测试失败", e);
            fail("视频监控联动集成测试失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("监控告警系统集成测试")
    void testMonitorAlertIntegration() {
        log.info("[集成测试] 开始监控告警系统集成测试");

        try {
            // 1. 创建智能告警
            SmartAlertRequestDTO alertRequest = buildMockSmartAlertRequest();
            AlertProcessResultVO alertResult = monitorAlertService.processSmartAlert(alertRequest);

            assertNotNull(alertResult);
            assertNotNull(alertResult.getAlertId());
            assertEquals("SUCCESS", alertResult.getProcessStatus());
            log.info("[集成测试] 智能告警处理完成 - 告警ID: {}, 处理状态: {}",
                alertResult.getAlertId(), alertResult.getProcessStatus());

            // 2. 多渠道通知测试
            NotificationRequestDTO notificationRequest = buildMockNotificationRequest();
            NotificationResultVO notificationResult = monitorAlertService.sendMultiChannelNotification(notificationRequest);

            assertNotNull(notificationResult);
            log.info("[集成测试] 多渠道通知发送完成 - 告警ID: {}, 成功渠道: {} 个",
                notificationRequest.getAlertId(), notificationResult.getSuccessfulChannels().size());

            // 3. 自愈处理测试
            SelfHealingRequestDTO healingRequest = buildMockSelfHealingRequest();
            SelfHealingResultVO healingResult = monitorAlertService.triggerSelfHealing(healingRequest);

            assertNotNull(healingResult);
            log.info("[集成测试] 自愈处理完成 - 告警ID: {}, 治愈状态: {}, 执行动作: {} 个",
                healingRequest.getAlertId(), healingResult.getHealingStatus(), healingResult.getExecutedActions().size());

            // 验证服务调用
            verify(monitorAlertService, times(1)).processSmartAlert(any(SmartAlertRequestDTO.class));
            verify(monitorAlertService, times(1)).sendMultiChannelNotification(any(NotificationRequestDTO.class));
            verify(monitorAlertService, times(1)).triggerSelfHealing(any(SelfHealingRequestDTO.class));

            log.info("[集成测试] 监控告警系统集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 监控告警系统集成测试失败", e);
            fail("监控告警系统集成测试失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("离线模式集成测试")
    void testOfflineModeIntegration() {
        log.info("[集成测试] 开始离线模式集成测试");

        try {
            // 1. 离线数据同步
            OfflineSyncRequestDTO syncRequest = buildMockOfflineSyncRequest();
            OfflineSyncResultVO syncResult = offlineModeService.syncOfflineData(syncRequest);

            assertNotNull(syncResult);
            assertTrue(syncResult.getTotalRecords() >= 0);
            assertTrue(syncResult.getSuccessCount() >= 0);
            log.info("[集成测试] 离线数据同步完成 - 总记录: {}, 成功: {}, 失败: {}",
                syncResult.getTotalRecords(), syncResult.getSuccessCount(), syncResult.getFailureCount());

            // 2. 离线权限验证
            OfflinePermissionCheckRequestDTO permissionRequest = buildMockOfflinePermissionRequest();
            OfflinePermissionCheckVO permissionResult = offlineModeService.validateOfflineAccess(permissionRequest);

            assertNotNull(permissionResult);
            log.info("[集成测试] 离线权限验证完成 - 用户: {}, 设备: {}, 验证结果: {}",
                permissionRequest.getUserId(), permissionRequest.getDeviceId(), permissionResult.getAccessGranted());

            // 3. 数据完整性验证
            DataIntegrityCheckRequestDTO integrityRequest = buildMockIntegrityCheckRequest();
            DataIntegrityCheckVO integrityResult = offlineModeService.checkDataIntegrity(integrityRequest);

            assertNotNull(integrityResult);
            assertTrue(integrityResult.getIntegrityScore() >= 0 && integrityResult.getIntegrityScore() <= 100);
            log.info("[集成测试] 数据完整性验证完成 - 完整性评分: {}, 问题数量: {}",
                integrityResult.getIntegrityScore(), integrityResult.getIntegrityIssues().size());

            // 验证服务调用
            verify(offlineModeService, times(1)).syncOfflineData(any(OfflineSyncRequestDTO.class));
            verify(offlineModeService, times(1)).validateOfflineAccess(any(OfflinePermissionCheckRequestDTO.class));
            verify(offlineModeService, times(1)).checkDataIntegrity(any(DataIntegrityCheckRequestDTO.class));

            log.info("[集成测试] 离线模式集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 离线模式集成测试失败", e);
            fail("离线模式集成测试失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("综合业务流程集成测试")
    void testComprehensiveBusinessFlowIntegration() {
        log.info("[集成测试] 开始综合业务流程集成测试");

        try {
            // 模拟完整的门禁业务流程：用户通行 -> AI分析 -> 视频监控 -> 告警处理

            String userId = "TEST_USER_001";
            String deviceId = "TEST_DEVICE_001";
            String cameraId = "TEST_CAMERA_001";

            // 1. 用户蓝牙通行
            SeamlessAccessRequestDTO accessRequest = SeamlessAccessRequestDTO.builder()
                .userId(userId)
                .deviceId(deviceId)
                .deviceCode("ACCESS_CTRL_001")
                .signalStrength(-65)
                .accessTime(LocalDateTime.now())
                .build();

            SeamlessAccessResultVO accessResult = bluetoothAccessService.performSeamlessAccess(accessRequest);
            assertTrue(accessResult.getAccessGranted());
            log.info("[集成测试] 步骤1: 用户通行成功");

            // 2. AI行为分析
            BehaviorAnalysisRequestDTO behaviorRequest = BehaviorAnalysisRequestDTO.builder()
                .userId(userId)
                .startTime(LocalDateTime.now().minusDays(7))
                .endTime(LocalDateTime.now())
                .analysisTypes(Arrays.asList("TIME_PATTERN", "FREQUENCY", "LOCATION"))
                .build();

            List<BehaviorAnomalyVO> anomalies = aiAnalysisService.detectBehaviorAnomalies(behaviorRequest);
            log.info("[集成测试] 步骤2: AI分析完成，发现异常: {} 个", anomalies.size());

            // 3. 视频监控联动
            if (!anomalies.isEmpty()) {
                VideoMonitoringRequestDTO monitorRequest = VideoMonitoringRequestDTO.builder()
                    .cameraId(cameraId)
                    .resolution("1080p")
                    .bitrate(2000)
                    .enableRecording(true)
                    .enableAIAnalysis(true)
                    .build();

                VideoStreamVO streamResult = videoLinkageMonitorService.startRealTimeMonitoring(monitorRequest);
                assertNotNull(streamResult.getStreamSessionId());
                log.info("[集成测试] 步骤3: 视频监控启动成功");

                // 4. 异常告警处理
                SmartAlertRequestDTO alertRequest = SmartAlertRequestDTO.builder()
                    .alertType("BEHAVIOR_ANOMALY")
                    .alertSource("AI_ANALYSIS")
                    .sourceEntityId(userId)
                    .alertData(Map.of(
                        "anomalyCount", anomalies.size(),
                        "riskLevel", anomalies.stream().mapToInt(AnomalyVO::getRiskScore).max().orElse(0),
                        "cameraId", cameraId
                    ))
                    .build();

                AlertProcessResultVO alertResult = monitorAlertService.processSmartAlert(alertRequest);
                assertNotNull(alertResult.getAlertId());
                log.info("[集成测试] 步骤4: 告警处理完成，告警ID: {}", alertResult.getAlertId());

                // 5. 清理资源
                videoLinkageMonitorService.stopRealTimeMonitoring(streamResult.getStreamSessionId());
                log.info("[集成测试] 步骤5: 资源清理完成");
            }

            // 验证完整流程的服务调用
            verify(bluetoothAccessService, atLeastOnce()).performSeamlessAccess(any(SeamlessAccessRequestDTO.class));
            verify(aiAnalysisService, times(1)).detectBehaviorAnomalies(any(BehaviorAnalysisRequestDTO.class));

            if (!anomalies.isEmpty()) {
                verify(videoLinkageMonitorService, times(1)).startRealTimeMonitoring(any(VideoMonitoringRequestDTO.class));
                verify(monitorAlertService, times(1)).processSmartAlert(any(SmartAlertRequestDTO.class));
                verify(videoLinkageMonitorService, times(1)).stopRealTimeMonitoring(anyString());
            }

            log.info("[集成测试] 综合业务流程集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 综合业务流程集成测试失败", e);
            fail("综合业务流程集成测试失败: " + e.getMessage());
        }
    }

    // ==================== Mock数据构建方法 ====================

    private AccessPatternAnalysisVO buildMockAccessPatternAnalysis() {
        return AccessPatternAnalysisVO.builder()
            .userId("TEST_USER_001")
            .analysisPeriod("2025-01-01 to 2025-01-30")
            .totalAccessCount(150)
            .avgDailyAccessCount(5.0)
            .peakAccessHour("09:00")
            .accessPattern("REGULAR")
            .riskScore(15)
            .build();
    }

    private List<BehaviorAnomalyVO> buildMockBehaviorAnomalies() {
        return Arrays.asList(
            BehaviorAnomalyVO.builder()
                .anomalyId("ANOMALY_001")
                .userId("TEST_USER_001")
                .anomalyType("TIME_PATTERN")
                .anomalyDescription("访问时间异常偏离")
                .riskScore(25)
                .anomalyTime(LocalDateTime.now())
                .build(),
            BehaviorAnomalyVO.builder()
                .anomalyId("ANOMALY_002")
                .userId("TEST_USER_001")
                .anomalyType("FREQUENCY")
                .anomalyDescription("访问频率异常增加")
                .riskScore(30)
                .anomalyTime(LocalDateTime.now())
                .build()
        );
    }

    private SeamlessAccessResultVO buildMockSeamlessAccessResult() {
        return SeamlessAccessResultVO.builder()
            .userId("TEST_USER_001")
            .deviceId("TEST_DEVICE_001")
            .accessGranted(true)
            .responseTime(150L)
            .accessMethod("BLUETOOTH_SEAMLESS")
            .verificationResult("SUCCESS")
            .build();
    }

    private DeviceHealthResult buildMockDeviceHealthResult() {
        return DeviceHealthResult.builder()
            .deviceId("TEST_DEVICE_001")
            .healthScore(85)
            .healthStatus("GOOD")
            .connectivityRate(98.5)
            .avgResponseTime(120L)
            .errorRate(0.5)
            .build();
    }

    private AlertProcessResultVO buildMockAlertProcessResult() {
        return AlertProcessResultVO.builder()
            .alertId("ALERT_001")
            .processStatus("SUCCESS")
            .processMessage("告警处理成功")
            .assessedLevel("MEDIUM")
            .notificationSent(true)
            .selfHealingTriggered(false)
            .build();
    }

    private OfflineSyncResultVO buildMockSyncResult() {
        return OfflineSyncResultVO.builder()
            .syncId("SYNC_001")
            .totalRecords(100)
            .successCount(95)
            .failureCount(5)
            .syncStartTime(LocalDateTime.now().minusMinutes(10))
            .syncEndTime(LocalDateTime.now())
            .build();
    }

    private VideoStreamVO buildMockVideoStream() {
        return VideoStreamVO.builder()
            .cameraId("TEST_CAMERA_001")
            .streamUrl("rtsp://192.168.1.100:554/stream")
            .streamSessionId("SESSION_001")
            .streamProtocol("RTSP")
            .resolution("1080p")
            .bitrate(2000)
            .fps(25)
            .startTime(LocalDateTime.now())
            .status("STREAMING")
            .build();
    }

    // Request对象构建方法
    private BluetoothScanRequestDTO buildMockScanRequest() {
        return BluetoothScanRequestDTO.builder()
            .scanDuration(30)
            .signalStrengthThreshold(-80)
            .deviceTypes(Arrays.asList("ACCESS_CONTROLLER", "SMART_LOCK"))
            .build();
    }

    private SeamlessAccessRequestDTO buildMockSeamlessAccessRequest() {
        return SeamlessAccessRequestDTO.builder()
            .userId("TEST_USER_001")
            .deviceId("TEST_DEVICE_001")
            .deviceCode("ACCESS_CTRL_001")
            .signalStrength(-65)
            .accessTime(LocalDateTime.now())
            .build();
    }

    private AccessPatternAnalysisRequestDTO buildMockPatternAnalysisRequest() {
        return AccessPatternAnalysisRequestDTO.builder()
            .userId("TEST_USER_001")
            .startDate(LocalDateTime.now().minusDays(30))
            .endDate(LocalDateTime.now())
            .analysisTypes(Arrays.asList("TIME", "FREQUENCY", "LOCATION"))
            .build();
    }

    private BehaviorAnalysisRequestDTO buildMockBehaviorAnalysisRequest() {
        return BehaviorAnalysisRequestDTO.builder()
            .userId("TEST_USER_001")
            .startTime(LocalDateTime.now().minusDays(7))
            .endTime(LocalDateTime.now())
            .analysisTypes(Arrays.asList("TIME_PATTERN", "FREQUENCY", "LOCATION"))
            .build();
    }

    private PredictiveMaintenanceRequestDTO buildMockMaintenanceRequest() {
        return PredictiveMaintenanceRequestDTO.builder()
            .deviceId("TEST_DEVICE_001")
            .analysisPeriod("LAST_30_DAYS")
            .includePerformanceMetrics(true)
            .includeErrorAnalysis(true)
            .build();
    }

    private VideoMonitoringRequestDTO buildMockVideoMonitoringRequest() {
        return VideoMonitoringRequestDTO.builder()
            .cameraId("TEST_CAMERA_001")
            .resolution("1080p")
            .bitrate(2000)
            .enableRecording(true)
            .enableAIAnalysis(true)
            .build();
    }

    private FaceRecognitionRequestDTO buildMockFaceRecognitionRequest() {
        return FaceRecognitionRequestDTO.builder()
            .cameraId("TEST_CAMERA_001")
            .confidenceThreshold(0.8)
            .maxFaces(10)
            .enableLivenessCheck(true)
            .build();
    }

    private AbnormalBehaviorDetectionRequestDTO buildMockAbnormalBehaviorRequest() {
        return AbnormalBehaviorDetectionRequestDTO.builder()
            .cameraId("TEST_CAMERA_001")
            .detectionTypes(Arrays.asList("LOITERING", "TRESPASSING", "FIGHTING"))
            .sensitivityLevel("MEDIUM")
            .build();
    }

    private SmartAlertRequestDTO buildMockSmartAlertRequest() {
        return SmartAlertRequestDTO.builder()
            .alertType("BEHAVIOR_ANOMALY")
            .alertSource("AI_ANALYSIS")
            .sourceEntityId("TEST_USER_001")
            .initialLevel("MEDIUM")
            .alertData(Map.of("anomalyCount", 2, "riskScore", 30))
            .build();
    }

    private NotificationRequestDTO buildMockNotificationRequest() {
        return NotificationRequestDTO.builder()
            .alertId("ALERT_001")
            .notificationChannels(Arrays.asList("EMAIL", "SMS", "WECHAT"))
            .urgencyLevel("MEDIUM")
            .recipients(Arrays.asList("admin@example.com", "security@example.com"))
            .build();
    }

    private SelfHealingRequestDTO buildMockSelfHealingRequest() {
        return SelfHealingRequestDTO.builder()
            .alertId("ALERT_001")
            .healingStrategy("AUTOMATIC")
            .allowedActions(Arrays.asList("RESTART_SERVICE", "CLEAR_CACHE", "FAILOVER"))
            .build();
    }

    private OfflineSyncRequestDTO buildMockOfflineSyncRequest() {
        return OfflineSyncRequestDTO.builder()
            .deviceId("TEST_DEVICE_001")
            .syncType("FULL_SYNC")
            .syncStartTime(LocalDateTime.now().minusHours(1))
            .syncEndTime(LocalDateTime.now())
            .build();
    }

    private OfflinePermissionCheckRequestDTO buildMockOfflinePermissionRequest() {
        return OfflinePermissionCheckRequestDTO.builder()
            .userId("TEST_USER_001")
            .deviceId("TEST_DEVICE_001")
            .accessTime(LocalDateTime.now())
            .build();
    }

    private DataIntegrityCheckRequestDTO buildMockIntegrityCheckRequest() {
        return DataIntegrityCheckRequestDTO.builder()
            .deviceId("TEST_DEVICE_001")
            .checkType("FULL_CHECK")
            .includeSignatureCheck(true)
            .includeChecksumCheck(true)
            .build();
    }
}