package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 移动端快速消费表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeMobileQuickForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 消费金额
     */
    @NotNull(message = "消费金额不能为空")
    @Positive(message = "消费金额必须大于0")
    private BigDecimal amount;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 备注
     */
    private String remark;
}
