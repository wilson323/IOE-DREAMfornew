package net.lab1024.sa.common.permission.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.common.permission.service.UnifiedPermissionService;
import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;
import net.lab1024.sa.common.permission.domain.dto.PermissionValidationStats;
import net.lab1024.sa.common.permission.domain.enums.PermissionCondition;
import net.lab1024.sa.common.permission.domain.enums.LogicOperator;
import net.lab1024.sa.common.permission.manager.PermissionCacheManager;
import net.lab1024.sa.common.permission.manager.PermissionValidationManager;
import net.lab1024.sa.common.permission.manager.PermissionAuditManager;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 统一权限验证服务实现
 * <p>
 * 企业级权限验证的核心实现，提供：
 * - 高性能权限验证
 * - 多级缓存支持
 * - 异步验证处理
 * - 详细的审计日志
 * - 统计分析功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UnifiedPermissionServiceImpl implements UnifiedPermissionService {

    @Resource
    private PermissionValidationManager validationManager;

    @Resource
    private PermissionCacheManager cacheManager;

    @Resource
    private PermissionAuditManager auditManager;

    /**
     * 异步执行器
     */
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors() * 2,
        r -> {
            Thread thread = new Thread(r, "permission-validation-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        }
    );

    /**
     * 权限验证统计
     */
    private final PermissionValidationStats stats = PermissionValidationStats.createEmpty();

    @Override
    public PermissionValidationResult validatePermission(Long userId, String permission, String resource) {
        long startTime = System.currentTimeMillis();
        PermissionValidationResult result = null;
        boolean cacheHit = false;

        try {
            log.debug("[权限验证] 开始验证用户权限, userId={}, permission={}, resource={}",
                userId, permission, resource);

            // 1. 参数验证
            if (userId == null || permission == null || permission.trim().isEmpty()) {
                result = PermissionValidationResult.failure("INVALID_PARAMS", "用户ID和权限标识不能为空");
                return result;
            }

            // 2. 缓存检查
            String cacheKey = buildPermissionCacheKey(userId, permission, resource);
            result = cacheManager.getValidationResult(cacheKey);
            if (result != null) {
                cacheHit = true;
                log.debug("[权限验证] 缓存命中, userId={}, permission={}", userId, permission);
            } else {
                // 3. 执行权限验证
                result = validationManager.validatePermission(userId, permission, resource);

                // 4. 缓存结果
                if (result.isValid()) {
                    cacheManager.cacheValidationResult(cacheKey, result, 300); // 缓存5分钟
                }
            }

            // 5. 更新统计信息
            long duration = System.currentTimeMillis() - startTime;
            if (result.isValid()) {
                stats.recordSuccess("PERMISSION", duration, cacheHit);
            } else {
                stats.recordFailure("PERMISSION", getErrorType(result.getStatusCode()), duration, cacheHit);
            }

            // 6. 异步审计
            final PermissionValidationResult finalResult = result;
            asyncAudit(() -> auditManager.recordPermissionValidation(userId, permission, resource, finalResult));

            log.debug("[权限验证] 验证完成, userId={}, permission={}, valid={}, duration={}ms",
                userId, permission, result.isValid(), duration);

            return result.withPerformance(duration, cacheHit)
                          .withUser(userId, getUserPermissions(userId), getUserRoles(userId))
                          .withPermission(permission, resource)
                          .withValidationType("PERMISSION")
                          .withValidatePath("UnifiedPermissionService.validatePermission");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[权限验证] 验证异常, userId={}, permission={}, error={}",
                userId, permission, e.getMessage(), e);

            result = PermissionValidationResult.systemError("权限验证异常: " + e.getMessage());
            stats.recordFailure("PERMISSION", "SYSTEM_ERROR", duration, false);

            final PermissionValidationResult errorResult = result;
            asyncAudit(() -> auditManager.recordPermissionValidation(userId, permission, resource, errorResult));
            return result.withPerformance(duration, false);
        }
    }

    @Override
    public PermissionValidationResult validateRole(Long userId, String role, String resource) {
        long startTime = System.currentTimeMillis();
        PermissionValidationResult result = null;
        boolean cacheHit = false;

        try {
            log.debug("[角色验证] 开始验证用户角色, userId={}, role={}, resource={}",
                userId, role, resource);

            // 1. 参数验证
            if (userId == null || role == null || role.trim().isEmpty()) {
                result = PermissionValidationResult.failure("INVALID_PARAMS", "用户ID和角色标识不能为空");
                return result;
            }

            // 2. 缓存检查
            String cacheKey = buildRoleCacheKey(userId, role, resource);
            result = cacheManager.getValidationResult(cacheKey);
            if (result != null) {
                cacheHit = true;
                log.debug("[角色验证] 缓存命中, userId={}, role={}", userId, role);
            } else {
                // 3. 执行角色验证
                result = validationManager.validateRole(userId, role, resource);

                // 4. 缓存结果
                if (result.isValid()) {
                    cacheManager.cacheValidationResult(cacheKey, result, 600); // 缓存10分钟
                }
            }

            // 5. 更新统计信息
            long duration = System.currentTimeMillis() - startTime;
            if (result.isValid()) {
                stats.recordSuccess("ROLE", duration, cacheHit);
            } else {
                stats.recordFailure("ROLE", getErrorType(result.getStatusCode()), duration, cacheHit);
            }

            // 6. 异步审计
            final PermissionValidationResult auditResult = result;
            asyncAudit(() -> auditManager.recordRoleValidation(userId, role, resource, auditResult));

            log.debug("[角色验证] 验证完成, userId={}, role={}, valid={}, duration={}ms",
                userId, role, result.isValid(), duration);

            return result.withPerformance(duration, cacheHit)
                          .withUser(userId, getUserPermissions(userId), getUserRoles(userId))
                          .withRole(role, resource)
                          .withValidationType("ROLE")
                          .withValidatePath("UnifiedPermissionService.validateRole");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[角色验证] 验证异常, userId={}, role={}, error={}",
                userId, role, e.getMessage(), e);

            result = PermissionValidationResult.systemError("角色验证异常: " + e.getMessage());
            stats.recordFailure("ROLE", "SYSTEM_ERROR", duration, false);

            final PermissionValidationResult errorResult = result;
            asyncAudit(() -> auditManager.recordRoleValidation(userId, role, resource, errorResult));
            return result.withPerformance(duration, false);
        }
    }

    @Override
    public PermissionValidationResult validateDataScope(Long userId, String dataType, Object resourceId) {
        long startTime = System.currentTimeMillis();
        PermissionValidationResult result = null;
        boolean cacheHit = false;

        try {
            log.debug("[数据权限验证] 开始验证用户数据权限, userId={}, dataType={}, resourceId={}",
                userId, dataType, resourceId);

            // 1. 参数验证
            if (userId == null || dataType == null || dataType.trim().isEmpty()) {
                result = PermissionValidationResult.failure("INVALID_PARAMS", "用户ID和数据类型不能为空");
                return result;
            }

            // 2. 缓存检查（数据权限缓存时间较短）
            String cacheKey = buildDataScopeCacheKey(userId, dataType, resourceId);
            result = cacheManager.getValidationResult(cacheKey);
            if (result != null) {
                cacheHit = true;
                log.debug("[数据权限验证] 缓存命中, userId={}, dataType={}", userId, dataType);
            } else {
                // 3. 执行数据权限验证
                result = validationManager.validateDataScope(userId, dataType, resourceId);

                // 4. 缓存结果（数据权限缓存时间较短，2分钟）
                if (result.isValid()) {
                    cacheManager.cacheValidationResult(cacheKey, result, 120);
                }
            }

            // 5. 更新统计信息
            long duration = System.currentTimeMillis() - startTime;
            if (result.isValid()) {
                stats.recordSuccess("DATA_SCOPE", duration, cacheHit);
            } else {
                stats.recordFailure("DATA_SCOPE", getErrorType(result.getStatusCode()), duration, cacheHit);
            }

            // 6. 异步审计
            final PermissionValidationResult auditResult = result;
            asyncAudit(() -> auditManager.recordDataScopeValidation(userId, dataType, resourceId, auditResult));

            log.debug("[数据权限验证] 验证完成, userId={}, dataType={}, valid={}, duration={}ms",
                userId, dataType, result.isValid(), duration);

            return result.withPerformance(duration, cacheHit)
                          .withUser(userId, getUserPermissions(userId), getUserRoles(userId))
                          .withValidationType("DATA_SCOPE")
                          .withValidatePath("UnifiedPermissionService.validateDataScope");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[数据权限验证] 验证异常, userId={}, dataType={}, error={}",
                userId, dataType, e.getMessage(), e);

            result = PermissionValidationResult.systemError("数据权限验证异常: " + e.getMessage());
            stats.recordFailure("DATA_SCOPE", "SYSTEM_ERROR", duration, false);

            final PermissionValidationResult errorResult = result;
            asyncAudit(() -> auditManager.recordDataScopeValidation(userId, dataType, resourceId, errorResult));
            return result.withPerformance(duration, false);
        }
    }

    @Override
    public PermissionValidationResult validateConditions(Long userId, PermissionCondition[] conditions, LogicOperator logicOperator) {
        long startTime = System.currentTimeMillis();
        PermissionValidationResult result = null;

        try {
            log.debug("[复合权限验证] 开始验证复合条件, userId={}, conditionCount={}, operator={}",
                userId, conditions != null ? conditions.length : 0, logicOperator);

            // 1. 参数验证
            if (userId == null || conditions == null || conditions.length == 0 || logicOperator == null) {
                result = PermissionValidationResult.failure("INVALID_PARAMS", "参数不能为空");
                return result;
            }

            // 2. 验证逻辑操作符参数
            if (!logicOperator.isValidParameters(conditions.length, 0)) {
                result = PermissionValidationResult.failure("INVALID_PARAMS", "逻辑操作符参数无效");
                return result;
            }

            // 3. 执行复合条件验证
            result = validationManager.validateConditions(userId, conditions, logicOperator);

            // 4. 更新统计信息
            long duration = System.currentTimeMillis() - startTime;
            if (result.isValid()) {
                stats.recordSuccess("COMPOSITE", duration, false);
            } else {
                stats.recordFailure("COMPOSITE", getErrorType(result.getStatusCode()), duration, false);
            }

            // 5. 异步审计
            final PermissionValidationResult auditResult = result;
            asyncAudit(() -> auditManager.recordCompositeValidation(userId, conditions, logicOperator, auditResult));

            log.debug("[复合权限验证] 验证完成, userId={}, operator={}, valid={}, duration={}ms",
                userId, logicOperator.getCode(), result.isValid(), duration);

            return result.withPerformance(duration, false)
                          .withUser(userId, getUserPermissions(userId), getUserRoles(userId))
                          .withValidationType("COMPOSITE")
                          .withValidatePath("UnifiedPermissionService.validateConditions");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[复合权限验证] 验证异常, userId={}, operator={}, error={}",
                userId, logicOperator != null ? logicOperator.getCode() : "null", e.getMessage(), e);

            result = PermissionValidationResult.systemError("复合权限验证异常: " + e.getMessage());
            stats.recordFailure("COMPOSITE", "SYSTEM_ERROR", duration, false);

            final PermissionValidationResult errorResult = result;
            asyncAudit(() -> auditManager.recordCompositeValidation(userId, conditions, logicOperator, errorResult));
            return result.withPerformance(duration, false);
        }
    }

    @Override
    public Set<String> getUserPermissions(Long userId) {
        try {
            // 从缓存获取用户权限
            Set<String> permissions = cacheManager.getUserPermissions(userId);
            if (permissions != null) {
                return permissions;
            }

            // 从数据库获取用户权限
            permissions = validationManager.getUserPermissions(userId);

            // 缓存用户权限（10分钟）
            if (permissions != null && !permissions.isEmpty()) {
                cacheManager.cacheUserPermissions(userId, permissions, 600);
            }

            return permissions != null ? permissions : Collections.emptySet();

        } catch (Exception e) {
            log.error("[获取用户权限] 异常, userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    @Override
    public Set<String> getUserRoles(Long userId) {
        try {
            // 从缓存获取用户角色
            Set<String> roles = cacheManager.getUserRoles(userId);
            if (roles != null) {
                return roles;
            }

            // 从数据库获取用户角色
            roles = validationManager.getUserRoles(userId);

            // 缓存用户角色（15分钟，角色相对稳定）
            if (roles != null && !roles.isEmpty()) {
                cacheManager.cacheUserRoles(userId, roles, 900);
            }

            return roles != null ? roles : Collections.emptySet();

        } catch (Exception e) {
            log.error("[获取用户角色] 异常, userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    @Override
    public boolean hasAreaPermission(Long userId, Long areaId, String permission) {
        try {
            PermissionValidationResult result = validatePermission(userId, "AREA:" + areaId + ":" + permission, "area:" + areaId);
            return result.isValid();
        } catch (Exception e) {
            log.error("[区域权限检查] 异常, userId={}, areaId={}, permission={}, error={}",
                userId, areaId, permission, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hasDevicePermission(Long userId, String deviceId, String permission) {
        try {
            PermissionValidationResult result = validatePermission(userId, "DEVICE:" + deviceId + ":" + permission, "device:" + deviceId);
            return result.isValid();
        } catch (Exception e) {
            log.error("[设备权限检查] 异常, userId={}, deviceId={}, permission={}, error={}",
                userId, deviceId, permission, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hasModulePermission(Long userId, String moduleCode, String permission) {
        try {
            PermissionValidationResult result = validatePermission(userId, "MODULE:" + moduleCode + ":" + permission, "module:" + moduleCode);
            return result.isValid();
        } catch (Exception e) {
            log.error("[模块权限检查] 异常, userId={}, moduleCode={}, permission={}, error={}",
                userId, moduleCode, permission, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void refreshUserPermissionCache(Long userId) {
        try {
            log.debug("[刷新用户权限缓存] 开始, userId={}", userId);

            // 清除缓存
            cacheManager.evictUserPermissions(userId);
            cacheManager.evictUserRoles(userId);

            // 预热缓存
            getUserPermissions(userId);
            getUserRoles(userId);

            log.debug("[刷新用户权限缓存] 完成, userId={}", userId);

        } catch (Exception e) {
            log.error("[刷新用户权限缓存] 异常, userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    @Override
    public void refreshUserPermissionCache(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        log.debug("[批量刷新用户权限缓存] 开始, count={}", userIds.size());

        // 并行刷新多个用户的权限缓存
        CompletableFuture.allOf(
            userIds.stream()
                .map(userId -> CompletableFuture.runAsync(() -> refreshUserPermissionCache(userId), asyncExecutor))
                .toArray(CompletableFuture[]::new)
        ).join();

        log.debug("[批量刷新用户权限缓存] 完成, count={}", userIds.size());
    }

    @Override
    public void preloadUserPermissions(Long userId) {
        try {
            log.debug("[预加载用户权限] 开始, userId={}", userId);

            // 预加载权限和角色
            getUserPermissions(userId);
            getUserRoles(userId);

            log.debug("[预加载用户权限] 完成, userId={}", userId);

        } catch (Exception e) {
            log.error("[预加载用户权限] 异常, userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    @Override
    public PermissionValidationStats getValidationStats() {
        stats.updateEndTime();
        return stats;
    }

    // ==================== 私有方法 ====================

    /**
     * 构建权限缓存键
     */
    private String buildPermissionCacheKey(Long userId, String permission, String resource) {
        return String.format("permission:validation:user:%d:permission:%s:resource:%s",
            userId, permission, resource != null ? resource : "");
    }

    /**
     * 构建角色缓存键
     */
    private String buildRoleCacheKey(Long userId, String role, String resource) {
        return String.format("role:validation:user:%d:role:%s:resource:%s",
            userId, role, resource != null ? resource : "");
    }

    /**
     * 构建数据权限缓存键
     */
    private String buildDataScopeCacheKey(Long userId, String dataType, Object resourceId) {
        return String.format("data_scope:validation:user:%d:type:%s:resource:%s",
            userId, dataType, resourceId != null ? resourceId.toString() : "");
    }

    /**
     * 获取错误类型
     */
    private String getErrorType(Integer statusCode) {
        if (statusCode == null) {
            return "UNKNOWN";
        }
        return switch (statusCode) {
            case 403 -> "FORBIDDEN";
            case 401 -> "UNAUTHENTICATED";
            case 404 -> "PERMISSION_NOT_FOUND";
            case 500 -> "SYSTEM_ERROR";
            default -> "UNKNOWN";
        };
    }

    /**
     * 异步审计
     */
    private void asyncAudit(Runnable auditTask) {
        try {
            CompletableFuture.runAsync(auditTask, asyncExecutor)
                .exceptionally(throwable -> {
                    log.warn("[权限审计] 异步审计异常", throwable);
                    return null;
                });
        } catch (Exception e) {
            log.warn("[权限审计] 提交异步审计任务异常", e);
        }
    }
}
