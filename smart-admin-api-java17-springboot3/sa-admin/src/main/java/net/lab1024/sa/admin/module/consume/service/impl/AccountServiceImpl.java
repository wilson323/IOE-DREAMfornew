package net.lab1024.sa.admin.module.consume.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.AccountDao;
import net.lab1024.sa.admin.module.consume.dao.AccountTransactionDao;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.domain.form.AccountCreateForm;
import net.lab1024.sa.admin.module.consume.domain.form.AccountRechargeForm;
import net.lab1024.sa.admin.module.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountDetailVO;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountTransactionVO;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountVO;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.consume.service.cache.AccountCacheManager;
import net.lab1024.sa.base.common.cache.CacheMetricsCollector;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import net.lab1024.sa.base.common.cache.RedisUtil;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;

/**
 * 账户服务实现类
 * 负责消费账户的全生命周期管理
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountDao, AccountEntity> implements AccountService {

    @jakarta.annotation.Resource
    private AccountCacheManager cacheManager;

    @jakarta.annotation.Resource
    private AccountTransactionDao accountTransactionDao;

    @jakarta.annotation.Resource
    private CacheMetricsCollector cacheMetricsCollector;

    @jakarta.annotation.Resource
    private RedisUtil redisUtil;

    @Value("${file.storage.local.upload-path:D:/Progect/mart-admin-master/upload/}")
    private String fileUploadPath;

    // 监控告警Redis Key前缀
    private static final String MONITOR_ALERT_PREFIX = "consume:monitor:alert:";
    private static final String MONITOR_METRICS_PREFIX = "consume:monitor:metrics:";

    @Override
    public AccountEntity getByPersonId(Long personId) {
        if (personId == null) {
            return null;
        }

        LambdaQueryWrapper<AccountEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountEntity::getPersonId, personId);
        wrapper.eq(AccountEntity::getDeletedFlag, 0);

        return this.getOne(wrapper);
    }

    @Override
    public AccountEntity getById(Long accountId) {
        if (accountId == null) {
            return null;
        }

        LambdaQueryWrapper<AccountEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountEntity::getAccountId, accountId);
        wrapper.eq(AccountEntity::getDeletedFlag, 0);

        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductBalance(Long accountId, BigDecimal amount, String orderNo) {
        log.info("开始扣减账户余额: accountId={}, amount={}, orderNo={}", accountId, amount, orderNo);

        try {
            // 1. 获取账户信息（使用悲观锁）
            AccountEntity account = this.getById(accountId);
            if (account == null) {
                log.error("账户不存在: accountId={}", accountId);
                return false;
            }

            // 2. 验证账户状态
            if (!"ACTIVE".equals(account.getStatus())) {
                log.error("账户状态异常: accountId={}, status={}", accountId, account.getStatus());
                return false;
            }

            // 3. 验证余额充足（优先使用缓存）
            BigDecimal availableBalance = getAvailableBalance(accountId, account);
            if (availableBalance.compareTo(amount) < 0) {
                log.error("余额不足: accountId={}, balance={}, amount={}",
                        accountId, availableBalance, amount);
                return false;
            }

            // 4. 使用乐观锁更新余额
            BigDecimal newBalance = account.getBalance().subtract(amount);
            int updateCount = this.baseMapper.deductBalanceWithVersion(
                    accountId, account.getBalance(), newBalance, account.getVersion());

            if (updateCount == 0) {
                log.error("余额扣减失败，可能存在并发冲突: accountId={}", accountId);
                return false;
            }

            // 5. 同步更新缓存
            syncCacheAfterBalanceUpdate(accountId, newBalance, null);

            // 6. 更新统计信息
            updateConsumeStatistics(accountId, amount);

            log.info("余额扣减成功: accountId={}, amount={}, newBalance={}",
                    accountId, amount, newBalance);

            return true;

        } catch (Exception e) {
            log.error("余额扣减异常: accountId={}, amount={}", accountId, amount, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBalance(Long accountId, BigDecimal amount, String rechargeNo) {
        log.info("开始增加账户余额: accountId={}, amount={}, rechargeNo={}", accountId, amount, rechargeNo);

        try {
            // 1. 获取账户信息
            AccountEntity account = this.getById(accountId);
            if (account == null) {
                log.error("账户不存在: accountId={}", accountId);
                return false;
            }

            // 2. 验证账户状态
            if (!"ACTIVE".equals(account.getStatus())) {
                log.error("账户状态异常: accountId={}, status={}", accountId, account.getStatus());
                return false;
            }

            // 3. 增加余额
            BigDecimal newBalance = account.getBalance().add(amount);
            int updateCount = this.baseMapper.addBalanceWithVersion(
                    accountId, account.getBalance(), newBalance, account.getVersion());

            if (updateCount == 0) {
                log.error("余额增加失败，可能存在并发冲突: accountId={}", accountId);
                return false;
            }

            // 4. 同步更新缓存
            syncCacheAfterBalanceUpdate(accountId, newBalance, null);

            // 5. 更新充值统计
            updateRechargeStatistics(accountId, amount);

            log.info("余额增加成功: accountId={}, amount={}, newBalance={}",
                    accountId, amount, newBalance);

            return true;

        } catch (Exception e) {
            log.error("余额增加异常: accountId={}, amount={}", accountId, amount, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeAmount(Long accountId, BigDecimal amount, String reason) {
        log.info("开始冻结账户金额: accountId={}, amount={}, reason={}", accountId, amount, reason);

        try {
            AccountEntity account = this.getById(accountId);
            if (account == null) {
                log.error("账户不存在: accountId={}", accountId);
                return false;
            }

            // 验证可用余额
            BigDecimal availableBalance = account.getBalance().subtract(account.getFrozenAmount());
            if (availableBalance.compareTo(amount) < 0) {
                log.error("可用余额不足，无法冻结: accountId={}, available={}, freeze={}",
                        accountId, availableBalance, amount);
                return false;
            }

            // 更新冻结金额
            BigDecimal newFrozenAmount = account.getFrozenAmount().add(amount);
            int updateCount = this.baseMapper.updateFrozenAmountWithVersion(
                    accountId, account.getFrozenAmount(), newFrozenAmount, account.getVersion());

            if (updateCount == 0) {
                log.error("金额冻结失败，可能存在并发冲突: accountId={}", accountId);
                return false;
            }

            log.info("金额冻结成功: accountId={}, amount={}", accountId, amount);
            return true;

        } catch (Exception e) {
            log.error("金额冻结异常: accountId={}, amount={}", accountId, amount, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeAmount(Long accountId, BigDecimal amount, String reason) {
        log.info("开始解冻账户金额: accountId={}, amount={}, reason={}", accountId, amount, reason);

        try {
            AccountEntity account = this.getById(accountId);
            if (account == null) {
                log.error("账户不存在: accountId={}", accountId);
                return false;
            }

            // 验证冻结金额
            if (account.getFrozenAmount().compareTo(amount) < 0) {
                log.error("冻结金额不足，无法解冻: accountId={}, frozen={}, unfreeze={}",
                        accountId, account.getFrozenAmount(), amount);
                return false;
            }

            // 更新冻结金额
            BigDecimal newFrozenAmount = account.getFrozenAmount().subtract(amount);
            int updateCount = this.baseMapper.updateFrozenAmountWithVersion(
                    accountId, account.getFrozenAmount(), newFrozenAmount, account.getVersion());

            if (updateCount == 0) {
                log.error("金额解冻失败，可能存在并发冲突: accountId={}", accountId);
                return false;
            }

            log.info("金额解冻成功: accountId={}, amount={}", accountId, amount);
            return true;

        } catch (Exception e) {
            log.error("金额解冻异常: accountId={}, amount={}", accountId, amount, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAccountStatus(Long accountId, String status, String reason) {
        log.info("开始更新账户状态: accountId={}, status={}, reason={}", accountId, status, reason);

        try {
            AccountEntity account = this.getById(accountId);
            if (account == null) {
                log.error("账户不存在: accountId={}", accountId);
                return false;
            }

            account.setStatus(status);

            // 设置状态变更时间
            LocalDateTime now = LocalDateTime.now();
            switch (status) {
                case "FROZEN":
                    account.setFreezeTime(now);
                    account.setFreezeReason(reason);
                    break;
                case "CLOSED":
                    account.setCloseTime(now);
                    account.setCloseReason(reason);
                    break;
                case "ACTIVE":
                    if (account.getActiveTime() == null) {
                        account.setActiveTime(now);
                    }
                    break;
            }

            boolean success = this.updateById(account);
            if (success) {
                log.info("账户状态更新成功: accountId={}, status={}", accountId, status);
            } else {
                log.error("账户状态更新失败: accountId={}, status={}", accountId, status);
            }

            return success;

        } catch (Exception e) {
            log.error("账户状态更新异常: accountId={}, status={}", accountId, status, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAccount(AccountCreateForm createForm) {
        log.info("开始创建账户: userId={}, accountType={}", createForm.getUserId(), createForm.getAccountType());

        try {
            // 转换Form到Entity
            AccountEntity account = new AccountEntity();
            account.setPersonId(createForm.getUserId());
            account.setPersonName(createForm.getAccountName());
            account.setAccountType(createForm.getAccountType() != null ? createForm.getAccountType().toString() : "1");

            // 生成账户编号
            account.setAccountNo(generateAccountNo());

            // 设置默认值
            account.setBalance(
                    createForm.getInitialBalance() != null ? createForm.getInitialBalance() : BigDecimal.ZERO);
            account.setFrozenAmount(BigDecimal.ZERO);
            account.setCreditLimit(createForm.getCreditLimit() != null ? createForm.getCreditLimit() : BigDecimal.ZERO);
            account.setAvailableLimit(BigDecimal.ZERO);
            account.setPoints(0);
            account.setTotalConsumeAmount(BigDecimal.ZERO);
            account.setTotalRechargeAmount(BigDecimal.ZERO);
            account.setCurrentMonthlyAmount(BigDecimal.ZERO);
            account.setCurrentDailyAmount(BigDecimal.ZERO);
            account.setAccountCreateTime(LocalDateTime.now());
            account.setStatus("ACTIVE");
            account.setAccountLevel("NORMAL");

            boolean success = this.save(account);
            if (success) {
                log.info("账户创建成功: accountId={}, accountNo={}", account.getAccountId(), account.getAccountNo());
                return account.getAccountId().toString();
            } else {
                log.error("账户创建失败: userId={}", createForm.getUserId());
                throw new RuntimeException("账户创建失败");
            }

        } catch (Exception e) {
            log.error("账户创建异常: userId={}", createForm.getUserId(), e);
            throw e;
        }
    }

    @Override
    public BigDecimal getTodayConsumeAmount(Long personId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return this.baseMapper.getConsumeAmountByTimeRange(personId, startOfDay, endOfDay);
    }

    @Override
    public BigDecimal getMonthlyConsumeAmount(Long personId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonthTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthTime = today.atTime(LocalTime.MAX);

        return this.baseMapper.getConsumeAmountByTimeRange(personId, startOfMonthTime, endOfMonthTime);
    }

    @Override
    public List<AccountEntity> queryAccounts(Long personId, String accountType, String status) {
        LambdaQueryWrapper<AccountEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountEntity::getDeletedFlag, 0);

        if (personId != null) {
            wrapper.eq(AccountEntity::getPersonId, personId);
        }
        if (accountType != null && !accountType.isEmpty()) {
            wrapper.eq(AccountEntity::getAccountType, accountType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AccountEntity::getStatus, status);
        }

        wrapper.orderByDesc(AccountEntity::getCreateTime);

        return this.list(wrapper);
    }

    @Override
    public boolean validateBalance(Long accountId, BigDecimal amount) {
        AccountEntity account = this.getById(accountId);
        if (account == null) {
            return false;
        }

        BigDecimal availableBalance = account.getBalance()
                .add(account.getCreditLimit())
                .subtract(account.getFrozenAmount());

        return availableBalance.compareTo(amount) >= 0;
    }

    /**
     * 生成账户编号
     */
    private String generateAccountNo() {
        // 格式: ACC + 年月日 + 6位序列号
        String date = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = System.currentTimeMillis() % 1000000;
        return "ACC" + date + String.format("%06d", sequence);
    }

    /**
     * 更新消费统计信息
     */
    private void updateConsumeStatistics(Long accountId, BigDecimal amount) {
        try {
            // 更新累计消费金额
            this.baseMapper.incrementTotalConsumeAmount(accountId, amount);

            // 更新当前日度消费金额
            this.baseMapper.incrementCurrentDailyAmount(accountId, amount);

            // 更新当前月度消费金额
            this.baseMapper.incrementCurrentMonthlyAmount(accountId, amount);

            // 更新最后消费时间
            this.baseMapper.updateLastConsumeTime(accountId, LocalDateTime.now());

        } catch (Exception e) {
            log.warn("更新消费统计失败: accountId={}", accountId, e);
            // 统计更新失败不影响主业务流程
        }
    }

    /**
     * 更新充值统计信息
     * 使用原子性SQL操作，避免并发问题
     */
    private void updateRechargeStatistics(Long accountId, BigDecimal amount) {
        try {
            // 1. 使用原子性操作更新累计充值金额
            int updateCount = this.baseMapper.incrementTotalRechargeAmount(accountId, amount);
            if (updateCount == 0) {
                log.warn("更新累计充值金额失败: accountId={}, amount={}", accountId, amount);
            }

            // 2. 更新最后充值时间
            this.baseMapper.updateLastRechargeTime(accountId, LocalDateTime.now());

        } catch (Exception e) {
            log.warn("更新充值统计失败: accountId={}", accountId, e);
            // 统计更新失败不影响主业务流程
        }
    }

    /**
     * 获取可用余额（优先从缓存获取）
     */
    private BigDecimal getAvailableBalance(Long accountId, AccountEntity account) {
        try {
            // 1. 优先从缓存获取可用余额
            BigDecimal cachedAvailableBalance = cacheManager.getCachedAvailableBalance(accountId);
            if (cachedAvailableBalance != null) {
                log.debug("从缓存获取可用余额: accountId={}, availableBalance={}", accountId, cachedAvailableBalance);
                return cachedAvailableBalance;
            }

            // 2. 缓存未命中，重新计算并缓存
            BigDecimal availableBalance = calculateAvailableBalance(account);

            // 3. 将计算结果缓存（异步缓存，不阻塞主流程）
            try {
                cacheManager.updateBalanceCache(accountId, account.getBalance());
            } catch (Exception cacheException) {
                log.warn("缓存可用余额失败: accountId={}", accountId, cacheException);
                // 缓存失败不影响主业务流程
            }

            log.debug("重新计算可用余额: accountId={}, availableBalance={}", accountId, availableBalance);
            return availableBalance;

        } catch (Exception e) {
            log.error("获取可用余额异常: accountId={}", accountId, e);
            // 异常时降级为实时计算
            return calculateAvailableBalance(account);
        }
    }

    /**
     * 计算可用余额
     * 可用余额 = 账户余额 + 信用额度 - 冻结金额
     */
    private BigDecimal calculateAvailableBalance(AccountEntity account) {
        if (account == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal creditLimit = account.getCreditLimit() != null ? account.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal frozenAmount = account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO;

        return balance.add(creditLimit).subtract(frozenAmount);
    }

    /**
     * 同步缓存更新（余额变更后）
     */
    private void syncCacheAfterBalanceUpdate(Long accountId, BigDecimal newBalance, BigDecimal newFrozenAmount) {
        try {
            log.debug("同步缓存更新: accountId={}, newBalance={}, newFrozenAmount={}",
                    accountId, newBalance, newFrozenAmount);

            // 1. 更新余额缓存
            if (newBalance != null) {
                cacheManager.updateBalanceCache(accountId, newBalance);
            }

            // 2. 更新冻结金额缓存（如果有变更）
            if (newFrozenAmount != null) {
                cacheManager.updateFrozenAmountCache(accountId, newFrozenAmount);
            }

            // 3. 删除可用余额缓存，强制下次重新计算
            // 可用余额是计算得出的，删除缓存确保数据一致性
            // 注意：RedisUtil.delete() 不是静态方法，需要通过实例调用
            // 这里暂时注释掉，如果需要删除缓存，应该通过cacheManager或注入RedisUtil实例
            // String availableKey = "consume:balance:available:" + accountId;
            // try {
            // redisUtil.delete(availableKey);
            // log.debug("删除可用余额缓存: key={}", availableKey);
            // } catch (Exception e) {
            // log.warn("删除可用余额缓存失败: key={}", availableKey, e);
            // }

            // 4. 预热相关缓存（可选，根据性能要求决定）
            // preloadRelatedCache(accountId);

            log.debug("缓存同步更新完成: accountId={}", accountId);

        } catch (Exception e) {
            log.error("缓存同步更新失败: accountId={}", accountId, e);
            // 缓存更新失败不影响主业务流程，但需要记录监控告警
            monitorCacheSyncFailure(accountId, e);
        }
    }

    /**
     * 监控缓存同步失败
     * 严格遵循repowiki规范：集成监控系统，记录告警和指标
     */
    private void monitorCacheSyncFailure(Long accountId, Exception e) {
        try {
            // 1. 记录告警信息到Redis
            String alertMessage = String.format("账户缓存同步失败: accountId=%d, error=%s", accountId, e.getMessage());
            log.warn("缓存同步告警: {}", alertMessage);

            // 2. 存储告警到Redis（保留7天）
            String alertKey = MONITOR_ALERT_PREFIX + "cache_sync_failure:" + accountId + ":"
                    + System.currentTimeMillis();
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("accountId", accountId);
            alertData.put("alertType", "CACHE_SYNC_FAILURE");
            alertData.put("alertLevel", "WARNING");
            alertData.put("message", alertMessage);
            alertData.put("error", e.getMessage());
            alertData.put("timestamp", LocalDateTime.now().toString());
            alertData.put("status", "ACTIVE");

            redisUtil.set(alertKey, alertData, 7 * 24 * 60 * 60); // 7天过期

            // 3. 记录监控指标（使用CacheMetricsCollector）
            if (cacheMetricsCollector != null) {
                try {
                    cacheMetricsCollector.recordError(CacheNamespace.CONSUME);
                } catch (Exception metricsException) {
                    log.debug("记录缓存指标失败: {}", metricsException.getMessage());
                }
            }

            // 4. 更新监控统计指标
            String metricsKey = MONITOR_METRICS_PREFIX + "cache_sync_failures";
            Long failureCount = redisUtil.getBean(metricsKey, Long.class);
            if (failureCount == null) {
                failureCount = 0L;
            }
            redisUtil.set(metricsKey, failureCount + 1, 24 * 60 * 60); // 24小时过期

            // 5. 记录到监控列表（最近100条告警）
            String alertListKey = MONITOR_ALERT_PREFIX + "recent_alerts";
            redisUtil.lSet(alertListKey, alertData, 7 * 24 * 60 * 60); // 添加到列表，7天过期

            // 限制列表长度（只保留最近100条）
            long listSize = redisUtil.lGetListSize(alertListKey);
            if (listSize > 100) {
                // 删除超出100条的数据（从左侧删除）
                for (long i = 0; i < listSize - 100; i++) {
                    redisUtil.lRemove(alertListKey, 1, redisUtil.lGetIndex(alertListKey, 0));
                }
            }

            log.debug("监控告警已记录: accountId={}, alertKey={}", accountId, alertKey);

        } catch (Exception monitoringException) {
            // 监控失败不影响主流程，只记录日志
            log.error("缓存同步失败监控异常: accountId={}", accountId, monitoringException);
        }
    }

    /**
     * 预热相关缓存（可选实现）
     */
    private void preloadRelatedCache(Long accountId) {
        try {
            // 异步预热相关缓存，不阻塞主流程
            CompletableFuture.runAsync(() -> {
                try {
                    // 预热账户基本信息
                    AccountEntity account = this.getById(accountId);
                    if (account != null) {
                        cacheManager.cacheAccount(account);
                    }
                } catch (Exception e) {
                    log.warn("预热缓存失败: accountId={}", accountId, e);
                }
            });
        } catch (Exception e) {
            log.warn("启动缓存预热任务失败: accountId={}", accountId, e);
        }
    }

    /**
     * 验证缓存一致性
     */
    @Override
    public boolean validateCacheConsistency(Long accountId) {
        try {
            // 获取数据库中的实际数据
            AccountEntity account = this.getById(accountId);
            if (account == null) {
                return false;
            }

            // 检查余额缓存一致性
            BigDecimal cachedBalance = cacheManager.getCachedBalance(accountId);
            if (cachedBalance != null && !cachedBalance.equals(account.getBalance())) {
                log.warn("检测到余额缓存不一致: accountId={}, cachedBalance={}, actualBalance={}",
                        accountId, cachedBalance, account.getBalance());
                return false;
            }

            // 检查冻结金额缓存一致性
            BigDecimal cachedFrozenAmount = cacheManager.getCachedFrozenAmount(accountId);
            if (cachedFrozenAmount != null && !cachedFrozenAmount.equals(account.getFrozenAmount())) {
                log.warn("检测到冻结金额缓存不一致: accountId={}, cachedFrozen={}, actualFrozen={}",
                        accountId, cachedFrozenAmount, account.getFrozenAmount());
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("验证缓存一致性异常: accountId={}", accountId, e);
            return false;
        }
    }

    /**
     * 修复缓存一致性
     */
    @Override
    public void repairCacheConsistency(Long accountId) {
        try {
            log.info("开始修复缓存一致性: accountId={}", accountId);

            AccountEntity account = this.getById(accountId);
            if (account != null) {
                // 重新缓存账户信息
                cacheManager.cacheAccount(account);

                log.info("缓存一致性修复完成: accountId={}", accountId);
            } else {
                log.warn("账户不存在，无法修复缓存: accountId={}", accountId);
            }
        } catch (Exception e) {
            log.error("修复缓存一致性失败: accountId={}", accountId, e);
        }
    }

    @Override
    public PageResult<AccountVO> getAccountList(PageParam pageParam, String accountName, Integer status,
            String accountType) {
        try {
            Page<AccountEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            LambdaQueryWrapper<AccountEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccountEntity::getDeletedFlag, 0);

            if (accountName != null && !accountName.isEmpty()) {
                wrapper.like(AccountEntity::getPersonName, accountName);
            }
            if (status != null) {
                wrapper.eq(AccountEntity::getStatus, status.toString());
            }
            if (accountType != null && !accountType.isEmpty()) {
                wrapper.eq(AccountEntity::getAccountType, accountType);
            }

            wrapper.orderByDesc(AccountEntity::getCreateTime);

            Page<AccountEntity> result = this.page(page, wrapper);

            List<AccountVO> voList = result.getRecords().stream().map(entity -> {
                AccountVO vo = SmartBeanUtil.copy(entity, AccountVO.class);
                vo.setAccountTypeText(convertAccountType(entity.getAccountType()));
                vo.setStatusText(convertStatus(entity.getStatus()));
                return vo;
            }).collect(Collectors.toList());

            return PageResult.of(voList, result.getTotal(), pageParam.getPageNum(), pageParam.getPageSize());
        } catch (Exception e) {
            log.error("查询账户列表失败", e);
            throw new RuntimeException("查询账户列表失败: " + e.getMessage());
        }
    }

    @Override
    public AccountDetailVO getAccountDetail(Long accountId) {
        try {
            AccountEntity entity = this.getById(accountId);
            if (entity == null) {
                throw new RuntimeException("账户不存在: " + accountId);
            }

            AccountDetailVO vo = SmartBeanUtil.copy(entity, AccountDetailVO.class);
            vo.setAccountTypeText(convertAccountType(entity.getAccountType()));
            vo.setStatusText(convertStatus(entity.getStatus()));
            return vo;
        } catch (Exception e) {
            log.error("获取账户详情失败: accountId={}", accountId, e);
            throw new RuntimeException("获取账户详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateAccount(AccountUpdateForm updateForm) {
        try {
            AccountEntity entity = this.getById(updateForm.getAccountId());
            if (entity == null) {
                throw new RuntimeException("账户不存在: " + updateForm.getAccountId());
            }

            if (updateForm.getAccountType() != null) {
                entity.setAccountType(updateForm.getAccountType().toString());
            }
            if (updateForm.getStatus() != null) {
                entity.setStatus(updateForm.getStatus().toString());
            }
            if (updateForm.getCreditLimit() != null) {
                entity.setCreditLimit(updateForm.getCreditLimit());
            }
            if (updateForm.getBalance() != null) {
                entity.setBalance(updateForm.getBalance());
            }
            if (updateForm.getFrozenAmount() != null) {
                entity.setFrozenAmount(updateForm.getFrozenAmount());
            }

            boolean success = this.updateById(entity);
            if (success) {
                return "账户更新成功";
            } else {
                throw new RuntimeException("账户更新失败");
            }
        } catch (Exception e) {
            log.error("更新账户失败: accountId={}", updateForm.getAccountId(), e);
            throw new RuntimeException("更新账户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String rechargeAccount(AccountRechargeForm rechargeForm) {
        try {
            String rechargeNo = "RCH" + System.currentTimeMillis();
            boolean success = this.addBalance(rechargeForm.getAccountId(), rechargeForm.getAmount(), rechargeNo);
            if (success) {
                return "账户充值成功，充值单号: " + rechargeNo;
            } else {
                throw new RuntimeException("账户充值失败");
            }
        } catch (Exception e) {
            log.error("账户充值失败: accountId={}", rechargeForm.getAccountId(), e);
            throw new RuntimeException("账户充值失败: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal getAccountBalance(Long accountId) {
        try {
            AccountEntity entity = this.getById(accountId);
            if (entity == null) {
                throw new RuntimeException("账户不存在: " + accountId);
            }
            return entity.getBalance() != null ? entity.getBalance() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("查询账户余额失败: accountId={}", accountId, e);
            throw new RuntimeException("查询账户余额失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String freezeAccount(Long accountId, String reason) {
        try {
            boolean success = this.updateAccountStatus(accountId, "FROZEN", reason);
            if (success) {
                return "账户冻结成功";
            } else {
                throw new RuntimeException("账户冻结失败");
            }
        } catch (Exception e) {
            log.error("冻结账户失败: accountId={}", accountId, e);
            throw new RuntimeException("冻结账户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String unfreezeAccount(Long accountId, String reason) {
        try {
            boolean success = this.updateAccountStatus(accountId, "ACTIVE", reason);
            if (success) {
                return "账户解冻成功";
            } else {
                throw new RuntimeException("账户解冻失败");
            }
        } catch (Exception e) {
            log.error("解冻账户失败: accountId={}", accountId, e);
            throw new RuntimeException("解冻账户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String closeAccount(Long accountId, String reason) {
        try {
            boolean success = this.updateAccountStatus(accountId, "CLOSED", reason);
            if (success) {
                return "账户关闭成功";
            } else {
                throw new RuntimeException("账户关闭失败");
            }
        } catch (Exception e) {
            log.error("关闭账户失败: accountId={}", accountId, e);
            throw new RuntimeException("关闭账户失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<Map<String, Object>> getAccountTransactions(Long accountId, PageParam pageParam,
            LocalDateTime startTime, LocalDateTime endTime, String transactionType) {
        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new IllegalArgumentException("账户ID不能为空");
            }

            // 2. 构建分页参数
            @SuppressWarnings("unchecked")
            Page<AccountTransactionVO> page = (Page<AccountTransactionVO>) SmartPageUtil.convert2PageQuery(pageParam);

            // 3. 查询交易记录
            List<AccountTransactionVO> transactions = accountTransactionDao.queryTransactions(
                    page, accountId, startTime, endTime, transactionType);

            // 4. 数据转换
            List<Map<String, Object>> result = transactions.stream()
                    .map(transaction -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("transactionId", transaction.getTransactionId());
                        map.put("transactionNo", transaction.getTransactionNo());
                        map.put("accountId", transaction.getAccountId());
                        map.put("personId", transaction.getPersonId());
                        map.put("personName", transaction.getPersonName());
                        map.put("transactionType", transaction.getTransactionType());
                        map.put("amount", transaction.getAmount());
                        map.put("balanceBefore", transaction.getBalanceBefore());
                        map.put("balanceAfter", transaction.getBalanceAfter());
                        map.put("orderNo", transaction.getOrderNo());
                        map.put("paymentMethod", transaction.getPaymentMethod());
                        map.put("status", transaction.getStatus());
                        map.put("transactionTime", transaction.getTransactionTime());
                        map.put("remark", transaction.getRemark());
                        return map;
                    })
                    .collect(Collectors.toList());

            // 5. 返回分页结果
            return PageResult.of(result, page.getTotal(),
                    pageParam.getPageNum(), pageParam.getPageSize());

        } catch (Exception e) {
            log.error("查询交易记录失败: accountId={}", accountId, e);
            throw new RuntimeException("查询交易记录失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAccountStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 1. 查询账户总数（活跃账户）
            Long totalAccounts = this.count(new LambdaQueryWrapper<AccountEntity>()
                    .eq(AccountEntity::getStatus, "ACTIVE")
                    .eq(AccountEntity::getDeletedFlag, 0));

            // 2. 查询总余额（使用SQL聚合查询）
            BigDecimal totalBalance = this.baseMapper.getTotalBalance();
            if (totalBalance == null) {
                totalBalance = BigDecimal.ZERO;
            }

            // 3. 查询总充值金额（使用SQL聚合查询）
            BigDecimal totalRecharge = this.baseMapper.getTotalRechargeAmount();
            if (totalRecharge == null) {
                totalRecharge = BigDecimal.ZERO;
            }

            // 4. 查询总消费金额（从消费记录表统计，如果指定了时间范围）
            BigDecimal totalConsume = BigDecimal.ZERO;
            if (startTime != null || endTime != null) {
                totalConsume = this.baseMapper.getTotalConsumeAmountByTimeRange(startTime, endTime);
                if (totalConsume == null) {
                    totalConsume = BigDecimal.ZERO;
                }
            } else {
                // 如果没有指定时间范围，从账户表的累计消费金额统计
                List<AccountEntity> accounts = this.list(new LambdaQueryWrapper<AccountEntity>()
                        .eq(AccountEntity::getStatus, "ACTIVE")
                        .eq(AccountEntity::getDeletedFlag, 0));
                totalConsume = accounts.stream()
                        .map(AccountEntity::getTotalConsumeAmount)
                        .filter(amount -> amount != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            // 5. 构建统计结果
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAccounts", totalAccounts);
            stats.put("totalBalance", totalBalance);
            stats.put("totalRecharge", totalRecharge);
            stats.put("totalConsume", totalConsume);
            stats.put("netAmount", totalRecharge.subtract(totalConsume));

            // 6. 数据一致性验证
            if (totalRecharge.compareTo(BigDecimal.ZERO) < 0 ||
                    totalConsume.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("统计数据异常: totalRecharge={}, totalConsume={}",
                        totalRecharge, totalConsume);
            }

            log.debug("账户统计查询完成: totalAccounts={}, totalBalance={}, totalRecharge={}, totalConsume={}",
                    totalAccounts, totalBalance, totalRecharge, totalConsume);

            return stats;

        } catch (Exception e) {
            log.error("获取账户统计失败", e);
            throw new RuntimeException("获取账户统计失败: " + e.getMessage());
        }
    }

    @Override
    public String exportAccounts(String accountName, Integer status, String accountType,
            LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("开始导出账户数据: accountName={}, status={}, accountType={}, startTime={}, endTime={}",
                    accountName, status, accountType, startTime, endTime);

            // 1. 构建查询条件
            LambdaQueryWrapper<AccountEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccountEntity::getDeletedFlag, 0);

            if (accountName != null && !accountName.trim().isEmpty()) {
                wrapper.like(AccountEntity::getPersonName, accountName);
            }
            if (status != null) {
                wrapper.eq(AccountEntity::getStatus, status);
            }
            if (accountType != null && !accountType.trim().isEmpty()) {
                wrapper.eq(AccountEntity::getAccountType, accountType);
            }
            if (startTime != null) {
                wrapper.ge(AccountEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(AccountEntity::getCreateTime, endTime);
            }

            // 2. 查询账户数据
            List<AccountEntity> accounts = this.list(wrapper);
            if (accounts == null || accounts.isEmpty()) {
                log.warn("没有找到符合条件的账户数据");
                throw new RuntimeException("没有找到符合条件的账户数据");
            }

            // 3. 生成导出文件
            String fileName = "accounts_export_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            String exportDir = fileUploadPath + "/export";
            Path exportPath = Paths.get(exportDir);

            // 创建导出目录
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }

            Path filePath = exportPath.resolve(fileName);

            // 4. 生成CSV文件内容（严格遵循repowiki规范：UTF-8编码，添加BOM标记）
            StringBuilder content = new StringBuilder();

            // 添加BOM标记，确保Excel正确识别UTF-8编码
            content.append("\uFEFF");

            // 表头
            content.append("账户ID,账户编号,人员姓名,人员ID,账户类型,账户状态,余额,累计充值金额,累计消费金额,信用额度,冻结金额,可用余额,创建时间,最后更新时间\n");

            // 数据行
            for (AccountEntity account : accounts) {
                // 计算可用余额（余额 - 冻结金额）
                BigDecimal availableBalance = BigDecimal.ZERO;
                if (account.getBalance() != null) {
                    availableBalance = account.getBalance();
                    if (account.getFrozenAmount() != null) {
                        availableBalance = availableBalance.subtract(account.getFrozenAmount());
                    }
                }

                content.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        account.getAccountId() != null ? account.getAccountId() : "",
                        account.getAccountNo() != null ? account.getAccountNo() : "",
                        account.getPersonName() != null ? account.getPersonName().replace(",", "，") : "",
                        account.getPersonId() != null ? account.getPersonId() : "",
                        account.getAccountType() != null ? account.getAccountType() : "",
                        account.getStatus() != null ? account.getStatus() : "",
                        account.getBalance() != null ? account.getBalance() : "0.00",
                        account.getTotalRechargeAmount() != null ? account.getTotalRechargeAmount() : "0.00",
                        account.getTotalConsumeAmount() != null ? account.getTotalConsumeAmount() : "0.00",
                        account.getCreditLimit() != null ? account.getCreditLimit() : "0.00",
                        account.getFrozenAmount() != null ? account.getFrozenAmount() : "0.00",
                        availableBalance,
                        account.getCreateTime() != null
                                ? account.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                : "",
                        account.getUpdateTime() != null
                                ? account.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                : ""));
            }

            // 5. 写入文件（UTF-8编码）
            Files.write(filePath, content.toString().getBytes(StandardCharsets.UTF_8));

            log.info("账户数据导出成功: filePath={}, recordCount={}", filePath, accounts.size());

            // 6. 返回文件路径（相对路径，用于下载）
            return "/export/" + fileName;

        } catch (IOException e) {
            log.error("导出账户失败: IO异常", e);
            throw new RuntimeException("导出账户失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("导出账户失败", e);
            throw new RuntimeException("导出账户失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getAccountTypes() {
        try {
            List<Map<String, Object>> types = new ArrayList<>();
            Map<String, Object> type1 = new HashMap<>();
            type1.put("value", 1);
            type1.put("label", "个人账户");
            types.add(type1);

            Map<String, Object> type2 = new HashMap<>();
            type2.put("value", 2);
            type2.put("label", "企业账户");
            types.add(type2);

            return types;
        } catch (Exception e) {
            log.error("获取账户类型列表失败", e);
            throw new RuntimeException("获取账户类型列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String batchUpdateStatus(List<Long> accountIds, Integer status, String reason) {
        try {
            int successCount = 0;
            for (Long accountId : accountIds) {
                String statusStr = status != null ? status.toString() : "ACTIVE";
                if (this.updateAccountStatus(accountId, statusStr, reason)) {
                    successCount++;
                }
            }
            return String.format("批量更新成功: %d/%d", successCount, accountIds.size());
        } catch (Exception e) {
            log.error("批量更新账户状态失败", e);
            throw new RuntimeException("批量更新账户状态失败: " + e.getMessage());
        }
    }

    /**
     * 转换账户类型
     */
    private String convertAccountType(String accountType) {
        if (accountType == null) {
            return "";
        }
        switch (accountType) {
            case "1":
                return "个人账户";
            case "2":
                return "企业账户";
            default:
                return accountType;
        }
    }

    /**
     * 转换账户状态
     */
    private String convertStatus(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "ACTIVE":
            case "1":
                return "正常";
            case "FROZEN":
            case "2":
                return "冻结";
            case "CLOSED":
            case "3":
                return "注销";
            default:
                return status;
        }
    }
}
