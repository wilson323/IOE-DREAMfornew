package net.lab1024.sa.audit.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;

import net.lab1024.sa.audit.domain.entity.AuditLogEntity;
import net.lab1024.sa.audit.domain.form.AuditLogExportForm;
import net.lab1024.sa.audit.domain.form.AuditLogQueryForm;
import net.lab1024.sa.audit.domain.form.AuditStatisticsQueryForm;
import net.lab1024.sa.audit.domain.form.ComplianceReportQueryForm;
import net.lab1024.sa.audit.domain.vo.AuditLogVO;
import net.lab1024.sa.audit.domain.vo.AuditStatisticsVO;
import net.lab1024.sa.audit.domain.vo.ComplianceReportVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 审计服务接口
 * 提供审计日志管理、统计分析、合规报告等功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
public interface AuditService {

    /**
     * 记录审计日志
     *
     * @param auditLog 审计日志实体
     */
    @Async
    void recordAuditLog(AuditLogEntity auditLog);

    /**
     * 批量记录审计日志
     *
     * @param auditLogs 审计日志实体列表
     */
    @Async
    void recordAuditLogBatch(List<AuditLogEntity> auditLogs);

    /**
     * 分页查询审计日志
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ResponseDTO<PageResult<AuditLogVO>> queryAuditLogPage(AuditLogQueryForm queryForm);

    /**
     * 获取审计日志详情
     *
     * @param auditId 审计日志ID
     * @return 审计日志详情
     */
    ResponseDTO<AuditLogVO> getAuditLogDetail(Long auditId);

    /**
     * 获取审计统计信息
     *
     * @param queryForm 统计查询表单
     * @return 统计信息
     */
    ResponseDTO<AuditStatisticsVO> getAuditStatistics(AuditStatisticsQueryForm queryForm);

    /**
     * 生成合规报告
     *
     * @param queryForm 合规报告查询表单
     * @return 合规报告
     */
    ResponseDTO<ComplianceReportVO> generateComplianceReport(ComplianceReportQueryForm queryForm);

    /**
     * 导出审计日志
     *
     * @param exportForm 导出表单
     * @return 导出任务ID
     */
    ResponseDTO<String> exportAuditLogs(AuditLogExportForm exportForm);

    /**
     * 清理过期审计日志
     *
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    ResponseDTO<String> cleanExpiredAuditLogs(int retentionDays);
}
