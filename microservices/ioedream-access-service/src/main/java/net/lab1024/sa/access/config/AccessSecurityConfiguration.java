package net.lab1024.sa.access.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.security.JwtAuthenticationEntryPoint;
import net.lab1024.sa.access.security.JwtAuthenticationFilter;
import net.lab1024.sa.access.security.JwtTokenProvider;
import net.lab1024.sa.access.security.RateLimitingFilter;
import net.lab1024.sa.access.security.SecurityHeadersFilter;

/**
 * 门禁访问服务安全配置
 * <p>
 * 提供全面的安全防护配置，包括：
 * - JWT认证和授权
 * - API访问控制和权限管理
 * - 跨域请求控制(CORS)
 * - 安全头部配置
 * - 请求频率限制
 * - 会话管理
 * - 密码加密策略
 * - 审计日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@Slf4j
public class AccessSecurityConfiguration {

    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private AccessSecurityProperties securityProperties;

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("[安全配置] 开始配置安全过滤器链");

        // 禁用CSRF（使用JWT时不需要）
        http.csrf(AbstractHttpConfigurer::disable)

            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 配置会话管理
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .maximumSessions(securityProperties.getSession().getMaximumSessions())
                .maxSessionsPreventsLogin(securityProperties.getSession().isMaxSessionsPreventsLogin())
                .sessionRegistry(sessionRegistry()))

            // 配置安全头部
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::and)
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(securityProperties.getHeaders().getHsts().getMaxAge())
                    .includeSubDomains(securityProperties.getHeaders().getHsts().isIncludeSubDomains()))
                .xssProtection(HeadersConfigurer.XXSSConfig::and))

            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 允许健康检查端点
                .requestMatchers(EndpointRequest.to("health", "info", "metrics", "prometheus")).permitAll()

                // 允许公共API
                .requestMatchers("/api/v1/access/public/**").permitAll()
                .requestMatchers("/api/v1/access/mobile/login").permitAll()
                .requestMatchers("/api/v1/access/mobile/refresh/token").permitAll()

                // 允许静态资源
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()

                // 允许Swagger文档（仅开发环境）
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

                // 管理员接口
                .requestMatchers("/api/v1/access/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/access/advanced/admin/**").hasRole("ADMIN")

                // 操作员接口
                .requestMatchers("/api/v1/access/operation/**").hasAnyRole("ADMIN", "OPERATOR")

                // 需要认证的接口
                .requestMatchers("/api/v1/access/**").authenticated()
                .requestMatchers("/api/v1/access/mobile/**").authenticated()
                .requestMatchers("/api/v1/access/advanced/**").authenticated()

                // 其他所有请求需要认证
                .anyRequest().authenticated())

            // 配置异常处理
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.error("[安全配置] 访问被拒绝 - IP: {}, URL: {}",
                        request.getRemoteAddr(), request.getRequestURL());
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"访问被拒绝\",\"data\":null}");
                }))

            // 添加JWT过滤器
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                UsernamePasswordAuthenticationFilter.class)

            // 添加安全头部过滤器
            .addFilterBefore(new SecurityHeadersFilter(securityProperties.getHeaders()),
                JwtAuthenticationFilter.class)

            // 添加频率限制过滤器
            .addFilterBefore(new RateLimitingFilter(securityProperties.getRateLimit()),
                SecurityHeadersFilter.class);

        log.info("[安全配置] 安全过滤器链配置完成");
        return http.build();
    }

    /**
     * CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("[安全配置] 配置CORS策略");

        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源
        List<String> allowedOrigins = securityProperties.getCors().getAllowedOrigins();
        if (!allowedOrigins.isEmpty()) {
            configuration.setAllowedOriginPatterns(allowedOrigins);
        } else {
            configuration.addAllowedOriginPattern("*");
        }

        // 允许的方法
        configuration.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
        ));

        // 允许的头部
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 允许凭证
        configuration.setAllowCredentials(true);

        // 预检请求缓存时间
        configuration.setMaxAge(Duration.ofHours(1));

        // 暴露的头部
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用BCrypt加密，强度为12
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * DAO认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    /**
     * 会话注册器
     */
    @Bean
    public org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    /**
     * HTTP会话事件发布器
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * 安全配置属性类
     */
    public static class AccessSecurityProperties {
        private CorsProperties cors = new CorsProperties();
        private HeadersProperties headers = new HeadersProperties();
        private SessionProperties session = new SessionProperties();
        private RateLimitProperties rateLimit = new RateLimitProperties();

        // Getters and Setters
        public CorsProperties getCors() {
            return cors;
        }

        public void setCors(CorsProperties cors) {
            this.cors = cors;
        }

        public HeadersProperties getHeaders() {
            return headers;
        }

        public void setHeaders(HeadersProperties headers) {
            this.headers = headers;
        }

        public SessionProperties getSession() {
            return session;
        }

        public void setSession(SessionProperties session) {
            this.session = session;
        }

        public RateLimitProperties getRateLimit() {
            return rateLimit;
        }

        public void setRateLimit(RateLimitProperties rateLimit) {
            this.rateLimit = rateLimit;
        }

        /**
         * CORS配置属性
         */
        public static class CorsProperties {
            private List<String> allowedOrigins = Arrays.asList("http://localhost:3000", "http://localhost:8080");

            public List<String> getAllowedOrigins() {
                return allowedOrigins;
            }

            public void setAllowedOrigins(List<String> allowedOrigins) {
                this.allowedOrigins = allowedOrigins;
            }
        }

        /**
         * 安全头部配置属性
         */
        public static class HeadersProperties {
            private HstsProperties hsts = new HstsProperties();

            public HstsProperties getHsts() {
                return hsts;
            }

            public void setHsts(HstsProperties hsts) {
                this.hsts = hsts;
            }

            public static class HstsProperties {
                private int maxAge = 31536000; // 1年
                private boolean includeSubDomains = true;

                public int getMaxAge() {
                    return maxAge;
                }

                public void setMaxAge(int maxAge) {
                    this.maxAge = maxAge;
                }

                public boolean isIncludeSubDomains() {
                    return includeSubDomains;
                }

                public void setIncludeSubDomains(boolean includeSubDomains) {
                    this.includeSubDomains = includeSubDomains;
                }
            }
        }

        /**
         * 会话管理配置属性
         */
        public static class SessionProperties {
            private int maximumSessions = 10;
            private boolean maxSessionsPreventsLogin = false;

            public int getMaximumSessions() {
                return maximumSessions;
            }

            public void setMaximumSessions(int maximumSessions) {
                this.maximumSessions = maximumSessions;
            }

            public boolean isMaxSessionsPreventsLogin() {
                return maxSessionsPreventsLogin;
            }

            public void setMaxSessionsPreventsLogin(boolean maxSessionsPreventsLogin) {
                this.maxSessionsPreventsLogin = maxSessionsPreventsLogin;
            }
        }

        /**
         * 频率限制配置属性
         */
        public static class RateLimitProperties {
            private int requestsPerMinute = 100;
            private int burstCapacity = 200;
            private int replenishRate = 100;

            public int getRequestsPerMinute() {
                return requestsPerMinute;
            }

            public void setRequestsPerMinute(int requestsPerMinute) {
                this.requestsPerMinute = requestsPerMinute;
            }

            public int getBurstCapacity() {
                return burstCapacity;
            }

            public void setBurstCapacity(int burstCapacity) {
                this.burstCapacity = burstCapacity;
            }

            public int getReplenishRate() {
                return replenishRate;
            }

            public void setReplenishRate(int replenishRate) {
                this.replenishRate = replenishRate;
            }
        }
    }
}