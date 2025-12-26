package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.dao.ConsumeAccountDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.domain.form.ConsumeAccountQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountRechargeForm;
import net.lab1024.sa.consume.domain.vo.ConsumeAccountVO;
import net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.exception.ConsumeAccountException;

/**
 * 消费账户管理器
 * <p>
 * 负责消费账户的业务逻辑编排，不使用Spring注解，保持为纯Java类
 * 严格遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
public class ConsumeAccountManager {

    private final ConsumeAccountDao consumeAccountDao;
    private final ConsumeTransactionDao consumeTransactionDao;
    private final ConsumeDistributedLockManager lockManager;

    /**
     * 构造函数注入依赖（增强版，包含分布式锁）
     *
     * @param consumeAccountDao 账户数据访问对象
     * @param consumeTransactionDao 交易数据访问对象
     * @param lockManager           分布式锁管理器
     */
    public ConsumeAccountManager(ConsumeAccountDao consumeAccountDao,
                               ConsumeTransactionDao consumeTransactionDao,
                               ConsumeDistributedLockManager lockManager) {
        this.consumeAccountDao = consumeAccountDao;
        this.consumeTransactionDao = consumeTransactionDao;
        this.lockManager = lockManager;
    }

    /**
     * 获取账户详情
     *
     * @param accountId 账户ID
     * @return 账户详情
     */
    public ConsumeAccountVO getAccountDetail(Long accountId) {
        log.info("[账户管理] 获取账户详情, accountId={}", accountId);

        // 1. 获取账户基本信息
        ConsumeAccountVO account = consumeAccountDao.selectAccountById(accountId);
        if (account == null) {
            log.warn("[账户管理] 账户不存在, accountId={}", accountId);
            return null;
        }

        // 2. 获取账户统计信息
        Map<String, Object> statistics = consumeTransactionDao.getAccountStatistics(accountId);
        if (statistics != null) {
            account.setTotalConsumeCount((Integer) statistics.get("totalTransactions"));
            account.setTotalConsumeAmount((BigDecimal) statistics.get("totalAmount"));
        }

        log.info("[账户管理] 账户详情获取成功, accountId={}, balance={}", accountId, account.getBalance());
        return account;
    }

    /**
     * 根据用户ID获取账户信息
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    public ConsumeAccountVO getAccountByUserId(Long userId) {
        log.info("[账户管理] 根据用户ID获取账户, userId={}", userId);

        ConsumeAccountVO account = consumeAccountDao.selectByUserId(userId);
        if (account == null) {
            log.warn("[账户管理] 用户账户不存在, userId={}", userId);
            return null;
        }

        log.info("[账户管理] 用户账户获取成功, userId={}, accountId={}", userId, account.getAccountId());
        return account;
    }

    /**
     * 账户充值处理
     *
     * @param accountId 账户ID
     * @param rechargeForm 充值表单
     * @return 充值结果
     */
    public Boolean rechargeAccount(Long accountId, ConsumeAccountRechargeForm rechargeForm) {
        log.info("[账户管理] 开始账户充值, accountId={}, amount={}", accountId, rechargeForm.getAmount());

        try {
            // 1. 验证账户状态
            ConsumeAccountVO account = consumeAccountDao.selectAccountById(accountId);
            if (account == null) {
                log.error("[账户管理] 充值失败，账户不存在, accountId={}", accountId);
                return false;
            }

            if (!"ACTIVE".equals(account.getStatus())) {
                log.error("[账户管理] 充值失败，账户状态异常, accountId={}, status={}", accountId, account.getStatus());
                return false;
            }

            // 2. 计算充值后余额
            BigDecimal newBalance = account.getBalance().add(rechargeForm.getAmount());

            // 3. 更新账户余额
            int updateCount = consumeAccountDao.updateBalance(accountId, newBalance);
            if (updateCount <= 0) {
                log.error("[账户管理] 充值失败，余额更新失败, accountId={}", accountId);
                return false;
            }

            // 4. 记录充值交易
            ConsumeTransactionEntity transaction = new ConsumeTransactionEntity();
            transaction.setAccountId(accountId);
            transaction.setUserId(account.getUserId());
            transaction.setTransactionAmount(rechargeForm.getAmount());
            transaction.setActualAmount(rechargeForm.getAmount());
            transaction.setBalanceBefore(account.getBalance());
            transaction.setBalanceAfter(newBalance);
            transaction.setTransactionType(1); // 1-充值
            transaction.setTransactionTypeName("充值");
            transaction.setPaymentMethod(1); // 1-余额支付
            transaction.setPaymentMethodName("余额支付");
            transaction.setTransactionStatus(1); // 1-成功
            transaction.setTransactionStatusName("成功");
            transaction.setTransactionTime(LocalDateTime.now());

            consumeTransactionDao.insert(transaction);

            log.info("[账户管理] 充值成功, accountId={}, amount={}, newBalance={}",
                    accountId, rechargeForm.getAmount(), newBalance);
            return true;

        } catch (Exception e) {
            log.error("[账户管理] 充值异常, accountId={}, amount={}", accountId, rechargeForm.getAmount(), e);
            return false;
        }
    }

    /**
     * 验证账户余额是否充足
     *
     * @param accountId 账户ID
     * @param amount 验证金额
     * @return 是否充足
     */
    public Boolean validateBalance(Long accountId, BigDecimal amount) {
        log.debug("[账户管理] 验证账户余额, accountId={}, amount={}", accountId, amount);

        ConsumeAccountVO account = consumeAccountDao.selectAccountById(accountId);
        if (account == null) {
            log.warn("[账户管理] 余额验证失败，账户不存在, accountId={}", accountId);
            return false;
        }

        boolean isValid = account.getBalance().compareTo(amount) >= 0;
        log.debug("[账户管理] 余额验证结果, accountId={}, balance={}, amount={}, isValid={}",
                 accountId, account.getBalance(), amount, isValid);

        return isValid;
    }

    /**
     * 冻结账户
     *
     * @param accountId 账户ID
     * @param reason 冻结原因
     * @return 操作结果
     */
    public Boolean freezeAccount(Long accountId, String reason) {
        log.info("[账户管理] 冻结账户, accountId={}, reason={}", accountId, reason);

        try {
            int updateCount = consumeAccountDao.updateStatus(accountId, 2); // 2-冻结状态
            if (updateCount <= 0) {
                log.error("[账户管理] 冻结账户失败, accountId={}", accountId);
                return false;
            }

            log.info("[账户管理] 账户冻结成功, accountId={}", accountId);
            return true;

        } catch (Exception e) {
            log.error("[账户管理] 冻结账户异常, accountId={}", accountId, e);
            return false;
        }
    }

    /**
     * 解冻账户
     *
     * @param accountId 账户ID
     * @param reason 解冻原因
     * @return 操作结果
     */
    public Boolean unfreezeAccount(Long accountId, String reason) {
        log.info("[账户管理] 解冻账户, accountId={}, reason={}", accountId, reason);

        try {
            int updateCount = consumeAccountDao.updateStatus(accountId, 1); // 1-正常状态
            if (updateCount <= 0) {
                log.error("[账户管理] 解冻账户失败, accountId={}", accountId);
                return false;
            }

            log.info("[账户管理] 账户解冻成功, accountId={}", accountId);
            return true;

        } catch (Exception e) {
            log.error("[账户管理] 解冻账户异常, accountId={}", accountId, e);
            return false;
        }
    }

    /**
     * 分页查询账户列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    public PageResult<ConsumeAccountVO> queryAccounts(ConsumeAccountQueryForm queryForm) {
        log.info("[账户管理] 分页查询账户, pageNum={}, pageSize={}", queryForm.getPageNum(), queryForm.getPageSize());

        try {
            PageResult<ConsumeAccountVO> result = consumeAccountDao.queryPage(queryForm);

            log.info("[账户管理] 账户查询完成, total={}, pageNum={}, pageSize={}",
                    result.getTotal(), result.getPageNum(), result.getPageSize());

            return result;

        } catch (Exception e) {
            log.error("[账户管理] 账户查询异常, queryForm={}", queryForm, e);
            return PageResult.empty();
        }
    }

    /**
     * 扣减账户余额（使用分布式锁保护）
     *
     * @param accountId 账户ID
     * @param amount 扣减金额
     * @param description 扣减描述
     * @return 扣减结果
     */
    public Boolean deductAmount(Long accountId, BigDecimal amount, String description) {
        log.info("[账户管理] 开始扣减账户余额: accountId={}, amount={}, description={}", accountId, amount, description);

        // 参数验证
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw ConsumeAccountException.invalidAmount(amount.toString());
        }

        return lockManager.executeWithAccountLock(accountId, () -> {
            return deductAmountWithLock(accountId, amount, description);
        });
    }

    /**
     * 账户锁保护下的余额扣减
     */
    private Boolean deductAmountWithLock(Long accountId, BigDecimal amount, String description) {
        try {
            // 1. 获取账户信息（防止锁定期间被修改）
            ConsumeAccountVO account = consumeAccountDao.selectAccountById(accountId);
            if (account == null) {
                throw ConsumeAccountException.notFound(accountId);
            }

            // 2. 验证账户状态
            if (!"ACTIVE".equals(account.getStatus())) {
                throw ConsumeAccountException.frozen(accountId);
            }

            // 3. 验证余额
            if (account.getBalance().compareTo(amount) < 0) {
                throw ConsumeAccountException.insufficientBalance(accountId, account.getBalance().toString(), amount.toString());
            }

            // 4. 计算新余额
            BigDecimal newBalance = account.getBalance().subtract(amount);

            // 5. 乐观锁更新余额
            int updateCount = consumeAccountDao.updateBalanceWithVersion(accountId, newBalance, account.getVersion());

            if (updateCount <= 0) {
                log.warn("[账户管理] 乐观锁冲突，账户已被其他事务修改: accountId={}", accountId);
                throw ConsumeAccountException.concurrentModification(accountId);
            }

            log.info("[账户管理] 余额扣减成功: accountId={}, amount={}, newBalance={}", accountId, amount, newBalance);
            return true;

        } catch (ConsumeAccountException e) {
            // 业务异常直接抛出
            throw e;

        } catch (Exception e) {
            log.error("[账户管理] 余额扣减异常: accountId={}, amount={}", accountId, amount, e);
            throw new ConsumeAccountException("DEDUCT_AMOUNT_FAILED", "余额扣减失败: " + e.getMessage(), accountId);
        }
    }

    /**
     * 获取账户统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getAccountStatistics() {
        log.info("[账户管理] 获取账户统计信息");

        try {
            Map<String, Object> statistics = consumeAccountDao.getAccountStatistics();
            log.info("[账户管理] 账户统计信息获取成功, statistics={}", statistics);
            return statistics;

        } catch (Exception e) {
            log.error("[账户管理] 获取账户统计信息异常", e);
            return Map.of();
        }
    }
}