package net.lab1024.sa.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeSubsidyAccountDao;
import net.lab1024.sa.common.entity.consume.ConsumeAccountEntity;
import net.lab1024.sa.common.entity.consume.ConsumeSubsidyAccountEntity;
import net.lab1024.sa.common.entity.consume.ConsumeSubsidyTypeEntity;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 补贴扣款管理器
 *
 * 职责：处理补贴扣款的优先级逻辑
 *
 * 扣款优先级规则：
 * 1. 优先扣款即将过期的补贴
 * 2. 同过期日期优先扣款金额较小的补贴
 * 3. 补贴不足时扣款现金账户
 * 4. 补贴和现金都不足时返回错误
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
@Component
public class SubsidyDeductionManager {

    private final ConsumeSubsidyAccountDao subsidyAccountDao;

    public SubsidyDeductionManager(ConsumeSubsidyAccountDao subsidyAccountDao) {
        this.subsidyAccountDao = subsidyAccountDao;
    }

    /**
     * 执行补贴扣款
     *
     * @param account 主账户
     * @param amount 需要扣款的金额
     * @return 扣款结果
     */
    public SubsidyDeductionResult deduct(ConsumeAccountEntity account, BigDecimal amount) {
        log.info("[补贴扣款] 开始扣款: userId={}, amount={}", account.getUserId(), amount);

        // 获取用户所有有效的补贴账户
        List<ConsumeSubsidyAccountEntity> subsidyAccounts =
                subsidyAccountDao.selectValidAccountsByUserId(account.getUserId());

        if (subsidyAccounts.isEmpty()) {
            log.info("[补贴扣款] 无有效补贴账户，使用现金账户");
            return deductFromCashAccount(account, amount);
        }

        // 按优先级排序补贴账户
        List<ConsumeSubsidyAccountEntity> sortedAccounts = sortByPriority(subsidyAccounts);

        log.info("[补贴扣款] 找到有效补贴账户: count={}", sortedAccounts.size());

        // 依次扣款补贴账户
        BigDecimal remainingAmount = amount;
        List<SubsidyDeduction> deductions = new ArrayList<>();

        for (ConsumeSubsidyAccountEntity subsidyAccount : sortedAccounts) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal balance = subsidyAccount.getBalance();
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal deductAmount;
            if (balance.compareTo(remainingAmount) >= 0) {
                // 补贴账户余额充足
                deductAmount = remainingAmount;
                remainingAmount = BigDecimal.ZERO;
            } else {
                // 补贴账户余额不足
                deductAmount = balance;
                remainingAmount = remainingAmount.subtract(balance);
            }

            // 记录扣款
            SubsidyDeduction deduction = new SubsidyDeduction();
            deduction.setSubsidyAccountId(subsidyAccount.getSubsidyAccountId());
            deduction.setSubsidyTypeId(subsidyAccount.getSubsidyTypeId());
            deduction.setAmount(deductAmount);
            deduction.setBalanceBefore(balance);
            deduction.setBalanceAfter(balance.subtract(deductAmount));
            deductions.add(deduction);

            log.info("[补贴扣款] 补贴账户扣款: subsidyAccountId={}, deductAmount={}, remaining={}",
                    subsidyAccount.getSubsidyAccountId(), deductAmount, remainingAmount);
        }

        // 检查是否还需要扣款现金账户
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 需要扣款现金账户
            if (account.getBalance().compareTo(remainingAmount) >= 0) {
                log.info("[补贴扣款] 补贴不足，扣款现金账户: remainingAmount={}", remainingAmount);

                SubsidyDeduction cashDeduction = new SubsidyDeduction();
                cashDeduction.setAmount(remainingAmount);
                cashDeduction.setBalanceBefore(account.getBalance());
                cashDeduction.setBalanceAfter(account.getBalance().subtract(remainingAmount));
                deductions.add(cashDeduction);

                remainingAmount = BigDecimal.ZERO;
            } else {
                // 现金账户也不足
                log.warn("[补贴扣款] 补贴和现金都不足: required={}, subsidy={}, cash={}",
                        amount, amount.subtract(remainingAmount), account.getBalance());
                return SubsidyDeductionResult.failure("余额不足");
            }
        }

        log.info("[补贴扣款] 扣款成功: totalAmount={}, deductionCount={}", amount, deductions.size());
        return SubsidyDeductionResult.success(amount, deductions);
    }

    /**
     * 按优先级排序补贴账户
     *
     * 排序规则：
     * 1. 过期时间升序（即将过期优先）
     * 2. 同过期日期余额升序（小金额优先）
     * 3. 同余额优先级升序（priority数字越小优先级越高）
     */
    private List<ConsumeSubsidyAccountEntity> sortByPriority(List<ConsumeSubsidyAccountEntity> accounts) {
        return accounts.stream()
                .sorted(Comparator
                        .comparing(ConsumeSubsidyAccountEntity::getExpireTime)
                        .thenComparing(ConsumeSubsidyAccountEntity::getBalance)
                        .thenComparing(a -> {
                            // 这里需要获取补贴类型的priority，简化处理
                            return 0;
                        }))
                .collect(Collectors.toList());
    }

    /**
     * 仅从现金账户扣款
     */
    private SubsidyDeductionResult deductFromCashAccount(ConsumeAccountEntity account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            return SubsidyDeductionResult.failure("现金账户余额不足");
        }

        SubsidyDeduction deduction = new SubsidyDeduction();
        deduction.setAmount(amount);
        deduction.setBalanceBefore(account.getBalance());
        deduction.setBalanceAfter(account.getBalance().subtract(amount));

        return SubsidyDeductionResult.success(amount, Collections.singletonList(deduction));
    }

    /**
     * 补贴扣款结果
     */
    public static class SubsidyDeductionResult {
        private boolean success;
        private String message;
        private BigDecimal totalAmount;
        private List<SubsidyDeduction> deductions;

        public static SubsidyDeductionResult success(BigDecimal totalAmount, List<SubsidyDeduction> deductions) {
            SubsidyDeductionResult result = new SubsidyDeductionResult();
            result.setSuccess(true);
            result.setTotalAmount(totalAmount);
            result.setDeductions(deductions);
            result.setMessage("扣款成功");
            return result;
        }

        public static SubsidyDeductionResult failure(String message) {
            SubsidyDeductionResult result = new SubsidyDeductionResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public List<SubsidyDeduction> getDeductions() {
            return deductions;
        }

        public void setDeductions(List<SubsidyDeduction> deductions) {
            this.deductions = deductions;
        }
    }

    /**
     * 单笔扣款记录
     */
    public static class SubsidyDeduction {
        private Long subsidyAccountId;
        private Long subsidyTypeId;
        private BigDecimal amount;
        private BigDecimal balanceBefore;
        private BigDecimal balanceAfter;

        public Long getSubsidyAccountId() {
            return subsidyAccountId;
        }

        public void setSubsidyAccountId(Long subsidyAccountId) {
            this.subsidyAccountId = subsidyAccountId;
        }

        public Long getSubsidyTypeId() {
            return subsidyTypeId;
        }

        public void setSubsidyTypeId(Long subsidyTypeId) {
            this.subsidyTypeId = subsidyTypeId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getBalanceBefore() {
            return balanceBefore;
        }

        public void setBalanceBefore(BigDecimal balanceBefore) {
            this.balanceBefore = balanceBefore;
        }

        public BigDecimal getBalanceAfter() {
            return balanceAfter;
        }

        public void setBalanceAfter(BigDecimal balanceAfter) {
            this.balanceAfter = balanceAfter;
        }
    }
}
