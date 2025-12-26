package net.lab1024.sa.common.auth.util;

import lombok.extern.slf4j.Slf4j;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT令牌工具类
 * <p>
 * 提供JWT令牌的生成、验证、解析等功能
 * 支持访问令牌和刷新令牌的区分管理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-20
 */
@Component
@Slf4j
public class JwtTokenUtil {

    @Value("${jwt.secret:ioedream-jwt-secret-key-2025}")
    private String secret;

    @Value("${jwt.expiration:7200}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration;

    @Resource
    private net.lab1024.sa.common.auth.service.JwtTokenBlacklistService blacklistService;

    private SecretKey secretKey;

    public JwtTokenUtil() {
    }

    public JwtTokenUtil(String secret, Long expiration, Long refreshExpiration) {
        this.secret = secret;
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    private SecretKey getSecretKey() {
        if (secretKey == null) {
            String keyString = secret;
            if (keyString.length() < 32) {
                keyString = String.format("%-32s", keyString).replace(' ', '0');
            }
            byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
            secretKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return secretKey;
    }

    public String generateToken(Long userId, String username, Integer expireMinutes) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("username", username);
            claims.put("tokenType", "access");

            long expireTime = System.currentTimeMillis()
                    + (expireMinutes != null ? expireMinutes * 60 * 1000L : expiration * 1000L);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId.toString())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(expireTime))
                    .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            log.error("[JWT令牌] 生成令牌失败: userId={}, username={}", userId, username, e);
            return null;
        }
    }

    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, (int) (expiration / 60));
    }

    /**
     * 生成访问令牌（带角色和权限）
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param roles 角色列表
     * @param permissions 权限列表
     * @return 访问令牌
     */
    public String generateAccessToken(Long userId, String username, List<String> roles, List<String> permissions) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("username", username);
            claims.put("tokenType", "access");
            if (roles != null && !roles.isEmpty()) {
                claims.put("roles", roles);
            }
            if (permissions != null && !permissions.isEmpty()) {
                claims.put("permissions", permissions);
            }

            long expireTime = System.currentTimeMillis() + expiration * 1000L;

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId.toString())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(expireTime))
                    .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            log.error("[JWT令牌] 生成访问令牌失败: userId={}, username={}", userId, username, e);
            return null;
        }
    }

    public String generateRefreshToken(Long userId) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("tokenType", "refresh");

            long expireTime = System.currentTimeMillis() + refreshExpiration * 1000L;

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId.toString())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(expireTime))
                    .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            log.error("[JWT令牌] 生成刷新令牌失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 生成刷新令牌（带用户名）
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("username", username);
            claims.put("tokenType", "refresh");

            long expireTime = System.currentTimeMillis() + refreshExpiration * 1000L;

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId.toString())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(expireTime))
                    .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            log.error("[JWT令牌] 生成刷新令牌失败: userId={}, username={}", userId, username, e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            // 1. 先检查黑名单
            if (blacklistService != null && blacklistService.isTokenBlacklisted(token)) {
                log.warn("[JWT令牌] 令牌已在黑名单中: token={}", maskToken(token));
                return false;
            }

            // 2. 验证令牌签名和有效期
            Jwts.parser()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.debug("[JWT令牌] 令牌验证失败: token={}, error={}", token, e.getMessage());
            return false;
        }
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.debug("[JWT令牌] 令牌解析失败: token={}, error={}", token, e.getMessage());
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                } else if (userIdObj instanceof String) {
                    return Long.parseLong((String) userIdObj);
                }
            }
            return null;
        } catch (Exception e) {
            log.debug("[JWT令牌] 获取用户ID失败: token={}", token, e);
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null ? (String) claims.get("username") : null;
        } catch (Exception e) {
            log.debug("[JWT令牌] 获取用户名失败: token={}", token, e);
            return null;
        }
    }

    public String getTokenTypeFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null ? (String) claims.get("tokenType") : "unknown";
        } catch (Exception e) {
            log.debug("[JWT令牌] 获取令牌类型失败: token={}", token, e);
            return "unknown";
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.debug("[JWT令牌] 检查令牌过期失败: token={}", token, e);
            return true;
        }
    }

    public long getRemainingTime(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                Date expirationDate = claims.getExpiration();
                long remaining = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
                return Math.max(0, remaining);
            }
            return 0;
        } catch (Exception e) {
            log.debug("[JWT令牌] 获取剩余时间失败: token={}", token, e);
            return 0;
        }
    }

    /**
     * 获取令牌剩余过期时间（秒）
     * 别名方法，与getRemainingTime功能相同
     *
     * @param token 令牌
     * @return 剩余过期时间（秒）
     */
    public long getRemainingExpiration(String token) {
        return getRemainingTime(token);
    }

    public String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                Long userId = getUserIdFromToken(token);
                String username = (String) claims.get("username");
                return generateAccessToken(userId, username);
            }
            return null;
        } catch (Exception e) {
            log.error("[JWT令牌] 刷新令牌失败: token={}", token, e);
            return null;
        }
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public String generateBearerToken(String token) {
        return "Bearer " + token;
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                String tokenType = (String) claims.get("tokenType");
                return "access".equals(tokenType);
            }
            return false;
        } catch (Exception e) {
            log.debug("[JWT令牌] 检查令牌类型失败: token={}", token, e);
            return false;
        }
    }

    /**
     * 检查是否为刷新令牌
     *
     * @param token 令牌
     * @return 是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                String tokenType = (String) claims.get("tokenType");
                return "refresh".equals(tokenType);
            }
            return false;
        } catch (Exception e) {
            log.debug("[JWT令牌] 检查令牌类型失败: token={}", token, e);
            return false;
        }
    }

    public Map<String, Object> parseJwtTokenToMap(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                Map<String, Object> result = new HashMap<>();
                claims.forEach((key, value) -> {
                    if (value instanceof Date) {
                        result.put(key, ((Date) value).getTime());
                    } else {
                        result.put(key, value);
                    }
                });
                return result;
            }
            return null;
        } catch (Exception e) {
            log.debug("[JWT令牌] 解析令牌为Map失败: token={}", token, e);
            return null;
        }
    }

    public String getSecretBase64() {
        return Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从令牌中获取权限列表
     *
     * @param token 令牌
     * @return 权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                Object permissionsObj = claims.get("permissions");
                if (permissionsObj instanceof List) {
                    return (List<String>) permissionsObj;
                }
            }
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            log.debug("[JWT令牌] 获取权限列表失败: token={}", token, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 从令牌中获取角色列表
     *
     * @param token 令牌
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                Object rolesObj = claims.get("roles");
                if (rolesObj instanceof List) {
                    return (List<String>) rolesObj;
                }
            }
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            log.debug("[JWT令牌] 获取角色列表失败: token={}", token, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 验证刷新令牌
     *
     * @param token 刷新令牌
     * @return 是否有效
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token) && isRefreshToken(token);
    }

    /**
     * 从刷新令牌中获取用户ID
     *
     * @param token 刷新令牌
     * @return 用户ID
     */
    public Long getUserIdFromRefreshToken(String token) {
        if (validateRefreshToken(token)) {
            return getUserIdFromToken(token);
        }
        return null;
    }

    /**
     * 验证访问令牌
     *
     * @param token 访问令牌
     * @return 是否有效
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token) && isAccessToken(token);
    }

    /**
     * 从访问令牌中获取用户ID
     *
     * @param token 访问令牌
     * @return 用户ID
     */
    public Long getUserIdFromAccessToken(String token) {
        if (validateAccessToken(token)) {
            return getUserIdFromToken(token);
        }
        return null;
    }

    /**
     * 撤销令牌（加入黑名单）
     * <p>
     * 将令牌加入黑名单，使其失效
     * 支持分布式环境，使用Redis存储黑名单
     * </p>
     *
     * @param token 令牌
     */
    public void revokeToken(String token) {
        if (blacklistService == null) {
            log.error("[JWT令牌] 黑名单服务未注入，无法撤销令牌: token={}", maskToken(token));
            return;
        }

        try {
            // 获取令牌过期时间（秒）
            Long expirationSeconds = getRemainingTime(token);

            // 加入黑名单
            blacklistService.blacklistToken(token, expirationSeconds);

            log.info("[JWT令牌] 令牌已撤销: token={}", maskToken(token));
        } catch (Exception e) {
            log.error("[JWT令牌] 撤销令牌失败: token={}, error={}", maskToken(token), e.getMessage(), e);
        }
    }

    /**
     * 撤销用户的所有令牌
     *
     * @param userId 用户ID
     * @return 撤销的令牌数量
     */
    public int revokeAllUserTokens(Long userId) {
        if (blacklistService == null) {
            log.error("[JWT令牌] 黑名单服务未注入，无法撤销用户令牌: userId={}", userId);
            return 0;
        }

        try {
            int count = blacklistService.revokeAllUserTokens(userId);
            log.info("[JWT令牌] 已撤销用户所有令牌: userId={}, count={}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("[JWT令牌] 撤销用户令牌失败: userId={}, error={}", userId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 从访问令牌中获取用户名
     *
     * @param token 访问令牌
     * @return 用户名
     */
    public String getUsernameFromAccessToken(String token) {
        if (validateAccessToken(token)) {
            return getUsernameFromToken(token);
        }
        return null;
    }

    /**
     * 从访问令牌中获取过期时间
     *
     * @param token 访问令牌
     * @return 过期时间（时间戳，毫秒）
     */
    public Long getExpirationFromAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null && isAccessToken(token)) {
                return claims.getExpiration().getTime();
            }
            return null;
        } catch (Exception e) {
            log.debug("[JWT令牌] 获取过期时间失败: token={}", token, e);
            return null;
        }
    }

    /**
     * 从访问令牌中获取签发时间
     *
     * @param token 访问令牌
     * @return 签发时间（时间戳，毫秒）
     */
    public Long getIssuedAtFromAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null && isAccessToken(token)) {
                return claims.getIssuedAt().getTime();
            }
            return null;
        } catch (Exception e) {
            log.debug("[JWT令牌] 获取签发时间失败: token={}", token, e);
            return null;
        }
    }

    /**
     * 从访问令牌中获取剩余时间
     *
     * @param token 访问令牌
     * @return 剩余时间（秒）
     */
    public long getRemainingTimeFromAccessToken(String token) {
        if (validateAccessToken(token)) {
            return getRemainingTime(token);
        }
        return 0;
    }

    /**
     * 遮盖令牌，只显示前20个字符，用于日志记录
     *
     * @param token JWT令牌
     * @return 遮盖后的令牌
     */
    private String maskToken(String token) {
        if (token == null) {
            return "null";
        }
        int length = Math.min(token.length(), 20);
        return token.substring(0, length) + "...";
    }
}
