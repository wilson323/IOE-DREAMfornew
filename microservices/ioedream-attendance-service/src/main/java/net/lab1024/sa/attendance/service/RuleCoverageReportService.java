package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.form.RuleCoverageReportForm;
import net.lab1024.sa.attendance.domain.vo.RuleCoverageReportVO;
import net.lab1024.sa.attendance.domain.vo.RuleCoverageTrendVO;
import net.lab1024.sa.attendance.domain.vo.RuleCoverageDetailVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 规则覆盖率报告服务接口
 * <p>
 * 提供规则覆盖率统计和分析功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface RuleCoverageReportService {

    /**
     * 生成覆盖率报告
     *
     * @param form 报告参数
     * @return 报告结果
     */
    RuleCoverageReportVO generateCoverageReport(RuleCoverageReportForm form);

    /**
     * 查询报告结果
     *
     * @param reportId 报告ID
     * @return 报告结果
     */
    RuleCoverageReportVO getReport(Long reportId);

    /**
     * 查询指定日期的报告
     *
     * @param reportDate 报告日期
     * @return 报告结果
     */
    RuleCoverageReportVO getReportByDate(LocalDate reportDate);

    /**
     * 查询最近的报告列表
     *
     * @param limit 限制数量
     * @return 报告列表
     */
    List<RuleCoverageReportVO> getRecentReports(Integer limit);

    /**
     * 查询覆盖率趋势数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据列表
     */
    List<RuleCoverageTrendVO> getCoverageTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 查询规则覆盖详情
     *
     * @param reportId 报告ID
     * @return 规则覆盖详情列表
     */
    List<RuleCoverageDetailVO> getRuleCoverageDetails(Long reportId);

    /**
     * 删除报告
     *
     * @param reportId 报告ID
     */
    void deleteReport(Long reportId);

    /**
     * 批量删除报告
     *
     * @param reportIds 报告ID列表
     */
    void batchDeleteReports(List<Long> reportIds);
}
