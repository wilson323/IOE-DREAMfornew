package net.lab1024.sa.attendance.config;

import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 考勤服务Manager配置类
 * <p>
 * 负责将Manager类注册为Spring Bean
 * Manager类在microservices-common-business中是纯Java类，不使用Spring注解
 * 需要在配置类中显式注册为Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Configuration
public class AttendanceManagerConfiguration {

    /**
     * 注册工作流审批Manager
     * 使用方法参数注入GatewayServiceClient，避免循环依赖
     */
    @Bean
    public WorkflowApprovalManager workflowApprovalManager(GatewayServiceClient gatewayServiceClient) {
        return new WorkflowApprovalManager(gatewayServiceClient);
    }
}
