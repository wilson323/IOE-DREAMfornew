package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费记录添加表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费记录添加表单")
public class ConsumeRecordAddForm {

    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String userName;

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "POS001")
    private String deviceId;

    @Schema(description = "设备名称", example = "一楼餐厅POS机")
    private String deviceName;

    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID", example = "2001")
    private Long merchantId;

    @Schema(description = "商户名称", example = "食堂餐饮")
    private String merchantName;

    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    @Schema(description = "消费金额", example = "25.50")
    private BigDecimal amount;

    @Schema(description = "原始金额", example = "30.00")
    private BigDecimal originalAmount;

    @Schema(description = "折扣金额", example = "4.50")
    private BigDecimal discountAmount;

    @NotBlank(message = "消费类型不能为空")
    @Schema(description = "消费类型", example = "MEAL", allowableValues = {"MEAL", "SNACK", "DRINK", "GROCERY"})
    private String consumeType;

    @Schema(description = "消费类型名称", example = "餐饮")
    private String consumeTypeName;

    @Schema(description = "商品明细")
    private String productDetail;

    @Schema(description = "支付方式", example = "BALANCE", allowableValues = {"BALANCE", "CARD", "CASH", "WECHAT", "ALIPAY"})
    private String paymentMethod;

    @Schema(description = "订单号", example = "ORDER202512210001")
    private String orderNo;

    @Schema(description = "交易流水号", example = "TXN202512210001")
    private String transactionNo;

    @Schema(description = "消费时间", example = "2025-12-23T12:00:00")
    private LocalDateTime consumeTime;

    @Schema(description = "消费地点", example = "一楼餐厅")
    private String consumeLocation;

    @Schema(description = "备注", example = "午餐")
    private String remark;

    @Schema(description = "是否离线消费", example = "false")
    private Integer offlineFlag;
}