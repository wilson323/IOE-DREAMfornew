package net.lab1024.sa.consume.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 账户创建表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccountAddForm {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 账户类别ID
     */
    @NotNull(message = "账户类别ID不能为空")
    private Long accountKindId;

    /**
     * 初始余额（单位：元，可选，默认为0）
     */
    private java.math.BigDecimal initialBalance;

    /**
     * 备注
     */
    private String remark;
}

