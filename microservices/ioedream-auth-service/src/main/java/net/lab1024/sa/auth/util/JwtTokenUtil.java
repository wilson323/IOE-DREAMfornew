package net.lab1024.sa.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT令牌工具类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成令牌
     *
     * @param claims 声明
     * @param subject 主题
     * @param expiration 过期时间
     * @return JWT令牌
     */
    private String generateToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成访问令牌
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param roles 角色
     * @param permissions 权限
     * @return 访问令牌
     */
    public String generateAccessToken(Long userId, String username, java.util.List<String> roles, java.util.List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        claims.put("tokenType", "access");

        return generateToken(claims, userId.toString(), expiration);
    }

    /**
     * 生成刷新令牌
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "refresh");

        return generateToken(claims, userId.toString(), refreshExpiration);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从令牌中获取角色
     *
     * @param token 令牌
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (java.util.List<String>) claims.get("roles");
    }

    /**
     * 从令牌中获取权限
     *
     * @param token 令牌
     * @return 权限列表
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getPermissionsFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (java.util.List<String>) claims.get("permissions");
    }

    /**
     * 获取令牌过期时间
     *
     * @param token 令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取指定声明
     *
     * @param token 令牌
     * @param claimsResolver 声明解析器
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中获取所有声明
     *
     * @param token 令牌
     * @return 声明
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT令牌格式错误: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("JWT令牌签名验证失败: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌参数错误: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 检查令牌是否过期
     *
     * @param token 令牌
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为刷新令牌
     *
     * @param token 令牌
     * @return 是否为刷新令牌
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "refresh".equals(claims.get("tokenType"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否为访问令牌
     *
     * @param token 令牌
     * @return 是否为访问令牌
     */
    public Boolean isAccessToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "access".equals(claims.get("tokenType"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取令牌剩余有效时间（秒）
     *
     * @param token 令牌
     * @return 剩余有效时间
     */
    public Long getRemainingExpiration(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            return (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
        } catch (Exception e) {
            return 0L;
        }
    }
}