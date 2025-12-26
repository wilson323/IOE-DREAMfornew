package net.lab1024.sa.attendance.engine.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OptimizationConfig {
    private Long planId;
    private List<Long> employeeIds;
    private List<Long> shiftIds;
    private LocalDate startDate;
    private LocalDate endDate;
    @Builder.Default
    private Integer optimizationGoal = 5;
    @Builder.Default
    private Integer maxConsecutiveWorkDays = 7;
    @Builder.Default
    private Integer minRestDays = 2;
    @Builder.Default
    private Integer minDailyStaff = 2;
    @Builder.Default
    private Double fairnessWeight = 0.4;
    @Builder.Default
    private Double costWeight = 0.3;
    @Builder.Default
    private Double efficiencyWeight = 0.2;
    @Builder.Default
    private Double satisfactionWeight = 0.1;
    @Builder.Default
    private Integer algorithmType = 4;
    @Builder.Default
    private Integer populationSize = 20;
    @Builder.Default
    private Integer maxGenerations = 50;
    @Builder.Default
    private Double crossoverRate = 0.8;
    @Builder.Default
    private Double mutationRate = 0.1;
    @Builder.Default
    private Double initialTemperature = 1000.0;
    @Builder.Default
    private Double coolingRate = 0.95;

    // ==================== 成本参数 ====================

    @Builder.Default
    private Double overtimeCostPerShift = 100.0;

    @Builder.Default
    private Double weekendCostPerShift = 150.0;

    @Builder.Default
    private Double holidayCostPerShift = 200.0;

    // ==================== 遗传算法高级参数 ====================

    @Builder.Default
    private Double selectionRate = 0.5;

    @Builder.Default
    private Double elitismRate = 0.1;

    public void validate() {
        if (employeeIds == null || employeeIds.isEmpty()) {
            throw new IllegalArgumentException("员工ID列表不能为空");
        }
        if (shiftIds == null || shiftIds.isEmpty()) {
            throw new IllegalArgumentException("班次ID列表不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }

    // ==================== 便捷计算方法 ====================

    /**
     * 获取员工数量
     */
    public int getEmployeeCount() {
        return employeeIds != null ? employeeIds.size() : 0;
    }

    /**
     * 获取排班天数
     */
    public long getPeriodDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * 获取最大迭代次数（别名方法）
     */
    public int getMaxIterations() {
        return maxGenerations != null ? maxGenerations : 50;
    }

    /**
     * 获取班次数量
     */
    public int getShiftCount() {
        return shiftIds != null ? shiftIds.size() : 0;
    }
}
