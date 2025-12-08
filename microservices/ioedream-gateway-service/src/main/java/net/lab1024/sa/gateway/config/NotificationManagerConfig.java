package net.lab1024.sa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
import net.lab1024.sa.common.monitor.dao.NotificationDao;
import net.lab1024.sa.common.monitor.manager.NotificationManager;

/**
 * NotificationManager企业级配置类
 * <p>
 * 符合CLAUDE.md规范 - Manager类通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 注册NotificationManager为Spring Bean
 * - 注入完整的企业级依赖（NotificationDao、AlertRuleDao）
 * - 支持告警通知、多渠道发送、重试机制
 * </p>
 * <p>
 * 企业级特性：
 * - 多渠道通知支持（邮件、短信、Webhook、微信）
 * - 异步发送机制
 * - 智能重试策略（指数退避）
 * - 通知优先级管理
 * - 告警规则驱动的通知分发
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Configuration
public class NotificationManagerConfig {

    @Resource
    private NotificationDao notificationDao;

    @Resource
    private AlertRuleDao alertRuleDao;

    /**
     * 注册企业级NotificationManager
     * <p>
     * 符合CLAUDE.md规范：
     * - Manager类是纯Java类，通过构造函数注入依赖
     * - 在微服务中通过配置类将Manager注册为Spring Bean
     * - 使用@Resource注解进行依赖注入（禁止@Autowired）
     * </p>
     * <p>
     * 企业级功能：
     * - 告警通知发送
     * - 多渠道通知管理（EMAIL/SMS/WEBHOOK/WECHAT）
     * - 通知重试机制（指数退避算法）
     * - 通知优先级控制
     * - 通知状态追踪
     * - 批量通知处理
     * - 通知统计分析
     * </p>
     * <p>
     * 渠道类型：
     * - 1: 邮件（EMAIL）
     * - 2: 短信（SMS）
     * - 3: Webhook
     * - 4: 微信（WECHAT）
     * </p>
     *
     * @return NotificationManager实例
     */
    @Bean
    public NotificationManager notificationManager() {
        log.info("[NotificationManager] 初始化企业级通知管理器");
        log.info("[NotificationManager] NotificationDao: {}", notificationDao != null ? "已注入" : "未注入");
        log.info("[NotificationManager] AlertRuleDao: {}", alertRuleDao != null ? "已注入" : "未注入");

        NotificationManager notificationManager = new NotificationManager(
                notificationDao,
                alertRuleDao
        );

        log.info("[NotificationManager] 企业级通知管理器初始化完成");
        log.info("[NotificationManager] 支持渠道：EMAIL(1), SMS(2), WEBHOOK(3), WECHAT(4)");
        log.info("[NotificationManager] 异步线程池：20线程");
        log.info("[NotificationManager] 重试策略：指数退避，最大重试3次");
        
        return notificationManager;
    }
}
