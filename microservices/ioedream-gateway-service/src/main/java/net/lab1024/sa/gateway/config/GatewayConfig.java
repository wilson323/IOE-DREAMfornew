package net.lab1024.sa.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * 网关配置类
 * 限流、熔断、重试等配置
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-30
 */
@Configuration
public class GatewayConfig {

    /**
     * 基于IP地址的限流键解析器
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-Real-IP");
            }
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress();
            }
            return Mono.just(clientIp);
        };
    }

    /**
     * 基于用户ID的限流键解析器
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-User-Id");
            if (userId == null || userId.isEmpty()) {
                userId = exchange.getRequest().getHeaders().getFirst("User-Id");
            }
            if (userId == null || userId.isEmpty()) {
                userId = "anonymous";
            }
            return Mono.just(userId);
        };
    }

    /**
     * 基于API路径的限流键解析器
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest()
                    .getPath()
                    .value();
            // 提取第一级路径作为限流键
            String[] pathParts = path.split("/");
            if (pathParts.length > 1) {
                return Mono.just(pathParts[1]);
            }
            return Mono.just("default");
        };
    }
}