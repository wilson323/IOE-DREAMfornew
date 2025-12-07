package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 消费对比分析数据VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeComparisonVO {

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 当前期消费金额
     */
    private BigDecimal currentPeriodAmount;

    /**
     * 对比期消费金额
     */
    private BigDecimal comparePeriodAmount;

    /**
     * 增长金额
     */
    private BigDecimal growthAmount;

    /**
     * 增长率（百分比）
     */
    private BigDecimal growthRate;

    /**
     * 当前期消费笔数
     */
    private Integer currentPeriodCount;

    /**
     * 对比期消费笔数
     */
    private Integer comparePeriodCount;

    /**
     * 增长笔数
     */
    private Integer growthCount;

    /**
     * 对比数据集
     */
    private List<ComparisonDataset> datasets;

    /**
     * 对比分析
     */
    private ComparisonAnalysis analysis;

    /**
     * 对比类型
     */
    private String comparisonType;

    /**
     * 对比数据集
     */
    @Data
    public static class ComparisonDataset {
        /**
         * 数据集名称
         */
        private String name;

        /**
         * 标识符
         */
        private String identifier;

        /**
         * 消费金额
         */
        private BigDecimal amount;

        /**
         * 消费笔数
         */
        private Long count;

        /**
         * 平均消费金额
         */
        private BigDecimal averageAmount;

        /**
         * 占比（百分比）
         */
        private BigDecimal percentage;
    }

    /**
     * 对比分析
     */
    @Data
    public static class ComparisonAnalysis {
        /**
         * 增长趋势
         */
        private String growthTrend;

        /**
         * 增长幅度
         */
        private BigDecimal growthRate;

        /**
         * 总差异
         */
        private BigDecimal totalDifference;

        /**
         * 差异百分比
         */
        private BigDecimal differencePercentage;

        /**
         * 最大差异项
         */
        private String maxDifferenceItem;

        /**
         * 最小差异项
         */
        private String minDifferenceItem;
    }
}
