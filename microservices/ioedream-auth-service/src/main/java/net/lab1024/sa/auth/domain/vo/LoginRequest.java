package net.lab1024.sa.auth.domain.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求VO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
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

    /**
     * 登录IP
     */
    private String loginIp;
}
