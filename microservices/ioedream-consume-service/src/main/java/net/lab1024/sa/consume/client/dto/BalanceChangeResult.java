package net.lab1024.sa.consume.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 余额变更结果DTO
 * <p>
 * 封装账户余额变更操作的执行结果
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
@Schema(description = "余额变更结果")
public class BalanceChangeResult {

    @Schema(description = "交易ID", example = "TXN-20251223-0001")
    private String transactionId;

    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @Schema(description = "变更前余额", example = "100.00")
    private BigDecimal balanceBefore;

    @Schema(description = "变更后余额", example = "200.00")
    private BigDecimal balanceAfter;

    @Schema(description = "变更金额", example = "100.00")
    private BigDecimal changeAmount;

    @Schema(description = "可用余额", example = "180.00")
    private BigDecimal availableBalance;

    @Schema(description = "冻结余额", example = "20.00")
    private BigDecimal frozenBalance;

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "错误码", example = "BALANCE_INSUFFICIENT")
    private String errorCode;

    @Schema(description = "错误信息", example = "余额不足")
    private String errorMessage;

    @Schema(description = "交易时间", example = "2025-12-23T12:00:00")
    private LocalDateTime transactionTime;

    @Schema(description = "幂等键", example = "SUB-20251223-0001")
    private String idempotentKey;

    /**
     * 创建成功结果
     */
    public static BalanceChangeResult success(String transactionId, Long userId,
                                             BigDecimal balanceBefore, BigDecimal balanceAfter,
                                             BigDecimal changeAmount) {
        return BalanceChangeResult.builder()
            .transactionId(transactionId)
            .userId(userId)
            .balanceBefore(balanceBefore)
            .balanceAfter(balanceAfter)
            .changeAmount(changeAmount)
            .success(true)
            .transactionTime(LocalDateTime.now())
            .build();
    }

    /**
     * 创建失败结果
     */
    public static BalanceChangeResult failure(String errorCode, String errorMessage) {
        return BalanceChangeResult.builder()
            .success(false)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .transactionTime(LocalDateTime.now())
            .build();
    }
}
