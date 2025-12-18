package net.lab1024.sa.access.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.BiometricTemplateDao;
import net.lab1024.sa.access.dao.BiometricConfigDao;
import net.lab1024.sa.access.dao.BiometricAuthRecordDao;
import net.lab1024.sa.access.manager.BiometricTemplateManager;
import net.lab1024.sa.common.organization.dao.DeviceDao;

/**
 * Manager配置类
 * <p>
 * 负责注册所有业务Manager类为Spring Bean
 * </p>
 * <p>
 * 包括所有Manager类型：notificationManager、workflowApprovalManager等
 * 继承自CommonBeanAutoConfiguration统一管理所有Manager类的Bean注册
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-14 更新配置方法统一使用Manager类的Bean注册方法
 * @updated 2025-12-17 完善BiometricTemplateManager Bean注册以及补充Manager类注解规范
 */
@Slf4j
@Configuration("accessManagerConfiguration")
public class ManagerConfiguration {

    @Resource
    private BiometricTemplateDao biometricTemplateDao;

    @Resource
    private BiometricConfigDao biometricConfigDao;

    @Resource
    private BiometricAuthRecordDao biometricAuthRecordDao;

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 注册BiometricTemplateManager为Spring Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public BiometricTemplateManager biometricTemplateManager() {
        log.info("[Manager配置] 注册BiometricTemplateManager Bean");
        return new BiometricTemplateManager(biometricTemplateDao, biometricConfigDao, deviceDao, redisTemplate);
    }

    /**
     * 注册其他业务Manager Bean
     * 根据需要可以添加更多的Manager Bean注册方法
     */
    // 这里可以添加其他Manager类的Bean注册
    // 例如：
    // @Bean
    // @ConditionalOnMissingBean
    // public AccessControlManager accessControlManager() {
    //     return new AccessControlManager(accessDeviceDao, accessRecordDao, redisTemplate);
    // }
}