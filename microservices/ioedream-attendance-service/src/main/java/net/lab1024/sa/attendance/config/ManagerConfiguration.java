package net.lab1024.sa.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.attendance.manager.AttendanceManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * Manager配置类
 * <p>
 * 用于将考勤模块特有的Manager实现类注册为Spring Bean
 * </p>
 * <p>
 * 注意：公共Manager（WorkflowApprovalManager等）
 * 已由CommonBeanAutoConfiguration统一装配，无需在此重复定义
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-14 移除重复的公共Bean定义，改用统一自动装配
 */
@Slf4j
@Configuration("attendanceManagerConfiguration")
public class ManagerConfiguration {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 注册AttendanceManager为Spring Bean
     * <p>
     * 考勤模块特有的Manager，用于处理考勤审批通过后的业务逻辑
     * 包括：用户信息获取、年假余额扣除、考勤统计更新等
     * </p>
     *
     * @return AttendanceManager实例
     */
    @Bean
    public AttendanceManager attendanceManager() {
        log.info("[AttendanceManager] 初始化考勤管理器");
        return new AttendanceManager(gatewayServiceClient);
    }
}



