package net.lab1024.sa.report.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.report.dao.ReportTemplateDao;
import net.lab1024.sa.report.domain.entity.ReportTemplateEntity;
import net.lab1024.sa.report.domain.vo.ReportTemplateRequestVO;
import net.lab1024.sa.report.domain.vo.ReportTemplateVO;

/**
 * 报表模板服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportTemplateService {

    private final ReportTemplateDao reportTemplateDao;

    /**
     * 创建报表模板
     */
    @Transactional
    public Long createTemplate(ReportTemplateRequestVO request) {
        ReportTemplateEntity entity = new ReportTemplateEntity();
        entity.setTemplateName(request.getTemplateName());
        entity.setTemplateCode(request.getTemplateCode());
        entity.setDescription(request.getDescription());
        entity.setTemplateType(request.getTemplateType());
        entity.setDataSourceConfig(request.getDataSourceConfig());
        entity.setSqlQuery(request.getSqlQuery());
        entity.setParameterConfig(request.getParameterConfig());
        entity.setChartConfig(request.getChartConfig());
        entity.setStyleConfig(request.getStyleConfig());
        entity.setCacheTtl(request.getCacheTtl());
        entity.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        reportTemplateDao.insert(entity);
        log.info("创建报表模板成功，模板ID: {}, 模板名称: {}", entity.getTemplateId(), entity.getTemplateName());

        return entity.getTemplateId();
    }

    /**
     * 更新报表模板
     */
    @Transactional
    public boolean updateTemplate(Long templateId, ReportTemplateRequestVO request) {
        ReportTemplateEntity entity = reportTemplateDao.selectById(templateId);
        if (entity == null) {
            throw new RuntimeException("报表模板不存在");
        }

        entity.setTemplateName(request.getTemplateName());
        entity.setTemplateCode(request.getTemplateCode());
        entity.setDescription(request.getDescription());
        entity.setTemplateType(request.getTemplateType());
        entity.setDataSourceConfig(request.getDataSourceConfig());
        entity.setSqlQuery(request.getSqlQuery());
        entity.setParameterConfig(request.getParameterConfig());
        entity.setChartConfig(request.getChartConfig());
        entity.setStyleConfig(request.getStyleConfig());
        entity.setCacheTtl(request.getCacheTtl());
        entity.setEnabled(request.getEnabled());
        entity.setSortOrder(request.getSortOrder());

        int result = reportTemplateDao.updateById(entity);
        log.info("更新报表模板完成，模板ID: {}, 结果: {}", templateId, result > 0);

        return result > 0;
    }

    /**
     * 删除报表模板
     */
    @Transactional
    public boolean deleteTemplate(Long templateId) {
        ReportTemplateEntity entity = reportTemplateDao.selectById(templateId);
        if (entity == null) {
            throw new RuntimeException("报表模板不存在");
        }

        int result = reportTemplateDao.deleteById(templateId);
        log.info("删除报表模板完成，模板ID: {}, 结果: {}", templateId, result > 0);

        return result > 0;
    }

    /**
     * 获取报表模板详情
     */
    public ReportTemplateVO getTemplateDetail(Long templateId) {
        ReportTemplateEntity entity = reportTemplateDao.selectById(templateId);
        if (entity == null) {
            throw new RuntimeException("报表模板不存在");
        }

        ReportTemplateVO vo = new ReportTemplateVO();
        vo.setTemplateId(entity.getTemplateId());
        vo.setTemplateName(entity.getTemplateName());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setDescription(entity.getDescription());
        vo.setTemplateType(entity.getTemplateType());
        vo.setDataSourceConfig(entity.getDataSourceConfig());
        vo.setSqlQuery(entity.getSqlQuery());
        vo.setParameterConfig(entity.getParameterConfig());
        vo.setChartConfig(entity.getChartConfig());
        vo.setStyleConfig(entity.getStyleConfig());
        vo.setCacheTtl(entity.getCacheTtl());
        vo.setEnabled(entity.getEnabled());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        return vo;
    }

    /**
     * 分页查询报表模板
     */
    public PageResult<ReportTemplateVO> getTemplateList(Integer pageNum, Integer pageSize, String templateName,
            String templateType) {
        Page<ReportTemplateEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReportTemplateEntity> wrapper = new LambdaQueryWrapper<>();

        if (templateName != null && !templateName.trim().isEmpty()) {
            wrapper.like(ReportTemplateEntity::getTemplateName, templateName);
        }

        if (templateType != null && !templateType.trim().isEmpty()) {
            wrapper.eq(ReportTemplateEntity::getTemplateType, templateType);
        }

        wrapper.orderByAsc(ReportTemplateEntity::getSortOrder)
                .orderByDesc(ReportTemplateEntity::getUpdateTime);

        IPage<ReportTemplateEntity> result = reportTemplateDao.selectPage(page, wrapper);

        List<ReportTemplateVO> voList = new ArrayList<>();
        for (ReportTemplateEntity entity : result.getRecords()) {
            ReportTemplateVO vo = new ReportTemplateVO();
            vo.setTemplateId(entity.getTemplateId());
            vo.setTemplateName(entity.getTemplateName());
            vo.setTemplateCode(entity.getTemplateCode());
            vo.setDescription(entity.getDescription());
            vo.setTemplateType(entity.getTemplateType());
            vo.setEnabled(entity.getEnabled());
            vo.setSortOrder(entity.getSortOrder());
            vo.setCreateTime(entity.getCreateTime());
            vo.setUpdateTime(entity.getUpdateTime());
            voList.add(vo);
        }

        return PageResult.of(voList, result.getTotal(), pageNum, pageSize);
    }

    /**
     * 复制报表模板
     */
    @Transactional
    public Long copyTemplate(Long sourceTemplateId, String newTemplateName) {
        ReportTemplateEntity sourceEntity = reportTemplateDao.selectById(sourceTemplateId);
        if (sourceEntity == null) {
            throw new RuntimeException("源报表模板不存在");
        }

        ReportTemplateEntity newEntity = new ReportTemplateEntity();
        newEntity.setTemplateName(newTemplateName);
        newEntity.setTemplateCode(sourceEntity.getTemplateCode() + "_copy");
        newEntity.setDescription(sourceEntity.getDescription() + " (副本)");
        newEntity.setTemplateType(sourceEntity.getTemplateType());
        newEntity.setDataSourceConfig(sourceEntity.getDataSourceConfig());
        newEntity.setSqlQuery(sourceEntity.getSqlQuery());
        newEntity.setParameterConfig(sourceEntity.getParameterConfig());
        newEntity.setChartConfig(sourceEntity.getChartConfig());
        newEntity.setStyleConfig(sourceEntity.getStyleConfig());
        newEntity.setCacheTtl(sourceEntity.getCacheTtl());
        newEntity.setEnabled(1);
        newEntity.setSortOrder(sourceEntity.getSortOrder());

        reportTemplateDao.insert(newEntity);
        log.info("复制报表模板完成，源模板ID: {}, 新模板ID: {}", sourceTemplateId, newEntity.getTemplateId());

        return newEntity.getTemplateId();
    }

    /**
     * 获取模板类型列表
     */
    public List<Map<String, Object>> getTemplateTypes() {
        List<Map<String, Object>> types = new ArrayList<>();

        Map<String, Object> table = new HashMap<>();
        table.put("code", "TABLE");
        table.put("name", "表格报表");
        table.put("description", "以表格形式展示数据的报表");
        types.add(table);

        Map<String, Object> chart = new HashMap<>();
        chart.put("code", "CHART");
        chart.put("name", "图表报表");
        chart.put("description", "以图表形式展示数据的报表");
        types.add(chart);

        Map<String, Object> mix = new HashMap<>();
        mix.put("code", "MIX");
        mix.put("name", "混合报表");
        mix.put("description", "同时包含表格和图表的复合报表");
        types.add(mix);

        return types;
    }

    /**
     * 测试SQL
     */
    public Map<String, Object> testSql(String sql, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 这里应该执行SQL查询并返回测试结果
            // 简化实现，只做基本的SQL语法检查
            if (sql == null || sql.trim().isEmpty()) {
                result.put("valid", false);
                result.put("error", "SQL不能为空");
                return result;
            }

            String upperSql = sql.toUpperCase();
            if (!upperSql.contains("SELECT")) {
                result.put("valid", false);
                result.put("error", "必须是SELECT语句");
                return result;
            }

            // 检查危险操作
            if (upperSql.contains("DROP ") || upperSql.contains("DELETE ") ||
                    upperSql.contains("UPDATE ") || upperSql.contains("INSERT ")) {
                result.put("valid", false);
                result.put("error", "不允许包含数据修改操作");
                return result;
            }

            result.put("valid", true);
            result.put("message", "SQL语法检查通过");
            result.put("sql", sql);

            return result;

        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 获取模板总数
     */
    public long count() {
        return reportTemplateDao.selectCount(null);
    }

    /**
     * 根据ID获取模板
     */
    public ReportTemplateEntity getById(Long templateId) {
        return reportTemplateDao.selectById(templateId);
    }
}
