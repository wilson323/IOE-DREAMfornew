package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 消费数据分析结果VO
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-12-24
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */
@Data
@Schema(description = "消费数据分析结果")
public class ConsumptionAnalysisVO {

    @Schema(description = "总消费金额")
    private BigDecimal totalAmount;

    @Schema(description = "消费次数")
    private Integer totalCount;

    @Schema(description = "日均消费")
    private BigDecimal dailyAverage;

    @Schema(description = "消费天数")
    private Integer consumeDays;

    @Schema(description = "平均单笔消费")
    private BigDecimal averagePerOrder;

    @Schema(description = "分类消费列表")
    private List<CategoryConsumptionVO> categories;

    @Schema(description = "趋势数据（近7天）")
    private List<BigDecimal> trend;

    @Schema(description = "最常消费时段")
    private String mostFrequentTime;

    @Schema(description = "最喜欢品类")
    private String favoriteCategory;

    /**
     * 分类消费VO
     */
    @Data
    @Schema(description = "分类消费")
    public static class CategoryConsumptionVO {

        @Schema(description = "分类名称")
        private String name;

        @Schema(description = "消费金额")
        private BigDecimal amount;

        @Schema(description = "占比（百分比）")
        private Integer percent;

        @Schema(description = "分类图标")
        private String icon;
    }
}
