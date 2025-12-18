package net.lab1024.sa.access.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.service.SecurityEnhancerService;
import net.lab1024.sa.access.domain.form.BiometricDataForm;
import net.lab1024.sa.access.domain.vo.BiometricAntiSpoofResultVO;
import net.lab1024.sa.access.domain.vo.TrajectoryAnomalyResultVO;
import net.lab1024.sa.access.edge.controller.EdgeSecurityController;
import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.model.InferenceRequest;
import net.lab1024.sa.video.edge.model.InferenceResult;

/**
 * 澧炲己闂ㄧ瀹夊叏鎺у埗鍣?
 * <p>
 * 鍩轰簬杈圭紭璁＄畻鏋舵瀯鐨勯棬绂佸畨鍏ㄥ寮虹郴缁燂紝鎻愪緵锛?
 * - 澶氭ā鎬佺敓鐗╄瘑鍒紙浜鸿劯銆佹寚绾广€佽櫣鑶溿€佸０绾癸級
 * - 瀹炴椂娲讳綋妫€娴嬩笌闃蹭吉楠岃瘉
 * - 鏅鸿兘椋庨櫓璇嗗埆涓庡紓甯歌涓哄垎鏋?
 * - 杈圭紭AI鎺ㄧ悊涓庝簯杈瑰崗鍚?
 * - 瀹炴椂鐩戞帶涓庡憡璀﹁仈鍔?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@PermissionCheck(value = "ACCESS_MANAGE", description = "闂ㄧ瀹夊叏妯″潡鏉冮檺")
@RequestMapping("/api/v1/access/security")
@Tag(name = "澧炲己闂ㄧ瀹夊叏", description = "鍩轰簬杈圭紭璁＄畻鐨勯棬绂佸畨鍏ㄥ寮虹郴缁?)
public class EnhancedAccessSecurityController {

    @Resource
    private SecurityEnhancerService securityEnhancerService;

    @Resource
    private EdgeSecurityController edgeSecurityController;

    // ==================== 澶氭ā鎬佺敓鐗╄瘑鍒?====================

    /**
     * 澶氭ā鎬佺敓鐗╄瘑鍒獙璇?
     * <p>
     * 鏀寔浜鸿劯銆佹寚绾广€佽櫣鑶溿€佸０绾圭瓑澶氱鐢熺墿鐗瑰緛铻嶅悎楠岃瘉
     * 浣跨敤杈圭紭璁＄畻瀹炵幇姣绾у搷搴?
     * </p>
     *
     * @param biometricData 鐢熺墿璇嗗埆鏁版嵁
     * @return 楠岃瘉缁撴灉
     */
    @Observed(name = "access.security.multiModalAuth", contextualName = "multi-modal-biometric-auth")
    @PostMapping("/auth/multi-modal")
    @Operation(
            summary = "澶氭ā鎬佺敓鐗╄瘑鍒獙璇?,
            description = "铻嶅悎澶氱鐢熺墿鐗瑰緛杩涜韬唤楠岃瘉锛屾彁楂樺噯纭€у拰瀹夊叏鎬?,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "楠岃瘉鎴愬姛",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BiometricAntiSpoofResultVO.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "楠岃瘉澶辫触"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_USE", description = "澶氭ā鎬佺敓鐗╄瘑鍒獙璇?)
    public ResponseDTO<BiometricAntiSpoofResultVO> performMultiModalAuthentication(
            @Valid @RequestBody BiometricDataForm biometricData) {

        log.info("[澧炲己瀹夊叏] 澶氭ā鎬佺敓鐗╄瘑鍒獙璇侊紝userId={}, device={}, types={}",
                biometricData.getUserId(), biometricData.getDeviceId(), biometricData.getBiometricTypes());

        try {
            // 1. 杈圭紭璁惧娲讳綋妫€娴?
            CompletableFuture<BiometricAntiSpoofResultVO> antiSpoofResult =
                performEdgeAntiSpoofing(biometricData);

            // 2. 澶氭ā鎬佺壒寰佽瀺鍚堥獙璇?
            CompletableFuture<BiometricAntiSpoofResultVO> fusionResult =
                performMultiModalFusion(biometricData);

            // 3. 绛夊緟缁撴灉鍚堝苟
            BiometricAntiSpoofResultVO antiSpoof = antiSpoofResult.get();
            BiometricAntiSpoofResultVO fusion = fusionResult.get();

            // 4. 缁煎悎瀹夊叏璇勪及
            BiometricAntiSpoofResultVO finalResult = securityEnhancerService
                .performSecurityAssessment(antiSpoof, fusion);

            log.info("[澧炲己瀹夊叏] 澶氭ā鎬侀獙璇佸畬鎴愶紝userId={}, confidence={}, riskLevel={}",
                    biometricData.getUserId(), finalResult.getConfidence(), finalResult.getRiskLevel());

            return ResponseDTO.ok(finalResult);

        } catch (Exception e) {
            log.error("[澧炲己瀹夊叏] 澶氭ā鎬侀獙璇佸紓甯革紝userId={}, error={}",
                    biometricData.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("MULTI_MODAL_AUTH_ERROR", "澶氭ā鎬侀獙璇佸紓甯革細" + e.getMessage());
        }
    }

    /**
     * 瀹炴椂娲讳綋妫€娴?
     * <p>
     * 鍩轰簬杈圭紭AI杩涜瀹炴椂娲讳綋妫€娴嬶紝闃叉鐓х墖銆佽棰戙€侀潰鍏风瓑鏀诲嚮
     * 鏀寔澶氱娲讳綋妫€娴嬬畻娉曡瀺鍚?
     * </p>
     *
     * @param biometricData 鐢熺墿璇嗗埆鏁版嵁
     * @return 娲讳綋妫€娴嬬粨鏋?
     */
    @Observed(name = "access.security.antiSpoofing", contextualName = "real-time-anti-spoofing")
    @PostMapping("/auth/anti-spoofing")
    @Operation(
            summary = "瀹炴椂娲讳綋妫€娴?,
            description = "鍩轰簬杈圭紭AI杩涜瀹炴椂娲讳綋妫€娴嬶紝闃叉鍚勭娆洪獥鏀诲嚮",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "妫€娴嬪畬鎴?
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_USE", description = "瀹炴椂娲讳綋妫€娴?)
    public ResponseDTO<BiometricAntiSpoofResultVO> performRealTimeAntiSpoofing(
            @Valid @RequestBody BiometricDataForm biometricData) {

        log.info("[澧炲己瀹夊叏] 瀹炴椂娲讳綋妫€娴嬶紝userId={}, device={}",
                biometricData.getUserId(), biometricData.getDeviceId());

        try {
            // 杈圭紭璁惧娲讳綋妫€娴?
            CompletableFuture<BiometricAntiSpoofResultVO> edgeResult =
                performEdgeAntiSpoofing(biometricData);

            BiometricAntiSpoofResultVO result = edgeResult.get();

            log.info("[澧炲己瀹夊叏] 娲讳綋妫€娴嬪畬鎴愶紝userId={}, isLive={}, confidence={}",
                    biometricData.getUserId(), result.isLive(), result.getConfidence());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[澧炲己瀹夊叏] 娲讳綋妫€娴嬪紓甯革紝userId={}, error={}",
                    biometricData.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("ANTI_SPOOFING_ERROR", "娲讳綋妫€娴嬪紓甯革細" + e.getMessage());
        }
    }

    /**
     * 寮傚父琛屼负鍒嗘瀽
     * <p>
     * 鍩轰簬鍘嗗彶璁块棶鏁版嵁鍜屽疄鏃惰涓烘ā寮忚繘琛屽紓甯告娴?
     * 璇嗗埆娼滃湪鐨勫▉鑳佸拰寮傚父璁块棶妯″紡
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param analysisPeriod 鍒嗘瀽鍛ㄦ湡锛堝ぉ锛?
     * @return 寮傚父琛屼负鍒嗘瀽缁撴灉
     */
    @Observed(name = "access.security.behaviorAnalysis", contextualName = "abnormal-behavior-analysis")
    @PostMapping("/analysis/abnormal-behavior")
    @Operation(
            summary = "寮傚父琛屼负鍒嗘瀽",
            description = "鍩轰簬鍘嗗彶鏁版嵁鍜屽疄鏃惰涓烘ā寮忚繘琛屽紓甯告娴嬪垎鏋?,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "鍒嗘瀽瀹屾垚"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "寮傚父琛屼负鍒嗘瀽")
    public ResponseDTO<TrajectoryAnomalyResultVO> analyzeAbnormalBehavior(
            @RequestParam @Parameter(description = "鐢ㄦ埛ID") Long userId,
            @RequestParam @Parameter(description = "璁惧ID") Long deviceId,
            @RequestParam(defaultValue = "30") @Parameter(description = "鍒嗘瀽鍛ㄦ湡(澶?") Integer analysisPeriod) {

        log.info("[澧炲己瀹夊叏] 寮傚父琛屼负鍒嗘瀽锛寀serId={}, deviceId={}, period={}澶?,
                userId, deviceId, analysisPeriod);

        try {
            TrajectoryAnomalyResultVO result = securityEnhancerService
                .analyzeAbnormalBehavior(userId, deviceId, analysisPeriod);

            log.info("[澧炲己瀹夊叏] 寮傚父琛屼负鍒嗘瀽瀹屾垚锛寀serId={}, anomaliesDetected={}, riskScore={}",
                    userId, result.getAnomaliesDetected(), result.getRiskScore());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[澧炲己瀹夊叏] 寮傚父琛屼负鍒嗘瀽寮傚父锛寀serId={}, error={}",
                    userId, e.getMessage(), e);
            return ResponseDTO.error("BEHAVIOR_ANALYSIS_ERROR", "寮傚父琛屼负鍒嗘瀽寮傚父锛? + e.getMessage());
        }
    }

    // ==================== 杈圭紭AI瀹夊叏鍔熻兘 ====================

    /**
     * 杈圭紭AI瀹夊叏楠岃瘉
     * <p>
     * 鍒╃敤杈圭紭璁惧鐨凙I鑳藉姏杩涜瀹炴椂鐨勫畨鍏ㄩ獙璇?
     * 鍖呮嫭浜鸿劯璇嗗埆銆佽涓哄垎鏋愩€侀闄╄瘎浼扮瓑
     * </p>
     *
     * @param inferenceRequest AI鎺ㄧ悊璇锋眰
     * @return AI鎺ㄧ悊缁撴灉
     */
    @Observed(name = "access.security.edgeAI", contextualName = "edge-ai-security-verification")
    @PostMapping("/ai/edge-verification")
    @Operation(
            summary = "杈圭紭AI瀹夊叏楠岃瘉",
            description = "鍒╃敤杈圭紭璁惧AI鑳藉姏杩涜瀹炴椂瀹夊叏楠岃瘉",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "楠岃瘉瀹屾垚"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_USE", description = "杈圭紭AI瀹夊叏楠岃瘉")
    public ResponseDTO<InferenceResult> performEdgeAIVerification(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[澧炲己瀹夊叏] 杈圭紭AI瀹夊叏楠岃瘉锛宼askId={}, taskType={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getTaskType(), inferenceRequest.getDeviceId());

        try {
            // 璋冪敤杈圭紭瀹夊叏鎺у埗鍣ㄦ墽琛孉I鎺ㄧ悊
            ResponseEntity<ResponseDTO<Future<InferenceResult>>> response =
                edgeSecurityController.performSecurityInference(inferenceRequest);

            // 鑾峰彇鎺ㄧ悊缁撴灉
            Future<InferenceResult> future = response.getBody().getData();
            InferenceResult result = future.get();

            log.info("[澧炲己瀹夊叏] 杈圭紭AI楠岃瘉瀹屾垚锛宼askId={}, success={}, confidence={}",
                    inferenceRequest.getTaskId(), result.isSuccess(), result.getConfidence());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[澧炲己瀹夊叏] 杈圭紭AI楠岃瘉寮傚父锛宼askId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseDTO.error("EDGE_AI_VERIFICATION_ERROR", "杈圭紭AI楠岃瘉寮傚父锛? + e.getMessage());
        }
    }

    /**
     * 浜戣竟鍗忓悓瀹夊叏鍒嗘瀽
     * <p>
     * 澶嶆潅瀹夊叏鍒嗘瀽鍦烘櫙涓嬬殑浜戣竟鍗忓悓澶勭悊
     * 杈圭紭璁惧杩涜鍒濇鍒嗘瀽锛屼簯绔繘琛屾繁搴﹀垎鏋?
     * </p>
     *
     * @param inferenceRequest AI鎺ㄧ悊璇锋眰
     * @return 鍗忓悓鍒嗘瀽缁撴灉
     */
    @Observed(name = "access.security.cloudEdgeCollaboration", contextualName = "cloud-edge-collaborative-analysis")
    @PostMapping("/ai/cloud-edge-collaboration")
    @Operation(
            summary = "浜戣竟鍗忓悓瀹夊叏鍒嗘瀽",
            description = "澶嶆潅鍦烘櫙涓嬬殑浜戣竟鍗忓悓瀹夊叏鍒嗘瀽",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "鍗忓悓鍒嗘瀽瀹屾垚"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "浜戣竟鍗忓悓瀹夊叏鍒嗘瀽")
    public ResponseDTO<InferenceResult> performCloudEdgeCollaborativeAnalysis(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[澧炲己瀹夊叏] 浜戣竟鍗忓悓瀹夊叏鍒嗘瀽锛宼askId={}, taskType={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getTaskType(), inferenceRequest.getDeviceId());

        try {
            // 璁剧疆鍗忓悓鍒嗘瀽鏍囪
            inferenceRequest.setPriority("HIGH");
            inferenceRequest.setTimeout(10000L); // 10绉掕秴鏃?

            // 璋冪敤杈圭紭瀹夊叏鎺у埗鍣ㄦ墽琛屽崗鍚屾帹鐞?
            ResponseEntity<ResponseDTO<Future<InferenceResult>>> response =
                edgeSecurityController.performCollaborativeSecurityInference(inferenceRequest);

            // 鑾峰彇鍗忓悓鍒嗘瀽缁撴灉
            Future<InferenceResult> future = response.getBody().getData();
            InferenceResult result = future.get();

            log.info("[澧炲己瀹夊叏] 浜戣竟鍗忓悓鍒嗘瀽瀹屾垚锛宼askId={}, success={}, confidence={}, collaborationType={}",
                    inferenceRequest.getTaskId(), result.isSuccess(), result.getConfidence(),
                    result.getMetadata() != null ? result.getMetadata().get("collaborationType") : "unknown");

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[澧炲己瀹夊叏] 浜戣竟鍗忓悓鍒嗘瀽寮傚父锛宼askId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseDTO.error("COLLABORATIVE_ANALYSIS_ERROR", "浜戣竟鍗忓悓鍒嗘瀽寮傚父锛? + e.getMessage());
        }
    }

    /**
     * 杈圭紭璁惧鐘舵€佺洃鎺?
     * <p>
     * 鐩戞帶杈圭紭瀹夊叏璁惧鐨勮繍琛岀姸鎬佸拰鎬ц兘鎸囨爣
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧鐘舵€佷俊鎭?
     */
    @Observed(name = "access.security.edgeDeviceStatus", contextualName = "edge-device-status-monitoring")
    @GetMapping("/edge/device/{deviceId}/status")
    @Operation(
            summary = "杈圭紭璁惧鐘舵€佺洃鎺?,
            description = "鐩戞帶杈圭紭瀹夊叏璁惧鐨勮繍琛岀姸鎬佸拰鎬ц兘鎸囨爣",
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "杈圭紭璁惧鐘舵€佺洃鎺?)
    public ResponseDTO<Map<String, Object>> getEdgeDeviceStatus(@PathVariable String deviceId) {
        log.info("[澧炲己瀹夊叏] 鏌ヨ杈圭紭璁惧鐘舵€侊紝deviceId={}", deviceId);

        try {
            // 鑾峰彇杈圭紭璁惧鐘舵€?
            ResponseEntity<ResponseDTO<Map<String, Object>>> response =
                edgeSecurityController.getEdgeDeviceSecurityStatus(deviceId);

            return response.getBody();

        } catch (Exception e) {
            log.error("[澧炲己瀹夊叏] 鏌ヨ杈圭紭璁惧鐘舵€佸紓甯革紝deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseDTO.error("EDGE_DEVICE_STATUS_ERROR", "鏌ヨ杈圭紭璁惧鐘舵€佸紓甯革細" + e.getMessage());
        }
    }

    // ==================== 瀹炴椂瀹夊叏鐩戞帶 ====================

    /**
     * 瀹炴椂瀹夊叏濞佽儊妫€娴?
     * <p>
     * 鍩轰簬澶氱淮搴︽暟鎹繘琛屽疄鏃跺畨鍏ㄥ▉鑳佹娴?
     * 鍖呮嫭寮傚父璁块棶妯″紡銆佽澶囧紓甯哥姸鎬佺瓑
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param areaId 鍖哄煙ID
     * @param userId 鐢ㄦ埛ID锛堝彲閫夛級
     * @return 濞佽儊妫€娴嬬粨鏋?
     */
    @Observed(name = "access.security.threatDetection", contextualName = "real-time-threat-detection")
    @PostMapping("/threat/detection")
    @Operation(
            summary = "瀹炴椂瀹夊叏濞佽儊妫€娴?,
            description = "鍩轰簬澶氱淮搴︽暟鎹繘琛屽疄鏃跺畨鍏ㄥ▉鑳佹娴?,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "妫€娴嬪畬鎴?
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "瀹炴椂瀹夊叏濞佽儊妫€娴?)
    public ResponseDTO<Map<String, Object>> detectRealTimeThreats(
            @RequestParam @Parameter(description = "璁惧ID") Long deviceId,
            @RequestParam @Parameter(description = "鍖哄煙ID") String areaId,
            @RequestParam(required = false) @Parameter(description = "鐢ㄦ埛ID") Long userId) {

        log.info("[澧炲己瀹夊叏] 瀹炴椂濞佽儊妫€娴嬶紝deviceId={}, areaId={}, userId={}",
                deviceId, areaId, userId);

        try {
            Map<String, Object> threatResult = securityEnhancerService
                .detectRealTimeThreats(deviceId, areaId, userId);

            log.info("[澧炲己瀹夊叏] 濞佽儊妫€娴嬪畬鎴愶紝deviceId={}, threatsFound={}, highestRiskLevel={}",
                    deviceId, threatResult.get("threatsFound"), threatResult.get("highestRiskLevel"));

            return ResponseDTO.ok(threatResult);

        } catch (Exception e) {
            log.error("[澧炲己瀹夊叏] 濞佽儊妫€娴嬪紓甯革紝deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseDTO.error("THREAT_DETECTION_ERROR", "濞佽儊妫€娴嬪紓甯革細" + e.getMessage());
        }
    }

    /**
     * 瀹夊叏浜嬩欢鍝嶅簲
     * <p>
     * 瀵规娴嬪埌鐨勫畨鍏ㄤ簨浠惰繘琛岃嚜鍔ㄥ搷搴斿鐞?
     * 鍖呮嫭璁惧閿佸畾銆佸憡璀﹂€氱煡銆佽褰曚笂鎶ョ瓑
     * </p>
     *
     * @param securityEventId 瀹夊叏浜嬩欢ID
     * @param responseType 鍝嶅簲绫诲瀷
     * @return 鍝嶅簲澶勭悊缁撴灉
     */
    @Observed(name = "access.security.eventResponse", contextualName = "security-event-response")
    @PostMapping("/event/{securityEventId}/response")
    @Operation(
            summary = "瀹夊叏浜嬩欢鍝嶅簲",
            description = "瀵规娴嬪埌鐨勫畨鍏ㄤ簨浠惰繘琛岃嚜鍔ㄥ搷搴斿鐞?,
            parameters = {
                    @Parameter(name = "securityEventId", description = "瀹夊叏浜嬩欢ID", required = true),
                    @Parameter(name = "responseType", description = "鍝嶅簲绫诲瀷", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "瀹夊叏浜嬩欢鍝嶅簲")
    public ResponseDTO<Map<String, Object>> respondToSecurityEvent(
            @PathVariable @Parameter(description = "瀹夊叏浜嬩欢ID") String securityEventId,
            @RequestParam @Parameter(description = "鍝嶅簲绫诲瀷") String responseType) {

        log.info("[澧炲己瀹夊叏] 瀹夊叏浜嬩欢鍝嶅簲锛宔ventId={}, responseType={}",
                securityEventId, responseType);

        try {
            Map<String, Object> responseResult = securityEnhancerService
                .respondToSecurityEvent(securityEventId, responseType);

            log.info("[澧炲己瀹夊叏] 瀹夊叏浜嬩欢鍝嶅簲瀹屾垚锛宔ventId={}, responseSuccess={}, actionsTaken={}",
                    securityEventId, responseResult.get("responseSuccess"), responseResult.get("actionsTaken"));

            return ResponseDTO.ok(responseResult);

        } catch (Exception e) {
            log.error("[澧炲己瀹夊叏] 瀹夊叏浜嬩欢鍝嶅簲寮傚父锛宔ventId={}, error={}",
                    securityEventId, e.getMessage(), e);
            return ResponseDTO.error("SECURITY_EVENT_RESPONSE_ERROR", "瀹夊叏浜嬩欢鍝嶅簲寮傚父锛? + e.getMessage());
        }
    }

    // ==================== 绉佹湁杈呭姪鏂规硶 ====================

    /**
     * 鎵ц杈圭紭璁惧娲讳綋妫€娴?
     */
    private CompletableFuture<BiometricAntiSpoofResultVO> performEdgeAntiSpoofing(BiometricDataForm biometricData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 鏋勫缓杈圭紭鎺ㄧ悊璇锋眰
                InferenceRequest request = new InferenceRequest();
                request.setTaskId("ANTI_SPOOF_" + System.currentTimeMillis());
                request.setTaskType("anti_spoofing");
                request.setModelType("liveness_detection");
                request.setDeviceId(biometricData.getDeviceId());
                request.setData(biometricData.getBiometricData());
                request.setPriority("HIGH");
                request.setTimeout(5000L);

                // 璋冪敤杈圭紭璁惧杩涜娲讳綋妫€娴?
                ResponseEntity<ResponseDTO<Future<InferenceResult>>> response =
                    edgeSecurityController.performSecurityInference(request);

                Future<InferenceResult> future = response.getBody().getData();
                InferenceResult inferenceResult = future.get();

                // 杞崲涓烘椿浣撴娴嬬粨鏋?
                BiometricAntiSpoofResultVO result = new BiometricAntiSpoofResultVO();
                result.setUserId(biometricData.getUserId());
                result.setDeviceId(biometricData.getDeviceId());
                result.setLive(inferenceResult.isSuccess() &&
                    (Boolean) inferenceResult.getData());
                result.setConfidence(inferenceResult.getConfidence());
                result.setAntiSpoofMethod("edge_ai_multi_algorithm");
                result.setDetectionTime(inferenceResult.getCostTime());
                result.setRiskLevel(calculateRiskLevel(result.getConfidence()));

                return result;

            } catch (Exception e) {
                log.error("[澧炲己瀹夊叏] 杈圭紭娲讳綋妫€娴嬪紓甯革紝error={}", e.getMessage(), e);

                BiometricAntiSpoofResultVO errorResult = new BiometricAntiSpoofResultVO();
                errorResult.setUserId(biometricData.getUserId());
                errorResult.setDeviceId(biometricData.getDeviceId());
                errorResult.setLive(false);
                errorResult.setConfidence(0.0);
                errorResult.setAntiSpoofMethod("edge_ai_error");
                errorResult.setErrorMessage(e.getMessage());
                errorResult.setRiskLevel("HIGH");

                return errorResult;
            }
        });
    }

    /**
     * 鎵ц澶氭ā鎬佺壒寰佽瀺鍚堥獙璇?
     */
    private CompletableFuture<BiometricAntiSpoofResultVO> performMultiModalFusion(BiometricDataForm biometricData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 璋冪敤瀹夊叏澧炲己鏈嶅姟杩涜澶氭ā鎬佽瀺鍚?
                return securityEnhancerService.performMultiModalFusion(biometricData);

            } catch (Exception e) {
                log.error("[澧炲己瀹夊叏] 澶氭ā鎬佽瀺鍚堝紓甯革紝error={}", e.getMessage(), e);

                BiometricAntiSpoofResultVO errorResult = new BiometricAntiSpoofResultVO();
                errorResult.setUserId(biometricData.getUserId());
                errorResult.setDeviceId(biometricData.getDeviceId());
                errorResult.setLive(false);
                errorResult.setConfidence(0.0);
                errorResult.setAntiSpoofMethod("multi_modal_fusion_error");
                errorResult.setErrorMessage(e.getMessage());
                errorResult.setRiskLevel("HIGH");

                return errorResult;
            }
        });
    }

    /**
     * 璁＄畻椋庨櫓绛夌骇
     */
    private String calculateRiskLevel(Double confidence) {
        if (confidence == null || confidence < 0.3) {
            return "HIGH";
        } else if (confidence < 0.7) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}