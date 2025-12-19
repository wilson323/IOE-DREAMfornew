package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;

import net.lab1024.sa.consume.entity.AccountEntity;

/**
 * 账户管理Manager接口
 * <p>
 * 用于账户相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中不使用Spring注解
 * - Manager类通过构造函数注入依赖
 * - 保持为纯Java类
 * </p>
 * <p>
 * 业务场景：
 * - 账户余额管理
 * - 账户充值
 * - 账户消费扣款
 * - 账户冻结/解冻
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccountManager {

    /**
     * 根据用户ID获取账户信息
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    AccountEntity getAccountByUserId(Long userId);

    /**
     * 根据账户ID获取账户信息
     *
     * @param accountId 账户ID
     * @return 账户信息
     */
    AccountEntity getAccountById(Long accountId);

    /**
     * 扣减账户余额
     *
     * @param accountId 账户ID
     * @param amount 扣减金额（单位：元）
     * @return 是否成功
     */
    boolean deductBalance(Long accountId, BigDecimal amount);

    /**
     * 增加账户余额
     *
     * @param accountId 账户ID
     * @param amount 增加金额（单位：元）
     * @return 是否成功
     */
    boolean addBalance(Long accountId, BigDecimal amount);

    /**
     * 检查账户余额是否充足
     *
     * @param accountId 账户ID
     * @param amount 需要金额（单位：元）
     * @return 是否充足
     */
    boolean checkBalanceSufficient(Long accountId, BigDecimal amount);
}



