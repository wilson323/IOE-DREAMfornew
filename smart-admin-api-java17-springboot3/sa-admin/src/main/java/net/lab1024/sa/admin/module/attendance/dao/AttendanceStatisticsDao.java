package net.lab1024.sa.admin.module.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceStatisticsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤统计 DAO
 *
 * 严格遵循repowiki规范:
 * - 继承BaseMapper，提供基础CRUD操作
 * - 使用@Mapper注解标记
 * - 提供统计查询和分析接口
 * - 支持多维度统计分析
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface AttendanceStatisticsDao extends BaseMapper<AttendanceStatisticsEntity> {

    /**
     * 根据员工ID和统计类型查询统计数据
     *
     * @param employeeId 员工ID
     * @param statisticsType 统计类型：DAILY/WEEKLY/MONTHLY/QUARTERLY/YEARLY
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<AttendanceStatisticsEntity> selectByEmployeeAndType(
            @Param("employeeId") Long employeeId,
            @Param("statisticsType") String statisticsType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 根据部门ID和统计类型查询统计数据
     *
     * @param departmentId 部门ID
     * @param statisticsType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<AttendanceStatisticsEntity> selectByDepartmentAndType(
            @Param("departmentId") Long departmentId,
            @Param("statisticsType") String statisticsType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 根据统计类型查询统计数据
     *
     * @param statisticsType 统计类型
     * @param statisticsPeriod 统计周期（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计数据列表
     */
    List<AttendanceStatisticsEntity> selectByStatisticsType(
            @Param("statisticsType") String statisticsType,
            @Param("statisticsPeriod") String statisticsPeriod,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询员工最新统计
     *
     * @param employeeId 员工ID
     * @param statisticsType 统计类型（可选）
     * @return 最新统计数据
     */
    AttendanceStatisticsEntity selectLatestByEmployee(
            @Param("employeeId") Long employeeId,
            @Param("statisticsType") String statisticsType
    );

    /**
     * 查询部门最新统计
     *
     * @param departmentId 部门ID
     * @param statisticsType 统计类型（可选）
     * @return 最新统计数据
     */
    AttendanceStatisticsEntity selectLatestByDepartment(
            @Param("departmentId") Long departmentId,
            @Param("statisticsType") String statisticsType
    );

    /**
     * 按条件查询统计数据
     *
     * @param employeeId 员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param companyId 公司ID（可选）
     * @param statisticsType 统计类型（可选）
     * @param statisticsPeriod 统计周期（可选）
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @param quarter 季度（可选）
     * @param calculationStatus 计算状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计数据列表
     */
    List<AttendanceStatisticsEntity> selectByCondition(
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("companyId") Long companyId,
            @Param("statisticsType") String statisticsType,
            @Param("statisticsPeriod") String statisticsPeriod,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("quarter") Integer quarter,
            @Param("calculationStatus") String calculationStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询待计算的统计数据
     *
     * @param calculationStatus 计算状态
     * @return 待计算的统计数据列表
     */
    List<AttendanceStatisticsEntity> selectByCalculationStatus(@Param("calculationStatus") String calculationStatus);

    /**
     * 检查统计是否存在
     *
     * @param employeeId 员工ID
     * @param departmentId 部门ID
     * @param statisticsType 统计类型
     * @param statisticsPeriod 统计周期
     * @param statisticsDate 统计日期
     * @return 是否存在
     */
    Boolean existsStatistics(
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("statisticsType") String statisticsType,
            @Param("statisticsPeriod") String statisticsPeriod,
            @Param("statisticsDate") LocalDate statisticsDate
    );

    /**
     * 批量插入统计数据
     *
     * @param statisticsList 统计数据列表
     * @return 插入行数
     */
    int batchInsert(@Param("statisticsList") List<AttendanceStatisticsEntity> statisticsList);

    /**
     * 批量更新统计数据
     *
     * @param statisticsList 统计数据列表
     * @return 更新行数
     */
    int batchUpdate(@Param("statisticsList") List<AttendanceStatisticsEntity> statisticsList);

    /**
     * 查询月度考勤汇总
     *
     * @param year 年份
     * @param month 月份
     * @param departmentId 部门ID（可选）
     * @param employeeId 员工ID（可选）
     * @return 月度汇总数据
     */
    List<Map<String, Object>> selectMonthlySummary(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("departmentId") Long departmentId,
            @Param("employeeId") Long employeeId
    );

    /**
     * 查询季度考勤汇总
     *
     * @param year 年份
     * @param quarter 季度
     * @param departmentId 部门ID（可选）
     * @return 季度汇总数据
     */
    List<Map<String, Object>> selectQuarterlySummary(
            @Param("year") Integer year,
            @Param("quarter") Integer quarter,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询年度考勤汇总
     *
     * @param year 年份
     * @param departmentId 部门ID（可选）
     * @return 年度汇总数据
     */
    List<Map<String, Object>> selectYearlySummary(
            @Param("year") Integer year,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询员工考勤排名
     *
     * @param statisticsType 统计类型
     * @param statisticsPeriod 统计周期
     * @param departmentId 部门ID（可选）
     * @param orderBy 排序字段：attendanceRate/punctualityRate/efficiencyRate/qualityScore
     * @param limit 返回数量限制
     * @return 排名数据
     */
    List<Map<String, Object>> selectEmployeeRanking(
            @Param("statisticsType") String statisticsType,
            @Param("statisticsPeriod") String statisticsPeriod,
            @Param("departmentId") Long departmentId,
            @Param("orderBy") String orderBy,
            @Param("limit") Integer limit
    );

    /**
     * 查询部门考勤排名
     *
     * @param statisticsType 统计类型
     * @param statisticsPeriod 统计周期
     * @param companyId 公司ID（可选）
     * @param orderBy 排序字段
     * @param limit 返回数量限制
     * @return 部门排名数据
     */
    List<Map<String, Object>> selectDepartmentRanking(
            @Param("statisticsType") String statisticsType,
            @Param("statisticsPeriod") String statisticsPeriod,
            @Param("companyId") Long companyId,
            @Param("orderBy") String orderBy,
            @Param("limit") Integer limit
    );

    /**
     * 查询考勤趋势分析
     *
     * @param employeeId 员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param statisticsType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据
     */
    List<Map<String, Object>> selectTrendAnalysis(
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("statisticsType") String statisticsType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询考勤质量分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 质量分析数据
     */
    List<Map<String, Object>> selectQualityAnalysis(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询考勤效率分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 效率分析数据
     */
    List<Map<String, Object>> selectEfficiencyAnalysis(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询考勤风险分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 风险分析数据
     */
    List<Map<String, Object>> selectRiskAnalysis(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询统计类型分布
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 统计类型分布数据
     */
    List<Map<String, Object>> selectStatisticsTypeDistribution(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询计算状态统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 计算状态统计数据
     */
    List<Map<String, Object>> selectCalculationStatusStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 更新计算状态
     *
     * @param statisticsIds 统计ID列表
     * @param calculationStatus 计算状态
     * @param lastCalculatedTime 最后计算时间
     * @return 更新行数
     */
    int batchUpdateCalculationStatus(
            @Param("statisticsIds") List<Long> statisticsIds,
            @Param("calculationStatus") String calculationStatus,
            @Param("lastCalculatedTime") java.time.LocalDateTime lastCalculatedTime
    );

    /**
     * 删除指定时间之前的统计数据
     *
     * @param beforeDate 清理日期
     * @param keepYearly 是否保留年度数据
     * @return 删除行数
     */
    int cleanOldStatistics(
            @Param("beforeDate") LocalDate beforeDate,
            @Param("keepYearly") Boolean keepYearly
    );

    /**
     * 查询统计数据完整性检查
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statisticsType 统计类型
     * @return 完整性检查结果
     */
    List<Map<String, Object>> selectDataIntegrityCheck(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statisticsType") String statisticsType
    );

    /**
     * 查询考勤统计报表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dimension 报表维度：department/employee/type/period
     * @param departmentId 部门ID（可选）
     * @param statisticsType 统计类型（可选）
     * @return 报表数据
     */
    List<Map<String, Object>> selectStatisticsReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dimension") String dimension,
            @Param("departmentId") Long departmentId,
            @Param("statisticsType") String statisticsType
    );

    /**
     * 查询员工考勤年度对比
     *
     * @param employeeId 员工ID
     * @param currentYear 当前年份
     * @param previousYear 上一年份
     * @param statisticsType 统计类型
     * @return 年度对比数据
     */
    Map<String, Object> selectEmployeeYearlyComparison(
            @Param("employeeId") Long employeeId,
            @Param("currentYear") Integer currentYear,
            @Param("previousYear") Integer previousYear,
            @Param("statisticsType") String statisticsType
    );

    /**
     * 查询部门考勤年度对比
     *
     * @param departmentId 部门ID
     * @param currentYear 当前年份
     * @param previousYear 上一年份
     * @param statisticsType 统计类型
     * @return 年度对比数据
     */
    Map<String, Object> selectDepartmentYearlyComparison(
            @Param("departmentId") Long departmentId,
            @Param("currentYear") Integer currentYear,
            @Param("previousYear") Integer previousYear,
            @Param("statisticsType") String statisticsType
    );

    /**
     * 查询绩效得分分布
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 绩效得分分布数据
     */
    List<Map<String, Object>> selectPerformanceScoreDistribution(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询风险等级分布
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 风险等级分布数据
     */
    List<Map<String, Object>> selectRiskLevelDistribution(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询工作时间分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 工作时间分析数据
     */
    List<Map<String, Object>> selectWorkingHoursAnalysis(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询加班分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 加班分析数据
     */
    List<Map<String, Object>> selectOvertimeAnalysis(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询请假使用情况
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 请假使用情况数据
     */
    List<Map<String, Object>> selectLeaveUsageAnalysis(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 获取统计仪表板数据
     *
     * @param departmentId 部门ID（可选）
     * @return 仪表板数据
     */
    Map<String, Object> selectDashboardData(@Param("departmentId") Long departmentId);

    /**
     * 查询员工考勤日历数据
     *
     * @param employeeId 员工ID
     * @param year 年份
     * @param month 月份
     * @return 日历数据
     */
    List<Map<String, Object>> selectEmployeeCalendarData(
            @Param("employeeId") Long employeeId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );
}