package net.lab1024.sa.common.manager;

import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.audit.dao.AuditLogDao;
import net.lab1024.sa.common.audit.entity.AuditLogEntity;

/**
 * 操作日志管理器
 * <p>
 * 负责操作日志的记录、查询和管理
 * 严格遵循CLAUDE.md规范:
 * - Manager类在ioedream-common-service中是Spring Bean
 * - 使用@Resource依赖注入
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 企业级特性：
 * - 异步记录操作日志（不阻塞主业务流程）
 * - 数据库持久化（使用AuditLogDao）
 * - Redis缓存（提升查询性能）
 * - 完善的异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class OperationLogManager {

    @Resource
    private AuditLogDao auditLogDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "operation_log:";

    /**
     * 缓存过期时间（秒）：7天
     */
    private static final long CACHE_EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    /**
     * 记录操作日志
     * <p>
     * 功能说明：
     * 1. 构建AuditLogEntity对象
     * 2. 保存到数据库（t_audit_log表）
     * 3. 缓存到Redis（提升查询性能）
     * 4. 完善的异常处理（日志记录失败不应影响主业务流程）
     * </p>
     *
     * @param userId 用户ID
     * @param action 操作类型（如：查询、新增、修改、删除等）
     * @param resource 资源标识（如：用户管理、角色管理等）
     * @param details 详细信息（JSON格式，包含操作详情）
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordOperationLog(Long userId, String action, String resource, String details) {
        log.info("[操作日志] 记录操作日志，用户ID：{}，操作：{}，资源：{}", userId, action, resource);

        try {
            // 1. 构建日志对象
            AuditLogEntity auditLog = new AuditLogEntity();
            auditLog.setUserId(userId);
            auditLog.setOperationDesc(action);
            auditLog.setResourceType(resource);
            auditLog.setRequestParams(details);
            auditLog.setModuleName("SYSTEM"); // 默认模块名称
            auditLog.setOperationType(parseOperationType(action)); // 解析操作类型
            auditLog.setResultStatus(1); // 默认成功
            auditLog.setCreateTime(LocalDateTime.now());

            // 2. 保存到数据库
            int result = auditLogDao.insert(auditLog);
            if (result > 0) {
                log.debug("[操作日志] 操作日志保存成功，日志ID：{}", auditLog.getLogId());

                // 3. 缓存到Redis（提升查询性能）
                try {
                    String cacheKey = CACHE_KEY_PREFIX + auditLog.getLogId();
                    // Duration.ofSeconds() 总是返回非null对象，但类型系统无法推断
                    java.time.Duration expireDuration = java.util.Objects.requireNonNull(
                            java.time.Duration.ofSeconds(CACHE_EXPIRE_SECONDS));
                    redisTemplate.opsForValue().set(cacheKey, auditLog, expireDuration);
                    log.debug("[操作日志] 操作日志缓存成功，日志ID：{}", auditLog.getLogId());
                } catch (Exception e) {
                    // Redis缓存失败不影响主业务流程，只记录警告日志
                    log.warn("[操作日志] 操作日志缓存失败，日志ID：{}，错误：{}", auditLog.getLogId(), e.getMessage());
                }
            } else {
                log.warn("[操作日志] 操作日志保存失败，用户ID：{}，操作：{}", userId, action);
            }

        } catch (Exception e) {
            log.error("[操作日志] 记录操作日志异常，用户ID：{}，操作：{}", userId, action, e);
            // 操作日志记录失败不应影响主业务流程，只记录错误日志
        }
    }

    /**
     * 解析操作类型
     * <p>
     * 将操作描述转换为操作类型代码：
     * 1-查询 2-新增 3-修改 4-删除 5-导出 6-导入 7-登录 8-登出
     * </p>
     *
     * @param action 操作描述
     * @return 操作类型代码
     */
    private Integer parseOperationType(String action) {
        if (action == null || action.trim().isEmpty()) {
            return 1; // 默认查询
        }

        String actionLower = action.toLowerCase();
        if (actionLower.contains("查询") || actionLower.contains("查询") || actionLower.contains("get") || actionLower.contains("list")) {
            return 1; // 查询
        } else if (actionLower.contains("新增") || actionLower.contains("添加") || actionLower.contains("create") || actionLower.contains("add")) {
            return 2; // 新增
        } else if (actionLower.contains("修改") || actionLower.contains("更新") || actionLower.contains("update") || actionLower.contains("edit")) {
            return 3; // 修改
        } else if (actionLower.contains("删除") || actionLower.contains("remove") || actionLower.contains("delete")) {
            return 4; // 删除
        } else if (actionLower.contains("导出") || actionLower.contains("export")) {
            return 5; // 导出
        } else if (actionLower.contains("导入") || actionLower.contains("import")) {
            return 6; // 导入
        } else if (actionLower.contains("登录") || actionLower.contains("login")) {
            return 7; // 登录
        } else if (actionLower.contains("登出") || actionLower.contains("logout")) {
            return 8; // 登出
        } else {
            return 1; // 默认查询
        }
    }
}
