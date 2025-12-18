package net.lab1024.sa.access.edge.controller;

import java.util.Map;
import java.util.concurrent.Future;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.edge.service.EdgeSecurityService;
import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.model.InferenceRequest;
import net.lab1024.sa.video.edge.model.InferenceResult;

/**
 * 杈圭紭瀹夊叏鎺у埗鍣?
 * <p>
 * 涓撻棬澶勭悊杈圭紭璁惧鐨勫畨鍏ˋI鎺ㄧ悊鍔熻兘锛屽寘鎷細
 * - 杈圭紭璁惧瀹夊叏鎺ㄧ悊绠＄悊
 * - 娲讳綋妫€娴嬩笌闃蹭吉楠岃瘉
 * - 浜鸿劯璇嗗埆涓庣壒寰佹瘮瀵?
 * - 琛屼负鍒嗘瀽涓庡紓甯告娴?
 * - 浜戣竟鍗忓悓鎺ㄧ悊璋冨害
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/edge/security")
@Tag(name = "杈圭紭瀹夊叏鎺ㄧ悊", description = "杈圭紭璁惧瀹夊叏AI鎺ㄧ悊绠＄悊")
@PermissionCheck(value = "ACCESS", description = "杈圭紭瀹夊叏绠＄悊")
public class EdgeSecurityController {

    @Resource
    private EdgeSecurityService edgeSecurityService;

    // ==================== 杈圭紭瀹夊叏鎺ㄧ悊鏍稿績鎺ュ彛 ====================

    /**
     * 鎵ц瀹夊叏鎺ㄧ悊
     * <p>
     * 鍦ㄨ竟缂樿澶囦笂鎵ц瀹夊叏鐩稿叧鐨凙I鎺ㄧ悊浠诲姟
     * 鏀寔娲讳綋妫€娴嬨€佷汉鑴歌瘑鍒€佽涓哄垎鏋愮瓑
     * </p>
     *
     * @param inferenceRequest 鎺ㄧ悊璇锋眰
     * @return 鎺ㄧ悊缁撴灉Future
     */
    @Observed(name = "edge.security.inference", contextualName = "edge-security-inference")
    @PostMapping("/inference")
    @Operation(
            summary = "鎵ц瀹夊叏鎺ㄧ悊",
            description = "鍦ㄨ竟缂樿澶囦笂鎵ц瀹夊叏鐩稿叧鐨凙I鎺ㄧ悊浠诲姟",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "鎺ㄧ悊浠诲姟鎻愪氦鎴愬姛",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Future.class)
                            )
                    )
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_USER", description = "杈圭紭瀹夊叏鏌ヨ")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performSecurityInference(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[杈圭紭瀹夊叏] 鎵ц瀹夊叏鎺ㄧ悊锛宼askId={}, taskType={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getTaskType(), inferenceRequest.getDeviceId());

        try {
            Future<InferenceResult> result = edgeSecurityService.performSecurityInference(inferenceRequest);

            log.info("[杈圭紭瀹夊叏] 瀹夊叏鎺ㄧ悊浠诲姟鎻愪氦鎴愬姛锛宼askId={}, deviceId={}",
                    inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 瀹夊叏鎺ㄧ悊浠诲姟鎻愪氦寮傚父锛宼askId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("SECURITY_INFERENCE_ERROR", "瀹夊叏鎺ㄧ悊浠诲姟鎻愪氦寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 鎵ц鍗忓悓瀹夊叏鎺ㄧ悊
     * <p>
     * 瀵逛簬澶嶆潅鐨勫畨鍏ㄦ帹鐞嗕换鍔★紝閲囩敤浜戣竟鍗忓悓妯″紡
     * 杈圭紭璁惧浼樺厛锛屼簯绔緟鍔╋紝缁撴灉铻嶅悎
     * </p>
     *
     * @param inferenceRequest 鎺ㄧ悊璇锋眰
     * @return 鍗忓悓鎺ㄧ悊缁撴灉Future
     */
    @Observed(name = "edge.security.collaborativeInference", contextualName = "edge-security-collaborative-inference")
    @PostMapping("/inference/collaborative")
    @Operation(
            summary = "鎵ц鍗忓悓瀹夊叏鎺ㄧ悊",
            description = "澶嶆潅鍦烘櫙涓嬬殑浜戣竟鍗忓悓瀹夊叏鎺ㄧ悊",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "鍗忓悓鎺ㄧ悊浠诲姟鎻愪氦鎴愬姛"
                    )
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "杈圭紭瀹夊叏绠＄悊")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performCollaborativeSecurityInference(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[杈圭紭瀹夊叏] 鎵ц鍗忓悓瀹夊叏鎺ㄧ悊锛宼askId={}, taskType={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getTaskType(), inferenceRequest.getDeviceId());

        try {
            Future<InferenceResult> result = edgeSecurityService.performCollaborativeSecurityInference(inferenceRequest);

            log.info("[杈圭紭瀹夊叏] 鍗忓悓瀹夊叏鎺ㄧ悊浠诲姟鎻愪氦鎴愬姛锛宼askId={}, deviceId={}",
                    inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 鍗忓悓瀹夊叏鎺ㄧ悊浠诲姟鎻愪氦寮傚父锛宼askId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("COLLABORATIVE_INFERENCE_ERROR", "鍗忓悓鎺ㄧ悊浠诲姟鎻愪氦寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 鎵归噺瀹夊叏鎺ㄧ悊
     * <p>
     * 鎵归噺澶勭悊澶氫釜瀹夊叏鎺ㄧ悊浠诲姟锛屾彁楂樿竟缂樿澶囧埄鐢ㄧ巼
     * </p>
     *
     * @param inferenceRequests 鎺ㄧ悊璇锋眰鍒楄〃
     * @return 鎵归噺鎺ㄧ悊缁撴灉Map
     */
    @Observed(name = "edge.security.batchInference", contextualName = "edge-security-batch-inference")
    @PostMapping("/inference/batch")
    @Operation(
            summary = "鎵归噺瀹夊叏鎺ㄧ悊",
            description = "鎵归噺澶勭悊澶氫釜瀹夊叏鎺ㄧ悊浠诲姟",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "鎵归噺鎺ㄧ悊浠诲姟鎻愪氦鎴愬姛"
                    )
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "杈圭紭瀹夊叏绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Future<InferenceResult>>>> performBatchSecurityInference(
            @Valid @RequestBody Map<String, InferenceRequest> inferenceRequests) {

        log.info("[杈圭紭瀹夊叏] 鎵ц鎵归噺瀹夊叏鎺ㄧ悊锛屼换鍔℃暟閲?{}", inferenceRequests.size());

        try {
            Map<String, Future<InferenceResult>> results = edgeSecurityService.performBatchSecurityInference(inferenceRequests);

            log.info("[杈圭紭瀹夊叏] 鎵归噺瀹夊叏鎺ㄧ悊浠诲姟鎻愪氦鎴愬姛锛屼换鍔℃暟閲?{}", results.size());

            return ResponseEntity.ok(ResponseDTO.ok(results));

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 鎵归噺瀹夊叏鎺ㄧ悊浠诲姟鎻愪氦寮傚父锛宔rror={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("BATCH_INFERENCE_ERROR", "鎵归噺鎺ㄧ悊浠诲姟鎻愪氦寮傚父锛? + e.getMessage()));
        }
    }

    // ==================== 杈圭紭璁惧绠＄悊 ====================

    /**
     * 娉ㄥ唽杈圭紭瀹夊叏璁惧
     * <p>
     * 娉ㄥ唽鏂扮殑杈圭紭瀹夊叏璁惧鍒扮郴缁熶腑
     * 寤虹珛杩炴帴骞跺悓姝ュ畨鍏ˋI妯″瀷
     * </p>
     *
     * @param edgeDevice 杈圭紭璁惧淇℃伅
     * @return 娉ㄥ唽缁撴灉
     */
    @Observed(name = "edge.security.registerDevice", contextualName = "edge-security-register-device")
    @PostMapping("/device/register")
    @Operation(
            summary = "娉ㄥ唽杈圭紭瀹夊叏璁惧",
            description = "娉ㄥ唽鏂扮殑杈圭紭瀹夊叏璁惧鍒扮郴缁熶腑"
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "杈圭紭瀹夊叏绠＄悊")
    public ResponseEntity<ResponseDTO<String>> registerEdgeSecurityDevice(
            @Valid @RequestBody EdgeDevice edgeDevice) {

        log.info("[杈圭紭瀹夊叏] 娉ㄥ唽杈圭紭瀹夊叏璁惧锛宒eviceId={}, deviceType={}, location={}",
                edgeDevice.getDeviceId(), edgeDevice.getDeviceType(), edgeDevice.getLocation());

        try {
            boolean success = edgeSecurityService.registerEdgeSecurityDevice(edgeDevice);

            if (success) {
                log.info("[杈圭紭瀹夊叏] 杈圭紭瀹夊叏璁惧娉ㄥ唽鎴愬姛锛宒eviceId={}", edgeDevice.getDeviceId());
                return ResponseEntity.ok(ResponseDTO.ok("杈圭紭瀹夊叏璁惧娉ㄥ唽鎴愬姛"));
            } else {
                log.warn("[杈圭紭瀹夊叏] 杈圭紭瀹夊叏璁惧娉ㄥ唽澶辫触锛宒eviceId={}", edgeDevice.getDeviceId());
                return ResponseEntity.ok(ResponseDTO.error("DEVICE_REGISTER_FAILED", "杈圭紭瀹夊叏璁惧娉ㄥ唽澶辫触"));
            }

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 杈圭紭瀹夊叏璁惧娉ㄥ唽寮傚父锛宒eviceId={}, error={}",
                    edgeDevice.getDeviceId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("DEVICE_REGISTER_ERROR", "杈圭紭瀹夊叏璁惧娉ㄥ唽寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 鑾峰彇杈圭紭璁惧瀹夊叏鐘舵€?
     * <p>
     * 鏌ヨ杈圭紭瀹夊叏璁惧鐨勮繍琛岀姸鎬佸拰鎬ц兘鎸囨爣
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧鐘舵€佷俊鎭?
     */
    @Observed(name = "edge.security.deviceStatus", contextualName = "edge-security-device-status")
    @GetMapping("/device/{deviceId}/status")
    @Operation(
            summary = "鑾峰彇杈圭紭璁惧瀹夊叏鐘舵€?,
            description = "鏌ヨ杈圭紭瀹夊叏璁惧鐨勮繍琛岀姸鎬佸拰鎬ц兘鎸囨爣",
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true)
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "杈圭紭瀹夊叏绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getEdgeDeviceSecurityStatus(@PathVariable String deviceId) {
        log.info("[杈圭紭瀹夊叏] 鏌ヨ杈圭紭璁惧瀹夊叏鐘舵€侊紝deviceId={}", deviceId);

        try {
            Map<String, Object> status = edgeSecurityService.getEdgeDeviceSecurityStatus(deviceId);

            log.info("[杈圭紭瀹夊叏] 杈圭紭璁惧瀹夊叏鐘舵€佹煡璇㈡垚鍔燂紝deviceId={}, status={}",
                    deviceId, status.get("status"));

            return ResponseEntity.ok(ResponseDTO.ok(status));

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 鏌ヨ杈圭紭璁惧瀹夊叏鐘舵€佸紓甯革紝deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("DEVICE_STATUS_ERROR", "鏌ヨ璁惧鐘舵€佸紓甯革細" + e.getMessage()));
        }
    }

    /**
     * 鏇存柊杈圭紭璁惧瀹夊叏妯″瀷
     * <p>
     * 鍔ㄦ€佹洿鏂拌竟缂樿澶囦笂鐨勫畨鍏ˋI妯″瀷
     * 鏀寔鐑洿鏂帮紝涓嶅奖鍝嶈澶囨甯歌繍琛?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param modelType 妯″瀷绫诲瀷
     * @param modelData 妯″瀷鏁版嵁锛圔ase64缂栫爜锛?
     * @return 鏇存柊缁撴灉
     */
    @Observed(name = "edge.security.updateModel", contextualName = "edge-security-update-model")
    @PostMapping("/device/{deviceId}/model/{modelType}")
    @Operation(
            summary = "鏇存柊杈圭紭璁惧瀹夊叏妯″瀷",
            description = "鍔ㄦ€佹洿鏂拌竟缂樿澶囦笂鐨勫畨鍏ˋI妯″瀷",
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true),
                    @Parameter(name = "modelType", description = "妯″瀷绫诲瀷", required = true)
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "杈圭紭瀹夊叏绠＄悊")
    public ResponseEntity<ResponseDTO<String>> updateEdgeSecurityModel(
            @PathVariable String deviceId,
            @PathVariable String modelType,
            @RequestParam String modelData) {

        log.info("[杈圭紭瀹夊叏] 鏇存柊杈圭紭璁惧瀹夊叏妯″瀷锛宒eviceId={}, modelType={}, modelSize={}KB",
                deviceId, modelType, modelData.length() / 1024);

        try {
            // 瑙ｇ爜Base64妯″瀷鏁版嵁
            byte[] modelBytes = java.util.Base64.getDecoder().decode(modelData);
            boolean success = edgeSecurityService.updateEdgeSecurityModel(deviceId, modelType, modelBytes);

            if (success) {
                log.info("[杈圭紭瀹夊叏] 杈圭紭瀹夊叏妯″瀷鏇存柊鎴愬姛锛宒eviceId={}, modelType={}", deviceId, modelType);
                return ResponseEntity.ok(ResponseDTO.ok("杈圭紭瀹夊叏妯″瀷鏇存柊鎴愬姛"));
            } else {
                log.warn("[杈圭紭瀹夊叏] 杈圭紭瀹夊叏妯″瀷鏇存柊澶辫触锛宒eviceId={}, modelType={}", deviceId, modelType);
                return ResponseEntity.ok(ResponseDTO.error("MODEL_UPDATE_FAILED", "杈圭紭瀹夊叏妯″瀷鏇存柊澶辫触"));
            }

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 杈圭紭瀹夊叏妯″瀷鏇存柊寮傚父锛宒eviceId={}, modelType={}, error={}",
                    deviceId, modelType, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("MODEL_UPDATE_ERROR", "杈圭紭瀹夊叏妯″瀷鏇存柊寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 鑾峰彇杈圭紭瀹夊叏缁熻淇℃伅
     * <p>
     * 鑾峰彇杈圭紭瀹夊叏绯荤粺鐨勮繍琛岀粺璁′俊鎭?
     * 鍖呮嫭璁惧鏁伴噺銆佹帹鐞嗘垚鍔熺巼銆佸钩鍧囧搷搴旀椂闂寸瓑
     * </p>
     *
     * @return 缁熻淇℃伅
     */
    @Observed(name = "edge.security.statistics", contextualName = "edge-security-statistics")
    @GetMapping("/statistics")
    @Operation(
            summary = "鑾峰彇杈圭紭瀹夊叏缁熻淇℃伅",
            description = "鑾峰彇杈圭紭瀹夊叏绯荤粺鐨勮繍琛岀粺璁′俊鎭?
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "杈圭紭瀹夊叏绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getEdgeSecurityStatistics() {
        log.info("[杈圭紭瀹夊叏] 鑾峰彇杈圭紭瀹夊叏缁熻淇℃伅");

        try {
            Map<String, Object> statistics = edgeSecurityService.getEdgeSecurityStatistics();

            log.info("[杈圭紭瀹夊叏] 杈圭紭瀹夊叏缁熻淇℃伅鑾峰彇鎴愬姛锛宼otalDevices={}, totalInferences={}, averageInferenceTime={}ms",
                    statistics.get("totalDevices"), statistics.get("totalInferences"), statistics.get("averageInferenceTime"));

            return ResponseEntity.ok(ResponseDTO.ok(statistics));

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 鑾峰彇杈圭紭瀹夊叏缁熻淇℃伅寮傚父锛宔rror={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("STATISTICS_ERROR", "鑾峰彇缁熻淇℃伅寮傚父锛? + e.getMessage()));
        }
    }

    // ==================== 杈圭紭瀹夊叏鍔熻兘鎺ュ彛 ====================

    /**
     * 瀹炴椂娲讳綋妫€娴?
     * <p>
     * 涓撶敤鐨勮竟缂樻椿浣撴娴嬫帴鍙?
     * 鏀寔澶氱娲讳綋妫€娴嬬畻娉曡瀺鍚?
     * </p>
     *
     * @param inferenceRequest 鎺ㄧ悊璇锋眰
     * @return 娲讳綋妫€娴嬬粨鏋?
     */
    @Observed(name = "edge.security.livenessDetection", contextualName = "edge-security-liveness-detection")
    @PostMapping("/liveness/detection")
    @Operation(
            summary = "瀹炴椂娲讳綋妫€娴?,
            description = "涓撶敤鐨勮竟缂樻椿浣撴娴嬫帴鍙?
    )
    @PermissionCheck(value = "EDGE_SECURITY_USER", description = "杈圭紭瀹夊叏鏌ヨ")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performLivenessDetection(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[杈圭紭瀹夊叏] 鎵ц娲讳綋妫€娴嬶紝taskId={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

        try {
            // 璁剧疆娲讳綋妫€娴嬪弬鏁?
            inferenceRequest.setTaskType("liveness_detection");
            inferenceRequest.setModelType("multi_algorithm_liveness");
            inferenceRequest.setPriority("HIGH");

            Future<InferenceResult> result = edgeSecurityService.performSecurityInference(inferenceRequest);

            log.info("[杈圭紭瀹夊叏] 娲讳綋妫€娴嬩换鍔℃彁浜ゆ垚鍔燂紝taskId={}", inferenceRequest.getTaskId());
            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 娲讳綋妫€娴嬩换鍔℃彁浜ゅ紓甯革紝taskId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("LIVENESS_DETECTION_ERROR", "娲讳綋妫€娴嬩换鍔℃彁浜ゅ紓甯革細" + e.getMessage()));
        }
    }

    /**
     * 浜鸿劯璇嗗埆楠岃瘉
     * <p>
     * 涓撶敤鐨勮竟缂樹汉鑴歌瘑鍒獙璇佹帴鍙?
     * 鏀寔1:1鍜?:N璇嗗埆妯″紡
     * </p>
     *
     * @param inferenceRequest 鎺ㄧ悊璇锋眰
     * @return 璇嗗埆缁撴灉
     */
    @Observed(name = "edge.security.faceRecognition", contextualName = "edge-security-face-recognition")
    @PostMapping("/face/recognition")
    @Operation(
            summary = "浜鸿劯璇嗗埆楠岃瘉",
            description = "涓撶敤鐨勮竟缂樹汉鑴歌瘑鍒獙璇佹帴鍙?
    )
    @PermissionCheck(value = "EDGE_SECURITY_USER", description = "杈圭紭瀹夊叏鏌ヨ")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performFaceRecognition(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[杈圭紭瀹夊叏] 鎵ц浜鸿劯璇嗗埆锛宼askId={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

        try {
            // 璁剧疆浜鸿劯璇嗗埆鍙傛暟
            inferenceRequest.setTaskType("face_recognition");
            inferenceRequest.setModelType("deep_face_recognition");
            inferenceRequest.setPriority("NORMAL");

            Future<InferenceResult> result = edgeSecurityService.performSecurityInference(inferenceRequest);

            log.info("[杈圭紭瀹夊叏] 浜鸿劯璇嗗埆浠诲姟鎻愪氦鎴愬姛锛宼askId={}", inferenceRequest.getTaskId());
            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 浜鸿劯璇嗗埆浠诲姟鎻愪氦寮傚父锛宼askId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("FACE_RECOGNITION_ERROR", "浜鸿劯璇嗗埆浠诲姟鎻愪氦寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 琛屼负寮傚父鍒嗘瀽
     * <p>
     * 涓撶敤鐨勮竟缂樿涓哄紓甯稿垎鏋愭帴鍙?
     * 瀹炴椂妫€娴嬪紓甯歌涓烘ā寮?
     * </p>
     *
     * @param inferenceRequest 鎺ㄧ悊璇锋眰
     * @return 鍒嗘瀽缁撴灉
     */
    @Observed(name = "edge.security.behaviorAnalysis", contextualName = "edge-security-behavior-analysis")
    @PostMapping("/behavior/analysis")
    @Operation(
            summary = "琛屼负寮傚父鍒嗘瀽",
            description = "涓撶敤鐨勮竟缂樿涓哄紓甯稿垎鏋愭帴鍙?
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "杈圭紭瀹夊叏绠＄悊")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performBehaviorAnalysis(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[杈圭紭瀹夊叏] 鎵ц琛屼负寮傚父鍒嗘瀽锛宼askId={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

        try {
            // 璁剧疆琛屼负鍒嗘瀽鍙傛暟
            inferenceRequest.setTaskType("behavior_analysis");
            inferenceRequest.setModelType("anomaly_behavior_detection");
            inferenceRequest.setPriority("MEDIUM");

            Future<InferenceResult> result = edgeSecurityService.performSecurityInference(inferenceRequest);

            log.info("[杈圭紭瀹夊叏] 琛屼负寮傚父鍒嗘瀽浠诲姟鎻愪氦鎴愬姛锛宼askId={}", inferenceRequest.getTaskId());
            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[杈圭紭瀹夊叏] 琛屼负寮傚父鍒嗘瀽浠诲姟鎻愪氦寮傚父锛宼askId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("BEHAVIOR_ANALYSIS_ERROR", "琛屼负寮傚父鍒嗘瀽浠诲姟鎻愪氦寮傚父锛? + e.getMessage()));
        }
    }
}