package net.lab1024.sa.common.rbac.manager;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.rbac.dao.RbacResourceDao;
import net.lab1024.sa.common.rbac.dao.RoleDao;
import net.lab1024.sa.common.rbac.dao.RoleResourceDao;
import net.lab1024.sa.common.rbac.dao.UserRoleDao;
import net.lab1024.sa.common.rbac.domain.entity.RoleEntity;
import net.lab1024.sa.common.rbac.domain.entity.RoleResourceEntity;
import net.lab1024.sa.common.rbac.domain.entity.UserRoleEntity;

/**
 * RBAC角色管理器
 * <p>
 * 负责RBAC权限模型中的角色管理功能
 * 严格遵循CLAUDE.md规范:
 * - Manager类在ioedream-common-service中是Spring Bean
 * - 使用@Resource依赖注入
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 企业级特性：
 * - 角色权限分配
 * - 用户角色管理
 * - 权限验证
 * - 权限缓存管理（Redis）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
@SuppressWarnings("null")
public class RbacRoleManager {

    @Resource
    private RoleDao roleDao;

    @Resource
    private RbacResourceDao rbacResourceDao;

    @Resource
    private UserRoleDao userRoleDao;

    @Resource
    private RoleResourceDao roleResourceDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 为角色分配权限
     * <p>
     * 将指定的资源权限分配给角色
     * </p>
     *
     * @param roleId 角色ID
     * @param resourceIds 资源ID列表
     * @return 是否分配成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionsToRole(Long roleId, List<Long> resourceIds) {
        log.info("为角色分配权限，角色ID：{}，资源数量：{}", roleId, resourceIds.size());

        try {
            // 1. 验证角色是否存在
            RoleEntity role = roleDao.selectById(roleId);
            if (role == null) {
                log.warn("角色不存在，角色ID：{}", roleId);
                return false;
            }

            // 2. 删除旧的角色权限关联
            roleResourceDao.delete(new LambdaQueryWrapper<RoleResourceEntity>()
                    .eq(RoleResourceEntity::getRoleId, roleId));

            // 3. 创建新的角色权限关联
            if (resourceIds != null && !resourceIds.isEmpty()) {
                for (Long resourceId : resourceIds) {
                    RoleResourceEntity roleResource = new RoleResourceEntity();
                    roleResource.setRoleId(roleId);
                    roleResource.setResourceId(resourceId);
                    roleResource.setStatus(1); // 1-启用
                    roleResourceDao.insert(roleResource);
                }
            }

            // 4. 清除权限缓存
            clearRolePermissionCache(roleId);

            log.info("角色权限分配成功，角色ID：{}，资源数量：{}", roleId, resourceIds.size());
            return true;

        } catch (Exception e) {
            log.error("为角色分配权限失败，角色ID：{}", roleId, e);
            throw new RuntimeException("分配角色权限失败: " + e.getMessage(), e);
        }
    }

    /**
     * 为用户分配角色
     * <p>
     * 将指定的角色分配给用户
     * </p>
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否分配成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        log.info("为用户分配角色，用户ID：{}，角色数量：{}", userId, roleIds.size());

        try {
            // 1. 删除旧的用户角色关联
            userRoleDao.delete(new LambdaQueryWrapper<UserRoleEntity>()
                    .eq(UserRoleEntity::getUserId, userId));

            // 2. 创建新的用户角色关联
            if (roleIds != null && !roleIds.isEmpty()) {
                for (Long roleId : roleIds) {
                    UserRoleEntity userRole = new UserRoleEntity();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRole.setStatus(1); // 1-启用
                    userRoleDao.insert(userRole);
                }
            }

            // 3. 清除用户权限缓存
            clearUserPermissionCache(userId);

            log.info("用户角色分配成功，用户ID：{}，角色数量：{}", userId, roleIds.size());
            return true;

        } catch (Exception e) {
            log.error("为用户分配角色失败，用户ID：{}", userId, e);
            throw new RuntimeException("分配用户角色失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户的所有角色
     * <p>
     * 查询用户关联的所有角色列表
     * </p>
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> getUserRoleIds(Long userId) {
        log.debug("获取用户角色，用户ID：{}", userId);

        try {
            List<UserRoleEntity> userRoles = userRoleDao.selectList(
                    new LambdaQueryWrapper<UserRoleEntity>()
                            .eq(UserRoleEntity::getUserId, userId)
                            .eq(UserRoleEntity::getStatus, 1)
                            .eq(UserRoleEntity::getDeletedFlag, 0));

            return userRoles.stream()
                    .map(UserRoleEntity::getRoleId)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取用户角色失败，用户ID：{}", userId, e);
            return List.of();
        }
    }

    /**
     * 获取角色的所有权限资源
     * <p>
     * 查询角色关联的所有资源权限列表
     * </p>
     *
     * @param roleId 角色ID
     * @return 资源ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> getRoleResourceIds(Long roleId) {
        log.debug("获取角色权限，角色ID：{}", roleId);

        try {
            // 先从缓存获取
            String cacheKey = "rbac:role:resources:" + roleId;
            @SuppressWarnings("unchecked")
            List<Long> cachedResources = (List<Long>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResources != null) {
                return cachedResources;
            }

            // 从数据库查询
            List<RoleResourceEntity> roleResources = roleResourceDao.selectList(
                    new LambdaQueryWrapper<RoleResourceEntity>()
                            .eq(RoleResourceEntity::getRoleId, roleId)
                            .eq(RoleResourceEntity::getStatus, 1)
                            .eq(RoleResourceEntity::getDeletedFlag, 0));

            List<Long> resourceIds = roleResources.stream()
                    .map(RoleResourceEntity::getResourceId)
                    .collect(Collectors.toList());

            // 存入缓存（有效期30分钟）
            redisTemplate.opsForValue().set(cacheKey, resourceIds, java.time.Duration.ofMinutes(30));

            return resourceIds;

        } catch (Exception e) {
            log.error("获取角色权限失败，角色ID：{}", roleId, e);
            return List.of();
        }
    }

    /**
     * 验证用户是否有指定资源权限
     * <p>
     * 通过用户的角色验证是否有资源访问权限
     * </p>
     *
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否有权限
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long resourceId) {
        log.debug("验证用户权限，用户ID：{}，资源ID：{}", userId, resourceId);

        try {
            // 1. 获取用户的所有角色
            List<Long> roleIds = getUserRoleIds(userId);
            if (roleIds.isEmpty()) {
                return false;
            }

            // 2. 检查任一角色是否有该资源权限
            for (Long roleId : roleIds) {
                List<Long> resourceIds = getRoleResourceIds(roleId);
                if (resourceIds.contains(resourceId)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("验证用户权限失败，用户ID：{}，资源ID：{}", userId, resourceId, e);
            return false;
        }
    }

    /**
     * 获取用户的所有权限资源
     * <p>
     * 通过用户的角色获取所有可访问的资源
     * </p>
     *
     * @param userId 用户ID
     * @return 资源ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> getUserResourceIds(Long userId) {
        log.debug("获取用户权限，用户ID：{}", userId);

        try {
            // 1. 获取用户的所有角色
            List<Long> roleIds = getUserRoleIds(userId);
            if (roleIds.isEmpty()) {
                return List.of();
            }

            // 2. 合并所有角色的资源权限（去重）
            return roleIds.stream()
                    .flatMap(roleId -> getRoleResourceIds(roleId).stream())
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取用户权限失败，用户ID：{}", userId, e);
            return List.of();
        }
    }

    /**
     * 清除角色权限缓存
     *
     * @param roleId 角色ID
     */
    private void clearRolePermissionCache(Long roleId) {
        try {
            String cacheKey = "rbac:role:resources:" + roleId;
            redisTemplate.delete(cacheKey);
            log.debug("清除角色权限缓存，角色ID：{}", roleId);
        } catch (Exception e) {
            log.warn("清除角色权限缓存失败，角色ID：{}", roleId, e);
        }
    }

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     */
    private void clearUserPermissionCache(Long userId) {
        try {
            String cacheKey = "rbac:user:resources:" + userId;
            redisTemplate.delete(cacheKey);
            log.debug("清除用户权限缓存，用户ID：{}", userId);
        } catch (Exception e) {
            log.warn("清除用户权限缓存失败，用户ID：{}", userId, e);
        }
    }
}
