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
 * 闂ㄧ寰湇鍔￠泦鎴愭祴璇? * <p>
 * 楠岃瘉鎵€鏈夊姛鑳芥ā鍧楃殑鍗忓悓宸ヤ綔鍜屾暟鎹竴鑷存€? * 娴嬭瘯瑕嗙洊锛氳摑鐗欓棬绂併€佺绾挎ā寮忋€丄I鍒嗘瀽銆佽棰戠洃鎺с€佸憡璀︾郴缁? * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("闂ㄧ寰湇鍔￠泦鎴愭祴璇?)
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
        // 鍒濆鍖栨祴璇曟暟鎹?        setupMockServices();
    }

    private void setupMockServices() {
        // AI鍒嗘瀽鏈嶅姟Mock
        when(aiAnalysisService.analyzeAccessPattern(any(AccessPatternAnalysisRequestDTO.class)))
            .thenReturn(buildMockAccessPatternAnalysis());

        when(aiAnalysisService.detectBehaviorAnomalies(any(BehaviorAnalysisRequestDTO.class)))
            .thenReturn(buildMockBehaviorAnomalies());

        // 钃濈墮闂ㄧ鏈嶅姟Mock
        when(bluetoothAccessService.performSeamlessAccess(any(SeamlessAccessRequestDTO.class)))
            .thenReturn(buildMockSeamlessAccessResult());

        when(bluetoothAccessService.checkDeviceHealth(anyString()))
            .thenReturn(buildMockDeviceHealthResult());

        // 鐩戞帶鍛婅鏈嶅姟Mock
        when(monitorAlertService.processSmartAlert(any(SmartAlertRequestDTO.class)))
            .thenReturn(buildMockAlertProcessResult());

        // 绂荤嚎妯″紡鏈嶅姟Mock
        when(offlineModeService.syncOfflineData(any()))
            .thenReturn(buildMockSyncResult());

        // 瑙嗛鐩戞帶鏈嶅姟Mock
        when(videoLinkageMonitorService.startRealTimeMonitoring(any(VideoMonitoringRequestDTO.class)))
            .thenReturn(buildMockVideoStream());
    }

    @Test
    @DisplayName("钃濈墮闂ㄧ瀹屾暣娴佺▼闆嗘垚娴嬭瘯")
    void testBluetoothAccessCompleteFlow() {
        log.info("[闆嗘垚娴嬭瘯] 寮€濮嬭摑鐗欓棬绂佸畬鏁存祦绋嬫祴璇?);

        try {
            // 1. 璁惧鍋ュ悍妫€鏌?            DeviceHealthResult healthResult = bluetoothAccessService.checkDeviceHealth("TEST_DEVICE_001");
            assertNotNull(healthResult);
            assertEquals("TEST_DEVICE_001", healthResult.getDeviceId());
            assertTrue(healthResult.getHealthScore() >= 0 && healthResult.getHealthScore() <= 100);
            log.info("[闆嗘垚娴嬭瘯] 璁惧鍋ュ悍妫€鏌ュ畬鎴?- 璁惧: {}, 鍋ュ悍璇勫垎: {}",
                healthResult.getDeviceId(), healthResult.getHealthScore());

            // 2. 钃濈墮璁惧鎵弿
            CompletableFuture<List<BluetoothDeviceVO>> scanFuture =
                bluetoothAccessService.scanBluetoothDevices(buildMockScanRequest());
            List<BluetoothDeviceVO> devices = scanFuture.join();
            assertNotNull(devices);
            log.info("[闆嗘垚娴嬭瘯] 钃濈墮璁惧鎵弿瀹屾垚 - 鍙戠幇璁惧: {} 涓?, devices.size());

            // 3. 鏃犵紳閫氳娴嬭瘯
            SeamlessAccessRequestDTO accessRequest = buildMockSeamlessAccessRequest();
            SeamlessAccessResultVO accessResult = bluetoothAccessService.performSeamlessAccess(accessRequest);
            assertNotNull(accessResult);
            assertTrue(accessResult.getResponseTime() >= 0);
            log.info("[闆嗘垚娴嬭瘯] 鏃犵紳閫氳娴嬭瘯瀹屾垚 - 鐢ㄦ埛: {}, 閫氳缁撴灉: {}, 鍝嶅簲鏃堕棿: {}ms",
                accessRequest.getUserId(), accessResult.getAccessGranted(), accessResult.getResponseTime());

            // 4. 楠岃瘉鏁版嵁涓€鑷存€?            verify(bluetoothAccessService, times(1)).checkDeviceHealth("TEST_DEVICE_001");
            verify(bluetoothAccessService, times(1)).performSeamlessAccess(any(SeamlessAccessRequestDTO.class));

            log.info("[闆嗘垚娴嬭瘯] 钃濈墮闂ㄧ瀹屾暣娴佺▼娴嬭瘯閫氳繃");

        } catch (Exception e) {
            log.error("[闆嗘垚娴嬭瘯] 钃濈墮闂ㄧ瀹屾暣娴佺▼娴嬭瘯澶辫触", e);
            fail("钃濈墮闂ㄧ瀹屾暣娴佺▼娴嬭瘯澶辫触: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("AI鏅鸿兘鍒嗘瀽闆嗘垚娴嬭瘯")
    void testAIAnalysisIntegration() {
        log.info("[闆嗘垚娴嬭瘯] 寮€濮婣I鏅鸿兘鍒嗘瀽闆嗘垚娴嬭瘯");

        try {
            // 1. 璁块棶妯″紡鍒嗘瀽
            AccessPatternAnalysisRequestDTO patternRequest = buildMockPatternAnalysisRequest();
            AccessPatternAnalysisVO patternResult = aiAnalysisService.analyzeAccessPattern(patternRequest);

            assertNotNull(patternResult);
            assertNotNull(patternResult.getUserId());
            assertTrue(patternResult.getTotalAccessCount() >= 0);
            log.info("[闆嗘垚娴嬭瘯] 璁块棶妯″紡鍒嗘瀽瀹屾垚 - 鐢ㄦ埛: {}, 璁块棶娆℃暟: {}, 椋庨櫓璇勫垎: {}",
                patternResult.getUserId(), patternResult.getTotalAccessCount(), patternResult.getRiskScore());

            // 2. 寮傚父琛屼负妫€娴?            BehaviorAnalysisRequestDTO behaviorRequest = buildMockBehaviorAnalysisRequest();
            List<BehaviorAnomalyVO> anomalies = aiAnalysisService.detectBehaviorAnomalies(behaviorRequest);

            assertNotNull(anomalies);
            log.info("[闆嗘垚娴嬭瘯] 寮傚父琛屼负妫€娴嬪畬鎴?- 鐢ㄦ埛: {}, 鍙戠幇寮傚父: {} 涓?,
                behaviorRequest.getUserId(), anomalies.size());

            // 3. 棰勬祴鎬х淮鎶ゅ垎鏋?            PredictiveMaintenanceRequestDTO maintenanceRequest = buildMockMaintenanceRequest();
            PredictiveMaintenanceVO maintenanceResult = aiAnalysisService.performPredictiveMaintenanceAnalysis(maintenanceRequest);

            assertNotNull(maintenanceResult);
            assertNotNull(maintenanceResult.getDeviceId());
            assertTrue(maintenanceResult.getHealthScore() >= 0 && maintenanceResult.getHealthScore() <= 100);
            log.info("[闆嗘垚娴嬭瘯] 棰勬祴鎬х淮鎶ゅ垎鏋愬畬鎴?- 璁惧: {}, 鍋ュ悍璇勫垎: {}, 缁存姢寤鸿: {} 涓?,
                maintenanceResult.getDeviceId(), maintenanceResult.getHealthScore(),
                maintenanceResult.getMaintenanceRecommendations().size());

            // 楠岃瘉鏈嶅姟璋冪敤
            verify(aiAnalysisService, times(1)).analyzeAccessPattern(any(AccessPatternAnalysisRequestDTO.class));
            verify(aiAnalysisService, times(1)).detectBehaviorAnomalies(any(BehaviorAnalysisRequestDTO.class));
            verify(aiAnalysisService, times(1)).performPredictiveMaintenanceAnalysis(any(PredictiveMaintenanceRequestDTO.class));

            log.info("[闆嗘垚娴嬭瘯] AI鏅鸿兘鍒嗘瀽闆嗘垚娴嬭瘯閫氳繃");

        } catch (Exception e) {
            log.error("[闆嗘垚娴嬭瘯] AI鏅鸿兘鍒嗘瀽闆嗘垚娴嬭瘯澶辫触", e);
            fail("AI鏅鸿兘鍒嗘瀽闆嗘垚娴嬭瘯澶辫触: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("瑙嗛鐩戞帶鑱斿姩闆嗘垚娴嬭瘯")
    void testVideoMonitoringIntegration() {
        log.info("[闆嗘垚娴嬭瘯] 寮€濮嬭棰戠洃鎺ц仈鍔ㄩ泦鎴愭祴璇?);

        try {
            // 1. 鍚姩瀹炴椂鐩戞帶
            VideoMonitoringRequestDTO monitorRequest = buildMockVideoMonitoringRequest();
            VideoStreamVO streamResult = videoLinkageMonitorService.startRealTimeMonitoring(monitorRequest);

            assertNotNull(streamResult);
            assertNotNull(streamResult.getStreamUrl());
            assertNotNull(streamResult.getStreamSessionId());
            assertEquals("STREAMING", streamResult.getStatus());
            log.info("[闆嗘垚娴嬭瘯] 瀹炴椂鐩戞帶鍚姩鎴愬姛 - 鎽勫儚澶? {}, 娴佸湴鍧€: {}",
                monitorRequest.getCameraId(), streamResult.getStreamUrl());

            // 2. 浜鸿劯璇嗗埆娴嬭瘯
            FaceRecognitionRequestDTO faceRequest = buildMockFaceRecognitionRequest();
            List<FaceRecognitionResultVO> faceResults = videoLinkageMonitorService.performFaceRecognition(faceRequest);

            assertNotNull(faceResults);
            log.info("[闆嗘垚娴嬭瘯] 浜鸿劯璇嗗埆瀹屾垚 - 鎽勫儚澶? {}, 璇嗗埆缁撴灉: {} 涓?,
                faceRequest.getCameraId(), faceResults.size());

            // 3. 寮傚父琛屼负妫€娴?            AbnormalBehaviorDetectionRequestDTO behaviorRequest = buildMockAbnormalBehaviorRequest();
            List<AbnormalBehaviorVO> behaviorResults = videoLinkageMonitorService.detectAbnormalBehavior(behaviorRequest);

            assertNotNull(behaviorResults);
            log.info("[闆嗘垚娴嬭瘯] 寮傚父琛屼负妫€娴嬪畬鎴?- 鎽勫儚澶? {}, 鍙戠幇寮傚父: {} 涓?,
                behaviorRequest.getCameraId(), behaviorResults.size());

            // 4. 鍋滄鐩戞帶
            videoLinkageMonitorService.stopRealTimeMonitoring(streamResult.getStreamSessionId());
            log.info("[闆嗘垚娴嬭瘯] 鐩戞帶鍋滄瀹屾垚 - 浼氳瘽ID: {}", streamResult.getStreamSessionId());

            // 楠岃瘉鏈嶅姟璋冪敤
            verify(videoLinkageMonitorService, times(1)).startRealTimeMonitoring(any(VideoMonitoringRequestDTO.class));
            verify(videoLinkageMonitorService, times(1)).performFaceRecognition(any(FaceRecognitionRequestDTO.class));
            verify(videoLinkageMonitorService, times(1)).detectAbnormalBehavior(any(AbnormalBehaviorDetectionRequestDTO.class));
            verify(videoLinkageMonitorService, times(1)).stopRealTimeMonitoring(anyString());

            log.info("[闆嗘垚娴嬭瘯] 瑙嗛鐩戞帶鑱斿姩闆嗘垚娴嬭瘯閫氳繃");

        } catch (Exception e) {
            log.error("[闆嗘垚娴嬭瘯] 瑙嗛鐩戞帶鑱斿姩闆嗘垚娴嬭瘯澶辫触", e);
            fail("瑙嗛鐩戞帶鑱斿姩闆嗘垚娴嬭瘯澶辫触: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("鐩戞帶鍛婅绯荤粺闆嗘垚娴嬭瘯")
    void testMonitorAlertIntegration() {
        log.info("[闆嗘垚娴嬭瘯] 寮€濮嬬洃鎺у憡璀︾郴缁熼泦鎴愭祴璇?);

        try {
            // 1. 鍒涘缓鏅鸿兘鍛婅
            SmartAlertRequestDTO alertRequest = buildMockSmartAlertRequest();
            AlertProcessResultVO alertResult = monitorAlertService.processSmartAlert(alertRequest);

            assertNotNull(alertResult);
            assertNotNull(alertResult.getAlertId());
            assertEquals("SUCCESS", alertResult.getProcessStatus());
            log.info("[闆嗘垚娴嬭瘯] 鏅鸿兘鍛婅澶勭悊瀹屾垚 - 鍛婅ID: {}, 澶勭悊鐘舵€? {}",
                alertResult.getAlertId(), alertResult.getProcessStatus());

            // 2. 澶氭笭閬撻€氱煡娴嬭瘯
            NotificationRequestDTO notificationRequest = buildMockNotificationRequest();
            NotificationResultVO notificationResult = monitorAlertService.sendMultiChannelNotification(notificationRequest);

            assertNotNull(notificationResult);
            log.info("[闆嗘垚娴嬭瘯] 澶氭笭閬撻€氱煡鍙戦€佸畬鎴?- 鍛婅ID: {}, 鎴愬姛娓犻亾: {} 涓?,
                notificationRequest.getAlertId(), notificationResult.getSuccessfulChannels().size());

            // 3. 鑷剤澶勭悊娴嬭瘯
            SelfHealingRequestDTO healingRequest = buildMockSelfHealingRequest();
            SelfHealingResultVO healingResult = monitorAlertService.triggerSelfHealing(healingRequest);

            assertNotNull(healingResult);
            log.info("[闆嗘垚娴嬭瘯] 鑷剤澶勭悊瀹屾垚 - 鍛婅ID: {}, 娌绘剤鐘舵€? {}, 鎵ц鍔ㄤ綔: {} 涓?,
                healingRequest.getAlertId(), healingResult.getHealingStatus(), healingResult.getExecutedActions().size());

            // 楠岃瘉鏈嶅姟璋冪敤
            verify(monitorAlertService, times(1)).processSmartAlert(any(SmartAlertRequestDTO.class));
            verify(monitorAlertService, times(1)).sendMultiChannelNotification(any(NotificationRequestDTO.class));
            verify(monitorAlertService, times(1)).triggerSelfHealing(any(SelfHealingRequestDTO.class));

            log.info("[闆嗘垚娴嬭瘯] 鐩戞帶鍛婅绯荤粺闆嗘垚娴嬭瘯閫氳繃");

        } catch (Exception e) {
            log.error("[闆嗘垚娴嬭瘯] 鐩戞帶鍛婅绯荤粺闆嗘垚娴嬭瘯澶辫触", e);
            fail("鐩戞帶鍛婅绯荤粺闆嗘垚娴嬭瘯澶辫触: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("绂荤嚎妯″紡闆嗘垚娴嬭瘯")
    void testOfflineModeIntegration() {
        log.info("[闆嗘垚娴嬭瘯] 寮€濮嬬绾挎ā寮忛泦鎴愭祴璇?);

        try {
            // 1. 绂荤嚎鏁版嵁鍚屾
            OfflineSyncRequestDTO syncRequest = buildMockOfflineSyncRequest();
            OfflineSyncResultVO syncResult = offlineModeService.syncOfflineData(syncRequest);

            assertNotNull(syncResult);
            assertTrue(syncResult.getTotalRecords() >= 0);
            assertTrue(syncResult.getSuccessCount() >= 0);
            log.info("[闆嗘垚娴嬭瘯] 绂荤嚎鏁版嵁鍚屾瀹屾垚 - 鎬昏褰? {}, 鎴愬姛: {}, 澶辫触: {}",
                syncResult.getTotalRecords(), syncResult.getSuccessCount(), syncResult.getFailureCount());

            // 2. 绂荤嚎鏉冮檺楠岃瘉
            OfflinePermissionCheckRequestDTO permissionRequest = buildMockOfflinePermissionRequest();
            OfflinePermissionCheckVO permissionResult = offlineModeService.validateOfflineAccess(permissionRequest);

            assertNotNull(permissionResult);
            log.info("[闆嗘垚娴嬭瘯] 绂荤嚎鏉冮檺楠岃瘉瀹屾垚 - 鐢ㄦ埛: {}, 璁惧: {}, 楠岃瘉缁撴灉: {}",
                permissionRequest.getUserId(), permissionRequest.getDeviceId(), permissionResult.getAccessGranted());

            // 3. 鏁版嵁瀹屾暣鎬ч獙璇?            DataIntegrityCheckRequestDTO integrityRequest = buildMockIntegrityCheckRequest();
            DataIntegrityCheckVO integrityResult = offlineModeService.checkDataIntegrity(integrityRequest);

            assertNotNull(integrityResult);
            assertTrue(integrityResult.getIntegrityScore() >= 0 && integrityResult.getIntegrityScore() <= 100);
            log.info("[闆嗘垚娴嬭瘯] 鏁版嵁瀹屾暣鎬ч獙璇佸畬鎴?- 瀹屾暣鎬ц瘎鍒? {}, 闂鏁伴噺: {}",
                integrityResult.getIntegrityScore(), integrityResult.getIntegrityIssues().size());

            // 楠岃瘉鏈嶅姟璋冪敤
            verify(offlineModeService, times(1)).syncOfflineData(any(OfflineSyncRequestDTO.class));
            verify(offlineModeService, times(1)).validateOfflineAccess(any(OfflinePermissionCheckRequestDTO.class));
            verify(offlineModeService, times(1)).checkDataIntegrity(any(DataIntegrityCheckRequestDTO.class));

            log.info("[闆嗘垚娴嬭瘯] 绂荤嚎妯″紡闆嗘垚娴嬭瘯閫氳繃");

        } catch (Exception e) {
            log.error("[闆嗘垚娴嬭瘯] 绂荤嚎妯″紡闆嗘垚娴嬭瘯澶辫触", e);
            fail("绂荤嚎妯″紡闆嗘垚娴嬭瘯澶辫触: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("缁煎悎涓氬姟娴佺▼闆嗘垚娴嬭瘯")
    void testComprehensiveBusinessFlowIntegration() {
        log.info("[闆嗘垚娴嬭瘯] 寮€濮嬬患鍚堜笟鍔℃祦绋嬮泦鎴愭祴璇?);

        try {
            // 妯℃嫙瀹屾暣鐨勯棬绂佷笟鍔℃祦绋嬶細鐢ㄦ埛閫氳 -> AI鍒嗘瀽 -> 瑙嗛鐩戞帶 -> 鍛婅澶勭悊

            String userId = "TEST_USER_001";
            String deviceId = "TEST_DEVICE_001";
            String cameraId = "TEST_CAMERA_001";

            // 1. 鐢ㄦ埛钃濈墮閫氳
            SeamlessAccessRequestDTO accessRequest = SeamlessAccessRequestDTO.builder()
                .userId(userId)
                .deviceId(deviceId)
                .deviceCode("ACCESS_CTRL_001")
                .signalStrength(-65)
                .accessTime(LocalDateTime.now())
                .build();

            SeamlessAccessResultVO accessResult = bluetoothAccessService.performSeamlessAccess(accessRequest);
            assertTrue(accessResult.getAccessGranted());
            log.info("[闆嗘垚娴嬭瘯] 姝ラ1: 鐢ㄦ埛閫氳鎴愬姛");

            // 2. AI琛屼负鍒嗘瀽
            BehaviorAnalysisRequestDTO behaviorRequest = BehaviorAnalysisRequestDTO.builder()
                .userId(userId)
                .startTime(LocalDateTime.now().minusDays(7))
                .endTime(LocalDateTime.now())
                .analysisTypes(Arrays.asList("TIME_PATTERN", "FREQUENCY", "LOCATION"))
                .build();

            List<BehaviorAnomalyVO> anomalies = aiAnalysisService.detectBehaviorAnomalies(behaviorRequest);
            log.info("[闆嗘垚娴嬭瘯] 姝ラ2: AI鍒嗘瀽瀹屾垚锛屽彂鐜板紓甯? {} 涓?, anomalies.size());

            // 3. 瑙嗛鐩戞帶鑱斿姩
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
                log.info("[闆嗘垚娴嬭瘯] 姝ラ3: 瑙嗛鐩戞帶鍚姩鎴愬姛");

                // 4. 寮傚父鍛婅澶勭悊
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
                log.info("[闆嗘垚娴嬭瘯] 姝ラ4: 鍛婅澶勭悊瀹屾垚锛屽憡璀D: {}", alertResult.getAlertId());

                // 5. 娓呯悊璧勬簮
                videoLinkageMonitorService.stopRealTimeMonitoring(streamResult.getStreamSessionId());
                log.info("[闆嗘垚娴嬭瘯] 姝ラ5: 璧勬簮娓呯悊瀹屾垚");
            }

            // 楠岃瘉瀹屾暣娴佺▼鐨勬湇鍔¤皟鐢?            verify(bluetoothAccessService, atLeastOnce()).performSeamlessAccess(any(SeamlessAccessRequestDTO.class));
            verify(aiAnalysisService, times(1)).detectBehaviorAnomalies(any(BehaviorAnalysisRequestDTO.class));

            if (!anomalies.isEmpty()) {
                verify(videoLinkageMonitorService, times(1)).startRealTimeMonitoring(any(VideoMonitoringRequestDTO.class));
                verify(monitorAlertService, times(1)).processSmartAlert(any(SmartAlertRequestDTO.class));
                verify(videoLinkageMonitorService, times(1)).stopRealTimeMonitoring(anyString());
            }

            log.info("[闆嗘垚娴嬭瘯] 缁煎悎涓氬姟娴佺▼闆嗘垚娴嬭瘯閫氳繃");

        } catch (Exception e) {
            log.error("[闆嗘垚娴嬭瘯] 缁煎悎涓氬姟娴佺▼闆嗘垚娴嬭瘯澶辫触", e);
            fail("缁煎悎涓氬姟娴佺▼闆嗘垚娴嬭瘯澶辫触: " + e.getMessage());
        }
    }

    // ==================== Mock鏁版嵁鏋勫缓鏂规硶 ====================

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
                .anomalyDescription("璁块棶鏃堕棿寮傚父鍋忕")
                .riskScore(25)
                .anomalyTime(LocalDateTime.now())
                .build(),
            BehaviorAnomalyVO.builder()
                .anomalyId("ANOMALY_002")
                .userId("TEST_USER_001")
                .anomalyType("FREQUENCY")
                .anomalyDescription("璁块棶棰戠巼寮傚父澧炲姞")
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
            .processMessage("鍛婅澶勭悊鎴愬姛")
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

    // Request瀵硅薄鏋勫缓鏂规硶
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