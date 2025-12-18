package net.lab1024.sa.consume.template.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.template.AbstractConsumeFlowTemplate;
import net.lab1024.sa.common.consume.dao.AccountDao;
import net.lab1024.sa.common.consume.entity.AccountEntity;
import net.lab1024.sa.common.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 离线消费流程实现
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现中心实时验证模式的离线消费流程（降级方案）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component("offlineConsumeFlow")
public class OfflineConsumeFlow extends AbstractConsumeFlowTemplate {

    @Resource
    private AccountDao accountDao;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Override
    protected AccountEntity validateAccount(PaymentProcessForm form) {
        // 离线模式下，验证白名单用户
        if (!isWhitelistUser(form.getUserId())) {
            log.warn("[离线消费流程] 非白名单用户, userId={}", form.getUserId());
            return null;
        }

        // 查询账户信息
        AccountEntity account = accountDao.selectById(form.getAccountId());
        if (account == null) {
            log.warn("[离线消费流程] 账户不存在, accountId={}", form.getAccountId());
            return null;
        }

        log.debug("[离线消费流程] 账户验证通过, accountId={}", form.getAccountId());
        return account;
    }

    @Override
    protected boolean checkBalance(AccountEntity account, PaymentProcessForm form) {
        // 离线模式下，使用固定额度检查
        BigDecimal fixedAmount = getFixedOfflineAmount();
        BigDecimal amount = form.getPaymentAmount();

        boolean sufficient = amount.compareTo(fixedAmount) <= 0;
        log.debug("[离线消费流程] 固定额度检查, amount={}, fixedAmount={}, sufficient={}",
                amount, fixedAmount, sufficient);
        return sufficient;
    }

    @Override
    protected ConsumeRecordEntity executePayment(
            AccountEntity account, PaymentProcessForm form, DeviceEntity device) {

        // 离线模式下，记录消费（待网络恢复后补扣）
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setUserId(form.getUserId());
        record.setAccountId(form.getAccountId());
        record.setAmount(form.getPaymentAmount());
        record.setConsumeTime(LocalDateTime.now());
        record.setDeviceId(device != null ? device.getDeviceId() : form.getDeviceId());
        record.setPaymentStatus(2); // 待补扣
        record.setPaymentMethod(form.getPaymentMethod());
        record.setIsOfflineSync(true); // 标记为离线同步

        consumeRecordDao.insert(record);

        log.debug("[离线消费流程] 离线消费记录已保存, recordId={}, 待网络恢复后补扣", record.getRecordId());

        return record;
    }

    @Override
    protected void notifyConsumeEvent(AccountEntity account, ConsumeRecordEntity record, PaymentProcessForm form) {
        // 离线消费流程的事件通知逻辑
        log.info("[离线消费流程] 离线消费事件通知: userId={}, accountId={}, amount={}, recordId={}",
                form.getUserId(), form.getAccountId(), form.getPaymentAmount(), record.getRecordId());
        // TODO: 实现离线消费补扣逻辑
    }

    /**
     * 检查是否为白名单用户
     *
     * @param userId 用户ID
     * @return 是否为白名单用户
     */
    private boolean isWhitelistUser(Long userId) {
        // TODO: 实现白名单验证逻辑
        // 临时实现：默认允许
        return true;
    }

    /**
     * 获取固定离线消费额度
     *
     * @return 固定额度
     */
    private BigDecimal getFixedOfflineAmount() {
        // TODO: 从配置获取固定额度
        return new BigDecimal("10.00"); // 默认10元
    }
}
