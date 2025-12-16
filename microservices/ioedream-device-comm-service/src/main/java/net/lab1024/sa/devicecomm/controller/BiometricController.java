package net.lab1024.sa.devicecomm.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.devicecomm.biometric.BiometricDataManager;
import net.lab1024.sa.devicecomm.protocol.enums.VerifyTypeEnum;
import net.lab1024.sa.devicecomm.service.BiometricService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 生物识别API控制器
 * <p>
 * 提供完整的生物识别功能REST API：
 * - 生物识别注册
 * - 生物识别验证
 * - 生物识别数据管理
 * - 生物识别统计分析
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/biometric")
@Tag(name = "生物识别管理", description = "生物识别相关接口")
@Validated
public class BiometricController {

    @Resource
    private BiometricService biometricService;

    /**
     * 注册生物识别特征
     */
    @Observed(name = "biometric.register", contextualName = "biometric-register")
    @PostMapping("/register")
    @Operation(summary = "注册生物识别", description = "为用户注册生物识别特征")
    public ResponseDTO<Void> registerBiometric(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull Long userId,
            @Parameter(description = "验证类型", required = true) @RequestParam @NotNull VerifyTypeEnum verifyType,
            @Parameter(description = "设备ID", required = true) @RequestParam @NotNull Long deviceId,
            @Valid @RequestBody BiometricRegisterRequest request) {
        log.info("[生物识别注册] 接口调用: userId={}, verifyType={}, deviceId={}",
                userId, verifyType.getName(), deviceId);

        return biometricService.registerBiometric(
                userId, verifyType,
                request.getFeatureData(), request.getTemplateData(),
                deviceId);
    }

    /**
     * 异步注册生物识别特征
     */
    @Observed(name = "biometric.registerAsync", contextualName = "biometric-register-async")
    @PostMapping("/register-async")
    @Operation(summary = "异步注册生物识别", description = "异步为用户注册生物识别特征")
    public CompletableFuture<ResponseDTO<Void>> registerBiometricAsync(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull Long userId,
            @Parameter(description = "验证类型", required = true) @RequestParam @NotNull VerifyTypeEnum verifyType,
            @Parameter(description = "设备ID", required = true) @RequestParam @NotNull Long deviceId,
            @Valid @RequestBody BiometricRegisterRequest request) {
        log.info("[生物识别异步注册] 接口调用: userId={}, verifyType={}, deviceId={}",
                userId, verifyType.getName(), deviceId);

        return biometricService.registerBiometricAsync(
                userId, verifyType,
                request.getFeatureData(), request.getTemplateData(),
                deviceId);
    }

    /**
     * 验证生物识别特征
     */
    @Observed(name = "biometric.verify", contextualName = "biometric-verify")
    @PostMapping("/verify")
    @Operation(summary = "验证生物识别", description = "验证用户的生物识别特征")
    public ResponseDTO<BiometricDataManager.BiometricMatchResult> verifyBiometric(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull Long userId,
            @Parameter(description = "验证类型", required = true) @RequestParam @NotNull VerifyTypeEnum verifyType,
            @Valid @RequestBody BiometricVerifyRequest request) {
        log.info("[生物识别验证] 接口调用: userId={}, verifyType={}", userId, verifyType.getName());

        return biometricService.verifyBiometric(userId, verifyType, request.getFeatureData());
    }

    /**
     * 查找最佳匹配用户
     */
    @Observed(name = "biometric.findBestMatch", contextualName = "biometric-find-match")
    @PostMapping("/match")
    @Operation(summary = "查找最佳匹配", description = "根据生物特征查找最佳匹配用户")
    public ResponseDTO<BiometricDataManager.BiometricMatchResult> findBestMatch(
            @Parameter(description = "验证类型", required = true) @RequestParam @NotNull VerifyTypeEnum verifyType,
            @Valid @RequestBody BiometricMatchRequest request) {
        log.info("[生物识别匹配] 接口调用: verifyType={}, candidateCount={}",
                verifyType.getName(), request.getCandidateUserIds() != null ? request.getCandidateUserIds().size() : 0);

        return biometricService.findBestMatch(verifyType, request.getFeatureData(), request.getCandidateUserIds());
    }

    /**
     * 删除生物识别数据
     */
    @Observed(name = "biometric.deleteData", contextualName = "biometric-delete-data")
    @DeleteMapping("/data")
    @Operation(summary = "删除生物数据", description = "删除用户的生物识别数据")
    public ResponseDTO<Void> deleteBiometricData(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull Long userId,
            @Parameter(description = "验证类型", required = true) @RequestParam @NotNull VerifyTypeEnum verifyType) {
        log.info("[生物识别删除] 接口调用: userId={}, verifyType={}", userId, verifyType.getName());

        return biometricService.deleteBiometricData(userId, verifyType);
    }

    /**
     * 获取用户支持的验证方式
     */
    @Observed(name = "biometric.getSupportedVerifyTypes", contextualName = "biometric-get-verify-types")
    @GetMapping("/verify-types/{userId}")
    @Operation(summary = "获取验证方式", description = "获取用户支持的生物识别验证方式")
    public ResponseDTO<List<VerifyTypeEnum>> getSupportedVerifyTypes(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        log.debug("[获取验证方式] 接口调用: userId={}", userId);

        return biometricService.getSupportedVerifyTypes(userId);
    }

