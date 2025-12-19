package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.util.List;
import java.util.Map;

/**
 * 覆盖率优化结果
 * <p>
 * 封装班次覆盖率优化的结果
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
public class CoverageOptimizationResult {

    /**
     * 优化ID
     */
    private String optimizationId;

    /**
     * 覆盖率目标
     */
    private CoverageTargets coverageTargets;

    /**
     * 原始覆盖率
     */
    private Map<String, Double> originalCoverageRates;

    /**
     * 优化后覆盖率
     */
    private Map<String, Double> optimizedCoverageRates;

    /**
     * 覆盖率提升
     */
    private Double coverageImprovement;

    /**
     * 优化是否成功
     */
    private Boolean optimizationSuccessful;

    /**
     * 优化后的排班记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 覆盖率统计
     */
    private Map<String, Double> coverageStatistics;

    /**
     * 覆盖率达成率
     */
    private Double coverageAchievementRate;

    /**
     * 覆盖率改进率
     */
    private Double improvementRate;

    /**
     * 优化指标
     */
    private Map<String, Object> optimizedMetrics;

    public boolean isOptimizationSuccessful() {
        return Boolean.TRUE.equals(optimizationSuccessful);
    }

    public Map<String, Object> getOptimizedMetrics() {
        return optimizedMetrics;
    }
}
