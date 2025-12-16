package net.lab1024.sa.visitor.controller;

import io.github.resilience4j.annotation.CircuitBreaker;
import io.github.resilience4j.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.response.ResponseDTO;
import net.lab1024.sa.visitor.service.VisitorAccessControlService;
import net.lab1024.sa.visitor.service.VisitorFaceRecognitionService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 访客安全管理控制器
 * <p>
 * 内存优化设计：
 * - 使用异步处理，提高并发性能
 * - 合理的参数验证，避免内存溢出
 * - 熔断器保护，防止级联故障
 * - 分页参数限制，避免大数据量传输
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/visitor/security")
@Tag(name = "访客安全管理", description = "访客人脸识别和门禁控制相关API")
@Validated
@CircuitBreaker(name = "visitorSecurityController")
public class VisitorSecurityController {

    @Resource
    private VisitorFaceRecognitionService faceRecognitionService;

    @Resource
    private VisitorAccessControlService accessControlService;

    // ==================== 人脸识别相关API ====================

    /**
     * 人脸特征注册
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "人脸特征注册",
        description = "为访客注册人脸特征，用于后续识别验证"
    )
    @PostMapping("/face/register/{visitorId}")
    public CompletableFuture<ResponseDTO<String>> registerFaceFeature(
            @Parameter(description = "访客ID", required = true, example = "1001")
            @PathVariable Long visitorId,
            @Parameter(description = "人脸图像数据数组", required = true)
            @RequestBody String[] faceImages
    ) {
        log.info("[人脸识别] 注册人脸特征, visitorId={}, imageCount={}", visitorId, faceImages.length);
        return faceRecognitionService.registerFaceFeature(visitorId, faceImages);
    }

    /**
     * 人脸识别验证
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "人脸识别验证",
        description = "实时人脸识别验证，支持活体检测"
    )
    @PostMapping("/face/recognize")
    public CompletableFuture<ResponseDTO<Object>> recognizeFace(
            @Parameter(description = "人脸图像数据", required = true)
            @RequestBody String faceImage,
            @Parameter(description = "设备ID", example = "DEVICE_001")
            @RequestParam(required = false) String deviceId
    ) {
        log.info("[人脸识别] 开始人脸识别验证, deviceId={}", deviceId);
        return faceRecognitionService.recognizeFace(faceImage, deviceId);
    }

    /**
     * 批量人脸识别
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "批量人脸识别",
        description = "批量处理多张人脸图像，提高处理效率"
    )
    @PostMapping("/face/batch-recognize")
    public CompletableFuture<ResponseDTO<Object>> batchRecognizeFaces(
            @Parameter(description = "人脸图像数组", required = true)
            @RequestBody String[] faceImages,
            @Parameter(description = "设备ID", required = true, example = "DEVICE_001")
            @RequestParam String deviceId
    ) {
        log.info("[人脸识别] 批量人脸识别, imageCount={}, deviceId={}", faceImages.length, deviceId);
        return faceRecognitionService.batchRecognizeFaces(faceImages, deviceId);
    }

    /**
     * 更新人脸特征
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "更新人脸特征",
        description = "更新访客的人脸特征信息"
    )
    @PutMapping("/face/{visitorId}/feature/{featureId}")
    public CompletableFuture<ResponseDTO<Void>> updateFaceFeature(
            @Parameter(description = "访客ID", required = true, example = "1001")
            @PathVariable Long visitorId,
            @Parameter(description = "特征ID", required = true, example = "FEATURE_001")
            @PathVariable String featureId,
            @Parameter(description = "新的人脸图像数据", required = true)
            @RequestBody String[] faceImages
    ) {
        log.info("[人脸识别] 更新人脸特征, visitorId={}, featureId={}", visitorId, featureId);
        return faceRecognitionService.updateFaceFeature(visitorId, featureId, faceImages);
    }

    /**
     * 删除人脸特征
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "删除人脸特征",
        description = "删除指定访客的人脸特征数据"
    )
    @DeleteMapping("/face/{visitorId}/feature/{featureId}")
    public CompletableFuture<ResponseDTO<Void>> deleteFaceFeature(
            @Parameter(description = "访客ID", required = true, example = "1001")
            @PathVariable Long visitorId,
            @Parameter(description = "特征ID", example = "FEATURE_001")
            @PathVariable(required = false) String featureId
    ) {
        log.info("[人脸识别] 删除人脸特征, visitorId={}, featureId={}", visitorId, featureId);
        return faceRecognitionService.deleteFaceFeature(visitorId, featureId);
    }

    /**
     * 人脸特征比对
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "人脸特征比对",
        description = "比较两张人脸图像的相似度"
    )
    @PostMapping("/face/compare")
    public CompletableFuture<ResponseDTO<Object>> compareFaces(
            @Parameter(description = "第一张人脸图像", required = true)
            @RequestParam String faceImage1,
            @Parameter(description = "第二张人脸图像", required = true)
            @RequestParam String faceImage2,
            @Parameter(description = "比对阈值", example = "0.8")
            @RequestParam(required = false) Double threshold
    ) {
        log.info("[人脸识别] 人脸特征比对");
        return faceRecognitionService.compareFaces(faceImage1, faceImage2, threshold);
    }

    /**
     * 人脸质量检测
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "人脸质量检测",
        description = "检测人脸图像的质量，包括清晰度、角度、光照等"
    )
    @PostMapping("/face/quality")
    public CompletableFuture<ResponseDTO<Object>> detectFaceQuality(
            @Parameter(description = "人脸图像数据", required = true)
            @RequestBody String faceImage
    ) {
        log.info("[人脸识别] 人脸质量检测");
        return faceRecognitionService.detectFaceQuality(faceImage);
    }

    /**
     * 活体检测
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "活体检测",
        description = "检测人脸是否为真实活体，防止照片、视频等攻击"
    )
    @PostMapping("/face/liveness")
    public CompletableFuture<ResponseDTO<Object>> livenessDetection(
            @Parameter(description = "人脸图像数据", required = true)
            @RequestBody String faceImage,
            @Parameter(description = "动作类型", example = "blink")
            @RequestParam(required = false) String actionType
    ) {
        log.info("[人脸识别] 活体检测, actionType={}", actionType);
        return faceRecognitionService.livenessDetection(faceImage, actionType);
    }

    /**
     * 获取访客人脸特征
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "获取访客人脸特征",
        description = "查询指定访客的所有人脸特征信息"
    )
    @GetMapping("/face/{visitorId}/features")
    public CompletableFuture<ResponseDTO<Object>> getVisitorFaceFeatures(
            @Parameter(description = "访客ID", required = true, example = "1001")
            @PathVariable Long visitorId
    ) {
        log.info("[人脸识别] 获取访客人脸特征, visitorId={}", visitorId);
        return faceRecognitionService.getVisitorFaceFeatures(visitorId);
    }

    /**
     * 人脸识别统计
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "人脸识别统计",
        description = "获取人脸识别系统的统计数据"
    )
    @GetMapping("/face/statistics")
    public CompletableFuture<ResponseDTO<Object>> getFaceRecognitionStatistics(
            @Parameter(description = "开始时间", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) String endTime
    ) {
        log.info("[人脸识别] 获取统计数据, startTime={}, endTime={}", startTime, endTime);
        return faceRecognitionService.getFaceRecognitionStatistics(startTime, endTime);
    }

    // ==================== 门禁控制相关API ====================

    /**
     * 门禁授权
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "门禁授权",
        description = "为访客授权门禁通行权限"
    )
    @PostMapping("/access/authorize")
    public CompletableFuture<ResponseDTO<String>> authorizeAccess(
            @Parameter(description = "访客ID", required = true, example = "1001")
            @RequestParam Long visitorId,
            @Parameter(description = "门禁设备ID", required = true, example = "ACCESS_001")
            @RequestParam String deviceId,
            @Parameter(description = "允许访问的区域ID列表", required = true)
            @RequestParam List<Long> areaIds,
            @Parameter(description = "开始时间", example = "2025-01-30T00:00:00")
            @RequestParam String startTime,
            @Parameter(description = "结束时间", example = "2025-12-31T23:59:59")
            @RequestParam String endTime
    ) {
        log.info("[门禁控制] 门禁授权, visitorId={}, deviceId={}", visitorId, deviceId);
        return accessControlService.authorizeAccess(visitorId, deviceId, areaIds, startTime, endTime);
    }

    /**
     * 门禁验证
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "门禁验证",
        description = "验证访客的门禁通行权限"
    )
    @PostMapping("/access/verify")
    public CompletableFuture<ResponseDTO<Object>> verifyAccess(
            @Parameter(description = "门禁设备ID", required = true, example = "ACCESS_001")
            @RequestParam String deviceId,
            @Parameter(description = "验证类型", required = true, example = "face")
            @RequestParam String verifyType,
            @Parameter(description = "验证数据", required = true)
            @RequestBody String verifyData
    ) {
        log.info("[门禁控制] 门禁验证, deviceId={}, verifyType={}", deviceId, verifyType);
        return accessControlService.verifyAccess(deviceId, verifyType, verifyData);
    }

    /**
     * 撤销门禁权限
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "撤销门禁权限",
        description = "撤销访客的门禁通行权限"
    )
    @DeleteMapping("/access/revoke/{visitorId}")
    public CompletableFuture<ResponseDTO<Void>> revokeAccess(
            @Parameter(description = "访客ID", required = true, example = "1001")
            @PathVariable Long visitorId,
            @Parameter(description = "访问凭证ID", example = "CREDENTIAL_001")
            @RequestParam(required = false) String credentialId,
            @Parameter(description = "撤销原因", required = true, example = "访问结束")
            @RequestParam String reason
    ) {
        log.info("[门禁控制] 撤销门禁权限, visitorId={}, credentialId={}", visitorId, credentialId);
        return accessControlService.revokeAccess(visitorId, credentialId, reason);
    }

    /**
     * 批量门禁授权
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "批量门禁授权",
        description = "为多个访客批量授权门禁权限"
    )
    @PostMapping("/access/batch-authorize")
    public CompletableFuture<ResponseDTO<List<String>>> batchAuthorizeAccess(
            @Parameter(description = "访客ID列表", required = true)
            @RequestParam List<Long> visitorIds,
            @Parameter(description = "门禁设备ID", required = true, example = "ACCESS_001")
            @RequestParam String deviceId,
            @Parameter(description = "区域权限列表", required = true)
            @RequestParam List<Long> areaIds,
            @Parameter(description = "开始时间", required = true)
            @RequestParam String startTime,
            @Parameter(description = "结束时间", required = true)
            @RequestParam String endTime
    ) {
        log.info("[门禁控制] 批量门禁授权, visitorCount={}, deviceId={}", visitorIds.size(), deviceId);
        return accessControlService.batchAuthorizeAccess(visitorIds, deviceId, areaIds, startTime, endTime);
    }

    /**
     * 检查门禁权限
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "检查门禁权限",
        description = "检查访客的门禁权限状态"
    )
    @GetMapping("/access/permission/check")
    public CompletableFuture<ResponseDTO<Object>> checkAccessPermission(
            @Parameter(description = "访客ID", required = true, example = "1001")
            @RequestParam Long visitorId,
            @Parameter(description = "门禁设备ID", required = true, example = "ACCESS_001")
            @RequestParam String deviceId
    ) {
        log.info("[门禁控制] 检查门禁权限, visitorId={}, deviceId={}", visitorId, deviceId);
        return accessControlService.checkAccessPermission(visitorId, deviceId);
    }

    /**
     * 获取通行记录
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "获取通行记录",
        description = "查询访客的门禁通行记录"
    )
    @GetMapping("/access/records")
    public CompletableFuture<ResponseDTO<Object>> getAccessRecords(
            @Parameter(description = "访客ID", example = "1001")
            @RequestParam(required = false) Long visitorId,
            @Parameter(description = "门禁设备ID", example = "ACCESS_001")
            @RequestParam(required = false) String deviceId,
            @Parameter(description = "开始时间", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) String endTime,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        log.info("[门禁控制] 获取通行记录, visitorId={}, deviceId={}", visitorId, deviceId);
        return accessControlService.getAccessRecords(visitorId, deviceId, startTime, endTime, pageNum, pageSize);
    }

    /**
     * 实时门禁监控
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "实时门禁监控",
        description = "实时监控门禁设备状态和通行情况"
    )
    @PostMapping("/access/monitor")
    public CompletableFuture<ResponseDTO<Object>> getRealTimeAccessMonitor(
            @Parameter(description = "门禁设备ID列表", required = true)
            @RequestBody List<String> deviceIds
    ) {
        log.info("[门禁控制] 实时门禁监控, deviceCount={}", deviceIds.size());
        return accessControlService.getRealTimeAccessMonitor(deviceIds);
    }

    /**
     * 远程开门
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "远程开门",
        description = "远程控制门禁设备开门"
    )
    @PostMapping("/access/remote-open/{deviceId}")
    public CompletableFuture<ResponseDTO<Void>> remoteOpenDoor(
            @Parameter(description = "门禁设备ID", required = true, example = "ACCESS_001")
            @PathVariable String deviceId,
            @Parameter(description = "操作人ID", required = true, example = "1001")
            @RequestParam Long operatorId,
            @Parameter(description = "开门原因", required = true, example = "紧急开门")
            @RequestParam String reason
    ) {
        log.info("[门禁控制] 远程开门, deviceId={}, operatorId={}", deviceId, operatorId);
        return accessControlService.remoteOpenDoor(deviceId, operatorId, reason);
    }

    /**
     * 获取设备状态
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "获取设备状态",
        description = "查询门禁设备的在线状态和健康情况"
    )
    @PostMapping("/access/device/status")
    public CompletableFuture<ResponseDTO<Object>> getDeviceStatus(
            @Parameter(description = "门禁设备ID列表", required = true)
            @RequestBody List<String> deviceIds
    ) {
        log.info("[门禁控制] 获取设备状态, deviceCount={}", deviceIds.size());
        return accessControlService.getDeviceStatus(deviceIds);
    }

    /**
     * 门禁统计
     */
    @TimeLimiter(name = "visitorSecurityController")
    @Operation(
        summary = "门禁统计",
        description = "获取门禁权限的统计数据"
    )
    @GetMapping("/access/statistics")
    public CompletableFuture<ResponseDTO<Object>> getAccessControlStatistics(
            @Parameter(description = "开始时间", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) String endTime
    ) {
        log.info("[门禁控制] 获取门禁统计, startTime={}, endTime={}", startTime, endTime);
        return accessControlService.getAccessControlStatistics(startTime, endTime);
    }
}