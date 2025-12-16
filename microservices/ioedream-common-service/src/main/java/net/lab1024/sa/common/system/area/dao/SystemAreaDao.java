package net.lab1024.sa.common.system.area.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.system.area.domain.entity.SystemAreaEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统区域DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Mapper
public interface SystemAreaDao extends BaseMapper<SystemAreaEntity> {
}

