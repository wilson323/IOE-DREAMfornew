package net.lab1024.sa.base.module.support.rbac.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.base.module.support.rbac.domain.entity.RbacResourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RBAC资源DAO
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Mapper
public interface RbacResourceDao extends BaseMapper<RbacResourceEntity> {

    /**
     * 根据资源编码查询资源是否存在
     *
     * @param resourceCode 资源编码
     * @return 资源是否存在
     */
    boolean existsByResourceCode(@Param("resourceCode") String resourceCode);

    /**
     * 根据角色ID列表查询权限编码列表
     *
     * @param roleIds 角色ID列表
     * @return 权限编码列表
     */
    List<String> getPermissionCodesByRoleIds(@Param("roleIds") List<Long> roleIds);
}

