package net.lab1024.sa.admin.module.consume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户扣减结果
 * 用于原子扣减操作的详细结果返回
 * 严格遵循repowiki规范，包含完整的扣减信息和状态
 *
 * @author IOE-DREAM Team
 * @date 2025-11-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeductResult {

    /**
     * 扣减是否成功
     */
    private boolean success;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 扣减金额
     */
    private BigDecimal deductAmount;

    /**
     * 扣减前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 扣减后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 交易ID
     */
    private Long transactionId;

    /**
     * 扣减时间
     */
    private LocalDateTime deductTime;

    /**
     * 扩展信息
     */
    private String extraInfo;

    /**
     * 创建成功的扣减结果
     */
    public static AccountDeductResult success(Long accountId, Long userId,
                                             BigDecimal deductAmount,
                                             BigDecimal balanceBefore,
                                             BigDecimal balanceAfter,
                                             String orderNo) {
        return AccountDeductResult.builder()
                .success(true)
                .accountId(accountId)
                .userId(userId)
                .deductAmount(deductAmount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .orderNo(orderNo)
                .transactionNo(generateTransactionNo())
                .deductTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败的扣减结果
     */
    public static AccountDeductResult failure(Long accountId, String errorCode, String failureReason) {
        return AccountDeductResult.builder()
                .success(false)
                .accountId(accountId)
                .errorCode(errorCode)
                .failureReason(failureReason)
                .deductTime(LocalDateTime.now())
                .build();
    }

    /**
     * 生成交易流水号
     */
    private static String generateTransactionNo() {
        return "TXN" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 余额是否充足
     */
    public boolean isBalanceSufficient() {
        return availableBalance != null &&
               deductAmount != null &&
               availableBalance.compareTo(deductAmount) >= 0;
    }

    /**
     * 获取成功状态描述
     */
    public String getStatusDescription() {
        return success ? "扣减成功" : "扣减失败";
    }
}