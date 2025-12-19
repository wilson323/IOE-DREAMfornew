package net.lab1024.sa.common.permission.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.permission.domain.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限DAO接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Mapper
public interface PermissionDao extends BaseMapper<PermissionEntity> {
}
