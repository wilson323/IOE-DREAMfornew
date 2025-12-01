package net.lab1024.sa.admin.module.consume.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

import net.lab1024.sa.base.common.cache.BaseCacheManager;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartStringUtil;
import net.lab1024.sa.admin.module.consume.dao.AccountDao;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.domain.dto.AccountValidationResult;
import net.lab1024.sa.admin.module.consume.domain.dto.AccountDeductResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 账户管理器
 * 严格遵循repowiki四层架构规范：Manager层负责复杂业务逻辑封装和缓存管理
 *
 * @author SmartAdmin Team
 * @since 2025-11-18
 */
@Slf4j
@Component
public class AccountManager {

    private static final String CACHE_PREFIX = "account:";
    private static final int CACHE_TTL_MINUTES = 30;

    @Resource
    private AccountDao accountDao;

    @Resource
    private BaseCacheManager cacheManager;

    /**
     * 账户验证
     *
     * @param account 账户信息
     * @return 验证结果
     */
    @Transactional(rollbackFor = Throwable.class)
    public AccountValidationResult validateAccount(AccountEntity account) {
        log.info("开始账户验证: accountId={}", account != null ? account.getAccountId() : null);

        try {
            if (account == null) {
                return AccountValidationResult.failure("账户信息不能为空");
            }

            // 检查账户状态
            if (account.getDeletedFlag() != null && account.getDeletedFlag()) {
                return AccountValidationResult.failure("账户已被删除");
            }

            // 检查账户冻结状态
            if (account.getFrozenAmount() != null && account.getFrozenAmount().compareTo(BigDecimal.ZERO) > 0) {
                return AccountValidationResult.failure("账户已被冻结，冻结金额: " + account.getFrozenAmount());
            }

            // 检查账户状态
            if (!"ACTIVE".equals(account.getStatus())) {
                return AccountValidationResult.failure("账户状态异常: " + account.getStatus());
            }

            log.info("账户验证成功: accountId={}", account.getAccountId());
            return AccountValidationResult.success();

        } catch (Exception e) {
            log.error("账户验证异常: accountId={}", account != null ? account.getAccountId() : null, e);
            return AccountValidationResult.failure("账户验证失败: " + e.getMessage());
        }
    }

