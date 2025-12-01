package net.lab1024.sa.identity.module.rbac.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.identity.module.rbac.domain.entity.RbacUserRoleEntity;

/**
 * RBAC用户角色关联DAO
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
@Mapper
public interface RbacUserRoleDao extends BaseMapper<RbacUserRoleEntity> {

    /**
     * 根据用户ID查询角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(@Param("userId") Long userId);
}