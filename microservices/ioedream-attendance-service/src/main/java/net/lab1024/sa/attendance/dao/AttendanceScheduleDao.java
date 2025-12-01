package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.domain.entity.AttendanceScheduleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 排班管理 DAO
 *
 * 严格遵循repowiki规范:
 * - 继承BaseMapper，提供基础CRUD操作
 * - 使用@Mapper注解标记
 * - 提供排班查询和统计接口
 * - 支持批量排班操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface AttendanceScheduleDao extends BaseMapper<AttendanceScheduleEntity> {

    /**
     * 根据员工ID和日期范围查询排班
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    List<AttendanceScheduleEntity> selectByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 根据日期查询排班
     *
     * @param scheduleDate 排班日期
     * @return 排班列表
     */
    List<AttendanceScheduleEntity> selectByDate(@Param("scheduleDate") LocalDate scheduleDate);

    /**
     * 根据部门ID和日期范围查询排班
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    List<AttendanceScheduleEntity> selectByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 根据班次ID查询排班
     *
     * @param shiftId 班次ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 排班列表
     */
    List<AttendanceScheduleEntity> selectByShiftId(
            @Param("shiftId") Long shiftId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询指定日期的加班排班
     *
     * @param scheduleDate 排班日期
     * @return 加班排班列表
     */
    List<AttendanceScheduleEntity> selectOvertimeByDate(@Param("scheduleDate") LocalDate scheduleDate);

    /**
     * 查询指定时间范围的加班排班
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentIds 部门ID列表（可选）
     * @return 加班排班列表
     */
    List<AttendanceScheduleEntity> selectOvertimeByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentIds") List<Long> departmentIds
    );

    /**
     * 查询节假日排班
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentIds 部门ID列表（可选）
     * @return 节假日排班列表
     */
    List<AttendanceScheduleEntity> selectHolidayByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentIds") List<Long> departmentIds
    );

    /**
     * 查询员工指定日期的排班
     *
     * @param employeeId 员工ID
     * @param scheduleDate 排班日期
     * @return 排班信息
     */
    AttendanceScheduleEntity selectByEmployeeAndDate(
            @Param("employeeId") Long employeeId,
            @Param("scheduleDate") LocalDate scheduleDate
    );

    /**
     * 按条件查询排班
     *
     * @param employeeId 员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param shiftId 班次ID（可选）
     * @param scheduleType 排班类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param locationId 工作地点ID（可选）
     * @return 排班列表
     */
    List<AttendanceScheduleEntity> selectByCondition(
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("shiftId") Long shiftId,
            @Param("scheduleType") String scheduleType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("locationId") Long locationId
    );

    /**
     * 查询员工的周排班
     *
     * @param employeeId 员工ID
     * @param weekStart 周开始日期
     * @param weekEnd 周结束日期
     * @return 周排班列表
     */
    List<AttendanceScheduleEntity> selectWeeklySchedule(
            @Param("employeeId") Long employeeId,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd
    );

    /**
     * 查询员工的月排班
     *
     * @param employeeId 员工ID
     * @param year 年份
     * @param month 月份
     * @return 月排班列表
     */
    List<AttendanceScheduleEntity> selectMonthlySchedule(
            @Param("employeeId") Long employeeId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    /**
     * 检查员工在指定日期是否有排班
     *
     * @param employeeId 员工ID
     * @param scheduleDate 排班日期
     * @return 是否有排班
     */
    Boolean existsScheduleForEmployee(
            @Param("employeeId") Long employeeId,
            @Param("scheduleDate") LocalDate scheduleDate
    );

    /**
     * 检查排班冲突
     *
     * @param employeeId 员工ID
     * @param scheduleDate 排班日期
     * @param workStartTime 工作开始时间
     * @param workEndTime 工作结束时间
     * @param excludeScheduleId 排除的排班ID（可选）
     * @return 冲突的排班数量
     */
    Integer checkScheduleConflict(
            @Param("employeeId") Long employeeId,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("workStartTime") String workStartTime,
            @Param("workEndTime") String workEndTime,
            @Param("excludeScheduleId") Long excludeScheduleId
    );

    /**
     * 统计部门排班数据
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Map<String, Object> selectDepartmentScheduleStats(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计员工排班数据
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Map<String, Object> selectEmployeeScheduleStats(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 批量插入排班
     *
     * @param schedules 排班列表
     * @return 插入行数
     */
    int batchInsert(@Param("schedules") List<AttendanceScheduleEntity> schedules);

    /**
     * 批量更新排班
     *
     * @param schedules 排班列表
     * @return 更新行数
     */
    int batchUpdate(@Param("schedules") List<AttendanceScheduleEntity> schedules);

    /**
     * 批量删除排班
     *
     * @param scheduleIds 排班ID列表
     * @return 删除行数
     */
    int batchDelete(@Param("scheduleIds") List<Long> scheduleIds);

    /**
     * 按日期范围删除排班
     *
     * @param employeeId 员工ID（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 删除行数
     */
    int deleteByDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询未分配考勤记录的日期
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 未分配排班的日期列表
     */
    List<LocalDate> selectUnscheduledDates(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询排班汇总报表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dimension 统计维度：department/employee/shift/location
     * @param departmentId 部门ID（可选）
     * @return 汇总报表数据
     */
    List<Map<String, Object>> selectScheduleReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dimension") String dimension,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询班次使用统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 班次使用统计
     */
    List<Map<String, Object>> selectShiftUsageStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询工作地点使用统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 工作地点使用统计
     */
    List<Map<String, Object>> selectLocationUsageStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询排班类型统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 排班类型统计
     */
    List<Map<String, Object>> selectScheduleTypeStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 查询主管排班统计
     *
     * @param supervisorId 主管ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 主管排班统计
     */
    Map<String, Object> selectSupervisorScheduleStats(
            @Param("supervisorId") Long supervisorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询需要提醒的排班
     *
     * @param reminderDate 提醒日期
     * @param daysBefore 提前天数
     * @return 需要提醒的排班列表
     */
    List<Map<String, Object>> selectReminderSchedules(
            @Param("reminderDate") LocalDate reminderDate,
            @Param("daysBefore") Integer daysBefore
    );

    /**
     * 查询排班异常
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param departmentId 部门ID（可选）
     * @return 排班异常列表
     */
    List<Map<String, Object>> selectScheduleAnomalies(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId
    );

    /**
     * 清理过期排班
     *
     * @param beforeDate 清理日期
     * @return 清理行数
     */
    int cleanExpiredSchedules(@Param("beforeDate") LocalDate beforeDate);

    /**
     * 复制排班到新日期范围
     *
     * @param sourceStartDate 源开始日期
     * @param sourceEndDate 源结束日期
     * @param targetStartDate 目标开始日期
     * @param targetEndDate 目标结束日期
     * @param employeeIds 员工ID列表（可选）
     * @return 复制的排班数量
     */
    int copySchedulesToDateRange(
            @Param("sourceStartDate") LocalDate sourceStartDate,
            @Param("sourceEndDate") LocalDate sourceEndDate,
            @Param("targetStartDate") LocalDate targetStartDate,
            @Param("targetEndDate") LocalDate targetEndDate,
            @Param("employeeIds") List<Long> employeeIds
    );

    /**
     * 获取排班日历数据
     *
     * @param year 年份
     * @param month 月份
     * @param departmentId 部门ID（可选）
     * @return 日历数据
     */
    List<Map<String, Object>> selectScheduleCalendar(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("departmentId") Long departmentId
    );
}