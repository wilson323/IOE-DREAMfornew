package net.lab1024.sa.common.permission.manager;

import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;
import net.lab1024.sa.common.permission.domain.enums.PermissionCondition;
import net.lab1024.sa.common.permission.domain.enums.LogicOperator;

import java.util.Set;

/**
 * 权限验证管理器
 * <p>
 * 企业级权限验证逻辑的核心执行组件，提供：
 * - 高性能权限验证算法
 * - 多种权限验证模式
 * - 复合条件逻辑处理
 * - 数据权限验证
 * - 区域权限验证
 * - 设备权限验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface PermissionValidationManager {

    /**
     * 验证用户权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @param resource 资源标识（可选）
     * @return 权限验证结果
     */
    PermissionValidationResult validatePermission(Long userId, String permission, String resource);

    /**
     * 验证用户角色
     *
     * @param userId 用户ID
     * @param role 角色标识
     * @param resource 资源标识（可选）
     * @return 权限验证结果
     */
    PermissionValidationResult validateRole(Long userId, String role, String resource);

    /**
     * 验证用户数据权限
     *
     * @param userId 用户ID
     * @param dataType 数据类型（如：DEPARTMENT、AREA等）
     * @param resourceId 资源ID
     * @return 权限验证结果
     */
    PermissionValidationResult validateDataScope(Long userId, String dataType, Object resourceId);

    /**
     * 验证复合权限条件
     * 支持AND/OR逻辑组合
     *
     * @param userId 用户ID
     * @param conditions 权限条件列表
     * @param logicOperator 逻辑操作符
     * @return 权限验证结果
     */
    PermissionValidationResult validateConditions(Long userId, PermissionCondition[] conditions, LogicOperator logicOperator);

    /**
     * 验证区域权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param permission 权限标识
     * @return 权限验证结果
     */
    PermissionValidationResult validateAreaPermission(Long userId, Long areaId, String permission);

    /**
     * 验证设备权限
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param permission 权限标识
     * @return 权限验证结果
     */
    PermissionValidationResult validateDevicePermission(Long userId, String deviceId, String permission);

    /**
     * 验证模块权限
     *
     * @param userId 用户ID
     * @param moduleCode 模块编码
     * @param permission 权限标识
     * @return 权限验证结果
     */
    PermissionValidationResult validateModulePermission(Long userId, String moduleCode, String permission);

    /**
     * 验证时间权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 权限验证结果
     */
    PermissionValidationResult validateTimePermission(Long userId, String permission, long startTime, long endTime);

    /**
     * 验证IP权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @param clientIp 客户端IP
     * @return 权限验证结果
     */
    PermissionValidationResult validateIpPermission(Long userId, String permission, String clientIp);

    /**
     * 验证次数权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @param timeWindow 时间窗口（秒）
     * @param maxCount 最大次数
     * @return 权限验证结果
     */
    PermissionValidationResult validateCountPermission(Long userId, String permission, int timeWindow, int maxCount);

    /**
     * 批量验证权限
     *
     * @param userId 用户ID
     * @param permissions 权限标识列表
     * @param logicOperator 逻辑操作符（AND/OR）
     * @return 权限验证结果
     */
    PermissionValidationResult validatePermissions(Long userId, String[] permissions, LogicOperator logicOperator);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限标识集合
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色标识集合
     */
    Set<String> getUserRoles(Long userId);

    /**
     * 获取用户数据权限范围
     *
     * @param userId 用户ID
     * @param dataType 数据类型
     * @return 数据权限范围
     */
    DataScopeResult getUserDataScope(Long userId, String dataType);

    /**
     * 获取用户可访问区域列表
     *
     * @param userId 用户ID
     * @return 区域ID列表
     */
    Set<Long> getUserAccessibleAreas(Long userId);

    /**
     * 获取用户可访问设备列表
     *
     * @param userId 用户ID
     * @return 设备ID列表
     */
    Set<String> getUserAccessibleDevices(Long userId);

    /**
     * 检查用户是否为超级管理员
     *
     * @param userId 用户ID
     * @return 是否为超级管理员
     */
    boolean isSuperAdmin(Long userId);

    /**
     * 检查用户是否为系统管理员
     *
     * @param userId 用户ID
     * @return 是否为系统管理员
     */
    boolean isSystemAdmin(Long userId);

    /**
     * 检查用户是否为租户管理员
     *
     * @param userId 用户ID
     * @return 是否为租户管理员
     */
    boolean isTenantAdmin(Long userId);

    /**
     * 检查用户是否为部门管理员
     *
     * @param userId 用户ID
     * @return 是否为部门管理员
     */
    boolean isDepartmentAdmin(Long userId);

    /**
     * 获取用户权限继承路径
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 权限继承路径
     */
    PermissionInheritancePath getPermissionInheritancePath(Long userId, String permission);

    /**
     * 验证权限继承链
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @param resource 资源标识
     * @return 权限验证结果
     */
    PermissionValidationResult validatePermissionInheritance(Long userId, String permission, String resource);

    /**
     * 数据权限结果
     */
    class DataScopeResult {
        private final int dataScope;
        private final Set<Long> departmentIds;
        private final Set<Long> areaIds;
        private final Set<String> customRules;

        public DataScopeResult(int dataScope, Set<Long> departmentIds, Set<Long> areaIds, Set<String> customRules) {
            this.dataScope = dataScope;
            this.departmentIds = departmentIds;
            this.areaIds = areaIds;
            this.customRules = customRules;
        }

        public int getDataScope() {
            return dataScope;
        }

        public Set<Long> getDepartmentIds() {
            return departmentIds;
        }

        public Set<Long> getAreaIds() {
            return areaIds;
        }

        public Set<String> getCustomRules() {
            return customRules;
        }

        public boolean hasAccessToDepartment(Long departmentId) {
            switch (dataScope) {
                case 1: // 全部数据
                    return true;
                case 2: // 自定义数据
                    return departmentIds != null && departmentIds.contains(departmentId);
                case 3: // 本部门数据
                case 4: // 本部门及子部门数据
                    return departmentIds != null && departmentIds.contains(departmentId);
                case 5: // 仅本人数据
                    return false;
                default:
                    return false;
            }
        }

        public boolean hasAccessToArea(Long areaId) {
            switch (dataScope) {
                case 1: // 全部数据
                    return true;
                case 2: // 自定义数据
                    return areaIds != null && areaIds.contains(areaId);
                case 3: // 本部门数据
                case 4: // 本部门及子部门数据
                case 5: // 仅本人数据
                    return areaIds != null && areaIds.contains(areaId);
                default:
                    return false;
            }
        }

        public boolean hasCustomAccess(String rule) {
            return customRules != null && customRules.contains(rule);
        }
    }

    /**
     * 权限继承路径
     */
    class PermissionInheritancePath {
        private final String permission;
        private final Set<String> directRoles;
        private final Set<String> inheritedRoles;
        private final Set<String> directPermissions;
        private final Set<String> inheritedPermissions;
        private final int inheritanceLevel;

        public PermissionInheritancePath(String permission, Set<String> directRoles, Set<String> inheritedRoles,
                                       Set<String> directPermissions, Set<String> inheritedPermissions, int inheritanceLevel) {
            this.permission = permission;
            this.directRoles = directRoles;
            this.inheritedRoles = inheritedRoles;
            this.directPermissions = directPermissions;
            this.inheritedPermissions = inheritedPermissions;
            this.inheritanceLevel = inheritanceLevel;
        }

        public String getPermission() {
            return permission;
        }

        public Set<String> getDirectRoles() {
            return directRoles;
        }

        public Set<String> getInheritedRoles() {
            return inheritedRoles;
        }

        public Set<String> getDirectPermissions() {
            return directPermissions;
        }

        public Set<String> getInheritedPermissions() {
            return inheritedPermissions;
        }

        public int getInheritanceLevel() {
            return inheritanceLevel;
        }

        public boolean hasDirectPermission() {
            return directPermissions != null && directPermissions.contains(permission);
        }

        public boolean hasInheritedPermission() {
            return inheritedPermissions != null && inheritedPermissions.contains(permission);
        }

        public Set<String> getAllRoles() {
            Set<String> allRoles = new java.util.HashSet<>();
            if (directRoles != null) {
                allRoles.addAll(directRoles);
            }
            if (inheritedRoles != null) {
                allRoles.addAll(inheritedRoles);
            }
            return allRoles;
        }

        public Set<String> getAllPermissions() {
            Set<String> allPermissions = new java.util.HashSet<>();
            if (directPermissions != null) {
                allPermissions.addAll(directPermissions);
            }
            if (inheritedPermissions != null) {
                allPermissions.addAll(inheritedPermissions);
            }
            return allPermissions;
        }
    }

    /**
     * 权限验证配置
     */
    class ValidationConfig {
        private final boolean enableCache;
        private final boolean enableAudit;
        private final boolean enableInheritance;
        private final boolean enableDataScope;
        private final int maxInheritanceLevel;
        private final long validationTimeoutMs;
        private final boolean enableParallelValidation;

        public ValidationConfig(boolean enableCache, boolean enableAudit, boolean enableInheritance,
                               boolean enableDataScope, int maxInheritanceLevel, long validationTimeoutMs,
                               boolean enableParallelValidation) {
            this.enableCache = enableCache;
            this.enableAudit = enableAudit;
            this.enableInheritance = enableInheritance;
            this.enableDataScope = enableDataScope;
            this.maxInheritanceLevel = maxInheritanceLevel;
            this.validationTimeoutMs = validationTimeoutMs;
            this.enableParallelValidation = enableParallelValidation;
        }

        /**
         * 默认配置
         */
        public static ValidationConfig defaultConfig() {
            return new ValidationConfig(
                true,                     // 启用缓存
                true,                     // 启用审计
                true,                     // 启用继承
                true,                     // 启用数据权限
                5,                        // 最大继承层级
                5000L,                    // 验证超时5秒
                false                     // 禁用并行验证
            );
        }

        /**
         * 高性能配置
         */
        public static ValidationConfig highPerformanceConfig() {
            return new ValidationConfig(
                true,                     // 启用缓存
                false,                    // 禁用审计（提升性能）
                true,                     // 启用继承
                true,                     // 启用数据权限
                3,                        // 最大继承层级（减少层级）
                3000L,                    // 验证超时3秒
                true                      // 启用并行验证
            );
        }

        /**
         * 高安全配置
         */
        public static ValidationConfig highSecurityConfig() {
            return new ValidationConfig(
                true,                     // 启用缓存
                true,                     // 启用审计
                true,                     // 启用继承
                true,                     // 启用数据权限
                10,                       // 最大继承层级
                10000L,                   // 验证超时10秒
                false                     // 禁用并行验证（确保安全）
            );
        }

        public boolean isEnableCache() {
            return enableCache;
        }

        public boolean isEnableAudit() {
            return enableAudit;
        }

        public boolean isEnableInheritance() {
            return enableInheritance;
        }

        public boolean isEnableDataScope() {
            return enableDataScope;
        }

        public int getMaxInheritanceLevel() {
            return maxInheritanceLevel;
        }

        public long getValidationTimeoutMs() {
            return validationTimeoutMs;
        }

        public boolean isEnableParallelValidation() {
            return enableParallelValidation;
        }
    }
}