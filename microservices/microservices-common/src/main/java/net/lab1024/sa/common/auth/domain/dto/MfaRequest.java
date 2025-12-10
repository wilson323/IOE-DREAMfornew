package net.lab1024.sa.common.auth.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 多因素认证请求DTO
 * <p>
 * 统一身份认证系统多因素认证请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Schema(description = "多因素认证请求参数")
public class MfaRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001", required = true)
    private Long userId;

    @NotBlank(message = "MFA类型不能为空")
    @Schema(description = "MFA类型", example = "SMS", allowableValues = {"SMS", "EMAIL", "BIOMETRIC", "TOTP"}, required = true)
    private String mfaType;

    @Schema(description = "验证码", example = "123456")
    private String verifyCode;

    @Schema(description = "生物特征数据", example = "biometric-data-hash")
    private String biometricData;

    @Schema(description = "TOTP代码", example = "123456")
    private String totpCode;

    @Schema(description = "邮箱验证码", example = "ABC123")
    private String emailCode;

    @Schema(description = "设备ID", example = "device-uuid")
    private String deviceId;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;
}