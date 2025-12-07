package net.lab1024.sa.consume.strategy;

import java.math.BigDecimal;

import net.lab1024.sa.consume.domain.entity.AccountEntity;

/**
 * 消费金额计算器接口（策略模式）
 * <p>
 * 用于不同消费模式的金额计算策略
 * 严格遵循CLAUDE.md规范：
 * - 策略接口定义在业务服务模块中
 * - 使用策略模式实现不同消费模式的计算逻辑
 * </p>
 * <p>
 * 支持的消费模式：
 * - FIXED - 定值模式：从区域配置或账户类别配置获取定值金额
 * - AMOUNT - 金额模式：使用传入金额
 * - PRODUCT - 商品模式：计算商品总价（需要商品ID和数量）
 * - COUNT - 计次模式：固定金额（从配置获取）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeAmountCalculator {

    /**
     * 计算消费金额
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @param request 消费请求对象（可选，商品模式时需要传递以获取商品信息）
     * @return 实际消费金额（单位：元）
     */
    BigDecimal calculate(Long accountId, String areaId, AccountEntity account, Object request);

    /**
     * 获取策略支持的消费模式
     *
     * @return 消费模式（FIXED/AMOUNT/PRODUCT/COUNT）
     */
    String getConsumeMode();

    /**
     * 验证消费模式是否支持
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @return 是否支持
     */
    boolean isSupported(Long accountId, String areaId, AccountEntity account);
}

