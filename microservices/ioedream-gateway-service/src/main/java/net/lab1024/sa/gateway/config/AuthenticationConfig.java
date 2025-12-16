package net.lab1024.sa.gateway.config;

import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 认证配置类
 * <p>
 * 注册统一认证管理器为Spring Bean
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类是纯Java类，通过构造函数注入依赖
 * - 在微服务中通过配置类将Manager注册为Spring Bean
 * </p>
 * <p>
 * 企业级功能：
 * - 多种认证方式支持（用户名密码、短信、邮件、生物识别、TOTP）
 * - JWT令牌管理
 * - Redis会话和黑名单管理
 * - 登录安全策略
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-01-30（企业级完善版）
 */
@Configuration
public class AuthenticationConfig {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration:86400}")
    private Long accessTokenExpiration;

    @Value("${security.jwt.refresh-expiration:604800}")
    private Long refreshTokenExpiration;

    /**
     * 注册JwtTokenUtil Bean
     * <p>
     * JwtTokenUtil是纯Java类，通过配置类注册为Spring Bean
     * </p>
     */
    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil(jwtSecret, accessTokenExpiration, refreshTokenExpiration);
    }

    // 架构优化方案A：网关服务轻量化
    // UnifiedAuthenticationManager依赖阻塞式组件，认证逻辑移至common-service
    // @Bean
    // public UnifiedAuthenticationManager unifiedAuthenticationManager(...) { ... }
}
