package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 考勤记录 DAO
 *
 * 严格遵循repowiki规范:
 * - 继承BaseMapper，提供基础CRUD操作
 * - 使用@Mapper注解标记
 * - 提供复杂查询接口
 * - 支持统计和分析功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface AttendanceRecordDao extends BaseMapper<AttendanceRecordEntity> {

    /**
     * 根据员工ID和日期范围查询考勤记录
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> selectByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 根据日期查询考勤记录
     *
     * @param attendanceDate 考勤日期
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> selectByDate(@Param("attendanceDate") LocalDate attendanceDate);

    /**
     * 根据部门ID和日期范围查询考勤记录
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> selectByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询指定日期的异常考勤记录
     *
     * @param attendanceDate 考勤日期
     * @return 异常考勤记录列表
     */
    List<AttendanceRecordEntity> selectAbnormalByDate(@Param("attendanceDate") LocalDate attendanceDate);

    /**
     * 查询指定时间范围的异常考勤记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param exceptionTypes 异常类型，多个用逗号分隔
     * @return 异常考勤记录列表
     */
    List<AttendanceRecordEntity> selectAbnormalByDateRangeAndTypes(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("exceptionTypes") String exceptionTypes
    );

    /**
     * 统计员工考勤数据
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Map<String, Object> selectEmployeeAttendanceStats(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计部门考勤数据
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Map<String, Object> selectDepartmentAttendanceStats(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询迟到记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentIds 部门ID列表
     * @return 迟到记录列表
     */
    List<AttendanceRecordEntity> selectLateRecords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentIds") List<Long> departmentIds
    );

    /**
     * 查询早退记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentIds 部门ID列表
     * @return 早退记录列表
     */
    List<AttendanceRecordEntity> selectEarlyLeaveRecords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentIds") List<Long> departmentIds
    );

    /**
     * 查询旷工记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentIds 部门ID列表
     * @return 旷工记录列表
     */
    List<AttendanceRecordEntity> selectAbsentRecords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentIds") List<Long> departmentIds
    );

    /**
     * 查询加班记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentIds 部门ID列表
     * @return 加班记录列表
     */
    List<AttendanceRecordEntity> selectOvertimeRecords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentIds") List<Long> departmentIds
    );

    /**
     * 按条件分页查询考勤记录
     *
     * @param employeeId 员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param attendanceStatus 考勤状态（可选）
     * @param exceptionType 异常类型（可选）
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> selectByCondition(
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("attendanceStatus") String attendanceStatus,
            @Param("exceptionType") String exceptionType
    );

    /**
     * 查询员工指定日期的考勤记录
     *
     * @param employeeId 员工ID
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     */
    AttendanceRecordEntity selectEmployeeTodayRecord(
            @Param("employeeId") Long employeeId,
            @Param("attendanceDate") LocalDate attendanceDate
    );

    /**
     * 检查员工在指定日期是否有考勤记录
     *
     * @param employeeId 员工ID
     * @param attendanceDate 考勤日期
     * @return 是否存在记录
     */
    Boolean existsByEmployeeAndDate(
            @Param("employeeId") Long employeeId,
            @Param("attendanceDate") LocalDate attendanceDate
    );

    /**
     * 查询员工的首次打卡时间
     *
     * @param employeeId 员工ID
     * @param attendanceDate 考勤日期
     * @return 首次打卡时间
     */
    LocalTime selectFirstPunchInTime(
            @Param("employeeId") Long employeeId,
            @Param("attendanceDate") LocalDate attendanceDate
    );

    /**
     * 查询员工的最后打卡时间
     *
     * @param employeeId 员工ID
     * @param attendanceDate 考勤日期
     * @return 最后打卡时间
     */
    LocalTime selectLastPunchOutTime(
            @Param("employeeId") Long employeeId,
            @Param("attendanceDate") LocalDate attendanceDate
    );

    /**
     * 统计月度考勤汇总
     *
     * @param year 年份
     * @param month 月份
     * @param departmentId 部门ID（可选）
     * @return 月度统计数据
     */
    List<Map<String, Object>> selectMonthlySummary(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("departmentId") Long departmentId
    );

    /**
     * 考勤异常趋势分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @param groupBy 分组方式：day/week/month
     * @return 趋势数据
     */
    List<Map<String, Object>> selectAbnormalTrend(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId,
            @Param("groupBy") String groupBy
    );

    /**
     * 查询连续旷工记录
     *
     * @param minConsecutiveDays 最少连续天数
     * @param endDate 截止日期
     * @return 连续旷工记录
     */
    List<Map<String, Object>> selectConsecutiveAbsentRecords(
            @Param("minConsecutiveDays") Integer minConsecutiveDays,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 批量更新考勤状态
     *
     * @param recordIds 记录ID列表
     * @param attendanceStatus 考勤状态
     * @param exceptionType 异常类型
     * @return 更新行数
     */
    int batchUpdateStatus(
            @Param("recordIds") List<Long> recordIds,
            @Param("attendanceStatus") String attendanceStatus,
            @Param("exceptionType") String exceptionType
    );

    /**
     * 清理指定日期之前的考勤记录
     *
     * @param beforeDate 清理日期
     * @return 清理行数
     */
    int cleanRecordsBefore(@Param("beforeDate") LocalDate beforeDate);

    /**
     * 获取考勤数据统计报表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dimension 统计维度：department/employee/date
     * @return 统计报表数据
     */
    List<Map<String, Object>> selectAttendanceReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dimension") String dimension
    );

    /**
     * 保存考勤记录
     *
     * @param record 考勤记录实体
     * @return 影响行数
     */
    int save(AttendanceRecordEntity record);

    /**
     * 查询异常考勤记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param employeeId 员工ID（可选）
     * @param exceptionType 异常类型（可选）
     * @return 异常考勤记录列表
     */
    List<AttendanceRecordEntity> selectExceptionRecords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("employeeId") Long employeeId,
            @Param("exceptionType") String exceptionType
    );
}

