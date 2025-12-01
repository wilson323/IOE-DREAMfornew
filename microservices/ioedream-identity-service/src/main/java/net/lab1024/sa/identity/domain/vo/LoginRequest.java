package net.lab1024.sa.identity.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求VO
 * 基于现有登录模式重构
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    /**
     * 用户名（基于原username字段）
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 登录IP（基于原登录模式）
     */
    @Schema(description = "登录IP", example = "192.168.1.100")
    private String loginIp;

    /**
     * 用户代理
     */
    @Schema(description = "用户代理", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

    /**
     * 验证码（基于原有验证码模式）
     */
    @Schema(description = "验证码", example = "1234")
    private String captcha;

    /**
     * 验证码key
     */
    @Schema(description = "验证码key", example = "captcha_key_123")
    private String captchaKey;

    /**
     * 记住我
     */
    @Schema(description = "记住我", example = "false")
    private Boolean rememberMe = false;

    // 兼容性字段，保持与原有登录模式的兼容

    /**
     * 登录类型（兼容性字段）
     */
    @Schema(description = "登录类型", example = "password")
    private String loginType = "password";

    /**
     * 设备信息（兼容性字段）
     */
    @Schema(description = "设备信息", example = "PC")
    private String deviceInfo;

    /**
     * 默认构造函数
     */
    public LoginRequest() {
    }

    /**
     * 全参数构造函数（兼容性）
     */
    public LoginRequest(String username, String password, String loginIp, String userAgent) {
        this.username = username;
        this.password = password;
        this.loginIp = loginIp;
        this.userAgent = userAgent;
    }

    /**
     * 获取登录IP，如果为空则返回默认值
     */
    public String getLoginIpOrDefault() {
        return loginIp != null ? loginIp : "127.0.0.1";
    }

    /**
     * 获取用户代理，如果为空则返回默认值
     */
    public String getUserAgentOrDefault() {
        return userAgent != null ? userAgent : "Unknown";
    }

    /**
     * 是否需要验证码（基于原有验证码逻辑）
     */
    public boolean needsCaptcha() {
        return captcha != null && !captcha.trim().isEmpty();
    }

    /**
     * 是否记住登录（基于原记住我逻辑）
     */
    public boolean isRememberMe() {
        return rememberMe != null && rememberMe;
    }
}
