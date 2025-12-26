package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费账户添加表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费账户添加表单")
public class ConsumeAccountAddForm {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "张三")
    private String username;

    @NotBlank(message = "账户类型不能为空")
    @Schema(description = "账户类型", example = "STAFF", allowableValues = {"STAFF", "STUDENT", "VISITOR"})
    private String accountType;

    @DecimalMin(value = "0.0", inclusive = true, message = "初始余额不能为负数")
    @Schema(description = "初始余额", example = "100.00")
    private BigDecimal initialBalance;

    @Schema(description = "信用额度", example = "500.00")
    private BigDecimal creditLimit;

    @Schema(description = "月消费限额", example = "2000.00")
    private BigDecimal monthlyLimit;

    @Schema(description = "单次消费限额", example = "100.00")
    private BigDecimal singleTransactionLimit;

    @Schema(description = "每日消费限额", example = "500.00")
    private BigDecimal dailyLimit;

    @Schema(description = "账户状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "FROZEN"})
    private String status;

    @Schema(description = "备注", example = "员工账户")
    private String remark;
}