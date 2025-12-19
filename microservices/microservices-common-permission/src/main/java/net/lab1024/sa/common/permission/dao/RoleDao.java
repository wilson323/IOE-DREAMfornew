package net.lab1024.sa.common.permission.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.rbac.domain.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色DAO接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Mapper
public interface RoleDao extends BaseMapper<RoleEntity> {
}
