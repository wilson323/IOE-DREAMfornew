package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 冲突解决统计信息
 * <p>
 * 封装冲突解决的统计信息
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
public class ResolutionStatistics {

    /**
     * 总冲突数量
     */
    private Integer totalConflicts;

    /**
     * 总解决次数（用于兼容实现层字段名）
     */
    private Integer totalResolutions;

    /**
     * 成功解决次数（用于兼容实现层字段名）
     */
    private Integer successfulResolutions;

    /**
     * 失败解决次数（用于兼容实现层字段名）
     */
    private Integer failedResolutions;

    /**
     * 已解决数量
     */
    private Integer resolvedCount;

    /**
     * 未解决数量
     */
    private Integer unresolvedCount;

    /**
     * 自动解决数量
     */
    private Integer autoResolvedCount;

    /**
     * 手动解决数量
     */
    private Integer manualResolvedCount;

    /**
     * 按冲突类型统计
     */
    private Map<String, Integer> conflictsByType;

    /**
     * 按严重程度统计
     */
    private Map<Integer, Integer> conflictsBySeverity;

    /**
     * 解决成功率
     */
    private Double resolutionSuccessRate;

    /**
     * 平均解决时间（毫秒）
     */
    private Double averageResolutionTime;

    /**
     * 解决策略使用统计
     */
    private Map<String, Integer> strategyUsageCount;

    /**
     * 解决类型统计（用于兼容实现层字段名）
     */
    @Builder.Default
    private Map<String, Integer> resolutionTypeStatistics = new HashMap<>();

    /**
     * 平均质量得分（0-1）
     */
    private Double averageQualityScore;
}

