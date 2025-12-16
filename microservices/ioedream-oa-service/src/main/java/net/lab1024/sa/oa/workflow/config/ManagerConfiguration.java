package net.lab1024.sa.oa.workflow.config;

import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Manager配置类
 * <p>
 * 用于将OA模块特有的Manager实现类注册为Spring Bean
 * </p>
 * <p>
 * 注意：公共Manager（NotificationManager、WorkflowApprovalManager、ApprovalConfigManager等）
 * 已由CommonBeanAutoConfiguration统一装配，无需在此重复定义
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-14 移除重复的公共Bean定义，改用统一自动装配
 */
@Slf4j
@Configuration("oaManagerConfiguration")
public class ManagerConfiguration {

    // 公共Bean（NotificationManager、WorkflowApprovalManager、ApprovalConfigManager）
    // 已由CommonBeanAutoConfiguration统一装配
    // 此处仅保留OA模块特有的Manager定义（如有）

}





