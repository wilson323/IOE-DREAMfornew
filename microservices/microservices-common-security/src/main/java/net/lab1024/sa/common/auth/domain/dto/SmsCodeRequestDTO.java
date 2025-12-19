package net.lab1024.sa.common.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 短信验证码请求DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "短信验证码请求")
public class SmsCodeRequestDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "验证码用途：login,mfa,reset_password,bind_phone", example = "mfa")
    private String purpose;

    @Schema(description = "用户ID（可选，用于安全验证）", example = "12345")
    private Long userId;

    @Schema(description = "设备指纹", example = "device_fingerprint_123")
    private String deviceFingerprint;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;
}