package net.lab1024.sa.common.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 刷新令牌响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新令牌响应")
public class RefreshTokenResponse {

    @Schema(description = "新的访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "新的刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌过期时间（秒）", example = "7200")
    private Long expiresIn;

    @Schema(description = "访问令牌过期时间", example = "2025-12-16T15:30:00")
    private LocalDateTime accessTokenExpireTime;

    @Schema(description = "刷新令牌过期时间", example = "2025-12-30T15:30:00")
    private LocalDateTime refreshTokenExpireTime;
}