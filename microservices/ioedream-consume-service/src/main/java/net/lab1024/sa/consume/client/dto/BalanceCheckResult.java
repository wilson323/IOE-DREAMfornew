package net.lab1024.sa.consume.client.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 余额检查结果DTO
 * <p>
 * 封装账户余额检查操作的执行结果
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "余额检查结果")
public class BalanceCheckResult {

    @Schema(description = "是否充足", example = "true")
    private Boolean sufficient;

    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @Schema(description = "当前余额", example = "100.00")
    private BigDecimal currentBalance;

    @Schema(description = "可用余额", example = "80.00")
    private BigDecimal availableBalance;

    @Schema(description = "冻结余额", example = "20.00")
    private BigDecimal frozenBalance;

    @Schema(description = "检查金额", example = "50.00")
    private BigDecimal checkAmount;

    @Schema(description = "差额（当前余额 - 检查金额）", example = "50.00")
    private BigDecimal difference;

    @Schema(description = "批量检查结果", example = "{\"10001\":true,\"10002\":false}")
    private Map<Long, Boolean> batchResults;

    @Schema(description = "不足的用户列表", example = "[10002,10003]")
    private List<Long> insufficientUsers;

    /**
     * 创建单用户检查结果
     */
    public static BalanceCheckResult forSingleUser(Long userId, Boolean sufficient,
                                                   BigDecimal currentBalance, BigDecimal checkAmount) {
        BigDecimal difference = currentBalance.subtract(checkAmount);
        return BalanceCheckResult.builder()
            .userId(userId)
            .sufficient(sufficient)
            .currentBalance(currentBalance)
            .checkAmount(checkAmount)
            .difference(difference)
            .build();
    }

    /**
     * 创建批量检查结果
     */
    public static BalanceCheckResult forBatch(Map<Long, Boolean> batchResults,
                                              List<Long> insufficientUsers) {
        return BalanceCheckResult.builder()
            .batchResults(batchResults)
            .insufficientUsers(insufficientUsers)
            .sufficient(insufficientUsers.isEmpty())
            .build();
    }
}
