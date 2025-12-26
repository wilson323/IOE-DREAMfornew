package net.lab1024.sa.oa.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.oa.manager.WorkflowEngineManager;

/**
 * 工作流模块配置类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Configuration注解标识配置类
 * - 使用@Bean注册Manager实例
 * - Manager类通过构造函数注入依赖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Configuration
@Slf4j
public class WorkflowConfig {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 注册WorkflowEngineManager为Spring Bean
     * <p>
     * 通过构造函数注入GatewayServiceClient依赖
     * </p>
     *
     * @return 工作流引擎管理器实例
     */
    @Bean
    public WorkflowEngineManager workflowEngineManager() {
        log.info("[WorkflowConfig] 初始化WorkflowEngineManager");
        return new WorkflowEngineManager(gatewayServiceClient, objectMapper);
    }
}
