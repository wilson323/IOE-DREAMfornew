package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 员工偏好
 * <p>
 * 定义员工的排班偏好设置
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePreference {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 偏好工作日
     */
    private List<DayOfWeek> preferredDays;

    /**
     * 偏好工作时间
     */
    private Map<DayOfWeek, LocalTime> preferredStartTimes;

    /**
     * 偏好工作时间
     */
    private Map<DayOfWeek, LocalTime> preferredEndTimes;

    /**
     * 不偏好工作日
     */
    private List<DayOfWeek> dispreferredDays;

    /**
     * 偏好班次类型
     */
    private List<String> preferredShiftTypes;

    /**
     * 偏好权重
     */
    private Double preferenceWeight;
}
