package net.lab1024.sa.admin.module.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 支付请求DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/23
 */

@Schema(description = "支付请求参数")
public class PaymentRequestDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户ID", example = "10001")
    private Long accountId;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @Schema(description = "支付金额", example = "100.50")
    private BigDecimal amount;

    @NotBlank(message = "支付方式不能为空")
    @Schema(description = "支付方式", example = "WECHAT", allowableValues = {"WECHAT", "ALIPAY", "BALANCE"})
    private String payMethod;

    @Schema(description = "OpenID(微信支付使用)", example = "ox1234567890abcdef")
    private String openid;

    @Schema(description = "商品描述", example = "食堂午餐消费")
    private String description;

    @Schema(description = "商户订单号", example = "RC1694876400001234")
    private String outTradeNo;

    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备信息", example = "POS机001")
    private String deviceInfo;

    @Schema(description = "商品ID", example = "1001")
    private Long productId;

    @Schema(description = "商品编码", example = "P001")
    private String productCode;

    @Schema(description = "消费模式", example = "PRODUCT")
    private String consumeMode;

    @Schema(description = "扩展参数")
    private String extendParams;

    @Schema(description = "消费记录ID", example = "1001")
    private Long consumeRecordId;

    // 支付宝相关字段
    @Schema(description = "订单标题", example = "食堂午餐消费")
    private String subject;

    @Schema(description = "订单描述", example = "消费详情")
    private String body;
}