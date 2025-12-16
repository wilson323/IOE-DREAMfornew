package net.lab1024.sa.consume.util;

import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * 账户验证工具类
 * <p>
 * 提供统一的账户验证方法，减少代码重复
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
public class AccountValidator {

    /**
     * 账户状态常量
     */
    public static final Integer STATUS_NORMAL = 1;  // 正常
    public static final Integer STATUS_FROZEN = 2;  // 冻结
    public static final Integer STATUS_CLOSED = 3;  // 注销

    /**
     * 验证账户是否存在
     *
     * @param accountDao 账户DAO
     * @param accountId 账户ID
     * @return 账户实体
     * @throws BusinessException 账户不存在时抛出
     */
    public static AccountEntity validateAccountExists(AccountDao accountDao, Long accountId) {
        if (accountId == null) {
            throw new BusinessException("ACCOUNT_ID_NULL", "账户ID不能为空");
        }

        AccountEntity account = accountDao.selectById(accountId);
        if (account == null) {
            throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        }

        return account;
    }

    /**
     * 验证账户状态是否有效（正常状态）
     *
     * @param account 账户实体
     * @throws BusinessException 账户状态无效时抛出
     */
    public static void validateAccountStatus(AccountEntity account) {
        if (account == null) {
            throw new BusinessException("ACCOUNT_NULL", "账户信息不能为空");
        }

        if (account.getStatus() == null || !account.getStatus().equals(STATUS_NORMAL)) {
            throw new BusinessException("ACCOUNT_INVALID",
                "账户状态无效，当前状态: " + account.getStatus());
        }
    }

    /**
     * 验证账户是否未被冻结
     *
     * @param account 账户实体
     * @throws BusinessException 账户被冻结时抛出
     */
    public static void validateAccountNotFrozen(AccountEntity account) {
        if (account == null) {
            throw new BusinessException("ACCOUNT_NULL", "账户信息不能为空");
        }

        if (account.getStatus() != null && account.getStatus().equals(STATUS_FROZEN)) {
            throw new BusinessException("ACCOUNT_FROZEN", "账户已被冻结");
        }
    }

    /**
     * 验证账户是否未被注销
     *
     * @param account 账户实体
     * @throws BusinessException 账户已注销时抛出
     */
    public static void validateAccountNotClosed(AccountEntity account) {
        if (account == null) {
            throw new BusinessException("ACCOUNT_NULL", "账户信息不能为空");
        }

        if (account.getStatus() != null && account.getStatus().equals(STATUS_CLOSED)) {
            throw new BusinessException("ACCOUNT_CLOSED", "账户已被注销");
        }
    }

    /**
     * 验证并获取账户（存在性+状态）
     *
     * @param accountDao 账户DAO
     * @param accountId 账户ID
     * @return 账户实体
     * @throws BusinessException 账户不存在或状态无效时抛出
     */
    public static AccountEntity validateAndGetAccount(AccountDao accountDao, Long accountId) {
        AccountEntity account = validateAccountExists(accountDao, accountId);
        validateAccountStatus(account);
        return account;
    }

    /**
     * 验证账户是否可用于操作（存在+正常+未冻结+未注销）
     *
     * @param accountDao 账户DAO
     * @param accountId 账户ID
     * @return 账户实体
     * @throws BusinessException 账户不可用时抛出
     */
    public static AccountEntity validateAccountAvailable(AccountDao accountDao, Long accountId) {
        AccountEntity account = validateAccountExists(accountDao, accountId);
        validateAccountStatus(account);
        validateAccountNotFrozen(account);
        validateAccountNotClosed(account);
        return account;
    }
}



