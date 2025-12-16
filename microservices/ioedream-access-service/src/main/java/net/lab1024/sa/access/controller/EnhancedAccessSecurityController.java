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
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.access.service.SecurityEnhancerService;
import net.lab1024.sa.access.domain.form.BiometricDataForm;
import net.lab1024.sa.access.domain.vo.BiometricAntiSpoofResultVO;
import net.lab1024.sa.access.domain.vo.TrajectoryAnomalyResultVO;
import net.lab1024.sa.access.edge.controller.EdgeSecurityController;
import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.model.InferenceRequest;
import net.lab1024.sa.video.edge.model.InferenceResult;

/**
 * 增强门禁安全控制器
 * <p>
 * 基于边缘计算架构的门禁安全增强系统，提供：
 * - 多模态生物识别（人脸、指纹、虹膜、声纹）
 * - 实时活体检测与防伪验证
 * - 智能风险识别与异常行为分析
 * - 边缘AI推理与云边协同
 * - 实时监控与告警联动
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@PermissionCheck(value = "ACCESS_MANAGE", description = "门禁安全模块权限")
@RequestMapping("/api/v1/access/security")
@Tag(name = "增强门禁安全", description = "基于边缘计算的门禁安全增强系统")
public class EnhancedAccessSecurityController {

    @Resource
    private SecurityEnhancerService securityEnhancerService;

    @Resource
    private EdgeSecurityController edgeSecurityController;

    // ==================== 多模态生物识别 ====================

    /**
     * 多模态生物识别验证
     * <p>
     * 支持人脸、指纹、虹膜、声纹等多种生物特征融合验证
     * 使用边缘计算实现毫秒级响应
     * </p>
     *
     * @param biometricData 生物识别数据
     * @return 验证结果
     */
    @Observed(name = "access.security.multiModalAuth", contextualName = "multi-modal-biometric-auth")
    @PostMapping("/auth/multi-modal")
    @Operation(
            summary = "多模态生物识别验证",
            description = "融合多种生物特征进行身份验证，提高准确性和安全性",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "验证成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BiometricAntiSpoofResultVO.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "验证失败"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_USE", description = "多模态生物识别验证")
    public ResponseDTO<BiometricAntiSpoofResultVO> performMultiModalAuthentication(
            @Valid @RequestBody BiometricDataForm biometricData) {

        log.info("[增强安全] 多模态生物识别验证，userId={}, device={}, types={}",
                biometricData.getUserId(), biometricData.getDeviceId(), biometricData.getBiometricTypes());

        try {
            // 1. 边缘设备活体检测
            CompletableFuture<BiometricAntiSpoofResultVO> antiSpoofResult =
                performEdgeAntiSpoofing(biometricData);

            // 2. 多模态特征融合验证
            CompletableFuture<BiometricAntiSpoofResultVO> fusionResult =
                performMultiModalFusion(biometricData);

            // 3. 等待结果合并
            BiometricAntiSpoofResultVO antiSpoof = antiSpoofResult.get();
            BiometricAntiSpoofResultVO fusion = fusionResult.get();

            // 4. 综合安全评估
            BiometricAntiSpoofResultVO finalResult = securityEnhancerService
                .performSecurityAssessment(antiSpoof, fusion);

            log.info("[增强安全] 多模态验证完成，userId={}, confidence={}, riskLevel={}",
                    biometricData.getUserId(), finalResult.getConfidence(), finalResult.getRiskLevel());

            return ResponseDTO.ok(finalResult);

        } catch (Exception e) {
            log.error("[增强安全] 多模态验证异常，userId={}, error={}",
                    biometricData.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("MULTI_MODAL_AUTH_ERROR", "多模态验证异常：" + e.getMessage());
        }
    }

    /**
     * 实时活体检测
     * <p>
     * 基于边缘AI进行实时活体检测，防止照片、视频、面具等攻击
     * 支持多种活体检测算法融合
     * </p>
     *
     * @param biometricData 生物识别数据
     * @return 活体检测结果
     */
    @Observed(name = "access.security.antiSpoofing", contextualName = "real-time-anti-spoofing")
    @PostMapping("/auth/anti-spoofing")
    @Operation(
            summary = "实时活体检测",
            description = "基于边缘AI进行实时活体检测，防止各种欺骗攻击",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "检测完成"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_USE", description = "实时活体检测")
    public ResponseDTO<BiometricAntiSpoofResultVO> performRealTimeAntiSpoofing(
            @Valid @RequestBody BiometricDataForm biometricData) {

        log.info("[增强安全] 实时活体检测，userId={}, device={}",
                biometricData.getUserId(), biometricData.getDeviceId());

        try {
            // 边缘设备活体检测
            CompletableFuture<BiometricAntiSpoofResultVO> edgeResult =
                performEdgeAntiSpoofing(biometricData);

            BiometricAntiSpoofResultVO result = edgeResult.get();

            log.info("[增强安全] 活体检测完成，userId={}, isLive={}, confidence={}",
                    biometricData.getUserId(), result.isLive(), result.getConfidence());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强安全] 活体检测异常，userId={}, error={}",
                    biometricData.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("ANTI_SPOOFING_ERROR", "活体检测异常：" + e.getMessage());
        }
    }

    /**
     * 异常行为分析
     * <p>
     * 基于历史访问数据和实时行为模式进行异常检测
     * 识别潜在的威胁和异常访问模式
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param analysisPeriod 分析周期（天）
     * @return 异常行为分析结果
     */
    @Observed(name = "access.security.behaviorAnalysis", contextualName = "abnormal-behavior-analysis")
    @PostMapping("/analysis/abnormal-behavior")
    @Operation(
            summary = "异常行为分析",
            description = "基于历史数据和实时行为模式进行异常检测分析",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "分析完成"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "异常行为分析")
    public ResponseDTO<TrajectoryAnomalyResultVO> analyzeAbnormalBehavior(
            @RequestParam @Parameter(description = "用户ID") Long userId,
            @RequestParam @Parameter(description = "设备ID") Long deviceId,
            @RequestParam(defaultValue = "30") @Parameter(description = "分析周期(天)") Integer analysisPeriod) {

        log.info("[增强安全] 异常行为分析，userId={}, deviceId={}, period={}天",
                userId, deviceId, analysisPeriod);

        try {
            TrajectoryAnomalyResultVO result = securityEnhancerService
                .analyzeAbnormalBehavior(userId, deviceId, analysisPeriod);

            log.info("[增强安全] 异常行为分析完成，userId={}, anomaliesDetected={}, riskScore={}",
                    userId, result.getAnomaliesDetected(), result.getRiskScore());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强安全] 异常行为分析异常，userId={}, error={}",
                    userId, e.getMessage(), e);
            return ResponseDTO.error("BEHAVIOR_ANALYSIS_ERROR", "异常行为分析异常：" + e.getMessage());
        }
    }

    // ==================== 边缘AI安全功能 ====================

    /**
     * 边缘AI安全验证
     * <p>
     * 利用边缘设备的AI能力进行实时的安全验证
     * 包括人脸识别、行为分析、风险评估等
     * </p>
     *
     * @param inferenceRequest AI推理请求
     * @return AI推理结果
     */
    @Observed(name = "access.security.edgeAI", contextualName = "edge-ai-security-verification")
    @PostMapping("/ai/edge-verification")
    @Operation(
            summary = "边缘AI安全验证",
            description = "利用边缘设备AI能力进行实时安全验证",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "验证完成"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_USE", description = "边缘AI安全验证")
    public ResponseDTO<InferenceResult> performEdgeAIVerification(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[增强安全] 边缘AI安全验证，taskId={}, taskType={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getTaskType(), inferenceRequest.getDeviceId());

        try {
            // 调用边缘安全控制器执行AI推理
            ResponseEntity<ResponseDTO<Future<InferenceResult>>> response =
                edgeSecurityController.performSecurityInference(inferenceRequest);

            // 获取推理结果
            Future<InferenceResult> future = response.getBody().getData();
            InferenceResult result = future.get();

            log.info("[增强安全] 边缘AI验证完成，taskId={}, success={}, confidence={}",
                    inferenceRequest.getTaskId(), result.isSuccess(), result.getConfidence());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强安全] 边缘AI验证异常，taskId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseDTO.error("EDGE_AI_VERIFICATION_ERROR", "边缘AI验证异常：" + e.getMessage());
        }
    }

    /**
     * 云边协同安全分析
     * <p>
     * 复杂安全分析场景下的云边协同处理
     * 边缘设备进行初步分析，云端进行深度分析
     * </p>
     *
     * @param inferenceRequest AI推理请求
     * @return 协同分析结果
     */
    @Observed(name = "access.security.cloudEdgeCollaboration", contextualName = "cloud-edge-collaborative-analysis")
    @PostMapping("/ai/cloud-edge-collaboration")
    @Operation(
            summary = "云边协同安全分析",
            description = "复杂场景下的云边协同安全分析",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "协同分析完成"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "云边协同安全分析")
    public ResponseDTO<InferenceResult> performCloudEdgeCollaborativeAnalysis(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[增强安全] 云边协同安全分析，taskId={}, taskType={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getTaskType(), inferenceRequest.getDeviceId());

        try {
            // 设置协同分析标记
            inferenceRequest.setPriority("HIGH");
            inferenceRequest.setTimeout(10000L); // 10秒超时

            // 调用边缘安全控制器执行协同推理
            ResponseEntity<ResponseDTO<Future<InferenceResult>>> response =
                edgeSecurityController.performCollaborativeSecurityInference(inferenceRequest);

            // 获取协同分析结果
            Future<InferenceResult> future = response.getBody().getData();
            InferenceResult result = future.get();

            log.info("[增强安全] 云边协同分析完成，taskId={}, success={}, confidence={}, collaborationType={}",
                    inferenceRequest.getTaskId(), result.isSuccess(), result.getConfidence(),
                    result.getMetadata() != null ? result.getMetadata().get("collaborationType") : "unknown");

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强安全] 云边协同分析异常，taskId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseDTO.error("COLLABORATIVE_ANALYSIS_ERROR", "云边协同分析异常：" + e.getMessage());
        }
    }

    /**
     * 边缘设备状态监控
     * <p>
     * 监控边缘安全设备的运行状态和性能指标
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备状态信息
     */
    @Observed(name = "access.security.edgeDeviceStatus", contextualName = "edge-device-status-monitoring")
    @GetMapping("/edge/device/{deviceId}/status")
    @Operation(
            summary = "边缘设备状态监控",
            description = "监控边缘安全设备的运行状态和性能指标",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "边缘设备状态监控")
    public ResponseDTO<Map<String, Object>> getEdgeDeviceStatus(@PathVariable String deviceId) {
        log.info("[增强安全] 查询边缘设备状态，deviceId={}", deviceId);

        try {
            // 获取边缘设备状态
            ResponseEntity<ResponseDTO<Map<String, Object>>> response =
                edgeSecurityController.getEdgeDeviceSecurityStatus(deviceId);

            return response.getBody();

        } catch (Exception e) {
            log.error("[增强安全] 查询边缘设备状态异常，deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseDTO.error("EDGE_DEVICE_STATUS_ERROR", "查询边缘设备状态异常：" + e.getMessage());
        }
    }

    // ==================== 实时安全监控 ====================

    /**
     * 实时安全威胁检测
     * <p>
     * 基于多维度数据进行实时安全威胁检测
     * 包括异常访问模式、设备异常状态等
     * </p>
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param userId 用户ID（可选）
     * @return 威胁检测结果
     */
    @Observed(name = "access.security.threatDetection", contextualName = "real-time-threat-detection")
    @PostMapping("/threat/detection")
    @Operation(
            summary = "实时安全威胁检测",
            description = "基于多维度数据进行实时安全威胁检测",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "检测完成"
                    )
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "实时安全威胁检测")
    public ResponseDTO<Map<String, Object>> detectRealTimeThreats(
            @RequestParam @Parameter(description = "设备ID") Long deviceId,
            @RequestParam @Parameter(description = "区域ID") String areaId,
            @RequestParam(required = false) @Parameter(description = "用户ID") Long userId) {

        log.info("[增强安全] 实时威胁检测，deviceId={}, areaId={}, userId={}",
                deviceId, areaId, userId);

        try {
            Map<String, Object> threatResult = securityEnhancerService
                .detectRealTimeThreats(deviceId, areaId, userId);

            log.info("[增强安全] 威胁检测完成，deviceId={}, threatsFound={}, highestRiskLevel={}",
                    deviceId, threatResult.get("threatsFound"), threatResult.get("highestRiskLevel"));

            return ResponseDTO.ok(threatResult);

        } catch (Exception e) {
            log.error("[增强安全] 威胁检测异常，deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseDTO.error("THREAT_DETECTION_ERROR", "威胁检测异常：" + e.getMessage());
        }
    }

    /**
     * 安全事件响应
     * <p>
     * 对检测到的安全事件进行自动响应处理
     * 包括设备锁定、告警通知、记录上报等
     * </p>
     *
     * @param securityEventId 安全事件ID
     * @param responseType 响应类型
     * @return 响应处理结果
     */
    @Observed(name = "access.security.eventResponse", contextualName = "security-event-response")
    @PostMapping("/event/{securityEventId}/response")
    @Operation(
            summary = "安全事件响应",
            description = "对检测到的安全事件进行自动响应处理",
            parameters = {
                    @Parameter(name = "securityEventId", description = "安全事件ID", required = true),
                    @Parameter(name = "responseType", description = "响应类型", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGE", description = "安全事件响应")
    public ResponseDTO<Map<String, Object>> respondToSecurityEvent(
            @PathVariable @Parameter(description = "安全事件ID") String securityEventId,
            @RequestParam @Parameter(description = "响应类型") String responseType) {

        log.info("[增强安全] 安全事件响应，eventId={}, responseType={}",
                securityEventId, responseType);

        try {
            Map<String, Object> responseResult = securityEnhancerService
                .respondToSecurityEvent(securityEventId, responseType);

            log.info("[增强安全] 安全事件响应完成，eventId={}, responseSuccess={}, actionsTaken={}",
                    securityEventId, responseResult.get("responseSuccess"), responseResult.get("actionsTaken"));

            return ResponseDTO.ok(responseResult);

        } catch (Exception e) {
            log.error("[增强安全] 安全事件响应异常，eventId={}, error={}",
                    securityEventId, e.getMessage(), e);
            return ResponseDTO.error("SECURITY_EVENT_RESPONSE_ERROR", "安全事件响应异常：" + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 执行边缘设备活体检测
     */
    private CompletableFuture<BiometricAntiSpoofResultVO> performEdgeAntiSpoofing(BiometricDataForm biometricData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 构建边缘推理请求
                InferenceRequest request = new InferenceRequest();
                request.setTaskId("ANTI_SPOOF_" + System.currentTimeMillis());
                request.setTaskType("anti_spoofing");
                request.setModelType("liveness_detection");
                request.setDeviceId(biometricData.getDeviceId());
                request.setData(biometricData.getBiometricData());
                request.setPriority("HIGH");
                request.setTimeout(5000L);

                // 调用边缘设备进行活体检测
                ResponseEntity<ResponseDTO<Future<InferenceResult>>> response =
                    edgeSecurityController.performSecurityInference(request);

                Future<InferenceResult> future = response.getBody().getData();
                InferenceResult inferenceResult = future.get();

                // 转换为活体检测结果
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
                log.error("[增强安全] 边缘活体检测异常，error={}", e.getMessage(), e);

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
     * 执行多模态特征融合验证
     */
    private CompletableFuture<BiometricAntiSpoofResultVO> performMultiModalFusion(BiometricDataForm biometricData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 调用安全增强服务进行多模态融合
                return securityEnhancerService.performMultiModalFusion(biometricData);

            } catch (Exception e) {
                log.error("[增强安全] 多模态融合异常，error={}", e.getMessage(), e);

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
     * 计算风险等级
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