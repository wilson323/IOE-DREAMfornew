package net.lab1024.sa.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.database.entity.DatabaseVersionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据库版本Mapper
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Mapper
public interface DatabaseVersionMapper extends BaseMapper<DatabaseVersionEntity> {
}
