package net.lab1024.sa.consume.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消费请求DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class ConsumeRequestDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 消费金额
     */
    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;

    /**
     * 消费类型
     */
    private String consumeType;

    /**
     * 消费模式
     */
    private String consumptionMode;

    /**
     * 消费描述
     */
    private String description;

    /**
     * 扩展参数
     */
    private java.util.Map<String, Object> extraParams;
}