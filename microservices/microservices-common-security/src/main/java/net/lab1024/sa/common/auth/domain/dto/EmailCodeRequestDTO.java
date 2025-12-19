package net.lab1024.sa.common.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 邮箱验证码请求DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "邮箱验证码请求")
public class EmailCodeRequestDTO {

    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "user@example.com")
    private String email;

    @Schema(description = "验证码用途：login,mfa,reset_password,bind_email", example = "mfa")
    private String purpose;

    @Schema(description = "用户ID（可选，用于安全验证）", example = "12345")
    private Long userId;

    @Schema(description = "设备指纹", example = "device_fingerprint_123")
    private String deviceFingerprint;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "邮件主题模板", example = "IOE-DREAM MFA验证码")
    private String subjectTemplate;
}