package net.lab1024.sa.common.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 令牌验证响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "令牌验证响应")
public class TokenValidationResponse {

    @Schema(description = "是否有效", example = "true")
    private Boolean valid;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "过期时间", example = "2025-12-16T17:30:00")
    private LocalDateTime expireTime;

    @Schema(description = "剩余有效时间（秒）", example = "3600")
    private Long remainingTime;

    @Schema(description = "令牌类型", example = "access_token")
    private String tokenType;

    @Schema(description = "签发时间", example = "2025-12-16T15:30:00")
    private LocalDateTime issueTime;

    @Schema(description = "验证失败原因", example = "令牌已过期")
    private String errorMessage;
}