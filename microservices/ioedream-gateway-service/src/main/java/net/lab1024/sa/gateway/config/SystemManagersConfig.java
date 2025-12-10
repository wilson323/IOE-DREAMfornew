package net.lab1024.sa.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.audit.dao.AuditArchiveDao;
import net.lab1024.sa.common.audit.dao.AuditLogDao;
import net.lab1024.sa.common.audit.manager.AuditManager;
import net.lab1024.sa.common.attendance.manager.AttendanceManager;
import net.lab1024.sa.common.cache.UnifiedCacheManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.notification.dao.NotificationConfigDao;
import net.lab1024.sa.common.notification.dao.NotificationTemplateDao;
import net.lab1024.sa.common.notification.manager.NotificationConfigManager;
import net.lab1024.sa.common.notification.manager.NotificationRetryManager;
import net.lab1024.sa.common.notification.manager.NotificationTemplateManager;
import net.lab1024.sa.common.system.dao.SystemConfigDao;
import net.lab1024.sa.common.system.dao.SystemDictDao;
import net.lab1024.sa.common.system.employee.dao.EmployeeDao;
import net.lab1024.sa.common.system.employee.manager.EmployeeManager;
import net.lab1024.sa.common.system.manager.ConfigManager;
import net.lab1024.sa.common.system.manager.DictManager;
import net.lab1024.sa.common.util.AESUtil;
import net.lab1024.sa.common.workflow.dao.ApprovalConfigDao;
import net.lab1024.sa.common.workflow.manager.ApprovalConfigManager;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;

