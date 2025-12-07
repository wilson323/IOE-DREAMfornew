package net.lab1024.sa.common.auth.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT令牌工具类
 * 整合自ioedream-auth-service
 *
 * 功能：
 * - 生成访问令牌和刷新令牌
 * - 验证令牌有效性
 * - 解析令牌信息
 * - 管理令牌生命周期
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自auth-service）
 */
@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${auth.jwt.secret:ioedream-jwt-secret-key-2025-must-be-at-least-256-bits}")
    private String secret;

    @Value("${auth.jwt.access-token-expiration:86400}")
    private Long accessTokenExpiration;

    @Value("${auth.jwt.refresh-token-expiration:604800}")
    private Long refreshTokenExpiration;

    private static final String CLAIM_KEY_USER_ID = "userId";
    private static final String CLAIM_KEY_USERNAME = "username";
    private static final String CLAIM_KEY_ROLES = "roles";
    private static final String CLAIM_KEY_PERMISSIONS = "permissions";
    private static final String CLAIM_KEY_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long userId, String username, List<String> roles, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USER_ID, userId);
        claims.put(CLAIM_KEY_USERNAME, username);
        claims.put(CLAIM_KEY_ROLES, roles);
        claims.put(CLAIM_KEY_PERMISSIONS, permissions);
        claims.put(CLAIM_KEY_TOKEN_TYPE, TOKEN_TYPE_ACCESS);

        return generateToken(claims, accessTokenExpiration);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USER_ID, userId);
        claims.put(CLAIM_KEY_USERNAME, username);
        claims.put(CLAIM_KEY_TOKEN_TYPE, TOKEN_TYPE_REFRESH);

        return generateToken(claims, refreshTokenExpiration);
    }

    /**
     * 生成令牌
     * <p>
     * 使用JWT 0.13.0最新API：
     * - 使用SecretKey替代Key和SignatureAlgorithm
     * - 使用Keys.hmacShaKeyFor()生成SecretKey
     * - 使用.signWith(SecretKey)方法
     * </p>
     *
     * @param claims     令牌声明
     * @param expiration 过期时间（秒）
     * @return JWT令牌字符串
     */
    private String generateToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        // 使用JWT 0.13.0 API：生成SecretKey
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

        // 使用JWT 0.13.0 API：构建令牌
        return Jwts.builder()
                .claims(claims) // 0.13.0使用claims()而不是setClaims()
                .issuedAt(now) // 0.13.0使用issuedAt()而不是setIssuedAt()
                .expiration(expiryDate) // 0.13.0使用expiration()而不是setExpiration()
                .signWith(secretKey) // 0.13.0直接传入SecretKey，无需指定算法
                .compact();
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("令牌验证失败", e);
            return false;
        }
    }

    /**
     * 检查令牌是否过期
     */
    private boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从令牌中获取Claims
     * <p>
     * 使用JWT 0.13.0最新API：
     * - 使用SecretKey替代Key
     * - 使用parser()和verifyWith()方法
     * </p>
     *
     * @param token JWT令牌字符串
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        try {
            // 使用JWT 0.13.0 API：生成SecretKey
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
            
            // 使用JWT 0.13.0 API：解析令牌
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析令牌失败", e);
            return null;
        }
    }

    /**
     * 获取令牌过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 获取剩余有效时间（秒）
     */
    public Long getRemainingExpiration(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return 0L;
            }
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(remaining, 0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object userIdObj = claims.get(CLAIM_KEY_USER_ID);
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get(CLAIM_KEY_USERNAME) : null;
    }

    /**
     * 从令牌中获取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (List<String>) claims.get(CLAIM_KEY_ROLES) : null;
    }

    /**
     * 从令牌中获取权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (List<String>) claims.get(CLAIM_KEY_PERMISSIONS) : null;
    }

    /**
     * 判断是否为访问令牌
     */
    public boolean isAccessToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        String tokenType = (String) claims.get(CLAIM_KEY_TOKEN_TYPE);
        return TOKEN_TYPE_ACCESS.equals(tokenType);
    }

    /**
     * 判断是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        String tokenType = (String) claims.get(CLAIM_KEY_TOKEN_TYPE);
        return TOKEN_TYPE_REFRESH.equals(tokenType);
    }
}
