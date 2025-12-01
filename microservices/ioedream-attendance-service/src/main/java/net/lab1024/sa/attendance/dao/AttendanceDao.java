package net.lab1024.sa.attendance.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;

/**
 * 考勤记录 DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface AttendanceDao extends BaseMapper<AttendanceRecordEntity> {

    /**
     * 查询今日考勤记录
     *
     * @param employeeId 员工ID
     * @param dateTime   日期时间
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> selectTodayRecord(@Param("employeeId") Long employeeId,
            @Param("dateTime") LocalDateTime dateTime);

    /**
     * 按日期范围查询考勤记录
     *
     * @param employeeId 员工ID（可为null）
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> selectByDateRange(@Param("employeeId") Long employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询员工统计数据
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 统计数据Map
     */
    Map<String, Object> selectEmployeeStatistics(@Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 检查员工今日是否已打卡
     *
     * @param employeeId 员工ID
     * @return 是否已打卡
     */
    Boolean hasEmployeeTodayPunched(@Param("employeeId") Long employeeId);

    /**
     * 查询员工最后一条考勤记录
     *
     * @param employeeId 员工ID
     * @return 考勤记录
     */
    AttendanceRecordEntity selectLastRecord(@Param("employeeId") Long employeeId);

    /**
     * 查询异常考勤记录
     *
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param employeeId    员工ID（可选）
     * @param exceptionType 异常类型（可选）
     * @return 异常考勤记录列表
     */
    List<AttendanceRecordEntity> selectExceptionRecords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("employeeId") Long employeeId,
            @Param("exceptionType") String exceptionType);

    /**
     * 查询员工月度考勤记录
     *
     * @param employeeId 员工ID
     * @param yearMonth  年月份（格式：yyyy-MM）
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> selectEmployeeMonthlyRecords(
            @Param("employeeId") Long employeeId,
            @Param("yearMonth") String yearMonth);

    /**
     * 查询员工今日考勤记录
     *
     * @param employeeId 员工ID
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     */
    AttendanceRecordEntity selectEmployeeTodayRecord(
            @Param("employeeId") Long employeeId,
            @Param("attendanceDate") LocalDate attendanceDate);

    /**
     * 保存考勤记录
     *
     * @param record 考勤记录实体
     * @return 影响行数
     */
    int save(AttendanceRecordEntity record);
}
