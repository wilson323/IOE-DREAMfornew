package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 移动端认证初始化响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端认证初始化响应")
public class MobileAuthInitVO {

    @Schema(description = "设备ID", example = "MOBILE_001")
    private String deviceId;

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌过期时间（秒）", example = "7200")
    private Long expiresIn;

    @Schema(description = "刷新令牌过期时间（秒）", example = "604800")
    private Long refreshExpiresIn;

    @Schema(description = "服务器时间戳", example = "1705880400000")
    private Long serverTimestamp;

    @Schema(description = "服务器公钥（用于设备验证）")
    private String serverPublicKey;

    @Schema(description = "挑战字符串（用于设备认证）")
    private String challenge;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "用户名", example = "张三")
        private String username;

        @Schema(description = "真实姓名", example = "张三")
        private String realName;

        @Schema(description = "手机号", example = "13800138000")
        private String phone;

        @Schema(description = "邮箱", example = "zhangsan@example.com")
        private String email;
    }
}
