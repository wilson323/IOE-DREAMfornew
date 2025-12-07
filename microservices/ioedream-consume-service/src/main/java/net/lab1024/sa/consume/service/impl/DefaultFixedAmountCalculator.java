package net.lab1024.sa.consume.service.impl;

import java.time.LocalTime;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
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
            ObjectMapper objectMapper) {
        this.cacheService = cacheService;
        this.accountManager = accountManager;
        this.consumeAreaManager = consumeAreaManager;
        this.gatewayServiceClient = gatewayServiceClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 计算定值金额
     * <p>
     * 功能说明：
     * 1. 根据当前时间段判断餐别（早餐/午餐/晚餐）
     * 2. 获取区域定值配置
     * 3. 获取账户类别定值配置（如果有，覆盖区域默认）
     * 4. 计算最终定值金额（单位：分）
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

            // 4. 获取对应餐别的定值金额
            Double baseAmount = getMealAmount(areaConfig, mealType);
            if (baseAmount == null || baseAmount <= 0) {
                log.warn("[定值计算] 餐别定值金额为空或无效，mealType={}, areaId={}", mealType, areaId);
                return 0;
            }

            // 5. 检查是否周末，应用周末加价
            boolean isWeekend = isWeekend();
            if (isWeekend) {
                Double weekendMultiplier = (Double) areaConfig.get("weekendMultiplier");
                if (weekendMultiplier != null && weekendMultiplier > 0) {
                    baseAmount = baseAmount * weekendMultiplier;
                }
            }

            // 6. 转换为分（整数）
            Integer amountInCents = (int) Math.round(baseAmount * 100);

            log.info("[定值计算] 计算完成，userId={}, areaId={}, mealType={}, amount={}分",
                    request.getUserId(), areaId, mealType, amountInCents);
            return amountInCents;

        } catch (Exception e) {
            log.error("[定值计算] 计算定值金额失败，userId={}, areaId={}", request.getUserId(), request.getAreaId(), e);
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
}
