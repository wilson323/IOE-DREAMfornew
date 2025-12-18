package net.lab1024.sa.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dict.dao.DictDataDao;
import net.lab1024.sa.common.dict.dao.DictTypeDao;
import net.lab1024.sa.common.dict.manager.DictManager;
import net.lab1024.sa.common.menu.dao.MenuDao;
import net.lab1024.sa.common.notification.dao.NotificationConfigDao;
import net.lab1024.sa.common.notification.manager.NotificationConfigManager;
import net.lab1024.sa.common.menu.manager.MenuManager;
import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
import net.lab1024.sa.common.monitor.dao.NotificationDao;
import net.lab1024.sa.common.monitor.dao.SystemMonitorDao;
import net.lab1024.sa.common.monitor.manager.HealthCheckManager;
import net.lab1024.sa.common.monitor.manager.NotificationManager;
import net.lab1024.sa.common.monitor.manager.PerformanceMonitorManager;
import net.lab1024.sa.common.monitor.manager.SystemMonitorManager;
import net.lab1024.sa.common.system.employee.dao.EmployeeDao;
import net.lab1024.sa.common.system.employee.manager.EmployeeManager;
import net.lab1024.sa.common.system.dao.SystemConfigDao;
import net.lab1024.sa.common.system.manager.ConfigManager;
import net.lab1024.sa.common.system.manager.SystemConfigBatchManager;
import net.lab1024.sa.common.system.dao.SystemDictDao;
import net.lab1024.sa.common.auth.manager.AuthManager;
import net.lab1024.sa.common.auth.dao.UserSessionDao;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.util.AESUtil;
// Audit DAO/Manager已移至security模块，暂时注释
// import net.lab1024.sa.common.security.audit.manager.AuditManager;
// import net.lab1024.sa.common.security.audit.dao.AuditLogDao;
// import net.lab1024.sa.common.security.audit.dao.AuditArchiveDao;
// import net.lab1024.sa.common.attendance.manager.AttendanceManager; // 已迁移到attendance-service
import net.lab1024.sa.common.notification.manager.NotificationTemplateManager;
import net.lab1024.sa.common.notification.dao.NotificationTemplateDao;
import net.lab1024.sa.common.workflow.manager.ApprovalConfigManager;
import net.lab1024.sa.common.workflow.dao.ApprovalConfigDao;
import net.lab1024.sa.common.organization.manager.UserAreaPermissionManager;
import net.lab1024.sa.common.organization.manager.AreaUserManager;
import net.lab1024.sa.common.organization.dao.UserAreaPermissionDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.visitor.manager.LogisticsReservationManager;
import net.lab1024.sa.common.visitor.dao.LogisticsReservationDao;
import net.lab1024.sa.common.video.manager.VideoObjectDetectionManager;
import net.lab1024.sa.common.video.dao.VideoObjectDetectionDao;
import net.lab1024.sa.common.permission.alert.PermissionAlertManager;
import net.lab1024.sa.common.permission.audit.PermissionAuditLogger;
import net.lab1024.sa.common.openapi.manager.impl.DefaultSecurityManager;
import net.lab1024.sa.common.transaction.SeataTransactionManager;
import net.lab1024.sa.common.cache.UnifiedCacheManager;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * Manager配置类
 * <p>
 * 将microservices-common中的Manager类注册为Spring Bean
 * 确保Manager类保持纯Java特性，不使用Spring注解
 * </p>
 * <p>
 * Bean注册规范（严格遵循）：
 * 1. 使用@ConditionalOnMissingBean避免重复注册
 * 2. 禁止使用@ConditionalOnBean检查Spring Boot自动配置Bean（ObjectMapper、MeterRegistry、CacheManager、RedisTemplate等）
 * 3. 通过方法参数注入依赖，方法参数注入已经确保了依赖存在
 * 4. 添加初始化日志，记录依赖注入状态
 * </p>
 * <p>
 * 参考文档：
 * - BEAN_REGISTRATION_STANDARDS.md - Bean注册规范详细说明
 * - ARCHITECTURE_BEAN_REGISTRATION_FIX_2025-01-30.md - Bean注册问题修复报告
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 * @updated 2025-01-30 添加Bean注册规范说明
 */
