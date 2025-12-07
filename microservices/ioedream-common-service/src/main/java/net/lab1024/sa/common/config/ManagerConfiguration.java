package net.lab1024.sa.common.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import net.lab1024.sa.common.audit.dao.AuditArchiveDao;
import net.lab1024.sa.common.audit.dao.AuditLogDao;
import net.lab1024.sa.common.audit.manager.AuditManager;
import net.lab1024.sa.common.auth.dao.UserSessionDao;
import net.lab1024.sa.common.auth.manager.AuthManager;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.cache.UnifiedCacheManager;
import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
import net.lab1024.sa.common.monitor.dao.NotificationDao;
import net.lab1024.sa.common.monitor.dao.SystemLogDao;
import net.lab1024.sa.common.monitor.dao.SystemMonitorDao;
import net.lab1024.sa.common.monitor.manager.HealthCheckManager;
import net.lab1024.sa.common.monitor.manager.LogManagementManager;
import net.lab1024.sa.common.monitor.manager.MetricsCollectorManager;
import net.lab1024.sa.common.monitor.manager.NotificationManager;
import net.lab1024.sa.common.monitor.manager.PerformanceMonitorManager;
import net.lab1024.sa.common.monitor.manager.SystemMonitorManager;
import net.lab1024.sa.common.system.dao.SystemConfigDao;
import net.lab1024.sa.common.system.dao.SystemDictDao;
import net.lab1024.sa.common.system.employee.dao.EmployeeDao;
import net.lab1024.sa.common.system.employee.manager.EmployeeManager;
import net.lab1024.sa.common.system.manager.ConfigManager;
import net.lab1024.sa.common.system.manager.DictManager;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * Manager配置类
 * <p>
 * 将所有microservices-common中的Manager类注册为Spring Bean
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 在ioedream-common-service中，通过@Configuration类将Manager注册为Spring Bean
 * - Service层通过@Resource注入Manager实例（由Spring容器管理）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
public class ManagerConfiguration {

    // ==================== DAO依赖注入 ====================

    @Resource
    private SystemConfigDao systemConfigDao;

    @Resource
    private SystemDictDao systemDictDao;

    @Resource
    private EmployeeDao employeeDao;

    @Resource
    private NotificationDao notificationDao;

    @Resource
    private AlertRuleDao alertRuleDao;

    @Resource
    private SystemMonitorDao systemMonitorDao;

    @Resource
    private SystemLogDao systemLogDao;

    @Resource
    private UserSessionDao userSessionDao;

    @Resource
    private AuditLogDao auditLogDao;

    @Resource
    private AuditArchiveDao auditArchiveDao;

    @Resource
    private net.lab1024.sa.common.rbac.dao.RoleDao roleDao;

    @Resource
    private net.lab1024.sa.common.notification.dao.NotificationConfigDao notificationConfigDao;

    @Resource
    private net.lab1024.sa.common.notification.dao.NotificationTemplateDao notificationTemplateDao;

    // ==================== 其他依赖注入 ====================

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private MeterRegistry meterRegistry;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ApplicationContext applicationContext;

    // ==================== Manager Bean注册 ====================

    /**
     * 配置Manager Bean
     * <p>
     * 注册System配置相关的Manager
     * </p>
     *
     * @return ConfigManager实例
     */
    @Bean
    public ConfigManager configManager() {
        log.info("注册ConfigManager为Spring Bean");
        return new ConfigManager(systemConfigDao, redisTemplate);
    }

    /**
     * 字典Manager Bean
     * <p>
     * 注册字典管理相关的Manager
     * </p>
     *
     * @return DictManager实例
     */
    @Bean
    public DictManager dictManager() {
        log.info("注册DictManager为Spring Bean");
        return new DictManager(systemDictDao, redisTemplate);
    }

    /**
     * 员工Manager Bean
     * <p>
     * 注册员工管理相关的Manager
     * </p>
     *
     * @return EmployeeManager实例
     */
    @Bean
    public EmployeeManager employeeManager() {
        log.info("注册EmployeeManager为Spring Bean");
        return new EmployeeManager(employeeDao);
    }

    /**
     * 通知Manager Bean
     * <p>
     * 注册通知管理相关的Manager
     * 注意：NotificationManagerImpl已经在微服务中通过@Component注册
     * 这里注册的是基类NotificationManager，供其他服务使用
     * </p>
     *
     * @return NotificationManager实例
     */
    @Bean
    public NotificationManager notificationManager() {
        log.info("注册NotificationManager为Spring Bean");
        return new NotificationManager(notificationDao, alertRuleDao);
    }

    /**
     * 系统监控Manager Bean
     *
     * @return SystemMonitorManager实例
     */
    @Bean
    public SystemMonitorManager systemMonitorManager() {
        log.info("注册SystemMonitorManager为Spring Bean");
        return new SystemMonitorManager(systemMonitorDao);
    }

    /**
     * 日志管理Manager Bean
     *
     * @return LogManagementManager实例
     */
    /**
     * 日志管理Manager Bean
     *
     * @return LogManagementManager实例
     */
    @Bean
    public LogManagementManager logManagementManager() {
        log.info("注册LogManagementManager为Spring Bean");
        return new LogManagementManager(systemLogDao);
    }

    /**
     * 性能监控Manager Bean
     *
     * @return PerformanceMonitorManager实例
     */
    @Bean
    public PerformanceMonitorManager performanceMonitorManager() {
        log.info("注册PerformanceMonitorManager为Spring Bean");
        return new PerformanceMonitorManager(systemMonitorDao);
    }

    /**
     * 指标收集Manager Bean
     *
     * @return MetricsCollectorManager实例
     */
    @Bean
    public MetricsCollectorManager metricsCollectorManager() {
        log.info("注册MetricsCollectorManager为Spring Bean");
        return new MetricsCollectorManager(meterRegistry, redisTemplate);
    }

