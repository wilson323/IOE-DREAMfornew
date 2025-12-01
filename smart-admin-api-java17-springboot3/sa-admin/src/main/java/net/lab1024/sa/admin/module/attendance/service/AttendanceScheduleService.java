package net.lab1024.sa.admin.module.attendance.service;

import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤排班服务接口
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
public interface AttendanceScheduleService {

    /**
     * 获取员工指定日期范围的排班
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    List<AttendanceScheduleEntity> getEmployeeSchedule(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取部门指定日期范围的排班
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    List<AttendanceScheduleEntity> getDepartmentSchedule(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 保存或更新排班
     *
     * @param schedule 排班
     * @return 是否成功
     */
    boolean saveOrUpdateSchedule(AttendanceScheduleEntity schedule);

    /**
     * 删除排班
     *
     * @param scheduleId 排班ID
     * @return 是否成功
     */
    boolean deleteSchedule(Long scheduleId);

    /**
     * 批量删除排班
     *
     * @param scheduleIds 排班ID列表
     * @return 删除数量
     */
    int batchDeleteSchedules(List<Long> scheduleIds);

    /**
     * 检查指定日期是否有排班
     *
     * @param employeeId 员工ID
     * @param date 日期
     * @return 是否有排班
     */
    boolean hasSchedule(Long employeeId, LocalDate date);

    /**
     * 获取员工指定日期的排班
     *
     * @param employeeId 员工ID
     * @param date 日期
     * @return 排班
     */
    AttendanceScheduleEntity getEmployeeScheduleByDate(Long employeeId, LocalDate date);
}