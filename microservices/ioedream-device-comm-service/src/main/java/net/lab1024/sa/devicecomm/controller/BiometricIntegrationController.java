package net.lab1024.sa.devicecomm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.devicecomm.integration.BiometricIntegrationService;
import net.lab1024.sa.devicecomm.protocol.enums.VerifyTypeEnum;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 生物识别集成API控制器
 * <p>
 * 提供生物识别功能与其他业务模块集成的REST API：
 * - 门禁生物识别验证
 * - 考勤生物识别打卡
 * - 访客生物识别验证
 * - 消费生物识别支付
 * - 批量生物识别注册
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/biometric/integration")
@Tag(name = "生物识别集成", description = "生物识别与其他业务模块集成接口")
@Validated
public class BiometricIntegrationController {

    @Resource
    private BiometricIntegrationService biometricIntegrationService;

    /**
     * 门禁生物识别验证
     */
    @PostMapping("/access/verify")
    @Operation(summary = "门禁生物识别验证", description = "验证用户门禁权限并进行生物识别")
    public ResponseDTO<BiometricIntegrationService.BiometricAccessResult> verifyAccessBiometric(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull Long userId,
            @Parameter(description = "设备ID", required = true) @RequestParam @NotNull Long deviceId,
            @Parameter(description = "区域ID", required = true) @RequestParam @NotNull Long areaId,
            @Parameter(description = "验证类型", required = true) @RequestParam @NotNull VerifyTypeEnum verifyType,
            @Valid @RequestBody BiometricFeatureRequest request) {
        log.info("[门禁生物识别验证] 接口调用: userId={}, deviceId={}, areaId={}, verifyType={}",
                userId, deviceId, areaId, verifyType.getName());

        return biometricIntegrationService.verifyAccessBiometric(
                userId, deviceId, areaId, verifyType, request.getFeatureData());
    }

    /**
     * 考勤生物识别打卡
     */
    @PostMapping("/attendance/punch")
    @Operation(summary = "考勤生物识别打卡", description = "进行考勤打卡并进行生物识别验证")
    public ResponseDTO<BiometricIntegrationService.BiometricAttendanceResult> punchAttendanceBiometric(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull Long userId,
            @Parameter(description = "设备ID", required = true) @RequestParam @NotNull Long deviceId,
            @Parameter(description = "验证类型", required = true) @RequestParam @NotNull VerifyTypeEnum verifyType,
            @Parameter(description = "打卡类型", required = true) @RequestParam @NotNull String punchType,
            @Valid @RequestBody BiometricFeatureRequest request) {
        log.info("[考勤生物识别打卡] 接口调用: userId={}, deviceId={}, verifyType={}, punchType={}",
                userId, deviceId, verifyType.getName(), punchType);

        return biometricIntegrationService.punchAttendanceBiometric(
                userId, deviceId, verifyType, request.getFeatureData(), punchType);
    }

    /**
     * 访客生物识别验证
     */
    @PostMapping("/visitor/verify")
    @Operation(summary = "访客生物识别验证", description = "验证访客身份并进行生物识别")
    public ResponseDTO<BiometricIntegrationService.BiometricVisitorResult> verifyVisitorBiometric(
            @Parameter(description = "访客ID", required = true) @RequestParam @NotNull Long visitorId,
            @Parameter(description = "设备ID", required = true) @RequestParam @NotNull Long deviceId,
            @Parameter(description = "区域ID", required = true) @RequestParam @NotNull Long areaId,
            @Parameter(description = "验证类型", required = true) @RequestParam @NotNull VerifyTypeEnum verifyType,
            @Valid @RequestBody BiometricFeatureRequest request) {
        log.info("[访客生物识别验证] 接口调用: visitorId={}, deviceId={}, areaId={}, verifyType={}",
                visitorId, deviceId, areaId, verifyType.getName());

        return biometricIntegrationService.verifyVisitorBiometric(
                visitorId, deviceId, areaId, verifyType, request.getFeatureData());
    }

