package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.form.ScheduleRecordForm;
import net.lab1024.sa.attendance.domain.form.ScheduleTemplateForm;
import net.lab1024.sa.attendance.domain.form.SmartSchedulingForm;
import net.lab1024.sa.attendance.domain.vo.ScheduleRecordVO;
import net.lab1024.sa.attendance.domain.vo.ScheduleTemplateVO;
import net.lab1024.sa.attendance.domain.vo.SchedulingResultVO;
import net.lab1024.sa.attendance.domain.vo.SchedulingStatisticsVO;
import net.lab1024.sa.attendance.manager.SmartSchedulingEngine;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ScheduleService {

    /**
     * 创建排班记录
     */
    void createScheduleRecord(ScheduleRecordForm form);

    /**
     * 批量创建排班记录
     */
    void batchCreateScheduleRecords(List<ScheduleRecordForm> forms);

    /**
     * 更新排班记录
     */
    void updateScheduleRecord(Long scheduleId, ScheduleRecordForm form);

    /**
     * 删除排班记录
     */
    void deleteScheduleRecord(Long scheduleId);

    /**
     * 获取员工排班列表
     */
    List<ScheduleRecordVO> getEmployeeSchedules(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取部门排班列表
     */
    List<ScheduleRecordVO> getDepartmentSchedules(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取排班日历
     */
    List<ScheduleRecordVO> getScheduleCalendar(Long departmentId, int year, int month);

    /**
     * 创建排班模板
     */
    Long createScheduleTemplate(ScheduleTemplateForm form);

    /**
     * 更新排班模板
     */
    void updateScheduleTemplate(Long templateId, ScheduleTemplateForm form);

    /**
     * 删除排班模板
     */
    void deleteScheduleTemplate(Long templateId);

    /**
     * 应用排班模板
     */
    List<ScheduleRecordVO> applyScheduleTemplate(Long templateId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取排班模板列表
     */
    List<ScheduleTemplateVO> getScheduleTemplates(String templateType, Long departmentId);

    /**
     * 生成智能排班
     */
    SchedulingResultVO generateSmartSchedule(SmartSchedulingForm form);

    /**
     * 优化排班方案
     */
    SchedulingResultVO optimizeSchedule(String requestId);

    /**
     * 预测排班需求
     */
    SchedulingStatisticsVO forecastDemand(Long departmentId, String forecastPeriod);

    /**
     * 获取排班统计信息
     */
    SchedulingStatisticsVO getSchedulingStatistics(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 冲突检测
     */
    List<String> detectScheduleConflicts(List<Long> employeeIds, LocalDate date);
}