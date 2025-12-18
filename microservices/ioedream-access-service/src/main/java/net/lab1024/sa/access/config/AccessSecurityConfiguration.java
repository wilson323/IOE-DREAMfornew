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
 * 闂ㄧ寰湇鍔″畨鍏ㄩ厤缃?
 * <p>
 * 鎻愪緵鍏ㄩ潰鐨勫畨鍏ㄩ槻鎶ら厤缃紝鍖呮嫭锛?
 * - JWT认证和授权
 * - API璁块棶鎺у埗鍜屾潈闄愮鐞?
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
        log.info("[瀹夊叏閰嶇疆] 寮€濮嬮厤缃畨鍏ㄨ繃婊ゅ櫒閾?);

        // 绂佺敤CSRF锛堜娇鐢↗WT鏃朵笉闇€瑕侊級
        http.csrf(AbstractHttpConfigurer::disable)

            // 鍚敤CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 閰嶇疆浼氳瘽绠＄悊
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .maximumSessions(securityProperties.getSession().getMaximumSessions())
                .maxSessionsPreventsLogin(securityProperties.getSession().isMaxSessionsPreventsLogin())
                .sessionRegistry(sessionRegistry()))

            // 閰嶇疆瀹夊叏澶撮儴
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::and)
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(securityProperties.getHeaders().getHsts().getMaxAge())
                    .includeSubDomains(securityProperties.getHeaders().getHsts().isIncludeSubDomains()))
                .xssProtection(HeadersConfigurer.XXSSConfig::and))

            // 閰嶇疆璇锋眰鎺堟潈
            .authorizeHttpRequests(authz -> authz
                // 鍏佽鍋ュ悍妫€鏌ョ鐐?
                .requestMatchers(EndpointRequest.to("health", "info", "metrics", "prometheus")).permitAll()

                // 鍏佽鍏叡API
                .requestMatchers("/api/v1/access/public/**").permitAll()
                .requestMatchers("/api/v1/access/mobile/login").permitAll()
                .requestMatchers("/api/v1/access/mobile/refresh/token").permitAll()

                // 鍏佽闈欐€佽祫婧?
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()

                // 鍏佽Swagger鏂囨。锛堜粎寮€鍙戠幆澧冿級
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

                // 绠＄悊鍛樻帴鍙?
                .requestMatchers("/api/v1/access/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/access/advanced/admin/**").hasRole("ADMIN")

                // 鎿嶄綔鍛樻帴鍙?
                .requestMatchers("/api/v1/access/operation/**").hasAnyRole("ADMIN", "OPERATOR")

                // 闇€瑕佽璇佺殑鎺ュ彛
                .requestMatchers("/api/v1/access/**").authenticated()
                .requestMatchers("/api/v1/access/mobile/**").authenticated()
                .requestMatchers("/api/v1/access/advanced/**").authenticated()

                // 鍏朵粬鎵€鏈夎姹傞渶瑕佽璇?
                .anyRequest().authenticated())

            // 閰嶇疆寮傚父澶勭悊
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.error("[瀹夊叏閰嶇疆] 璁块棶琚嫆缁?- IP: {}, URL: {}",
                        request.getRemoteAddr(), request.getRequestURL());
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"璁块棶琚嫆缁漒",\"data\":null}");
                }))

            // 娣诲姞JWT杩囨护鍣?
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                UsernamePasswordAuthenticationFilter.class)

            // 娣诲姞瀹夊叏澶撮儴杩囨护鍣?
            .addFilterBefore(new SecurityHeadersFilter(securityProperties.getHeaders()),
                JwtAuthenticationFilter.class)

            // 娣诲姞棰戠巼闄愬埗杩囨护鍣?
            .addFilterBefore(new RateLimitingFilter(securityProperties.getRateLimit()),
                SecurityHeadersFilter.class);

        log.info("[瀹夊叏閰嶇疆] 安全过滤器链配置瀹屾垚");
        return http.build();
    }

    /**
     * CORS閰嶇疆婧?
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("[瀹夊叏閰嶇疆] 閰嶇疆CORS绛栫暐");

        CorsConfiguration configuration = new CorsConfiguration();

        // 鍏佽鐨勬簮
        List<String> allowedOrigins = securityProperties.getCors().getAllowedOrigins();
        if (!allowedOrigins.isEmpty()) {
            configuration.setAllowedOriginPatterns(allowedOrigins);
        } else {
            configuration.addAllowedOriginPattern("*");
        }

        // 鍏佽鐨勬柟娉?
        configuration.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
        ));

        // 鍏佽鐨勫ご閮?
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 鍏佽鍑瘉
        configuration.setAllowCredentials(true);

        // 棰勬璇锋眰缂撳瓨鏃堕棿
        configuration.setMaxAge(Duration.ofHours(1));

        // 鏆撮湶鐨勫ご閮?
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
     * 瀵嗙爜缂栫爜鍣?
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 浣跨敤BCrypt鍔犲瘑锛屽己搴︿负12
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 璁よ瘉绠＄悊鍣?
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * DAO璁よ瘉鎻愪緵鑰?
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
     * 浼氳瘽娉ㄥ唽鍣?
     */
    @Bean
    public org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    /**
     * HTTP浼氳瘽浜嬩欢鍙戝竷鍣?
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * 瀹夊叏閰嶇疆灞炴€х被
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
         * CORS閰嶇疆灞炴€?
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
         * 瀹夊叏澶撮儴閰嶇疆灞炴€?
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
                private int maxAge = 31536000; // 1骞?
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
         * 浼氳瘽绠＄悊閰嶇疆灞炴€?
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
         * 棰戠巼闄愬埗閰嶇疆灞炴€?
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