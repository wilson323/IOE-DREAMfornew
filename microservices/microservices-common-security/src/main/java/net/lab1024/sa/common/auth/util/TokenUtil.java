package net.lab1024.sa.common.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.constant.Constants;
import net.lab1024.sa.common.domain.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Token工具类
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 提供JWT令牌的生成、解析和验证功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class TokenUtil {

    /**
     * JWT密钥
     * <p>
     * 从配置文件读取，必须至少256位（32字节）以确保安全性
     * </p>
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * 生成JWT令牌
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * </p>
     *
     * @param userContext 用户上下文信息
     * @return JWT令牌字符串
     */
    public String generateToken(UserContext userContext) {
        if (userContext == null || userContext.getUserId() == null) {
            throw new IllegalArgumentException("用户上下文信息不能为空");
        }

        // 使用JJWT 0.12.6 API：生成SecretKey
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userContext.getUserId().toString())
                .claim("username", userContext.getUserName())
                .claim("roles", userContext.getRoles())
                .claim("departmentId", userContext.getDepartmentId())
                .expiration(new Date(
                        System.currentTimeMillis() + Constants.TOKEN_EXPIRE_SECONDS * 1000
                ))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析JWT令牌
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * </p>
     *
     * @param token JWT令牌字符串
     * @return 用户上下文信息
     */
    public UserContext parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            // 使用JJWT 0.12.6 API：生成SecretKey
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 使用JJWT 0.12.6 API：解析令牌
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UserContext context = new UserContext();
            context.setUserId(Long.parseLong(claims.getSubject()));
            context.setUserName(claims.get("username", String.class));

            @SuppressWarnings("unchecked")
            java.util.List<String> roles = claims.get("roles", java.util.List.class);
            context.setRoles(roles);

            Object departmentIdObj = claims.get("departmentId");
            if (departmentIdObj != null) {
                if (departmentIdObj instanceof Integer) {
                    context.setDepartmentId(((Integer) departmentIdObj).longValue());
                } else if (departmentIdObj instanceof Long) {
                    context.setDepartmentId((Long) departmentIdObj);
                }
            }

            return context;
        } catch (Exception e) {
            log.error("[Token工具] 解析令牌失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 验证JWT令牌
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * </p>
     *
     * @param token JWT令牌字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            // 使用JJWT 0.12.6 API：生成SecretKey
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 使用JJWT 0.12.6 API：验证令牌
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("[Token工具] 令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
}
