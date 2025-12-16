package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.client.AccountKindConfigClient;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;

/**
 * 定值金额计算器实现类
 * <p>
 * 用于计算定值消费模式的消费金额
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-consume-service中
 * - 使用@Resource注入依赖
 * - 提供定值金额计算逻辑
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
@org.springframework.stereotype.Service
public class DefaultFixedAmountCalculator {

    @SuppressWarnings("unused")
    private final CacheService cacheService;
    @SuppressWarnings("unused")
    private final AccountManager accountManager;
	    private final ConsumeAreaManager consumeAreaManager;
	    @SuppressWarnings("unused")
	    private final GatewayServiceClient gatewayServiceClient;
	    @SuppressWarnings("unused")
	    private final ObjectMapper objectMapper;
	    private final AccountKindConfigClient accountKindConfigClient;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：通过构造函数接收依赖
     * </p>
     *
     * @param cacheService 缓存服务
     * @param accountManager 账户管理器
     * @param consumeAreaManager 区域管理器
     * @param gatewayServiceClient 网关服务客户端
     * @param objectMapper JSON对象映射器
     */
    public DefaultFixedAmountCalculator(
            CacheService cacheService,
            AccountManager accountManager,
	            ConsumeAreaManager consumeAreaManager,
	            GatewayServiceClient gatewayServiceClient,
	            ObjectMapper objectMapper,
	            AccountKindConfigClient accountKindConfigClient) {
	        this.cacheService = cacheService;
	        this.accountManager = accountManager;
	        this.consumeAreaManager = consumeAreaManager;
	        this.gatewayServiceClient = gatewayServiceClient;
	        this.objectMapper = objectMapper;
	        this.accountKindConfigClient = accountKindConfigClient;
	    }

    /**
     * 计算定值金额
     * <p>
     * 功能说明：
     * 1. 根据当前时间段判断餐别（早餐/午餐/晚餐）
     * 2. 获取区域定值配置
     * 3. 获取账户类别定值配置（如果有，覆盖区域默认）
     * 4. 检查特殊日期（节假日、周末）并应用加价规则
     * 5. 应用会员等级折扣（如果需要）
     * 6. 计算最终定值金额（单位：分）
     * </p>
     *
     * @param request 消费请求
     * @param account 账户信息
     * @return 定值金额（单位：分）
     */
    public Integer calculate(ConsumeRequestDTO request, AccountEntity account) {
        log.info("[定值计算] 开始计算定值金额，userId={}, areaId={}", request.getUserId(), request.getAreaId());

        try {
            // 1. 获取区域定值配置
            String areaId = request.getAreaId() != null ? request.getAreaId().toString() : null;
            if (areaId == null) {
                log.warn("[定值计算] 区域ID为空，无法计算定值金额");
                return 0;
            }

            // 2. 从区域管理器获取定值配置
            Map<String, Object> areaConfig = consumeAreaManager.parseFixedValueConfig(areaId);
            if (areaConfig == null || areaConfig.isEmpty()) {
                log.warn("[定值计算] 区域定值配置为空，areaId={}", areaId);
                return 0;
            }

            // 3. 根据当前时间段判断餐别
            LocalTime currentTime = java.time.LocalTime.now();
            String mealType = determineMealType(currentTime);

            // 4. 获取对应餐别的定值金额（优先使用账户类别配置，否则使用区域配置）
            Double baseAmount = getMealAmountWithAccountKindOverride(areaConfig, mealType, account);
            if (baseAmount == null || baseAmount <= 0) {
                log.warn("[定值计算] 餐别定值金额为空或无效，mealType={}, areaId={}", mealType, areaId);
                return 0;
            }

            // 5. 检查特殊日期并应用加价规则
            baseAmount = applySpecialDatePricing(baseAmount, areaConfig);

            // 6. 应用会员等级折扣（如果需要）
            baseAmount = applyMembershipDiscount(baseAmount, account);

            // 7. 转换为分（整数）
            Integer amountInCents = (int) Math.round(baseAmount * 100);

            log.info("[定值计算] 计算完成，userId={}, areaId={}, mealType={}, amount={}分",
                    request.getUserId(), areaId, mealType, amountInCents);
            return amountInCents;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[定值计算] 计算定值金额参数错误，userId={}, areaId={}, error={}", request.getUserId(), request.getAreaId(), e.getMessage());
            return 0;
        } catch (BusinessException e) {
            log.warn("[定值计算] 计算定值金额业务异常，userId={}, areaId={}, code={}, message={}", request.getUserId(), request.getAreaId(), e.getCode(), e.getMessage());
            return 0;
        } catch (SystemException e) {
            log.error("[定值计算] 计算定值金额系统异常，userId={}, areaId={}, code={}, message={}", request.getUserId(), request.getAreaId(), e.getCode(), e.getMessage(), e);
            return 0;
        } catch (Exception e) {
            log.error("[定值计算] 计算定值金额未知异常，userId={}, areaId={}", request.getUserId(), request.getAreaId(), e);
            return 0;
        }
    }

    /**
     * 判断当前时间段对应的餐别
     *
     * @param currentTime 当前时间
     * @return 餐别类型（breakfast/lunch/dinner）
     */
    private String determineMealType(LocalTime currentTime) {
        int hour = currentTime.getHour();
        if (hour >= 6 && hour < 10) {
            return "breakfast";
        } else if (hour >= 11 && hour < 14) {
            return "lunch";
        } else if (hour >= 17 && hour < 20) {
            return "dinner";
        } else {
            return "lunch"; // 默认午餐
        }
    }

    /**
     * 获取餐别定值金额
     *
     * @param areaConfig 区域配置
     * @param mealType 餐别类型
     * @return 定值金额（单位：元）
     */
    private Double getMealAmount(Map<String, Object> areaConfig, String mealType) {
        String key = mealType + "Amount";
        Object value = areaConfig.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.warn("[定值计算] 餐别金额格式错误，mealType={}, value={}", mealType, value);
                return null;
            }
        }
        return null;
    }

    /**
     * 判断是否周末
     *
     * @return 是否周末
     */
    private boolean isWeekend() {
        java.time.DayOfWeek dayOfWeek = java.time.LocalDate.now().getDayOfWeek();
        return dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY;
    }

    /**
     * 获取餐别定值金额（支持账户类别覆盖）
     * <p>
     * 优先级：账户类别定值配置 > 区域定值配置
     * </p>
     *
     * @param areaConfig 区域配置
     * @param mealType 餐别类型
     * @param account 账户信息
     * @return 定值金额（单位：元）
     */
    private Double getMealAmountWithAccountKindOverride(
            Map<String, Object> areaConfig, String mealType, AccountEntity account) {
        try {
            // 1. 优先尝试从账户类别配置获取定值金额
            if (account != null && account.getAccountKindId() != null) {
                Map<String, Object> accountKindConfig = getAccountKindFixedValueConfig(account.getAccountKindId());
                if (accountKindConfig != null && !accountKindConfig.isEmpty()) {
                    Double accountKindAmount = getMealAmount(accountKindConfig, mealType);
                    if (accountKindAmount != null && accountKindAmount > 0) {
                        log.debug("[定值计算] 使用账户类别定值配置，accountKindId={}, mealType={}, amount={}",
                                account.getAccountKindId(), mealType, accountKindAmount);
                        return accountKindAmount;
                    }
                }
            }

            // 2. 如果没有账户类别配置或配置无效，使用区域配置
            Double areaAmount = getMealAmount(areaConfig, mealType);
            if (areaAmount != null && areaAmount > 0) {
                log.debug("[定值计算] 使用区域定值配置，mealType={}, amount={}", mealType, areaAmount);
                return areaAmount;
            }

            return null;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[定值计算] 获取餐别定值金额参数错误，mealType={}, error={}", mealType, e.getMessage());
            // 降级：使用区域配置
            return getMealAmount(areaConfig, mealType);
        } catch (BusinessException e) {
            log.warn("[定值计算] 获取餐别定值金额业务异常，mealType={}, code={}, message={}", mealType, e.getCode(), e.getMessage());
            // 降级：使用区域配置
            return getMealAmount(areaConfig, mealType);
        } catch (SystemException e) {
            log.error("[定值计算] 获取餐别定值金额系统异常，mealType={}, code={}, message={}", mealType, e.getCode(), e.getMessage(), e);
            // 降级：使用区域配置
            return getMealAmount(areaConfig, mealType);
        } catch (Exception e) {
            log.error("[定值计算] 获取餐别定值金额未知异常，mealType={}", mealType, e);
            // 降级：使用区域配置
            return getMealAmount(areaConfig, mealType);
        }
    }

    /**
     * 获取账户类别定值配置
     * <p>
     * 通过网关调用公共服务获取账户类别的定值配置
     * </p>
     *
     * @param accountKindId 账户类别ID
     * @return 定值配置Map，如果获取失败返回null
     */
    private Map<String, Object> getAccountKindFixedValueConfig(Long accountKindId) {
        try {
	            net.lab1024.sa.common.dto.ResponseDTO<java.util.Map<String, Object>> response =
	                    accountKindConfigClient.getAccountKind(accountKindId);

            if (response == null || !response.isSuccess() || response.getData() == null) {
                log.debug("[定值计算] 获取账户类别信息失败，accountKindId={}", accountKindId);
                return null;
            }

            java.util.Map<String, Object> accountKind = response.getData();

            // 提取fixedValueConfig字段（JSON格式）
            Object fixedValueConfigObj = accountKind.get("fixedValueConfig");
            if (fixedValueConfigObj == null) {
                return null;
            }

            // 解析JSON配置
            if (fixedValueConfigObj instanceof String) {
                return objectMapper.readValue((String) fixedValueConfigObj,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } else if (fixedValueConfigObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) fixedValueConfigObj;
                return config;
            }

            return null;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[定值计算] 获取账户类别定值配置参数错误，accountKindId={}, error={}", accountKindId, e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.warn("[定值计算] 获取账户类别定值配置业务异常，accountKindId={}, code={}, message={}", accountKindId, e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.warn("[定值计算] 获取账户类别定值配置系统异常，accountKindId={}, code={}, message={}", accountKindId, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.warn("[定值计算] 获取账户类别定值配置未知异常，accountKindId={}", accountKindId, e);
            return null;
        }
    }

    /**
     * 应用特殊日期定价规则
     * <p>
     * 支持：
     * 1. 周末加价
     * 2. 节假日加价
     * 3. 工作日折扣
     * </p>
     *
     * @param baseAmount 基础金额
     * @param areaConfig 区域配置
     * @return 应用特殊日期规则后的金额
     */
    private Double applySpecialDatePricing(Double baseAmount, Map<String, Object> areaConfig) {
        try {
            java.time.LocalDate currentDate = java.time.LocalDate.now();

            // 1. 检查是否周末
            boolean isWeekend = isWeekend();
            if (isWeekend) {
                Object weekendMultiplierObj = areaConfig.get("weekendMultiplier");
                if (weekendMultiplierObj != null) {
                    Double weekendMultiplier = parseDouble(weekendMultiplierObj);
                    if (weekendMultiplier != null && weekendMultiplier > 0) {
                        baseAmount = baseAmount * weekendMultiplier;
                        log.debug("[定值计算] 应用周末加价，multiplier={}, amount={}", weekendMultiplier, baseAmount);
                    }
                }
            }

            // 2. 检查是否节假日（通过网关调用公共服务获取节假日列表）
            boolean isHoliday = isHoliday(currentDate);
            if (isHoliday) {
                Object holidayMultiplierObj = areaConfig.get("holidayMultiplier");
                if (holidayMultiplierObj != null) {
                    Double holidayMultiplier = parseDouble(holidayMultiplierObj);
                    if (holidayMultiplier != null && holidayMultiplier > 0) {
                        baseAmount = baseAmount * holidayMultiplier;
                        log.debug("[定值计算] 应用节假日加价，multiplier={}, amount={}", holidayMultiplier, baseAmount);
                    }
                }
            }

            // 3. 检查是否工作日折扣
            if (!isWeekend && !isHoliday) {
                Object workdayDiscountObj = areaConfig.get("workdayDiscount");
                if (workdayDiscountObj != null) {
                    Double workdayDiscount = parseDouble(workdayDiscountObj);
                    if (workdayDiscount != null && workdayDiscount > 0 && workdayDiscount <= 1) {
                        baseAmount = baseAmount * workdayDiscount;
                        log.debug("[定值计算] 应用工作日折扣，discount={}, amount={}", workdayDiscount, baseAmount);
                    }
                }
            }

            return baseAmount;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[定值计算] 应用特殊日期定价规则参数错误，error={}", e.getMessage());
            return baseAmount; // 失败时返回原金额
        } catch (BusinessException e) {
            log.warn("[定值计算] 应用特殊日期定价规则业务异常，code={}, message={}", e.getCode(), e.getMessage());
            return baseAmount; // 失败时返回原金额
        } catch (SystemException e) {
            log.warn("[定值计算] 应用特殊日期定价规则系统异常，code={}, message={}", e.getCode(), e.getMessage(), e);
            return baseAmount; // 失败时返回原金额
        } catch (Exception e) {
            log.warn("[定值计算] 应用特殊日期定价规则未知异常", e);
            return baseAmount; // 失败时返回原金额
        }
    }

    /**
     * 判断是否为节假日
     * <p>
     * 通过网关调用公共服务查询节假日信息
     * </p>
     *
     * @param date 日期
     * @return 是否为节假日
     */
    private boolean isHoliday(java.time.LocalDate date) {
        try {
            String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            net.lab1024.sa.common.dto.ResponseDTO<Boolean> response =
                    gatewayServiceClient.callCommonService(
                            "/api/v1/holiday/check?date=" + dateStr,
                            org.springframework.http.HttpMethod.GET,
                            null,
                            Boolean.class
                    );

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }

            return false; // 默认不是节假日

        } catch (IllegalArgumentException | ParamException e) {
            log.debug("[定值计算] 查询节假日信息参数错误，date={}, error={}", date, e.getMessage());
            return false; // 失败时默认不是节假日
        } catch (BusinessException e) {
            log.debug("[定值计算] 查询节假日信息业务异常，date={}, code={}, message={}", date, e.getCode(), e.getMessage());
            return false; // 失败时默认不是节假日
        } catch (SystemException e) {
            log.debug("[定值计算] 查询节假日信息系统异常，date={}, code={}, message={}", date, e.getCode(), e.getMessage(), e);
            return false; // 失败时默认不是节假日
        } catch (Exception e) {
            log.debug("[定值计算] 查询节假日信息未知异常，date={}", date, e);
            return false; // 失败时默认不是节假日
        }
    }

    /**
     * 应用会员等级折扣
     * <p>
     * 根据账户的总消费金额或会员等级应用折扣
     * </p>
     *
     * @param baseAmount 基础金额
     * @param account 账户信息
     * @return 应用折扣后的金额
     */
    private Double applyMembershipDiscount(Double baseAmount, AccountEntity account) {
        try {
            if (account == null) {
                return baseAmount;
            }

            // 1. 获取账户总消费金额（用于判断会员等级）
            BigDecimal totalConsumeAmount = account.getTotalConsumeAmount();
            if (totalConsumeAmount == null || totalConsumeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return baseAmount; // 无消费记录，不应用折扣
            }

            // 2. 根据总消费金额计算会员等级（示例：0-1000普通，1000-5000银卡，5000-10000金卡，10000+钻石）
            int memberLevel = calculateMemberLevel(totalConsumeAmount);
            if (memberLevel <= 0) {
                return baseAmount; // 普通会员，无折扣
            }

            // 3. 通过网关调用公共服务获取会员折扣率
            Double discountRate = getMemberDiscountRate(memberLevel);
            if (discountRate == null || discountRate <= 0 || discountRate >= 1) {
                return baseAmount; // 折扣率无效
            }

            // 4. 应用折扣
            Double discountedAmount = baseAmount * discountRate;
            log.debug("[定值计算] 应用会员折扣，memberLevel={}, discountRate={}, originalAmount={}, discountedAmount={}",
                    memberLevel, discountRate, baseAmount, discountedAmount);

            return discountedAmount;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[定值计算] 应用会员折扣参数错误，error={}", e.getMessage());
            return baseAmount; // 失败时返回原金额
        } catch (BusinessException e) {
            log.warn("[定值计算] 应用会员折扣业务异常，code={}, message={}", e.getCode(), e.getMessage());
            return baseAmount; // 失败时返回原金额
        } catch (SystemException e) {
            log.warn("[定值计算] 应用会员折扣系统异常，code={}, message={}", e.getCode(), e.getMessage(), e);
            return baseAmount; // 失败时返回原金额
        } catch (Exception e) {
            log.warn("[定值计算] 应用会员折扣未知异常", e);
            return baseAmount; // 失败时返回原金额
        }
    }

    /**
     * 根据总消费金额计算会员等级
     * <p>
     * 会员等级规则（可配置）：
     * - 0级（普通）：0-1000元
     * - 1级（银卡）：1000-5000元
     * - 2级（金卡）：5000-10000元
     * - 3级（钻石）：10000元以上
     * </p>
     *
     * @param totalConsumeAmount 总消费金额
     * @return 会员等级（0=普通，1=银卡，2=金卡，3=钻石）
     */
    private int calculateMemberLevel(BigDecimal totalConsumeAmount) {
        double amount = totalConsumeAmount.doubleValue();
        if (amount >= 10000) {
            return 3; // 钻石会员
        } else if (amount >= 5000) {
            return 2; // 金卡会员
        } else if (amount >= 1000) {
            return 1; // 银卡会员
        } else {
            return 0; // 普通会员
        }
    }

    /**
     * 获取会员折扣率
     * <p>
     * 通过网关调用公共服务获取会员等级的折扣率
     * </p>
     *
     * @param memberLevel 会员等级
     * @return 折扣率（0-1之间，例如0.9表示9折）
     */
    private Double getMemberDiscountRate(int memberLevel) {
        try {
            net.lab1024.sa.common.dto.ResponseDTO<java.util.Map<String, Object>> response =
                    gatewayServiceClient.callCommonService(
                            "/api/v1/membership/level/" + memberLevel + "/discount",
                            org.springframework.http.HttpMethod.GET,
                            null,
                            new com.fasterxml.jackson.core.type.TypeReference<
                                    net.lab1024.sa.common.dto.ResponseDTO<java.util.Map<String, Object>>>() {}
                    );

            if (response != null && response.isSuccess() && response.getData() != null) {
                java.util.Map<String, Object> data = response.getData();
                Object discountRateObj = data.get("discountRate");
                if (discountRateObj != null) {
                    return parseDouble(discountRateObj);
                }
            }

            // 默认折扣率（如果公共服务未配置）
            return getDefaultMemberDiscountRate(memberLevel);

        } catch (IllegalArgumentException | ParamException e) {
            log.debug("[定值计算] 获取会员折扣率参数错误，memberLevel={}, error={}", memberLevel, e.getMessage());
            return getDefaultMemberDiscountRate(memberLevel);
        } catch (BusinessException e) {
            log.debug("[定值计算] 获取会员折扣率业务异常，memberLevel={}, code={}, message={}", memberLevel, e.getCode(), e.getMessage());
            return getDefaultMemberDiscountRate(memberLevel);
        } catch (SystemException e) {
            log.debug("[定值计算] 获取会员折扣率系统异常，memberLevel={}, code={}, message={}", memberLevel, e.getCode(), e.getMessage(), e);
            return getDefaultMemberDiscountRate(memberLevel);
        } catch (Exception e) {
            log.debug("[定值计算] 获取会员折扣率未知异常，memberLevel={}", memberLevel, e);
            return getDefaultMemberDiscountRate(memberLevel);
        }
    }

    /**
     * 获取默认会员折扣率
     * <p>
     * 当公共服务未配置时使用默认折扣率
     * </p>
     *
     * @param memberLevel 会员等级
     * @return 默认折扣率
     */
    private Double getDefaultMemberDiscountRate(int memberLevel) {
        switch (memberLevel) {
            case 3: // 钻石会员
                return 0.85; // 85折
            case 2: // 金卡会员
                return 0.90; // 9折
            case 1: // 银卡会员
                return 0.95; // 95折
            default:
                return 1.0; // 普通会员无折扣
        }
    }

    /**
     * 安全地将对象转换为Double
     *
     * @param value 待转换的对象
     * @return Double值，转换失败返回null
     */
    private Double parseDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.warn("[定值计算] 无法将字符串转换为Double: {}", value);
                return null;
            }
        }
        return null;
    }
}



