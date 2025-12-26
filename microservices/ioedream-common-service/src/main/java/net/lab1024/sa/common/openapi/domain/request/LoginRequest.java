package net.lab1024.sa.common.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Schema(description = "用户名", example = "admin", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Schema(description = "密码", example = "123456", required = true)
    private String password;

    @Schema(description = "手机号（短信登录时使用）", example = "13800138000")
    private String phone;

    @Schema(description = "验证码", example = "1234")
    private String captcha;

    @Schema(description = "验证码KEY", example = "captcha_key_123")
    private String captchaKey;

    @Schema(description = "登录类型", example = "password", allowableValues = {"password", "sms", "oauth"})
    private String loginType = "password";

    @Schema(description = "设备信息", example = "Chrome/Windows 10")
    private String userAgent;

    @Schema(description = "设备ID", example = "device_123456")
    private String deviceId;

    @Schema(description = "是否记住登录状态", example = "false")
    private Boolean rememberMe = false;

    @Schema(description = "第三方登录令牌（OAuth登录时使用）", example = "oauth_token_123")
    private String thirdPartyToken;

    @Schema(description = "第三方平台类型", example = "wechat", allowableValues = {"wechat", "dingtalk", "feishu"})
    private String thirdPartyType;
}
