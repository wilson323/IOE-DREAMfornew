package net.lab1024.sa.common.security;

import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

/**
 * 安全模块自动配置
 * <p>
 * 提供JWT令牌处理、Spring Security配置、权限验证等安全相关功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Slf4j
@Configuration
public class SecurityModuleAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("[模块化] microservices-common-security 安全模块已加载");
    }
}
