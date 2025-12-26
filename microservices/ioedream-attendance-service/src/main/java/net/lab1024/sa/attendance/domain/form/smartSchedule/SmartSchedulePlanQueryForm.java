package net.lab1024.sa.attendance.domain.form.smartSchedule;

import lombok.Data;

import java.time.LocalDate;

/**
 * 智能排班计划查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class SmartSchedulePlanQueryForm {

    /**
     * 计划名称（模糊查询）
     */
    private String planName;

    /**
     * 计划状态 (0-草稿 1-待优化 2-优化中 3-已完成 4-已确认 5-已取消 6-优化失败)
     */
    private Integer status;

    /**
     * 开始日期（范围查询）
     */
    private LocalDate startDateBegin;

    /**
     * 开始日期（范围查询）
     */
    private LocalDate startDateEnd;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 10;
}
