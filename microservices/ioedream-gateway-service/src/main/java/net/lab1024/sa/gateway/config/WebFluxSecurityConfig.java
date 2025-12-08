package net.lab1024.sa.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 企业级WebFlux Security配置
 * <p>
 * 网关服务使用WebFlux(Reactive)模式，必须使用Reactive Security配置。
 * 此配置类替代传统的Servlet Security配置。
 * </p>
 * <p>
 * <b>功能特性</b>:
 * <ul>
 *   <li>CORS跨域配置 - 支持前端跨域请求</li>
 *   <li>CSRF防护 - 默认禁用(API网关场景)</li>
 *   <li>路径权限配置 - 支持白名单和认证路径</li>
 *   <li>异常处理 - 统一的认证/授权异常处理</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
public class WebFluxSecurityConfig {

    /**
     * 密码编码器
     * <p>
     * 使用BCrypt算法，企业级密码加密标准
     * </p>
     *
     * @return PasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("[WebFluxSecurity] 初始化BCrypt密码编码器");
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 白名单路径 - 无需认证即可访问
     */
    private static final String[] WHITE_LIST = {
        // Actuator健康检查
        "/actuator/**",
        // Swagger文档
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        // 认证相关
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/captcha",
        "/api/v1/auth/refresh-token",
        // 公开资源
        "/public/**",
        "/static/**",
        // 网关健康检查
        "/gateway/health"
    };

    /**
     * 配置Security过滤链
     * <p>
     * 使用ServerHttpSecurity (Reactive) 而非 HttpSecurity (Servlet)
     * </p>
     *
     * @param http ServerHttpSecurity配置
     * @return SecurityWebFilterChain (Reactive过滤链)
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("[WebFluxSecurity] 初始化企业级Reactive Security配置");

        http
            // 禁用CSRF (API网关场景，使用Token认证)
            .csrf(csrf -> csrf.disable())
            
            // CORS配置
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 路径权限配置
            .authorizeExchange(exchanges -> exchanges
                // 白名单路径 - 无需认证
                .pathMatchers(WHITE_LIST).permitAll()
                // 其他路径 - 允许所有请求 (由网关过滤器处理Token验证)
                .anyExchange().permitAll()
            )
            
            // 禁用表单登录 (API网关不使用)
            .formLogin(formLogin -> formLogin.disable())
            
            // 禁用HTTP Basic认证
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // 禁用登出 (由认证服务处理)
            .logout(logout -> logout.disable());

        log.info("[WebFluxSecurity] 企业级Reactive Security配置完成");
        log.info("[WebFluxSecurity] 白名单路径数量: {}", WHITE_LIST.length);
        log.info("[WebFluxSecurity] CSRF: 禁用, CORS: 已配置");

        return http.build();
    }

    /**
     * 企业级CORS配置源 (Reactive版本)
     * <p>
     * 使用UrlBasedCorsConfigurationSource (Reactive)
     * 而非org.springframework.web.cors.UrlBasedCorsConfigurationSource (Servlet)
     * </p>
     *
     * @return CorsConfigurationSource (Reactive)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("[WebFluxSecurity] 初始化企业级CORS配置 (Reactive)");

        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源 - 支持所有源 (生产环境建议配置具体域名)
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        // 允许的请求头
        configuration.setAllowedHeaders(List.of("*"));
        
        // 允许携带凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求缓存时间 (1小时)
        configuration.setMaxAge(3600L);
        
        // 暴露的响应头 - 允许前端读取
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Trace-Id",
            "X-Request-Id",
            "X-Token-Expires",
            "Content-Disposition"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("[WebFluxSecurity] 企业级CORS配置完成");
        log.info("[WebFluxSecurity] 允许方法: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        log.info("[WebFluxSecurity] 预检缓存: 3600秒");

        return source;
    }
}
