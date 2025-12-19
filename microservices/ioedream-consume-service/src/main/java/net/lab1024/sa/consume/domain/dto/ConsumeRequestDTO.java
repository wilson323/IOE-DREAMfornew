package net.lab1024.sa.consume.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

/**
 * 消费请求DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Accessors(chain = true)
public class ConsumeRequestDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 消费金额（手动模式时使用）
     */
    private BigDecimal amount;

    /**
     * 消费模式：FIXED-定值，MANUAL-手动
     */
    private String consumeMode;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 备注
     */
    private String remark;
}
