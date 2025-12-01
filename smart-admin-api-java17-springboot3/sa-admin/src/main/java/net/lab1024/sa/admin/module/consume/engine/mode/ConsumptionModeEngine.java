package net.lab1024.sa.admin.module.consume.engine.mode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.engine.mode.config.ConsumptionModeConfig;
import net.lab1024.sa.admin.module.consume.engine.mode.config.ModeConfigurationParser;
import net.lab1024.sa.admin.module.consume.engine.mode.strategy.ConsumptionModeStrategy;
import net.lab1024.sa.base.common.exception.SmartException;

/**
 * 消费模式引擎
 * 负责管理不同消费模式的注册、配置解析、策略分发和降级处理
 * 支持固定金额、按次消费、补贴消费等多种消费模式
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class ConsumptionModeEngine {

    @Resource
    private ModeConfigurationParser configurationParser;

    // 消费模式策略注册表
    private final Map<String, ConsumptionModeStrategy> strategyRegistry = new ConcurrentHashMap<>();

    // 消费模式配置缓存
    private final Map<String, ConsumptionModeConfig> configCache = new ConcurrentHashMap<>();

    // 默认降级模式
    private static final String DEFAULT_FALLBACK_MODE = "STANDARD";

    /**
     * 注册消费模式策略
     *
     * @param modeCode 模式编码
     * @param strategy 策略实现
     */
    public void registerStrategy(String modeCode, ConsumptionModeStrategy strategy) {
        Objects.requireNonNull(modeCode, "消费模式编码不能为空");
        Objects.requireNonNull(strategy, "消费模式策略不能为空");

        strategyRegistry.put(modeCode.toUpperCase(), strategy);
        log.info("消费模式策略注册成功: {} -> {}", modeCode, strategy.getClass().getSimpleName());
    }

    /**
     * 获取消费模式策略
     *
     * @param modeCode 模式编码
     * @return 策略实现，如果不存在则返回null
     */
    public ConsumptionModeStrategy getStrategy(String modeCode) {
        if (modeCode == null) {
            return null;
        }
        return strategyRegistry.get(modeCode.toUpperCase());
    }

    /**
     * 检查消费模式是否已注册
     *
     * @param modeCode 模式编码
     * @return 是否已注册
     */
    public boolean isModeSupported(String modeCode) {
        return modeCode != null && strategyRegistry.containsKey(modeCode.toUpperCase());
    }

    /**
     * 解析并缓存消费模式配置
     *
     * @param modeConfigJson 配置JSON字符串
     * @return 消费模式配置对象
     */
    public ConsumptionModeConfig parseAndCacheConfig(String modeConfigJson) {
        try {
            ConsumptionModeConfig config = configurationParser.parse(modeConfigJson);
            configCache.put(config.getModeCode(), config);
            log.info("消费模式配置解析并缓存成功: {}", config.getModeCode());
            return config;
        } catch (Exception e) {
            log.error("消费模式配置解析失败: {}", modeConfigJson, e);
            throw new SmartException("消费模式配置解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取消费模式配置
     *
     * @param modeCode 模式编码
     * @return 配置对象，如果不存在则返回null
     */
    public ConsumptionModeConfig getModeConfig(String modeCode) {
        return configCache.get(modeCode);
    }

    /**
     * 执行消费模式处理
     *
     * @param request 消费请求
     * @return 消费处理结果
     */
    public ConsumeResult executeModeProcessing(ConsumeRequest request) {
        String modeCode = request.getConsumptionMode();

        try {
            log.info("开始执行消费模式处理: modeCode={}, orderNo={}", modeCode, request.getOrderNo());

            // 1. 获取消费模式策略
            ConsumptionModeStrategy strategy = getStrategy(modeCode);
            if (strategy == null) {
                log.warn("消费模式策略不存在，启用降级处理: modeCode={}", modeCode);
                return executeFallbackProcessing(request);
            }

            // 2. 获取消费模式配置
            ConsumptionModeConfig config = getModeConfig(modeCode);
            if (config == null) {
                log.warn("消费模式配置不存在，使用默认配置: modeCode={}", modeCode);
                config = createDefaultConfig(modeCode);
            }

            // 3. 验证消费模式状态
            if (!config.isEnabled()) {
                log.warn("消费模式已禁用，启用降级处理: modeCode={}", modeCode);
                return executeFallbackProcessing(request);
            }

            // 4. 预处理验证
            ConsumeResult preResult = strategy.preProcess(request, config);
            if (preResult != null && !preResult.isSuccess()) {
                String errorMsg = preResult.getMessage() != null ? preResult.getMessage()
                        : (preResult.getErrorDetail() != null ? preResult.getErrorDetail() : "预处理失败");
                log.warn("消费模式预处理失败: modeCode={}, error={}", modeCode, errorMsg);
                return preResult;
            }

            // 5. 执行消费模式策略
            ConsumeResult result = strategy.process(request, config);

            // 6. 后处理
            try {
                strategy.postProcess(request, config, result);
            } catch (Exception e) {
                log.warn("消费模式后处理异常，不影响主流程: modeCode={}, orderNo={}", modeCode, request.getOrderNo(), e);
            }

            log.info("消费模式处理完成: modeCode={}, orderNo={}, success={}",
                    modeCode, request.getOrderNo(), result.isSuccess());

            return result;

        } catch (Exception e) {
            log.error("消费模式处理异常: modeCode={}, orderNo={}", modeCode, request.getOrderNo(), e);
            return executeFallbackProcessing(request);
        }
    }

    /**
     * 执行降级处理
     * 当主策略失败时，使用标准消费模式作为降级方案
     *
     * @param request 消费请求
     * @return 降级处理结果
     */
    private ConsumeResult executeFallbackProcessing(ConsumeRequest request) {
        try {
            log.info("执行消费模式降级处理: originalMode={}, orderNo={}",
                    request.getConsumptionMode(), request.getOrderNo());

            // 使用标准消费模式进行降级
            ConsumptionModeStrategy fallbackStrategy = getStrategy(DEFAULT_FALLBACK_MODE);
            if (fallbackStrategy != null) {
                ConsumptionModeConfig fallbackConfig = getModeConfig(DEFAULT_FALLBACK_MODE);
                if (fallbackConfig == null) {
                    fallbackConfig = createDefaultConfig(DEFAULT_FALLBACK_MODE);
                }

                ConsumeResult result = fallbackStrategy.process(request, fallbackConfig);
                log.info("消费模式降级处理完成: orderNo={}, success={}",
                        request.getOrderNo(), result.isSuccess());

                return result;
            }

            // 如果降级策略也不存在，返回基本成功结果
            log.warn("降级策略不存在，返回基本处理结果: orderNo={}", request.getOrderNo());
            return ConsumeResult.success(request.getAmount());

        } catch (Exception e) {
            log.error("消费模式降级处理异常: orderNo={}", request.getOrderNo(), e);
            return ConsumeResult.failure("MODE_FALLBACK_ERROR", "消费模式降级处理异常: " + e.getMessage());
        }
    }

    /**
     * 创建默认配置
     *
     * @param modeCode 模式编码
     * @return 默认配置
     */
    private ConsumptionModeConfig createDefaultConfig(String modeCode) {
        ConsumptionModeConfig config = new ConsumptionModeConfig();
        config.setModeCode(modeCode);
        config.setModeName(getDefaultModeName(modeCode));
        config.setEnabled(true);
        config.setPriority(1);
        config.setMaxRetryCount(3);
        config.setTimeoutSeconds(30);
        config.setDescription("自动生成的默认配置");

        log.info("创建默认消费模式配置: {}", modeCode);
        return config;
    }

    /**
     * 获取默认模式名称
     *
     * @param modeCode 模式编码
     * @return 默认模式名称
     */
    private String getDefaultModeName(String modeCode) {
        Map<String, String> defaultNames = Map.of(
                "STANDARD", "标准消费模式",
                "FIXED_AMOUNT", "固定金额模式",
                "SUBSIDY", "补贴消费模式",
                "PER_COUNT", "按次消费模式");
        return defaultNames.getOrDefault(modeCode, "未知模式");
    }

    /**
     * 获取所有已注册的消费模式
     *
     * @return 模式编码列表
     */
    public Set<String> getAllSupportedModes() {
        return new HashSet<>(strategyRegistry.keySet());
    }

    /**
     * 获取所有已缓存的配置
     *
     * @return 配置映射
     */
    public Map<String, ConsumptionModeConfig> getAllConfigs() {
        return new HashMap<>(configCache);
    }

    /**
     * 清理配置缓存
     *
     * @param modeCode 模式编码，如果为null则清理所有
     */
    public void clearConfigCache(String modeCode) {
        if (modeCode == null) {
            configCache.clear();
            log.info("已清理所有消费模式配置缓存");
        } else {
            configCache.remove(modeCode);
            log.info("已清理消费模式配置缓存: {}", modeCode);
        }
    }

    /**
     * 重新加载配置
     *
     * @param modeCode   模式编码
     * @param configJson 新的配置JSON
     */
    public void reloadConfig(String modeCode, String configJson) {
        // 清理旧配置
        configCache.remove(modeCode);

        // 解析新配置
        parseAndCacheConfig(configJson);

        log.info("消费模式配置重新加载完成: {}", modeCode);
    }

    /**
     * 检查引擎健康状态
     *
     * @return 健康检查结果
     */
    public EngineHealthResult checkHealth() {
        try {
            int registeredStrategies = strategyRegistry.size();
            int cachedConfigs = configCache.size();
            boolean fallbackAvailable = strategyRegistry.containsKey(DEFAULT_FALLBACK_MODE);

            EngineHealthResult result = new EngineHealthResult();
            result.setHealthy(registeredStrategies > 0 && fallbackAvailable);
            result.setRegisteredStrategies(registeredStrategies);
            result.setCachedConfigs(cachedConfigs);
            result.setFallbackAvailable(fallbackAvailable);
            result.setCheckTime(System.currentTimeMillis());

            return result;

        } catch (Exception e) {
            log.error("消费模式引擎健康检查异常", e);
            EngineHealthResult result = new EngineHealthResult();
            result.setHealthy(false);
            result.setErrorMessage(e.getMessage());
            result.setCheckTime(System.currentTimeMillis());
            return result;
        }
    }

    /**
     * 引擎健康检查结果
     */
    public static class EngineHealthResult {
        private boolean healthy;
        private int registeredStrategies;
        private int cachedConfigs;
        private boolean fallbackAvailable;
        private String errorMessage;
        private long checkTime;

        // Getters and Setters
        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public int getRegisteredStrategies() {
            return registeredStrategies;
        }

        public void setRegisteredStrategies(int registeredStrategies) {
            this.registeredStrategies = registeredStrategies;
        }

        public int getCachedConfigs() {
            return cachedConfigs;
        }

        public void setCachedConfigs(int cachedConfigs) {
            this.cachedConfigs = cachedConfigs;
        }

        public boolean isFallbackAvailable() {
            return fallbackAvailable;
        }

        public void setFallbackAvailable(boolean fallbackAvailable) {
            this.fallbackAvailable = fallbackAvailable;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public long getCheckTime() {
            return checkTime;
        }

        public void setCheckTime(long checkTime) {
            this.checkTime = checkTime;
        }

        @Override
        public String toString() {
            return "EngineHealthResult{" +
                    "healthy" + healthy +
                    ", registeredStrategies" + registeredStrategies +
                    ", cachedConfigs" + cachedConfigs +
                    ", fallbackAvailable" + fallbackAvailable +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", checkTime" + checkTime +
                    '}';
        }
    }
}