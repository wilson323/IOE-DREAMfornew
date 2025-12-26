package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消费交易统计视图对象
 * <p>
 * 用于返回给前端显示的交易统计信息
 * 包含今日统计和历史统计数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费交易统计信息")
public class ConsumeTransactionStatisticsVO {

    /**
     * 统计时间
     */
    @Schema(description = "统计时间", example = "2025-12-21T15:30:00")
    private LocalDateTime statisticsTime;

    /**
     * 今日交易笔数
     */
    @Schema(description = "今日交易笔数", example = "156")
    private Long todayTransactionCount;

    /**
     * 今日交易总金额
     */
    @Schema(description = "今日交易总金额", example = "3125.50")
    private BigDecimal todayTotalAmount;

    /**
     * 今日平均交易金额
     */
    @Schema(description = "今日平均交易金额", example = "20.04")
    private BigDecimal todayAverageAmount;

    /**
     * 今日成功交易笔数
     */
    @Schema(description = "今日成功交易笔数", example = "150")
    private Long todaySuccessCount;

    /**
     * 今日失败交易笔数
     */
    @Schema(description = "今日失败交易笔数", example = "6")
    private Long todayFailureCount;

    /**
     * 今日成功率
     */
    @Schema(description = "今日成功率", example = "96.15")
    private BigDecimal todaySuccessRate;

    /**
     * 历史总交易笔数
     */
    @Schema(description = "历史总交易笔数", example = "45892")
    private Long totalTransactionCount;

    /**
     * 历史交易总金额
     */
    @Schema(description = "历史交易总金额", example = "892456.78")
    private BigDecimal totalTransactionAmount;

    /**
     * 历史平均交易金额
     */
    @Schema(description = "历史平均交易金额", example = "19.44")
    private BigDecimal totalAverageAmount;

    /**
     * 活跃用户数（今日有交易的用户）
     */
    @Schema(description = "活跃用户数", example = "89")
    private Long activeUserCount;

    /**
     * 活跃设备数（今日有交易的设备）
     */
    @Schema(description = "活跃设备数", example = "12")
    private Long activeDeviceCount;

    /**
     * 最大单笔交易金额（今日）
     */
    @Schema(description = "最大单笔交易金额", example = "150.00")
    private BigDecimal todayMaxAmount;

    /**
     * 最小单笔交易金额（今日）
     */
    @Schema(description = "最小单笔交易金额", example = "0.50")
    private BigDecimal todayMinAmount;

    /**
     * 按餐次统计
     */
    @Schema(description = "按餐次统计")
    private MealStatistics mealStatistics;

    /**
     * 按设备统计
     */
    @Schema(description = "按设备统计")
    private DeviceStatistics deviceStatistics;

    /**
     * 按小时统计
     */
    @Schema(description = "按小时统计")
    private HourlyStatistics hourlyStatistics;

    /**
     * 餐次统计数据
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "餐次统计数据")
    public static class MealStatistics {

        @Schema(description = "早餐交易笔数", example = "35")
        private Long breakfastCount;

        @Schema(description = "早餐交易金额", example = "525.00")
        private BigDecimal breakfastAmount;

        @Schema(description = "午餐交易笔数", example = "85")
        private Long lunchCount;

        @Schema(description = "午餐交易金额", example = "1700.00")
        private BigDecimal lunchAmount;

        @Schema(description = "晚餐交易笔数", example = "36")
        private Long dinnerCount;

        @Schema(description = "晚餐交易金额", example = "900.50")
        private BigDecimal dinnerAmount;
    }

    /**
     * 设备统计数据
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "设备统计数据")
    public static class DeviceStatistics {

        @Schema(description = "设备名称", example = "食堂POS机01")
        private String deviceName;

        @Schema(description = "交易笔数", example = "45")
        private Long transactionCount;

        @Schema(description = "交易金额", example = "890.50")
        private BigDecimal transactionAmount;

        @Schema(description = "设备状态", example = "ONLINE")
        private String deviceStatus;
    }

    /**
     * 按小时统计数据
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "按小时统计数据")
    public static class HourlyStatistics {

        @Schema(description = "小时", example = "12")
        private Integer hour;

        @Schema(description = "交易笔数", example = "28")
        private Long transactionCount;

        @Schema(description = "交易金额", example = "560.00")
        private BigDecimal transactionAmount;
    }
}