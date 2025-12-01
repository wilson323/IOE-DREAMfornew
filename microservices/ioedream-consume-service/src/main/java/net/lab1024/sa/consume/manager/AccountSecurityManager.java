package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;

/**
 * 账户安全管理器接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface AccountSecurityManager {

    /**
     * 检查账户余额是否充足
     *
     * @param userId 用户ID
     * @param amount 金额
     * @return 是否充足
     */
    boolean isBalanceSufficient(Long userId, BigDecimal amount);

    /**
     * 冻结账户资金
     *
     * @param userId 用户ID
     * @param amount 金额
     * @return 是否成功
     */
    boolean freezeAccountBalance(Long userId, BigDecimal amount);

    /**
     * 解冻账户资金
     *
     * @param userId 用户ID
     * @param amount 金额
     * @return 是否成功
     */
    boolean unfreezeAccountBalance(Long userId, BigDecimal amount);

    /**
     * 扣除账户余额
     *
     * @param userId 用户ID
     * @param amount 金额
     * @return 是否成功
     */
    boolean deductAccountBalance(Long userId, BigDecimal amount);

    /**
     * 检查账户是否被冻结
     *
     * @param userId 用户ID
     * @return 是否被冻结
     */
    boolean isAccountFrozen(Long userId);
}