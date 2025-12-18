package net.lab1024.sa.biometric.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.domain.vo.FeatureVector;
import net.lab1024.sa.biometric.service.BiometricFeatureExtractionService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;

/**
 * 生物特征提取控制器
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - RESTful API设计
 * - 统一的ResponseDTO响应格式
 * </p>
 * <p>
 * ⚠️ 重要说明:
 * - 只在用户入职/更新模板时调用
 * - 不用于实时识别（实时识别由设备端完成）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/biometric/feature")
@Tag(name = "生物特征提取", description = "生物特征提取API")
@Validated
public class BiometricFeatureExtractionController {

    @Resource
    private BiometricFeatureExtractionService biometricFeatureExtractionService;

    /**
     * 提取人脸特征
     */
    @PostMapping("/face")
    @Operation(summary = "提取人脸特征", description = "从用户上传的人脸照片提取512维特征向量")
    public ResponseDTO<FeatureVector> extractFaceFeature(
            @RequestParam("photo") @NotNull MultipartFile photo) {
        return biometricFeatureExtractionService.extractFaceFeature(photo);
    }

    /**
     * 提取指纹特征
     */
    @PostMapping("/fingerprint")
    @Operation(summary = "提取指纹特征", description = "从用户上传的指纹图像提取特征向量")
    public ResponseDTO<FeatureVector> extractFingerprintFeature(
            @RequestParam("fingerprintImage") @NotNull MultipartFile fingerprintImage) {
        return biometricFeatureExtractionService.extractFingerprintFeature(fingerprintImage);
    }

    /**
     * 提取虹膜特征
     */
    @PostMapping("/iris")
    @Operation(summary = "提取虹膜特征", description = "从用户上传的虹膜图像提取特征向量")
    public ResponseDTO<FeatureVector> extractIrisFeature(
            @RequestParam("irisImage") @NotNull MultipartFile irisImage) {
        return biometricFeatureExtractionService.extractIrisFeature(irisImage);
    }

    /**
     * 提取掌纹特征
     */
    @PostMapping("/palm")
    @Operation(summary = "提取掌纹特征", description = "从用户上传的掌纹图像提取特征向量")
    public ResponseDTO<FeatureVector> extractPalmFeature(
            @RequestParam("palmImage") @NotNull MultipartFile palmImage) {
        return biometricFeatureExtractionService.extractPalmFeature(palmImage);
    }

    /**
     * 提取声纹特征
     */
    @PostMapping("/voice")
    @Operation(summary = "提取声纹特征", description = "从用户上传的语音文件提取特征向量")
    public ResponseDTO<FeatureVector> extractVoiceFeature(
            @RequestParam("voiceFile") @NotNull MultipartFile voiceFile) {
        return biometricFeatureExtractionService.extractVoiceFeature(voiceFile);
    }

    /**
     * 通用特征提取
     */
    @PostMapping("/extract")
    @Operation(summary = "通用特征提取", description = "根据生物识别类型自动选择对应的提取策略")
    public ResponseDTO<FeatureVector> extractFeature(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("biometricType") @NotNull Integer biometricType) {
        return biometricFeatureExtractionService.extractFeature(file, biometricType);
    }

    /**
     * 验证特征质量
     */
    @PostMapping("/validate")
    @Operation(summary = "验证特征质量", description = "检查提取的特征向量是否符合质量要求")
    public ResponseDTO<Boolean> validateFeatureQuality(@RequestBody FeatureVector featureVector) {
        return biometricFeatureExtractionService.validateFeatureQuality(featureVector);
    }
}
