package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 充值订单创建表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Schema(description = "充值订单创建表单")
public class ConsumeRechargeCreateForm {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal rechargeAmount;

    @NotBlank(message = "支付方式不能为空")
    @Schema(description = "支付方式", example = "WECHAT", allowableValues = {"WECHAT", "ALIPAY"})
    private String paymentMethod;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备信息", example = "iPhone 14 Pro")
    private String deviceInfo;
}
