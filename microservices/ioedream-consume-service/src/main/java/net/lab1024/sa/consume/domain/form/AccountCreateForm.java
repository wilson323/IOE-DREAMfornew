package net.lab1024.sa.consume.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 账户创建表单
 *
 * @author OpenSpec Task 2.10 Implementation
 * @version 1.0
 * @since 2025-11-17
 */
@Data
public class AccountCreateForm {

    /**
     * 用户ID（人员ID）
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 账户名称
     */
    @NotBlank(message = "账户名称不能为空")
    private String accountName;

    /**
     * 账户类型
     */
    @NotBlank(message = "账户类型不能为空")
    private String accountType;

    /**
     * 初始余额
     */
    private java.math.BigDecimal initialBalance;

    /**
     * 备注
     */
    private String remark;
}

