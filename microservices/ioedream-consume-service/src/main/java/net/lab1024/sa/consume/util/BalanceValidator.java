package net.lab1024.sa.consume.util;

import java.math.BigDecimal;

import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * 余额验证工具类
 * <p>
 * 提供统一的余额验证方法，减少代码重复
 * 严格遵循CLAUDE.md规范：
 * - 工具类不使用Spring注解
 * - 使用静态方法
 * - 提供清晰的错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class BalanceValidator {

    /**
     * 验证金额是否有效（大于0）
     *
     * @param amount 金额
     * @param fieldName 字段名（用于错误提示）
     * @throws BusinessException 金额无效时抛出
     */
    public static void validateAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new BusinessException("AMOUNT_NULL", fieldName + "不能为空");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("AMOUNT_INVALID", fieldName + "必须大于0");
        }
    }

    /**
     * 计算可用余额
     * <p>
     * 可用余额 = 账户余额 - 冻结金额
     * </p>
     *
     * @param account 账户实体
     * @return 可用余额
     */
    public static BigDecimal calculateAvailableBalance(AccountEntity account) {
        if (account == null) {
            throw new BusinessException("ACCOUNT_NULL", "账户信息不能为空");
        }

        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal frozenAmount = account.getFrozenAmount() != null
            ? account.getFrozenAmount() : BigDecimal.ZERO;

        return balance.subtract(frozenAmount);
    }

    /**
     * 验证余额是否充足
     *
     * @param account 账户实体
     * @param amount 需要金额
     * @throws BusinessException 余额不足时抛出
     */
    public static void validateBalanceSufficient(AccountEntity account, BigDecimal amount) {
        if (account == null) {
            throw new BusinessException("ACCOUNT_NULL", "账户信息不能为空");
        }

        validateAmount(amount, "金额");

        BigDecimal availableBalance = calculateAvailableBalance(account);
        if (availableBalance.compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE",
                String.format("余额不足，需要: %s，可用余额: %s", amount, availableBalance));
        }
    }

    /**
     * 验证余额是否充足（包含补贴余额）
     * <p>
     * 总可用余额 = 账户余额 + 补贴余额 - 冻结金额
     * </p>
     *
     * @param account 账户实体
     * @param amount 需要金额
     * @throws BusinessException 余额不足时抛出
     */
    public static void validateTotalBalanceSufficient(AccountEntity account, BigDecimal amount) {
        if (account == null) {
            throw new BusinessException("ACCOUNT_NULL", "账户信息不能为空");
        }

        validateAmount(amount, "金额");

        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal allowanceBalance = account.getAllowanceBalance() != null
            ? account.getAllowanceBalance() : BigDecimal.ZERO;
        BigDecimal frozenAmount = account.getFrozenAmount() != null
            ? account.getFrozenAmount() : BigDecimal.ZERO;

        BigDecimal totalAvailableBalance = balance.add(allowanceBalance).subtract(frozenAmount);

        if (totalAvailableBalance.compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE",
                String.format("余额不足，需要: %s，总可用余额: %s", amount, totalAvailableBalance));
        }
    }

    /**
     * 验证冻结金额是否有效
     *
     * @param account 账户实体
     * @param freezeAmount 冻结金额
     * @throws BusinessException 冻结金额无效时抛出
     */
    public static void validateFreezeAmount(AccountEntity account, BigDecimal freezeAmount) {
        if (account == null) {
            throw new BusinessException("ACCOUNT_NULL", "账户信息不能为空");
        }

        validateAmount(freezeAmount, "冻结金额");

        BigDecimal availableBalance = calculateAvailableBalance(account);
        if (freezeAmount.compareTo(availableBalance) > 0) {
            throw new BusinessException("FREEZE_AMOUNT_EXCEED",
                String.format("冻结金额超过可用余额，可用余额: %s", availableBalance));
        }
    }

    /**
     * 验证解冻金额是否有效
     *
     * @param account 账户实体
     * @param unfreezeAmount 解冻金额
     * @throws BusinessException 解冻金额无效时抛出
     */
    public static void validateUnfreezeAmount(AccountEntity account, BigDecimal unfreezeAmount) {
        if (account == null) {
            throw new BusinessException("ACCOUNT_NULL", "账户信息不能为空");
        }

        validateAmount(unfreezeAmount, "解冻金额");

        BigDecimal frozenAmount = account.getFrozenAmount() != null
            ? account.getFrozenAmount() : BigDecimal.ZERO;

        if (unfreezeAmount.compareTo(frozenAmount) > 0) {
            throw new BusinessException("UNFREEZE_AMOUNT_EXCEED",
                String.format("解冻金额超过冻结金额，冻结金额: %s", frozenAmount));
        }
    }
}



