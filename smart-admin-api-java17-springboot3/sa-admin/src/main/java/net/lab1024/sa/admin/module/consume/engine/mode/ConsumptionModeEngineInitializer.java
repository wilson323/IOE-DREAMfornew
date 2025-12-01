package net.lab1024.sa.admin.module.consume.engine.mode;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.mode.config.ConsumptionModeConfig;
import net.lab1024.sa.admin.module.consume.engine.mode.config.ModeConfigurationParser;
import net.lab1024.sa.admin.module.consume.engine.mode.strategy.ConsumptionModeStrategy;
import net.lab1024.sa.admin.module.consume.engine.mode.strategy.impl.StandardConsumptionModeStrategy;
import net.lab1024.sa.admin.module.consume.engine.mode.strategy.impl.FixedAmountModeStrategy;
import net.lab1024.sa.admin.module.consume.engine.mode.strategy.impl.SubsidyModeStrategy;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * 消费模式引擎初始化器
 * 负责在应用启动时注册所有消费模式策略和默认配置
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class ConsumptionModeEngineInitializer {

    @Resource
    private ConsumptionModeEngine modeEngine;

    @Resource
    private ModeConfigurationParser configurationParser;

    @Resource
    private StandardConsumptionModeStrategy standardStrategy;

    // 可以注入其他策略实现
    @Resource
    private FixedAmountModeStrategy fixedAmountStrategy;

    @Resource
    private SubsidyModeStrategy subsidyStrategy;

    /**
     * 初始化消费模式引擎
     */
    @PostConstruct
    public void initialize() {
        try {
            log.info("开始初始化消费模式引擎...");

            // 1. 注册默认策略
            registerDefaultStrategies();

            // 2. 创建默认配置
            createDefaultConfigurations();

            // 3. 验证引擎状态
            validateEngineStatus();

            log.info("消费模式引擎初始化完成");

        } catch (Exception e) {
            log.error("消费模式引擎初始化失败", e);
            throw new RuntimeException("消费模式引擎初始化失败: " + e.getMessage());
        }
    }

    /**
     * 注册默认策略
     */
    private void registerDefaultStrategies() {
        log.info("注册默认消费模式策略...");

        try {
            // 注册标准消费模式策略
            modeEngine.registerStrategy(standardStrategy.getStrategyCode(), standardStrategy);
            log.info("已注册策略: {} -> {}", standardStrategy.getStrategyCode(), standardStrategy.getStrategyName());

            // 注册固定金额消费模式策略
            if (fixedAmountStrategy != null) {
                modeEngine.registerStrategy(fixedAmountStrategy.getStrategyCode(), fixedAmountStrategy);
                log.info("已注册策略: {} -> {}", fixedAmountStrategy.getStrategyCode(), fixedAmountStrategy.getStrategyName());
            }

            // 注册补贴消费模式策略
            if (subsidyStrategy != null) {
                modeEngine.registerStrategy(subsidyStrategy.getStrategyCode(), subsidyStrategy);
                log.info("已注册策略: {} -> {}", subsidyStrategy.getStrategyCode(), subsidyStrategy.getStrategyName());
            }

            log.info("默认策略注册完成，共注册 {} 个策略", modeEngine.getAllSupportedModes().size());

        } catch (Exception e) {
            log.error("注册默认策略失败", e);
            throw e;
        }
    }

    /**
     * 创建默认配置
     */
    private void createDefaultConfigurations() {
        log.info("创建默认消费模式配置...");

        try {
            // 创建标准消费模式配置
            String standardConfigJson = createStandardModeConfig();
            modeEngine.parseAndCacheConfig(standardConfigJson);
            log.info("已创建标准消费模式默认配置");

            // 创建固定金额消费模式配置
            String fixedAmountConfigJson = createFixedAmountModeConfig();
            modeEngine.parseAndCacheConfig(fixedAmountConfigJson);
            log.info("已创建固定金额消费模式默认配置");

            // 创建补贴消费模式配置
            String subsidyConfigJson = createSubsidyModeConfig();
            modeEngine.parseAndCacheConfig(subsidyConfigJson);
            log.info("已创建补贴消费模式默认配置");

            log.info("默认配置创建完成");

        } catch (Exception e) {
            log.error("创建默认配置失败", e);
            throw e;
        }
    }

    /**
     * 创建标准消费模式配置
     */
    private String createStandardModeConfig() {
        return "{\n" +
                "  \"modeCode\": \"STANDARD\",\n" +
                "  \"modeName\": \"标准消费模式\",\n" +
                "  \"description\": \"标准消费模式，支持基本的金额验证和消费流程\",\n" +
                "  \"enabled\": true,\n" +
                "  \"priority\": 1,\n" +
                "  \"maxRetryCount\": 3,\n" +
                "  \"timeoutSeconds\": 30,\n" +
                "  \"supportsFallback\": true,\n" +
                "  \"modeSpecificConfig\": {\n" +
                "    \"allowOverdraft\": false,\n" +
                "    \"requirePin\": false,\n" +
                "    \"logLevel\": \"INFO\"\n" +
                "  },\n" +
                "  \"amountRestriction\": {\n" +
                "    \"enabled\": false,\n" +
                "    \"minAmount\": 0.01,\n" +
                "    \"maxAmount\": 10000.00\n" +
                "  },\n" +
                "  \"timeRestriction\": {\n" +
                "    \"enabled\": false,\n" +
                "    \"allowedTimeRange\": \"00:00-23:59\",\n" +
                "    \"allowedWeekdays\": \"1,2,3,4,5,6,7\"\n" +
                "  },\n" +
                "  \"frequencyRestriction\": {\n" +
                "    \"enabled\": false,\n" +
                "    \"maxConsumptionsPerDay\": 100,\n" +
                "    \"maxConsumptionsPerWindow\": 50,\n" +
                "    \"windowSizeMinutes\": 60,\n" +
                "    \"cooldownMinutes\": 0\n" +
                "  },\n" +
                "  \"subsidyConfig\": {\n" +
                "    \"enabled\": false,\n" +
                "    \"subsidyType\": \"FIXED_AMOUNT\",\n" +
                "    \"fixedSubsidyAmount\": 0.00,\n" +
                "    \"dailyMaxSubsidy\": 0.00\n" +
                "  }\n" +
                "}";
    }

    /**
     * 创建固定金额消费模式配置（示例）
     */
    private String createFixedAmountModeConfig() {
        return "{\n" +
                "  \"modeCode\": \"FIXED_AMOUNT\",\n" +
                "  \"modeName\": \"固定金额模式\",\n" +
                "  \"description\": \"固定金额消费模式，只允许预设的固定金额消费\",\n" +
                "  \"enabled\": false,\n" +
                "  \"priority\": 50,\n" +
                "  \"maxRetryCount\": 2,\n" +
                "  \"timeoutSeconds\": 25,\n" +
                "  \"supportsFallback\": true,\n" +
                "  \"modeSpecificConfig\": {\n" +
                "    \"allowBreakfast\": true,\n" +
                "    \"allowLunch\": true,\n" +
                "    \"allowDinner\": true,\n" +
                "    \"mealPeriodCheck\": true\n" +
                "  },\n" +
                "  \"amountRestriction\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"fixedAmounts\": \"5.00,10.00,15.00,20.00\"\n" +
                "  }\n" +
                "}";
    }

    /**
     * 验证引擎状态
     */
    private void validateEngineStatus() {
        log.info("验证消费模式引擎状态...");

        try {
            ConsumptionModeEngine.EngineHealthResult healthResult = modeEngine.checkHealth();

            if (healthResult.isHealthy()) {
                log.info("消费模式引擎状态健康: {}", healthResult);
            } else {
                log.error("消费模式引擎状态异常: {}", healthResult);
                throw new RuntimeException("消费模式引擎状态异常: " + healthResult.getErrorMessage());
            }

            // 验证必要的策略是否已注册
            validateRequiredStrategies();

            // 验证必要的配置是否存在
            validateRequiredConfigs();

            log.info("消费模式引擎状态验证通过");

        } catch (Exception e) {
            log.error("消费模式引擎状态验证失败", e);
            throw e;
        }
    }

    /**
     * 验证必要的策略是否已注册
     */
    private void validateRequiredStrategies() {
        List<String> requiredStrategies = List.of("STANDARD");

        for (String strategyCode : requiredStrategies) {
            if (!modeEngine.isModeSupported(strategyCode)) {
                throw new RuntimeException("缺少必要的消费模式策略: " + strategyCode);
            }
            log.debug("必要策略验证通过: {}", strategyCode);
        }
    }

    /**
     * 验证必要的配置是否存在
     */
    private void validateRequiredConfigs() {
        List<String> requiredConfigs = List.of("STANDARD");

        for (String modeCode : requiredConfigs) {
            ConsumptionModeConfig config = modeEngine.getModeConfig(modeCode);
            if (config == null) {
                throw new RuntimeException("缺少必要的消费模式配置: " + modeCode);
            }
            log.debug("必要配置验证通过: {} - {}", modeCode, config.generateSummary());
        }
    }

    /**
     * 获取初始化摘要信息
     *
     * @return 初始化摘要
     */
    public String getInitializationSummary() {
        try {
            ConsumptionModeEngine.EngineHealthResult healthResult = modeEngine.checkHealth();
            return String.format("消费模式引擎初始化摘要: 状态=%s, 策略数=%d, 配置数=%d",
                    healthResult.isHealthy() ? "健康" : "异常",
                    healthResult.getRegisteredStrategies(),
                    healthResult.getCachedConfigs());
        } catch (Exception e) {
            return "消费模式引擎初始化摘要获取失败: " + e.getMessage();
        }
    }

    /**
     * 创建补贴消费模式配置
     */
    private String createSubsidyModeConfig() {
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
                "    \"maxSubsidyRatio\": 0.8,\n" +
                "    \"employeeSubsidy\": true,\n" +
                "    \"subsidyAccounting\": true\n" +
                "  },\n" +
                "  \"amountRestriction\": {\n" +
                "    \"enabled\": false\n" +
                "  },\n" +
                "  \"timeRestriction\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"allowedTimeRange\": \"06:00-22:00\",\n" +
                "    \"allowedWeekdays\": \"1,2,3,4,5\"\n" +
                "  },\n" +
                "  \"frequencyRestriction\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"maxConsumptionsPerDay\": 6\n" +
                "  },\n" +
                "  \"subsidyConfig\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"subsidyType\": \"FIXED_AMOUNT\",\n" +
                "    \"fixedSubsidyAmount\": 3.00,\n" +
                "    \"dailyMaxSubsidy\": 30.00,\n" +
                "    \"subsidyStartTime\": \"2025-01-01 00:00:00\",\n" +
                "    \"subsidyEndTime\": \"2025-12-31 23:59:59\",\n" +
                "    \"employeeTypeSubsidy\": {\n" +
                "      \"FULL_TIME\": 5.00,\n" +
                "      \"PART_TIME\": 3.00,\n" +
                "      \"INTERN\": 2.00\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}