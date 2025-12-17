package net.lab1024.sa.access.config;

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
 * 用于将门禁模块特有的Manager实现类注册为Spring Bean
 * </p>
 * <p>
 * 注意：公共Manager（NotificationManager、WorkflowApprovalManager等）
 * 已由CommonBeanAutoConfiguration统一装配，无需在此重复定义
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-14 移除重复的公共Bean定义，改用统一自动装配
 * @updated 2025-12-17 添加BiometricTemplateManager Bean注册，修复Manager注解违规
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
     * <p>
     * 生物识别模板管理器，负责复杂的生物识别模板业务流程编排
     * 包括：模板注册、特征匹配、活体检测、1:N识别等
     * </p>
     *
     * @return BiometricTemplateManager实例
     */
    @Bean
    public BiometricTemplateManager biometricTemplateManager() {
        log.info("[BiometricTemplateManager] 初始化生物识别模板管理器");
        return new BiometricTemplateManager(
                biometricTemplateDao,
                biometricConfigDao,
                biometricAuthRecordDao,
                deviceDao,
                redisTemplate
        );
    }

    // 公共Bean（NotificationManager、WorkflowApprovalManager）已由CommonBeanAutoConfiguration统一装配
    // 此处仅保留门禁模块特有的Manager定义

}