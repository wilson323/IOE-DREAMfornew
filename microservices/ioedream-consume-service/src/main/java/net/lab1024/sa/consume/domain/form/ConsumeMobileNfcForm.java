package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端NFC消费表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeMobileNfcForm {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotBlank(message = "卡号不能为空")
    private String cardNumber;

    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;
}
