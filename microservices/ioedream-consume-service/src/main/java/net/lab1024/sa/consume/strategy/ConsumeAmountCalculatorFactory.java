package net.lab1024.sa.consume.strategy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 消费金额计算器工厂类（策略模式）
 * <p>
 * 用于管理和获取不同消费模式的金额计算器
 * 严格遵循CLAUDE.md规范：
 * - 工厂类使用@Component注解
 * - 使用@Resource注入策略实现类
 * - 使用Map缓存策略实例
 * </p>
 * <p>
 * 支持的消费模式：
 * - FIXED - 定值模式
 * - AMOUNT - 金额模式
 * - PRODUCT - 商品模式
 * - COUNT - 计次模式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class ConsumeAmountCalculatorFactory {

    @Resource
    private List<ConsumeAmountCalculator> calculators;

    /**
     * 策略缓存Map（消费模式 -> 计算器实例）
     */
    private final Map<String, ConsumeAmountCalculator> calculatorMap = new ConcurrentHashMap<>();

    /**
     * 初始化策略缓存
     * <p>
     * 在Spring容器初始化完成后，将所有策略实现类注册到Map中
     * </p>
     */
    @PostConstruct
    public void init() {
        if (calculators == null || calculators.isEmpty()) {
            log.warn("[策略工厂] 未找到任何消费金额计算器实现类");
            return;
        }

        for (ConsumeAmountCalculator calculator : calculators) {
            String consumeMode = calculator.getConsumeMode();
            if (consumeMode != null && !consumeMode.isEmpty()) {
                calculatorMap.put(consumeMode.toUpperCase(), calculator);
                log.info("[策略工厂] 注册消费金额计算器，模式={}, 实现类={}", 
                        consumeMode, calculator.getClass().getSimpleName());
            } else {
                log.warn("[策略工厂] 计算器未指定消费模式，实现类={}", 
                        calculator.getClass().getSimpleName());
            }
        }

        log.info("[策略工厂] 策略初始化完成，已注册{}个计算器", calculatorMap.size());
    }

    /**
     * 根据消费模式获取计算器
     *
     * @param consumeMode 消费模式（FIXED/AMOUNT/PRODUCT/COUNT）
     * @return 计算器实例，如果不存在则返回null
     */
    public ConsumeAmountCalculator getCalculator(String consumeMode) {
        if (consumeMode == null || consumeMode.isEmpty()) {
            log.warn("[策略工厂] 消费模式为空，无法获取计算器");
            return null;
        }

        ConsumeAmountCalculator calculator = calculatorMap.get(consumeMode.toUpperCase());
        if (calculator == null) {
            log.warn("[策略工厂] 未找到消费模式对应的计算器，consumeMode={}", consumeMode);
        }

        return calculator;
    }

    /**
     * 获取所有已注册的计算器
     *
     * @return 计算器Map
     */
    public Map<String, ConsumeAmountCalculator> getAllCalculators() {
        return new ConcurrentHashMap<>(calculatorMap);
    }

    /**
     * 检查消费模式是否支持
     *
     * @param consumeMode 消费模式
     * @return 是否支持
     */
    public boolean isConsumeModeSupported(String consumeMode) {
        return consumeMode != null && calculatorMap.containsKey(consumeMode.toUpperCase());
    }
}