    /**
     * 消费生物识别验证
     */
    @PostMapping("/consume/verify")
    @Operation(summary = "消费生物识别验证", description = "进行消费支付并进行生物识别验证")
    public ResponseDTO<BiometricIntegrationService.BiometricConsumeResult> verifyConsumeBiometric(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull Long userId,
            @Parameter(description = "设备ID", required = true) @RequestParam @NotNull Long deviceId,
            @Parameter(description = "消费金额", required = true) @RequestParam @NotNull Double amount,
            @Parameter(description = "验证类型", required = true) @RequestParam @NotNull VerifyTypeEnum verifyType,
            @Valid @RequestBody BiometricFeatureRequest request) {
        log.info("[消费生物识别验证] 接口调用: userId={}, deviceId={}, amount={}, verifyType={}",
                userId, deviceId, amount, verifyType.getName());

        return biometricIntegrationService.verifyConsumeBiometric(
                userId, deviceId, amount, verifyType, request.getFeatureData());
    }

    /**
     * 批量注册用户生物识别
     */
    @PostMapping("/batch-register/{userId}")
    @Operation(summary = "批量注册生物识别", description = "为用户批量注册多个生物识别特征")
    public ResponseDTO<BiometricIntegrationService.BiometricBatchRegisterResult> batchRegisterUserBiometric(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @Valid @RequestBody List<BiometricRegisterRequest> requestList) {
        log.info("[批量注册生物识别] 接口调用: userId={}, count={}", userId, requestList.size());

        List<BiometricIntegrationService.BiometricRegisterData> biometricDataList = requestList.stream()
                .map(req -> {
                    BiometricIntegrationService.BiometricRegisterData data = new BiometricIntegrationService.BiometricRegisterData();
                    data.setVerifyType(req.getVerifyType());
                    data.setFeatureData(req.getFeatureData());
                    data.setTemplateData(req.getTemplateData());
                    data.setDeviceId(req.getDeviceId());
                    return data;
                })
                .toList();

        return biometricIntegrationService.batchRegisterUserBiometric(userId, biometricDataList);
    }

    /**
     * 异步批量注册用户生物识别
     */
    @PostMapping("/batch-register-async/{userId}")
    @Operation(summary = "异步批量注册生物识别", description = "异步为用户批量注册多个生物识别特征")
    public CompletableFuture<ResponseDTO<BiometricIntegrationService.BiometricBatchRegisterResult>> batchRegisterUserBiometricAsync(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @Valid @RequestBody List<BiometricRegisterRequest> requestList) {
        log.info("[异步批量注册生物识别] 接口调用: userId={}, count={}", userId, requestList.size());

        List<BiometricIntegrationService.BiometricRegisterData> biometricDataList = requestList.stream()
                .map(req -> {
                    BiometricIntegrationService.BiometricRegisterData data = new BiometricIntegrationService.BiometricRegisterData();
                    data.setVerifyType(req.getVerifyType());
                    data.setFeatureData(req.getFeatureData());
                    data.setTemplateData(req.getTemplateData());
                    data.setDeviceId(req.getDeviceId());
                    return data;
                })
                .toList();

        return biometricIntegrationService.batchRegisterUserBiometricAsync(userId, biometricDataList);
    }

    /**
     * 快速门禁验证（简化版）
     */
    @PostMapping("/access/quick-verify")
    @Operation(summary = "快速门禁验证", description = "快速门禁生物识别验证，自动获取设备信息")
    public ResponseDTO<BiometricIntegrationService.BiometricAccessResult> quickVerifyAccess(
            @Valid @RequestBody QuickAccessRequest request) {
        log.info("[快速门禁验证] 接口调用: userId={}, verifyType={}",
                request.getUserId(), request.getVerifyType().getName());

        return biometricIntegrationService.verifyAccessBiometric(
                request.getUserId(), request.getDeviceId(), request.getAreaId(),
                request.getVerifyType(), request.getFeatureData());
    }

    /**
     * 快速考勤打卡（简化版）
     */
    @PostMapping("/attendance/quick-punch")
    @Operation(summary = "快速考勤打卡", description = "快速考勤生物识别打卡，自动获取设备信息")
    public ResponseDTO<BiometricIntegrationService.BiometricAttendanceResult> quickPunchAttendance(
            @Valid @RequestBody QuickAttendanceRequest request) {
        log.info("[快速考勤打卡] 接口调用: userId={}, verifyType={}, punchType={}",
                request.getUserId(), request.getVerifyType().getName(), request.getPunchType());

        return biometricIntegrationService.punchAttendanceBiometric(
                request.getUserId(), request.getDeviceId(),
                request.getVerifyType(), request.getFeatureData(), request.getPunchType());
    }

