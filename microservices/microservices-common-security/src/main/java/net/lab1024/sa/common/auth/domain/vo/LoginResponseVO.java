package net.lab1024.sa.common.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户登录响应VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseVO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Long refreshExpiresIn;
    private LocalDateTime accessTokenExpireTime;
    private LocalDateTime refreshTokenExpireTime;
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private UserInfoVO userInfo;
    private List<String> permissions;
    private List<String> roles;
}

