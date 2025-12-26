package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 消费习惯分析VO
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-12-24
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */
@Data
@Schema(description = "消费习惯分析")
public class ConsumptionHabitsVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "最常消费时段")
    private String mostFrequentTime;

    @Schema(description = "最常消费时段开始时间")
    private LocalTime frequentTimeStart;

    @Schema(description = "最常消费时段结束时间")
    private LocalTime frequentTimeEnd;

    @Schema(description = "最喜欢品类")
    private String favoriteCategory;

    @Schema(description = "最喜欢品类ID")
    private Long favoriteCategoryId;

    @Schema(description = "平均单笔消费")
    private BigDecimal averagePerOrder;

    @Schema(description = "消费总次数")
    private Integer totalCount;

    @Schema(description = "消费天数")
    private Integer consumeDays;

    @Schema(description = "平均每日消费次数")
    private BigDecimal averageDailyCount;

    @Schema(description = "最大单笔消费")
    private BigDecimal maxOrderAmount;

    @Schema(description = "最小单笔消费")
    private BigDecimal minOrderAmount;

    @Schema(description = "是否高频用户（消费次数>20）")
    private Boolean isHighFrequencyUser;

    @Schema(description = "是否高消费用户（平均单笔>50）")
    private Boolean isHighValueUser;
}
