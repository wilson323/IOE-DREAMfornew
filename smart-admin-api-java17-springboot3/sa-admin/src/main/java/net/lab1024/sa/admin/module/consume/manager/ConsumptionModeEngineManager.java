package net.lab1024.sa.admin.module.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeEngine;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeResultDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeValidationResult;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费模式引擎管理器
 * 策略模式实现，负责管理所有消费模式引擎
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class ConsumptionModeEngineManager {

    @Resource
    private List<ConsumeModeEngine> modeEngines;

    private Map<String, ConsumeModeEngine> strategyMap = new HashMap<>();
    private ConsumeModeEngine defaultStrategy;

    @PostConstruct
    public void init() {
        // 初始化策略映射
        if (!CollectionUtils.isEmpty(modeEngines)) {
            for (ConsumeModeEngine engine : modeEngines) {
                strategyMap.put(engine.getConsumeMode().name(), engine);
                log.info("注册消费模式引擎: {} -> {}", engine.getConsumeMode().name(), engine.getClass().getSimpleName());
            }
        }

        // 设置默认策略（固定金额模式）
        defaultStrategy = strategyMap.get(ConsumeModeEnum.FIXED_AMOUNT.name());
        if (defaultStrategy == null && !strategyMap.isEmpty()) {
            defaultStrategy = strategyMap.values().iterator().next();
        }

        log.info("消费模式引擎管理器初始化完成，共注册 {} 个策略，默认策略: {}",
                strategyMap.size(),
                defaultStrategy != null ? defaultStrategy.getConsumeMode().name() : "无");
    }

    /**
     * 处理消费请求
     *
     * @param consumeRequest 消费请求
     * @param modeCode 消费模式代码
     * @return 消费结果
     */
    public ConsumeResultDTO processConsume(ConsumeRequestDTO consumeRequest, String modeCode) {
        // 1. 获取策略
        ConsumeModeEngine strategy = getStrategy(modeCode);
        if (strategy == null) {
            log.warn("未找到消费模式策略: {}, 使用默认策略", modeCode);
            strategy = defaultStrategy;
            if (strategy == null) {
                return createErrorResult("系统错误：无可用的消费模式引擎");
            }
        }

        try {
            // 2. 预处理 - 验证请求
            ConsumeValidationResult validationResult = strategy.validateRequest(consumeRequest);
            if (!validationResult.isValid()) {
                return createErrorResult(validationResult.getErrorMessage());
            }

            // 3. 执行处理
            ConsumeResultDTO result = strategy.processConsume(consumeRequest);

            // 4. 后处理 - 记录统计信息
            recordProcessingStatistics(strategy.getConsumeMode(), result);

            return result;

        } catch (Exception e) {
            log.error("消费处理异常: modeCode={}, error={}", modeCode, e.getMessage(), e);
            return createErrorResult("消费处理异常: " + e.getMessage());
        }
    }

    /**
     * 验证消费请求
     *
     * @param consumeRequest 消费请求
     * @param modeCode 消费模式代码
     * @return 验证结果
     */
    public ConsumeValidationResult validateRequest(ConsumeRequestDTO consumeRequest, String modeCode) {
        ConsumeModeEngine strategy = getStrategy(modeCode);
        if (strategy == null) {
            ConsumeValidationResult result = new ConsumeValidationResult();
            result.setValid(false);
            result.setErrorMessage("不支持的消费模式: " + modeCode);
            return result;
        }

        return strategy.validateRequest(consumeRequest);
    }

    /**
     * 计算消费金额
     *
     * @param consumeRequest 消费请求
     * @param modeCode 消费模式代码
     * @return 消费金额
     */
    public java.math.BigDecimal calculateAmount(ConsumeRequestDTO consumeRequest, String modeCode) {
        ConsumeModeEngine strategy = getStrategy(modeCode);
        if (strategy == null) {
            log.warn("未找到消费模式策略: {}, 返回金额0", modeCode);
            return java.math.BigDecimal.ZERO;
        }

        return strategy.calculateAmount(consumeRequest);
    }

    /**
     * 检查消费模式是否可用
     *
     * @param modeCode 消费模式代码
     * @param deviceId 设备ID
     * @return 是否可用
     */
    public boolean isModeAvailable(String modeCode, Long deviceId) {
        ConsumeModeEngine strategy = getStrategy(modeCode);
        if (strategy == null) {
            return false;
        }

        return strategy.isModeAvailable(deviceId);
    }

    /**
     * 获取消费模式配置
     *
     * @param modeCode 消费模式代码
     * @param deviceId 设备ID
     * @return 模式配置
     */
    public net.lab1024.sa.admin.module.consume.domain.dto.ConsumeModeConfig getModeConfig(String modeCode, Long deviceId) {
        ConsumeModeEngine strategy = getStrategy(modeCode);
        if (strategy == null) {
            return null;
        }

        return strategy.getModeConfig(deviceId);
    }

    /**
     * 获取所有可用的消费模式
     *
     * @return 消费模式列表
     */
    public List<ConsumeModeEnum> getAvailableModes() {
        return strategyMap.keySet().stream()
                .map(ConsumeModeEnum::valueOf)
                .sorted()
                .toList();
    }

    /**
     * 获取消费模式描述
     *
     * @param modeCode 消费模式代码
     * @return 模式描述
     */
    public String getModeDescription(String modeCode) {
        ConsumeModeEngine strategy = getStrategy(modeCode);
        if (strategy == null) {
            return "未知的消费模式";
        }

        return strategy.getModeDescription();
    }

    /**
     * 获取策略
     *
     * @param modeCode 模式代码
     * @return 策略实例
     */
    private ConsumeModeEngine getStrategy(String modeCode) {
        if (modeCode == null || modeCode.trim().isEmpty()) {
            return null;
        }
        return strategyMap.get(modeCode.toUpperCase());
    }

    /**
     * 记录处理统计信息
     *
     * @param mode 消费模式
     * @param result 处理结果
     */
    private void recordProcessingStatistics(ConsumeModeEnum mode, ConsumeResultDTO result) {
        try {
            // TODO: 实现统计信息记录
            // 可以记录到数据库、缓存或监控系统
            log.debug("记录消费统计: mode={}, success={}, amount={}",
                    mode, result.isSuccess(), result.getAmount());
        } catch (Exception e) {
            log.warn("记录消费统计失败", e);
        }
    }

    /**
     * 创建错误结果
     *
     * @param errorMessage 错误信息
     * @return 错误结果
     */
    private ConsumeResultDTO createErrorResult(String errorMessage) {
        ConsumeResultDTO result = new ConsumeResultDTO();
        result.setSuccess(false);
        result.setMessage(errorMessage);
        result.setStatus(net.lab1024.sa.admin.module.consume.domain.enums.ConsumeStatusEnum.FAILED);
        return result;
    }

    /**
     * 获取模式引擎统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getEngineStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEngines", strategyMap.size());
        stats.put("availableModes", getAvailableModes());
        stats.put("defaultMode", defaultStrategy != null ? defaultStrategy.getConsumeMode().name() : null);

        Map<String, String> modeDescriptions = new HashMap<>();
        for (Map.Entry<String, ConsumeModeEngine> entry : strategyMap.entrySet()) {
            modeDescriptions.put(entry.getKey(), entry.getValue().getModeDescription());
        }
        stats.put("modeDescriptions", modeDescriptions);

        return stats;
    }

    /**
     * 检查引擎健康状态
     *
     * @return 健康状态
     */
    public Map<String, Object> checkEngineHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("healthy", !strategyMap.isEmpty());
        health.put("engineCount", strategyMap.size());
        health.put("hasDefault", defaultStrategy != null);

        // 检查每个引擎的健康状态
        Map<String, Boolean> engineHealth = new HashMap<>();
        for (Map.Entry<String, ConsumeModeEngine> entry : strategyMap.entrySet()) {
            try {
                // 简单的健康检查 - 尝试调用基本方法
                entry.getValue().getModeDescription();
                engineHealth.put(entry.getKey(), true);
            } catch (Exception e) {
                engineHealth.put(entry.getKey(), false);
                log.warn("消费模式引擎健康检查失败: {} - {}", entry.getKey(), e.getMessage());
            }
        }
        health.put("engines", engineHealth);

        return health;
    }
}