package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 优化统计信息
 * <p>
 * 封装优化结果的统计信息
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
public class OptimizationStatistics {

    /**
     * 总优化次数
     */
    private Integer totalOptimizations;

    /**
     * 成功优化次数
     */
    private Integer successfulOptimizations;

    /**
     * 失败优化次数
     */
    private Integer failedOptimizations;

    /**
     * 平均质量评分
     */
    private Double averageQualityScore;

    /**
     * 平均改进评分
     */
    private Double averageImprovementScore;

    /**
     * 优化类型统计
     */
    private Map<String, Integer> optimizationTypeStatistics;

    /**
     * 优化次数
     */
    private Integer optimizationCount;

    /**
     * 平均优化评分
     */
    private Double averageScore;

    /**
     * 优化成功率
     */
    private Double successRate;

    /**
     * 各类型优化统计
     */
    private Map<String, Integer> typeStatistics;

    /**
     * 优化耗时统计
     */
    private Map<String, Long> durationStatistics;
}
