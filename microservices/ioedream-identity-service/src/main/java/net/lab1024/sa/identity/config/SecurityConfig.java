package net.lab1024.sa.identity.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 配置类
 * 基于现有Sa-Token模式重构，适配Spring Security + JWT
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * 密码编码器（基于现有项目密码加密方式）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 保持与现有项目兼容的密码加密方式
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS配置（基于现有跨域配置）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // 允许所有来源
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（适配前后端分离架构）
                .csrf(AbstractHttpConfigurer::disable)

                // 启用CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 会话管理（JWT无状态）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 请求授权配置
                .authorizeHttpRequests(authz -> authz
                        // 允许访问的端点（健康检查、API文档等）
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/doc.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/error")
                        .permitAll()

                        // 认证相关端点
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/auth/validate")
                        .permitAll()

                        // 兼容性员工查询端点（公开访问）
                        .requestMatchers("/api/user/employee/**").permitAll()

                        // 用户管理端点需要认证
                        .requestMatchers("/api/user/**").authenticated()

                        // 其他所有请求需要认证
                        .anyRequest().authenticated());

        // 这里可以添加JWT过滤器
        // http.addFilterBefore(jwtAuthenticationFilter(),
        // UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 兼容性配置，保持与现有Sa-Token模式的兼容性
    /**
     * 获取Sa-Token配置兼容类
     * 注意：这里模拟Sa-Token的配置转换到Spring Security
     */
    public static class SaTokenCompatibilityConfig {
        // Sa-Token配置项映射
        public static final String TOKEN_NAME = "Authorization";
        public static final String TOKEN_PREFIX = "Bearer ";
        public static final long TIMEOUT = 7200; // 2小时
        public static final long ACTIVE_TIMEOUT = 1800; // 30分钟
        public static final boolean IS_CONCURRENT = false;
        public static final boolean IS_SHARE = false;
    }
}
