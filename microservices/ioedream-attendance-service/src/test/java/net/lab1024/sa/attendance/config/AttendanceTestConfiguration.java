package net.lab1024.sa.attendance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 测试配置类
 * <p>
 * 提供测试所需的Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@TestConfiguration
public class AttendanceTestConfiguration {

    /**
     * 提供RestTemplate Bean用于测试
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 提供ObjectMapper Bean用于测试
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * 提供GatewayServiceClient Bean用于测试
     */
    @Bean
    public GatewayServiceClient gatewayServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        // 使用测试环境的基础URL
        return new GatewayServiceClient(restTemplate, objectMapper, "http://localhost:8080");
    }
}
