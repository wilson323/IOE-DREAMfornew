package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 消费分类统计VO
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-12-24
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */
@Data
@Schema(description = "消费分类统计")
public class CategoryStatsVO {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "消费金额")
    private BigDecimal amount;

    @Schema(description = "消费次数")
    private Integer count;

    @Schema(description = "占比（百分比）")
    private BigDecimal percent;

    @Schema(description = "分类图标")
    private String icon;

    @Schema(description = "排序序号")
    private Integer sortFlag;
}
