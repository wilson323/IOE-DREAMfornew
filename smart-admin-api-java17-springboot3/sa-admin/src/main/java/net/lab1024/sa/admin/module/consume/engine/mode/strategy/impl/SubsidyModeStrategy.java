package net.lab1024.sa.admin.module.consume.engine.mode.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.engine.mode.config.ConsumptionModeConfig;
import net.lab1024.sa.admin.module.consume.engine.mode.strategy.ConsumptionModeStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 补贴消费模式策略
 * 支持固定补贴、比例补贴、分级补贴等多种补贴模式
 * 适用于员工福利、政府补贴等场景
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class SubsidyModeStrategy implements ConsumptionModeStrategy {

    @Override
    public String getStrategyCode() {
        return "SUBSIDY";
    }

    @Override
    public String getStrategyName() {
        return "补贴消费模式";
    }

    @Override
    public String getStrategyDescription() {
        return "补贴消费模式，支持固定补贴、比例补贴、分级补贴等多种补贴计算方式";
    }

    @Override
    public ConsumeResult preProcess(ConsumeRequest request, ConsumptionModeConfig config) {
        log.debug("补贴消费模式预处理: orderNo={}, amount={}", request.getOrderNo(), request.getAmount());

        try {
            // 1. 基础验证
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ConsumeResult.failure("INVALID_AMOUNT", "消费金额必须大于0");
            }

            // 2. 验证消费模式配置
            if (!validateConfig(config)) {
                return ConsumeResult.failure("INVALID_CONFIG", "补贴消费模式配置无效");
            }

            // 3. 验证补贴配置
            if (config.getSubsidyConfig() == null || !config.getSubsidyConfig().isEnabled()) {
                return ConsumeResult.failure("SUBSIDY_NOT_ENABLED", "补贴功能未启用");
            }

            // 4. 验证补贴时间范围
            if (!isWithinSubsidyTimeRange(config)) {
                return ConsumeResult.failure("SUBSIDY_TIME_RESTRICTION", "当前时间不在补贴时间范围内");
            }

            // 5. 验证补贴金额限制
            if (!isWithinSubsidyLimit(request, config)) {
                return ConsumeResult.failure("SUBSIDY_LIMIT_EXCEEDED", "超出补贴金额限制");
            }

            log.debug("补贴消费模式预处理通过: orderNo={}, amount={}", request.getOrderNo(), request.getAmount());
            return null; // 继续执行

        } catch (Exception e) {
            log.error("补贴消费模式预处理异常: orderNo={}", request.getOrderNo(), e);
            return ConsumeResult.failure("PREPROCESS_ERROR", "补贴消费模式预处理异常: " + e.getMessage());
        }
    }

    @Override
    public ConsumeResult process(ConsumeRequest request, ConsumptionModeConfig config) {
        try {
            log.info("执行补贴消费模式处理: orderNo={}, amount={}, modeCode={}",
                    request.getOrderNo(), request.getAmount(), config.getModeCode());

            // 1. 计算补贴金额
            SubsidyCalculationResult subsidyResult = calculateSubsidy(request, config);
            log.debug("补贴计算结果: originalAmount={}, subsidyAmount={}, finalAmount={}, subsidyType={}",
                    request.getAmount(), subsidyResult.getSubsidyAmount(), subsidyResult.getFinalAmount(), subsidyResult.getSubsidyType());

            // 2. 验证补贴结果
            if (subsidyResult.getSubsidyAmount().compareTo(BigDecimal.ZERO) < 0) {
                return ConsumeResult.failure("INVALID_SUBSIDY", "补贴金额计算错误");
            }

            // 3. 检查每日补贴限额
            if (!checkDailySubsidyLimit(request, config, subsidyResult.getSubsidyAmount())) {
                return ConsumeResult.failure("DAILY_SUBSIDY_LIMIT_EXCEEDED", "超出每日补贴限额");
            }

            // 4. 记录补贴消费模式特有信息
            String modeConfigInfo = String.format("补贴消费模式 - 补贴类型:%s, 原金额:%.2f, 补贴金额:%.2f, 最终金额:%.2f",
                    subsidyResult.getSubsidyType(), request.getAmount(), subsidyResult.getSubsidyAmount(), subsidyResult.getFinalAmount());
            request.setModeConfig(modeConfigInfo);

            // 5. 创建处理结果
            ConsumeResult result = ConsumeResult.success(request.getAmount());
            result.setConsumptionMode(getStrategyCode());
            result.setSubsidyAmount(subsidyResult.getSubsidyAmount());
            result.setSubsidyType(subsidyResult.getSubsidyType());
            result.setSubsidyRule(subsidyResult.getSubsidyRule());
            result.setFinalAmount(subsidyResult.getFinalAmount());

            // 6. 设置补贴相关信息
            result.setRemark(String.format("补贴消费 - %s补贴，补贴金额: %.2f",
                    getSubsidyTypeDisplayName(subsidyResult.getSubsidyType()), subsidyResult.getSubsidyAmount()));

            log.info("补贴消费模式处理完成: orderNo={}, amount={}, subsidy={}, final={}, type={}",
                    request.getOrderNo(), result.getAmount(), result.getSubsidyAmount(),
                    result.getFinalAmount(), result.getSubsidyType());

            return result;

        } catch (Exception e) {
            log.error("补贴消费模式处理异常: orderNo={}", request.getOrderNo(), e);
            return ConsumeResult.failure("SUBSIDY_MODE_ERROR", "补贴消费模式处理异常: " + e.getMessage());
        }
    }

    @Override
    public void postProcess(ConsumeRequest request, ConsumptionModeConfig config, ConsumeResult result) {
        try {
            log.debug("补贴消费模式后处理: orderNo={}, success={}", request.getOrderNo(), result.isSuccess());

            if (result.isSuccess()) {
                // 1. 更新补贴统计
                updateSubsidyStatistics(request, config, result);

                // 2. 更新每日补贴使用记录
                updateDailySubsidyUsage(request, result);

                // 3. 记录补贴消费历史
                recordSubsidyConsumptionHistory(request, result);

                log.info("补贴消费模式成功处理: orderNo={}, subsidy={}, type={}",
                        request.getOrderNo(), result.getSubsidyAmount(), result.getSubsidyType());
            } else {
                log.warn("补贴消费模式处理失败: orderNo={}, error={}",
                        request.getOrderNo(), result.getMessage() != null ? result.getMessage() : 
                        (result.getErrorDetail() != null ? result.getErrorDetail() : "处理失败"));
            }

        } catch (Exception e) {
            log.error("补贴消费模式后处理异常: orderNo={}", request.getOrderNo(), e);
        }
    }

    @Override
    public boolean validateConfig(ConsumptionModeConfig config) {
        if (!ConsumptionModeStrategy.super.validateConfig(config)) {
            return false;
        }

        try {
            // 验证补贴配置
            ConsumptionModeConfig.SubsidyConfig subsidyConfig = config.getSubsidyConfig();
            if (subsidyConfig == null) {
                return false;
            }

            // 验证补贴类型
            if (subsidyConfig.getSubsidyType() == null || subsidyConfig.getSubsidyType().isEmpty()) {
                log.error("补贴类型不能为空");
                return false;
            }

            // 根据补贴类型验证相应配置
            switch (subsidyConfig.getSubsidyType().toUpperCase()) {
                case "FIXED_AMOUNT":
                    if (subsidyConfig.getFixedSubsidyAmount() == null ||
                        subsidyConfig.getFixedSubsidyAmount().doubleValue() < 0) {
                        log.error("固定补贴金额配置错误");
                        return false;
                    }
                    break;

                case "PERCENTAGE":
                    if (subsidyConfig.getSubsidyPercentage() == null ||
                        subsidyConfig.getSubsidyPercentage().doubleValue() < 0 ||
                        subsidyConfig.getSubsidyPercentage().doubleValue() > 100) {
                        log.error("补贴比例配置错误");
                        return false;
                    }
                    break;

                case "TIERED":
                    if (subsidyConfig.getTieredSubsidyRules() == null ||
                        subsidyConfig.getTieredSubsidyRules().isEmpty()) {
                        log.error("分级补贴规则配置错误");
                        return false;
                    }
                    break;

                default:
                    log.error("不支持的补贴类型: {}", subsidyConfig.getSubsidyType());
                    return false;
            }

            return true;

        } catch (Exception e) {
            log.error("补贴消费模式配置验证异常", e);
            return false;
        }
    }

    @Override
    public int getPriority() {
        return 30; // 中等优先级
    }

    @Override
    public boolean supportsDynamicConfig() {
        return true;
    }

    @Override
    public String getConfigTemplate() {
        return "{\n" +
                "  \"modeCode\": \"SUBSIDY\",\n" +
                "  \"modeName\": \"补贴消费模式\",\n" +
                "  \"description\": \"补贴消费模式，支持固定补贴、比例补贴、分级补贴等多种补贴计算方式\",\n" +
                "  \"enabled\": true,\n" +
                "  \"priority\": 30,\n" +
                "  \"maxRetryCount\": 2,\n" +
                "  \"timeoutSeconds\": 25,\n" +
                "  \"supportsFallback\": true,\n" +
                "  \"modeSpecificConfig\": {\n" +
                "    \"subsidyPriority\": true,\n" +
                "    \"allowZeroSubsidy\": false,\n" +
                "    \"maxSubsidyRatio\": 0.8\n" +
                "  },\n" +
                "  \"amountRestriction\": {\n" +
                "    \"enabled\": false\n" +
                "  },\n" +
                "  \"subsidyConfig\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"subsidyType\": \"FIXED_AMOUNT\",\n" +
                "    \"fixedSubsidyAmount\": 5.00,\n" +
                "    \"dailyMaxSubsidy\": 50.00,\n" +
                "    \"subsidyStartTime\": \"2025-01-01 00:00:00\",\n" +
                "    \"subsidyEndTime\": \"2025-12-31 23:59:59\"\n" +
                "  }\n" +
                "}";
    }

    /**
     * 检查是否在补贴时间范围内
     */
    private boolean isWithinSubsidyTimeRange(ConsumptionModeConfig config) {
        ConsumptionModeConfig.SubsidyConfig subsidyConfig = config.getSubsidyConfig();
        if (subsidyConfig.getSubsidyStartTime() == null && subsidyConfig.getSubsidyEndTime() == null) {
            return true; // 没有时间限制
        }

        LocalDateTime now = LocalDateTime.now();

        if (subsidyConfig.getSubsidyStartTime() != null && now.isBefore(subsidyConfig.getSubsidyStartTime())) {
            log.debug("补贴未开始时间: now={}, start={}", now, subsidyConfig.getSubsidyStartTime());
            return false;
        }

        if (subsidyConfig.getSubsidyEndTime() != null && now.isAfter(subsidyConfig.getSubsidyEndTime())) {
            log.debug("补贴已结束时间: now={}, end={}", now, subsidyConfig.getSubsidyEndTime());
            return false;
        }

        return true;
    }

    /**
     * 检查是否在补贴金额限制内
     */
    private boolean isWithinSubsidyLimit(ConsumeRequest request, ConsumptionModeConfig config) {
        ConsumptionModeConfig.SubsidyConfig subsidyConfig = config.getSubsidyConfig();
        if (subsidyConfig.getDailyMaxSubsidy() == null) {
            return true; // 没有每日补贴限制
        }

        // 这里应该查询用户当日已使用的补贴金额
        // 简化实现，假设总是通过
        return true;
    }

    /**
     * 计算补贴金额
     */
    private SubsidyCalculationResult calculateSubsidy(ConsumeRequest request, ConsumptionModeConfig config) {
        ConsumptionModeConfig.SubsidyConfig subsidyConfig = config.getSubsidyConfig();
        BigDecimal originalAmount = request.getAmount();
        String subsidyType = subsidyConfig.getSubsidyType().toUpperCase();

        try {
            switch (subsidyType) {
                case "FIXED_AMOUNT":
                    return calculateFixedAmountSubsidy(originalAmount, subsidyConfig);

                case "PERCENTAGE":
                    return calculatePercentageSubsidy(originalAmount, subsidyConfig);

                case "TIERED":
                    return calculateTieredSubsidy(originalAmount, subsidyConfig);

                default:
                    log.error("不支持的补贴类型: {}", subsidyType);
                    return new SubsidyCalculationResult(BigDecimal.ZERO, originalAmount, "UNKNOWN", "不支持的补贴类型");
            }
        } catch (Exception e) {
            log.error("计算补贴金额异常: orderNo={}, type={}", request.getOrderNo(), subsidyType, e);
            return new SubsidyCalculationResult(BigDecimal.ZERO, originalAmount, "ERROR", "补贴计算异常: " + e.getMessage());
        }
    }

    /**
     * 计算固定金额补贴
     */
    private SubsidyCalculationResult calculateFixedAmountSubsidy(BigDecimal amount,
            ConsumptionModeConfig.SubsidyConfig subsidyConfig) {

        BigDecimal subsidyAmount = subsidyConfig.getFixedSubsidyAmount();

        // 检查补贴比例限制
        BigDecimal maxSubsidyRatio = subsidyConfig.getFixedSubsidyAmount().compareTo(BigDecimal.ZERO) > 0 ?
                subsidyConfig.getFixedSubsidyAmount().divide(amount, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        if (maxSubsidyRatio.compareTo(new BigDecimal("1.0")) > 0) {
            subsidyAmount = amount; // 补贴不能超过原金额
        }

        String rule = String.format("固定补贴: %.2f", subsidyAmount);
        return new SubsidyCalculationResult(subsidyAmount, amount, "FIXED_AMOUNT", rule);
    }

    /**
     * 计算比例补贴
     */
    private SubsidyCalculationResult calculatePercentageSubsidy(BigDecimal amount,
            ConsumptionModeConfig.SubsidyConfig subsidyConfig) {

        BigDecimal subsidyPercentage = subsidyConfig.getSubsidyPercentage();
        BigDecimal subsidyAmount = amount.multiply(subsidyPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        String rule = String.format("比例补贴: %s%%", subsidyPercentage);
        return new SubsidyCalculationResult(subsidyAmount, amount, "PERCENTAGE", rule);
    }

    /**
     * 计算分级补贴
     */
    private SubsidyCalculationResult calculateTieredSubsidy(BigDecimal amount,
            ConsumptionModeConfig.SubsidyConfig subsidyConfig) {

        String tieredRules = subsidyConfig.getTieredSubsidyRules();
        BigDecimal subsidyAmount = BigDecimal.ZERO;
        String appliedRule = "无匹配规则";

        try {
            // 解析分级规则，格式示例: "0-10:1.00;10-20:2.00;20-999999:3.00"
            String[] rules = tieredRules.split(";");
            for (String rule : rules) {
                String[] parts = rule.split(":");
                if (parts.length != 2) continue;

                String range = parts[0].trim();
                String subsidyAmountStr = parts[1].trim();

                if (isAmountInRange(amount, range)) {
                    subsidyAmount = new BigDecimal(subsidyAmountStr);
                    appliedRule = String.format("分级补贴: %s范围 -> %s", range, subsidyAmountStr);
                    break;
                }
            }

        } catch (Exception e) {
            log.error("解析分级补贴规则异常: {}", tieredRules, e);
        }

        return new SubsidyCalculationResult(subsidyAmount, amount, "TIERED", appliedRule);
    }

    /**
     * 检查金额是否在指定范围内
     */
    private boolean isAmountInRange(BigDecimal amount, String range) {
        try {
            String[] bounds = range.split("-");
            if (bounds.length != 2) return false;

            BigDecimal lowerBound = new BigDecimal(bounds[0].trim());
            BigDecimal upperBound = new BigDecimal(bounds[1].trim());

            return amount.compareTo(lowerBound) >= 0 && amount.compareTo(upperBound) <= 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查每日补贴限额
     */
    private boolean checkDailySubsidyLimit(ConsumeRequest request, ConsumptionModeConfig config, BigDecimal currentSubsidy) {
        ConsumptionModeConfig.SubsidyConfig subsidyConfig = config.getSubsidyConfig();
        if (subsidyConfig.getDailyMaxSubsidy() == null) {
            return true;
        }

        // 这里应该查询用户当日已使用的补贴金额
        // 简化实现，假设当日已使用补贴为0
        BigDecimal dailyUsedSubsidy = BigDecimal.ZERO;

        BigDecimal totalDailySubsidy = dailyUsedSubsidy.add(currentSubsidy);
        return totalDailySubsidy.compareTo(subsidyConfig.getDailyMaxSubsidy()) <= 0;
    }

    /**
     * 获取补贴类型显示名称
     */
    private String getSubsidyTypeDisplayName(String subsidyType) {
        switch (subsidyType.toUpperCase()) {
            case "FIXED_AMOUNT":
                return "固定金额";
            case "PERCENTAGE":
                return "比例";
            case "TIERED":
                return "分级";
            default:
                return "未知";
        }
    }

    /**
     * 更新补贴统计
     */
    private void updateSubsidyStatistics(ConsumeRequest request, ConsumptionModeConfig config, ConsumeResult result) {
        try {
            // 这里可以实现补贴统计更新逻辑
            log.debug("更新补贴统计: personId={}, subsidyAmount={}, type={}",
                    request.getPersonId(), result.getSubsidyAmount(), result.getSubsidyType());
        } catch (Exception e) {
            log.error("更新补贴统计异常: orderNo={}", request.getOrderNo(), e);
        }
    }

    /**
     * 更新每日补贴使用记录
     */
    private void updateDailySubsidyUsage(ConsumeRequest request, ConsumeResult result) {
        try {
            // 这里可以更新Redis中的每日补贴使用记录
            String dailyKey = String.format("subsidy:daily:%s:%s",
                    request.getPersonId(), LocalDateTime.now().toLocalDate());

            log.debug("更新每日补贴使用记录: key={}, amount={}", dailyKey, result.getSubsidyAmount());
        } catch (Exception e) {
            log.error("更新每日补贴使用记录异常: orderNo={}", request.getOrderNo(), e);
        }
    }

    /**
     * 记录补贴消费历史
     */
    private void recordSubsidyConsumptionHistory(ConsumeRequest request, ConsumeResult result) {
        try {
            // 这里可以记录补贴消费历史到数据库或缓存
            log.debug("记录补贴消费历史: personId={}, amount={}, subsidy={}, type={}",
                    request.getPersonId(), result.getAmount(), result.getSubsidyAmount(), result.getSubsidyType());
        } catch (Exception e) {
            log.error("记录补贴消费历史异常: orderNo={}", request.getOrderNo(), e);
        }
    }

    /**
     * 补贴计算结果
     */
    private static class SubsidyCalculationResult {
        private final BigDecimal subsidyAmount;
        private final BigDecimal originalAmount;
        private final String subsidyType;
        private final String subsidyRule;

        public SubsidyCalculationResult(BigDecimal subsidyAmount, BigDecimal originalAmount,
                                       String subsidyType, String subsidyRule) {
            this.subsidyAmount = subsidyAmount;
            this.originalAmount = originalAmount;
            this.subsidyType = subsidyType;
            this.subsidyRule = subsidyRule;
        }

        public BigDecimal getSubsidyAmount() { return subsidyAmount; }
        public BigDecimal getOriginalAmount() { return originalAmount; }
        public BigDecimal getFinalAmount() { return originalAmount.subtract(subsidyAmount); }
        public String getSubsidyType() { return subsidyType; }
        public String getSubsidyRule() { return subsidyRule; }
    }
}