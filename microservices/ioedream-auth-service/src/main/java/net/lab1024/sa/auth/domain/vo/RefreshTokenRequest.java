package net.lab1024.sa.auth.domain.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求VO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
public class RefreshTokenRequest {

    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
