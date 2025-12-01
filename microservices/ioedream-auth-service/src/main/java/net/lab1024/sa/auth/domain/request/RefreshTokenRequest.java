package net.lab1024.sa.auth.domain.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新令牌请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Data
public class RefreshTokenRequest {

    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}