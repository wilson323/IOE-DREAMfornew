package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 消费趋势数据VO
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-12-24
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */
@Data
@Schema(description = "消费趋势数据")
public class ConsumptionTrendVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "消费金额")
    private BigDecimal amount;

    @Schema(description = "消费次数")
    private Integer count;

    @Schema(description = "日期标签（用于图表显示）")
    private String dateLabel;
}
