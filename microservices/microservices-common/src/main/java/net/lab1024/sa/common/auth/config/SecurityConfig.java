package net.lab1024.sa.common.auth.config;

import net.lab1024.sa.common.constant.SystemConstants;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 安全配置类 (Servlet应用专用)
 * <p>
 * 注意: 此配置类仅在Servlet应用中生效，
 * WebFlux应用(如API网关)不加载此配置
 * </p>
 * <p>
 * 功能：
 * - Spring Security配置
 * - 密码编码器配置
 * - JWT认证配置
 * - 权限拦截配置
 * </p>
 * <p>
 * 企业级安全特性：
 * - BCrypt密码加密
 * - 无状态会话管理
 * - CORS跨域配置
 * - CSRF防护
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SecurityConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 密码编码器
     * 使用BCrypt算法，企业级密码加密标准
     * 使用@ConditionalOnMissingBean避免Bean冲突
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(SystemConstants.BCRYPT_STRENGTH);
    }

    @Bean
    @ConditionalOnMissingBean(JwtTokenUtil.class)
    public JwtTokenUtil jwtTokenUtil(
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${security.jwt.expiration:86400}") Long accessTokenExpirationSeconds,
            @Value("${security.jwt.refresh-expiration:604800}") Long refreshTokenExpirationSeconds) {
        return new JwtTokenUtil(jwtSecret, accessTokenExpirationSeconds, refreshTokenExpirationSeconds);
    }

    @Bean
    @ConditionalOnMissingBean(name = "jwtAuthenticationFilter")
    public OncePerRequestFilter jwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String authorization = request.getHeader(AUTHORIZATION_HEADER);
                if (authorization != null && authorization.startsWith(BEARER_PREFIX)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String token = authorization.substring(BEARER_PREFIX.length()).trim();
                    if (!token.isEmpty() && jwtTokenUtil.validateToken(token) && jwtTokenUtil.isAccessToken(token)) {
                        String username = jwtTokenUtil.getUsernameFromToken(token);
                        List<String> roles = jwtTokenUtil.getRolesFromToken(token);
                        List<String> permissions = jwtTokenUtil.getPermissionsFromToken(token);

                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        if (roles != null) {
                            for (String role : roles) {
                                if (role == null || role.isBlank()) {
                                    continue;
                                }
                                String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                                authorities.add(new SimpleGrantedAuthority(normalized));
                            }
                        }
                        if (permissions != null) {
                            for (String permission : permissions) {
                                if (permission == null || permission.isBlank()) {
                                    continue;
                                }
                                authorities.add(new SimpleGrantedAuthority(permission));
                            }
                        }

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username != null ? username : "anonymous", null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OncePerRequestFilter jwtAuthenticationFilter, ObjectMapper objectMapper)
            throws Exception {
        http
                // 禁用CSRF（使用JWT令牌，不需要CSRF保护）
                .csrf(csrf -> csrf.disable())

                // 配置会话管理（无状态）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 禁用默认表单/Basic认证，统一使用JWT Bearer
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 认证失败/鉴权失败统一返回ResponseDTO
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(), ResponseDTO.error(401, "未登录或令牌无效"));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(), ResponseDTO.error(403, "无权限访问"));
                        }))

                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开接口（无需认证）
                        .requestMatchers(
                                "/actuator/**",
                                "/doc.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/favicon.ico",
                                // 认证相关接口（登录、验证码、刷新令牌等）
                                "/api/v1/auth/**",
                                "/auth/**",
                                // 兼容前端legacy登录路径
                                "/login/**",
                                "/logout/**",
                                "/captcha/**",
                                // 菜单接口（需要登录后访问，但验证码和登录不需要）
                                "/api/v1/menu/**",
                                "/menu/**")
                        .permitAll()

                        // 其他接口需要认证
                        .anyRequest().authenticated())

                // 配置CORS
                .cors(Customizer.withDefaults())

                // JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
