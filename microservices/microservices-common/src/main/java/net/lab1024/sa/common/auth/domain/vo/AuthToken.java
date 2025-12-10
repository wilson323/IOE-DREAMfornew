package net.lab1024.sa.common.auth.domain.vo;

import lombok.Data;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 认证令牌VO
 * <p>
 * 统一身份认证系统令牌对象
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Builder
@Schema(description = "认证令牌")
public class AuthToken {

    @Schema(description = "访问令牌", example = "jwt-token-uuid")
    private String accessToken;

    @Schema(description = "刷新令牌", example = "refresh-token-uuid")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌过期时间（秒）", example = "3600")
    private Integer expiresIn;

    @Schema(description = "刷新令牌过期时间（秒）", example = "604800")
    private Integer refreshExpiresIn;

    @Schema(description = "令牌颁发时间")
    private LocalDateTime issuedAt;

    @Schema(description = "令牌过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "令牌范围", example = "read write")
    private String scope;
}