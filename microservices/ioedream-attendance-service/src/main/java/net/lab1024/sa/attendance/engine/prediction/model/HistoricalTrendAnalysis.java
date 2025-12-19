package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 历史趋势分析（简化）
 *
 * <p>用于承载历史数据趋势分析的核心结果。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalTrendAnalysis {

    /**
     * 趋势方向：STABLE/UP/DOWN 等（简化为字符串）
     */
    private String trendDirection;

    /**
     * 增长率（0-1）
     */
    private Double growthRate;

    /**
     * 是否存在季节性模式
     */
    private Boolean seasonalPattern;
}

