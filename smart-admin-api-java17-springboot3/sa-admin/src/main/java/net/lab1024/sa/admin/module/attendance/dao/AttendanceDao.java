package net.lab1024.sa.admin.module.attendance.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;

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
     * 查询员工今日考勤记录（兼容性方法）
     *
     * @param employeeId 员工ID
     * @param date       日期
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> selectEmployeeTodayRecord(@Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);

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
     * 查询部门指定日期的考勤记录
     *
     * @param departmentId 部门ID
     * @param date         日期
     * @return 考勤记录列表
     */
    List<AttendanceRecordEntity> selectDepartmentRecordsByDate(@Param("departmentId") Long departmentId,
            @Param("date") LocalDate date);

    // ==================== 补充缺失的方法（修复编译错误） ====================

    /**
     * 查询员工今日考勤记录（单个记录版本）
     * 兼容性方法，返回单个记录
     *
     * @param employeeId 员工ID
     * @param date       日期
     * @return 考勤记录
     */
    AttendanceRecordEntity selectEmployeeTodayRecordSingle(@Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);

    /**
     * 保存考勤记录
     * 兼容性方法
     *
     * @param entity 考勤记录实体
     * @return 保存结果
     */
    int save(AttendanceRecordEntity entity);
}
