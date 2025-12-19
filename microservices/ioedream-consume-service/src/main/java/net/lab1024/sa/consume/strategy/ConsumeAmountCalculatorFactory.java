package net.lab1024.sa.consume.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 消费金额计算器工厂
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Component
public class ConsumeAmountCalculatorFactory {

    private static final Logger log = LoggerFactory.getLogger(ConsumeAmountCalculatorFactory.class);

    private final Map<String, ConsumeAmountCalculator> calculatorMap;

    public ConsumeAmountCalculatorFactory(List<ConsumeAmountCalculator> calculators) {
        this.calculatorMap = calculators.stream()
                .collect(Collectors.toMap(
                        ConsumeAmountCalculator::getConsumeMode,
                        Function.identity()));
        log.debug("[计算器工厂] 初始化完成，共{}个计算器策略", calculators.size());
    }

    /**
     * 根据消费模式获取计算器
     *
     * @param consumeMode 消费模式
     * @return 计算器实例
     */
    public ConsumeAmountCalculator getCalculator(String consumeMode) {
        if (consumeMode == null) {
            log.warn("[计算器工厂] 消费模式为空，返回null");
            return null;
        }

        ConsumeAmountCalculator calculator = calculatorMap.get(consumeMode);
        if (calculator == null) {
            log.warn("[计算器工厂] 未找到支持的模式：{}", consumeMode);
        }

        return calculator;
    }
}