    /**
     * 健康检查Manager Bean
     *
     * @return HealthCheckManager实例
     */
    @Bean
    public HealthCheckManager healthCheckManager() {
        log.info("注册HealthCheckManager为Spring Bean");
        return new HealthCheckManager(discoveryClient, restTemplate, objectMapper);
    }

    /**
     * 认证Manager Bean
     *
     * @return AuthManager实例
     */
    @Bean
    public AuthManager authManager() {
        log.info("注册AuthManager为Spring Bean");
        return new AuthManager(userSessionDao, jwtTokenUtil, stringRedisTemplate);
    }

    /**
     * 审计Manager Bean
     * <p>
     * 注册审计管理相关的Manager
     * 需要传入ObjectMapper和文件存储路径
     * </p>
     *
     * @return AuditManager实例
     */
    @Bean
    public AuditManager auditManager() {
        log.info("注册AuditManager为Spring Bean");
        String exportBasePath = "./exports/audit"; // 可从配置文件中读取
        String archiveBasePath = "./archives/audit"; // 可从配置文件中读取
        return new AuditManager(auditLogDao, auditArchiveDao, objectMapper, exportBasePath, archiveBasePath);
    }

    /**
     * 统一缓存Manager Bean
     * <p>
     * 注册统一缓存管理器，支持多级缓存、缓存击穿防护和缓存命中率监控
     * </p>
     *
     * @return UnifiedCacheManager实例
     */
    @Bean
    public UnifiedCacheManager unifiedCacheManager() {
        log.info("注册UnifiedCacheManager为Spring Bean（支持缓存击穿防护和命中率监控）");
        // 尝试从Spring容器获取RedissonClient（如果已配置）
        RedissonClient redissonClient = null;
        try {
            redissonClient = applicationContext.getBean(RedissonClient.class);
            log.info("RedissonClient已配置，缓存击穿防护功能已启用");
        } catch (Exception e) {
            log.warn("RedissonClient未配置，缓存击穿防护功能将不可用。请配置RedissonClient Bean。");
        }
        return new UnifiedCacheManager(redisTemplate, redissonClient, meterRegistry);
    }

    /**
     * 通知配置Manager Bean
     * <p>
     * 注册通知配置管理相关的Manager
     * 负责通知配置的获取、缓存、解密等管理功能
     * </p>
     *
     * @return NotificationConfigManager实例
     */
    @Bean
    public net.lab1024.sa.common.notification.manager.NotificationConfigManager notificationConfigManager() {
        log.info("注册NotificationConfigManager为Spring Bean");
        // 创建AESUtil实例（从环境变量获取密钥）
        net.lab1024.sa.common.util.AESUtil aesUtil = new net.lab1024.sa.common.util.AESUtil();
        return new net.lab1024.sa.common.notification.manager.NotificationConfigManager(
                notificationConfigDao,
                unifiedCacheManager(),
                objectMapper,
                aesUtil
        );
    }

    /**
     * 通知限流管理器Bean
     * <p>
     * 统一管理所有通知渠道的限流策略
     * 使用滑动窗口算法实现限流
     * </p>
     *
     * @return NotificationRateLimiter实例
     */
    @Bean
    public net.lab1024.sa.common.notification.manager.NotificationRateLimiter notificationRateLimiter() {
        log.info("注册NotificationRateLimiter为Spring Bean");
        return new net.lab1024.sa.common.notification.manager.NotificationRateLimiter();
    }

    /**
     * 通知重试管理器Bean
     * <p>
     * 统一管理所有通知渠道的重试策略
     * 使用指数退避算法实现重试
     * </p>
     *
     * @return NotificationRetryManager实例
     */
    @Bean
    public net.lab1024.sa.common.notification.manager.NotificationRetryManager notificationRetryManager() {
        log.info("注册NotificationRetryManager为Spring Bean");
        return new net.lab1024.sa.common.notification.manager.NotificationRetryManager();
    }

    /**
     * 通知监控指标收集器Bean
     * <p>
     * 统一收集通知系统的监控指标
     * 集成Micrometer进行指标收集
     * </p>
     *
     * @param meterRegistry Micrometer指标注册表
     * @return NotificationMetricsCollector实例
     */
    @Bean
    public net.lab1024.sa.common.notification.manager.NotificationMetricsCollector notificationMetricsCollector(
            io.micrometer.core.instrument.MeterRegistry meterRegistry) {
        log.info("注册NotificationMetricsCollector为Spring Bean");
        return new net.lab1024.sa.common.notification.manager.NotificationMetricsCollector(meterRegistry);
    }

    /**
     * 通知模板管理器Bean
     * <p>
     * 统一管理通知模板的获取、渲染、缓存等操作
     * 支持变量替换和模板缓存
     * </p>
     *
     * @return NotificationTemplateManager实例
     */
    @Bean
    public net.lab1024.sa.common.notification.manager.NotificationTemplateManager notificationTemplateManager() {
        log.info("注册NotificationTemplateManager为Spring Bean");
        return new net.lab1024.sa.common.notification.manager.NotificationTemplateManager(
                notificationTemplateDao,
                unifiedCacheManager(),
                objectMapper
        );
    }

    /**
     * 工作流审批Manager Bean
     * <p>
     * 注册工作流审批管理相关的Manager
     * 供各个业务模块使用，统一调用OA服务的工作流API
     * </p>
     *
     * @return WorkflowApprovalManager实例
     */
    @Bean
    public WorkflowApprovalManager workflowApprovalManager() {
        log.info("注册WorkflowApprovalManager为Spring Bean");
        return new WorkflowApprovalManager(gatewayServiceClient);
    }
}
