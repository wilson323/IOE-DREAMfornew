package net.lab1024.sa.identity.module.rbac.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.identity.module.rbac.domain.entity.RbacRoleResourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RBAC角色资源关联DAO
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
@Mapper
public interface RbacRoleResourceDao extends BaseMapper<RbacRoleResourceEntity> {

    /**
     * 根据角色ID列表查询资源编码列表
     *
     * @param roleIds 角色ID列表
     * @return 资源编码列表
     */
    List<String> getResourceCodesByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 检查角色是否有指定资源的权限
     *
     * @param roleIds 角色ID列表
     * @param resourceCode 资源编码
     * @param action 操作动作
     * @return 是否有权限
     */
    boolean hasRoleResourcePermission(@Param("roleIds") List<Long> roleIds,
                                     @Param("resourceCode") String resourceCode,
                                     @Param("action") String action);
}