package net.lab1024.sa.common.auth.domain.vo;

import lombok.Data;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 令牌验证结果VO
 * <p>
 * 统一身份认证系统令牌验证结果返回对象
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Builder
@Schema(description = "令牌验证结果")
public class TokenValidationResult {

    @Schema(description = "令牌是否有效", example = "true")
    private Boolean valid;

    @Schema(description = "错误码", example = "TOKEN_EXPIRED")
    private String errorCode;

    @Schema(description = "错误信息", example = "令牌已过期")
    private String errorMessage;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "登录名", example = "admin")
    private String loginName;

    @Schema(description = "用户姓名", example = "管理员")
    private String userName;

    @Schema(description = "令牌声明")
    private Map<String, Object> claims;

    @Schema(description = "令牌剩余有效时间（秒）", example = "1800")
    private Long remainingTime;

    @Schema(description = "令牌过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "验证时间")
    private LocalDateTime validateTime;
}