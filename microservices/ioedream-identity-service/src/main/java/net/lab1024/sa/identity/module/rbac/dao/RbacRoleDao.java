package net.lab1024.sa.identity.module.rbac.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.identity.module.rbac.domain.entity.RbacRoleEntity;

/**
 * RBAC角色DAO
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
@Mapper
public interface RbacRoleDao extends BaseMapper<RbacRoleEntity> {
}