package net.lab1024.sa.attendance.domain.vo.smartSchedule;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能排班计划详情VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SmartSchedulePlanDetailVO extends SmartSchedulePlanVO {

    /**
     * 员工ID列表
     */
    private List<Long> employeeIds;

    /**
     * 班次ID列表
     */
    private List<Long> shiftIds;

    /**
     * 最大连续工作天数
     */
    private Integer maxConsecutiveWorkDays;

    /**
     * 最小休息天数
     */
    private Integer minRestDays;

    /**
     * 每日最少人员数
     */
    private Integer minDailyStaff;

    /**
     * 公平性权重
     */
    private Double fairnessWeight;

    /**
     * 成本权重
     */
    private Double costWeight;

    /**
     * 效率权重
     */
    private Double efficiencyWeight;

    /**
     * 满意度权重
     */
    private Double satisfactionWeight;

    /**
     * 种群大小
     */
    private Integer populationSize;

    /**
     * 最大迭代次数
     */
    private Integer maxGenerations;

    /**
     * 交叉率
     */
    private Double crossoverRate;

    /**
     * 变异率
     */
    private Double mutationRate;

    /**
     * 优化结果摘要
     */
    private SmartScheduleResultVO.OptimizationResultSummary optimizationResult;
}
