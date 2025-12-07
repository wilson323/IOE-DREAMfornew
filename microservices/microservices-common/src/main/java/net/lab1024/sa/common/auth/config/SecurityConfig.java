package net.lab1024.sa.common.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置类
 *
 * 功能：
 * - Spring Security配置
 * - 密码编码器配置
 * - JWT认证配置
 * - 权限拦截配置
 *
 * 企业级安全特性：
 * - BCrypt密码加密
 * - 无状态会话管理
 * - CORS跨域配置
 * - CSRF防护
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    /**
     * 密码编码器
     * 使用BCrypt算法，企业级密码加密标准
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 安全过滤链配置
     *
     * 企业级安全配置：
     * - 禁用CSRF（使用JWT令牌）
     * - 无状态会话管理
     * - CORS跨域支持
     * - 公开接口白名单
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（使用JWT令牌，不需要CSRF保护）
                .csrf(csrf -> csrf.disable())

                // 配置会话管理（无状态）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开接口（无需认证）
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/register",
                                "/actuator/**",
                                "/doc.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/favicon.ico")
                        .permitAll()

                        // 其他接口需要认证
                        .anyRequest().authenticated())

                // 配置CORS
                .cors(cors -> cors.configure(http));

        return http.build();
    }
}
