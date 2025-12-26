package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.OfflineConsumeConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 离线消费配置DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Mapper
public interface OfflineConsumeConfigDao extends BaseMapper<OfflineConsumeConfigEntity> {
}
