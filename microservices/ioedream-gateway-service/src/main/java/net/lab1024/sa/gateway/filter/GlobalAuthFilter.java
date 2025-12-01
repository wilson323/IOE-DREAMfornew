package net.lab1024.sa.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 全局认证过滤器
 * 统一的认证、鉴权、日志记录
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-30
 */
@Slf4j
@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    // 无需认证的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh-token",
            "/api/auth/captcha",
            "/health",
            "/actuator/**",
            "/gateway/**",
            "/v1/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getPath().value();
        String method = request.getMethod().name();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeaders().getFirst("User-Agent");

        // 记录请求日志
        log.info("🚀 请求日志 - IP: {}, Method: {}, Path: {}, UserAgent: {}, Time: {}",
                clientIp, method, path, userAgent, LocalDateTime.now());

        // 检查是否需要跳过认证
        if (shouldExcludePath(path)) {
            log.debug("⏭️ 跳过认证路径: {}", path);
            return chain.filter(exchange);
        }

        // 检查Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getHeaders().getFirst("X-Access-Token");
        }

        if (token == null || token.isEmpty()) {
            log.warn("🚫 认证失败 - 缺少Token: IP={}, Path={}", clientIp, path);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

            String responseBody = "{\"code\":401,\"message\":\"认证失败，请先登录\",\"timestamp\":\"" +
                               LocalDateTime.now() + "\"}";
            return response.writeWith(Mono.just(response.bufferFactory().wrap(responseBody.getBytes())));
        }

        // 验证Token格式（简化版本）
        if (!validateToken(token)) {
            log.warn("🚫 Token无效: IP={}, Path={}", clientIp, path);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

            String responseBody = "{\"code\":401,\"message\":\"Token已过期或无效\",\"timestamp\":\"" +
                               LocalDateTime.now() + "\"}";
            return response.writeWith(Mono.just(response.bufferFactory().wrap(responseBody.getBytes())));
        }

        // 添加用户信息到请求头
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", extractUserIdFromToken(token))
                .header("X-User-Name", extractUsernameFromToken(token))
                .header("X-Gateway-Timestamp", LocalDateTime.now().toString())
                .build();

        log.info("✅ 认证通过 - IP={}, Path={}, User={}", clientIp, path, extractUsernameFromToken(token));

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * 检查是否需要跳过认证
     */
    private boolean shouldExcludePath(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeaders().getFirst("X-Client-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }

    /**
     * 验证Token（简化版本）
     */
    private boolean validateToken(String token) {
        // 实际项目中应该调用认证服务验证JWT
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 简单验证：Token长度和格式
        return token != null && token.length() > 20 && !token.contains(" ");
    }

    /**
     * 从Token中提取用户ID（简化版本）
     */
    private String extractUserIdFromToken(String token) {
        // 实际项目中应该解析JWT获取用户ID
        return "1001";
    }

    /**
     * 从Token中提取用户名（简化版本）
     */
    private String extractUsernameFromToken(String token) {
        // 实际项目中应该解析JWT获取用户名
        return "admin";
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级，在普通过滤器之前执行
    }
}