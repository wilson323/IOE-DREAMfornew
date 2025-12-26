package net.lab1024.sa.consume.service.report;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.report.ReportGenerationEntity;
import net.lab1024.sa.consume.dao.report.ReportGenerationDao;
import net.lab1024.sa.consume.manager.report.ReportGenerateManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 报表生成服务 - 报表生成记录管理
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Service
public class ReportGenerationService extends ServiceImpl<ReportGenerationDao, ReportGenerationEntity> {

    @Resource
    private ReportGenerationDao reportGenerationDao;

    @Resource
    private ReportGenerateManager reportGenerateManager;

    /**
     * 查询报表生成记录（分页）
     */
    public List<ReportGenerationEntity> queryGenerationRecords(Long reportId, Integer pageNum, Integer pageSize) {
        log.info("[报表中心] 查询生成记录: reportId={}", reportId);
        return reportGenerateManager.queryGenerationRecords(reportId, pageNum, pageSize);
    }

    /**
     * 获取生成记录详情
     */
    public ReportGenerationEntity getGenerationDetail(Long generationId) {
        log.info("[报表中心] 查询生成记录详情: generationId={}", generationId);
        return reportGenerateManager.getGenerationDetail(generationId);
    }

    /**
     * 删除生成记录
     */
    public void deleteGeneration(Long generationId) {
        log.info("[报表中心] 删除生成记录: generationId={}", generationId);
        reportGenerationDao.deleteById(generationId);
    }

    /**
     * 查询用户生成记录
     */
    public List<ReportGenerationEntity> queryUserGenerations(Long userId, Integer pageNum, Integer pageSize) {
        log.info("[报表中心] 查询用户生成记录: userId={}", userId);
        // TODO: 根据用户ID查询生成记录
        return reportGenerateManager.queryGenerationRecords(null, pageNum, pageSize);
    }
}
