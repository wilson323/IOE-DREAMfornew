package net.lab1024.sa.auth.domain.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Data
public class LoginRequest {

    /**
     * 用户名或手机号
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码key
     */
    private String captchaKey;

    /**
     * 记住我
     */
    private Boolean rememberMe = false;

    /**
     * 登录类型: password, sms, biometric
     */
    private String loginType = "password";

    /**
     * 设备信息
     */
    private String deviceInfo;
}