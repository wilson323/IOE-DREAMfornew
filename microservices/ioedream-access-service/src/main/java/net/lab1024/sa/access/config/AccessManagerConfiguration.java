package net.lab1024.sa.access.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.manager.AccessVerificationManager;
import net.lab1024.sa.access.manager.AntiPassbackManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;

/**
 * 门禁服务Manager配置类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Configuration注解
 * - 使用@ConditionalOnMissingBean避免重复注册
 * - Manager类通过构造函数注入依赖
 * </p>
 * <p>
 * 核心职责：
 * - 注册所有业务Manager类为Spring Bean
 * - 确保Manager类单例注册
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
public class AccessManagerConfiguration {

    @Resource
    private net.lab1024.sa.common.organization.dao.AntiPassbackRecordDao antiPassbackRecordDao;

    @Resource
    private net.lab1024.sa.common.organization.dao.UserAreaPermissionDao userAreaPermissionDao;

    @Resource
    private net.lab1024.sa.common.organization.manager.UserAreaPermissionManager userAreaPermissionManager;

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private net.lab1024.sa.common.organization.dao.InterlockRecordDao interlockRecordDao;

    @Resource
    private net.lab1024.sa.common.organization.dao.MultiPersonRecordDao multiPersonRecordDao;

    @Resource
    private net.lab1024.sa.common.organization.dao.AreaAccessExtDao areaAccessExtDao;

    @Resource
    private AccessVerificationProperties verificationProperties;

    /**
     * 注册AntiPassbackManager为Spring Bean
     * <p>
     * 注意：必须在AccessVerificationManager之前注册，因为AccessVerificationManager依赖AntiPassbackManager
     * </p>
     *
     * @return AntiPassbackManager实例
     */
    @Bean
    @ConditionalOnMissingBean(AntiPassbackManager.class)
    public AntiPassbackManager antiPassbackManager() {
        log.info("[Manager配置] 注册AntiPassbackManager Bean");
        return new AntiPassbackManager(
                antiPassbackRecordDao,
                areaAccessExtDao,
                redisTemplate,
                objectMapper
        );
    }

    /**
     * 注册AccessVerificationManager为Spring Bean
     * <p>
     * 注意：使用@DependsOn确保AntiPassbackManager先注册
     * </p>
     *
     * @return AccessVerificationManager实例
     */
    @Bean
    @ConditionalOnMissingBean(AccessVerificationManager.class)
    @org.springframework.context.annotation.DependsOn("antiPassbackManager")
    public AccessVerificationManager accessVerificationManager() {
        log.info("[Manager配置] 注册AccessVerificationManager Bean");
        return new AccessVerificationManager(
                antiPassbackManager(),  // 使用已注册的AntiPassbackManager Bean
                userAreaPermissionDao,
                userAreaPermissionManager,
                deviceDao,
                gatewayServiceClient,
                redisTemplate,
                objectMapper,
                areaAccessExtDao,
                interlockRecordDao,
                multiPersonRecordDao,
                verificationProperties
        );
    }

}
