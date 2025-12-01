package net.lab1024.sa.admin.module.consume.domain.form;

import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 账户更新表单
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 使用@NotNull进行参数验证
 * - 提供完整的更新字段
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Data
@Accessors(chain = true)
public class AccountUpdateForm {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    /**
     * 账户类型：1-个人账户 2-企业账户
     */
    private Integer accountType;

    /**
     * 账户状态：1-正常 2-冻结 3-注销
     */
    private Integer status;

    /**
     * 信用额度
     */
    private BigDecimal creditLimit;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 备注
     */
    private String remark;
}