package net.lab1024.sa.admin.module.consume.domain.form;

import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 账户充值表单
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 使用@NotNull进行参数验证
 * - 提供完整的充值字段
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Data
@Accessors(chain = true)
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
    private BigDecimal amount;

    /**
     * 充值方式：1-现金 2-银行卡 3-微信 4-支付宝 5-其他
     */
    @NotNull(message = "充值方式不能为空")
    private Integer rechargeMethod;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 充值时间
     */
    private String rechargeTime;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 备注
     */
    private String remark;
}