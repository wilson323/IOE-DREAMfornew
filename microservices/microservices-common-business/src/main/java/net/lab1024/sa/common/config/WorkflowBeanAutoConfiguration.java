package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.workflow.dao.ApprovalConfigDao;
import net.lab1024.sa.common.workflow.manager.ApprovalConfigManager;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 工作流相关Bean自动装配配置
 * <p>
 * 在 microservices-common-business 模块中，专门管理工作流相关的Bean装配
 * 这些Bean依赖 MyBatis-Plus 或其他业务组件，不适合放在 microservices-common 中
 * </p>
 * <p>
 * 包含的Bean：
 * - WorkflowApprovalManager: 工作流审批管理器
 * - ApprovalConfigManager: 审批配置管理器（可选Bean，依赖ApprovalConfigDao）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.2
 * @since 2025-12-15
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(WorkflowApprovalManager.class)
public class WorkflowBeanAutoConfiguration {

    /**
     * 注册 ApprovalConfigManager Bean（可选）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ApprovalConfigDao.class)
    public ApprovalConfigManager approvalConfigManager(ApprovalConfigDao approvalConfigDao) {
        log.info("[工作流Bean] 初始化ApprovalConfigManager，支持动态审批配置");
        return new ApprovalConfigManager(approvalConfigDao);
    }

    /**
     * 注册 WorkflowApprovalManager Bean
     * <p>
     * 使用 ObjectProvider 延迟获取依赖，避免时序问题
     * 如果 GatewayServiceClient 不存在，则创建一个默认的
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkflowApprovalManager workflowApprovalManager(
            ObjectProvider<GatewayServiceClient> gatewayServiceClientProvider,
            ObjectProvider<ApprovalConfigManager> approvalConfigManagerProvider,
            ObjectProvider<RestTemplate> restTemplateProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider) {

        GatewayServiceClient gatewayServiceClient = gatewayServiceClientProvider.getIfAvailable();
        if (gatewayServiceClient == null) {
            log.warn("[工作流Bean] GatewayServiceClient不存在，创建默认实例");
            RestTemplate restTemplate = restTemplateProvider.getIfAvailable(() -> new RestTemplate());
            ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
            gatewayServiceClient = new GatewayServiceClient(restTemplate, objectMapper, "http://localhost:8080");
        }

        ApprovalConfigManager approvalConfigManager = approvalConfigManagerProvider.getIfAvailable();
        log.info("[工作流Bean] 初始化WorkflowApprovalManager，ApprovalConfigManager存在: {}",
                approvalConfigManager != null);
        return new WorkflowApprovalManager(gatewayServiceClient, approvalConfigManager);
    }
}
