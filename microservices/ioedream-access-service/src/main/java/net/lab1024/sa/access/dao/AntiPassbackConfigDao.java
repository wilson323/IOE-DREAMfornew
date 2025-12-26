package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.AntiPassbackConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门禁反潜回配置DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AntiPassbackConfigDao extends BaseMapper<AntiPassbackConfigEntity> {
}
