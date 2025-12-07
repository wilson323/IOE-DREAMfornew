package net.lab1024.sa.consume.strategy.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.strategy.ConsumeAmountCalculator;

/**
 * 计次模式计算器策略实现
 * <p>
 * 用于计算计次消费模式的消费金额
 * 严格遵循CLAUDE.md规范：
 * - 策略实现类使用@Component注解
 * - 使用@Resource注入依赖
 * - 实现ConsumeAmountCalculator接口
 * </p>
 * <p>
 * 业务场景：
 * - 固定计次消费
 * - 从账户类别配置获取计次价格
 * - 应用计次折扣
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class CountAmountCalculator implements ConsumeAmountCalculator {

    @Resource
    private AccountManager accountManager;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /**
     * 计算计次模式消费金额
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @param request 消费请求对象（计次模式不需要）
     * @return 计次固定金额（单位：元）
     */
    @Override
    public BigDecimal calculate(Long accountId, String areaId, AccountEntity account, Object request) {
        log.debug("[计次策略] 计算计次模式消费金额，accountId={}, areaId={}", accountId, areaId);

        try {
            // 1. 获取账户类别ID
            Long accountKindId = account.getAccountKindId();
            if (accountKindId == null) {
                log.warn("[计次策略] 账户类别ID为空，无法计算计次金额，accountId={}", accountId);
                return BigDecimal.ZERO;
            }

            // 2. 通过网关调用公共服务获取账户类别信息
            ResponseDTO<java.util.Map<String, Object>> accountKindResponse =
                    gatewayServiceClient.callCommonService(
                            "/api/v1/account-kind/" + accountKindId,
                            org.springframework.http.HttpMethod.GET,
                            null,
                            new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<java.util.Map<String, Object>>>() {}
                    );

            if (accountKindResponse == null || !accountKindResponse.isSuccess() 
                    || accountKindResponse.getData() == null) {
                log.warn("[计次策略] 获取账户类别信息失败，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> accountKind = accountKindResponse.getData();

            // 3. 获取mode_config JSON字段
            Object modeConfigObj = accountKind.get("mode_config");
            if (modeConfigObj == null) {
                log.warn("[计次策略] 账户类别未配置消费模式，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            // 4. 解析mode_config JSON
            java.util.Map<String, Object> modeConfig = parseModeConfig(modeConfigObj);
            if (modeConfig == null) {
                return BigDecimal.ZERO;
            }

            // 5. 获取METERED配置
            java.util.Map<String, Object> meteredConfig = getMeteredConfig(modeConfig, accountKindId);
            if (meteredConfig == null) {
                return BigDecimal.ZERO;
            }

            // 6. 检查是否启用
            Object enabledObj = meteredConfig.get("enabled");
            if (enabledObj == null || !Boolean.parseBoolean(enabledObj.toString())) {
                log.warn("[计次策略] METERED模式未启用，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            // 7. 检查子类型是否为COUNT
            Object subTypeObj = meteredConfig.get("subType");
            if (subTypeObj == null || !"COUNT".equals(subTypeObj.toString())) {
                log.warn("[计次策略] METERED子类型不是COUNT，accountKindId={}, subType={}", 
                        accountKindId, subTypeObj);
                return BigDecimal.ZERO;
            }

            // 8. 获取count配置
            java.util.Map<String, Object> countConfig = getCountConfig(meteredConfig, accountKindId);
            if (countConfig == null) {
                return BigDecimal.ZERO;
            }

            // 9. 获取pricePerTime（每次价格，单位：分）
            Integer pricePerTimeInCents = getPricePerTime(countConfig, accountKindId);
            if (pricePerTimeInCents == null || pricePerTimeInCents <= 0) {
                return BigDecimal.ZERO;
            }

            // 10. 转换为元
            BigDecimal amount = BigDecimal.valueOf(pricePerTimeInCents)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            // 11. 应用折扣规则（如有）
            Object applyDiscountObj = countConfig.get("applyDiscount");
            boolean applyDiscount = applyDiscountObj != null 
                    && Boolean.parseBoolean(applyDiscountObj.toString());
            if (applyDiscount) {
                BigDecimal discountRate = getCountModeDiscountRate(accountKind);
                if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal discountAmount = amount.multiply(discountRate);
                    amount = amount.subtract(discountAmount);
                    log.debug("[计次策略] 应用计次模式折扣，accountId={}, discountRate={}, finalAmount={}",
                            accountId, discountRate, amount);
                }
            }

            log.debug("[计次策略] 计次金额计算完成，accountId={}, areaId={}, amount={}", 
                    accountId, areaId, amount);
            return amount;

        } catch (Exception e) {
            log.error("[计次策略] 计算计次模式消费金额失败，accountId={}, areaId={}", accountId, areaId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 解析mode_config
     */
    private java.util.Map<String, Object> parseModeConfig(Object modeConfigObj) {
        try {
            if (modeConfigObj instanceof String) {
                return objectMapper.readValue((String) modeConfigObj, 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (modeConfigObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) modeConfigObj;
                return map;
            } else {
                log.warn("[计次策略] mode_config格式不正确");
                return null;
            }
        } catch (Exception e) {
            log.error("[计次策略] 解析mode_config失败", e);
            return null;
        }
    }

    /**
     * 获取METERED配置
     */
    private java.util.Map<String, Object> getMeteredConfig(
            java.util.Map<String, Object> modeConfig, Long accountKindId) {
        try {
            Object meteredObj = modeConfig.get("METERED");
            if (meteredObj == null) {
                log.warn("[计次策略] 账户类别未配置METERED模式，accountKindId={}", accountKindId);
                return null;
            }

            if (meteredObj instanceof String) {
                return objectMapper.readValue((String) meteredObj, 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (meteredObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) meteredObj;
                return map;
            } else {
                log.warn("[计次策略] METERED配置格式不正确，accountKindId={}", accountKindId);
                return null;
            }
        } catch (Exception e) {
            log.error("[计次策略] 获取METERED配置失败，accountKindId={}", accountKindId, e);
            return null;
        }
    }

    /**
     * 获取count配置
     */
    private java.util.Map<String, Object> getCountConfig(
            java.util.Map<String, Object> meteredConfig, Long accountKindId) {
        try {
            Object countObj = meteredConfig.get("count");
            if (countObj == null) {
                log.warn("[计次策略] METERED未配置count子类型，accountKindId={}", accountKindId);
                return null;
            }

            if (countObj instanceof String) {
                return objectMapper.readValue((String) countObj, 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (countObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) countObj;
                return map;
            } else {
                log.warn("[计次策略] count配置格式不正确，accountKindId={}", accountKindId);
                return null;
            }
        } catch (Exception e) {
            log.error("[计次策略] 获取count配置失败，accountKindId={}", accountKindId, e);
            return null;
        }
    }

    /**
     * 获取pricePerTime
     */
    private Integer getPricePerTime(java.util.Map<String, Object> countConfig, Long accountKindId) {
        try {
            Object pricePerTimeObj = countConfig.get("pricePerTime");
            if (pricePerTimeObj == null) {
                log.warn("[计次策略] count配置未设置pricePerTime，accountKindId={}", accountKindId);
                return null;
            }

            Integer pricePerTimeInCents;
            if (pricePerTimeObj instanceof Number) {
                pricePerTimeInCents = ((Number) pricePerTimeObj).intValue();
            } else if (pricePerTimeObj instanceof String) {
                pricePerTimeInCents = Integer.parseInt((String) pricePerTimeObj);
            } else {
                log.warn("[计次策略] pricePerTime格式不正确，accountKindId={}", accountKindId);
                return null;
            }

            if (pricePerTimeInCents <= 0) {
                log.warn("[计次策略] pricePerTime无效，accountKindId={}, pricePerTime={}", 
                        accountKindId, pricePerTimeInCents);
                return null;
            }

            return pricePerTimeInCents;
        } catch (Exception e) {
            log.error("[计次策略] 获取pricePerTime失败，accountKindId={}", accountKindId, e);
            return null;
        }
    }

    /**
     * 获取计次模式折扣率
     */
    private BigDecimal getCountModeDiscountRate(java.util.Map<String, Object> accountKind) {
        try {
            // 1. 获取mode_config
            Object modeConfigObj = accountKind.get("mode_config");
            if (modeConfigObj == null) {
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> modeConfig = parseModeConfig(modeConfigObj);
            if (modeConfig == null) {
                return BigDecimal.ZERO;
            }

            // 2. 获取METERED配置
            java.util.Map<String, Object> meteredConfig = getMeteredConfig(modeConfig, null);
            if (meteredConfig == null) {
                return BigDecimal.ZERO;
            }

            // 3. 获取count配置
            java.util.Map<String, Object> countConfig = getCountConfig(meteredConfig, null);
            if (countConfig == null) {
                return BigDecimal.ZERO;
            }

            // 4. 获取discountRate
            Object discountRateObj = countConfig.get("discountRate");
            if (discountRateObj == null) {
                return BigDecimal.ZERO;
            }

            BigDecimal discountRate;
            if (discountRateObj instanceof Number) {
                discountRate = BigDecimal.valueOf(((Number) discountRateObj).doubleValue());
            } else if (discountRateObj instanceof String) {
                discountRate = new BigDecimal((String) discountRateObj);
            } else {
                log.warn("[计次策略] discountRate格式不正确");
                return BigDecimal.ZERO;
            }

            // 5. 验证折扣率范围（0-1之间）
            if (discountRate.compareTo(BigDecimal.ZERO) < 0 
                    || discountRate.compareTo(BigDecimal.ONE) > 0) {
                log.warn("[计次策略] 折扣率超出范围，discountRate={}", discountRate);
                return BigDecimal.ZERO;
            }

            return discountRate;
        } catch (Exception e) {
            log.error("[计次策略] 获取计次模式折扣率失败", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取策略支持的消费模式
     *
     * @return 消费模式：COUNT
     */
    @Override
    public String getConsumeMode() {
        return "COUNT";
    }

    /**
     * 验证计次模式是否支持
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @return 是否支持
     */
    @Override
    public boolean isSupported(Long accountId, String areaId, AccountEntity account) {
        // 计次模式需要账户类别配置METERED模式
        // 这里简化处理，实际应该检查账户类别的mode_config
        return account != null && account.getAccountKindId() != null;
    }
}

