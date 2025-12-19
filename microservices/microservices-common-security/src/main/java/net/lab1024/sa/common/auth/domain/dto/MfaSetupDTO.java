package net.lab1024.sa.common.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.util.List;

/**
 * MFA设置信息DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "MFA设置信息")
public class MfaSetupDTO {

    @Schema(description = "MFA密钥")
    private String secret;

    @Schema(description = "二维码URL")
    private String qrCodeUrl;

    @Schema(description = "备用恢复码列表")
    private List<String> backupCodes;

    @Schema(description = "是否已启用MFA")
    private Boolean enabled;

    @Schema(description = "MFA类型：totp,sms,email,biometric")
    private String mfaType;

    @Schema(description = "关联的手机号（脱敏）")
    private String maskedPhone;

    @Schema(description = "关联的邮箱（脱敏）")
    private String maskedEmail;
}