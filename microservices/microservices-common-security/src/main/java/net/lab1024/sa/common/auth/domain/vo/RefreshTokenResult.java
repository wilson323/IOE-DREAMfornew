package net.lab1024.sa.common.auth.domain.vo;

import lombok.Data;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 刷新令牌结果VO
 * <p>
 * 统一身份认证系统令牌刷新结果返回对象
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Builder
@Schema(description = "刷新令牌结果")
public class RefreshTokenResult {

    @Schema(description = "刷新是否成功", example = "true")
    private Boolean success;

    @Schema(description = "错误码", example = "INVALID_REFRESH_TOKEN")
    private String errorCode;

    @Schema(description = "错误信息", example = "无效的刷新令牌")
    private String errorMessage;

    @Schema(description = "新的访问令牌", example = "new-jwt-token-uuid")
    private String accessToken;

    @Schema(description = "新的刷新令牌", example = "new-refresh-token-uuid")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌过期时间（秒）", example = "3600")
    private Integer expiresIn;

    @Schema(description = "刷新令牌过期时间（秒）", example = "604800")
    private Integer refreshExpiresIn;
}