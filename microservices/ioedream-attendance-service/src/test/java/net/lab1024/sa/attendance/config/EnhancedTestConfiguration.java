package net.lab1024.sa.attendance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

/**
 * 增强型测试配置类
 * <p>
 * 提供完整的测试Bean配置，支持需要完整Spring上下文的测试
 * 包括：RestTemplate、ObjectMapper、GatewayServiceClient、WorkflowApprovalManager
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@TestConfiguration
public class EnhancedTestConfiguration {

    /**
     * 提供RestTemplate Bean用于测试
     */
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 提供ObjectMapper Bean用于测试
     * 配置Java 8时间模块和驼峰命名策略
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    /**
     * 提供GatewayServiceClient Bean用于测试
     */
    @Bean
    @Primary
    public GatewayServiceClient gatewayServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new GatewayServiceClient(
            restTemplate,
            objectMapper,
            "http://localhost:8080"  // 测试环境网关地址
        );
    }

    /**
     * 提供WorkflowApprovalManager Bean用于测试
     */
    @Bean
    @Primary
    public WorkflowApprovalManager workflowApprovalManager(GatewayServiceClient gatewayServiceClient) {
        return new WorkflowApprovalManager(gatewayServiceClient);
    }
}
