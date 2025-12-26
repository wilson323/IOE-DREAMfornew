package net.lab1024.sa.consume.dao.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.report.ReportGenerationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表生成记录DAO接口
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Mapper
public interface ReportGenerationDao extends BaseMapper<ReportGenerationEntity> {
}
