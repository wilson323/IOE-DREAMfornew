package net.lab1024.sa.consume.dao.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.report.ReportParameterEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表参数DAO接口
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Mapper
public interface ReportParameterDao extends BaseMapper<ReportParameterEntity> {
}
