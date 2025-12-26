package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

import net.lab1024.sa.common.domain.form.BaseQueryForm;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费记录查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费记录查询表单")
public class ConsumeRecordQueryForm extends BaseQueryForm {

    @Schema(description = "记录ID", example = "1001")
    private Long recordId;

    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "设备ID", example = "POS001")
    private String deviceId;

    @Schema(description = "商户ID", example = "2001")
    private Long merchantId;

    @Schema(description = "商户名称", example = "一楼餐厅")
    private String merchantName;

    @Schema(description = "消费类型", example = "MEAL")
    private String consumeType;

    @Schema(description = "支付方式", example = "BALANCE")
    private String paymentMethod;

    @Schema(description = "最小金额", example = "0.01")
    private BigDecimal minAmount;

    @Schema(description = "最大金额", example = "1000.00")
    private BigDecimal maxAmount;

    @Schema(description = "订单号", example = "ORDER202512210001")
    private String orderNo;

    @Schema(description = "交易流水号", example = "TXN202512210001")
    private String transactionNo;

    @Schema(description = "消费开始时间", example = "2025-12-21T00:00:00")
    private LocalDateTime consumeTimeStart;

    @Schema(description = "消费结束时间", example = "2025-12-21T23:59:59")
    private LocalDateTime consumeTimeEnd;

    @Schema(description = "关键词搜索", example = "午餐")
    private String keyword;
}