package net.lab1024.sa.common.auth.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 短信登录请求DTO
 * <p>
 * 统一身份认证系统短信登录请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Schema(description = "短信登录请求参数")
public class SmsLoginRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000", required = true)
    private String phone;

    @NotBlank(message = "短信验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "短信验证码必须为6位数字")
    @Schema(description = "短信验证码", example = "123456", required = true)
    private String smsCode;

    @Schema(description = "设备ID", example = "device-uuid")
    private String deviceId;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    private String userAgent;

    @Schema(description = "是否记住登录", example = "false")
    private Boolean rememberMe = false;
}