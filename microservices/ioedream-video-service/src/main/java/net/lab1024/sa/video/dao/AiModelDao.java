package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.video.AiModelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI模型DAO
 *
 * @author IOE-DREAM AI Team
 * @since 2025-01-30
 */
@Mapper
public interface AiModelDao extends BaseMapper<AiModelEntity> {
}
