package net.lab1024.sa.consume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.QueryBuilder;
import net.lab1024.sa.consume.client.AccountServiceClient;
import net.lab1024.sa.consume.client.dto.BalanceChangeResult;
import net.lab1024.sa.consume.client.dto.BalanceDecreaseRequest;
import net.lab1024.sa.consume.client.dto.BalanceIncreaseRequest;
import net.lab1024.sa.consume.dao.ConsumeAccountDao;
import net.lab1024.sa.consume.dao.ConsumeAccountTransactionDao;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.common.entity.consume.ConsumeAccountEntity;
import net.lab1024.sa.common.entity.consume.ConsumeAccountTransactionEntity;
import net.lab1024.sa.consume.domain.form.ConsumeAccountAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountRechargeForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeAccountVO;
import net.lab1024.sa.consume.exception.ConsumeAccountException;
import net.lab1024.sa.consume.manager.ConsumeAccountManager;
import net.lab1024.sa.consume.service.ConsumeAccountService;

/**
 * 消费账户服务实现
 * <p>
 * 提供消费账户的完整业务功能实现，包括：
 * - 在线消费余额扣减（实时调用账户服务）
 * - 退款余额增加（实时调用账户服务）
 * - 离线消费补偿（异步同步）
 * - 账户管理功能（查询、创建、更新等）
 * </p>
 *
 * <p>技术特性：</p>
 * <ul>
 *   <li>使用OpenFeign调用账户服务进行余额操作</li>
 *   <li>集成Seata分布式事务保证数据一致性</li>
 *   <li>提供降级策略（账户服务不可用时使用本地补偿）</li>
 *   <li>完整的事务审计日志</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class ConsumeAccountServiceImpl implements ConsumeAccountService {

    private final ConsumeAccountManager accountManager;
    private final ConsumeAccountDao accountDao;
    private final ConsumeAccountTransactionDao transactionDao;
    private final ConsumeRecordDao recordDao;
    private final AccountServiceClient accountServiceClient;

    /**
     * 构造函数注入依赖
     */
    public ConsumeAccountServiceImpl(ConsumeAccountManager accountManager,
                                     ConsumeAccountDao accountDao,
                                     ConsumeAccountTransactionDao transactionDao,
                                     ConsumeRecordDao recordDao,
                                     AccountServiceClient accountServiceClient) {
        this.accountManager = accountManager;
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
        this.recordDao = recordDao;
        this.accountServiceClient = accountServiceClient;
    }

    @Override
    public PageResult<ConsumeAccountVO> queryAccounts(ConsumeAccountQueryForm queryForm) {
        log.info("[账户服务] 分页查询账户列表: queryForm={}", queryForm);

        try {
            // 使用MyBatis-Plus分页查询
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<ConsumeAccountVO> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
                );

            com.baomidou.mybatisplus.core.metadata.IPage<ConsumeAccountVO> pageResult =
                accountDao.selectPage(page, queryForm);

            // 转换为PageResult
            PageResult<ConsumeAccountVO> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum(queryForm.getPageNum());
            result.setPageSize(queryForm.getPageSize());
            result.setPages((int) pageResult.getPages());

            return result;
        } catch (Exception e) {
            log.error("[账户服务] 分页查询失败", e);
            throw ConsumeAccountException.queryFailed("查询账户列表失败: " + e.getMessage());
        }
    }

    @Override
    public ConsumeAccountVO getAccountDetail(Long accountId) {
        log.info("[账户服务] 获取账户详情: accountId={}", accountId);

        try {
            return accountManager.getAccountDetail(accountId);
        } catch (Exception e) {
            log.error("[账户服务] 获取账户详情失败: accountId={}", accountId, e);
            throw ConsumeAccountException.queryFailed("获取账户详情失败: " + e.getMessage());
        }
    }

    @Override
    public ConsumeAccountVO getAccountByUserId(Long userId) {
        log.info("[账户服务] 根据用户ID获取账户: userId={}", userId);

        try {
            return accountManager.getAccountByUserId(userId);
        } catch (Exception e) {
            log.error("[账户服务] 获取用户账户失败: userId={}", userId, e);
            throw ConsumeAccountException.queryFailed("获取用户账户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAccount(ConsumeAccountAddForm addForm) {
        log.info("[账户服务] 创建账户: userId={}, username={}", addForm.getUserId(), addForm.getUsername());

        try {
            // 1. 验证用户是否已有账户
            ConsumeAccountVO existingAccount = accountDao.selectByUserId(addForm.getUserId());
            if (existingAccount != null) {
                log.warn("[账户服务] 用户已有账户: userId={}", addForm.getUserId());
                throw ConsumeAccountException.accountAlreadyExists("用户已有账户，无法重复创建");
            }

            // 2. 创建账户实体
            ConsumeAccountEntity account = new ConsumeAccountEntity();
            account.setUserId(addForm.getUserId());

            // 生成账户编码
            String accountCode = "ACC_" + System.currentTimeMillis();
            account.setAccountCode(accountCode);

            // 转换账户类型：String -> Integer
            Integer accountType = convertAccountType(addForm.getAccountType());
            account.setAccountType(accountType);

            // 使用username作为账户名称
            account.setAccountName(addForm.getUsername());

            account.setBalance(addForm.getInitialBalance() != null ? addForm.getInitialBalance() : BigDecimal.ZERO);
            account.setFrozenAmount(BigDecimal.ZERO);
            account.setCreditLimit(addForm.getCreditLimit() != null ? addForm.getCreditLimit() : BigDecimal.ZERO);
            account.setTotalRecharge(BigDecimal.ZERO);
            account.setTotalConsume(BigDecimal.ZERO);
            account.setAccountStatus(1); // 正常状态

            // 3. 插入数据库
            accountDao.insert(account);

            log.info("[账户服务] 账户创建成功: accountId={}, accountCode={}", account.getAccountId(), accountCode);
            return account.getAccountId();

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[账户服务] 创建账户失败", e);
            throw ConsumeAccountException.createFailed("创建账户失败: " + e.getMessage());
        }
    }

    /**
     * 转换账户类型字符串为整数
     */
    private Integer convertAccountType(String accountTypeStr) {
        if (accountTypeStr == null) {
            return 1; // 默认员工账户
        }
        return switch (accountTypeStr.toUpperCase()) {
            case "STAFF" -> 1;
            case "STUDENT" -> 2;
            case "VISITOR" -> 3;
            default -> 1;
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccount(Long accountId, ConsumeAccountUpdateForm updateForm) {
        log.info("[账户服务] 更新账户信息: accountId={}", accountId);

        try {
            // 1. 查询账户
            ConsumeAccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw ConsumeAccountException.accountNotFound("账户不存在");
            }

            // 2. 更新字段（只更新Entity中存在的字段）
            if (updateForm.getCreditLimit() != null) {
                account.setCreditLimit(updateForm.getCreditLimit());
            }

            // 3. 保存更新
            accountDao.updateById(account);

            log.info("[账户服务] 账户信息更新成功: accountId={}", accountId);

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[账户服务] 更新账户失败: accountId={}", accountId, e);
            throw ConsumeAccountException.updateFailed("更新账户失败: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional(name = "recharge-account", rollbackFor = Exception.class)
    public Boolean rechargeAccount(Long accountId, ConsumeAccountRechargeForm rechargeForm) {
        log.info("[账户服务] 账户充值: accountId={}, amount={}", accountId, rechargeForm.getAmount());

        try {
            // 1. 查询账户
            ConsumeAccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw ConsumeAccountException.accountNotFound("账户不存在");
            }

            // 2. 调用账户服务增加余额
            String businessNo = "RECHARGE-" + System.currentTimeMillis() + "-" + accountId;
            BalanceIncreaseRequest request = new BalanceIncreaseRequest();
            request.setUserId(account.getUserId());
            request.setAmount(rechargeForm.getAmount());
            request.setBusinessType("RECHARGE");
            request.setBusinessNo(businessNo);
            request.setRemark(rechargeForm.getRemark());

            ResponseDTO<BalanceChangeResult> response = accountServiceClient.increaseBalance(request);

            if (response == null || !response.isSuccess()) {
                log.error("[账户服务] 调用账户服务失败: accountId={}, response={}", accountId, response);
                throw ConsumeAccountException.rechargeFailed(accountId, "充值失败: " + (response != null ? response.getMessage() : "账户服务无响应"));
            }

            BalanceChangeResult result = response.getData();
            if (result == null || !result.getSuccess()) {
                log.error("[账户服务] 账户服务返回失败: accountId={}, result={}", accountId, result);
                throw ConsumeAccountException.rechargeFailed(accountId, "充值失败: " + (result != null ? result.getErrorMessage() : "未知错误"));
            }

            log.info("[账户服务] 账户充值成功: accountId={}, balanceBefore={}, balanceAfter={}",
                    accountId, result.getBalanceBefore(), result.getBalanceAfter());
            return true;

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[账户服务] 充值异常: accountId={}", accountId, e);
            throw ConsumeAccountException.rechargeFailed(accountId, "充值异常: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional(name = "deduct-amount", rollbackFor = Exception.class)
    public Boolean deductAmount(Long accountId, BigDecimal amount, String description) {
        log.info("[账户服务] 扣减账户余额: accountId={}, amount={}, description={}", accountId, amount, description);

        try {
            // 1. 查询账户
            ConsumeAccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw ConsumeAccountException.accountNotFound("账户不存在");
            }

            // 2. 调用账户服务扣减余额（核心功能：在线消费）
            String businessNo = "CONSUME-" + System.currentTimeMillis() + "-" + accountId;
            BalanceDecreaseRequest request = new BalanceDecreaseRequest();
            request.setUserId(account.getUserId());
            request.setAmount(amount);
            request.setBusinessType("CONSUME");
            request.setBusinessNo(businessNo);
            request.setRemark(description);
            request.setCheckBalance(true); // 检查余额是否充足

            log.info("[账户服务] 调用账户服务扣减余额: userId={}, amount={}, businessNo={}",
                    account.getUserId(), amount, businessNo);

            ResponseDTO<BalanceChangeResult> response = accountServiceClient.decreaseBalance(request);

            if (response == null || !response.isSuccess()) {
                log.error("[账户服务] 调用账户服务失败: accountId={}, response={}", accountId, response);
                throw ConsumeAccountException.deductFailed("扣款失败: " + (response != null ? response.getMessage() : "账户服务无响应"));
            }

            BalanceChangeResult result = response.getData();
            if (result == null || !result.getSuccess()) {
                log.error("[账户服务] 账户服务返回失败: accountId={}, result={}", accountId, result);
                throw ConsumeAccountException.deductFailed("扣款失败: " + (result != null ? result.getErrorMessage() : "未知错误"));
            }

            log.info("[账户服务] 账户余额扣减成功: accountId={}, balanceBefore={}, balanceAfter={}, transactionId={}",
                    accountId, result.getBalanceBefore(), result.getBalanceAfter(), result.getTransactionId());
            return true;

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[账户服务] 扣减余额异常: accountId={}", accountId, e);
            throw ConsumeAccountException.deductFailed("扣减异常: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional(name = "refund-amount", rollbackFor = Exception.class)
    public Boolean refundAmount(Long accountId, BigDecimal amount, String reason) {
        log.info("[账户服务] 账户退款: accountId={}, amount={}, reason={}", accountId, amount, reason);

        try {
            // 1. 查询账户
            ConsumeAccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw ConsumeAccountException.accountNotFound("账户不存在");
            }

            // 2. 调用账户服务增加余额（退款）
            String businessNo = "REFUND-" + System.currentTimeMillis() + "-" + accountId;
            BalanceIncreaseRequest request = new BalanceIncreaseRequest();
            request.setUserId(account.getUserId());
            request.setAmount(amount);
            request.setBusinessType("REFUND");
            request.setBusinessNo(businessNo);
            request.setRemark(reason);

            log.info("[账户服务] 调用账户服务退款: userId={}, amount={}, businessNo={}",
                    account.getUserId(), amount, businessNo);

            ResponseDTO<BalanceChangeResult> response = accountServiceClient.increaseBalance(request);

            if (response == null || !response.isSuccess()) {
                log.error("[账户服务] 调用账户服务失败: accountId={}, response={}", accountId, response);
                throw ConsumeAccountException.refundFailed("退款失败: " + (response != null ? response.getMessage() : "账户服务无响应"));
            }

            BalanceChangeResult result = response.getData();
            if (result == null || !result.getSuccess()) {
                log.error("[账户服务] 账户服务返回失败: accountId={}, result={}", accountId, result);
                throw ConsumeAccountException.refundFailed("退款失败: " + (result != null ? result.getErrorMessage() : "未知错误"));
            }

            log.info("[账户服务] 账户退款成功: accountId={}, balanceBefore={}, balanceAfter={}",
                    accountId, result.getBalanceBefore(), result.getBalanceAfter());
            return true;

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[账户服务] 退款异常: accountId={}", accountId, e);
            throw ConsumeAccountException.refundFailed("退款异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeAccount(Long accountId, String reason) {
        log.info("[账户服务] 冻结账户: accountId={}, reason={}", accountId, reason);

        try {
            ConsumeAccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw ConsumeAccountException.accountNotFound("账户不存在");
            }

            account.setAccountStatus(0); // 冻结状态
            accountDao.updateById(account);

            log.info("[账户服务] 账户冻结成功: accountId={}", accountId);

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[账户服务] 冻结账户失败: accountId={}", accountId, e);
            throw ConsumeAccountException.updateFailed("冻结账户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeAccount(Long accountId, String reason) {
        log.info("[账户服务] 解冻账户: accountId={}, reason={}", accountId, reason);

        try {
            ConsumeAccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw ConsumeAccountException.accountNotFound("账户不存在");
            }

            account.setAccountStatus(1); // 正常状态
            accountDao.updateById(account);

            log.info("[账户服务] 账户解冻成功: accountId={}", accountId);

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[账户服务] 解冻账户失败: accountId={}", accountId, e);
            throw ConsumeAccountException.updateFailed("解冻账户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeAccount(Long accountId, String reason) {
        log.info("[账户服务] 注销账户: accountId={}, reason={}", accountId, reason);

        try {
            ConsumeAccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw ConsumeAccountException.accountNotFound("账户不存在");
            }

            // 检查余额
            if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                throw ConsumeAccountException.closeFailed("账户余额不为零，无法注销");
            }

            account.setAccountStatus(2); // 注销状态
            accountDao.updateById(account);

            log.info("[账户服务] 账户注销成功: accountId={}", accountId);

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[账户服务] 注销账户失败: accountId={}", accountId, e);
            throw ConsumeAccountException.closeFailed("注销账户失败: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal getAccountBalance(Long accountId) {
        log.info("[账户服务] 查询账户余额: accountId={}", accountId);

        try {
            ConsumeAccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw ConsumeAccountException.accountNotFound("账户不存在");
            }

            log.info("[账户服务] 账户余额查询成功: accountId={}, balance={}", accountId, account.getBalance());
            return account.getBalance();

        } catch (ConsumeAccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("[账户服务] 查询账户余额失败: accountId={}", accountId, e);
            throw ConsumeAccountException.queryFailed("查询余额失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserConsumeStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("[账户服务] 获取用户消费统计: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        try {
            // 使用MyBatis-Plus查询用户在指定时间范围的消费记录
            LambdaQueryWrapper<ConsumeAccountTransactionEntity> queryWrapper = QueryBuilder.of(ConsumeAccountTransactionEntity.class)
                .eq(ConsumeAccountTransactionEntity::getUserId, userId)
                .ge(ConsumeAccountTransactionEntity::getTransactionTime, startDate)
                .le(ConsumeAccountTransactionEntity::getTransactionTime, endDate)
                .orderByDesc(ConsumeAccountTransactionEntity::getTransactionTime)
                .build();

            List<ConsumeAccountTransactionEntity> transactions = transactionDao.selectList(queryWrapper);

            // 构建统计结果
            Map<String, Object> statistics = new HashMap<>();

            java.math.BigDecimal totalRecharge = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalConsume = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalRefund = java.math.BigDecimal.ZERO;
            int transactionCount = transactions.size();

            for (ConsumeAccountTransactionEntity transaction : transactions) {
                if ("RECHARGE".equals(transaction.getTransactionType()) && transaction.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    totalRecharge = totalRecharge.add(transaction.getAmount());
                } else if ("CONSUME".equals(transaction.getTransactionType()) || "DEDUCT".equals(transaction.getTransactionType())) {
                    totalConsume = totalConsume.add(transaction.getAmount().abs());
                } else if ("REFUND".equals(transaction.getTransactionType())) {
                    totalRefund = totalRefund.add(transaction.getAmount());
                }
            }

            statistics.put("userId", userId);
            statistics.put("startDate", startDate);
            statistics.put("endDate", endDate);
            statistics.put("transactionCount", transactionCount);
            statistics.put("totalRecharge", totalRecharge);
            statistics.put("totalConsume", totalConsume);
            statistics.put("totalRefund", totalRefund);
            statistics.put("netAmount", totalRecharge.subtract(totalConsume).add(totalRefund));

            log.info("[账户服务] 消费统计查询成功: userId={}, transactionCount={}", userId, transactionCount);
            return statistics;

        } catch (Exception e) {
            log.error("[账户服务] 获取消费统计失败", e);
            throw ConsumeAccountException.queryFailed("获取消费统计失败: " + e.getMessage());
        }
    }

    @Override
    public List<ConsumeAccountVO> getActiveAccounts() {
        log.info("[账户服务] 获取活跃账户列表");

        try {
            // 使用MyBatis-Plus查询状态为1（正常）的账户
            LambdaQueryWrapper<ConsumeAccountEntity> queryWrapper = QueryBuilder.of(ConsumeAccountEntity.class)
                .eq(ConsumeAccountEntity::getStatus, 1)
                .build();

            List<ConsumeAccountEntity> entities = accountDao.selectList(queryWrapper);

            // 转换为VO
            return entities.stream()
                    .map(this::convertToVO)
                    .toList();
        } catch (Exception e) {
            log.error("[账户服务] 获取活跃账户失败", e);
            throw ConsumeAccountException.queryFailed("获取活跃账户失败: " + e.getMessage());
        }
    }

    /**
     * 将Entity转换为VO
     */
    private ConsumeAccountVO convertToVO(ConsumeAccountEntity entity) {
        return ConsumeAccountVO.builder()
                .accountId(entity.getAccountId())
                .userId(entity.getUserId())
                .balance(entity.getBalance())
                .creditLimit(entity.getCreditLimit())
                .availableLimit(entity.getBalance().add(entity.getCreditLimit()))
                .status(entity.getAccountStatus() == 1 ? "ACTIVE" : "FROZEN")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchCreateAccounts(List<ConsumeAccountAddForm> addForms) {
        log.info("[账户服务] 批量创建账户: count={}", addForms.size());

        int successCount = 0;
        int failCount = 0;

        for (ConsumeAccountAddForm addForm : addForms) {
            try {
                createAccount(addForm);
                successCount++;
            } catch (Exception e) {
                log.error("[账户服务] 批量创建失败: userId={}", addForm.getUserId(), e);
                failCount++;
            }
        }

        log.info("[账户服务] 批量创建完成: total={}, success={}, fail={}", addForms.size(), successCount, failCount);

        return Map.of(
            "total", addForms.size(),
            "successCount", successCount,
            "failCount", failCount
        );
    }
}
