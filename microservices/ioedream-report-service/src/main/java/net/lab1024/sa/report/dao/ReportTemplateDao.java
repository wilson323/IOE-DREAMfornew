package net.lab1024.sa.report.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.report.domain.entity.ReportTemplateEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表模板DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Mapper
public interface ReportTemplateDao extends BaseMapper<ReportTemplateEntity> {
}