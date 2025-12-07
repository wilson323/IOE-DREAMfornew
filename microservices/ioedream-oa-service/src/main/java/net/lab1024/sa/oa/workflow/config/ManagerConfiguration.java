package net.lab1024.sa.oa.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.workflow.dao.ApprovalConfigDao;
import net.lab1024.sa.common.workflow.manager.ApprovalConfigManager;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;

/**
 * Manager配置类
 * <p>
 * 用于将Manager实现类注册为Spring Bean
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 在ioedream-oa-service中，通过配置类将Manager注册为Spring Bean
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

    @Resource
    private ApprovalConfigDao approvalConfigDao;

    /**
     * 注册ApprovalConfigManager为Spring Bean
     * <p>
     * 供OA服务使用，用于管理审批配置
     * </p>
     *
     * @return ApprovalConfigManager实例
     */
    @Bean
    public ApprovalConfigManager approvalConfigManager() {
        log.info("注册ApprovalConfigManager为Spring Bean（OA服务）");
        return new ApprovalConfigManager(approvalConfigDao);
    }

    /**
     * 注册WorkflowApprovalManager为Spring Bean
     * <p>
     * 供OA服务使用，支持动态审批配置
     * </p>
     *
     * @return WorkflowApprovalManager实例
     */
    @Bean
    public WorkflowApprovalManager workflowApprovalManager() {
        log.info("注册WorkflowApprovalManager为Spring Bean（OA服务），支持动态审批配置");
        return new WorkflowApprovalManager(gatewayServiceClient, approvalConfigManager());
    }
}

