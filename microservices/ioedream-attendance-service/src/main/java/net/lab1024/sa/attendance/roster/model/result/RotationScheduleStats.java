package net.lab1024.sa.attendance.roster.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 轮班统计信息
 * <p>
 * 用于统计轮班安排的相关数据
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
public class RotationScheduleStats {

    /**
     * 总天数
     */
    private Integer totalDays;

    /**
     * 工作日天数
     */
    private Integer workDays;

    /**
     * 休息日天数
     */
    private Integer restDays;

    /**
     * 班次类型分布统计
     */
    private Map<net.lab1024.sa.attendance.roster.model.RotationSystemConfig.ShiftType, Long> shiftTypeDistribution;

    /**
     * 加班统计
     */
    private Integer overtimeDays;

    /**
     * 夜班统计
     */
    private Integer nightShiftDays;

    /**
     * 周末统计
     */
    private Integer weekendDays;

    /**
     * 统计时间
     */
    private java.time.LocalDateTime statisticsTime;
}