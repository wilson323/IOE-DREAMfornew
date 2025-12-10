package net.lab1024.sa.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * 企业级GatewayServiceClient配置类
 * <p>
 * 配置网关服务的统一服务调用客户端
 * </p>
 * <p>
 * <b>功能特性</b>:
 * <ul>
 *   <li>RestTemplate - 统一HTTP客户端，支持超时配置</li>
 *   <li>GatewayServiceClient - 统一服务间调用客户端</li>
 * </ul>
 * </p>
 * <p>
 * <b>注意</b>: CORS配置已移至WebFluxSecurityConfig，
 * 使用Reactive版本的CorsConfigurationSource
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 * @see WebFluxSecurityConfig
 */
@Slf4j
@Configuration
public class GatewayServiceClientConfiguration {

    @Value("${spring.cloud.gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @Value("${gateway.client.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${gateway.client.read-timeout:30000}")
    private int readTimeout;

    private static final String SERVICE_NAME = "ioedream-gateway-service";

    /**
     * 配置RestTemplate Bean
     * <p>
     * 企业级HTTP客户端，支持超时配置
     * </p>
     * <p>
     * <b>配置项</b>:
     * <ul>
     *   <li>连接超时: gateway.client.connect-timeout (默认5000ms)</li>
     *   <li>读取超时: gateway.client.read-timeout (默认30000ms)</li>
     * </ul>
     * </p>
     *
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        log.info("[RestTemplate] 初始化企业级HTTP客户端");
        log.info("[RestTemplate] 连接超时: {}ms, 读取超时: {}ms", connectTimeout, readTimeout);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        RestTemplate restTemplate = new RestTemplate(factory);
        log.info("[RestTemplate] 企业级HTTP客户端初始化完成");
        return restTemplate;
    }

    /**
     * 配置GatewayServiceClient Bean
     * <p>
     * 统一服务间调用客户端，通过网关调用其他微服务
     * </p>
     * <p>
     * <b>配置项</b>:
     * <ul>
     *   <li>网关URL: spring.cloud.gateway.url (默认http://localhost:8080)</li>
     *   <li>服务名称: ioedream-gateway-service</li>
     * </ul>
     * </p>
     *
     * @param restTemplate RestTemplate实例
     * @param objectMapper JSON序列化工具
     * @return GatewayServiceClient实例
     */
    @Bean
    public GatewayServiceClient gatewayServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        log.info("[GatewayServiceClient] 初始化企业级网关服务客户端");
        log.info("[GatewayServiceClient] 网关URL: {}, 服务名称: {}", gatewayUrl, SERVICE_NAME);

        GatewayServiceClient client = new GatewayServiceClient(
                restTemplate,
                objectMapper,
                gatewayUrl,
                SERVICE_NAME
        );

        log.info("[GatewayServiceClient] 企业级网关服务客户端初始化完成");
        return client;
    }
}