    /**
     * 获取用户生物识别数据
     */
    @Observed(name = "biometric.getUserBiometricData", contextualName = "biometric-get-user-data")
    @GetMapping("/data/{userId}")
    @Operation(summary = "获取生物数据", description = "获取用户的所有生物识别数据")
    public ResponseDTO<List<BiometricDataManager.BiometricData>> getUserBiometricData(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        log.debug("[获取生物数据] 接口调用: userId={}", userId);

        return biometricService.getUserBiometricData(userId);
    }

    /**
     * 处理设备生物识别消息
     */
    @Observed(name = "biometric.processDeviceMessage", contextualName = "biometric-process-device-message")
    @PostMapping("/device-message/{deviceId}")
    @Operation(summary = "处理设备消息", description = "处理来自生物识别设备的消息")
    public ResponseDTO<String> processDeviceBiometricMessage(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId,
            @RequestBody byte[] rawData) {
        log.info("[设备消息处理] 接口调用: deviceId={}, dataLength={}",
                deviceId, rawData.length);

        return biometricService.processDeviceBiometricMessage(deviceId, rawData);
    }

    /**
     * 批量注册生物识别特征
     */
    @Observed(name = "biometric.batchRegister", contextualName = "biometric-batch-register")
    @PostMapping("/batch-register")
    @Operation(summary = "批量注册", description = "批量注册用户的多个生物识别特征")
    public ResponseDTO<Void> batchRegisterBiometric(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull Long userId,
            @Valid @RequestBody List<BiometricService.BiometricRegisterRequest> requestList) {
        log.info("[批量注册] 接口调用: userId={}, count={}", userId, requestList.size());

        return biometricService.batchRegisterBiometric(userId, requestList);
    }

    /**
     * 清理过期数据
     */
    @Observed(name = "biometric.cleanExpiredData", contextualName = "biometric-clean-expired")
    @PostMapping("/clean-expired")
    @Operation(summary = "清理过期数据", description = "清理过期的生物识别数据")
    public ResponseDTO<String> cleanExpiredData() {
        log.info("[清理过期数据] 接口调用");

        return biometricService.cleanExpiredData();
    }

    /**
     * 获取系统统计信息
     */
    @Observed(name = "biometric.getStatistics", contextualName = "biometric-get-statistics")
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取生物识别系统统计信息")
    public ResponseDTO<BiometricDataManager.BiometricDataStatistics> getStatistics() {
        log.debug("[获取统计信息] 接口调用");

        return biometricService.getStatistics();
    }

    /**
     * 获取支持的验证类型列表
     */
    @Observed(name = "biometric.getSupportedTypes", contextualName = "biometric-get-supported-types")
    @GetMapping("/supported-types")
    @Operation(summary = "获取支持的验证类型", description = "获取系统支持的所有生物识别验证类型")
    public ResponseDTO<List<VerifyTypeInfo>> getSupportedTypes() {
        log.debug("[获取支持类型] 接口调用");

        List<VerifyTypeInfo> types = List.of(
                new VerifyTypeInfo(VerifyTypeEnum.FACE, "人脸识别", "高精度人脸特征匹配"),
                new VerifyTypeInfo(VerifyTypeEnum.FINGERPRINT, "指纹识别", "高精度指纹特征匹配"),
                new VerifyTypeInfo(VerifyTypeEnum.IRIS, "虹膜识别", "高精度虹膜特征匹配"),
                new VerifyTypeInfo(VerifyTypeEnum.PALM, "掌纹识别", "高精度掌纹特征匹配"),
                new VerifyTypeInfo(VerifyTypeEnum.FINGER_VEIN, "指静脉识别", "高精度指静脉特征匹配"),
                new VerifyTypeInfo(VerifyTypeEnum.PALM_VEIN, "掌静脉识别", "高精度掌静脉特征匹配")
        );

        return ResponseDTO.ok(types);
    }

    /**
     * 健康检查
     */
    @Observed(name = "biometric.health", contextualName = "biometric-health")
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "生物识别服务健康检查")
    public ResponseDTO<String> health() {
        return ResponseDTO.ok("生物识别服务运行正常");
    }

    // ==================== DTO类 ====================

    /**
     * 生物识别注册请求DTO
     */
    public static class BiometricRegisterRequest {
        @Parameter(description = "特征数据", required = true)
        private byte[] featureData;

        @Parameter(description = "模板数据")
        private byte[] templateData;

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }

        public byte[] getTemplateData() { return templateData; }
        public void setTemplateData(byte[] templateData) { this.templateData = templateData; }
    }

    /**
     * 生物识别验证请求DTO
     */
    public static class BiometricVerifyRequest {
        @Parameter(description = "特征数据", required = true)
        private byte[] featureData;

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }
    }

    /**
     * 生物识别匹配请求DTO
     */
    public static class BiometricMatchRequest {
        @Parameter(description = "特征数据", required = true)
        private byte[] featureData;

        @Parameter(description = "候选用户ID列表", required = true)
        private List<Long> candidateUserIds;

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }

        public List<Long> getCandidateUserIds() { return candidateUserIds; }
        public void setCandidateUserIds(List<Long> candidateUserIds) { this.candidateUserIds = candidateUserIds; }
    }

    /**
     * 验证类型信息DTO
     */
    public static class VerifyTypeInfo {
        @Parameter(description = "验证类型")
        private VerifyTypeEnum verifyType;

        @Parameter(description = "名称")
        private String name;

        @Parameter(description = "描述")
        private String description;

        public VerifyTypeInfo() {}

        public VerifyTypeInfo(VerifyTypeEnum verifyType, String name, String description) {
            this.verifyType = verifyType;
            this.name = name;
            this.description = description;
        }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
