package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.attendance.domain.form.AttendanceReportQueryForm;
import net.lab1024.sa.attendance.domain.form.AttendanceStatisticsQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceReportVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceStatisticsVO;
import net.lab1024.sa.attendance.service.AttendanceReportService;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 考勤报表统计控制器
 * <p>
 * 提供考勤报表和统计相关API接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance")
@Tag(name = "考勤报表统计")
public class AttendanceReportController {

    @Resource
    private AttendanceReportService attendanceReportService;

    /**
     * 查询日报表
     *
     * @param queryForm 查询表单
     * @return 日报表数据
     */
    @Observed(name = "attendanceReport.getDailyReport", contextualName = "get-daily-report")
    @GetMapping("/report/daily")
    @Operation(summary = "查询日报表", description = "查询指定日期范围的考勤日报表数据")
    public ResponseDTO<List<AttendanceReportVO>> getDailyReport(@Valid AttendanceReportQueryForm queryForm) {
        log.info("[考勤报表] 查询日报表: queryForm={}", queryForm);
        List<AttendanceReportVO> report = attendanceReportService.getDailyReport(queryForm);
        log.info("[考勤报表] 查询日报表成功: count={}", report.size());
        return ResponseDTO.ok(report);
    }

    /**
     * 查询月报表
     *
     * @param queryForm 查询表单
     * @return 月报表数据
     */
    @Observed(name = "attendanceReport.getMonthlyReport", contextualName = "get-monthly-report")
    @GetMapping("/report/monthly")
    @Operation(summary = "查询月报表", description = "查询指定月份范围的考勤月报表数据")
    public ResponseDTO<List<AttendanceReportVO>> getMonthlyReport(@Valid AttendanceReportQueryForm queryForm) {
        log.info("[考勤报表] 查询月报表: queryForm={}", queryForm);
        List<AttendanceReportVO> report = attendanceReportService.getMonthlyReport(queryForm);
        log.info("[考勤报表] 查询月报表成功: count={}", report.size());
        return ResponseDTO.ok(report);
    }

    /**
     * 导出报表
     *
     * @param queryForm 查询表单
     * @param response HTTP响应
     */
    @Observed(name = "attendanceReport.exportReport", contextualName = "export-report")
    @PostMapping("/report/export")
    @Operation(summary = "导出报表", description = "导出考勤报表为Excel文件")
    public void exportReport(@Valid @RequestBody AttendanceReportQueryForm queryForm,
                            HttpServletResponse response) {
        log.info("[考勤报表] 导出报表: queryForm={}", queryForm);
        attendanceReportService.exportReport(queryForm, response);
        log.info("[考勤报表] 导出报表成功");
    }

