package net.lab1024.sa.consume.strategy.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;
import net.lab1024.sa.consume.service.impl.DefaultFixedAmountCalculator;
import net.lab1024.sa.consume.strategy.ConsumeAmountCalculator;

/**
 * 定值金额计算器策略实现
 * <p>
 * 用于计算定值消费模式的消费金额
 * 严格遵循CLAUDE.md规范：
 * - 策略实现类使用@Component注解
 * - 使用@Resource注入依赖
 * - 实现ConsumeAmountCalculator接口
 * </p>
 * <p>
 * 业务场景：
 * - 早餐/午餐/晚餐定值计算
 * - 周末加价计算
 * - 账户类别定值覆盖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class FixedAmountCalculator implements ConsumeAmountCalculator {

    private final DefaultFixedAmountCalculator defaultFixedAmountCalculator;
    private final ConsumeAreaManager consumeAreaManager;

    /**
     * 构造函数注入依赖
     *
     * @param defaultFixedAmountCalculator 定值计算器
     * @param consumeAreaManager 区域管理器
     */
    public FixedAmountCalculator(
            DefaultFixedAmountCalculator defaultFixedAmountCalculator,
            ConsumeAreaManager consumeAreaManager) {
        this.defaultFixedAmountCalculator = defaultFixedAmountCalculator;
        this.consumeAreaManager = consumeAreaManager;
    }

    /**
     * 计算定值金额
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @param request 消费请求对象（定值模式不需要）
     * @return 定值金额（单位：元）
     */
    @Override
    public BigDecimal calculate(Long accountId, String areaId, AccountEntity account, Object request) {
        log.debug("[定值策略] 计算定值金额，accountId={}, areaId={}", accountId, areaId);

        try {
            // 1. 构建消费请求DTO
            net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO requestDTO = 
                    new net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO();
            requestDTO.setUserId(account.getUserId());
            requestDTO.setAccountId(accountId);
            requestDTO.setAreaId(areaId != null ? Long.parseLong(areaId) : null);

            // 2. 使用定值计算器计算金额（单位：分）
            Integer fixedAmountInCents = defaultFixedAmountCalculator.calculate(requestDTO, account);
            if (fixedAmountInCents == null || fixedAmountInCents <= 0) {
                log.warn("[定值策略] 定值金额计算结果为0或无效，accountId={}, areaId={}", accountId, areaId);
                return BigDecimal.ZERO;
            }

            // 3. 转换为元（单位：分 -> 元）
            BigDecimal fixedAmount = BigDecimal.valueOf(fixedAmountInCents)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            log.debug("[定值策略] 定值金额计算完成，accountId={}, areaId={}, amount={}", 
                    accountId, areaId, fixedAmount);
            return fixedAmount;

        } catch (Exception e) {
            log.error("[定值策略] 计算定值金额失败，accountId={}, areaId={}", accountId, areaId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取策略支持的消费模式
     *
     * @return 消费模式：FIXED
     */
    @Override
    public String getConsumeMode() {
        return "FIXED";
    }

    /**
     * 验证定值模式是否支持
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @return 是否支持
     */
    @Override
    public boolean isSupported(Long accountId, String areaId, AccountEntity account) {
        try {
            // 1. 验证区域是否存在
            if (areaId == null || areaId.isEmpty()) {
                return false;
            }

            net.lab1024.sa.consume.domain.entity.ConsumeAreaEntity area = 
                    consumeAreaManager.getAreaById(areaId);
            if (area == null) {
                log.warn("[定值策略] 区域不存在，areaId={}", areaId);
                return false;
            }

            // 2. 验证区域经营模式是否支持定值模式
            // 餐别制(1)和混合模式(3)支持定值模式
            Integer manageMode = area.getManageMode();
            if (manageMode == null) {
                return false;
            }

            boolean supported = manageMode == 1 || manageMode == 3;
            log.debug("[定值策略] 定值模式支持验证，areaId={}, manageMode={}, supported={}", 
                    areaId, manageMode, supported);
            return supported;

        } catch (Exception e) {
            log.error("[定值策略] 验证定值模式支持失败，accountId={}, areaId={}", accountId, areaId, e);
            return false;
        }
    }
}




