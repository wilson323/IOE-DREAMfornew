package net.lab1024.sa.video.edge.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.edge.EdgeVideoProcessor;
import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.model.InferenceRequest;
import net.lab1024.sa.video.edge.model.InferenceResult;
import net.lab1024.sa.video.edge.form.EdgeDeviceRegisterForm;
import net.lab1024.sa.video.edge.form.InferenceBatchForm;
import net.lab1024.sa.video.edge.form.InferenceForm;
import net.lab1024.sa.video.edge.vo.EdgeDeviceStatusVO;
import net.lab1024.sa.video.edge.vo.EdgeStatisticsVO;

/**
 * 边缘视频控制器
 * <p>
 * 提供边缘计算相关的API接口
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@PermissionCheck(value = "VIDEO_MANAGE", description = "边缘视频管理权限")
@RequestMapping("/api/v1/edge/video")
@Tag(name = "边缘视频管理", description = "边缘设备注册、AI推理、模型管理、云边协同等API")
public class EdgeVideoController {

    @Resource
    private EdgeVideoProcessor edgeVideoProcessor;

    /**
     * 注册边缘设备
     * <p>
     * 将边缘设备注册到边缘计算系统
     * 建立连接并同步AI模型
     * </p>
     *
     * @param registerForm 注册表单
     * @return 注册结果
     */
    @Observed(name = "edge.video.registerDevice", contextualName = "edge-video-register-device")
    @PostMapping("/device/register")
    @Operation(
            summary = "注册边缘设备",
            description = "注册边缘设备到边缘计算系统，建立连接并同步AI模型",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "注册成功"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "参数错误"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGE", description = "注册边缘设备")
    public ResponseDTO<String> registerEdgeDevice(@Valid @RequestBody EdgeDeviceRegisterForm registerForm) {
        log.info("[边缘视频] 注册边缘设备，deviceId={}, deviceType={}",
                registerForm.getDeviceId(), registerForm.getDeviceType());

        try {
            EdgeDevice edgeDevice = convertToEdgeDevice(registerForm);
            boolean success = edgeVideoProcessor.registerEdgeDevice(edgeDevice);

            if (success) {
                return ResponseDTO.ok("边缘设备注册成功");
            } else {
                return ResponseDTO.error("REGISTER_FAILED", "边缘设备注册失败");
            }

        } catch (Exception e) {
            log.error("[边缘视频] 注册边缘设备异常，deviceId={}, error={}",
                    registerForm.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常：" + e.getMessage());
        }
    }

    /**
     * 注销边缘设备
     *
     * @param deviceId 设备ID
     * @return 注销结果
     */
    @Observed(name = "edge.video.unregisterDevice", contextualName = "edge-video-unregister-device")
    @DeleteMapping("/device/{deviceId}")
    @Operation(
            summary = "注销边缘设备",
            description = "从边缘计算系统注销边缘设备",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGE", description = "注销边缘设备")
    public ResponseDTO<String> unregisterEdgeDevice(@PathVariable String deviceId) {
        log.info("[边缘视频] 注销边缘设备，deviceId={}", deviceId);

        try {
            boolean success = edgeVideoProcessor.unregisterEdgeDevice(deviceId);

            if (success) {
                return ResponseDTO.ok("边缘设备注销成功");
            } else {
                return ResponseDTO.error("UNREGISTER_FAILED", "边缘设备注销失败");
            }

        } catch (Exception e) {
            log.error("[边缘视频] 注销边缘设备异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常：" + e.getMessage());
        }
    }

    /**
     * 执行边缘AI推理
     * <p>
     * 将AI推理任务分发到边缘设备执行
     * </p>
     *
     * @param inferenceForm 推理表单
     * @return 推理结果
     */
    @Observed(name = "edge.video.inference", contextualName = "edge-video-inference")
    @PostMapping("/inference")
    @Operation(
            summary = "执行边缘AI推理",
            description = "将AI推理任务分发到边缘设备执行",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "推理成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = InferenceResult.class)
                            )
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_USE", description = "执行边缘AI推理")
    public ResponseDTO<Future<InferenceResult>> performInference(@Valid @RequestBody InferenceForm inferenceForm) {
        log.info("[边缘视频] 执行边缘AI推理，deviceId={}, taskType={}",
                inferenceForm.getDeviceId(), inferenceForm.getTaskType());

        try {
            InferenceRequest request = convertToInferenceRequest(inferenceForm);
            Future<InferenceResult> result = edgeVideoProcessor.performInference(request);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[边缘视频] 执行边缘AI推理异常，deviceId={}, taskType={}, error={}",
                    inferenceForm.getDeviceId(), inferenceForm.getTaskType(), e.getMessage(), e);
            return ResponseDTO.error("INFERENCE_ERROR", "推理异常：" + e.getMessage());
        }
    }

    /**
     * 批量边缘推理
     * <p>
     * 支持批量推理任务，提高边缘设备利用率
     * </p>
     *
     * @param batchForm 批量推理表单
     * @return 批量推理结果
     */
    @Observed(name = "edge.video.batchInference", contextualName = "edge-video-batch-inference")
    @PostMapping("/inference/batch")
    @Operation(
            summary = "批量边缘推理",
            description = "执行批量边缘推理任务",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "批量推理成功"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_USE", description = "批量边缘推理")
    public ResponseDTO<Map<String, Future<InferenceResult>>> performBatchInference(@Valid @RequestBody InferenceBatchForm batchForm) {
        log.info("[边缘视频] 执行批量边缘推理，请求数量={}", batchForm.getRequests().size());

        try {
            List<InferenceRequest> requests = batchForm.getRequests().stream()
                    .map(this::convertToInferenceRequest)
                    .toList();

            Map<String, Future<InferenceResult>> results = edgeVideoProcessor.performBatchInference(requests);
            return ResponseDTO.ok(results);

        } catch (Exception e) {
            log.error("[边缘视频] 执行批量边缘推理异常，error={}", e.getMessage(), e);
            return ResponseDTO.error("BATCH_INFERENCE_ERROR", "批量推理异常：" + e.getMessage());
        }
    }

    /**
     * 云边协同推理
     * <p>
     * 复杂推理任务的云边协同处理
     * </p>
     *
     * @param inferenceForm 推理表单
     * @return 协同推理结果
     */
    @Observed(name = "edge.video.collaborativeInference", contextualName = "edge-video-collaborative-inference")
    @PostMapping("/inference/collaborative")
    @Operation(
            summary = "云边协同推理",
            description = "复杂推理任务的云边协同处理",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "协同推理成功"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_USE", description = "云边协同推理")
    public ResponseDTO<Future<InferenceResult>> performCollaborativeInference(@Valid @RequestBody InferenceForm inferenceForm) {
        log.info("[边缘视频] 执行云边协同推理，deviceId={}, taskType={}",
                inferenceForm.getDeviceId(), inferenceForm.getTaskType());

        try {
            InferenceRequest request = convertToInferenceRequest(inferenceForm);
            Future<InferenceResult> result = edgeVideoProcessor.performCloudCollaborativeInference(request);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[边缘视频] 执行云边协同推理异常，deviceId={}, taskType={}, error={}",
                    inferenceForm.getDeviceId(), inferenceForm.getTaskType(), e.getMessage(), e);
            return ResponseDTO.error("COLLABORATIVE_INFERENCE_ERROR", "协同推理异常：" + e.getMessage());
        }
    }

    /**
     * 更新边缘设备模型
     *
     * @param deviceId 设备ID
     * @param modelType 模型类型
     * @param modelData 模型数据（Base64编码）
     * @return 更新结果
     */
    @Observed(name = "edge.video.updateModel", contextualName = "edge-video-update-model")
    @PostMapping("/device/{deviceId}/model/{modelType}")
    @Operation(
            summary = "更新边缘设备模型",
            description = "动态更新边缘设备上的AI模型",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true),
                    @Parameter(name = "modelType", description = "模型类型", required = true)
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGE", description = "更新边缘设备模型")
    public ResponseDTO<String> updateEdgeModel(
            @PathVariable String deviceId,
            @PathVariable String modelType,
            @RequestParam String modelData) {

        log.info("[边缘视频] 更新边缘设备模型，deviceId={}, modelType={}, modelSize={}KB",
                deviceId, modelType, modelData.length() / 1024);

        try {
            // 解码Base64模型数据
            byte[] modelBytes = java.util.Base64.getDecoder().decode(modelData);
            boolean success = edgeVideoProcessor.updateEdgeModel(deviceId, modelType, modelBytes);

            if (success) {
                return ResponseDTO.ok("边缘模型更新成功");
            } else {
                return ResponseDTO.error("UPDATE_MODEL_FAILED", "边缘模型更新失败");
            }

        } catch (Exception e) {
            log.error("[边缘视频] 更新边缘设备模型异常，deviceId={}, modelType={}, error={}",
                    deviceId, modelType, e.getMessage(), e);
            return ResponseDTO.error("UPDATE_MODEL_ERROR", "模型更新异常：" + e.getMessage());
        }
    }

    /**
     * 获取边缘设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    @Observed(name = "edge.video.getDeviceStatus", contextualName = "edge-video-get-device-status")
    @GetMapping("/device/{deviceId}/status")
    @Operation(
            summary = "获取边缘设备状态",
            description = "查询边缘设备的运行状态",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGE", description = "获取边缘设备状态")
    public ResponseDTO<EdgeDeviceStatusVO> getEdgeDeviceStatus(@PathVariable String deviceId) {
        log.info("[边缘视频] 获取边缘设备状态，deviceId={}", deviceId);

        try {
            EdgeDeviceStatus status = edgeVideoProcessor.getEdgeDeviceStatus(deviceId);
            EdgeDeviceStatusVO statusVO = convertToStatusVO(deviceId, status);

            return ResponseDTO.ok(statusVO);

        } catch (Exception e) {
            log.error("[边缘视频] 获取边缘设备状态异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("GET_STATUS_ERROR", "获取状态异常：" + e.getMessage());
        }
    }

    /**
     * 获取边缘计算统计信息
     *
     * @return 统计信息
     */
    @Observed(name = "edge.video.getStatistics", contextualName = "edge-video-get-statistics")
    @GetMapping("/statistics")
    @Operation(
            summary = "获取边缘计算统计信息",
            description = "获取边缘计算系统的运行统计信息"
    )
    @PermissionCheck(value = "VIDEO_MANAGE", description = "获取边缘计算统计信息")
    public ResponseDTO<EdgeStatisticsVO> getEdgeStatistics() {
        log.info("[边缘视频] 获取边缘计算统计信息");

        try {
            EdgeStatistics stats = edgeVideoProcessor.getEdgeStatistics();
            EdgeStatisticsVO statsVO = convertToStatisticsVO(stats);

            return ResponseDTO.ok(statsVO);

        } catch (Exception e) {
            log.error("[边缘视频] 获取边缘计算统计信息异常，error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取统计异常：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    private EdgeDevice convertToEdgeDevice(EdgeDeviceRegisterForm form) {
        EdgeDevice device = new EdgeDevice();
        device.setDeviceId(form.getDeviceId());
        device.setDeviceType(form.getDeviceType());
        device.setDeviceName(form.getDeviceName());
        device.setIpAddress(form.getIpAddress());
        device.setPort(form.getPort());
        device.setHardwareSpec(form.getHardwareSpec());
        device.setCapabilities(form.getCapabilities());
        device.setLocation(form.getLocation());
        return device;
    }

    private InferenceRequest convertToInferenceRequest(InferenceForm form) {
        InferenceRequest request = new InferenceRequest();
        request.setTaskId(form.getTaskId());
        request.setDeviceId(form.getDeviceId());
        request.setTaskType(form.getTaskType());
        request.setModelType(form.getModelType());
        request.setData(form.getData());
        request.setPriority(form.getPriority());
        request.setTimeout(form.getTimeout());
        return request;
    }

    private EdgeDeviceStatusVO convertToStatusVO(String deviceId, EdgeDeviceStatus status) {
        EdgeDeviceStatusVO statusVO = new EdgeDeviceStatusVO();
        statusVO.setDeviceId(deviceId);
        statusVO.setStatus(status);
        statusVO.setStatusDescription(status.getDescription());
        statusVO.setUpdateTime(java.time.LocalDateTime.now());
        return statusVO;
    }

    private EdgeStatisticsVO convertToStatisticsVO(EdgeStatistics stats) {
        EdgeStatisticsVO statsVO = new EdgeStatisticsVO();
        statsVO.setTotalDevices(stats.getTotalDevices());
        statsVO.setReadyDevices(stats.getReadyDevices());
        statsVO.setTotalInferences(stats.getTotalInferences());
        statsVO.setAverageInferenceTime(stats.getAverageInferenceTime());
        statsVO.setLastUpdateTime(stats.getLastUpdateTime());
        return statsVO;
    }
}