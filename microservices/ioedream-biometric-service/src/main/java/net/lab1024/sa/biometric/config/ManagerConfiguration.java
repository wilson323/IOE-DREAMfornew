package net.lab1024.sa.biometric.config;

import net.lab1024.sa.biometric.dao.BiometricTemplateDao;
import net.lab1024.sa.biometric.manager.BiometricTemplateManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Manager Bean配置类
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 在微服务中通过@Configuration类将Manager注册为Spring Bean
 * - 使用@ConditionalOnMissingBean避免重复注册
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Configuration
public class ManagerConfiguration {

    /**
     * 注册BiometricTemplateManager Bean
     */
    @Bean
    @ConditionalOnMissingBean(BiometricTemplateManager.class)
    public BiometricTemplateManager biometricTemplateManager(
            BiometricTemplateDao biometricTemplateDao,
            DeviceDao deviceDao,
            GatewayServiceClient gatewayServiceClient,
            RedisTemplate<String, Object> redisTemplate) {
        return new BiometricTemplateManager(
                biometricTemplateDao,
                deviceDao,
                gatewayServiceClient,
                redisTemplate
        );
    }
}