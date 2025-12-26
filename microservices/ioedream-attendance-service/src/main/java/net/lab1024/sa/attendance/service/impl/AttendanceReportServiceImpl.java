package net.lab1024.sa.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.common.util.QueryBuilder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceSummaryDao;
import net.lab1024.sa.attendance.dao.DepartmentStatisticsDao;
import net.lab1024.sa.attendance.domain.form.AttendanceReportQueryForm;
import net.lab1024.sa.attendance.domain.form.AttendanceStatisticsQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceReportExcelVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceReportVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceStatisticsExcelVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceStatisticsVO;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;
import net.lab1024.sa.common.entity.attendance.DepartmentStatisticsEntity;
import net.lab1024.sa.attendance.service.AttendanceReportService;
import net.lab1024.sa.attendance.service.AttendanceSummaryService;
import net.lab1024.sa.attendance.util.AttendanceExcelUtil;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.system.employee.domain.vo.EmployeeVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤报表统计服务实现类
 * <p>
 * 提供考勤报表和统计相关业务功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class AttendanceReportServiceImpl implements AttendanceReportService {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private AttendanceSummaryDao attendanceSummaryDao;

    @Resource
    private DepartmentStatisticsDao departmentStatisticsDao;

    @Resource
    private AttendanceSummaryService attendanceSummaryService;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 查询日报表
     *
     * @param queryForm 查询表单
     * @return 日报表数据
     */
    @Override
    public List<AttendanceReportVO> getDailyReport(AttendanceReportQueryForm queryForm) {
        log.info("[考勤报表] 查询日报表: queryForm={}", queryForm);

        LocalDate startDate = queryForm.getStartDate() != null ? queryForm.getStartDate() : LocalDate.now();
        LocalDate endDate = queryForm.getEndDate() != null ? queryForm.getEndDate() : LocalDate.now();

        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                .le(AttendanceRecordEntity::getAttendanceDate, endDate);

        if (queryForm.getEmployeeId() != null) {
            queryWrapper.eq(AttendanceRecordEntity::getUserId, queryForm.getEmployeeId());
        }

        List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

        // 按日期分组统计
        Map<LocalDate, List<AttendanceRecordEntity>> groupedByDate = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecordEntity::getAttendanceDate));

        List<AttendanceReportVO> reportList = new ArrayList<>();
        for (Map.Entry<LocalDate, List<AttendanceRecordEntity>> entry : groupedByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<AttendanceRecordEntity> dateRecords = entry.getValue();

            // 统计当天数据
            int attendanceDays = (int) dateRecords.stream()
                    .filter(r -> "NORMAL".equals(r.getAttendanceStatus())).count();

            int lateCount = (int) dateRecords.stream()
                    .filter(r -> "LATE".equals(r.getAttendanceStatus())).count();

            int earlyLeaveCount = (int) dateRecords.stream()
                    .filter(r -> "EARLY".equals(r.getAttendanceStatus())).count();

            // 默认值
            BigDecimal totalOvertimeHours = BigDecimal.ZERO;
            BigDecimal totalWorkHours = BigDecimal.valueOf(8.0 * attendanceDays);

            AttendanceReportVO report = AttendanceReportVO.builder()
                    .reportDate(date)
                    .attendanceDays(attendanceDays)
                    .absenceDays(dateRecords.size() - attendanceDays)
                    .lateCount(lateCount)
                    .earlyLeaveCount(earlyLeaveCount)
                    .overtimeHours(totalOvertimeHours)
                    .totalWorkHours(totalWorkHours)
                    .build();

            reportList.add(report);
        }

        log.info("[考勤报表] 查询日报表成功: count={}", reportList.size());
        return reportList;
    }

    /**
     * 查询月报表
     *
     * @param queryForm 查询表单
     * @return 月报表数据
     */
    @Override
    public List<AttendanceReportVO> getMonthlyReport(AttendanceReportQueryForm queryForm) {
        log.info("[考勤报表] 查询月报表: queryForm={}", queryForm);

        LocalDate startDate = queryForm.getStartDate() != null ? queryForm.getStartDate() : LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = queryForm.getEndDate() != null ? queryForm.getEndDate() : LocalDate.now();

        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                .le(AttendanceRecordEntity::getAttendanceDate, endDate);

        if (queryForm.getEmployeeId() != null) {
            queryWrapper.eq(AttendanceRecordEntity::getUserId, queryForm.getEmployeeId());
        }

        List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

        // 按月份分组统计
        Map<String, List<AttendanceRecordEntity>> groupedByMonth = records.stream()
                .collect(Collectors.groupingBy(r -> r.getAttendanceDate().getYear() + "-" +
                        String.format("%02d", r.getAttendanceDate().getMonthValue())));

        List<AttendanceReportVO> reportList = new ArrayList<>();
        for (Map.Entry<String, List<AttendanceRecordEntity>> entry : groupedByMonth.entrySet()) {
            List<AttendanceRecordEntity> monthRecords = entry.getValue();

            // 统计月度数据
            int attendanceDays = (int) monthRecords.stream()
                    .filter(r -> "NORMAL".equals(r.getAttendanceStatus())).count();

            int lateCount = (int) monthRecords.stream()
                    .filter(r -> "LATE".equals(r.getAttendanceStatus())).count();

            int earlyLeaveCount = (int) monthRecords.stream()
                    .filter(r -> "EARLY".equals(r.getAttendanceStatus())).count();

            BigDecimal totalOvertimeHours = BigDecimal.ZERO;
            BigDecimal totalWorkHours = BigDecimal.valueOf(8.0 * attendanceDays);

            AttendanceReportVO report = AttendanceReportVO.builder()
                    .attendanceDays(attendanceDays)
                    .absenceDays(monthRecords.size() - attendanceDays)
                    .lateCount(lateCount)
                    .earlyLeaveCount(earlyLeaveCount)
                    .overtimeHours(totalOvertimeHours)
                    .totalWorkHours(totalWorkHours)
                    .build();

            reportList.add(report);
        }

        log.info("[考勤报表] 查询月报表成功: count={}", reportList.size());
        return reportList;
    }

    /**
     * 导出报表
     *
     * @param queryForm 查询表单
     * @param response HTTP响应
     */
    @Override
    public void exportReport(AttendanceReportQueryForm queryForm, HttpServletResponse response) {
        log.info("[考勤报表] 导出报表: queryForm={}", queryForm);

        try {
            // 1. 根据查询条件获取报表数据
            List<AttendanceReportVO> reportList = getDailyReport(queryForm);

            // 2. 转换为Excel数据模型
            List<AttendanceReportExcelVO> excelData = reportList.stream()
                    .map(vo -> AttendanceReportExcelVO.builder()
                            .employeeId(vo.getEmployeeId())
                            .employeeName(vo.getEmployeeName())
                            .departmentName(vo.getDepartmentName())
                            .reportDate(vo.getReportDate() != null ? vo.getReportDate().toString() : "")
                            .attendanceDays(vo.getAttendanceDays())
                            .absenceDays(vo.getAbsenceDays())
                            .lateCount(vo.getLateCount())
                            .earlyLeaveCount(vo.getEarlyLeaveCount())
                            .overtimeHours(vo.getOvertimeHours())
                            .totalWorkHours(vo.getTotalWorkHours())
                            .build())
                    .collect(Collectors.toList());

            // 3. 生成文件名
            String fileName = AttendanceExcelUtil.generateFileName("考勤报表");

            // 4. 导出Excel
            AttendanceExcelUtil.exportExcel(response, excelData,
                    AttendanceReportExcelVO.class, fileName);

            log.info("[考勤报表] 导出报表成功: fileName={}, rowCount={}", fileName, excelData.size());

        } catch (Exception e) {
            log.error("[考勤报表] 导出报表失败: queryForm={}, error={}", queryForm, e.getMessage(), e);
            throw new RuntimeException("导出报表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询个人统计
     *
     * @param queryForm 查询表单
     * @return 个人统计数据
     */
    @Override
    public AttendanceStatisticsVO getPersonalStatistics(AttendanceStatisticsQueryForm queryForm) {
        log.info("[考勤统计] 查询个人统计: queryForm={}", queryForm);

        if (queryForm.getEmployeeId() == null) {
            log.warn("[考勤统计] 员工ID不能为空");
            throw new IllegalArgumentException("员工ID不能为空");
        }

        LocalDate startDate = queryForm.getStartDate() != null ? queryForm.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = queryForm.getEndDate() != null ? queryForm.getEndDate() : LocalDate.now();

        // 优先从汇总表读取
        try {
            AttendanceStatisticsVO summary = attendanceSummaryService.getPersonalSummary(
                    queryForm.getEmployeeId(), startDate, endDate);

            if (summary != null) {
                log.info("[考勤统计] 从汇总表查询个人统计成功: employeeId={}", queryForm.getEmployeeId());
                return summary;
            }
        } catch (Exception e) {
            log.warn("[考勤统计] 从汇总表读取失败，使用实时计算: error={}", e.getMessage());
        }

        // 降级到实时计算
        log.info("[考勤统计] 使用实时计算个人统计: employeeId={}", queryForm.getEmployeeId());

        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecordEntity::getUserId, queryForm.getEmployeeId())
                .ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                .le(AttendanceRecordEntity::getAttendanceDate, endDate);

        List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

        // 统计数据
        int totalCount = records.size();
        int attendanceCount = (int) records.stream()
                .filter(r -> "NORMAL".equals(r.getAttendanceStatus())).count();
        int absenceCount = totalCount - attendanceCount;
        BigDecimal attendanceRate = totalCount > 0 ?
                BigDecimal.valueOf(attendanceCount * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal avgWorkHours = BigDecimal.valueOf(8.0); // 默认8小时工作制

        int lateCount = (int) records.stream()
                .filter(r -> "LATE".equals(r.getAttendanceStatus())).count();
        int earlyLeaveCount = (int) records.stream()
                .filter(r -> "EARLY".equals(r.getAttendanceStatus())).count();

        BigDecimal totalOvertimeHours = BigDecimal.ZERO;
        BigDecimal avgOvertimeHours = BigDecimal.ZERO;

        AttendanceStatisticsVO statistics = AttendanceStatisticsVO.builder()
                .statisticsType("PERSONAL")
                .targetId(queryForm.getEmployeeId())
                .totalCount(totalCount)
                .attendanceCount(attendanceCount)
                .absenceCount(absenceCount)
                .attendanceRate(attendanceRate)
                .avgWorkHours(avgWorkHours)
                .lateCount(lateCount)
                .earlyLeaveCount(earlyLeaveCount)
                .totalOvertimeHours(totalOvertimeHours)
                .avgOvertimeHours(avgOvertimeHours)
                .build();

        log.info("[考勤统计] 查询个人统计成功: employeeId={}", queryForm.getEmployeeId());
        return statistics;
    }

    /**
     * 查询部门统计
     *
     * @param queryForm 查询表单
     * @return 部门统计数据
     */
    @Override
    public AttendanceStatisticsVO getDepartmentStatistics(AttendanceStatisticsQueryForm queryForm) {
        log.info("[考勤统计] 查询部门统计: queryForm={}", queryForm);

        if (queryForm.getDepartmentId() == null) {
            log.warn("[考勤统计] 部门ID不能为空");
            throw new IllegalArgumentException("部门ID不能为空");
        }

        LocalDate startDate = queryForm.getStartDate() != null ? queryForm.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = queryForm.getEndDate() != null ? queryForm.getEndDate() : LocalDate.now();

        // 优先从汇总表读取
        try {
            AttendanceStatisticsVO statistics = attendanceSummaryService.getDepartmentSummary(
                    queryForm.getDepartmentId(), startDate, endDate);

            if (statistics != null && statistics.getTotalCount() != null && statistics.getTotalCount() > 0) {
                log.info("[考勤统计] 从汇总表查询部门统计成功: departmentId={}", queryForm.getDepartmentId());
                return statistics;
            }
        } catch (Exception e) {
            log.warn("[考勤统计] 从汇总表读取失败，使用实时计算: error={}", e.getMessage());
        }

        // 降级到实时计算（原来的GatewayServiceClient实现）
        log.info("[考勤统计] 使用实时计算部门统计: departmentId={}", queryForm.getDepartmentId());

        try {
            // 1. 通过GatewayServiceClient调用用户服务获取部门员工列表
            @SuppressWarnings("unchecked")
            ResponseDTO<List<EmployeeVO>> employeeResponse = gatewayServiceClient.callCommonService(
                    "/api/employee/department/" + queryForm.getDepartmentId(),
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<List<EmployeeVO>>>() {}
            );

            if (employeeResponse == null || employeeResponse.getData() == null) {
                log.warn("[考勤统计] 获取部门员工列表失败: departmentId={}", queryForm.getDepartmentId());
                throw new RuntimeException("获取部门员工列表失败");
            }

            List<EmployeeVO> employees = employeeResponse.getData();
            if (employees.isEmpty()) {
                log.info("[考勤统计] 部门无员工: departmentId={}", queryForm.getDepartmentId());
                return AttendanceStatisticsVO.builder()
                        .statisticsType("DEPARTMENT")
                        .targetId(queryForm.getDepartmentId())
                        .targetName("部门")
                        .totalCount(0)
                        .attendanceCount(0)
                        .absenceCount(0)
                        .attendanceRate(BigDecimal.ZERO)
                        .avgWorkHours(BigDecimal.ZERO)
                        .lateCount(0)
                        .earlyLeaveCount(0)
                        .totalOvertimeHours(BigDecimal.ZERO)
                        .avgOvertimeHours(BigDecimal.ZERO)
                        .build();
            }

            // 2. 提取员工ID列表
            List<Long> employeeIds = employees.stream()
                    .map(EmployeeVO::getEmployeeId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 3. 查询部门所有员工的考勤记录
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(AttendanceRecordEntity::getUserId, employeeIds)
                    .ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                    .le(AttendanceRecordEntity::getAttendanceDate, endDate);

            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

            // 4. 统计部门级别数据
            int totalCount = employees.size();
            int attendanceCount = (int) records.stream()
                    .filter(r -> "NORMAL".equals(r.getAttendanceStatus())).count();
            int absenceCount = records.size() - attendanceCount;
            BigDecimal attendanceRate = records.size() > 0 ?
                    BigDecimal.valueOf(attendanceCount * 100.0 / records.size()).setScale(2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            BigDecimal avgWorkHours = BigDecimal.valueOf(8.0);

            int lateCount = (int) records.stream()
                    .filter(r -> "LATE".equals(r.getAttendanceStatus())).count();
            int earlyLeaveCount = (int) records.stream()
                    .filter(r -> "EARLY".equals(r.getAttendanceStatus())).count();

            BigDecimal totalOvertimeHours = BigDecimal.ZERO;
            BigDecimal avgOvertimeHours = BigDecimal.ZERO;

            // 获取部门名称
            String departmentName = employees.stream()
                    .map(EmployeeVO::getDepartmentName)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("部门");

            AttendanceStatisticsVO statistics = AttendanceStatisticsVO.builder()
                    .statisticsType("DEPARTMENT")
                    .targetId(queryForm.getDepartmentId())
                    .targetName(departmentName)
                    .totalCount(totalCount)
                    .attendanceCount(attendanceCount)
                    .absenceCount(absenceCount)
                    .attendanceRate(attendanceRate)
                    .avgWorkHours(avgWorkHours)
                    .lateCount(lateCount)
                    .earlyLeaveCount(earlyLeaveCount)
                    .totalOvertimeHours(totalOvertimeHours)
                    .avgOvertimeHours(avgOvertimeHours)
                    .build();

            log.info("[考勤统计] 查询部门统计成功: departmentId={}, employeeCount={}", queryForm.getDepartmentId(), totalCount);
            return statistics;

        } catch (Exception e) {
            log.error("[考勤统计] 查询部门统计异常: departmentId={}, error={}", queryForm.getDepartmentId(), e.getMessage(), e);
            throw new RuntimeException("查询部门统计失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询公司统计
     *
     * @param queryForm 查询表单
     * @return 公司统计数据
     */
    @Override
    public AttendanceStatisticsVO getCompanyStatistics(AttendanceStatisticsQueryForm queryForm) {
        log.info("[考勤统计] 查询公司统计: queryForm={}", queryForm);

        LocalDate startDate = queryForm.getStartDate() != null ? queryForm.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = queryForm.getEndDate() != null ? queryForm.getEndDate() : LocalDate.now();

        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                .le(AttendanceRecordEntity::getAttendanceDate, endDate);

        List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

        // 统计公司级别数据
        long totalEmployees = records.stream().map(AttendanceRecordEntity::getUserId).distinct().count();
        int totalCount = records.size();
        int attendanceCount = (int) records.stream()
                .filter(r -> "NORMAL".equals(r.getAttendanceStatus())).count();
        int absenceCount = totalCount - attendanceCount;
        BigDecimal attendanceRate = totalCount > 0 ?
                BigDecimal.valueOf(attendanceCount * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal avgWorkHours = BigDecimal.valueOf(8.0);

        int lateCount = (int) records.stream()
                .filter(r -> "LATE".equals(r.getAttendanceStatus())).count();
        int earlyLeaveCount = (int) records.stream()
                .filter(r -> "EARLY".equals(r.getAttendanceStatus())).count();

        BigDecimal totalOvertimeHours = BigDecimal.ZERO;
        BigDecimal avgOvertimeHours = BigDecimal.ZERO;

        AttendanceStatisticsVO statistics = AttendanceStatisticsVO.builder()
                .statisticsType("COMPANY")
                .targetId(0L)
                .targetName("公司")
                .totalCount((int) totalEmployees)
                .attendanceCount(attendanceCount)
                .absenceCount(absenceCount)
                .attendanceRate(attendanceRate)
                .avgWorkHours(avgWorkHours)
                .lateCount(lateCount)
                .earlyLeaveCount(earlyLeaveCount)
                .totalOvertimeHours(totalOvertimeHours)
                .avgOvertimeHours(avgOvertimeHours)
                .build();

        log.info("[考勤统计] 查询公司统计成功");
        return statistics;
    }

    /**
     * 导出统计数据
     *
     * @param queryForm 查询表单
     * @param response HTTP响应
     */
    @Override
    public void exportStatistics(AttendanceStatisticsQueryForm queryForm, HttpServletResponse response) {
        log.info("[考勤统计] 导出统计数据: queryForm={}", queryForm);

        try {
            // 1. 根据统计类型获取统计数据
            AttendanceStatisticsVO statistics;
            String statisticsTypeName;

            if ("PERSONAL".equals(queryForm.getStatisticsType())) {
                statistics = getPersonalStatistics(queryForm);
                statisticsTypeName = "个人统计";
            } else if ("DEPARTMENT".equals(queryForm.getStatisticsType())) {
                statistics = getDepartmentStatistics(queryForm);
                statisticsTypeName = "部门统计";
            } else {
                statistics = getCompanyStatistics(queryForm);
                statisticsTypeName = "公司统计";
            }

            // 2. 转换为Excel数据模型
            List<AttendanceStatisticsExcelVO> excelData = new ArrayList<>();
            excelData.add(AttendanceStatisticsExcelVO.builder()
                    .statisticsType(statisticsTypeName)
                    .targetName(statistics.getTargetName() != null ? statistics.getTargetName() : "")
                    .totalCount(statistics.getTotalCount())
                    .attendanceCount(statistics.getAttendanceCount())
                    .absenceCount(statistics.getAbsenceCount())
                    .attendanceRate(statistics.getAttendanceRate())
                    .avgWorkHours(statistics.getAvgWorkHours())
                    .lateCount(statistics.getLateCount())
                    .earlyLeaveCount(statistics.getEarlyLeaveCount())
                    .totalOvertimeHours(statistics.getTotalOvertimeHours())
                    .avgOvertimeHours(statistics.getAvgOvertimeHours())
                    .build());

            // 3. 生成文件名
            String fileName = AttendanceExcelUtil.generateFileName(statisticsTypeName);

            // 4. 导出Excel
            AttendanceExcelUtil.exportExcel(response, excelData,
                    AttendanceStatisticsExcelVO.class, fileName);

            log.info("[考勤统计] 导出统计数据成功: fileName={}, statisticsType={}", fileName, queryForm.getStatisticsType());

        } catch (Exception e) {
            log.error("[考勤统计] 导出统计数据失败: queryForm={}, error={}", queryForm, e.getMessage(), e);
            throw new RuntimeException("导出统计数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询图表数据
     *
     * @param queryForm 查询表单
     * @return 图表数据
     */
    @Override
    public Map<String, Object> getChartData(AttendanceStatisticsQueryForm queryForm) {
        log.info("[考勤统计] 查询图表数据: queryForm={}", queryForm);

        LocalDate startDate = queryForm.getStartDate() != null ? queryForm.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = queryForm.getEndDate() != null ? queryForm.getEndDate() : LocalDate.now();

        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                .le(AttendanceRecordEntity::getAttendanceDate, endDate);

        if (queryForm.getEmployeeId() != null) {
            queryWrapper.eq(AttendanceRecordEntity::getUserId, queryForm.getEmployeeId());
        }

        List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

        // 生成趋势数据
        List<Map<String, Object>> trendData = new ArrayList<>();
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (int i = 0; i < daysBetween; i++) {
            LocalDate date = startDate.plusDays(i);
            final LocalDate currentDate = date;

            int attendanceCount = (int) records.stream()
                    .filter(r -> r.getAttendanceDate().equals(currentDate) &&
                            "NORMAL".equals(r.getAttendanceStatus()))
                    .count();

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", date.toString());
            dataPoint.put("attendanceCount", attendanceCount);
            trendData.add(dataPoint);
        }

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("trend", trendData);
        chartData.put("summary", Map.of(
                "totalRecords", records.size(),
                "startDate", startDate.toString(),
                "endDate", endDate.toString()
        ));

        log.info("[考勤统计] 查询图表数据成功");
        return chartData;
    }
}
