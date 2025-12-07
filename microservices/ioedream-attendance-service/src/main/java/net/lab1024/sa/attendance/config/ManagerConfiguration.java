package net.lab1024.sa.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.attendance.manager.AttendanceManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;

/**
 * Manager配置类
 * <p>
 * 用于将Manager实现类注册为Spring Bean
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 在ioedream-attendance-service中，通过配置类将Manager注册为Spring Bean
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
     * 供考勤模块使用，用于启动请假、出差、加班、补签、调班等审批流程
     * </p>
     *
     * @return WorkflowApprovalManager实例
     */
    @Bean
    public WorkflowApprovalManager workflowApprovalManager() {
        log.info("注册WorkflowApprovalManager为Spring Bean（考勤模块）");
        return new WorkflowApprovalManager(gatewayServiceClient);
    }

    /**
     * 注册AttendanceManager为Spring Bean
     * <p>
     * 供考勤模块使用，用于处理考勤审批通过后的业务逻辑
     * 包括：用户信息获取、年假余额扣除、考勤统计更新等
     * </p>
     *
     * @return AttendanceManager实例
     */
    @Bean
    public AttendanceManager attendanceManager() {
        log.info("注册AttendanceManager为Spring Bean（考勤模块）");
        return new AttendanceManager(gatewayServiceClient);
    }
}

