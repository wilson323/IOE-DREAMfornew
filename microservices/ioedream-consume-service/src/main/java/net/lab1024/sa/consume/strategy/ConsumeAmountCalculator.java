package net.lab1024.sa.consume.strategy;

import java.math.BigDecimal;
import net.lab1024.sa.consume.entity.AccountEntity;

/**
 * 消费金额计算器策略接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
public interface ConsumeAmountCalculator {

    /**
     * 计算消费金额
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @param request 消费请求对象
     * @return 消费金额（单位：元）
     */
    BigDecimal calculate(Long accountId, String areaId, AccountEntity account, Object request);

    /**
     * 获取策略支持的消费模式
     *
     * @return 消费模式
     */
    String getConsumeMode();

    /**
     * 验证策略是否支持当前场景
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @return 是否支持
     */
    boolean isSupported(Long accountId, String areaId, AccountEntity account);
}
