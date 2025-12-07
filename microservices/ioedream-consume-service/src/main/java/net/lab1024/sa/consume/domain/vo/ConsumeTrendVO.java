package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 消费趋势数据VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeTrendVO {

    /**
     * 趋势数据点列表
     */
    private List<TrendDataPoint> dataPoints;

    /**
     * 时间类型
     * <p>
     * DAILY-按天
     * WEEKLY-按周
     * MONTHLY-按月
     * </p>
     */
    private String timeType;

    /**
     * 趋势方向
     * <p>
     * UP-上升
     * DOWN-下降
     * STABLE-稳定
     * </p>
     */
    private String trendDirection;

    /**
     * 趋势分析
     */
    private TrendAnalysis analysis;

    /**
     * 趋势分析
     */
    @Data
    public static class TrendAnalysis {
        /**
         * 趋势方向
         */
        private String direction;

        /**
         * 变化幅度
         */
        private BigDecimal changeRate;

        /**
         * 增长率
         */
        private BigDecimal growthRate;
    }

    /**
     * 趋势数据点
     */
    @Data
    public static class TrendDataPoint {
        /**
         * 时间标签
         */
        private String timeLabel;

        /**
         * 时间点（日期）
         */
        private String timePoint;

        /**
         * 消费金额
         */
        private BigDecimal amount;

        /**
         * 消费笔数
         */
        private Long count;
    }
}