@Slf4j
@Configuration("commonManagerConfiguration")
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
     * 员工管理器Bean配置
     * <p>
     * 负责员工信息查询、验证等管理功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     * <p>
     * 使用方：
     * - EmployeeServiceImpl需要此Manager
     * - MenuManager间接需要（通过EmployeeDao，不是EmployeeManager）
     * </p>
     *
     * @param employeeDao 员工DAO
     * @return 员工管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(EmployeeManager.class)
    public EmployeeManager employeeManager(EmployeeDao employeeDao) {
        log.info("[EmployeeManager] 初始化员工管理器");
        return new EmployeeManager(employeeDao);
    }

    /**
     * 通知管理器Bean配置
     * <p>
     * 负责告警通知发送、通知渠道管理、重试机制等功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     * <p>
     * 注意：使用@ConditionalOnMissingBean避免与gateway-service中的注册冲突
     * </p>
     *
     * @param notificationDao 通知DAO
     * @param alertRuleDao 告警规则DAO
     * @return 通知管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(NotificationManager.class)
    public NotificationManager notificationManager(NotificationDao notificationDao, AlertRuleDao alertRuleDao) {
        log.info("[NotificationManager] 初始化通知管理器");
        log.info("[NotificationManager] NotificationDao: {}", notificationDao != null ? "已注入" : "未注入");
        log.info("[NotificationManager] AlertRuleDao: {}", alertRuleDao != null ? "已注入" : "未注入");
        return new NotificationManager(notificationDao, alertRuleDao);
    }

    /**
     * 健康检查管理器Bean配置
     * <p>
     * 负责系统健康状态检查、微服务健康监控等功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     * <p>
     * 企业级特性：
     * - 系统健康状态检查（CPU、内存、线程）
     * - 微服务健康状态监控（通过Nacos DiscoveryClient）
     * - 并发健康检查（10线程线程池）
     * - 健康检查超时控制（5秒超时）
     * </p>
     *
     * @param discoveryClient 服务发现客户端（Spring Cloud自动注入）
     * @param restTemplate HTTP客户端（RestTemplateConfiguration配置）
     * @param objectMapper JSON序列化工具（Spring Boot自动注入）
     * @return 健康检查管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(HealthCheckManager.class)
    public HealthCheckManager healthCheckManager(
            DiscoveryClient discoveryClient,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        log.info("[HealthCheckManager] 初始化健康检查管理器");
        log.info("[HealthCheckManager] DiscoveryClient: {}", discoveryClient != null ? "已注入" : "未注入");
        log.info("[HealthCheckManager] RestTemplate: {}", restTemplate != null ? "已注入" : "未注入");
        log.info("[HealthCheckManager] ObjectMapper: {}", objectMapper != null ? "已注入" : "未注入");
        return new HealthCheckManager(discoveryClient, restTemplate, objectMapper);
    }

    /**
     * 系统监控管理器Bean配置
     * <p>
     * 负责系统资源监控、性能数据收集、监控数据存储等功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param systemMonitorDao 系统监控DAO（@MapperScan已扫描）
     * @return 系统监控管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(SystemMonitorManager.class)
    public SystemMonitorManager systemMonitorManager(SystemMonitorDao systemMonitorDao) {
        log.info("[SystemMonitorManager] 初始化系统监控管理器");
        return new SystemMonitorManager(systemMonitorDao);
    }

    /**
     * 性能监控管理器Bean配置
     * <p>
     * 负责JVM性能指标收集、性能数据分析等功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param systemMonitorDao 系统监控DAO（@MapperScan已扫描）
     * @return 性能监控管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(PerformanceMonitorManager.class)
    public PerformanceMonitorManager performanceMonitorManager(SystemMonitorDao systemMonitorDao) {
        log.info("[PerformanceMonitorManager] 初始化性能监控管理器");
        return new PerformanceMonitorManager(systemMonitorDao);
    }

    /**
     * 统一缓存管理器Bean配置（已废弃，已移除）
     * <p>
     * ✅ 已迁移到Spring Cache标准方案
     * - 使用 {@link net.lab1024.sa.common.cache.LightCacheConfiguration} 中的CompositeCacheManager
     * - 在Service层使用@Cacheable、@CacheEvict、@CachePut注解
     * - 参考 {@link documentation/technical/SPRING_CACHE_USAGE_GUIDE.md} 使用指南
     * </p>
     */

    /**
     * 缓存服务实现Bean配置
     * <p>
     * ✅ 已迁移到Spring Cache标准方案
     * - 使用Spring Cache CacheManager替代UnifiedCacheManager
     * - 提供统一的缓存操作接口
     * </p>
     * <p>
     * 注意：移除了@ConditionalOnMissingBean，确保CacheService总是被注册
     * - CacheManager和RedisTemplate是Spring Boot自动配置的Bean，应该总是存在
     * - 通过方法参数注入的方式已经确保了依赖存在（如果不存在，方法参数注入会失败）
     * </p>
     *
     * @param cacheManager Spring Cache缓存管理器（Spring Boot自动配置）
     * @param redisTemplate Redis模板（Spring Boot自动配置）
     * @return 缓存服务实现实例
     */
    @Bean
    @ConditionalOnMissingBean(net.lab1024.sa.common.cache.CacheService.class)
    public net.lab1024.sa.common.cache.CacheService cacheService(
            CacheManager cacheManager,
            RedisTemplate<String, Object> redisTemplate) {
        log.info("[CacheService] 初始化缓存服务（common-service备用）");
        log.info("[CacheService] CacheManager: {}", cacheManager != null ? "已注入" : "未注入");
        log.info("[CacheService] RedisTemplate: {}", redisTemplate != null ? "已注入" : "未注入");
        return new net.lab1024.sa.common.cache.CacheServiceImpl(cacheManager, redisTemplate);
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
     * 负责通知配置的获取、解密等管理功能
     * 支持从数据库读取配置，管理员可通过界面配置通知渠道启用状态
     * </p>
     * <p>
     * ✅ 已迁移到Spring Cache标准方案
     * - 已移除UnifiedCacheManager依赖
     * - 缓存逻辑在Service层使用@Cacheable/@CacheEvict注解
     * </p>
     * <p>
     * 注意：移除了@ConditionalOnBean条件，因为：
     * - ObjectMapper是Spring Boot自动配置的Bean，应该总是存在
     * - AESUtil在前面已经注册（@ConditionalOnMissingBean），应该总是存在
     * - 通过方法参数注入的方式已经确保了依赖存在（如果不存在，方法参数注入会失败）
     * </p>
     *
     * @param notificationConfigDao 通知配置DAO
     * @param objectMapper          JSON对象映射器（Spring Boot自动配置）
     * @param aesUtil              AES加密工具（前面已注册）
     * @return 通知配置管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(NotificationConfigManager.class)
    public NotificationConfigManager notificationConfigManager(
            NotificationConfigDao notificationConfigDao,
            ObjectMapper objectMapper,
            AESUtil aesUtil) {
        log.info("[NotificationConfigManager] 初始化通知配置管理器");
        log.info("[NotificationConfigManager] NotificationConfigDao: {}", notificationConfigDao != null ? "已注入" : "未注入");
        log.info("[NotificationConfigManager] ObjectMapper: {}", objectMapper != null ? "已注入" : "未注入");
        log.info("[NotificationConfigManager] AESUtil: {}", aesUtil != null ? "已注入" : "未注入");
        return new NotificationConfigManager(
                notificationConfigDao,
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
     * 缓存优化管理器配置对象（已废弃）
     * <p>
     * ⚠️ <strong>已废弃：</strong>CacheOptimizationManager已废弃，应使用Spring Cache注解替代。
     * 项目已迁移到Spring Cache标准方案，所有缓存操作应在Service层使用@Cacheable、@CacheEvict、@CachePut注解。
     * </p>
     * <p>
     * 参考：
     * - {@link net.lab1024.sa.common.cache.LightCacheConfiguration} - Spring Cache配置
     * - {@link documentation/technical/SPRING_CACHE_USAGE_GUIDE.md} - 使用指南
     * </p>
     *
     * @deprecated 使用Spring Cache注解（@Cacheable、@CacheEvict、@CachePut）替代
     */
    // @Bean
    // @ConditionalOnMissingBean(net.lab1024.sa.common.config.CacheOptimizationManager.CacheConfiguration.class)
    // public net.lab1024.sa.common.config.CacheOptimizationManager.CacheConfiguration cacheOptimizationConfig() {
    //     return new net.lab1024.sa.common.config.CacheOptimizationManager.CacheConfiguration();
    // }

    /**
     * 缓存优化管理器Bean配置（已废弃）
     * <p>
     * ⚠️ <strong>已废弃：</strong>CacheOptimizationManager已废弃，应使用Spring Cache注解替代。
     * 项目已迁移到Spring Cache标准方案，所有缓存操作应在Service层使用@Cacheable、@CacheEvict、@CachePut注解。
     * </p>
     * <p>
     * 参考：
     * - {@link net.lab1024.sa.common.cache.LightCacheConfiguration} - Spring Cache配置
     * - {@link documentation/technical/SPRING_CACHE_USAGE_GUIDE.md} - 使用指南
     * </p>
     *
     * @deprecated 使用Spring Cache注解（@Cacheable、@CacheEvict、@CachePut）替代
     */
    // @Bean
    // @ConditionalOnBean(RedisTemplate.class)
    // public net.lab1024.sa.common.config.CacheOptimizationManager cacheOptimizationManager(
    //         RedisTemplate<String, Object> redisTemplate,
    //         net.lab1024.sa.common.config.CacheOptimizationManager.CacheConfiguration cacheConfig) {
    //     return new net.lab1024.sa.common.config.CacheOptimizationManager(redisTemplate, cacheConfig);
    // }

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
    // 暂时禁用：ConfigChangeAuditDao在audit模块中，路径不匹配
    // @Bean
    // public net.lab1024.sa.common.audit.manager.ConfigChangeAuditManager configChangeAuditManager(
    //         net.lab1024.sa.common.audit.dao.ConfigChangeAuditDao configChangeAuditDao,
    //         ObjectMapper objectMapper) {
    //     return new net.lab1024.sa.common.audit.manager.ConfigChangeAuditManager(
    //             configChangeAuditDao,
    //             objectMapper
    //     );
    // }

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
    public SystemConfigBatchManager systemConfigBatchManager(
            SystemConfigDao systemConfigDao) {
        return new SystemConfigBatchManager(
                systemConfigDao
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
    // 已创建t_user_preference表（database-scripts/common-service/25-t_user_preference.sql）
    @Bean
    @ConditionalOnMissingBean(net.lab1024.sa.common.preference.manager.UserPreferenceManager.class)
    public net.lab1024.sa.common.preference.manager.UserPreferenceManager userPreferenceManager(
            net.lab1024.sa.common.preference.dao.UserPreferenceDao userPreferenceDao) {
        log.info("[UserPreferenceManager] 初始化用户偏好管理器");
        return new net.lab1024.sa.common.preference.manager.UserPreferenceManager(
                userPreferenceDao
        );
    }

    /**
     * 配置管理器Bean配置
     * <p>
     * 负责系统配置管理、配置缓存等功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     * <p>
     * 使用方：
     * - SystemServiceImpl需要此Manager
     * </p>
     *
     * @param systemConfigDao 系统配置DAO
     * @param redisTemplate Redis模板
     * @return 配置管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(ConfigManager.class)
    public ConfigManager configManager(SystemConfigDao systemConfigDao) {
        log.info("[ConfigManager] 初始化配置管理器");
        return new ConfigManager(systemConfigDao);
    }

    /**
     * 认证管理器Bean配置
     * <p>
     * 负责用户会话管理、令牌黑名单、登录安全策略等功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     * <p>
     * 使用方：
     * - AuthServiceImpl需要此Manager
     * </p>
     *
     * @param userSessionDao 用户会话DAO（@MapperScan已扫描）
     * @param jwtTokenUtil JWT工具类（需要在admin/config/AdminManagerConfiguration中注册）
     * @param stringRedisTemplate String Redis模板
     * @return 认证管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(AuthManager.class)
    public AuthManager authManager(
            UserSessionDao userSessionDao,
            JwtTokenUtil jwtTokenUtil,
            StringRedisTemplate stringRedisTemplate) {
        log.info("[AuthManager] 初始化认证管理器");
        return new AuthManager(userSessionDao, jwtTokenUtil, stringRedisTemplate);
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
    // 暂时禁用：缺少GatewayServiceClient
    // @Bean
    // @ConditionalOnMissingBean(net.lab1024.sa.common.workflow.manager.ExpressionEngineManager.class)
    // public net.lab1024.sa.common.workflow.manager.ExpressionEngineManager expressionEngineManager(
    //         net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient) {
    //     return new net.lab1024.sa.common.workflow.manager.ExpressionEngineManager(gatewayServiceClient);
    // }

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
    // 暂时禁用：缺少GatewayServiceClient和ExpressionEngineManager
    // @Bean
    // public net.lab1024.sa.common.workflow.executor.WorkflowExecutorRegistry workflowExecutorRegistry(
    //         net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient,
    //         net.lab1024.sa.common.workflow.manager.ExpressionEngineManager expressionEngineManager) {
    //     return new net.lab1024.sa.common.workflow.executor.WorkflowExecutorRegistry(
    //             gatewayServiceClient,
    //             expressionEngineManager
    //     );
    // }

    // ==================== 新增：统一注册公共Manager ====================

    /**
     * 系统字典管理器Bean配置（system包）
     * <p>
     * 负责系统字典数据管理，区别于dict包的DictManager
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param systemDictDao 系统字典DAO
     * @return 系统字典管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(net.lab1024.sa.common.system.manager.DictManager.class)
    public net.lab1024.sa.common.system.manager.DictManager systemDictManager(SystemDictDao systemDictDao) {
        log.info("[SystemDictManager] 初始化系统字典管理器");
        return new net.lab1024.sa.common.system.manager.DictManager(systemDictDao);
    }

    /**
     * 审计管理器Bean配置
     * <p>
     * 负责审计日志管理、归档等功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param auditLogDao 审计日志DAO
     * @param auditArchiveDao 审计归档DAO
     * @param objectMapper JSON对象映射器
     * @param auditExportPath 审计导出路径
     * @param auditArchivePath 审计归档路径
     * @return 审计管理器实例
     */
    // 暂时禁用：AuditManager依赖的DAO在security模块中，路径不匹配
    // @Bean
    // @ConditionalOnMissingBean(AuditManager.class)
    // public AuditManager auditManager(
    //         AuditLogDao auditLogDao,
    //         AuditArchiveDao auditArchiveDao,
    //         ObjectMapper objectMapper,
    //         @Value("${audit.export.path:./exports/audit}") String auditExportPath,
    //         @Value("${audit.archive.path:./archives/audit}") String auditArchivePath) {
    //     log.info("[AuditManager] 初始化审计管理器");
    //     log.info("[AuditManager] 导出路径：{}", auditExportPath);
    //     log.info("[AuditManager] 归档路径：{}", auditArchivePath);
    //     return new AuditManager(auditLogDao, auditArchiveDao, objectMapper, auditExportPath, auditArchivePath);
    // }

    // AttendanceManager已迁移到ioedream-attendance-service

    /**
     * 通知模板管理器Bean配置
     * <p>
     * 负责通知模板管理、变量替换等功能
     * ✅ 已迁移到Spring Cache标准方案
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param notificationTemplateDao 通知模板DAO
     * @param objectMapper JSON对象映射器
     * @return 通知模板管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(NotificationTemplateManager.class)
    public NotificationTemplateManager notificationTemplateManager(
            NotificationTemplateDao notificationTemplateDao,
            ObjectMapper objectMapper) {
        log.info("[NotificationTemplateManager] 初始化通知模板管理器");
        log.info("[NotificationTemplateManager] NotificationTemplateDao: {}", notificationTemplateDao != null ? "已注入" : "未注入");
        log.info("[NotificationTemplateManager] ObjectMapper: {}", objectMapper != null ? "已注入" : "未注入");
        return new NotificationTemplateManager(notificationTemplateDao, objectMapper);
    }

    /**
     * 审批配置管理器Bean配置
     * <p>
     * 负责审批配置管理、流程定义ID获取等功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param approvalConfigDao 审批配置DAO
     * @return 审批配置管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(ApprovalConfigManager.class)
    public ApprovalConfigManager approvalConfigManager(ApprovalConfigDao approvalConfigDao) {
        log.info("[ApprovalConfigManager] 初始化审批配置管理器");
        log.info("[ApprovalConfigManager] ApprovalConfigDao: {}", approvalConfigDao != null ? "已注入" : "未注入");
        return new ApprovalConfigManager(approvalConfigDao);
    }

    /**
     * 工作流审批管理器Bean配置
     * <p>
     * 负责工作流审批流程管理、任务处理等功能
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     * <p>
     * 注意：此Manager在多个业务服务中也需要使用，统一在common-service注册
     * 其他服务使用@ConditionalOnMissingBean避免重复注册
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     * @param approvalConfigManager 审批配置管理器
     * @return 工作流审批管理器实例
     */
    // 暂时禁用：缺少GatewayServiceClient
    // @Bean
    // @ConditionalOnMissingBean(WorkflowApprovalManager.class)
    // public WorkflowApprovalManager workflowApprovalManager(
    //         GatewayServiceClient gatewayServiceClient,
    //         ApprovalConfigManager approvalConfigManager) {
    //     log.info("[WorkflowApprovalManager] 初始化工作流审批管理器");
    //     log.info("[WorkflowApprovalManager] GatewayServiceClient: {}", gatewayServiceClient != null ? "已注入" : "未注入");
    //     log.info("[WorkflowApprovalManager] ApprovalConfigManager: {}", approvalConfigManager != null ? "已注入" : "未注入");
    //     return new WorkflowApprovalManager(gatewayServiceClient, approvalConfigManager);
    // }

    /**
     * 用户区域权限管理器Bean配置
     * <p>
     * 负责用户区域权限的复杂业务逻辑编排、权限验证和有效期管理
     * 符合CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     * <p>
     * 使用方：
     * - 门禁服务需要此Manager进行权限验证
     * - 生物识别服务需要此Manager查询用户权限区域
     * </p>
     *
     * @param userAreaPermissionDao 用户区域权限DAO
     * @return 用户区域权限管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(UserAreaPermissionManager.class)
    public UserAreaPermissionManager userAreaPermissionManager(UserAreaPermissionDao userAreaPermissionDao) {
        log.info("[UserAreaPermissionManager] 初始化用户区域权限管理器");
        log.info("[UserAreaPermissionManager] UserAreaPermissionDao: {}", userAreaPermissionDao != null ? "已注入" : "未注入");
        return new UserAreaPermissionManager(userAreaPermissionDao);
    }

    /**
     * 区域人员关联管理器Bean配置
     * <p>
     * 负责区域人员关联的业务逻辑处理
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param areaUserDao 区域用户DAO
     * @return 区域人员关联管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(AreaUserManager.class)
    public AreaUserManager areaUserManager(AreaUserDao areaUserDao) {
        log.info("[AreaUserManager] 初始化区域人员关联管理器");
        return new AreaUserManager(areaUserDao);
    }

    /**
     * 物流预约管理器Bean配置
     * <p>
     * 负责物流预约的业务逻辑处理
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param logisticsReservationDao 物流预约DAO
     * @return 物流预约管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(LogisticsReservationManager.class)
    public LogisticsReservationManager logisticsReservationManager(LogisticsReservationDao logisticsReservationDao) {
        log.info("[LogisticsReservationManager] 初始化物流预约管理器");
        return new LogisticsReservationManager(logisticsReservationDao);
    }

    /**
     * 视频目标检测管理器Bean配置
     * <p>
     * 负责视频目标检测的业务逻辑处理
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param videoObjectDetectionDao 视频目标检测DAO
     * @return 视频目标检测管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(VideoObjectDetectionManager.class)
    public VideoObjectDetectionManager videoObjectDetectionManager(VideoObjectDetectionDao videoObjectDetectionDao) {
        log.info("[VideoObjectDetectionManager] 初始化视频目标检测管理器");
        return new VideoObjectDetectionManager(videoObjectDetectionDao);
    }

    /**
     * 权限异常访问告警管理器Bean配置
     * <p>
     * 负责权限异常访问检测和告警
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param permissionAuditLogger 权限审计日志记录器
     * @param redisTemplate Redis模板
     * @return 权限异常访问告警管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(PermissionAlertManager.class)
    public PermissionAlertManager permissionAlertManager(
            PermissionAuditLogger permissionAuditLogger,
            RedisTemplate<String, Object> redisTemplate) {
        log.info("[PermissionAlertManager] 初始化权限异常访问告警管理器");
        return new PermissionAlertManager(permissionAuditLogger, redisTemplate);
    }

    /**
     * 默认安全管理器Bean配置
     * <p>
     * 提供基础的安全管理功能实现
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @return 默认安全管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(DefaultSecurityManager.class)
    public DefaultSecurityManager defaultSecurityManager() {
        log.info("[DefaultSecurityManager] 初始化默认安全管理器");
        return new DefaultSecurityManager();
    }

    /**
     * Seata分布式事务管理器Bean配置
     * <p>
     * 统一使用Seata的@GlobalTransactional注解
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @return Seata分布式事务管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(SeataTransactionManager.class)
    public SeataTransactionManager seataTransactionManager() {
        log.info("[SeataTransactionManager] 初始化Seata分布式事务管理器");
        return new SeataTransactionManager();
    }

    /**
     * 统一缓存管理器Bean配置（microservices-common-cache）
     * <p>
     * 实现三级缓存架构：L1本地缓存 + L2 Redis缓存
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     *
     * @param redisTemplate Redis模板
     * @param redissonClient Redisson客户端
     * @return 统一缓存管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "unifiedCacheManager")
    public net.lab1024.sa.common.cache.UnifiedCacheManager unifiedCacheManager(
            RedisTemplate<String, Object> redisTemplate,
            RedissonClient redissonClient) {
        log.info("[UnifiedCacheManager] 初始化统一缓存管理器（common-cache）");
        return new net.lab1024.sa.common.cache.UnifiedCacheManager(redisTemplate, redissonClient);
    }

    /**
     * 权限模块统一缓存管理器Bean配置（microservices-common-permission）
     * <p>
     * 权限模块专用的统一缓存管理器，实现三级缓存架构
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
     * </p>
     * <p>
     * 使用方：
     * - PermissionCacheManagerImpl需要此Manager进行权限缓存管理
     * </p>
     *
     * @param redisTemplate Redis模板
     * @return 权限模块统一缓存管理器实例
     */
    @Bean("permissionUnifiedCacheManager")
    @ConditionalOnMissingBean(name = "permissionUnifiedCacheManager")
    public net.lab1024.sa.common.permission.cache.UnifiedCacheManager permissionUnifiedCacheManager(
            RedisTemplate<String, Object> redisTemplate) {
        log.info("[PermissionUnifiedCacheManager] 初始化权限模块统一缓存管理器");
        return new net.lab1024.sa.common.permission.cache.UnifiedCacheManager(redisTemplate);
    }
}
