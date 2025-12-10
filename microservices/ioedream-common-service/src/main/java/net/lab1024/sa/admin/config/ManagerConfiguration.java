package net.lab1024.sa.admin.config;

import net.lab1024.sa.common.auth.dao.UserSessionDao;
import net.lab1024.sa.common.auth.manager.AuthManager;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Manager层Bean配置类
 * <p>
 * 将microservices-common中的Manager类和工具类注册为Spring Bean
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用@Component注解
 * - Manager类通过构造函数注入依赖
 * - 在微服务中通过@Configuration类将Manager注册为Spring Bean
 * - Service层通过@Resource注入Manager实例（由Spring容器管理）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Configuration
public class ManagerConfiguration {

    @Value("${auth.jwt.secret:ioedream-jwt-secret-key-2025-must-be-at-least-256-bits}")
    private String jwtSecret;

    @Value("${auth.jwt.access-token-expiration:86400}")
    private Long accessTokenExpiration;

    @Value("${auth.jwt.refresh-token-expiration:604800}")
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

    /**
     * 注册AuthManager Bean
     * <p>
     * 用于认证授权的业务流程编排
     * </p>
     */
    @Bean
    public AuthManager authManager(
            UserSessionDao userSessionDao,
            JwtTokenUtil jwtTokenUtil,
            StringRedisTemplate redisTemplate) {
        return new AuthManager(userSessionDao, jwtTokenUtil, redisTemplate);
    }
}
