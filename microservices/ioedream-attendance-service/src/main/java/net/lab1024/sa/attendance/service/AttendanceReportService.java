package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.form.AttendanceReportQueryForm;
import net.lab1024.sa.attendance.domain.form.AttendanceStatisticsQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceReportVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceStatisticsVO;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 考勤报表统计服务接口
 * <p>
 * 提供考勤报表和统计相关业务功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
public interface AttendanceReportService {

    /**
     * 查询日报表
     *
     * @param queryForm 查询表单
     * @return 日报表数据
     */
    List<AttendanceReportVO> getDailyReport(AttendanceReportQueryForm queryForm);

    /**
     * 查询月报表
     *
     * @param queryForm 查询表单
     * @return 月报表数据
     */
    List<AttendanceReportVO> getMonthlyReport(AttendanceReportQueryForm queryForm);

    /**
     * 导出报表
     *
     * @param queryForm 查询表单
     * @param response HTTP响应
     */
    void exportReport(AttendanceReportQueryForm queryForm, HttpServletResponse response);

    /**
     * 查询个人统计
     *
     * @param queryForm 查询表单
     * @return 个人统计数据
     */
    AttendanceStatisticsVO getPersonalStatistics(AttendanceStatisticsQueryForm queryForm);

    /**
     * 查询部门统计
     *
     * @param queryForm 查询表单
     * @return 部门统计数据
     */
    AttendanceStatisticsVO getDepartmentStatistics(AttendanceStatisticsQueryForm queryForm);

    /**
     * 查询公司统计
     *
     * @param queryForm 查询表单
     * @return 公司统计数据
     */
    AttendanceStatisticsVO getCompanyStatistics(AttendanceStatisticsQueryForm queryForm);

    /**
     * 导出统计数据
     *
     * @param queryForm 查询表单
     * @param response HTTP响应
     */
    void exportStatistics(AttendanceStatisticsQueryForm queryForm, HttpServletResponse response);

    /**
     * 查询图表数据
     *
     * @param queryForm 查询表单
     * @return 图表数据
     */
    Map<String, Object> getChartData(AttendanceStatisticsQueryForm queryForm);
}
