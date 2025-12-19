package net.lab1024.sa.attendance.engine.optimizer.model;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 满意度优化结果
 * <p>
 * 封装员工满意度优化的结果 严格遵循CLAUDE.md全局架构规范
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
public class SatisfactionOptimizationResult {

    /**
     * 优化ID
     */
    private String optimizationId;

    /**
     * 员工偏好
     */
    private Map<Long, EmployeePreference> employeePreferences;

    /**
     * 原始满意度分数
     */
    private Double originalSatisfactionScore;

    /**
     * 优化后的排班记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 优化后满意度分数
     */
    private Double optimizedSatisfactionScore;

    /**
     * 满意度改进
     */
    private Double satisfactionImprovement;

    /**
     * 平均满意度
     */
    private Double averageSatisfaction;

    /**
     * 满意度改进率
     */
    private Double improvementRate;

    /**
     * 员工满意度明细
     */
    private Map<Long, Double> employeeSatisfactionScores;

    /**
     * 优化是否成功
     */
    private boolean optimizationSuccessful;

    /**
     * 优化指标
     */
    private Map<String, Object> optimizedMetrics;

    public boolean isOptimizationSuccessful () {
        return optimizationSuccessful;
    }

    public Map<String, Object> getOptimizedMetrics () {
        return optimizedMetrics;
    }
}
