package net.lab1024.sa.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import net.lab1024.sa.common.cache.UnifiedCacheManager;
import net.lab1024.sa.common.dict.dao.DictDataDao;
import net.lab1024.sa.common.dict.dao.DictTypeDao;
import net.lab1024.sa.common.dict.manager.DictManager;
import net.lab1024.sa.common.menu.dao.MenuDao;
import net.lab1024.sa.common.notification.dao.NotificationConfigDao;
import net.lab1024.sa.common.notification.manager.NotificationConfigManager;
import net.lab1024.sa.common.organization.dao.EmployeeDao;
import net.lab1024.sa.common.menu.manager.MenuManager;
import net.lab1024.sa.common.util.AESUtil;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Manager配置类
 * <p>
 * 将microservices-common中的Manager类注册为Spring Bean
 * 确保Manager类保持纯Java特性，不使用Spring注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Configuration
public class ManagerConfiguration {

    /**
     * 字典管理器Bean配置
     *
     * @param dictTypeDao 字典类型DAO
     * @param dictDataDao 字典数据DAO
     * @return 字典管理器实例
     */
    @Bean
    public DictManager dictManager(DictTypeDao dictTypeDao, DictDataDao dictDataDao) {
        return new DictManager(dictTypeDao, dictDataDao);
    }

    /**
     * 菜单管理器Bean配置
     *
     * @param menuDao 菜单DAO
     * @param employeeDao 员工DAO
     * @return 菜单管理器实例
     */
    @Bean
    public MenuManager menuManager(MenuDao menuDao, EmployeeDao employeeDao) {
        return new MenuManager(menuDao, employeeDao);
    }

    /**
     * 统一缓存管理器Bean配置
     * <p>
     * 实现多级缓存架构（L1本地缓存 + L2 Redis缓存）
     * 如果已存在UnifiedCacheManager Bean，则不重复创建
     * </p>
     *
     * @param redisTemplate Redis模板
     * @param redissonClient Redisson客户端（可选）
     * @param meterRegistry Micrometer指标注册表（可选）
     * @return 统一缓存管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(UnifiedCacheManager.class)
    public UnifiedCacheManager unifiedCacheManager(
            RedisTemplate<String, Object> redisTemplate,
            RedissonClient redissonClient,
            MeterRegistry meterRegistry) {
        return new UnifiedCacheManager(
                redisTemplate,
                redissonClient,
                meterRegistry
        );
    }

    /**
     * AES加密工具Bean配置
     * <p>
     * 用于敏感数据的加密和解密
     * 如果已存在AESUtil Bean，则不重复创建
     * </p>
     *
     * @return AES加密工具实例
     */
    @Bean
    @ConditionalOnMissingBean(AESUtil.class)
    public AESUtil aesUtil() {
        return new AESUtil();
    }

    /**
     * 通知配置管理器Bean配置
     * <p>
     * 负责通知配置的获取、缓存、解密等管理功能
     * 支持从数据库读取配置，管理员可通过界面配置通知渠道启用状态
     * </p>
     *
     * @param notificationConfigDao 通知配置DAO
     * @param cacheManager          统一缓存管理器
     * @param objectMapper          JSON对象映射器
     * @param aesUtil              AES加密工具
     * @return 通知配置管理器实例
     */
    @Bean
    @ConditionalOnBean({UnifiedCacheManager.class, ObjectMapper.class, AESUtil.class})
    public NotificationConfigManager notificationConfigManager(
            NotificationConfigDao notificationConfigDao,
            UnifiedCacheManager cacheManager,
            ObjectMapper objectMapper,
            AESUtil aesUtil) {
        return new NotificationConfigManager(
                notificationConfigDao,
                cacheManager,
                objectMapper,
                aesUtil
        );
    }

    /**
     * 查询优化管理器Bean配置
     * <p>
     * 符合CLAUDE.md规范：Manager类是纯Java类，无状态设计，可直接创建单例
     * </p>
     *
     * @return 查询优化管理器实例
     */
    @Bean
    public net.lab1024.sa.common.config.QueryOptimizationManager queryOptimizationManager() {
        return new net.lab1024.sa.common.config.QueryOptimizationManager();
    }

    /**
     * 数据库优化管理器配置属性绑定
     * <p>
     * 使用@ConfigurationProperties绑定配置，然后传入Manager构造函数
     * </p>
     *
     * @return 数据库优化配置对象
     */
    @Bean
    @ConfigurationProperties(prefix = "ioedream.database")
    public net.lab1024.sa.common.config.DatabaseOptimizationManager.PoolConfig databasePoolConfig() {
        return new net.lab1024.sa.common.config.DatabaseOptimizationManager.PoolConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "ioedream.database.monitoring")
    public net.lab1024.sa.common.config.DatabaseOptimizationManager.MonitoringConfig databaseMonitoringConfig() {
        return new net.lab1024.sa.common.config.DatabaseOptimizationManager.MonitoringConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "ioedream.database.query-optimization")
    public net.lab1024.sa.common.config.DatabaseOptimizationManager.QueryOptimizationConfig databaseQueryOptimizationConfig() {
        return new net.lab1024.sa.common.config.DatabaseOptimizationManager.QueryOptimizationConfig();
    }

    /**
     * 数据库优化管理器Bean配置
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收配置对象
     * </p>
     *
     * @param poolConfig 连接池配置
     * @param monitoringConfig 监控配置
     * @param queryOptimizationConfig 查询优化配置
     * @return 数据库优化管理器实例
     */
    @Bean
    public net.lab1024.sa.common.config.DatabaseOptimizationManager databaseOptimizationManager(
            net.lab1024.sa.common.config.DatabaseOptimizationManager.PoolConfig poolConfig,
            net.lab1024.sa.common.config.DatabaseOptimizationManager.MonitoringConfig monitoringConfig,
            net.lab1024.sa.common.config.DatabaseOptimizationManager.QueryOptimizationConfig queryOptimizationConfig) {
        return new net.lab1024.sa.common.config.DatabaseOptimizationManager(
                poolConfig,
                monitoringConfig,
                queryOptimizationConfig
        );
    }

