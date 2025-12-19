package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 移动端生物识别验证请求
 * <p>
 * 封装移动端生物识别验证的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端生物识别验证请求")
public class MobileBiometricVerificationRequest {

    /**
     * 生物识别类型
     */
    @NotBlank(message = "生物识别类型不能为空")
    @Schema(description = "生物识别类型", example = "FACE", allowableValues = {"FACE", "FINGERPRINT", "VOICE", "IRIS"})
    private String biometricType;

    /**
     * 生物识别数据（Base64编码）
     */
    @NotBlank(message = "生物识别数据不能为空")
    @Schema(description = "生物识别数据（Base64编码）", example = "base64_encoded_biometric_data")
    private String biometricData;
}


