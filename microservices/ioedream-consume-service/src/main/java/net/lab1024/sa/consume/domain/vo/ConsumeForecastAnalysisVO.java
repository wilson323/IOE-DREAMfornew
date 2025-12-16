package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 消费预测分析数据VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeForecastAnalysisVO {

    /**
     * 预测周期
     * <p>
     * NEXT_WEEK-下周
     * NEXT_MONTH-下月
     * NEXT_QUARTER-下季度
     * </p>
     */
    private String forecastPeriod;

    /**
     * 预测消费金额
     */
    private BigDecimal forecastAmount;

    /**
     * 预测消费笔数
     */
    private Integer forecastCount;

    /**
     * 预测置信度（0-100）
     */
    private Integer confidence;

    /**
     * 预测趋势
     * <p>
     * UP-上升
     * DOWN-下降
     * STABLE-稳定
     * </p>
     */
    private String forecastTrend;

    /**
     * 预测类型
     */
    private String forecastType;

    /**
     * 预测数据点列表
     */
    private List<ForecastDataPoint> forecastDataPoints;

    /**
     * 预测数据
     */
    private List<ForecastDataPoint> forecastData;

    /**
     * 预测模型信息
     */
    private ForecastModelInfo modelInfo;

    /**
     * 预测准确度
     */
    private ForecastAccuracy accuracy;

    /**
     * 预测数据点
     */
    @Data
    public static class ForecastDataPoint {
        /**
         * 时间点（字符串格式）
         */
        private String timePoint;

        /**
         * 预测金额
         */
        private BigDecimal predictedAmount;

        /**
         * 预测笔数
         */
        private Long predictedCount;

        /**
         * 置信区间下限
         */
        private BigDecimal confidenceLower;

        /**
         * 置信区间上限
         */
        private BigDecimal confidenceUpper;
    }

    /**
     * 预测模型信息
     */
    @Data
    public static class ForecastModelInfo {
        /**
         * 模型名称
         */
        private String modelName;

        /**
         * 模型版本
         */
        private String modelVersion;

        /**
         * 训练周期
         */
        private String trainingPeriod;
    }

    /**
     * 预测准确度
     */
    @Data
    public static class ForecastAccuracy {
        /**
         * 准确度（0-100）
         */
        private Integer accuracy;

        /**
         * 误差率
         */
        private BigDecimal errorRate;

        /**
         * 准确度评级
         */
        private String accuracyRating;
    }
}



