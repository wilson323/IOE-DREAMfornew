package net.lab1024.sa.data.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.client.GatewayServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Gateway服务客户端配置
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Configuration
public class GatewayServiceConfig {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 配置RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 设置连接超时
        restTemplate.getRequestFactory().setConnectTimeout(Duration.ofSeconds(10));

        // 设置读取超时
        restTemplate.getRequestFactory().setReadTimeout(Duration.ofSeconds(30));

        log.info("[Gateway配置] RestTemplate初始化完成");
        return restTemplate;
    }

    /**
     * 配置GatewayServiceClient
     */
    @Bean
    public GatewayServiceClient gatewayServiceClient(RestTemplate restTemplate) {
        GatewayServiceClient client = new GatewayServiceClient(restTemplate, objectMapper);
        log.info("[Gateway配置] GatewayServiceClient初始化完成");
        return client;
    }
}
