package net.lab1024.sa.attendance.engine.optimizer.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多目标优化结果
 * <p>
 * 封装多目标优化的结果 严格遵循CLAUDE.md全局架构规范
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
public class MultiObjectiveOptimizationResult {

    /**
     * 优化ID
     */
    private String optimizationId;

    /**
     * 多目标优化配置
     */
    private MultiObjectiveGoals multiObjectiveGoals;

    /**
     * 优化后的排班记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 各目标达成情况
     */
    private Map<String, Double> objectiveAchievements;

    /**
     * 当前目标
     */
    private Map<String, Double> currentObjectives;

    /**
     * 优化后目标
     */
    private Map<String, Double> optimizedObjectives;

    /**
     * 目标达成情况
     */
    private Map<String, net.lab1024.sa.attendance.engine.optimizer.OptimizationResult.GoalAchievement> goalAchievements;

    /**
     * 综合评分
     */
    private Double compositeScore;

    /**
     * 整体改进评分
     */
    private Double overallImprovementScore;

    /**
     * 帕累托前沿解
     */
    private List<List<ScheduleRecord>> paretoFront;

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

    /**
     * 获取目标达成情况（兼容方法）
     */
    public Map<String, Double> getGoalAchievements () {
        // 将objectiveAchievements转换为Map<String, Double>类型
        if (objectiveAchievements == null) {
            return new HashMap<> ();
        }
        return objectiveAchievements;
    }
}

