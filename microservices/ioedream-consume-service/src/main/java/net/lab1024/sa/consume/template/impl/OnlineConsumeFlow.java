package net.lab1024.sa.consume.template.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.template.AbstractConsumeFlowTemplate;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 在线消费流程实现
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现中心实时验证模式的在线消费流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component("onlineConsumeFlow")
public class OnlineConsumeFlow extends AbstractConsumeFlowTemplate {

    @Resource
    private AccountDao accountDao;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Override
    protected AccountEntity validateAccount(PaymentProcessForm form) {
        // 查询账户信息
        AccountEntity account = accountDao.selectById(form.getAccountId());
        if (account == null) {
            log.warn("[在线消费流程] 账户不存在, accountId={}", form.getAccountId());
            return null;
        }

        if (account.getStatus() == null || account.getStatus() != 1) {
            log.warn("[在线消费流程] 账户状态异常, accountId={}, status={}", form.getAccountId(), account.getStatus());
            return null;
        }

        log.debug("[在线消费流程] 账户验证通过, accountId={}, balance={}", form.getAccountId(), account.getBalance());
        return account;
    }

    @Override
    protected boolean checkBalance(AccountEntity account, PaymentProcessForm form) {
        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal amount = form.getPaymentAmount();

        boolean sufficient = balance.compareTo(amount) >= 0;
        log.debug("[在线消费流程] 余额检查, accountId={}, balance={}, amount={}, sufficient={}",
                account.getAccountId(), balance, amount, sufficient);
        return sufficient;
    }

    @Override
    protected ConsumeRecordEntity executePayment(
            AccountEntity account, PaymentProcessForm form, DeviceEntity device) {

        // 执行扣款（使用乐观锁）
        int updated = accountDao.deductBalance(
                account.getAccountId(),
                form.getPaymentAmount(),
                account.getVersion()
        );

        if (updated == 0) {
            log.warn("[在线消费流程] 账户余额更新失败（并发冲突）, accountId={}", account.getAccountId());
            throw new RuntimeException("账户余额更新失败,请重试");
        }

        // 创建消费记录
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setUserId(form.getUserId());
        record.setAccountId(form.getAccountId());
        record.setAmount(form.getPaymentAmount());
        record.setConsumeTime(LocalDateTime.now());
        record.setDeviceId(device != null ? device.getDeviceId() : form.getDeviceId());
        record.setPaymentStatus(1); // 已支付
        record.setPaymentMethod(form.getPaymentMethod());

        consumeRecordDao.insert(record);

        log.debug("[在线消费流程] 扣款成功, accountId={}, amount={}, recordId={}",
                account.getAccountId(), form.getPaymentAmount(), record.getRecordId());

        return record;
    }

    @Override
    protected void notifyConsumeEvent(AccountEntity account, ConsumeRecordEntity record, PaymentProcessForm form) {
        // 在线消费流程的事件通知逻辑
        log.info("[在线消费流程] 消费事件通知: userId={}, accountId={}, amount={}, recordId={}",
                form.getUserId(), form.getAccountId(), form.getPaymentAmount(), record.getRecordId());
        // TODO: 实现WebSocket推送、RabbitMQ消息等
    }
}
