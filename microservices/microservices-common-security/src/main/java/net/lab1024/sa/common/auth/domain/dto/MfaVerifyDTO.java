package net.lab1024.sa.common.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

/**
 * MFA验证请求DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "MFA验证请求")
public class MfaVerifyDTO {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "12345")
    private Long userId;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "123456")
    private String code;

    @NotBlank(message = "MFA类型不能为空")
    @Schema(description = "MFA类型：totp,sms,email,biometric", example = "totp")
    private String mfaType;

    @Schema(description = "设备指纹", example = "device_fingerprint_123")
    private String deviceFingerprint;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;
}