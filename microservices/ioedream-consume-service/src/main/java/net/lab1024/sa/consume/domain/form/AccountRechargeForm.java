package net.lab1024.sa.consume.domain.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户充值表单
 *
 * @author OpenSpec Task 2.10 Implementation
 * @version 1.0
 * @since 2025-11-17
 */
@Data
public class AccountRechargeForm {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    /**
     * 充值金额
     */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    /**
     * 充值方式
     */
    private String rechargeType;

    /**
     * 备注
     */
    private String remark;
}

