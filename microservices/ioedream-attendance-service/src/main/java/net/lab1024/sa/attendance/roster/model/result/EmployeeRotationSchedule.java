package net.lab1024.sa.attendance.roster.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 员工轮班安排
 * <p>
 * 封装员工在指定时间范围内的轮班安排
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
public class EmployeeRotationSchedule {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 轮班安排列表
     */
    private List<net.lab1024.sa.attendance.roster.model.RotationSchedule> schedules;

    /**
     * 统计信息
     */
    private RotationScheduleStats stats;

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 轮班统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RotationScheduleStats {
        /**
         * 总天数
         */
        private Integer totalDays;

        /**
         * 工作天数
         */
        private Integer workDays;

        /**
         * 休息天数
         */
        private Integer restDays;

        /**
         * 班次类型分布
         */
        private Map<net.lab1024.sa.attendance.roster.model.RotationSystemConfig.ShiftType, Long> shiftTypeDistribution;

        /**
         * 总工作时长（小时）
         */
        private Double totalWorkHours;

        /**
         * 平均每日工作时长（小时）
         */
        private Double averageDailyWorkHours;

        /**
         * 夜班次数
         */
        private Integer nightShiftCount;

        /**
         * 跨天班次数
         */
        private Integer overnightShiftCount;

        /**
         * 周末班次数
         */
        private Integer weekendShiftCount;

        /**
         * 节假日班次数
         */
        private Integer holidayShiftCount;
    }
}
