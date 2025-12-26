package net.lab1024.sa.common.auth.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
public class LoginRequestDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    private String phone;
    private String captcha;
    private String captchaKey;
    private String loginType = "password";
    private String userAgent;
    private String deviceId;
    private String deviceInfo; // 设备信息（JSON格式）
    private Boolean rememberMe = false;
    private String thirdPartyToken;
    private String thirdPartyType;
    private String loginIp;
}

