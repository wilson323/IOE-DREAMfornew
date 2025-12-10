package net.lab1024.sa.common.auth.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录请求DTO
 * <p>
 * 统一身份认证系统登录请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Schema(description = "登录请求参数")
public class LoginRequest {

    @NotBlank(message = "登录名不能为空")
    @Size(max = 50, message = "登录名长度不能超过50个字符")
    @Schema(description = "登录名", example = "admin", required = true)
    private String loginName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Schema(description = "密码", example = "123456", required = true)
    private String password;

    @Schema(description = "验证码", example = "ABCD")
    private String captchaCode;

    @Schema(description = "验证码UUID", example = "uuid-string")
    private String captchaUuid;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备ID", example = "device-uuid")
    private String deviceId;

    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    private String userAgent;

    @Schema(description = "登录类型", example = "PASSWORD", allowableValues = {"PASSWORD", "SMS", "EMAIL", "BIOMETRIC", "TOTP"})
    private String loginType = "PASSWORD";

    @Schema(description = "是否记住登录", example = "false")
    private Boolean rememberMe = false;
}