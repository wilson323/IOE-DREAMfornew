package net.lab1024.sa.consume.strategy.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionForm;
import net.lab1024.sa.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.consume.strategy.ConsumeAmountCalculator;

/**
 * 金额模式计算器策略实现
 * <p>
 * 用于计算金额消费模式的消费金额
 * 严格遵循CLAUDE.md规范：
 * - 策略实现类使用@Component注解
 * - 实现ConsumeAmountCalculator接口
 * </p>
 * <p>
 * 业务场景：
 * - 自由金额消费
 * - 手动输入金额
 * - 金额验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class AmountCalculator implements ConsumeAmountCalculator {

    /**
     * 计算金额模式消费金额
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @param request 消费请求对象（应包含consumeAmount字段）
     * @return 消费金额（单位：元）
     */
    @Override
    public BigDecimal calculate(Long accountId, String areaId, AccountEntity account, Object request) {
        log.debug("[金额策略] 计算金额模式消费金额，accountId={}, areaId={}", accountId, areaId);

        try {
            BigDecimal consumeAmount = null;

            // 1. 从请求对象中提取金额
            if (request instanceof ConsumeTransactionForm) {
                ConsumeTransactionForm form = (ConsumeTransactionForm) request;
                consumeAmount = form.getAmount();
            } else if (request instanceof ConsumeRequest) {
                ConsumeRequest consumeRequest = (ConsumeRequest) request;
                consumeAmount = consumeRequest.getAmount();
            } else if (request instanceof BigDecimal) {
                consumeAmount = (BigDecimal) request;
            } else if (request instanceof Number) {
                consumeAmount = BigDecimal.valueOf(((Number) request).doubleValue());
            }

            // 2. 验证金额
            if (consumeAmount == null || consumeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[金额策略] 金额无效，accountId={}, amount={}", accountId, consumeAmount);
                return BigDecimal.ZERO;
            }

            log.debug("[金额策略] 金额模式计算完成，accountId={}, areaId={}, amount={}", 
                    accountId, areaId, consumeAmount);
            return consumeAmount;

        } catch (Exception e) {
            log.error("[金额策略] 计算金额模式消费金额失败，accountId={}, areaId={}", accountId, areaId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取策略支持的消费模式
     *
     * @return 消费模式：AMOUNT
     */
    @Override
    public String getConsumeMode() {
        return "AMOUNT";
    }

    /**
     * 验证金额模式是否支持
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @return 是否支持（金额模式通常都支持）
     */
    @Override
    public boolean isSupported(Long accountId, String areaId, AccountEntity account) {
        // 金额模式通常都支持，只要账户存在即可
        return account != null;
    }
}




