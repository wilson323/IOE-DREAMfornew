package net.lab1024.sa.gateway.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
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
     * <p>
     * <b>Bean冲突处理</b>:
     * - 使用@ConditionalOnMissingBean避免与microservices-common中的PasswordEncoder冲突
     * - 如果microservices-common中已定义PasswordEncoder，则使用common中的实例
     * </p>
     *
     * @return PasswordEncoder实例
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        log.info("[WebFluxSecurity] 初始化BCrypt密码编码器");
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 白名单路径 - 无需认证即可访问
     * <p>
     * 原则：以配置（Nacos）为单一真源；此处仅提供最小静态兜底，避免配置缺失导致不可用。
     * </p>
     */
    private static final String[] STATIC_WHITE_LIST = {
        "/actuator/**",
        "/doc.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/favicon.ico",
        "/api/v1/auth/**",
        "/login/**",
        "/public/**",
        "/static/**",
        "/gateway/health"
    };

    @Bean
    @ConfigurationProperties(prefix = "security.whitelist")
    public WhitelistProperties whitelistProperties() {
        return new WhitelistProperties();
    }

    public static class WhitelistProperties {
        private List<String> paths = new ArrayList<>();

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }
    }

    @Bean
    @ConfigurationProperties(prefix = "cors")
    public CorsProperties corsProperties() {
        return new CorsProperties();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        log.info("[WebFluxSecurity] 初始化企业级CORS配置 (Reactive)");

        CorsConfiguration configuration = new CorsConfiguration();

        CorsProperties props = corsProperties != null ? corsProperties : new CorsProperties();

        List<String> originPatterns = safeList(props.getAllowedOriginPatterns());
        List<String> origins = safeList(props.getAllowedOrigins());

        if (!originPatterns.isEmpty()) {
            configuration.setAllowedOriginPatterns(originPatterns);
        } else if (!origins.isEmpty()) {
            configuration.setAllowedOrigins(origins);
        } else {
            configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        }

        configuration.setAllowedMethods(nonEmptyOrDefault(safeList(props.getAllowedMethods()), Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        )));

        configuration.setAllowedHeaders(nonEmptyOrDefault(safeList(props.getAllowedHeaders()), List.of("*")));
        configuration.setExposedHeaders(safeList(props.getExposedHeaders()));
        configuration.setMaxAge(props.getMaxAge() != null ? props.getMaxAge() : 3600L);

        boolean allowCredentials = Boolean.TRUE.equals(props.getAllowCredentials());
        if (allowCredentials && isWildcardCors(configuration)) {
            log.warn("[WebFluxSecurity] CORS配置检测到 allowCredentials=true 且允许任意Origin（*），为避免高风险组合将关闭 allowCredentials");
            allowCredentials = false;
        }
        configuration.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("[WebFluxSecurity] 企业级CORS配置完成: allowCredentials={}, maxAge={}", allowCredentials, configuration.getMaxAge());
        log.info("[WebFluxSecurity] CORS origins={}, originPatterns={}", configuration.getAllowedOrigins(), configuration.getAllowedOriginPatterns());
        log.info("[WebFluxSecurity] CORS allowedMethods={}, allowedHeaders={}, exposedHeaders={}",
                configuration.getAllowedMethods(), configuration.getAllowedHeaders(), configuration.getExposedHeaders());

        return source;
    }

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
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, WhitelistProperties whitelistProperties, CorsConfigurationSource corsConfigurationSource) {
        log.info("[WebFluxSecurity] 初始化企业级Reactive Security配置");

        LinkedHashSet<String> effectiveWhitelist = new LinkedHashSet<>(Arrays.asList(STATIC_WHITE_LIST));
        if (whitelistProperties != null && whitelistProperties.getPaths() != null) {
            effectiveWhitelist.addAll(whitelistProperties.getPaths());
        }
        String[] whiteList = effectiveWhitelist.toArray(String[]::new);

        http
            // 禁用CSRF (API网关场景，使用Token认证)
            .csrf(csrf -> csrf.disable())

            // CORS配置
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // 路径权限配置
            .authorizeExchange(exchanges -> exchanges
                // 白名单路径 - 无需认证
                .pathMatchers(whiteList).permitAll()
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
        log.info("[WebFluxSecurity] 白名单路径数量: {}", whiteList.length);
        log.info("[WebFluxSecurity] CSRF: 禁用, CORS: 已配置");

        return http.build();
    }

    @Data
    public static class CorsProperties {

        private List<String> allowedOrigins = new ArrayList<>();

        private List<String> allowedOriginPatterns = new ArrayList<>();

        private List<String> allowedMethods = new ArrayList<>();

        private List<String> allowedHeaders = new ArrayList<>();

        private List<String> exposedHeaders = new ArrayList<>();

        private Boolean allowCredentials = true;

        private Long maxAge = 3600L;
    }

    private static List<String> safeList(List<String> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        List<String> filtered = new ArrayList<>();
        for (String item : list) {
            if (item == null) {
                continue;
            }
            String v = item.trim();
            if (v.isEmpty()) {
                continue;
            }
            // 兼容 yml 中将 list 写成逗号字符串的场景："GET,POST" / "http://a,http://b"
            if (v.contains(",")) {
                String[] parts = v.split(",");
                for (String part : parts) {
                    if (part == null) {
                        continue;
                    }
                    String p = part.trim();
                    if (!p.isEmpty()) {
                        filtered.add(p);
                    }
                }
            } else {
                filtered.add(v);
            }
        }
        return filtered;
    }

    private static List<String> nonEmptyOrDefault(List<String> list, List<String> defaultList) {
        return (list == null || list.isEmpty()) ? defaultList : list;
    }

    private static boolean isWildcardCors(CorsConfiguration configuration) {
        if (configuration == null) {
            return false;
        }
        List<String> patterns = configuration.getAllowedOriginPatterns();
        if (patterns != null && patterns.contains("*")) {
            return true;
        }
        List<String> origins = configuration.getAllowedOrigins();
        return origins != null && origins.contains("*");
    }
}