    /**
     * 快速消费支付（简化版）
     */
    @PostMapping("/consume/quick-pay")
    @Operation(summary = "快速消费支付", description = "快速消费生物识别支付，自动获取设备信息")
    public ResponseDTO<BiometricIntegrationService.BiometricConsumeResult> quickPayConsume(
            @Valid @RequestBody QuickConsumeRequest request) {
        log.info("[快速消费支付] 接口调用: userId={}, amount={}, verifyType={}",
                request.getUserId(), request.getAmount(), request.getVerifyType().getName());

        return biometricIntegrationService.verifyConsumeBiometric(
                request.getUserId(), request.getDeviceId(), request.getAmount(),
                request.getVerifyType(), request.getFeatureData());
    }

    /**
     * 集成服务健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "集成服务健康检查", description = "生物识别集成服务健康检查")
    public ResponseDTO<String> health() {
        return ResponseDTO.ok("生物识别集成服务运行正常");
    }

    /**
     * 获取集成功能列表
     */
    @GetMapping("/functions")
    @Operation(summary = "获取集成功能列表", description = "获取支持的所有生物识别集成功能")
    public ResponseDTO<List<IntegrationFunction>> getIntegrationFunctions() {
        List<IntegrationFunction> functions = List.of(
                new IntegrationFunction("access", "门禁集成", "与门禁系统集成的生物识别验证"),
                new IntegrationFunction("attendance", "考勤集成", "与考勤系统集成的生物识别打卡"),
                new IntegrationFunction("visitor", "访客集成", "与访客系统集成的生物识别验证"),
                new IntegrationFunction("consume", "消费集成", "与消费系统集成的生活识别支付"),
                new IntegrationFunction("batch_register", "批量注册", "批量注册用户生物识别特征")
        );

        return ResponseDTO.ok(functions);
    }

    // ==================== DTO类 ====================

    /**
     * 生物特征请求DTO
     */
    public static class BiometricFeatureRequest {
        @Parameter(description = "特征数据", required = true)
        private byte[] featureData;

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }
    }

    /**
     * 生物识别注册请求DTO
     */
    public static class BiometricRegisterRequest {
        @Parameter(description = "验证类型", required = true)
        private VerifyTypeEnum verifyType;

        @Parameter(description = "特征数据", required = true)
        private byte[] featureData;

        @Parameter(description = "模板数据")
        private byte[] templateData;

        @Parameter(description = "设备ID", required = true)
        private Long deviceId;

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }

        public byte[] getTemplateData() { return templateData; }
        public void setTemplateData(byte[] templateData) { this.templateData = templateData; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    }

    /**
     * 快速门禁请求DTO
     */
    public static class QuickAccessRequest {
        @Parameter(description = "用户ID", required = true)
        private Long userId;

        @Parameter(description = "设备ID", required = true)
        private Long deviceId;

        @Parameter(description = "区域ID", required = true)
        private Long areaId;

        @Parameter(description = "验证类型", required = true)
        private VerifyTypeEnum verifyType;

        @Parameter(description = "特征数据", required = true)
        private byte[] featureData;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }
    }

    /**
     * 快速考勤请求DTO
     */
    public static class QuickAttendanceRequest {
        @Parameter(description = "用户ID", required = true)
        private Long userId;

        @Parameter(description = "设备ID", required = true)
        private Long deviceId;

        @Parameter(description = "验证类型", required = true)
        private VerifyTypeEnum verifyType;

        @Parameter(description = "打卡类型", required = true)
        private String punchType;

        @Parameter(description = "特征数据", required = true)
        private byte[] featureData;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public String getPunchType() { return punchType; }
        public void setPunchType(String punchType) { this.punchType = punchType; }

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }
    }

    /**
     * 快速消费请求DTO
     */
    public static class QuickConsumeRequest {
        @Parameter(description = "用户ID", required = true)
        private Long userId;

        @Parameter(description = "设备ID", required = true)
        private Long deviceId;

        @Parameter(description = "消费金额", required = true)
        private Double amount;

        @Parameter(description = "验证类型", required = true)
        private VerifyTypeEnum verifyType;

        @Parameter(description = "特征数据", required = true)
        private byte[] featureData;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }
    }

    /**
     * 集成功能信息DTO
     */
    public static class IntegrationFunction {
        @Parameter(description = "功能代码")
        private String code;

        @Parameter(description = "功能名称")
        private String name;

        @Parameter(description = "功能描述")
        private String description;

        public IntegrationFunction() {}

        public IntegrationFunction(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}