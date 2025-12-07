package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 报销申请表单
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
public class ReimbursementApplicationForm {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 报销金额
     */
    @NotNull(message = "报销金额不能为空")
    @DecimalMin(value = "0.01", message = "报销金额必须大于0")
    private BigDecimal reimbursementAmount;

    /**
     * 报销类型
     */
    @NotBlank(message = "报销类型不能为空")
    private String reimbursementType;

    /**
     * 报销事由
     */
    @NotBlank(message = "报销事由不能为空")
    @Size(max = 500, message = "报销事由长度不能超过500字符")
    private String reason;
}

