package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费账户更新表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费账户更新表单")
public class ConsumeAccountUpdateForm {

    @Schema(description = "账户ID", example = "1001", required = true)
    private Long accountId;

    @Schema(description = "账户类型", example = "STAFF")
    private String accountType;

    @DecimalMin(value = "0.0", inclusive = true, message = "信用额度不能为负数")
    @Schema(description = "信用额度", example = "500.00")
    private BigDecimal creditLimit;

    @DecimalMin(value = "0.0", inclusive = true, message = "月消费限额不能为负数")
    @Schema(description = "月消费限额", example = "2000.00")
    private BigDecimal monthlyLimit;

    @DecimalMin(value = "0.0", inclusive = true, message = "单次消费限额不能为负数")
    @Schema(description = "单次消费限额", example = "100.00")
    private BigDecimal singleTransactionLimit;

    @DecimalMin(value = "0.0", inclusive = true, message = "每日消费限额不能为负数")
    @Schema(description = "每日消费限额", example = "500.00")
    private BigDecimal dailyLimit;

    @Schema(description = "账户状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "FROZEN"})
    private String status;

    @Schema(description = "备注", example = "员工账户")
    private String remark;
}