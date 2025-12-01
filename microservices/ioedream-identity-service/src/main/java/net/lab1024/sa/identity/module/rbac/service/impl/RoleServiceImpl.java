package net.lab1024.sa.identity.module.rbac.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.identity.module.rbac.dao.RbacRoleDao;
import net.lab1024.sa.identity.module.rbac.dao.RbacRoleResourceDao;
import net.lab1024.sa.identity.module.rbac.dao.RbacUserRoleDao;
import net.lab1024.sa.identity.module.rbac.domain.entity.RbacRoleEntity;
import net.lab1024.sa.identity.module.rbac.domain.entity.RbacRoleResourceEntity;
import net.lab1024.sa.identity.module.rbac.domain.entity.RbacUserRoleEntity;
import net.lab1024.sa.identity.module.rbac.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

/**
 * 角色管理服务实现
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {

    @Resource
    private RbacRoleDao rbacRoleDao;

    @Resource
    private RbacRoleResourceDao rbacRoleResourceDao;

    @Resource
    private RbacUserRoleDao rbacUserRoleDao;

    @Override
    public List<RbacRoleEntity> getRoleList(Integer status) {
        try {
            QueryWrapper<RbacRoleEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("deleted_flag", 0);
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            queryWrapper.orderByAsc("sort_order");

            return rbacRoleDao.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取角色列表异常", e);
            throw new RuntimeException("获取角色列表失败", e);
        }
    }

    @Override
    public RbacRoleEntity getRoleById(Long roleId) {
        try {
            QueryWrapper<RbacRoleEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("role_id", roleId);
            queryWrapper.eq("deleted_flag", 0);

            return rbacRoleDao.selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("获取角色详情异常: roleId={}", roleId, e);
            throw new RuntimeException("获取角色详情失败", e);
        }
    }

    @Override
    public Long createRole(RbacRoleEntity role) {
        try {
            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            role.setDeletedFlag(0);
            role.setVersion(1);

            rbacRoleDao.insert(role);
            return role.getRoleId();
        } catch (Exception e) {
            log.error("创建角色异常: {}", role.getRoleName(), e);
            throw new RuntimeException("创建角色失败", e);
        }
    }

    @Override
    public boolean updateRole(Long roleId, RbacRoleEntity role) {
        try {
            UpdateWrapper<RbacRoleEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("role_id", roleId);
            updateWrapper.eq("deleted_flag", 0);

            role.setUpdateTime(LocalDateTime.now());
            // 版本号递增用于乐观锁
            updateWrapper.setSql("version = version + 1");

            int result = rbacRoleDao.update(role, updateWrapper);
            return result > 0;
        } catch (Exception e) {
            log.error("更新角色异常: roleId={}", roleId, e);
            throw new RuntimeException("更新角色失败", e);
        }
    }

    @Override
    public boolean deleteRole(Long roleId) {
        try {
            UpdateWrapper<RbacRoleEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("role_id", roleId);

            RbacRoleEntity updateEntity = new RbacRoleEntity();
            updateEntity.setDeletedFlag(1);
            updateEntity.setUpdateTime(LocalDateTime.now());

            int result = rbacRoleDao.update(updateEntity, updateWrapper);

            // 同时删除相关的角色资源关联
            if (result > 0) {
                QueryWrapper<RbacRoleResourceEntity> roleResourceQuery = new QueryWrapper<>();
                roleResourceQuery.eq("role_id", roleId);
                rbacRoleResourceDao.delete(roleResourceQuery);

                // 同时删除相关的用户角色关联
                QueryWrapper<RbacUserRoleEntity> userRoleQuery = new QueryWrapper<>();
                userRoleQuery.eq("role_id", roleId);
                rbacUserRoleDao.delete(userRoleQuery);
            }

            return result > 0;
        } catch (Exception e) {
            log.error("删除角色异常: roleId={}", roleId, e);
            throw new RuntimeException("删除角色失败", e);
        }
    }

    @Override
    public boolean assignRolePermissions(Long roleId, List<Long> resourceIds) {
        try {
            // 先删除原有的权限关联
            QueryWrapper<RbacRoleResourceEntity> deleteQuery = new QueryWrapper<>();
            deleteQuery.eq("role_id", roleId);
            rbacRoleResourceDao.delete(deleteQuery);

            // 批量插入新的权限关联
            if (resourceIds != null && !resourceIds.isEmpty()) {
                for (Long resourceId : resourceIds) {
                    RbacRoleResourceEntity roleResource = new RbacRoleResourceEntity();
                    roleResource.setRoleId(roleId);
                    roleResource.setResourceId(resourceId);
                    roleResource.setAction("READ,WRITE"); // 默认权限
                    roleResource.setStatus(1);
                    roleResource.setPriority(1);
                    roleResource.setCreateTime(LocalDateTime.now());
                    roleResource.setUpdateTime(LocalDateTime.now());
                    roleResource.setDeletedFlag(0);
                    roleResource.setVersion(1);

                    rbacRoleResourceDao.insert(roleResource);
                }
            }

            return true;
        } catch (Exception e) {
            log.error("分配角色权限异常: roleId={}, resourceIds={}", roleId, resourceIds, e);
            throw new RuntimeException("分配角色权限失败", e);
        }
    }

    @Override
    public List<RbacRoleEntity> getRolePermissions(Long roleId) {
        try {
            // TODO: 实现获取角色权限的资源列表
            // 这里需要关联查询 t_rbac_role_resource 和 t_rbac_resource 表
            return null;
        } catch (Exception e) {
            log.error("获取角色权限异常: roleId={}", roleId, e);
            throw new RuntimeException("获取角色权限失败", e);
        }
    }

    @Override
    public boolean assignUserRoles(Long userId, List<Long> roleIds) {
        try {
            // 先删除原有的用户角色关联
            QueryWrapper<RbacUserRoleEntity> deleteQuery = new QueryWrapper<>();
            deleteQuery.eq("user_id", userId);
            rbacUserRoleDao.delete(deleteQuery);

            // 批量插入新的用户角色关联
            if (roleIds != null && !roleIds.isEmpty()) {
                for (Long roleId : roleIds) {
                    RbacUserRoleEntity userRole = new RbacUserRoleEntity();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRole.setStatus(1);
                    userRole.setGrantType("MANUAL");
                    userRole.setGrantSource("SYSTEM");
                    userRole.setEffectiveTime(LocalDateTime.now());
                    userRole.setExpireTime(LocalDateTime.now().plusYears(10)); // 10年后过期
                    userRole.setCreateTime(LocalDateTime.now());
                    userRole.setUpdateTime(LocalDateTime.now());
                    userRole.setDeletedFlag(0);
                    userRole.setVersion(1);

                    rbacUserRoleDao.insert(userRole);
                }
            }

            return true;
        } catch (Exception e) {
            log.error("用户分配角色异常: userId={}, roleIds={}", userId, roleIds, e);
            throw new RuntimeException("用户分配角色失败", e);
        }
    }
}