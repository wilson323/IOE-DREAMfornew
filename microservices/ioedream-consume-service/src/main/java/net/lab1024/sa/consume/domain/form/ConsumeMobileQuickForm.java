package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端快速消费表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeMobileQuickForm {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;

    private String consumeMode;

    private String remark;
}
