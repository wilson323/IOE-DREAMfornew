package net.lab1024.sa.admin.module.consume.engine.mode.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.engine.mode.config.ConsumptionModeConfig;
import net.lab1024.sa.admin.module.consume.engine.mode.strategy.ConsumptionModeStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 标准消费模式策略
 * 基础的消费模式实现，作为默认策略和降级策略
 * 支持基本的金额验证和标准消费流程
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class StandardConsumptionModeStrategy implements ConsumptionModeStrategy {

    @Override
    public String getStrategyCode() {
        return "STANDARD";
    }

    @Override
    public String getStrategyName() {
        return "标准消费模式";
    }

    @Override
    public String getStrategyDescription() {
        return "标准消费模式，支持基本的金额验证和消费流程";
    }

    @Override
    public ConsumeResult preProcess(ConsumeRequest request, ConsumptionModeConfig config) {
        log.debug("标准消费模式预处理: orderNo={}, amount={}", request.getOrderNo(), request.getAmount());

        // 1. 验证基本参数
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ConsumeResult.failure("INVALID_AMOUNT", "消费金额必须大于0");
        }

        // 2. 验证消费模式配置
        if (!validateConfig(config)) {
            return ConsumeResult.failure("INVALID_CONFIG", "消费模式配置无效");
        }

        // 3. 验证金额限制
        if (!config.isAmountWithinRestriction(request.getAmount())) {
            return ConsumeResult.failure("AMOUNT_RESTRICTION", "消费金额不符合限制要求");
        }

        // 4. 验证时间限制
        if (!config.isWithinValidTimeRange()) {
            return ConsumeResult.failure("TIME_RESTRICTION", "当前时间不允许此消费模式");
        }

        log.debug("标准消费模式预处理通过: orderNo={}", request.getOrderNo());
        return null; // 继续执行
    }

    @Override
    public ConsumeResult process(ConsumeRequest request, ConsumptionModeConfig config) {
        try {
            log.info("执行标准消费模式处理: orderNo={}, amount={}, modeCode={}",
                    request.getOrderNo(), request.getAmount(), config.getModeCode());

            // 标准消费模式的核心处理逻辑
            // 这里主要是验证和准备，实际的业务逻辑在ConsumeEngineService中处理

            // 1. 记录消费模式信息
            request.setModeConfig(config.toString());

            // 2. 应用补贴逻辑（如果有配置）
            BigDecimal subsidyAmount = calculateSubsidy(request, config);

            // 3. 返回成功结果（实际扣款等操作由上层服务处理）
            ConsumeResult result = ConsumeResult.success(request.getAmount());
            result.setConsumptionMode(getStrategyCode());
            result.setSubsidyAmount(subsidyAmount);

            log.info("标准消费模式处理完成: orderNo={}, success={}, subsidy={}",
                    request.getOrderNo(), result.isSuccess(), subsidyAmount);

            return result;

        } catch (Exception e) {
            log.error("标准消费模式处理异常: orderNo={}", request.getOrderNo(), e);
            return ConsumeResult.failure("STANDARD_MODE_ERROR", "标准消费模式处理异常: " + e.getMessage());
        }
    }

    @Override
    public void postProcess(ConsumeRequest request, ConsumptionModeConfig config, ConsumeResult result) {
        try {
            log.debug("标准消费模式后处理: orderNo={}, success={}", request.getOrderNo(), result.isSuccess());

            if (result.isSuccess()) {
                // 记录成功处理日志
                log.info("标准消费模式成功处理: orderNo={}, amount={}, balanceAfter={}",
                        request.getOrderNo(), result.getAmount(), result.getBalanceAfter());

                // 可以在这里添加统计信息、缓存更新等后处理逻辑
                updateConsumptionStatistics(request, config, result);
            } else {
                // 记录失败处理日志
                log.warn("标准消费模式处理失败: orderNo={}, error={}",
                        request.getOrderNo(), result.getMessage() != null ? result.getMessage() : 
                        (result.getErrorDetail() != null ? result.getErrorDetail() : "处理失败"));
            }

        } catch (Exception e) {
            log.error("标准消费模式后处理异常: orderNo={}", request.getOrderNo(), e);
            // 后处理异常不应影响主流程
        }
    }

    @Override
    public boolean validateConfig(ConsumptionModeConfig config) {
        if (!ConsumptionModeStrategy.super.validateConfig(config)) {
            return false;
        }

        // 标准消费模式的特定验证
        try {
            // 验证优先级
            if (config.getPriority() < 0 || config.getPriority() > 999) {
                log.warn("标准消费模式配置优先级超出范围: {}", config.getPriority());
                return false;
            }

            // 验证超时时间
            if (config.getTimeoutSeconds() < 5 || config.getTimeoutSeconds() > 120) {
                log.warn("标准消费模式配置超时时间不合理: {}", config.getTimeoutSeconds());
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("标准消费模式配置验证异常", e);
            return false;
        }
    }

    @Override
    public int getPriority() {
        return 1; // 最高优先级，作为默认策略
    }

    @Override
    public boolean supportsDynamicConfig() {
        return true; // 支持动态配置更新
    }

    @Override
    public String getConfigTemplate() {
        return "{\n" +
                "  \"modeCode\": \"STANDARD\",\n" +
                "  \"modeName\": \"标准消费模式\",\n" +
                "  \"description\": \"标准消费模式，支持基本的金额验证和消费流程\",\n" +
                "  \"enabled\": true,\n" +
                "  \"priority\": 1,\n" +
                "  \"maxRetryCount\": 3,\n" +
                "  \"timeoutSeconds\": 30,\n" +
                "  \"supportsFallback\": true,\n" +
                "  \"amountRestriction\": {\n" +
                "    \"enabled\": false,\n" +
                "    \"minAmount\": 0.01,\n" +
                "    \"maxAmount\": 1000.00\n" +
                "  },\n" +
                "  \"timeRestriction\": {\n" +
                "    \"enabled\": false,\n" +
                "    \"allowedTimeRange\": \"06:00-22:00\",\n" +
                "    \"allowedWeekdays\": \"1,2,3,4,5,6,7\"\n" +
                "  },\n" +
                "  \"frequencyRestriction\": {\n" +
                "    \"enabled\": false,\n" +
                "    \"maxConsumptionsPerDay\": 50\n" +
                "  }\n" +
                "}";
    }

    /**
     * 计算补贴金额
     *
     * @param request 消费请求
     * @param config 消费模式配置
     * @return 补贴金额
     */
    private BigDecimal calculateSubsidy(ConsumeRequest request, ConsumptionModeConfig config) {
        if (config.getSubsidyConfig() == null || !config.getSubsidyConfig().isEnabled()) {
            return BigDecimal.ZERO;
        }

        ConsumptionModeConfig.SubsidyConfig subsidyConfig = config.getSubsidyConfig();

        try {
            switch (subsidyConfig.getSubsidyType().toUpperCase()) {
                case "FIXED_AMOUNT":
                    return subsidyConfig.getFixedSubsidyAmount() != null ?
                            subsidyConfig.getFixedSubsidyAmount() : BigDecimal.ZERO;

                case "PERCENTAGE":
                    return subsidyConfig.getSubsidyPercentage() != null ?
                            request.getAmount().multiply(subsidyConfig.getSubsidyPercentage())
                                    .divide(new BigDecimal("100")) : BigDecimal.ZERO;

                default:
                    log.debug("不支持的补贴类型: {}", subsidyConfig.getSubsidyType());
                    return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.error("计算补贴金额异常: orderNo={}", request.getOrderNo(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 更新消费统计信息
     *
     * @param request 消费请求
     * @param config 消费模式配置
     * @param result 处理结果
     */
    private void updateConsumptionStatistics(ConsumeRequest request, ConsumptionModeConfig config, ConsumeResult result) {
        try {
            // 这里可以实现统计信息更新逻辑
            // 例如：更新Redis中的消费计数、金额统计等

            log.debug("更新消费统计信息: mode={}, amount={}", config.getModeCode(), result.getAmount());

            // 示例：更新模式使用次数
            // redisUtil.increment("consume:mode:stats:" + config.getModeCode() + ":count", 1);

            // 示例：更新模式消费总金额
            // redisUtil.increment("consume:mode:stats:" + config.getModeCode() + ":amount", result.getAmount().doubleValue());

        } catch (Exception e) {
            log.error("更新消费统计信息异常: orderNo={}", request.getOrderNo(), e);
        }
    }
}