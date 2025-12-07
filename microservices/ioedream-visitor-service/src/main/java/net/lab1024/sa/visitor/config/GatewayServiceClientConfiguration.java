package net.lab1024.sa.visitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * GatewayServiceClient配置类
 * <p>
 * 配置网关服务客户端Bean
 * 严格遵循CLAUDE.md规范：
 * - 统一的服务间调用客户端配置
 * - 所有微服务必须通过API网关调用
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
public class GatewayServiceClientConfiguration {

    @Value("${spring.cloud.gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    /**
     * 配置RestTemplate Bean
     *
     * @param builder RestTemplateBuilder
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("配置RestTemplate Bean（访客模块）");
        return builder.build();
    }

    /**
     * 配置GatewayServiceClient Bean
     * <p>
     * 统一的服务间调用客户端，所有微服务间调用必须通过API网关
     * </p>
     *
     * @param restTemplate RestTemplate实例
     * @param objectMapper ObjectMapper实例
     * @return GatewayServiceClient实例
     */
    @Bean
    public GatewayServiceClient gatewayServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        log.info("配置GatewayServiceClient Bean（访客模块），网关URL: {}", gatewayUrl);
        return new GatewayServiceClient(
                restTemplate,
                objectMapper,
                gatewayUrl,
                "ioedream-visitor-service"
        );
    }
}

