package net.lab1024.sa.consume.manager.impl;

import java.math.BigDecimal;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.manager.AccountManager;

/**
 * 账户管理Manager实现类
 * <p>
 * 实现账户相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager实现类在ioedream-consume-service中
 * - 通过构造函数注入依赖
 * - 保持为纯Java类（不使用Spring注解）
 * </p>
 * <p>
 * 业务场景：
 * - 账户余额管理
 * - 账户充值
 * - 账户消费扣款
 * - 账户冻结/解冻
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class AccountManagerImpl implements AccountManager {

    private final AccountDao accountDao;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：通过构造函数接收依赖
     * </p>
     *
     * @param accountDao 账户DAO
     */
    public AccountManagerImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * 根据用户ID获取账户信息
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    @Override
    public AccountEntity getAccountByUserId(Long userId) {
        log.debug("[账户管理] 根据用户ID获取账户信息，userId={}", userId);
        try {
            return accountDao.selectByUserId(userId);
        } catch (Exception e) {
            log.error("[账户管理] 获取账户信息失败，userId={}", userId, e);
            return null;
        }
    }

    /**
     * 根据账户ID获取账户信息
     *
     * @param accountId 账户ID
     * @return 账户信息
     */
    @Override
    public AccountEntity getAccountById(Long accountId) {
        log.debug("[账户管理] 根据账户ID获取账户信息，accountId={}", accountId);
        try {
            return accountDao.selectById(accountId);
        } catch (Exception e) {
            log.error("[账户管理] 获取账户信息失败，accountId={}", accountId, e);
            return null;
        }
    }

    /**
     * 扣减账户余额
     *
     * @param accountId 账户ID
     * @param amount 扣减金额（单位：元）
     * @return 是否成功
     */
    @Override
    public boolean deductBalance(Long accountId, BigDecimal amount) {
        log.info("[账户管理] 扣减账户余额，accountId={}, amount={}", accountId, amount);
        try {
            AccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                log.warn("[账户管理] 账户不存在，accountId={}", accountId);
                return false;
            }

            // 获取当前余额（元）
            BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
            
            // 计算新余额（元）
            BigDecimal newBalance = currentBalance.subtract(amount);

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("[账户管理] 余额不足，accountId={}, balance={}, amount={}", accountId, currentBalance, amount);
                return false;
            }

            // 更新余额（使用BigDecimal类型，单位为元）
            account.setBalance(newBalance);
            account.setVersion(account.getVersion() + 1); // 乐观锁版本号+1
            int result = accountDao.updateById(account);

            log.info("[账户管理] 扣减账户余额成功，accountId={}, amount={}, newBalance={}", accountId, amount, newBalance);
            return result > 0;

        } catch (Exception e) {
            log.error("[账户管理] 扣减账户余额失败，accountId={}, amount={}", accountId, amount, e);
            return false;
        }
    }

    /**
     * 增加账户余额
     *
     * @param accountId 账户ID
     * @param amount 增加金额（单位：元）
     * @return 是否成功
     */
    @Override
    public boolean addBalance(Long accountId, BigDecimal amount) {
        log.info("[账户管理] 增加账户余额，accountId={}, amount={}", accountId, amount);
        try {
            AccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                log.warn("[账户管理] 账户不存在，accountId={}", accountId);
                return false;
            }

            // 获取当前余额（元）
            BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
            
            // 计算新余额（元）
            BigDecimal newBalance = currentBalance.add(amount);

            // 更新余额（使用BigDecimal类型，单位为元）
            account.setBalance(newBalance);
            account.setVersion(account.getVersion() + 1); // 乐观锁版本号+1
            int result = accountDao.updateById(account);

            log.info("[账户管理] 增加账户余额成功，accountId={}, amount={}, newBalance={}", accountId, amount, newBalance);
            return result > 0;

        } catch (Exception e) {
            log.error("[账户管理] 增加账户余额失败，accountId={}, amount={}", accountId, amount, e);
            return false;
        }
    }

    /**
     * 检查账户余额是否充足
     *
     * @param accountId 账户ID
     * @param amount 需要金额（单位：元）
     * @return 是否充足
     */
    @Override
    public boolean checkBalanceSufficient(Long accountId, BigDecimal amount) {
        log.debug("[账户管理] 检查账户余额是否充足，accountId={}, amount={}", accountId, amount);
        try {
            AccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                log.warn("[账户管理] 账户不存在，accountId={}", accountId);
                return false;
            }

            // 获取当前余额（元）
            BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
            
            // 计算可用余额（余额 - 冻结金额）
            BigDecimal frozenAmount = account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO;
            BigDecimal availableBalance = currentBalance.subtract(frozenAmount);
            
            // 比较可用余额和需要金额
            boolean sufficient = availableBalance.compareTo(amount) >= 0;

            log.debug("[账户管理] 余额检查结果，accountId={}, balance={}, frozenAmount={}, availableBalance={}, amount={}, sufficient={}",
                    accountId, currentBalance, frozenAmount, availableBalance, amount, sufficient);
            return sufficient;

        } catch (Exception e) {
            log.error("[账户管理] 检查账户余额失败，accountId={}, amount={}", accountId, amount, e);
            return false;
        }
    }
}
