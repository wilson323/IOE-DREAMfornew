package net.lab1024.sa.common.auth.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新令牌请求DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
public class RefreshTokenRequestDTO {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}

