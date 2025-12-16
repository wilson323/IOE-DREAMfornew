package net.lab1024.sa.common.auth.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT令牌工具类
 * <p>
 * 企业级JWT令牌管理工具
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入配置参数
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 功能：
 * - 生成访问令牌和刷新令牌
 * - 验证令牌有效性
 * - 解析令牌信息
 * - 管理令牌生命周期
 * </p>
 * <p>
 * 企业级特性：
 * - 使用JWT 0.13.0最新API
 * - HS256算法签名
 * - 支持自定义过期时间
 * - 完整的异常处理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-01-30（重构为纯Java类）
 */
@Slf4j
public class JwtTokenUtil {

    /**
     * JWT密钥
     * <p>
     * 必须至少256位（32字节）以确保安全性
     * </p>
     */
    private final String secret;

    /**
     * 访问令牌过期时间（秒）
     * <p>
     * 默认86400秒（24小时）
     * </p>
     */
    private final Long accessTokenExpiration;

    /**
     * 刷新令牌过期时间（秒）
     * <p>
     * 默认604800秒（7天）
     * </p>
     */
    private final Long refreshTokenExpiration;

    /**
     * 构造函数
     * <p>
     * 通过构造函数注入配置参数，符合CLAUDE.md规范
     * </p>
     *
     * @param secret JWT密钥（必须至少256位）
     * @param accessTokenExpiration 访问令牌过期时间（秒）
     * @param refreshTokenExpiration 刷新令牌过期时间（秒）
     *
     * @example
     * <pre>
     * JwtTokenUtil jwtUtil = new JwtTokenUtil(
     *     "my-secret-key-at-least-256-bits-long",
     *     86400L,  // 24小时
     *     604800L  // 7天
     * );
     * </pre>
     */
    public JwtTokenUtil(String secret, Long accessTokenExpiration, Long refreshTokenExpiration) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT密钥不能为空");
        }

        if (secret.getBytes().length < 32) {
            log.warn("[JWT工具] JWT密钥长度不足256位，建议使用更长的密钥");
        }

        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration != null ? accessTokenExpiration : 86400L;
        this.refreshTokenExpiration = refreshTokenExpiration != null ? refreshTokenExpiration : 604800L;

        log.info("[JWT工具] 初始化完成，访问令牌过期时间: {}秒，刷新令牌过期时间: {}秒",
                this.accessTokenExpiration, this.refreshTokenExpiration);
    }

    /**
     * 构造函数（使用默认过期时间）
     * <p>
     * 访问令牌：24小时，刷新令牌：7天
     * </p>
     *
     * @param secret JWT密钥
     */
    public JwtTokenUtil(String secret) {
        this(secret, 86400L, 604800L);
    }

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
            log.debug("[JWT工具] 检查令牌过期状态失败，默认返回已过期: error={}", e.getMessage());
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
     * @return Claims对象，解析失败返回null
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
            log.error("[JWT工具] 解析令牌失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析JWT令牌为Map
     * <p>
     * 将JWT令牌解析为Map格式，供外部调用使用
     * 包含所有Claims信息，包括用户ID、用户名、角色、权限等
     * </p>
     *
     * @param token JWT令牌字符串
     * @return Claims Map，解析失败返回null
     *
     * @example
     * <pre>
     * Map&lt;String, Object&gt; claims = jwtUtil.parseJwtTokenToMap(token);
     * Long userId = Long.valueOf(claims.get("userId").toString());
     * </pre>
     */
    public Map<String, Object> parseJwtTokenToMap(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return null;
            }

            // 将Claims转换为Map
            Map<String, Object> claimsMap = new HashMap<>();
            claimsMap.put("userId", claims.get(CLAIM_KEY_USER_ID));
            claimsMap.put("username", claims.get(CLAIM_KEY_USERNAME));
            claimsMap.put("roles", claims.get(CLAIM_KEY_ROLES));
            claimsMap.put("permissions", claims.get(CLAIM_KEY_PERMISSIONS));
            claimsMap.put("tokenType", claims.get(CLAIM_KEY_TOKEN_TYPE));
            claimsMap.put("iat", claims.getIssuedAt() != null ? claims.getIssuedAt().getTime() / 1000 : null);
            claimsMap.put("exp", claims.getExpiration() != null ? claims.getExpiration().getTime() / 1000 : null);

            return claimsMap;
        } catch (Exception e) {
            log.error("[JWT工具] 解析令牌为Map失败: {}", e.getMessage(), e);
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
            log.debug("[JWT工具] 获取令牌剩余过期时间失败，返回0: token={}, error={}", token, e.getMessage());
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
