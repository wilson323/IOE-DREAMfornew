package net.lab1024.sa.consume.domain.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费记录Excel导出模型
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@HeadRowHeight(20)
@ContentRowHeight(18)
public class ConsumeRecordExcelModel {

    @ExcelProperty("消费时间")
    @ColumnWidth(20)
    private LocalDateTime consumeTime;

    @ExcelProperty("商户名称")
    @ColumnWidth(25)
    private String merchantName;

    @ExcelProperty("分类")
    @ColumnWidth(15)
    private String consumeTypeName;

    @ExcelProperty("消费金额")
    @ColumnWidth(15)
    private BigDecimal amount;

    @ExcelProperty("原价")
    @ColumnWidth(15)
    private BigDecimal originalAmount;

    @ExcelProperty("优惠")
    @ColumnWidth(15)
    private BigDecimal discountAmount;

    @ExcelProperty("支付方式")
    @ColumnWidth(15)
    private String paymentMethod;

    @ExcelProperty("设备名称")
    @ColumnWidth(20)
    private String deviceName;

    @ExcelProperty("订单号")
    @ColumnWidth(30)
    private String orderNo;
}
