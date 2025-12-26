package net.lab1024.sa.consume.domain.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 消费分析Excel导出模型
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@HeadRowHeight(20)
@ContentRowHeight(18)
public class ConsumeAnalysisExcelModel {

    @ExcelProperty("分类名称")
    @ColumnWidth(20)
    private String categoryName;

    @ExcelProperty("消费金额")
    @ColumnWidth(15)
    private BigDecimal amount;

    @ExcelProperty("消费次数")
    @ColumnWidth(15)
    private Integer count;

    @ExcelProperty("占比")
    @ColumnWidth(10)
    private String percent;

    @ExcelProperty("平均每单")
    @ColumnWidth(15)
    private BigDecimal averageAmount;
}