    /**
     * 查询个人统计
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 个人统计数据
     */
    @Observed(name = "attendanceReport.getPersonalStatistics", contextualName = "get-personal-statistics")
    @GetMapping("/statistics/personal")
    @Operation(summary = "查询个人统计", description = "查询指定员工的考勤统计数据")
    public ResponseDTO<AttendanceStatisticsVO> getPersonalStatistics(
            @RequestParam @Parameter(description = "员工ID", required = true) Long employeeId,
            @RequestParam(required = false) @Parameter(description = "开始日期") String startDate,
            @RequestParam(required = false) @Parameter(description = "结束日期") String endDate) {
        log.info("[考勤统计] 查询个人统计: employeeId={}, startDate={}, endDate={}", employeeId, startDate, endDate);

        AttendanceStatisticsQueryForm queryForm = AttendanceStatisticsQueryForm.builder()
                .employeeId(employeeId)
                .startDate(startDate != null ? java.time.LocalDate.parse(startDate) : null)
                .endDate(endDate != null ? java.time.LocalDate.parse(endDate) : null)
                .statisticsType("PERSONAL")
                .build();

        AttendanceStatisticsVO statistics = attendanceReportService.getPersonalStatistics(queryForm);
        log.info("[考勤统计] 查询个人统计成功: employeeId={}", employeeId);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 查询部门统计
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 部门统计数据
     */
    @Observed(name = "attendanceReport.getDepartmentStatistics", contextualName = "get-department-statistics")
    @GetMapping("/statistics/department")
    @Operation(summary = "查询部门统计", description = "查询指定部门的考勤统计数据")
    public ResponseDTO<AttendanceStatisticsVO> getDepartmentStatistics(
            @RequestParam @Parameter(description = "部门ID", required = true) Long departmentId,
            @RequestParam(required = false) @Parameter(description = "开始日期") String startDate,
            @RequestParam(required = false) @Parameter(description = "结束日期") String endDate) {
        log.info("[考勤统计] 查询部门统计: departmentId={}, startDate={}, endDate={}", departmentId, startDate, endDate);

        AttendanceStatisticsQueryForm queryForm = AttendanceStatisticsQueryForm.builder()
                .departmentId(departmentId)
                .startDate(startDate != null ? java.time.LocalDate.parse(startDate) : null)
                .endDate(endDate != null ? java.time.LocalDate.parse(endDate) : null)
                .statisticsType("DEPARTMENT")
                .build();

        AttendanceStatisticsVO statistics = attendanceReportService.getDepartmentStatistics(queryForm);
        log.info("[考勤统计] 查询部门统计成功: departmentId={}", departmentId);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 查询公司统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 公司统计数据
     */
    @Observed(name = "attendanceReport.getCompanyStatistics", contextualName = "get-company-statistics")
    @GetMapping("/statistics/company")
    @Operation(summary = "查询公司统计", description = "查询全公司的考勤统计数据")
    public ResponseDTO<AttendanceStatisticsVO> getCompanyStatistics(
            @RequestParam(required = false) @Parameter(description = "开始日期") String startDate,
            @RequestParam(required = false) @Parameter(description = "结束日期") String endDate) {
        log.info("[考勤统计] 查询公司统计: startDate={}, endDate={}", startDate, endDate);

        AttendanceStatisticsQueryForm queryForm = AttendanceStatisticsQueryForm.builder()
                .startDate(startDate != null ? java.time.LocalDate.parse(startDate) : null)
                .endDate(endDate != null ? java.time.LocalDate.parse(endDate) : null)
                .statisticsType("COMPANY")
                .build();

        AttendanceStatisticsVO statistics = attendanceReportService.getCompanyStatistics(queryForm);
        log.info("[考勤统计] 查询公司统计成功");
        return ResponseDTO.ok(statistics);
    }

    /**
     * 导出统计数据
     *
     * @param queryForm 查询表单
     * @param response HTTP响应
     */
    @Observed(name = "attendanceReport.exportStatistics", contextualName = "export-statistics")
    @PostMapping("/statistics/export")
    @Operation(summary = "导出统计数据", description = "导出考勤统计数据为Excel文件")
    public void exportStatistics(@Valid @RequestBody AttendanceStatisticsQueryForm queryForm,
                                 HttpServletResponse response) {
        log.info("[考勤统计] 导出统计数据: queryForm={}", queryForm);
        attendanceReportService.exportStatistics(queryForm, response);
        log.info("[考勤统计] 导出统计数据成功");
    }

    /**
     * 查询图表数据
     *
     * @param queryForm 查询表单
     * @return 图表数据
     */
    @Observed(name = "attendanceReport.getChartData", contextualName = "get-chart-data")
    @PostMapping("/statistics/chart")
    @Operation(summary = "查询图表数据", description = "查询考勤统计图表数据，用于前端可视化展示")
    public ResponseDTO<Map<String, Object>> getChartData(@Valid @RequestBody AttendanceStatisticsQueryForm queryForm) {
        log.info("[考勤统计] 查询图表数据: queryForm={}", queryForm);
        Map<String, Object> chartData = attendanceReportService.getChartData(queryForm);
        log.info("[考勤统计] 查询图表数据成功");
        return ResponseDTO.ok(chartData);
    }
}
