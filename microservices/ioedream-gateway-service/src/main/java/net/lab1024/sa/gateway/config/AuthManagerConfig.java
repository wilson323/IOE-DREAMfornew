package net.lab1024.sa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.auth.dao.UserSessionDao;
import net.lab1024.sa.common.auth.manager.AuthManager;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;

/**
 * AuthManager企业级配置类
 * <p>
 * 符合CLAUDE.md规范 - Manager类通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 注册AuthManager为Spring Bean
 * - 注入完整的企业级依赖（DAO、JWT工具、Redis）
 * - 支持多级缓存和会话管理
 * </p>
 * <p>
 * 企业级特性：
 * - 完整的数据库持久化支持
 * - JWT令牌管理
 * - Redis会话缓存
 * - 防暴力破解机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Configuration
public class AuthManagerConfig {

    @Resource
    private UserSessionDao userSessionDao;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 注册企业级AuthManager
     * <p>
     * 符合CLAUDE.md规范：
     * - Manager类是纯Java类，通过构造函数注入依赖
     * - 在微服务中通过配置类将Manager注册为Spring Bean
     * - 使用@Resource注解进行依赖注入（禁止@Autowired）
     * </p>
     * <p>
     * 企业级功能：
     * - 多级缓存（L1本地+L2Redis+L3网关）
     * - 会话并发控制
     * - 令牌黑名单管理
     * - 登录安全策略（防暴力破解）
     * - 过期会话自动清理
     * </p>
     *
     * @return AuthManager实例
     */
    @Bean
    public AuthManager authManager() {
        log.info("[AuthManager] 初始化企业级认证管理器");
        log.info("[AuthManager] UserSessionDao: {}", userSessionDao != null ? "已注入" : "未注入");
        log.info("[AuthManager] JwtTokenUtil: {}", jwtTokenUtil != null ? "已注入" : "未注入");
        log.info("[AuthManager] StringRedisTemplate: {}", stringRedisTemplate != null ? "已注入" : "未注入");

        AuthManager authManager = new AuthManager(
                userSessionDao,
                jwtTokenUtil,
                stringRedisTemplate
        );

        log.info("[AuthManager] 企业级认证管理器初始化完成");
        return authManager;
    }
}
