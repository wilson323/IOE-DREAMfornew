package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.workflow.manager.ApprovalConfigManager;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 工作流相关Bean自动装配配置
 * <p>
 * 在 microservices-common-business 模块中，专门管理工作流相关的Bean装配
 * 这些Bean依赖 MyBatis-Plus 或其他业务组件，不适合放在 microservices-common 中
 * </p>
 * <p>
 * 包含的Bean：
 * - WorkflowApprovalManager: 工作流审批管理器
 * - ApprovalConfigManager: 审批配置管理器（如果有数据库支持）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-15
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(WorkflowApprovalManager.class)
public class WorkflowBeanAutoConfiguration {

    /**
     * 注册 WorkflowApprovalManager Bean
     * <p>
     * 工作流审批管理器，供各业务模块使用
     * 支持启动审批流程、处理审批任务、查询审批状态等功能
     * </p>
     * <p>
     * 条件：
     * - 必须存在 GatewayServiceClient Bean
     * - 当前容器中不存在同名Bean
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(GatewayServiceClient.class)
    public WorkflowApprovalManager workflowApprovalManager(
            GatewayServiceClient gatewayServiceClient,
            ObjectProvider<ApprovalConfigManager> approvalConfigManagerProvider) {
        ApprovalConfigManager approvalConfigManager = approvalConfigManagerProvider.getIfAvailable();
        log.info("[工作流Bean] 初始化WorkflowApprovalManager，ApprovalConfigManager存在: {}",
                approvalConfigManager != null);
        return new WorkflowApprovalManager(gatewayServiceClient, approvalConfigManager);
    }
}
