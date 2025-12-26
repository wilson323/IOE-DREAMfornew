package net.lab1024.sa.consume.client.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 余额增加请求DTO
 * <p>
 * 用于向账户服务发起余额增加请求，支持多种业务场景
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Schema(description = "余额增加请求")
public class BalanceIncreaseRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @NotNull(message = "金额不能为空")
    @Schema(description = "增加金额", example = "100.00")
    private BigDecimal amount;

    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型", example = "SUBSIDY_GRANT",
            allowableValues = {
                "SUBSIDY_GRANT",    // 补贴发放
                "RECHARGE",          // 充值
                "REFUND",            // 退款
                "COMPENSATION",      // 补偿
                "BONUS"              // 奖励
            })
    private String businessType;

    @NotNull(message = "业务编号不能为空")
    @Schema(description = "业务编号（用于幂等性控制）", example = "SUB-20251223-0001")
    private String businessNo;

    @Schema(description = "业务子类型", example = "MONTHLY_MEAL")
    private String businessSubType;

    @Schema(description = "备注说明", example = "2025年12月餐补")
    private String remark;

    @Schema(description = "扩展参数（JSON格式）", example = "{\"period\":\"2025-12\"}")
    private String extendedParams;
}