    /**
     * 缓存优化管理器配置对象
     *
     * @return 缓存配置对象（可选，使用默认配置）
     */
    @Bean
    @ConditionalOnMissingBean(net.lab1024.sa.common.config.CacheOptimizationManager.CacheConfiguration.class)
    public net.lab1024.sa.common.config.CacheOptimizationManager.CacheConfiguration cacheOptimizationConfig() {
        return new net.lab1024.sa.common.config.CacheOptimizationManager.CacheConfiguration();
    }

    /**
     * 缓存优化管理器Bean配置
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收RedisTemplate和配置对象
     * </p>
     *
     * @param redisTemplate Redis模板
     * @param cacheConfig 缓存配置（可选）
     * @return 缓存优化管理器实例
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public net.lab1024.sa.common.config.CacheOptimizationManager cacheOptimizationManager(
            RedisTemplate<String, Object> redisTemplate,
            net.lab1024.sa.common.config.CacheOptimizationManager.CacheConfiguration cacheConfig) {
        return new net.lab1024.sa.common.config.CacheOptimizationManager(redisTemplate, cacheConfig);
    }

    /**
     * 配置变更审计管理器Bean配置
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param configChangeAuditDao 配置变更审计DAO
     * @param redisTemplate Redis模板
     * @param objectMapper JSON对象映射器
     * @return 配置变更审计管理器实例
     */
    @Bean
    public net.lab1024.sa.common.audit.manager.ConfigChangeAuditManager configChangeAuditManager(
            net.lab1024.sa.common.audit.dao.ConfigChangeAuditDao configChangeAuditDao,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        return new net.lab1024.sa.common.audit.manager.ConfigChangeAuditManager(
                configChangeAuditDao,
                redisTemplate,
                objectMapper
        );
    }

    /**
     * 系统配置批量管理器Bean配置
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param systemConfigDao 系统配置DAO
     * @param redisTemplate Redis模板
     * @return 系统配置批量管理器实例
     */
    @Bean
    public net.lab1024.sa.common.system.manager.SystemConfigBatchManager systemConfigBatchManager(
            net.lab1024.sa.common.system.dao.SystemConfigDao systemConfigDao,
            RedisTemplate<String, Object> redisTemplate) {
        return new net.lab1024.sa.common.system.manager.SystemConfigBatchManager(
                systemConfigDao,
                redisTemplate
        );
    }

    /**
     * 用户偏好设置管理器Bean配置
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param userPreferenceDao 用户偏好设置DAO
     * @param redisTemplate Redis模板
     * @return 用户偏好设置管理器实例
     */
    @Bean
    public net.lab1024.sa.common.preference.manager.UserPreferenceManager userPreferenceManager(
            net.lab1024.sa.common.preference.dao.UserPreferenceDao userPreferenceDao,
            RedisTemplate<String, Object> redisTemplate) {
        return new net.lab1024.sa.common.preference.manager.UserPreferenceManager(
                userPreferenceDao,
                redisTemplate
        );
    }

    /**
     * 安全管理优化器Bean配置
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param redisTemplate Redis模板
     * @param stringRedisTemplate String Redis模板
     * @param menuDao 菜单DAO
     * @param systemConfigDao 系统配置DAO
     * @param ipWhitelistConfig IP白名单配置
     * @param ipWhitelistDbEnabled 是否启用数据库IP白名单
     * @return 安全管理优化器实例
     */
    @Bean
    public net.lab1024.sa.common.security.SecurityOptimizationManager securityOptimizationManager(
            RedisTemplate<String, Object> redisTemplate,
            org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate,
            net.lab1024.sa.common.menu.dao.MenuDao menuDao,
            net.lab1024.sa.common.system.dao.SystemConfigDao systemConfigDao,
            @Value("${security.ip.whitelist:127.0.0.1,localhost,::1}") String ipWhitelistConfig,
            @Value("${security.ip.whitelist.db.enabled:true}") boolean ipWhitelistDbEnabled) {
        net.lab1024.sa.common.security.SecurityOptimizationManager manager =
                new net.lab1024.sa.common.security.SecurityOptimizationManager(
                        redisTemplate,
                        stringRedisTemplate,
                        menuDao,
                        systemConfigDao,
                        ipWhitelistConfig,
                        ipWhitelistDbEnabled
                );
        manager.init();
        return manager;
    }

    /**
     * 表达式引擎管理器Bean配置
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端（可选）
     * @return 表达式引擎管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(net.lab1024.sa.common.workflow.manager.ExpressionEngineManager.class)
    public net.lab1024.sa.common.workflow.manager.ExpressionEngineManager expressionEngineManager(
            net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient) {
        return new net.lab1024.sa.common.workflow.manager.ExpressionEngineManager(gatewayServiceClient);
    }

    /**
     * 工作流执行器注册表Bean配置
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     * @param expressionEngineManager 表达式引擎管理器
     * @return 工作流执行器注册表实例
     */
    @Bean
    public net.lab1024.sa.common.workflow.executor.WorkflowExecutorRegistry workflowExecutorRegistry(
            net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient,
            net.lab1024.sa.common.workflow.manager.ExpressionEngineManager expressionEngineManager) {
        return new net.lab1024.sa.common.workflow.executor.WorkflowExecutorRegistry(
                gatewayServiceClient,
                expressionEngineManager
        );
    }
}
