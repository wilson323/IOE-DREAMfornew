package net.lab1024.sa.attendance.domain.form.smartSchedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 智能排班计划创建表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class SmartSchedulePlanAddForm {

    /**
     * 计划名称
     */
    @NotBlank(message = "计划名称不能为空")
    private String planName;

    /**
     * 计划描述
     */
    private String description;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /**
     * 员工ID列表
     */
    @NotNull(message = "员工ID列表不能为空")
    private List<Long> employeeIds;

    /**
     * 班次ID列表
     */
    @NotNull(message = "班次ID列表不能为空")
    private List<Long> shiftIds;

    /**
     * 优化目标 (1-公平性 2-成本 3-效率 4-满意度 5-综合)
     */
    @NotNull(message = "优化目标不能为空")
    private Integer optimizationGoal;

    /**
     * 最大连续工作天数
     */
    @Min(value = 1, message = "最大连续工作天数至少为1天")
    @Max(value = 14, message = "最大连续工作天数不能超过14天")
    private Integer maxConsecutiveWorkDays = 7;

    /**
     * 最小休息天数
     */
    @Min(value = 1, message = "最小休息天数至少为1天")
    @Max(value = 7, message = "最小休息天数不能超过7天")
    private Integer minRestDays = 2;

    /**
     * 每日最少人员数
     */
    @Min(value = 1, message = "每日最少人员数至少为1人")
    private Integer minDailyStaff = 2;

    /**
     * 公平性权重 (0.0-1.0)
     */
    @Min(value = 0, message = "公平性权重不能为负数")
    @Max(value = 1, message = "公平性权重不能超过1")
    private Double fairnessWeight = 0.4;

    /**
     * 成本权重 (0.0-1.0)
     */
    @Min(value = 0, message = "成本权重不能为负数")
    @Max(value = 1, message = "成本权重不能超过1")
    private Double costWeight = 0.3;

    /**
     * 效率权重 (0.0-1.0)
     */
    @Min(value = 0, message = "效率权重不能为负数")
    @Max(value = 1, message = "效率权重不能超过1")
    private Double efficiencyWeight = 0.2;

    /**
     * 满意度权重 (0.0-1.0)
     */
    @Min(value = 0, message = "满意度权重不能为负数")
    @Max(value = 1, message = "满意度权重不能超过1")
    private Double satisfactionWeight = 0.1;

    /**
     * 算法类型 (1-遗传算法 2-模拟退火 3-混合算法 4-自动选择)
     */
    @NotNull(message = "算法类型不能为空")
    private Integer algorithmType = 4;

    /**
     * 种群大小（遗传算法参数）
     */
    @Min(value = 10, message = "种群大小至少为10")
    @Max(value = 200, message = "种群大小不能超过200")
    private Integer populationSize = 20;

    /**
     * 最大迭代次数
     */
    @Min(value = 10, message = "最大迭代次数至少为10")
    @Max(value = 1000, message = "最大迭代次数不能超过1000")
    private Integer maxGenerations = 50;

    /**
     * 交叉率 (0.0-1.0)
     */
    @Min(value = 0, message = "交叉率不能为负数")
    @Max(value = 1, message = "交叉率不能超过1")
    private Double crossoverRate = 0.8;

    /**
     * 变异率 (0.0-1.0)
     */
    @Min(value = 0, message = "变异率不能为负数")
    @Max(value = 1, message = "变异率不能超过1")
    private Double mutationRate = 0.1;
/**     * 排班周期（天）     */    @NotNull(message = "排班周期不能为空")    @Min(value = 1, message = "排班周期至少为1天")    private Integer periodDays;    /**     * 最小连续工作天数     */    @Min(value = 1, message = "最小连续工作天数至少为1天")    @Max(value = 14, message = "最小连续工作天数不能超过14天")    private Integer minConsecutiveWorkDays = 1;    /**     * 每日最多人员数     */    @Min(value = 1, message = "每日最多人员数至少为1人")    private Integer maxDailyStaff = 20;    /**     * 最大迭代次数（通用）     */    @Min(value = 10, message = "最大迭代次数至少为10")    @Max(value = 1000, message = "最大迭代次数不能超过1000")    private Integer maxIterations = 100;    /**     * 选择率（遗传算法参数）     */    @Min(value = 0, message = "选择率不能为负数")    @Max(value = 1, message = "选择率不能超过1")    private Double selectionRate = 0.5;    /**     * 精英保留率（遗传算法参数）     */    @Min(value = 0, message = "精英保留率不能为负数")    @Max(value = 1, message = "精英保留率不能超过1")    private Double elitismRate = 0.1;    /**     * 加班班次成本     */    @Min(value = 0, message = "加班成本不能为负数")    private Double overtimeCostPerShift = 100.0;    /**     * 周末班次成本     */    @Min(value = 0, message = "周末成本不能为负数")    private Double weekendCostPerShift = 150.0;    /**     * 节假日班次成本     */    @Min(value = 0, message = "节假日成本不能为负数")    private Double holidayCostPerShift = 200.0;
}
