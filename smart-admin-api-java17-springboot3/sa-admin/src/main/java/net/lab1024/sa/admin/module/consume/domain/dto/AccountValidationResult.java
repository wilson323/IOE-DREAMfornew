package net.lab1024.sa.admin.module.consume.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 账户验证结果DTO
 * 严格遵循repowiki规范：数据传输对象，包含账户验证的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountValidationResult {

    /**
     * 是否验证通过
     */
    private Boolean valid;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 账户状态
     */
    private String accountStatus;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 可用余额（包含信用额度）
     */
    private BigDecimal availableBalance;

    /**
     * 信用额度
     */
    private BigDecimal creditLimit;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 日累计消费金额
     */
    private BigDecimal dailyConsumedAmount;

    /**
     * 日消费限额
     */
    private BigDecimal dailyLimit;

    /**
     * 月累计消费金额
     */
    private BigDecimal monthlyConsumedAmount;

    /**
     * 月消费限额
     */
    private BigDecimal monthlyLimit;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 验证时间（毫秒）
     */
    private Long validationTime;

    /**
     * 创建成功结果
     */
    public static AccountValidationResult success(Long accountId, BigDecimal availableBalance) {
        return AccountValidationResult.builder()
                .valid(true)
                .accountId(accountId)
                .availableBalance(availableBalance)
                .validationTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建完整成功结果
     */
    public static AccountValidationResult success(Long accountId, String accountStatus,
                                                BigDecimal balance, BigDecimal availableBalance,
                                                BigDecimal creditLimit, BigDecimal frozenAmount) {
        return AccountValidationResult.builder()
                .valid(true)
                .accountId(accountId)
                .accountStatus(accountStatus)
                .balance(balance)
                .availableBalance(availableBalance)
                .creditLimit(creditLimit)
                .frozenAmount(frozenAmount)
                .validationTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static AccountValidationResult failure(String errorMessage) {
        return AccountValidationResult.builder()
                .valid(false)
                .errorMessage(errorMessage)
                .validationTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建账户异常结果
     */
    public static AccountValidationResult accountError(Long accountId, String accountStatus, String errorMessage) {
        return AccountValidationResult.builder()
                .valid(false)
                .accountId(accountId)
                .accountStatus(accountStatus)
                .errorMessage(errorMessage)
                .validationTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建余额不足结果
     */
    public static AccountValidationResult insufficientBalance(Long accountId, BigDecimal availableBalance, BigDecimal requestAmount) {
        return AccountValidationResult.builder()
                .valid(false)
                .accountId(accountId)
                .availableBalance(availableBalance)
                .errorMessage(String.format("余额不足，可用余额：%.2f，请求金额：%.2f", availableBalance, requestAmount))
                .validationTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建超限额结果
     */
    public static AccountValidationResult limitExceeded(Long accountId, String limitType, BigDecimal currentAmount, BigDecimal limitAmount) {
        return AccountValidationResult.builder()
                .valid(false)
                .accountId(accountId)
                .errorMessage(String.format("%s限额超限，当前：%.2f，限额：%.2f", limitType, currentAmount, limitAmount))
                .validationTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 检查是否验证通过
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(valid);
    }

    /**
     * 检查是否验证失败
     */
    public boolean isInvalid() {
        return Boolean.FALSE.equals(valid);
    }

    /**
     * 检查账户状态是否正常
     */
    public boolean isAccountStatusNormal() {
        return "NORMAL".equals(accountStatus) || "ACTIVE".equals(accountStatus);
    }

    /**
     * 检查是否余额充足
     */
    public boolean isBalanceSufficient(BigDecimal requestAmount) {
        return availableBalance != null && availableBalance.compareTo(requestAmount) >= 0;
    }

    /**
     * 检查是否超出日限额
     */
    public boolean isDailyLimitExceeded(BigDecimal additionalAmount) {
        if (dailyLimit == null || dailyConsumedAmount == null) {
            return false;
        }
        return dailyConsumedAmount.add(additionalAmount).compareTo(dailyLimit) > 0;
    }

    /**
     * 检查是否超出月限额
     */
    public boolean isMonthlyLimitExceeded(BigDecimal additionalAmount) {
        if (monthlyLimit == null || monthlyConsumedAmount == null) {
            return false;
        }
        return monthlyConsumedAmount.add(additionalAmount).compareTo(monthlyLimit) > 0;
    }

    /**
     * 获取格式化的可用余额
     */
    public String getFormattedAvailableBalance() {
        return availableBalance != null ? availableBalance.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
    }

    /**
     * 获取格式化的账户余额
     */
    public String getFormattedBalance() {
        return balance != null ? balance.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
    }
}