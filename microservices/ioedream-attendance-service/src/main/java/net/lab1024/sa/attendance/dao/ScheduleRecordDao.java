package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.domain.entity.ScheduleRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班记录数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ScheduleRecordDao extends BaseMapper<ScheduleRecordEntity> {

    /**
     * 根据员工ID查询排班记录
     */
    @Select("SELECT * FROM t_attendance_schedule_record WHERE employee_id = #{employeeId} " +
            "AND deleted_flag = 0 ORDER BY schedule_date DESC")
    List<ScheduleRecordEntity> selectByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 根据员工ID和日期范围查询排班记录
     */
    @Select("SELECT * FROM t_attendance_schedule_record WHERE employee_id = #{employeeId} " +
            "AND schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted_flag = 0 ORDER BY schedule_date ASC")
    List<ScheduleRecordEntity> selectByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据日期查询排班记录
     */
    @Select("SELECT * FROM t_attendance_schedule_record WHERE schedule_date = #{scheduleDate} " +
            "AND deleted_flag = 0 ORDER BY employee_id")
    List<ScheduleRecordEntity> selectByDate(@Param("scheduleDate") LocalDate scheduleDate);

    /**
     * 根据部门和日期范围查询排班记录
     */
    @Select("SELECT sr.* FROM t_attendance_schedule_record sr " +
            "JOIN t_common_employee e ON sr.employee_id = e.employee_id " +
            "WHERE e.department_id = #{departmentId} " +
            "AND sr.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND sr.deleted_flag = 0 AND e.deleted_flag = 0 " +
            "ORDER BY sr.schedule_date, sr.employee_id")
    List<ScheduleRecordEntity> selectByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 检查员工在指定时间段是否已有排班
     */
    @Select("SELECT COUNT(*) FROM t_attendance_schedule_record sr " +
            "JOIN t_attendance_work_shift ws ON sr.shift_id = ws.shift_id " +
            "WHERE sr.employee_id = #{employeeId} " +
            "AND sr.schedule_date = #{scheduleDate} " +
            "AND sr.status = 1 AND sr.deleted_flag = 0 " +
            "AND ws.deleted_flag = 0")
    int countEmployeeScheduleOnDate(@Param("employeeId") Long employeeId, @Param("scheduleDate") LocalDate scheduleDate);

    /**
     * 批量创建排班记录
     */
    @Update({
        "<script>",
        "INSERT INTO t_attendance_schedule_record ",
        "(employee_id, schedule_date, shift_id, schedule_type, is_temporary, reason, status, work_hours, create_user_id, create_time, update_time, deleted_flag) ",
        "VALUES ",
        "<foreach collection='records' item='record' separator=','>",
        "(#{record.employeeId}, #{record.scheduleDate}, #{record.shiftId}, #{record.scheduleType}, ",
        "#{record.isTemporary}, #{record.reason}, #{record.status}, #{record.workHours}, ",
        "#{record.createUserId}, NOW(), NOW(), 0)",
        "</foreach>",
        "</script>"
    })
    int batchInsert(@Param("records") List<ScheduleRecordEntity> records);

    /**
     * 更新排班状态
     */
    @Update("UPDATE t_attendance_schedule_record SET status = #{status}, update_time = NOW(), update_user_id = #{userId} " +
            "WHERE schedule_id = #{scheduleId}")
    int updateStatus(@Param("scheduleId") Long scheduleId, @Param("status") Integer status, @Param("userId") Long userId);

    /**
     * 删除指定日期的排班记录
     */
    @Update("UPDATE t_attendance_schedule_record SET deleted_flag = 1, update_time = NOW() " +
            "WHERE employee_id = #{employeeId} AND schedule_date = #{scheduleDate}")
    int deleteByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("scheduleDate") LocalDate scheduleDate);

    /**
     * 查询员工月度排班统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_days, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as work_days, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as completed_days, " +
            "SUM(work_hours) as total_hours " +
            "FROM t_attendance_schedule_record " +
            "WHERE employee_id = #{employeeId} " +
            "AND schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted_flag = 0")
    java.util.Map<String, Object> selectMonthlyStats(@Param("employeeId") Long employeeId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * 查询部门排班统计
     */
    @Select("SELECT " +
            "COUNT(DISTINCT sr.employee_id) as employee_count, " +
            "COUNT(*) as total_schedules, " +
            "SUM(CASE WHEN sr.status = 1 THEN 1 ELSE 0 END) as active_schedules, " +
            "SUM(sr.work_hours) as total_hours " +
            "FROM t_attendance_schedule_record sr " +
            "JOIN t_common_employee e ON sr.employee_id = e.employee_id " +
            "WHERE e.department_id = #{departmentId} " +
            "AND sr.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND sr.deleted_flag = 0 AND e.deleted_flag = 0")
    java.util.Map<String, Object> selectDepartmentStats(@Param("departmentId") Long departmentId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}