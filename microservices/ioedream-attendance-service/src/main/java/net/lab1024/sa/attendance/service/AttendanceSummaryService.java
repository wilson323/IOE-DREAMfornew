package net.lab1024.sa.attendance.service;

import java.time.LocalDate;
import java.util.List;

import net.lab1024.sa.attendance.domain.form.AttendanceStatisticsQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceStatisticsVO;

/**
 * 考勤汇总服务接口
 * <p>
 * 提供考勤汇总数据的生成、查询和管理功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
public interface AttendanceSummaryService {

    /**
     * 生成个人月度汇总
     *
     * @param employeeId 员工ID
     * @param year 年份
     * @param month 月份（1-12）
     * @return 是否成功
     */
    boolean generatePersonalSummary(Long employeeId, int year, int month);

    /**
     * 批量生成个人月度汇总（指定月份的所有员工）
     *
     * @param year 年份
     * @param month 月份（1-12）
     * @return 生成的汇总记录数
     */
    int batchGeneratePersonalSummaries(int year, int month);

    /**
     * 生成部门月度统计
     *
     * @param departmentId 部门ID
     * @param year 年份
     * @param month 月份（1-12）
     * @return 是否成功
     */
    boolean generateDepartmentStatistics(Long departmentId, int year, int month);

    /**
     * 批量生成部门月度统计（所有部门）
     *
     * @param year 年份
     * @param month 月份（1-12）
     * @return 生成的统计记录数
     */
    int batchGenerateDepartmentStatistics(int year, int month);

    /**
     * 查询个人汇总
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 汇总数据
     */
    AttendanceStatisticsVO getPersonalSummary(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询部门统计
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    AttendanceStatisticsVO getDepartmentSummary(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 触发全量汇总生成（生成指定月份的所有汇总和统计数据）
     *
     * @param year 年份
     * @param month 月份（1-12）
     * @return 生成结果摘要
     */
    String triggerFullSummaryGeneration(int year, int month);
}
