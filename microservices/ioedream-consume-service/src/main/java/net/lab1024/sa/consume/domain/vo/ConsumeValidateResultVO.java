package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 消费权限校验结果
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeValidateResultVO {

    private Boolean allowed;

    private String reason;

    private BigDecimal accountBalance;

    private BigDecimal requiredAmount;
}
