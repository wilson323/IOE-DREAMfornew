package net.lab1024.sa.access.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.access.domain.entity.BiometricDataEntity;
import net.lab1024.sa.access.domain.vo.BiometricEnrollRequestVO;
import net.lab1024.sa.access.domain.vo.BiometricMatchResultVO;
import net.lab1024.sa.access.service.BiometricService;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 生物识别移动端控制器
 * <p>
 * 提供移动端生物识别相关API接口，包括：
 * - 人脸识别
 * - 指纹识别
 * - 虹膜识别
 * - 生物特征注册
 * - 移动端验证
 * </p>
 * 严格遵循repowiki编码规范：
 * - 使用jakarta包名
 * - 使用@Resource依赖注入
 * - RESTful API设计
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@RestController
@RequestMapping("/biometric/mobile")
@Tag(name = "生物识别移动端", description = "移动端生物识别API接口")
public class BiometricMobileController {

    @Resource
    private BiometricService biometricService;

    @Operation(summary = "人脸识别验证", description = "通过移动端上传人脸进行身份验证")
    @PostMapping("/face/verify")
    public ResponseDTO<BiometricMatchResultVO> verifyFace(
            @Parameter(description = "人脸图片文件", required = true) @RequestParam("faceImage") MultipartFile faceImage,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {

        BiometricMatchResultVO result = biometricService.verifyFaceByMobile(faceImage, userId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "指纹识别验证", description = "通过移动端上传指纹进行身份验证")
    @PostMapping("/fingerprint/verify")
    public ResponseDTO<BiometricMatchResultVO> verifyFingerprint(
            @Parameter(description = "指纹特征数据", required = true) @RequestParam String fingerprintData,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {

        BiometricMatchResultVO result = biometricService.verifyFingerprintByMobile(fingerprintData, userId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "虹膜识别验证", description = "通过移动端上传虹膜进行身份验证")
    @PostMapping("/iris/verify")
    public ResponseDTO<BiometricMatchResultVO> verifyIris(
            @Parameter(description = "虹膜图片文件", required = true) @RequestParam("irisImage") MultipartFile irisImage,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {

        BiometricMatchResultVO result = biometricService.verifyIrisByMobile(irisImage, userId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "多模态生物识别验证", description = "结合多种生物特征进行身份验证")
    @PostMapping("/multimodal/verify")
    public ResponseDTO<BiometricMatchResultVO> verifyMultiModal(
            @Parameter(description = "人脸图片文件") @RequestParam(value = "faceImage", required = false) MultipartFile faceImage,
            @Parameter(description = "指纹特征数据") @RequestParam(value = "fingerprintData", required = false) String fingerprintData,
            @Parameter(description = "虹膜图片文件") @RequestParam(value = "irisImage", required = false) MultipartFile irisImage,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {

        BiometricMatchResultVO result = biometricService.verifyMultiModalByMobile(
                faceImage, fingerprintData, irisImage, userId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "注册人脸特征", description = "在移动端注册用户人脸特征")
    @PostMapping("/face/enroll")
    public ResponseDTO<String> enrollFace(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "人脸图片文件", required = true) @RequestParam("faceImage") MultipartFile faceImage) {

        String templateId = biometricService.enrollFaceByMobile(userId, faceImage);
        return ResponseDTO.ok(templateId);
    }

    @Operation(summary = "注册指纹特征", description = "在移动端注册用户指纹特征")
    @PostMapping("/fingerprint/enroll")
    public ResponseDTO<String> enrollFingerprint(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "指纹特征数据", required = true) @RequestParam String fingerprintData,
            @Parameter(description = "手指类型 1-拇指 2-食指 3-中指 4-无名指 5-小指") @RequestParam Integer fingerType) {

        String templateId = biometricService.enrollFingerprintByMobile(userId, fingerprintData, fingerType);
        return ResponseDTO.ok(templateId);
    }

    @Operation(summary = "注册虹膜特征", description = "在移动端注册用户虹膜特征")
    @PostMapping("/iris/enroll")
    public ResponseDTO<String> enrollIris(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "虹膜图片文件", required = true) @RequestParam("irisImage") MultipartFile irisImage,
            @Parameter(description = "眼睛类型 1-左眼 2-右眼") @RequestParam Integer eyeType) {

        String templateId = biometricService.enrollIrisByMobile(userId, irisImage, eyeType);
        return ResponseDTO.ok(templateId);
    }

    @Operation(summary = "批量注册生物特征", description = "批量注册用户多种生物特征")
    @PostMapping("/batch/enroll")
    public ResponseDTO<Map<String, String>> batchEnroll(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "生物特征注册请求", required = true) @Valid @ModelAttribute BiometricEnrollRequestVO enrollRequest) {

        Map<String, String> templateIds = biometricService.batchEnrollByMobile(userId, enrollRequest);
        return ResponseDTO.ok(templateIds);
    }

    @Operation(summary = "查询用户生物特征", description = "查询指定用户的注册生物特征信息")
    @GetMapping("/user/{userId}")
    public ResponseDTO<List<BiometricDataEntity>> getUserBiometrics(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {

        List<BiometricDataEntity> biometrics = biometricService.getUserBiometrics(userId);
        return ResponseDTO.ok(biometrics);
    }

    @Operation(summary = "删除生物特征", description = "删除指定的生物特征模板")
    @DeleteMapping("/template/{templateId}")
    public ResponseDTO<Boolean> deleteBiometric(
            @Parameter(description = "模板ID", required = true) @PathVariable Long templateId) {

        boolean result = biometricService.deleteBiometricTemplate(templateId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "更新生物特征", description = "更新指定的生物特征模板")
    @PutMapping("/template/{templateId}")
    public ResponseDTO<Boolean> updateBiometric(
            @Parameter(description = "模板ID", required = true) @PathVariable Long templateId,
            @Parameter(description = "生物特征数据") @RequestParam String biometricData) {

        boolean result = biometricService.updateBiometricTemplate(templateId, biometricData);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "生物特征质量检测", description = "检测上传的生物特征数据质量")
    @PostMapping("/quality/check")
    public ResponseDTO<Map<String, Object>> checkQuality(
            @Parameter(description = "生物特征类型", required = true) @RequestParam String biometricType,
            @Parameter(description = "生物特征文件") @RequestParam(value = "biometricFile", required = false) MultipartFile biometricFile,
            @Parameter(description = "生物特征数据") @RequestParam(value = "biometricData", required = false) String biometricData) {

        Map<String, Object> qualityResult = biometricService.checkBiometricQuality(
                biometricType, biometricFile, biometricData);
        return ResponseDTO.ok(qualityResult);
    }

    @Operation(summary = "获取识别统计", description = "获取用户生物识别的统计数据")
    @GetMapping("/statistics/{userId}")
    public ResponseDTO<Map<String, Object>> getBiometricStatistics(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "30") Integer days) {

        Map<String, Object> statistics = biometricService.getBiometricStatistics(userId, days);
        return ResponseDTO.ok(statistics);
    }

    @Operation(summary = "活体检测", description = "进行人脸活体检测，防止照片攻击")
    @PostMapping("/liveness/detect")
    public ResponseDTO<Map<String, Object>> livenessDetection(
            @Parameter(description = "人脸图片文件", required = true) @RequestParam("faceImage") MultipartFile faceImage,
            @Parameter(description = "活体检测类型 1-眨眼 2-摇头 3-张嘴 4-综合") @RequestParam(defaultValue = "4") Integer detectType) {

        Map<String, Object> livenessResult = biometricService.performLivenessDetection(faceImage, detectType);
        return ResponseDTO.ok(livenessResult);
    }

    @Operation(summary = "获取移动端配置", description = "获取移动端生物识别相关配置信息")
    @GetMapping("/config")
    public ResponseDTO<Map<String, Object>> getMobileConfig() {
        Map<String, Object> config = biometricService.getMobileBiometricConfig();
        return ResponseDTO.ok(config);
    }
}
