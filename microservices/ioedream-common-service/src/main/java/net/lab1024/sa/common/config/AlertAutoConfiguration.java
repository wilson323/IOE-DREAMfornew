package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitoring.AlertManager;
import net.lab1024.sa.common.monitoring.EnterpriseMonitoringManager;
import net.lab1024.sa.common.notification.manager.NotificationConfigManager;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PreDestroy;

/**
 * 告警自动配置类
 * <p>
 * 配置AlertManager和告警组件
 * 提供企业级告警监控和通知功能
 * 支持多种通知渠道和告警收敛策略
 * 支持从数据库读取通知渠道配置，管理员可通过界面配置启用状态
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "monitoring.alert.enabled", havingValue = "true", matchIfMissing = true)
public class AlertAutoConfiguration {

    private AlertManager alertManager;

    /**
     * 配置AlertManager（完整版本，支持从数据库读取配置）
     */
    @Bean
    @ConditionalOnMissingBean(AlertManager.class)
    @ConditionalOnBean(NotificationConfigManager.class)
    public AlertManager alertManager(
            NotificationConfigManager notificationConfigManager) {
        // 创建MetricsCollector实例
        net.lab1024.sa.common.monitoring.MetricsCollector metricsCollector = new net.lab1024.sa.common.monitoring.MetricsCollector();
        // 使用完整构造函数，支持从数据库读取配置
        alertManager = new AlertManager(
                metricsCollector,
                null, // GatewayServiceClient（可选）
                notificationConfigManager, // NotificationConfigManager（支持从数据库读取配置）
                null, null, null, null, null, null, null // 其他可选参数
        );

        log.info("[告警配置] AlertManager 已配置并启用（支持数据库配置）");
        return alertManager;
    }

    /**
     * 配置AlertManager（简化版本，使用默认配置）
     * <p>
     * 如果NotificationConfigManager未配置，则使用简化构造函数，使用默认配置
     * 作为降级方案
     * </p>
     * <p>
     * 已迁移：使用 MeterRegistry 替代已废弃的 MetricsCollector
     * </p>
     * <p>
     * 注意：
     * - MeterRegistry是Spring Boot自动配置的Bean，应该总是存在
     * - 只在AlertManager不存在时注册（如果NotificationConfigManager存在，完整版本会先注册）
     * - 作为降级方案，使用默认配置
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(AlertManager.class)
    public AlertManager alertManagerFallback() {
        // 创建MetricsCollector实例并使用简化构造函数
        net.lab1024.sa.common.monitoring.MetricsCollector metricsCollector = new net.lab1024.sa.common.monitoring.MetricsCollector();
        alertManager = new AlertManager(metricsCollector);

        log.info("[告警配置] AlertManager 已配置并启用（使用默认配置）");
        return alertManager;
    }

    /**
     * 配置EnterpriseMonitoringManager
     * <p>
     * 符合CLAUDE.md规范：
     * - Manager类是纯Java类，通过构造函数注入所有依赖
     * - 在微服务中通过配置类将Manager注册为Spring Bean
     * - 从application.yml读取配置值，通过构造函数传入
     * </p>
     * <p>
     * 注意：移除了@ConditionalOnBean条件，因为：
     * - MeterRegistry是Spring Boot自动配置的Bean，应该总是存在
     * - 通过方法参数注入的方式已经确保了依赖存在（如果不存在，方法参数注入会失败）
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(EnterpriseMonitoringManager.class)
    public EnterpriseMonitoringManager enterpriseMonitoringManager(
            MeterRegistry meterRegistry,
            RestTemplate restTemplate,
            @Value("${monitoring.alert.email.enabled:false}") boolean emailAlertEnabled,
            @Value("${monitoring.alert.sms.enabled:false}") boolean smsAlertEnabled,
            @Value("${monitoring.alert.webhook.url:}") String webhookUrl,
            @Value("${monitoring.alert.dingtalk.webhook:}") String dingTalkWebhook,
            @Value("${monitoring.alert.sms.provider:aliyun}") String smsProvider,
            @Value("${monitoring.alert.sms.api.url:}") String smsApiUrl,
            @Value("${monitoring.alert.sms.api.key:}") String smsApiKey,
            @Value("${monitoring.alert.sms.api.secret:}") String smsApiSecret,
            @Value("${monitoring.alert.sms.template.id:}") String smsTemplateId,
            @Value("${monitoring.alert.sms.phone.numbers:}") String smsPhoneNumbers,
            @Value("${monitoring.alert.email.provider:smtp}") String emailProvider,
            @Value("${monitoring.alert.email.smtp.host:}") String smtpHost,
            @Value("${monitoring.alert.email.smtp.port:587}") int smtpPort,
            @Value("${monitoring.alert.email.smtp.username:}") String smtpUsername,
            @Value("${monitoring.alert.email.smtp.password:}") String smtpPassword,
            @Value("${monitoring.alert.email.from:}") String emailFrom,
            @Value("${monitoring.alert.email.to:}") String emailTo,
            @Value("${monitoring.alert.email.api.url:}") String emailApiUrl,
            @Value("${monitoring.alert.email.api.key:}") String emailApiKey) {

        EnterpriseMonitoringManager manager = new EnterpriseMonitoringManager(
                meterRegistry,
                restTemplate,
                emailAlertEnabled,
                smsAlertEnabled,
                webhookUrl,
                dingTalkWebhook,
                smsProvider,
                smsApiUrl,
                smsApiKey,
                smsApiSecret,
                smsTemplateId,
                smsPhoneNumbers,
                emailProvider,
                smtpHost,
                smtpPort,
                smtpUsername,
                smtpPassword,
                emailFrom,
                emailTo,
                emailApiUrl,
                emailApiKey
        );

        // 调用初始化方法
        manager.init();

        log.info("[监控配置] EnterpriseMonitoringManager 已配置并启用");
        log.info("[监控配置] MeterRegistry: {}", meterRegistry != null ? "已注入" : "未注入");
        log.info("[监控配置] RestTemplate: {}", restTemplate != null ? "已注入" : "未注入");
        return manager;
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void cleanup() {
        if (alertManager != null) {
            alertManager.shutdown();
            log.info("[告警配置] AlertManager 已关闭，资源已清理");
        }
    }
}
