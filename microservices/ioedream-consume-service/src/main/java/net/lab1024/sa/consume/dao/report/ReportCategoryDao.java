package net.lab1024.sa.consume.dao.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.report.ReportCategoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表分类DAO接口
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Mapper
public interface ReportCategoryDao extends BaseMapper<ReportCategoryEntity> {
}
