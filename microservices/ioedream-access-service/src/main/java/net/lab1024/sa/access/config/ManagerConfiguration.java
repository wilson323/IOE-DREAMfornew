package net.lab1024.sa.access.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;

/**
 * Manager配置类
 * <p>
 * 用于将Manager实现类注册为Spring Bean
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 在ioedream-access-service中，通过配置类将Manager注册为Spring Bean
 * - Service层通过@Resource注入Manager实例（由Spring容器管理）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
public class ManagerConfiguration {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 注册WorkflowApprovalManager为Spring Bean
     * <p>
     * 供门禁模块使用，用于启动权限申请、紧急权限等审批流程
     * </p>
     *
     * @return WorkflowApprovalManager实例
     */
    @Bean
    public WorkflowApprovalManager workflowApprovalManager() {
        log.info("注册WorkflowApprovalManager为Spring Bean（门禁模块）");
        return new WorkflowApprovalManager(gatewayServiceClient);
    }
}

