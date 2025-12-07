package net.lab1024.sa.common.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * 整合自ioedream-auth-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自auth-service）
 */
@Data
@Schema(description = "登录请求")
public class LoginRequestDTO {

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "验证码", example = "1234")
    private String captcha;

    @Schema(description = "验证码Key", example = "captcha:xxx")
    private String captchaKey;

    @Schema(description = "设备信息", example = "Chrome/Windows")
    private String deviceInfo;

    @Schema(description = "登录IP", example = "192.168.1.1")
    private String loginIp;

    @Schema(description = "记住我", example = "false")
    private Boolean rememberMe = false;
}
