package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 移动端登录结果
 * <p>
 * 封装移动端用户登录的响应结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "移动端登录结果")
public class MobileLoginResult {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private boolean success;

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌", example = "refresh_token_123")
    private String refreshToken;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private MobileUserInfo userInfo;

    /**
     * 过期时间（秒）
     */
    @Schema(description = "过期时间（秒）", example = "3600")
    private long expiresIn;

    /**
     * 消息
     */
    @Schema(description = "消息", example = "登录成功")
    private String message;

    /**
     * 错误码
     */
    @Schema(description = "错误码", example = "LOGIN_FAILED")
    private String errorCode;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳", example = "1703020800000")
    private long timestamp;
}