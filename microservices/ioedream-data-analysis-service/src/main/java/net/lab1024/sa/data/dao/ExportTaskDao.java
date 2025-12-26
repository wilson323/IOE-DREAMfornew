package net.lab1024.sa.data.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.data.domain.entity.ExportTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据导出任务DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface ExportTaskDao extends BaseMapper<ExportTaskEntity> {
    // 遵循MyBatis-Plus规范，使用@Mapper注解而非@Repository
    // 基础CRUD由BaseMapper提供
}
