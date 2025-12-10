package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 性能评估
 * <p>
 * JVM性能的综合评估结果
 * 包含性能评分、告警、建议等信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class PerformanceAssessment {

    /**
     * 评估时间
     */
    private LocalDateTime assessmentTime;

    /**
     * 性能概览
     */
    private JvmPerformanceOverview overview;

    /**
     * 性能评分（0-100）
     */
    private double score;

    /**
     * 性能告警列表
     */
    private List<PerformanceAlert> alerts;

    /**
     * 优化建议列表
     */
    private List<String> recommendations;
}