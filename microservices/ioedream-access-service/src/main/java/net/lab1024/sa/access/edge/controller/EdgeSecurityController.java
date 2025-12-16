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
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.access.edge.service.EdgeSecurityService;
import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.model.InferenceRequest;
import net.lab1024.sa.video.edge.model.InferenceResult;

/**
 * 边缘安全控制器
 * <p>
 * 专门处理边缘设备的安全AI推理功能，包括：
 * - 边缘设备安全推理管理
 * - 活体检测与防伪验证
 * - 人脸识别与特征比对
 * - 行为分析与异常检测
 * - 云边协同推理调度
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/edge/security")
@Tag(name = "边缘安全推理", description = "边缘设备安全AI推理管理")
@PermissionCheck(value = "ACCESS", description = "边缘安全管理")
public class EdgeSecurityController {

    @Resource
    private EdgeSecurityService edgeSecurityService;

    // ==================== 边缘安全推理核心接口 ====================

    /**
     * 执行安全推理
     * <p>
     * 在边缘设备上执行安全相关的AI推理任务
     * 支持活体检测、人脸识别、行为分析等
     * </p>
     *
     * @param inferenceRequest 推理请求
     * @return 推理结果Future
     */
    @Observed(name = "edge.security.inference", contextualName = "edge-security-inference")
    @PostMapping("/inference")
    @Operation(
            summary = "执行安全推理",
            description = "在边缘设备上执行安全相关的AI推理任务",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "推理任务提交成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Future.class)
                            )
                    )
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_USER", description = "边缘安全查询")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performSecurityInference(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[边缘安全] 执行安全推理，taskId={}, taskType={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getTaskType(), inferenceRequest.getDeviceId());

        try {
            Future<InferenceResult> result = edgeSecurityService.performSecurityInference(inferenceRequest);

            log.info("[边缘安全] 安全推理任务提交成功，taskId={}, deviceId={}",
                    inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[边缘安全] 安全推理任务提交异常，taskId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("SECURITY_INFERENCE_ERROR", "安全推理任务提交异常：" + e.getMessage()));
        }
    }

    /**
     * 执行协同安全推理
     * <p>
     * 对于复杂的安全推理任务，采用云边协同模式
     * 边缘设备优先，云端辅助，结果融合
     * </p>
     *
     * @param inferenceRequest 推理请求
     * @return 协同推理结果Future
     */
    @Observed(name = "edge.security.collaborativeInference", contextualName = "edge-security-collaborative-inference")
    @PostMapping("/inference/collaborative")
    @Operation(
            summary = "执行协同安全推理",
            description = "复杂场景下的云边协同安全推理",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "协同推理任务提交成功"
                    )
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "边缘安全管理")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performCollaborativeSecurityInference(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[边缘安全] 执行协同安全推理，taskId={}, taskType={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getTaskType(), inferenceRequest.getDeviceId());

        try {
            Future<InferenceResult> result = edgeSecurityService.performCollaborativeSecurityInference(inferenceRequest);

            log.info("[边缘安全] 协同安全推理任务提交成功，taskId={}, deviceId={}",
                    inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[边缘安全] 协同安全推理任务提交异常，taskId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("COLLABORATIVE_INFERENCE_ERROR", "协同推理任务提交异常：" + e.getMessage()));
        }
    }

    /**
     * 批量安全推理
     * <p>
     * 批量处理多个安全推理任务，提高边缘设备利用率
     * </p>
     *
     * @param inferenceRequests 推理请求列表
     * @return 批量推理结果Map
     */
    @Observed(name = "edge.security.batchInference", contextualName = "edge-security-batch-inference")
    @PostMapping("/inference/batch")
    @Operation(
            summary = "批量安全推理",
            description = "批量处理多个安全推理任务",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "批量推理任务提交成功"
                    )
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "边缘安全管理")
    public ResponseEntity<ResponseDTO<Map<String, Future<InferenceResult>>>> performBatchSecurityInference(
            @Valid @RequestBody Map<String, InferenceRequest> inferenceRequests) {

        log.info("[边缘安全] 执行批量安全推理，任务数量={}", inferenceRequests.size());

        try {
            Map<String, Future<InferenceResult>> results = edgeSecurityService.performBatchSecurityInference(inferenceRequests);

            log.info("[边缘安全] 批量安全推理任务提交成功，任务数量={}", results.size());

            return ResponseEntity.ok(ResponseDTO.ok(results));

        } catch (Exception e) {
            log.error("[边缘安全] 批量安全推理任务提交异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("BATCH_INFERENCE_ERROR", "批量推理任务提交异常：" + e.getMessage()));
        }
    }

    // ==================== 边缘设备管理 ====================

    /**
     * 注册边缘安全设备
     * <p>
     * 注册新的边缘安全设备到系统中
     * 建立连接并同步安全AI模型
     * </p>
     *
     * @param edgeDevice 边缘设备信息
     * @return 注册结果
     */
    @Observed(name = "edge.security.registerDevice", contextualName = "edge-security-register-device")
    @PostMapping("/device/register")
    @Operation(
            summary = "注册边缘安全设备",
            description = "注册新的边缘安全设备到系统中"
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "边缘安全管理")
    public ResponseEntity<ResponseDTO<String>> registerEdgeSecurityDevice(
            @Valid @RequestBody EdgeDevice edgeDevice) {

        log.info("[边缘安全] 注册边缘安全设备，deviceId={}, deviceType={}, location={}",
                edgeDevice.getDeviceId(), edgeDevice.getDeviceType(), edgeDevice.getLocation());

        try {
            boolean success = edgeSecurityService.registerEdgeSecurityDevice(edgeDevice);

            if (success) {
                log.info("[边缘安全] 边缘安全设备注册成功，deviceId={}", edgeDevice.getDeviceId());
                return ResponseEntity.ok(ResponseDTO.ok("边缘安全设备注册成功"));
            } else {
                log.warn("[边缘安全] 边缘安全设备注册失败，deviceId={}", edgeDevice.getDeviceId());
                return ResponseEntity.ok(ResponseDTO.error("DEVICE_REGISTER_FAILED", "边缘安全设备注册失败"));
            }

        } catch (Exception e) {
            log.error("[边缘安全] 边缘安全设备注册异常，deviceId={}, error={}",
                    edgeDevice.getDeviceId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("DEVICE_REGISTER_ERROR", "边缘安全设备注册异常：" + e.getMessage()));
        }
    }

    /**
     * 获取边缘设备安全状态
     * <p>
     * 查询边缘安全设备的运行状态和性能指标
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备状态信息
     */
    @Observed(name = "edge.security.deviceStatus", contextualName = "edge-security-device-status")
    @GetMapping("/device/{deviceId}/status")
    @Operation(
            summary = "获取边缘设备安全状态",
            description = "查询边缘安全设备的运行状态和性能指标",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "边缘安全管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getEdgeDeviceSecurityStatus(@PathVariable String deviceId) {
        log.info("[边缘安全] 查询边缘设备安全状态，deviceId={}", deviceId);

        try {
            Map<String, Object> status = edgeSecurityService.getEdgeDeviceSecurityStatus(deviceId);

            log.info("[边缘安全] 边缘设备安全状态查询成功，deviceId={}, status={}",
                    deviceId, status.get("status"));

            return ResponseEntity.ok(ResponseDTO.ok(status));

        } catch (Exception e) {
            log.error("[边缘安全] 查询边缘设备安全状态异常，deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("DEVICE_STATUS_ERROR", "查询设备状态异常：" + e.getMessage()));
        }
    }

    /**
     * 更新边缘设备安全模型
     * <p>
     * 动态更新边缘设备上的安全AI模型
     * 支持热更新，不影响设备正常运行
     * </p>
     *
     * @param deviceId 设备ID
     * @param modelType 模型类型
     * @param modelData 模型数据（Base64编码）
     * @return 更新结果
     */
    @Observed(name = "edge.security.updateModel", contextualName = "edge-security-update-model")
    @PostMapping("/device/{deviceId}/model/{modelType}")
    @Operation(
            summary = "更新边缘设备安全模型",
            description = "动态更新边缘设备上的安全AI模型",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true),
                    @Parameter(name = "modelType", description = "模型类型", required = true)
            }
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "边缘安全管理")
    public ResponseEntity<ResponseDTO<String>> updateEdgeSecurityModel(
            @PathVariable String deviceId,
            @PathVariable String modelType,
            @RequestParam String modelData) {

        log.info("[边缘安全] 更新边缘设备安全模型，deviceId={}, modelType={}, modelSize={}KB",
                deviceId, modelType, modelData.length() / 1024);

        try {
            // 解码Base64模型数据
            byte[] modelBytes = java.util.Base64.getDecoder().decode(modelData);
            boolean success = edgeSecurityService.updateEdgeSecurityModel(deviceId, modelType, modelBytes);

            if (success) {
                log.info("[边缘安全] 边缘安全模型更新成功，deviceId={}, modelType={}", deviceId, modelType);
                return ResponseEntity.ok(ResponseDTO.ok("边缘安全模型更新成功"));
            } else {
                log.warn("[边缘安全] 边缘安全模型更新失败，deviceId={}, modelType={}", deviceId, modelType);
                return ResponseEntity.ok(ResponseDTO.error("MODEL_UPDATE_FAILED", "边缘安全模型更新失败"));
            }

        } catch (Exception e) {
            log.error("[边缘安全] 边缘安全模型更新异常，deviceId={}, modelType={}, error={}",
                    deviceId, modelType, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("MODEL_UPDATE_ERROR", "边缘安全模型更新异常：" + e.getMessage()));
        }
    }

    /**
     * 获取边缘安全统计信息
     * <p>
     * 获取边缘安全系统的运行统计信息
     * 包括设备数量、推理成功率、平均响应时间等
     * </p>
     *
     * @return 统计信息
     */
    @Observed(name = "edge.security.statistics", contextualName = "edge-security-statistics")
    @GetMapping("/statistics")
    @Operation(
            summary = "获取边缘安全统计信息",
            description = "获取边缘安全系统的运行统计信息"
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "边缘安全管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getEdgeSecurityStatistics() {
        log.info("[边缘安全] 获取边缘安全统计信息");

        try {
            Map<String, Object> statistics = edgeSecurityService.getEdgeSecurityStatistics();

            log.info("[边缘安全] 边缘安全统计信息获取成功，totalDevices={}, totalInferences={}, averageInferenceTime={}ms",
                    statistics.get("totalDevices"), statistics.get("totalInferences"), statistics.get("averageInferenceTime"));

            return ResponseEntity.ok(ResponseDTO.ok(statistics));

        } catch (Exception e) {
            log.error("[边缘安全] 获取边缘安全统计信息异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("STATISTICS_ERROR", "获取统计信息异常：" + e.getMessage()));
        }
    }

    // ==================== 边缘安全功能接口 ====================

    /**
     * 实时活体检测
     * <p>
     * 专用的边缘活体检测接口
     * 支持多种活体检测算法融合
     * </p>
     *
     * @param inferenceRequest 推理请求
     * @return 活体检测结果
     */
    @Observed(name = "edge.security.livenessDetection", contextualName = "edge-security-liveness-detection")
    @PostMapping("/liveness/detection")
    @Operation(
            summary = "实时活体检测",
            description = "专用的边缘活体检测接口"
    )
    @PermissionCheck(value = "EDGE_SECURITY_USER", description = "边缘安全查询")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performLivenessDetection(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[边缘安全] 执行活体检测，taskId={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

        try {
            // 设置活体检测参数
            inferenceRequest.setTaskType("liveness_detection");
            inferenceRequest.setModelType("multi_algorithm_liveness");
            inferenceRequest.setPriority("HIGH");

            Future<InferenceResult> result = edgeSecurityService.performSecurityInference(inferenceRequest);

            log.info("[边缘安全] 活体检测任务提交成功，taskId={}", inferenceRequest.getTaskId());
            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[边缘安全] 活体检测任务提交异常，taskId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("LIVENESS_DETECTION_ERROR", "活体检测任务提交异常：" + e.getMessage()));
        }
    }

    /**
     * 人脸识别验证
     * <p>
     * 专用的边缘人脸识别验证接口
     * 支持1:1和1:N识别模式
     * </p>
     *
     * @param inferenceRequest 推理请求
     * @return 识别结果
     */
    @Observed(name = "edge.security.faceRecognition", contextualName = "edge-security-face-recognition")
    @PostMapping("/face/recognition")
    @Operation(
            summary = "人脸识别验证",
            description = "专用的边缘人脸识别验证接口"
    )
    @PermissionCheck(value = "EDGE_SECURITY_USER", description = "边缘安全查询")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performFaceRecognition(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[边缘安全] 执行人脸识别，taskId={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

        try {
            // 设置人脸识别参数
            inferenceRequest.setTaskType("face_recognition");
            inferenceRequest.setModelType("deep_face_recognition");
            inferenceRequest.setPriority("NORMAL");

            Future<InferenceResult> result = edgeSecurityService.performSecurityInference(inferenceRequest);

            log.info("[边缘安全] 人脸识别任务提交成功，taskId={}", inferenceRequest.getTaskId());
            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[边缘安全] 人脸识别任务提交异常，taskId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("FACE_RECOGNITION_ERROR", "人脸识别任务提交异常：" + e.getMessage()));
        }
    }

    /**
     * 行为异常分析
     * <p>
     * 专用的边缘行为异常分析接口
     * 实时检测异常行为模式
     * </p>
     *
     * @param inferenceRequest 推理请求
     * @return 分析结果
     */
    @Observed(name = "edge.security.behaviorAnalysis", contextualName = "edge-security-behavior-analysis")
    @PostMapping("/behavior/analysis")
    @Operation(
            summary = "行为异常分析",
            description = "专用的边缘行为异常分析接口"
    )
    @PermissionCheck(value = "EDGE_SECURITY_MANAGER", description = "边缘安全管理")
    public ResponseEntity<ResponseDTO<Future<InferenceResult>>> performBehaviorAnalysis(
            @Valid @RequestBody InferenceRequest inferenceRequest) {

        log.info("[边缘安全] 执行行为异常分析，taskId={}, deviceId={}",
                inferenceRequest.getTaskId(), inferenceRequest.getDeviceId());

        try {
            // 设置行为分析参数
            inferenceRequest.setTaskType("behavior_analysis");
            inferenceRequest.setModelType("anomaly_behavior_detection");
            inferenceRequest.setPriority("MEDIUM");

            Future<InferenceResult> result = edgeSecurityService.performSecurityInference(inferenceRequest);

            log.info("[边缘安全] 行为异常分析任务提交成功，taskId={}", inferenceRequest.getTaskId());
            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[边缘安全] 行为异常分析任务提交异常，taskId={}, error={}",
                    inferenceRequest.getTaskId(), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("BEHAVIOR_ANALYSIS_ERROR", "行为异常分析任务提交异常：" + e.getMessage()));
        }
    }
}