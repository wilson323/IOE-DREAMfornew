package net.lab1024.sa.admin.module.consume.domain.dto;

import lombok.Data;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum;
import java.math.BigDecimal;
import java.util.List;

/**
 * 消费模式配置DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class ConsumeModeConfig {

    /**
     * 模式类型
     */
    private ConsumeModeEnum modeType;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 单次最小金额
     */
    private BigDecimal minSingleAmount;

    /**
     * 单次最大金额
     */
    private BigDecimal maxSingleAmount;

    /**
     * 每日限额
     */
    private BigDecimal dailyLimit;

    /**
     * 描述
     */
    private String description;

    /**
     * 支持的金额档位（固定金额模式使用）
     */
    private BigDecimal[] supportedAmounts;

    /**
     * 是否允许自定义金额（自由金额模式使用）
     */
    private boolean allowCustomAmount;
}