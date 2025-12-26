package net.lab1024.sa.consume.service.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.report.*;
import net.lab1024.sa.consume.dao.report.*;
import net.lab1024.sa.consume.manager.report.ReportGenerateManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 报表定义服务 - 报表CRUD管理完整实现
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Service
public class ReportDefinitionService extends ServiceImpl<ReportDefinitionDao, ReportDefinitionEntity> {

    @Resource
    private ReportDefinitionDao reportDefinitionDao;

    @Resource
    private ReportCategoryDao reportCategoryDao;

    @Resource
    private ReportParameterDao reportParameterDao;

    @Resource
    private ReportGenerateManager reportGenerateManager;

    /**
     * 新增报表定义
     */
    public Long addReport(ReportDefinitionEntity report) {
        log.info("[报表中心] 新增报表定义: reportName={}", report.getReportName());
        reportDefinitionDao.insert(report);
        return report.getReportId();
    }

    /**
     * 更新报表定义
     */
    public void updateReport(ReportDefinitionEntity report) {
        log.info("[报表中心] 更新报表定义: reportId={}", report.getReportId());
        reportDefinitionDao.updateById(report);
    }

    /**
     * 删除报表定义
     */
    public void deleteReport(Long reportId) {
        log.info("[报表中心] 删除报表定义: reportId={}", reportId);
        reportDefinitionDao.deleteById(reportId);
    }

    /**
     * 查询报表定义详情
     */
    public ReportDefinitionEntity getReportDetail(Long reportId) {
        log.info("[报表中心] 查询报表详情: reportId={}", reportId);
        return reportDefinitionDao.selectById(reportId);
    }

    /**
     * 分页查询报表列表
     */
    public Page<ReportDefinitionEntity> queryReports(Long categoryId, String businessModule,
                                                     Integer pageNum, Integer pageSize) {
        log.info("[报表中心] 查询报表列表: categoryId={}, businessModule={}", categoryId, businessModule);

        LambdaQueryWrapper<ReportDefinitionEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            queryWrapper.eq(ReportDefinitionEntity::getCategoryId, categoryId);
        }
        if (businessModule != null && !businessModule.isEmpty()) {
            queryWrapper.eq(ReportDefinitionEntity::getBusinessModule, businessModule);
        }
        queryWrapper.orderByAsc(ReportDefinitionEntity::getSortOrder);

        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    /**
     * 查询报表分类列表
     */
    public List<ReportCategoryEntity> getReportCategories() {
        log.info("[报表中心] 查询报表分类列表");
        LambdaQueryWrapper<ReportCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportCategoryEntity::getStatus, 1)
                .orderByAsc(ReportCategoryEntity::getSortOrder);
        return reportCategoryDao.selectList(queryWrapper);
    }

    /**
     * 查询报表参数列表
     */
    public List<ReportParameterEntity> getReportParameters(Long reportId) {
        log.info("[报表中心] 查询报表参数: reportId={}", reportId);
        LambdaQueryWrapper<ReportParameterEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportParameterEntity::getReportId, reportId)
                .orderByAsc(ReportParameterEntity::getSortOrder);
        return reportParameterDao.selectList(queryWrapper);
    }

    /**
     * 生成报表
     */
    public Long generateReport(Long reportId, String parameters, Integer generateType, String fileType) {
        log.info("[报表中心] 生成报表: reportId={}, fileType={}", reportId, fileType);
        return reportGenerateManager.generateReport(reportId, parameters, generateType, fileType);
    }

    /**
     * 启用报表
     */
    public void enableReport(Long reportId) {
        log.info("[报表中心] 启用报表: reportId={}", reportId);
        ReportDefinitionEntity report = reportDefinitionDao.selectById(reportId);
        if (report != null) {
            report.setStatus(1);
            reportDefinitionDao.updateById(report);
        }
    }

    /**
     * 禁用报表
     */
    public void disableReport(Long reportId) {
        log.info("[报表中心] 禁用报表: reportId={}", reportId);
        ReportDefinitionEntity report = reportDefinitionDao.selectById(reportId);
        if (report != null) {
            report.setStatus(0);
            reportDefinitionDao.updateById(report);
        }
    }
}
