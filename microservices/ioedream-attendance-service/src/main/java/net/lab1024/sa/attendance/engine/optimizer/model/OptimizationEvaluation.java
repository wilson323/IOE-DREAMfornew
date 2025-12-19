package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 优化评估结果
 * <p>
 * 封装优化效果的评估信息
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
public class OptimizationEvaluation {

    /**
     * 优化改进指标
     */
    private Map<String, Double> improvementMetrics;

    /**
     * 优化前后对比
     */
    private Map<String, Object> beforeAfterComparison;

    /**
     * 评估评分
     */
    private Double evaluationScore;

    /**
     * 评估结论
     */
    private String evaluationConclusion;
}