    /**
     * 余额充足性检查
     *
     * @param accountId 账户ID
     * @param amount 需要检查的金额
     * @return 检查结果
     */
    public boolean checkBalanceSufficient(Long accountId, BigDecimal amount) {
        log.debug("检查余额充足性: accountId={}, amount={}", accountId, amount);

        try {
            if (accountId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            // 从缓存获取账户余额
            String cacheKey = CACHE_PREFIX + "balance:" + accountId;
            BigDecimal balance = cacheManager.get(cacheKey, BigDecimal.class);

            if (balance == null) {
                // 缓存未命中，从数据库获取
                AccountEntity account = accountDao.selectById(accountId);
                if (account == null) {
                    return false;
                }
                balance = account.getBalance();

                // 写入缓存
                cacheManager.put(cacheKey, balance, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            }

            boolean sufficient = balance != null && balance.compareTo(amount.add(account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO)) >= 0;

            log.debug("余额检查结果: accountId={}, balance={}, amount={}, frozen={}, result={}",
                accountId, balance, amount, account.getFrozenAmount(), sufficient);

            return sufficient;

        } catch (Exception e) {
            log.error("余额检查异常: accountId={}, amount={}", accountId, amount, e);
            return false;
        }
    }

    /**
     * 检查是否有冻结金额
     *
     * @param accountId 账户ID
     * @return 是否有冻结金额
     */
    public boolean hasFrozenAmount(Long accountId) {
        try {
            if (accountId == null) {
                return false;
            }

            String cacheKey = CACHE_PREFIX + "frozen:" + accountId;
            BigDecimal frozenAmount = cacheManager.get(cacheKey, BigDecimal.class);

            if (frozenAmount == null) {
                AccountEntity account = accountDao.selectById(accountId);
                if (account == null) {
                    return false;
                }
                frozenAmount = account.getFrozenAmount();

                cacheManager.put(cacheKey, frozenAmount, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            }

            return frozenAmount != null && frozenAmount.compareTo(BigDecimal.ZERO) > 0;

        } catch (Exception e) {
            log.error("检查冻结金额异常: accountId={}", accountId, e);
            return false;
        }
    }

    /**
     * 获取账户余额（带缓存）
     *
     * @param accountId 账户ID
     * @return 账户余额
     */
    public BigDecimal getAccountBalance(Long accountId) {
        log.debug("获取账户余额: accountId={}", accountId);

        try {
            if (accountId == null) {
                return BigDecimal.ZERO;
            }

            String cacheKey = CACHE_PREFIX + "balance:" + accountId;
            BigDecimal balance = cacheManager.get(cacheKey, BigDecimal.class);

            if (balance == null) {
                AccountEntity account = accountDao.selectById(accountId);
                if (account == null) {
                    return BigDecimal.ZERO;
                }
                balance = account.getBalance();

                cacheManager.put(cacheKey, balance, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            }

            log.debug("账户余额查询结果: accountId={}, balance={}", accountId, balance);
            return balance;

        } catch (Exception e) {
            log.error("获取账户余额异常: accountId={}", accountId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 更新账户缓存
     *
     * @param account 账户信息
     */
    public void updateAccountCache(AccountEntity account) {
        try {
            if (account == null || account.getAccountId() == null) {
                return;
            }

            // 更新余额缓存
            if (account.getBalance() != null) {
                String balanceKey = CACHE_PREFIX + "balance:" + account.getAccountId();
                cacheManager.put(balanceKey, account.getBalance(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            }

            // 更新冻结金额缓存
            if (account.getFrozenAmount() != null) {
                String frozenKey = CACHE_PREFIX + "frozen:" + account.getAccountId();
                cacheManager.put(frozenKey, account.getFrozenAmount(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            }

            // 清除账户信息缓存
            String infoKey = CACHE_PREFIX + "info:" + account.getAccountId();
            cacheManager.evict(infoKey);

            log.debug("更新账户缓存: accountId={}", account.getAccountId());

        } catch (Exception e) {
            log.error("更新账户缓存异常: accountId={}", account.getAccountId(), e);
        }
    }

    /**
     * 清除账户缓存
     *
     * @param accountId 账户ID
     */
    public void clearAccountCache(Long accountId) {
        try {
            if (accountId == null) {
                return;
            }

            // 清除所有相关缓存
            cacheManager.evict(CACHE_PREFIX + "balance:" + accountId);
            cacheManager.evict(CACHE_PREFIX + "frozen:" + accountId);
            cacheManager.evict(CACHE_PREFIX + "info:" + accountId);
            cacheManager.evict(CACHE_PREFIX + "list:" + accountId);

            log.debug("清除账户缓存: accountId={}", accountId);

        } catch (Exception e) {
            log.error("清除账户缓存异常: accountId={}", accountId, e);
        }
    }

    /**
     * 批量清除缓存
     *
     * @param accountIds 账户ID列表
     */
    public void clearAccountCacheBatch(List<Long> accountIds) {
        try {
            if (accountIds == null || accountIds.isEmpty()) {
                return;
            }

            for (Long accountId : accountIds) {
                clearAccountCache(accountId);
            }

            log.info("批量清除账户缓存完成: 数量={}", accountIds.size());

        } catch (Exception e) {
            log.error("批量清除账户缓存异常", e);
        }
    }

    /**
     * 获取账户信息
     *
     * @param accountId 账户ID
     * @return 账户信息
     */
    public AccountEntity getAccountById(Long accountId) {
        log.debug("获取账户信息: accountId={}", accountId);

        try {
            if (accountId == null) {
                return null;
            }

            String cacheKey = CACHE_PREFIX + "info:" + accountId;
            AccountEntity account = cacheManager.get(cacheKey, AccountEntity.class);

            if (account == null) {
                account = accountDao.selectById(accountId);
                if (account != null) {
                    cacheManager.put(cacheKey, account, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                }
            }

            return account;

        } catch (Exception e) {
            log.error("获取账户信息异常: accountId={}", accountId, e);
            return null;
        }
    }

    /**
     * 获取账户列表缓存Key
     *
     * @param params 查询参数
     * @return 缓存Key
     */
    public String getAccountListCacheKey(String params) {
        return CACHE_PREFIX + "list:" + SmartStringUtil.md5(params);
    }
}