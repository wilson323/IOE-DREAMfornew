package net.lab1024.sa.common.rbac.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.rbac.domain.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色DAO
 * <p>
 * 负责角色数据访问
 * 严格遵循CLAUDE.md规范:
 * - 使用@Mapper注解
 * - 使用Dao后缀
 * - 继承BaseMapper
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface RoleDao extends BaseMapper<RoleEntity> {
}
