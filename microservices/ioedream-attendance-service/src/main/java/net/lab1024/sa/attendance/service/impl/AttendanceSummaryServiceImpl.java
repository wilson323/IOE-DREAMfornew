package net.lab1024.sa.attendance.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.common.util.QueryBuilder;

import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceSummaryDao;
import net.lab1024.sa.attendance.dao.DepartmentStatisticsDao;
import net.lab1024.sa.attendance.domain.vo.AttendanceStatisticsVO;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.entity.AttendanceSummaryEntity;
import net.lab1024.sa.attendance.entity.DepartmentStatisticsEntity;
import net.lab1024.sa.attendance.service.AttendanceSummaryService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.system.employee.domain.vo.EmployeeVO;

/**
 * 考勤汇总服务实现类
 * <p>
 * 从AttendanceRecord聚合数据到汇总表
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class AttendanceSummaryServiceImpl implements AttendanceSummaryService {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private AttendanceSummaryDao attendanceSummaryDao;

    @Resource
    private DepartmentStatisticsDao departmentStatisticsDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean generatePersonalSummary(Long employeeId, int year, int month) {
        log.info("[汇总服务] 生成个人汇总: employeeId={}, year={}, month={}", employeeId, year, month);

        try {
            String summaryMonth = String.format("%d-%02d", year, month);
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            // 查询该员工该月的所有考勤记录
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getUserId, employeeId)
                    .ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                    .le(AttendanceRecordEntity::getAttendanceDate, endDate);

            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

            // 聚合统计数据
            AttendanceSummaryEntity summary = aggregatePersonalSummary(employeeId, summaryMonth, records);

            // 删除旧汇总（如果存在）
            attendanceSummaryDao.delete(
                    QueryBuilder.of(AttendanceSummaryEntity.class)
                            .eq(AttendanceSummaryEntity::getEmployeeId, employeeId)
                            .eq(AttendanceSummaryEntity::getSummaryMonth, summaryMonth)
                            .build()
            );

            // 插入新汇总
            int result = attendanceSummaryDao.insert(summary);

            log.info("[汇总服务] 个人汇总生成成功: employeeId={}, month={}", employeeId, summaryMonth);
            return result > 0;

        } catch (Exception e) {
            log.error("[汇总服务] 生成个人汇总失败: employeeId={}, error={}", employeeId, e.getMessage(), e);
            throw new RuntimeException("生成个人汇总失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGeneratePersonalSummaries(int year, int month) {
        log.info("[汇总服务] 批量生成个人汇总: year={}, month={}", year, month);

        try {
            String summaryMonth = String.format("%d-%02d", year, month);
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            // 查询该月所有有考勤记录的员工
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                    .le(AttendanceRecordEntity::getAttendanceDate, endDate)
                    .select(AttendanceRecordEntity::getUserId);

            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

            // 按员工分组
            Map<Long, List<AttendanceRecordEntity>> recordsByEmployee = new HashMap<>();
            for (AttendanceRecordEntity record : records) {
                Long employeeId = record.getUserId();
                recordsByEmployee.computeIfAbsent(employeeId, k -> new java.util.ArrayList<>()).add(record);
            }

            // 为每个员工生成汇总
            int count = 0;
            for (Map.Entry<Long, List<AttendanceRecordEntity>> entry : recordsByEmployee.entrySet()) {
                Long employeeId = entry.getKey();
                List<AttendanceRecordEntity> employeeRecords = entry.getValue();

                AttendanceSummaryEntity summary = aggregatePersonalSummary(employeeId, summaryMonth, employeeRecords);

                // 删除旧汇总
                attendanceSummaryDao.delete(
                    QueryBuilder.of(AttendanceSummaryEntity.class)
                            .eq(AttendanceSummaryEntity::getEmployeeId, employeeId)
                            .eq(AttendanceSummaryEntity::getSummaryMonth, summaryMonth)
                            .build()
            );

                // 插入新汇总
                attendanceSummaryDao.insert(summary);
                count++;
            }

            log.info("[汇总服务] 批量生成个人汇总完成: year={}, month={}, count={}", year, month, count);
            return count;

        } catch (Exception e) {
            log.error("[汇总服务] 批量生成个人汇总失败: year={}, month={}, error={}", year, month, e.getMessage(), e);
            throw new RuntimeException("批量生成个人汇总失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean generateDepartmentStatistics(Long departmentId, int year, int month) {
        log.info("[汇总服务] 生成部门统计: departmentId={}, year={}, month={}", departmentId, year, month);

        try {
            String statisticsMonth = String.format("%d-%02d", year, month);

            // 查询该部门该月的所有汇总记录
            List<AttendanceSummaryEntity> summaries = attendanceSummaryDao.selectByDepartmentAndMonth(
                    departmentId, statisticsMonth);

            if (summaries.isEmpty()) {
                log.warn("[汇总服务] 部门无汇总数据: departmentId={}, month={}", departmentId, statisticsMonth);
                return false;
            }

            // 聚合部门统计数据
            DepartmentStatisticsEntity statistics = aggregateDepartmentStatistics(departmentId, statisticsMonth, summaries);

            // 删除旧统计
            departmentStatisticsDao.delete(
                    QueryBuilder.of(DepartmentStatisticsEntity.class)
                            .eq(DepartmentStatisticsEntity::getDepartmentId, departmentId)
                            .eq(DepartmentStatisticsEntity::getStatisticsMonth, statisticsMonth)
                            .build()
            );

            // 插入新统计
            int result = departmentStatisticsDao.insert(statistics);

            log.info("[汇总服务] 部门统计生成成功: departmentId={}, month={}", departmentId, statisticsMonth);
            return result > 0;

        } catch (Exception e) {
            log.error("[汇总服务] 生成部门统计失败: departmentId={}, error={}", departmentId, e.getMessage(), e);
            throw new RuntimeException("生成部门统计失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGenerateDepartmentStatistics(int year, int month) {
        log.info("[汇总服务] 批量生成部门统计: year={}, month={}", year, month);

        try {
            String statisticsMonth = String.format("%d-%02d", year, month);

            // 查询该月的所有汇总记录
            List<AttendanceSummaryEntity> summaries = attendanceSummaryDao.selectByMonth(statisticsMonth);

            // 按部门分组
            Map<Long, List<AttendanceSummaryEntity>> summariesByDepartment = new HashMap<>();
            for (AttendanceSummaryEntity summary : summaries) {
                Long departmentId = summary.getDepartmentId();
                if (departmentId != null) {
                    summariesByDepartment.computeIfAbsent(departmentId, k -> new java.util.ArrayList<>()).add(summary);
                }
            }

            // 为每个部门生成统计
            int count = 0;
            for (Map.Entry<Long, List<AttendanceSummaryEntity>> entry : summariesByDepartment.entrySet()) {
                Long departmentId = entry.getKey();
                List<AttendanceSummaryEntity> departmentSummaries = entry.getValue();

                DepartmentStatisticsEntity statistics = aggregateDepartmentStatistics(departmentId, statisticsMonth, departmentSummaries);

                // 删除旧统计
                departmentStatisticsDao.delete(
                    QueryBuilder.of(DepartmentStatisticsEntity.class)
                            .eq(DepartmentStatisticsEntity::getDepartmentId, departmentId)
                            .eq(DepartmentStatisticsEntity::getStatisticsMonth, statisticsMonth)
                            .build()
            );

                // 插入新统计
                departmentStatisticsDao.insert(statistics);
                count++;
            }

            log.info("[汇总服务] 批量生成部门统计完成: year={}, month={}, count={}", year, month, count);
            return count;

        } catch (Exception e) {
            log.error("[汇总服务] 批量生成部门统计失败: year={}, month={}, error={}", year, month, e.getMessage(), e);
            throw new RuntimeException("批量生成部门统计失败: " + e.getMessage(), e);
        }
    }

    @Override
    public AttendanceStatisticsVO getPersonalSummary(Long employeeId, LocalDate startDate, LocalDate endDate) {
        log.info("[汇总服务] 查询个人汇总: employeeId={}, startDate={}, endDate={}", employeeId, startDate, endDate);

        // 计算月份范围
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);

        // 查询汇总数据
        List<String> months = new java.util.ArrayList<>();
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            months.add(String.format("%d-%02d", current.getYear(), current.getMonthValue()));
            current = current.plusMonths(1);
        }

        LambdaQueryWrapper<AttendanceSummaryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceSummaryEntity::getEmployeeId, employeeId)
                .in(AttendanceSummaryEntity::getSummaryMonth, months);

        List<AttendanceSummaryEntity> summaries = attendanceSummaryDao.selectList(queryWrapper);

        // 聚合多个月的数据
        return aggregateSummaryToVO(summaries);
    }

    @Override
    public AttendanceStatisticsVO getDepartmentSummary(Long departmentId, LocalDate startDate, LocalDate endDate) {
        log.info("[汇总服务] 查询部门统计: departmentId={}, startDate={}, endDate={}", departmentId, startDate, endDate);

        // 计算月份范围
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);

        // 查询统计数据
        List<String> months = new java.util.ArrayList<>();
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            months.add(String.format("%d-%02d", current.getYear(), current.getMonthValue()));
            current = current.plusMonths(1);
        }

        LambdaQueryWrapper<DepartmentStatisticsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DepartmentStatisticsEntity::getDepartmentId, departmentId)
                .in(DepartmentStatisticsEntity::getStatisticsMonth, months);

        List<DepartmentStatisticsEntity> statisticsList = departmentStatisticsDao.selectList(queryWrapper);

        // 聚合多个月的数据
        return aggregateStatisticsToVO(departmentId, statisticsList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String triggerFullSummaryGeneration(int year, int month) {
        log.info("[汇总服务] 触发全量汇总生成: year={}, month={}", year, month);

        long startTime = System.currentTimeMillis();

        // 1. 生成个人汇总
        int personalCount = batchGeneratePersonalSummaries(year, month);

        // 2. 生成部门统计
        int departmentCount = batchGenerateDepartmentStatistics(year, month);

        long duration = System.currentTimeMillis() - startTime;

        String result = String.format("汇总生成完成：个人汇总=%d条，部门统计=%d条，耗时=%dms",
                personalCount, departmentCount, duration);

        log.info("[汇总服务] {}", result);
        return result;
    }

    /**
     * 聚合个人汇总数据
     */
    private AttendanceSummaryEntity aggregatePersonalSummary(Long employeeId, String summaryMonth,
                                                            List<AttendanceRecordEntity> records) {
        if (records.isEmpty()) {
            // 没有考勤记录，创建空汇总
            AttendanceSummaryEntity summary = new AttendanceSummaryEntity();
            summary.setEmployeeId(employeeId);
            summary.setSummaryMonth(summaryMonth);
            summary.setWorkDays(0);
            summary.setActualDays(0);
            summary.setAbsentDays(0);
            summary.setLateCount(0);
            summary.setEarlyCount(0);
            summary.setOvertimeHours(BigDecimal.ZERO);
            summary.setWeekendOvertimeHours(BigDecimal.ZERO);
            summary.setLeaveDays(BigDecimal.ZERO);
            summary.setAttendanceRate(BigDecimal.ZERO);
            summary.setStatus(1);
            return summary;
        }

        // 从第一条记录获取基本信息
        AttendanceRecordEntity firstRecord = records.get(0);
        AttendanceSummaryEntity summary = new AttendanceSummaryEntity();
        summary.setEmployeeId(employeeId);
        summary.setEmployeeName(firstRecord.getUserName());
        summary.setDepartmentId(firstRecord.getDepartmentId());
        summary.setDepartmentName(firstRecord.getDepartmentName());
        summary.setSummaryMonth(summaryMonth);

        // 统计各类数据
        int actualDays = (int) records.stream()
                .filter(r -> "NORMAL".equals(r.getAttendanceStatus())).count();
        int absentDays = (int) records.stream()
                .filter(r -> "ABSENT".equals(r.getAttendanceStatus())).count();
        int lateCount = (int) records.stream()
                .filter(r -> "LATE".equals(r.getAttendanceStatus())).count();
        int earlyCount = (int) records.stream()
                .filter(r -> "EARLY".equals(r.getAttendanceStatus())).count();

        // 计算工作日（假设22天）
        int workDays = 22;
        BigDecimal attendanceRate = workDays > 0 ?
                BigDecimal.valueOf((double) actualDays / workDays).setScale(4, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        summary.setWorkDays(workDays);
        summary.setActualDays(actualDays);
        summary.setAbsentDays(absentDays);
        summary.setLateCount(lateCount);
        summary.setEarlyCount(earlyCount);
        summary.setOvertimeHours(BigDecimal.ZERO); // 暂时设为0
        summary.setWeekendOvertimeHours(BigDecimal.ZERO);
        summary.setLeaveDays(BigDecimal.ZERO); // 需要从请假表获取
        summary.setAttendanceRate(attendanceRate);
        summary.setStatus(1);

        return summary;
    }

    /**
     * 聚合部门统计数据
     */
    private DepartmentStatisticsEntity aggregateDepartmentStatistics(Long departmentId, String statisticsMonth,
                                                                   List<AttendanceSummaryEntity> summaries) {
        DepartmentStatisticsEntity statistics = new DepartmentStatisticsEntity();
        statistics.setDepartmentId(departmentId);
        statistics.setDepartmentName(summaries.isEmpty() ? "" : summaries.get(0).getDepartmentName());
        statistics.setStatisticsMonth(statisticsMonth);

        int totalEmployees = summaries.size();
        int presentEmployees = (int) summaries.stream()
                .filter(s -> s.getActualDays() > 0).count();
        int absentEmployees = totalEmployees - presentEmployees;
        int lateEmployees = (int) summaries.stream()
                .filter(s -> s.getLateCount() > 0).count();

        // 计算平均出勤率
        double avgAttendanceRate = summaries.stream()
                .map(AttendanceSummaryEntity::getAttendanceRate)
                .map(BigDecimal::doubleValue)
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0);

        statistics.setTotalEmployees(totalEmployees);
        statistics.setPresentEmployees(presentEmployees);
        statistics.setAbsentEmployees(absentEmployees);
        statistics.setLateEmployees(lateEmployees);
        statistics.setAttendanceRate(BigDecimal.valueOf(avgAttendanceRate).setScale(4, RoundingMode.HALF_UP));
        statistics.setAvgWorkHours(BigDecimal.valueOf(8.0));
        statistics.setTotalOvertimeHours(BigDecimal.ZERO);
        statistics.setExceptionCount(0); // TODO: 计算异常次数
        statistics.setStatus(1);

        return statistics;
    }

    /**
     * 将汇总实体转换为VO
     */
    private AttendanceStatisticsVO aggregateSummaryToVO(List<AttendanceSummaryEntity> summaries) {
        int totalCount = summaries.size();
        int attendanceCount = summaries.stream().mapToInt(AttendanceSummaryEntity::getActualDays).sum();
        int absenceCount = summaries.stream().mapToInt(AttendanceSummaryEntity::getAbsentDays).sum();
        int lateCount = summaries.stream().mapToInt(AttendanceSummaryEntity::getLateCount).sum();
        int earlyCount = summaries.stream().mapToInt(AttendanceSummaryEntity::getEarlyCount).sum();

        BigDecimal avgAttendanceRate = summaries.stream()
                .map(AttendanceSummaryEntity::getAttendanceRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);

        return AttendanceStatisticsVO.builder()
                .statisticsType("PERSONAL")
                .totalCount(totalCount)
                .attendanceCount(attendanceCount)
                .absenceCount(absenceCount)
                .attendanceRate(avgAttendanceRate.multiply(BigDecimal.valueOf(100)))
                .avgWorkHours(BigDecimal.valueOf(8.0))
                .lateCount(lateCount)
                .earlyLeaveCount(earlyCount)
                .totalOvertimeHours(BigDecimal.ZERO)
                .avgOvertimeHours(BigDecimal.ZERO)
                .build();
    }

    /**
     * 将统计实体转换为VO
     */
    private AttendanceStatisticsVO aggregateStatisticsToVO(Long departmentId,
                                                           List<DepartmentStatisticsEntity> statisticsList) {
        if (statisticsList.isEmpty()) {
            return AttendanceStatisticsVO.builder()
                    .statisticsType("DEPARTMENT")
                    .targetId(departmentId)
                    .targetName("部门")
                    .totalCount(0)
                    .attendanceCount(0)
                    .absenceCount(0)
                    .attendanceRate(BigDecimal.ZERO)
                    .build();
        }

        DepartmentStatisticsEntity stats = statisticsList.get(0);

        return AttendanceStatisticsVO.builder()
                .statisticsType("DEPARTMENT")
                .targetId(departmentId)
                .targetName(stats.getDepartmentName())
                .totalCount(stats.getTotalEmployees())
                .attendanceCount(stats.getPresentEmployees())
                .absenceCount(stats.getAbsentEmployees())
                .attendanceRate(stats.getAttendanceRate().multiply(BigDecimal.valueOf(100)))
                .avgWorkHours(stats.getAvgWorkHours())
                .lateCount(stats.getLateEmployees())
                .earlyLeaveCount(0)
                .totalOvertimeHours(stats.getTotalOvertimeHours())
                .avgOvertimeHours(BigDecimal.ZERO)
                .build();
    }
}
