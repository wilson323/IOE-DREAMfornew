package net.lab1024.sa.consume.client.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 余额扣减请求DTO
 * <p>
 * 用于向账户服务发起余额扣减请求，支持多种业务场景
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Schema(description = "余额扣减请求")
public class BalanceDecreaseRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @NotNull(message = "金额不能为空")
    @Schema(description = "扣减金额", example = "25.50")
    private BigDecimal amount;

    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型", example = "CONSUME",
            allowableValues = {
                "CONSUME",           // 消费支付
                "SUBSIDY_REVOKE",    // 补贴撤销
                "WITHDRAW",          // 提现
                "DEDUCTION",         // 扣款
                "PENALTY"            // 罚款
            })
    private String businessType;

    @NotNull(message = "业务编号不能为空")
    @Schema(description = "业务编号（用于幂等性控制）", example = "CON-20251223-0001")
    private String businessNo;

    @Schema(description = "关联业务编号（如消费记录ID）", example = "CON-20251223-0001")
    private String relatedBusinessNo;

    @Schema(description = "备注说明", example = "午餐消费")
    private String remark;

    @Schema(description = "是否检查余额充足", example = "true")
    private Boolean checkBalance = true;

    @Schema(description = "扩展参数（JSON格式）", example = "{\"deviceId\":\"POS001\"}")
    private String extendedParams;
}