/**
 * 系统及业务Manager统一配置类
 * <p>
 * 符合CLAUDE.md规范 - Manager类通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 注册所有系统和业务相关Manager为Spring Bean
 * - 统一管理Manager的依赖注入
 * </p>
 * <p>
 * 包含的Manager：
 * - ConfigManager: 系统配置管理
 * - DictManager: 字典管理
 * - EmployeeManager: 员工管理
 * - AuditManager: 审计管理
 * - AttendanceManager: 考勤管理
 * - NotificationRetryManager: 通知重试管理
 * - NotificationTemplateManager: 通知模板管理
 * - NotificationConfigManager: 通知配置管理
 * - WorkflowApprovalManager: 工作流审批管理
 * - ApprovalConfigManager: 审批配置管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Configuration
public class SystemManagersConfig {

    // ==================== System DAOs ====================
    @Resource
    private SystemConfigDao systemConfigDao;
    
    @Resource
    private SystemDictDao systemDictDao;
    
    @Resource
    private EmployeeDao employeeDao;
    
    // ==================== Audit DAOs ====================
    @Resource
    private AuditLogDao auditLogDao;
    
    @Resource
    private AuditArchiveDao auditArchiveDao;
    
    // ==================== Notification DAOs ====================
    @Resource
    private NotificationTemplateDao notificationTemplateDao;
    
    @Resource
    private NotificationConfigDao notificationConfigDao;
    
    // ==================== Workflow DAOs ====================
    @Resource
    private ApprovalConfigDao approvalConfigDao;
    
    // ==================== Common Dependencies ====================
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Resource
    private GatewayServiceClient gatewayServiceClient;
    
    @Resource
    private UnifiedCacheManager unifiedCacheManager;
    
    @Resource
    private AESUtil aesUtil;
    
    // ==================== Configuration Properties ====================
    @Value("${audit.export.path:./exports/audit}")
    private String auditExportPath;
    
    @Value("${audit.archive.path:./archives/audit}")
    private String auditArchivePath;
    
    @Value("${notification.retry.max-retries:3}")
    private int notificationMaxRetries;
    
    @Value("${notification.retry.base-delay-ms:1000}")
    private long notificationBaseDelayMs;

    // ==================== System Managers ====================

    @Bean
    public ConfigManager configManager() {
        log.info("[ConfigManager] 初始化企业级配置管理器");
        ConfigManager manager = new ConfigManager(systemConfigDao, redisTemplate);
        log.info("[ConfigManager] 企业级配置管理器初始化完成");
        return manager;
    }

    @Bean
    public DictManager dictManager() {
        log.info("[DictManager] 初始化企业级字典管理器");
        DictManager manager = new DictManager(systemDictDao, redisTemplate);
        log.info("[DictManager] 企业级字典管理器初始化完成");
        return manager;
    }

    @Bean
    public EmployeeManager employeeManager() {
        log.info("[EmployeeManager] 初始化企业级员工管理器");
        EmployeeManager manager = new EmployeeManager(employeeDao);
        log.info("[EmployeeManager] 企业级员工管理器初始化完成");
        return manager;
    }

    // ==================== Audit Managers ====================

    @Bean
    public AuditManager auditManager() {
        log.info("[AuditManager] 初始化企业级审计管理器");
        AuditManager manager = new AuditManager(
                auditLogDao,
                auditArchiveDao,
                objectMapper,
                auditExportPath,
                auditArchivePath
        );
        log.info("[AuditManager] 企业级审计管理器初始化完成");
        log.info("[AuditManager] 导出路径：{}", auditExportPath);
        log.info("[AuditManager] 归档路径：{}", auditArchivePath);
        return manager;
    }

    // ==================== Attendance Managers ====================

    @Bean
    public AttendanceManager attendanceManager() {
        log.info("[AttendanceManager] 初始化企业级考勤管理器");
        log.info("[AttendanceManager] 依赖注入 - GatewayServiceClient: {}", gatewayServiceClient != null ? "已注入" : "未注入");
        AttendanceManager manager = new AttendanceManager(gatewayServiceClient);
        log.info("[AttendanceManager] 企业级考勤管理器初始化完成");
        log.info("[AttendanceManager] 功能支持: 考勤审批后处理、年假扣除、考勤统计更新、请假/加班/调班/补签/出差审批处理");
        return manager;
    }

    // ==================== Notification Managers ====================

    @Bean
    public NotificationRetryManager notificationRetryManager() {
        log.info("[NotificationRetryManager] 初始化企业级通知重试管理器");
        log.info("[NotificationRetryManager] 配置参数 - maxRetries: {}, baseDelayMs: {}ms", notificationMaxRetries, notificationBaseDelayMs);
        NotificationRetryManager manager = new NotificationRetryManager(notificationMaxRetries, notificationBaseDelayMs);
        log.info("[NotificationRetryManager] 企业级通知重试管理器初始化完成");
        log.info("[NotificationRetryManager] 功能支持: 指数退避重试策略、自定义重试次数和延迟、特定错误码跳过重试、线程安全");
        return manager;
    }

    @Bean
    public NotificationTemplateManager notificationTemplateManager() {
        log.info("[NotificationTemplateManager] 初始化企业级通知模板管理器");
        log.info("[NotificationTemplateManager] 依赖注入 - NotificationTemplateDao: {}, UnifiedCacheManager: {}, ObjectMapper: {}",
                notificationTemplateDao != null ? "已注入" : "未注入",
                unifiedCacheManager != null ? "已注入" : "未注入",
                objectMapper != null ? "已注入" : "未注入");
        NotificationTemplateManager manager = new NotificationTemplateManager(
                notificationTemplateDao,
                unifiedCacheManager,
                objectMapper
        );
        log.info("[NotificationTemplateManager] 企业级通知模板管理器初始化完成");
        log.info("[NotificationTemplateManager] 功能支持: 模板缓存(多级缓存)、变量替换({{variable}}格式)、模板验证、模板热更新");
        return manager;
    }

    @Bean
    public NotificationConfigManager notificationConfigManager() {
        log.info("[NotificationConfigManager] 初始化企业级通知配置管理器");
        log.info("[NotificationConfigManager] 依赖注入 - NotificationConfigDao: {}, UnifiedCacheManager: {}, ObjectMapper: {}, AESUtil: {}",
                notificationConfigDao != null ? "已注入" : "未注入",
                unifiedCacheManager != null ? "已注入" : "未注入",
                objectMapper != null ? "已注入" : "未注入",
                aesUtil != null ? "已注入" : "未注入");
        NotificationConfigManager manager = new NotificationConfigManager(
                notificationConfigDao,
                unifiedCacheManager,
                objectMapper,
                aesUtil
        );
        log.info("[NotificationConfigManager] 企业级通知配置管理器初始化完成");
        log.info("[NotificationConfigManager] 功能支持: 多级缓存(L1本地+L2Redis)、配置加密解密、配置热更新、配置验证");
        return manager;
    }

    // ==================== Workflow Managers ====================

    @Bean
    public ApprovalConfigManager approvalConfigManager() {
        log.info("[ApprovalConfigManager] 初始化企业级审批配置管理器");
        log.info("[ApprovalConfigManager] 依赖注入 - ApprovalConfigDao: {}",
                approvalConfigDao != null ? "已注入" : "未注入");
        ApprovalConfigManager manager = new ApprovalConfigManager(approvalConfigDao);
        log.info("[ApprovalConfigManager] 企业级审批配置管理器初始化完成");
        log.info("[ApprovalConfigManager] 功能支持: 业务类型配置查询、流程定义ID获取、审批规则解析、配置有效期验证");
        return manager;
    }

    @Bean
    public WorkflowApprovalManager workflowApprovalManager(ApprovalConfigManager approvalConfigManager) {
        log.info("[WorkflowApprovalManager] 初始化企业级工作流审批管理器");
        log.info("[WorkflowApprovalManager] 依赖注入 - GatewayServiceClient: {}, ApprovalConfigManager: {}",
                gatewayServiceClient != null ? "已注入" : "未注入",
                approvalConfigManager != null ? "已注入" : "未注入");
        WorkflowApprovalManager manager = new WorkflowApprovalManager(
                gatewayServiceClient,
                approvalConfigManager
        );
        log.info("[WorkflowApprovalManager] 企业级工作流审批管理器初始化完成");
        log.info("[WorkflowApprovalManager] 功能支持: 启动审批流程、同意/驳回/转办/委派任务、撤销流程、待办/已办查询、流程历史");
        return manager;
    }
}
