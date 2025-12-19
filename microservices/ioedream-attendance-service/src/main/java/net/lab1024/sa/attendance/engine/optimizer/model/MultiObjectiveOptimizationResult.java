package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.util.List;
import java.util.Map;

/**
 * 多目标优化结果
 * <p>
 * 封装多目标优化的结果
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
public class MultiObjectiveOptimizationResult {

    /**
     * 优化后的排班记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 各目标达成情况
     */
    private Map<String, Double> objectiveAchievements;

    /**
     * 综合评分
     */
    private Double compositeScore;

    /**
     * 帕累托前沿解
     */
    private List<OptimizationResult> paretoFront;
}
