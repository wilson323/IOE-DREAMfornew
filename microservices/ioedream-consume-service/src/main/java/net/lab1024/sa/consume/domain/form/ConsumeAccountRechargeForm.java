package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费账户充值表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费账户充值表单")
public class ConsumeAccountRechargeForm {

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @NotBlank(message = "充值方式不能为空")
    @Schema(description = "充值方式", example = "CASH", allowableValues = {"CASH", "BANK_CARD", "WECHAT", "ALIPAY", "TRANSFER"})
    private String rechargeWay;

    @Schema(description = "交易流水号", example = "TXN202512210001")
    private String transactionNo;

    @Schema(description = "充值时间")
    private java.time.LocalDateTime rechargeTime;

    @NotBlank(message = "操作人不能为空")
    @Schema(description = "操作人", example = "管理员")
    private String operator;

    @Schema(description = "充值说明", example = "月度补贴")
    private String remark;

    @Schema(description = "是否自动到账", example = "true")
    private Boolean autoConfirm = true;

    // ==================== 兼容性方法 ====================

    /**
     * 设置充值类型（兼容性方法）
     */
    public void setRechargeType(String rechargeType) {
        this.rechargeWay = rechargeType;
    }

    /**
     * 获取充值类型（兼容性方法）
     */
    public String getRechargeType() {
        return this.rechargeWay;
    }
}