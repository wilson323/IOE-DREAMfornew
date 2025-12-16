package net.lab1024.sa.consume.report.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.report.domain.entity.ConsumeReportTemplateEntity;

/**
 * 消费报表模板DAO接口
 * <p>
 * 用于报表模板的数据访问操作
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@org.apache.ibatis.annotations.Mapper
public interface ConsumeReportTemplateDao extends BaseMapper<ConsumeReportTemplateEntity> {

    /**
     * 根据模板类型查询模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    List<ConsumeReportTemplateEntity> selectByTemplateType(@Param("templateType") String templateType);

    /**
     * 查询启用的模板列表
     *
     * @return 启用的模板列表
     */
    List<ConsumeReportTemplateEntity> selectEnabledTemplates();
}



