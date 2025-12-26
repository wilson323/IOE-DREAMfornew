package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.domain.form.MultiFactorAuthenticationForm;
import net.lab1024.sa.access.domain.vo.MultiFactorAuthenticationResultVO;
import net.lab1024.sa.access.service.MultiFactorAuthenticationService;
import org.springframework.web.bind.annotation.*;

/**
 * 多因子认证控制器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - RESTful API设计
 * - Controller层负责HTTP响应包装
 * - 使用@Swagger注解标注API文档
 * </p>
 * <p>
 * 核心职责：
 * - 提供多因子认证API接口
 * - 处理HTTP请求和响应
 * - 参数验证和异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/multi-factor")
@Tag(name = "多因子认证管理", description = "提供多因子认证相关接口")
public class MultiFactorAuthenticationController {

    private final MultiFactorAuthenticationService multiFactorAuthenticationService;

    public MultiFactorAuthenticationController(MultiFactorAuthenticationService multiFactorAuthenticationService) {
        this.multiFactorAuthenticationService = multiFactorAuthenticationService;
    }

    /**
     * 执行多因子认证
     *
     * @param form 多因子认证请求表单
     * @return 认证结果
     */
    @PostMapping("/authenticate")
    @Operation(summary = "执行多因子认证", description = "支持人脸+指纹、人脸+IC卡等多种组合认证")
    public ResponseDTO<MultiFactorAuthenticationResultVO> authenticate(@RequestBody MultiFactorAuthenticationForm form) {
        log.info("[多因子认证] 收到认证请求: userId={}, mode={}, factors={}",
                form.getUserId(), form.getAuthenticationMode(), form.getFactors().size());

        try {
            MultiFactorAuthenticationResultVO result = multiFactorAuthenticationService.authenticate(form);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[多因子认证] 认证异常: userId={}", form.getUserId(), e);
            return ResponseDTO.error("MULTI_FACTOR_AUTH_ERROR", "多因子认证失败: " + e.getMessage());
        }
    }

    /**
     * 验证人脸特征
     *
     * @param userId 用户ID
     * @param faceImageData 人脸图像数据（Base64编码）
     * @return 验证结果
     */
    @PostMapping("/verify-face")
    @Operation(summary = "验证人脸特征", description = "单独验证人脸识别")
    public ResponseDTO<Boolean> verifyFace(
            @RequestParam Long userId,
            @RequestParam String faceImageData) {
        log.info("[多因子认证] 人脸验证请求: userId={}", userId);

        try {
            boolean result = multiFactorAuthenticationService.verifyFace(userId, faceImageData);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[多因子认证] 人脸验证异常: userId={}", userId, e);
            return ResponseDTO.error("FACE_VERIFY_ERROR", "人脸验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证指纹特征
     *
     * @param userId 用户ID
     * @param fingerprintData 指纹特征数据（Base64编码）
     * @return 验证结果
     */
    @PostMapping("/verify-fingerprint")
    @Operation(summary = "验证指纹特征", description = "单独验证指纹识别")
    public ResponseDTO<Boolean> verifyFingerprint(
            @RequestParam Long userId,
            @RequestParam String fingerprintData) {
        log.info("[多因子认证] 指纹验证请求: userId={}", userId);

        try {
            boolean result = multiFactorAuthenticationService.verifyFingerprint(userId, fingerprintData.getBytes());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[多因子认证] 指纹验证异常: userId={}", userId, e);
            return ResponseDTO.error("FINGERPRINT_VERIFY_ERROR", "指纹验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户多因子认证配置
     *
     * @param userId 用户ID
     * @return 多因子认证配置
     */
    @GetMapping("/config/{userId}")
    @Operation(summary = "获取用户多因子认证配置", description = "查询用户支持的多因子认证方式")
    public ResponseDTO<Object> getUserMultiFactorConfig(@PathVariable Long userId) {
        log.info("[多因子认证] 查询用户配置: userId={}", userId);

        try {
            Object config = multiFactorAuthenticationService.getUserMultiFactorConfig(userId);
            return ResponseDTO.ok(config);
        } catch (Exception e) {
            log.error("[多因子认证] 查询配置异常: userId={}", userId, e);
            return ResponseDTO.error("GET_CONFIG_ERROR", "获取配置失败: " + e.getMessage());
        }
    }
}
