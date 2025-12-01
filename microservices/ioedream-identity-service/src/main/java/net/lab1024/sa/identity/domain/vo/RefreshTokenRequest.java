package net.lab1024.sa.identity.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * 刷新令牌请求VO
 * 基于现有令牌刷新模式重构
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
@Builder
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {

    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    /**
     * 用户ID（可选，用于验证）
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 设备信息（基于原设备模式）
     */
    @Schema(description = "设备信息", example = "PC")
    private String deviceInfo;

    /**
     * 用户代理
     */
    @Schema(description = "用户代理", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

    /**
     * 默认构造函数
     */
    public RefreshTokenRequest() {
    }

    /**
     * 全参数构造函数
     */
    public RefreshTokenRequest(String refreshToken, Long userId, String deviceInfo, String userAgent) {
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.deviceInfo = deviceInfo;
        this.userAgent = userAgent;
    }

    /**
     * 验证刷新令牌格式
     */
    public boolean isValidToken() {
        return refreshToken != null && !refreshToken.trim().isEmpty()
                && refreshToken.length() > 20; // 基本的长度验证
    }

    /**
     * 获取设备信息，如果为空则返回默认值
     */
    public String getDeviceInfoOrDefault() {
        return deviceInfo != null ? deviceInfo : "Unknown";
    }

    /**
     * 获取用户代理，如果为空则返回默认值
     */
    public String getUserAgentOrDefault() {
        return userAgent != null ? userAgent : "Unknown";
    }
}
