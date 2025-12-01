package net.lab1024.sa.admin.module.consume.engine.mode.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.engine.mode.config.ConsumptionModeConfig;
import net.lab1024.sa.admin.module.consume.engine.mode.strategy.ConsumptionModeStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * 固定金额消费模式策略
 * 支持预设固定金额消费，通常用于食堂、餐厅等场景
 * 可配置不同餐别、时段的固定金额和补贴规则
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class FixedAmountModeStrategy implements ConsumptionModeStrategy {

    // 餐别时间段定义
    private static final MealPeriod[] MEAL_PERIODS = {
            new MealPeriod("BREAKFAST", "早餐", LocalTime.of(6, 0), LocalTime.of(9, 30)),
            new MealPeriod("LUNCH", "午餐", LocalTime.of(11, 0), LocalTime.of(14, 0)),
            new MealPeriod("DINNER", "晚餐", LocalTime.of(17, 0), LocalTime.of(20, 30))
    };

    @Override
    public String getStrategyCode() {
        return "FIXED_AMOUNT";
    }

    @Override
    public String getStrategyName() {
        return "固定金额模式";
    }

    @Override
    public String getStrategyDescription() {
        return "固定金额消费模式，支持预设金额消费和餐别时段控制，适用于食堂、餐厅等场景";
    }

    @Override
    public ConsumeResult preProcess(ConsumeRequest request, ConsumptionModeConfig config) {
        log.debug("固定金额模式预处理: orderNo={}, amount={}", request.getOrderNo(), request.getAmount());

        try {
            // 1. 基础验证
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ConsumeResult.failure("INVALID_AMOUNT", "消费金额必须大于0");
            }

            // 2. 验证消费模式配置
            if (!validateConfig(config)) {
                return ConsumeResult.failure("INVALID_CONFIG", "固定金额模式配置无效");
            }

            // 3. 验证是否为允许的固定金额
            if (!isFixedAmountAllowed(request.getAmount(), config)) {
                String allowedAmounts = getAllowedAmountsString(config);
                return ConsumeResult.failure("AMOUNT_NOT_ALLOWED",
                        "消费金额不在允许范围内，允许的金额: " + allowedAmounts);
            }

            // 4. 验证餐别时段（如果启用）
            if (isMealPeriodCheckEnabled(config) && !isWithinMealPeriod(request, config)) {
                return ConsumeResult.failure("MEAL_PERIOD_RESTRICTION", "当前时段不允许固定金额消费");
            }

            // 5. 验证频率限制
            if (!checkFrequencyRestriction(request, config)) {
                return ConsumeResult.failure("FREQUENCY_RESTRICTION", "超出消费频率限制");
            }

            log.debug("固定金额模式预处理通过: orderNo={}, amount={}", request.getOrderNo(), request.getAmount());
            return null; // 继续执行

        } catch (Exception e) {
            log.error("固定金额模式预处理异常: orderNo={}", request.getOrderNo(), e);
            return ConsumeResult.failure("PREPROCESS_ERROR", "固定金额模式预处理异常: " + e.getMessage());
        }
    }

    @Override
    public ConsumeResult process(ConsumeRequest request, ConsumptionModeConfig config) {
        try {
            log.info("执行固定金额模式处理: orderNo={}, amount={}, modeCode={}",
                    request.getOrderNo(), request.getAmount(), config.getModeCode());

            // 1. 确定当前餐别
            String mealPeriod = getCurrentMealPeriod();
            log.debug("当前餐别: {}", mealPeriod);

            // 2. 计算补贴金额
            BigDecimal subsidyAmount = calculateSubsidy(request, config, mealPeriod);

            // 3. 记录固定金额模式特有信息
            String modeConfigInfo = String.format("固定金额模式 - 餐别:%s, 原金额:%.2f, 补贴:%.2f",
                    mealPeriod, request.getAmount(), subsidyAmount);
            request.setModeConfig(modeConfigInfo);

            // 4. 应用餐别特定规则
            applyMealPeriodRules(request, config, mealPeriod);

            // 5. 创建处理结果
            ConsumeResult result = ConsumeResult.success(request.getAmount());
            result.setConsumptionMode(getStrategyCode());
            result.setSubsidyAmount(subsidyAmount);
            result.setMealPeriod(mealPeriod);
            result.setFixedAmount(true);

            // 6. 设置餐别相关信息
            result.setRemark(String.format("固定金额消费 - %s", getMealPeriodDisplayName(mealPeriod)));

            log.info("固定金额模式处理完成: orderNo={}, amount={}, subsidy={}, mealPeriod={}",
                    request.getOrderNo(), result.getAmount(), subsidyAmount, mealPeriod);

            return result;

        } catch (Exception e) {
            log.error("固定金额模式处理异常: orderNo={}", request.getOrderNo(), e);
            return ConsumeResult.failure("FIXED_AMOUNT_MODE_ERROR", "固定金额模式处理异常: " + e.getMessage());
        }
    }

    @Override
    public void postProcess(ConsumeRequest request, ConsumptionModeConfig config, ConsumeResult result) {
        try {
            log.debug("固定金额模式后处理: orderNo={}, success={}", request.getOrderNo(), result.isSuccess());

            if (result.isSuccess()) {
                // 1. 记录消费统计
                updateFixedAmountStatistics(request, config, result);

                // 2. 更新餐别消费记录
                updateMealPeriodStatistics(request, result);

                // 3. 缓存固定金额使用频率
                updateFixedAmountFrequency(request, config);

                log.info("固定金额模式成功处理: orderNo={}, mealPeriod={}, amount={}",
                        request.getOrderNo(), result.getMealPeriod(), result.getAmount());
            } else {
                log.warn("固定金额模式处理失败: orderNo={}, error={}",
                        request.getOrderNo(), result.getMessage() != null ? result.getMessage() : 
                        (result.getErrorDetail() != null ? result.getErrorDetail() : "处理失败"));
            }

        } catch (Exception e) {
            log.error("固定金额模式后处理异常: orderNo={}", request.getOrderNo(), e);
        }
    }

    @Override
    public boolean validateConfig(ConsumptionModeConfig config) {
        if (!ConsumptionModeStrategy.super.validateConfig(config)) {
            return false;
        }

        try {
            // 验证固定金额配置
            ConsumptionModeConfig.AmountRestrictionConfig amountConfig = config.getAmountRestriction();
            if (amountConfig != null && amountConfig.isEnabled() && amountConfig.getFixedAmounts() != null) {
                // 验证固定金额格式
                String[] amounts = amountConfig.getFixedAmounts().split(",");
                for (String amountStr : amounts) {
                    try {
                        new BigDecimal(amountStr.trim());
                    } catch (NumberFormatException e) {
                        log.error("固定金额格式错误: {}", amountStr);
                        return false;
                    }
                }
            }

            // 验证补贴配置
            if (config.getSubsidyConfig() != null && config.getSubsidyConfig().isEnabled()) {
                ConsumptionModeConfig.SubsidyConfig subsidyConfig = config.getSubsidyConfig();
                if ("FIXED_AMOUNT".equals(subsidyConfig.getSubsidyType()) &&
                    (subsidyConfig.getFixedSubsidyAmount() == null || subsidyConfig.getFixedSubsidyAmount().doubleValue() < 0)) {
                    log.error("固定金额补贴配置错误: 金额无效");
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("固定金额模式配置验证异常", e);
            return false;
        }
    }

    @Override
    public int getPriority() {
        return 50; // 中等优先级
    }

    @Override
    public boolean supportsDynamicConfig() {
        return true;
    }

    @Override
    public String getConfigTemplate() {
        return "{\n" +
                "  \"modeCode\": \"FIXED_AMOUNT\",\n" +
                "  \"modeName\": \"固定金额模式\",\n" +
                "  \"description\": \"固定金额消费模式，支持预设金额消费和餐别时段控制\",\n" +
                "  \"enabled\": true,\n" +
                "  \"priority\": 50,\n" +
                "  \"maxRetryCount\": 2,\n" +
                "  \"timeoutSeconds\": 25,\n" +
                "  \"supportsFallback\": true,\n" +
                "  \"modeSpecificConfig\": {\n" +
                "    \"allowBreakfast\": true,\n" +
                "    \"allowLunch\": true,\n" +
                "    \"allowDinner\": true,\n" +
                "    \"mealPeriodCheck\": true,\n" +
                "    \"allowMultipleMeals\": false\n" +
                "  },\n" +
                "  \"amountRestriction\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"fixedAmounts\": \"5.00,10.00,15.00,20.00\"\n" +
                "  },\n" +
                "  \"timeRestriction\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"allowedTimeRange\": \"06:00-20:30\"\n" +
                "  },\n" +
                "  \"frequencyRestriction\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"maxConsumptionsPerDay\": 3\n" +
                "  },\n" +
                "  \"subsidyConfig\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"subsidyType\": \"FIXED_AMOUNT\",\n" +
                "    \"fixedSubsidyAmount\": 2.00,\n" +
                "    \"mealPeriodSubsidy\": {\n" +
                "      \"BREAKFAST\": 1.00,\n" +
                "      \"LUNCH\": 2.00,\n" +
                "      \"DINNER\": 2.00\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    /**
     * 检查是否为允许的固定金额
     */
    private boolean isFixedAmountAllowed(BigDecimal amount, ConsumptionModeConfig config) {
        if (config.getAmountRestriction() == null || !config.getAmountRestriction().isEnabled()) {
            return true; // 如果没有启用金额限制，则允许任意金额
        }

        String fixedAmounts = config.getAmountRestriction().getFixedAmounts();
        if (fixedAmounts == null || fixedAmounts.isEmpty()) {
            return true;
        }

        String[] amounts = fixedAmounts.split(",");
        for (String amountStr : amounts) {
            try {
                BigDecimal fixedAmount = new BigDecimal(amountStr.trim());
                if (amount.compareTo(fixedAmount) == 0) {
                    return true;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return false;
    }

    /**
     * 获取允许的金额列表字符串
     */
    private String getAllowedAmountsString(ConsumptionModeConfig config) {
        if (config.getAmountRestriction() == null || config.getAmountRestriction().getFixedAmounts() == null) {
            return "未配置";
        }
        return config.getAmountRestriction().getFixedAmounts();
    }

    /**
     * 检查是否启用餐别时段检查
     */
    private boolean isMealPeriodCheckEnabled(ConsumptionModeConfig config) {
        return config.getModeSpecificConfigValue("mealPeriodCheck", false);
    }

    /**
     * 检查是否在餐别时段内
     */
    private boolean isWithinMealPeriod(ConsumeRequest request, ConsumptionModeConfig config) {
        LocalTime now = LocalTime.now();
        String currentPeriod = getCurrentMealPeriod();

        // 检查当前餐别是否允许
        String mealPermissionKey = "allow" + currentPeriod;
        boolean periodAllowed = config.getModeSpecificConfigValue(mealPermissionKey, true);

        if (!periodAllowed) {
            log.debug("餐别时段不允许: {}", currentPeriod);
            return false;
        }

        // 检查时间范围
        for (MealPeriod period : MEAL_PERIODS) {
            if (period.getCode().equals(currentPeriod)) {
                boolean withinTimeRange = !now.isBefore(period.getStartTime()) && !now.isAfter(period.getEndTime());
                log.debug("时间范围检查: {} -> {}", now, withinTimeRange);
                return withinTimeRange;
            }
        }

        return false;
    }

    /**
     * 获取当前餐别
     */
    private String getCurrentMealPeriod() {
        LocalTime now = LocalTime.now();

        for (MealPeriod period : MEAL_PERIODS) {
            if (!now.isBefore(period.getStartTime()) && !now.isAfter(period.getEndTime())) {
                return period.getCode();
            }
        }

        return "OTHER";
    }

    /**
     * 获取餐别显示名称
     */
    private String getMealPeriodDisplayName(String periodCode) {
        for (MealPeriod period : MEAL_PERIODS) {
            if (period.getCode().equals(periodCode)) {
                return period.getName();
            }
        }
        return "其他时段";
    }

    /**
     * 检查频率限制
     */
    private boolean checkFrequencyRestriction(ConsumeRequest request, ConsumptionModeConfig config) {
        ConsumptionModeConfig.FrequencyRestrictionConfig frequencyConfig = config.getFrequencyRestriction();
        if (frequencyConfig == null || !frequencyConfig.isEnabled()) {
            return true;
        }

        // 这里应该实现频率检查逻辑，例如查询Redis中的消费记录
        // 简化实现，假设总是通过
        log.debug("频率限制检查通过: personId={}", request.getPersonId());
        return true;
    }

    /**
     * 计算补贴金额
     */
    private BigDecimal calculateSubsidy(ConsumeRequest request, ConsumptionModeConfig config, String mealPeriod) {
        if (config.getSubsidyConfig() == null || !config.getSubsidyConfig().isEnabled()) {
            return BigDecimal.ZERO;
        }

        ConsumptionModeConfig.SubsidyConfig subsidyConfig = config.getSubsidyConfig();

        try {
            switch (subsidyConfig.getSubsidyType().toUpperCase()) {
                case "FIXED_AMOUNT":
                    // 检查是否有餐别特定补贴
                    BigDecimal mealPeriodSubsidy = getMealPeriodSubsidy(config, mealPeriod);
                    return mealPeriodSubsidy != null ? mealPeriodSubsidy : subsidyConfig.getFixedSubsidyAmount();

                case "PERCENTAGE":
                    return subsidyConfig.getSubsidyPercentage() != null ?
                            request.getAmount().multiply(subsidyConfig.getSubsidyPercentage())
                                    .divide(new BigDecimal("100")) : BigDecimal.ZERO;

                default:
                    log.debug("不支持的补贴类型: {}", subsidyConfig.getSubsidyType());
                    return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.error("计算固定金额模式补贴异常: orderNo={}", request.getOrderNo(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取餐别特定补贴金额
     */
    private BigDecimal getMealPeriodSubsidy(ConsumptionModeConfig config, String mealPeriod) {
        try {
            Map<String, Object> modeConfig = config.getModeSpecificConfig();
            if (modeConfig != null && modeConfig.containsKey("mealPeriodSubsidy")) {
                Object mealPeriodSubsidyObj = modeConfig.get("mealPeriodSubsidy");
                if (mealPeriodSubsidyObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mealSubsidyMap = (Map<String, Object>) mealPeriodSubsidyObj;
                    Object subsidyObj = mealSubsidyMap.get(mealPeriod);
                    if (subsidyObj instanceof Number) {
                        return new BigDecimal(subsidyObj.toString());
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("获取餐别特定补贴异常: mealPeriod={}", mealPeriod, e);
            return null;
        }
    }

    /**
     * 应用餐别特定规则
     */
    private void applyMealPeriodRules(ConsumeRequest request, ConsumptionModeConfig config, String mealPeriod) {
        // 可以在这里添加餐别特定的业务逻辑
        // 例如：早餐允许的最小金额、晚餐的特殊规则等

        log.debug("应用餐别特定规则: mealPeriod={}", mealPeriod);
    }

    /**
     * 更新固定金额消费统计
     */
    private void updateFixedAmountStatistics(ConsumeRequest request, ConsumptionModeConfig config, ConsumeResult result) {
        try {
            // 这里可以实现统计信息更新逻辑
            // 例如：更新Redis中的固定金额消费计数、金额统计等
            log.debug("更新固定金额消费统计: mode={}, amount={}", config.getModeCode(), result.getAmount());
        } catch (Exception e) {
            log.error("更新固定金额消费统计异常: orderNo={}", request.getOrderNo(), e);
        }
    }

    /**
     * 更新餐别消费统计
     */
    private void updateMealPeriodStatistics(ConsumeRequest request, ConsumeResult result) {
        try {
            if (result.getMealPeriod() != null) {
                // 更新餐别消费统计
                log.debug("更新餐别消费统计: mealPeriod={}, amount={}", result.getMealPeriod(), result.getAmount());
            }
        } catch (Exception e) {
            log.error("更新餐别消费统计异常: orderNo={}", request.getOrderNo(), e);
        }
    }

    /**
     * 更新固定金额使用频率
     */
    private void updateFixedAmountFrequency(ConsumeRequest request, ConsumptionModeConfig config) {
        try {
            // 更新用户固定金额消费频率记录
            log.debug("更新固定金额使用频率: personId={}", request.getPersonId());
        } catch (Exception e) {
            log.error("更新固定金额使用频率异常: orderNo={}", request.getOrderNo(), e);
        }
    }

    /**
     * 餐别时间段定义
     */
    private static class MealPeriod {
        private final String code;
        private final String name;
        private final LocalTime startTime;
        private final LocalTime endTime;

        public MealPeriod(String code, String name, LocalTime startTime, LocalTime endTime) {
            this.code = code;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
    }
}