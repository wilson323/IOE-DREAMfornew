package net.lab1024.sa.identity.module.rbac.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.identity.module.rbac.dao.AreaPersonDao;
import net.lab1024.sa.identity.module.rbac.dao.EmployeeDeptDao;
import net.lab1024.sa.identity.module.rbac.dao.RbacResourceDao;
import net.lab1024.sa.identity.module.rbac.dao.RbacRoleResourceDao;
import net.lab1024.sa.identity.module.rbac.dao.RbacUserRoleDao;

/**
 * 权限管理服务
 * <p>
 * 提供RBAC权限管理的核心服务功能，包括角色资源映射、权限查询等
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class PermissionService {

    @Resource
    private RbacUserRoleDao rbacUserRoleDao;

    @Resource
    private RbacRoleResourceDao rbacRoleResourceDao;

    @Resource
    private RbacResourceDao rbacResourceDao;

    @Resource
    private AreaPersonDao areaPersonDao;

    @Resource
    private EmployeeDeptDao employeeDeptDao;

    /**
     * 检查用户是否有指定资源的权限
     *
     * @param userId       用户ID
     * @param resourceCode 资源编码
     * @param action       操作动作
     * @return 是否有权限
     */
    public boolean hasPermission(Long userId, String resourceCode, String action) {
        try {
            // 1. 检查用户角色权限
            List<Long> roleIds = rbacUserRoleDao.getRoleIdsByUserId(userId);
            if (roleIds == null || roleIds.isEmpty()) {
                return false;
            }

            // 2. 检查资源是否存在
            if (!resourceExists(resourceCode)) {
                log.warn("资源不存在: resourceCode={}", resourceCode);
                return false;
            }

            // 3. 检查角色是否有该资源的权限
            return rbacRoleResourceDao.hasRoleResourcePermission(roleIds, resourceCode, action);

        } catch (Exception e) {
            log.error("检查用户权限异常: userId={}, resource={}, action={}",
                    userId, resourceCode, action, e);
            return false;
        }
    }

    /**
     * 检查用户是否有指定资源的任意权限
     *
     * @param userId       用户ID
     * @param resourceCode 资源编码
     * @return 是否有权限
     */
    public boolean hasAnyPermission(Long userId, String resourceCode) {
        return hasPermission(userId, resourceCode, "READ") ||
                hasPermission(userId, resourceCode, "WRITE") ||
                hasPermission(userId, resourceCode, "DELETE") ||
                hasPermission(userId, resourceCode, "APPROVE") ||
                hasPermission(userId, resourceCode, "*");
    }

    /**
     * 获取用户所有权限
     *
     * @param userId 用户ID
     * @return 权限编码集合
     */
    public Set<String> getUserPermissions(Long userId) {
        try {
            // 1. 查询用户角色ID列表
            List<Long> roleIds = rbacUserRoleDao.getRoleIdsByUserId(userId);
            if (roleIds == null || roleIds.isEmpty()) {
                return new HashSet<>();
            }

            // 2. 根据角色ID列表查询权限编码列表
            List<String> permissionCodes = rbacResourceDao.getPermissionCodesByRoleIds(roleIds);

            // 3. 转换为Set并返回
            return new HashSet<>(permissionCodes != null ? permissionCodes : new ArrayList<>());

        } catch (Exception e) {
            log.error("获取用户权限异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色编码集合
     */
    public Set<String> getUserRoles(Long userId) {
        try {
            // 查询 t_rbac_user_role 表获取角色编码列表
            List<String> roleCodes = rbacUserRoleDao.getRoleCodesByUserId(userId);
            return new HashSet<>(roleCodes != null ? roleCodes : new ArrayList<>());

        } catch (Exception e) {
            log.error("获取用户角色异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 获取用户区域权限
     *
     * @param userId 用户ID
     * @return 区域ID集合
     */
    public Set<Long> getUserAreaPermissions(Long userId) {
        try {
            // 查询 t_area_person 表获取区域ID列表
            List<Long> areaIds = areaPersonDao.getAreaIdsByPersonId(userId);
            return new HashSet<>(areaIds != null ? areaIds : new ArrayList<>());

        } catch (Exception e) {
            log.error("获取用户区域权限异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 获取用户部门权限
     * <p>
     * 从员工表查询用户所属的部门ID列表，支持查询直接部门和上级部门
     * </p>
     *
     * @param userId 用户ID（对应员工ID）
     * @return 部门ID集合，如果员工不存在或未分配部门，返回空集合
     */
    public Set<Long> getUserDeptPermissions(Long userId) {
        try {
            // 从数据库查询部门ID列表
            List<Long> deptIdList = employeeDeptDao.getDepartmentIdsByEmployeeId(userId);
            Set<Long> deptIds = new HashSet<>(deptIdList != null ? deptIdList : new ArrayList<>());

            return deptIds;

        } catch (Exception e) {
            log.error("获取用户部门权限异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 检查资源是否存在
     *
     * @param resourceCode 资源编码
     * @return 资源是否存在
     */
    public boolean resourceExists(String resourceCode) {
        try {
            // 查询 t_rbac_resource 表
            return rbacResourceDao.existsByResourceCode(resourceCode);

        } catch (Exception e) {
            log.error("检查资源存在性异常: resourceCode={}", resourceCode, e);
            return false;
        }
    }

    /**
     * 批量检查权限
     *
     * @param userId          用户ID
     * @param resourceActions 资源-动作映射
     * @return 检查结果映射（资源编码-动作 -> 是否有权限）
     */
    public Map<String, Boolean> batchCheckPermissions(Long userId, Map<String, String> resourceActions) {
        Map<String, Boolean> results = new HashMap<>();

        for (Map.Entry<String, String> entry : resourceActions.entrySet()) {
            String resourceCode = entry.getKey();
            String action = entry.getValue();
            boolean hasPermission = hasPermission(userId, resourceCode, action);
            results.put(resourceCode + ":" + action, hasPermission);
        }

        return results;
    }

    /**
     * 获取资源权限统计
     *
     * @param userId 用户ID
     * @return 权限统计信息
     */
    public Map<String, Object> getPermissionStatistics(Long userId) {
        try {
            Map<String, Object> statistics = new HashMap<>();

            Set<String> permissions = getUserPermissions(userId);
            Set<String> roles = getUserRoles(userId);
            Set<Long> areas = getUserAreaPermissions(userId);
            Set<Long> depts = getUserDeptPermissions(userId);

            statistics.put("totalPermissions", permissions.size());
            statistics.put("totalRoles", roles.size());
            statistics.put("totalAreas", areas.size());
            statistics.put("totalDepts", depts.size());
            statistics.put("permissions", permissions);
            statistics.put("roles", roles);
            statistics.put("areas", areas);
            statistics.put("depts", depts);

            return statistics;

        } catch (Exception e) {
            log.error("获取权限统计异常: userId={}", userId, e);
            return new HashMap<>();
        }
    }

    /**
     * 检查用户是否为超级管理员
     *
     * @param userId 用户ID
     * @return 是否为超级管理员
     */
    public boolean isSuperAdmin(Long userId) {
        Set<String> roles = getUserRoles(userId);
        return roles.contains("SUPER_ADMIN");
    }
}