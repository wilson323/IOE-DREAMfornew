package net.lab1024.sa.consume.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 账户更新表单
 *
 * @author OpenSpec Task 2.10 Implementation
 * @version 1.0
 * @since 2025-11-17
 */
@Data
public class AccountUpdateForm {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    /**
     * 账户名称
     */
    @NotBlank(message = "账户名称不能为空")
    private String accountName;

    /**
     * 备注
     */
    private String remark;
}

