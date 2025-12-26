package net.lab1024.sa.attendance.domain.vo.smartSchedule;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 智能排班结果VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class SmartScheduleResultVO {

    /**
     * 结果ID
     */
    private Long resultId;

    /**
     * 计划ID
     */
    private Long planId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 排班日期
     */
    private LocalDate shiftDate;

    /**
     * 班次ID
     */
    private Long shiftId;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 适应度得分 (0.0-1.0)
     */
    private BigDecimal fitnessScore;

    /**
     * 公平性得分
     */
    private BigDecimal fairnessScore;

    /**
     * 成本得分
     */
    private BigDecimal costScore;

    /**
     * 效率得分
     */
    private BigDecimal efficiencyScore;

    /**
     * 满意度得分
     */
    private BigDecimal satisfactionScore;

    /**
     * 是否冲突 (0-无冲突 1-有冲突)
     */
    private Integer hasConflict;

    /**
     * 冲突描述
     */
    private String conflictDescription;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 优化结果摘要内部类
     */
    @Data
    public static class OptimizationResultSummary {
        /**
         * 最优适应度
         */
        private BigDecimal bestFitness;

        /**
         * 平均适应度
         */
        private BigDecimal averageFitness;

        /**
         * 迭代次数
         */
        private Integer iterations;

        /**
         * 优化耗时（毫秒）
         */
        private Long duration;
    }
}
