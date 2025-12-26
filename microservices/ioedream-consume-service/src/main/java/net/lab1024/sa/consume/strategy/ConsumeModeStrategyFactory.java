package net.lab1024.sa.consume.strategy;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.strategy.impl.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消费模式策略工厂
 *
 * 职责：管理所有消费模式策略，根据消费模式类型获取对应策略
 *
 * 支持的6种消费模式：
 * - FIXED_AMOUNT: 固定金额模式
 * - FREE_AMOUNT: 自由金额模式
 * - METERED: 计量计费模式
 * - PRODUCT: 商品模式
 * - ORDER: 订餐模式
 * - INTELLIGENCE: 智能模式
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
@Component
public class ConsumeModeStrategyFactory {

    /**
     * 策略缓存
     */
    private final Map<String, ConsumeModeStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 构造函数：初始化所有策略
     */
    public ConsumeModeStrategyFactory() {
        registerStrategy(new FixedAmountStrategy());
        registerStrategy(new FreeAmountStrategy());
        registerStrategy(new MeteredStrategy());
        registerStrategy(new ProductStrategy());
        registerStrategy(new OrderStrategy());
        registerStrategy(new IntelligenceStrategy());

        log.info("[消费模式策略工厂] 初始化完成: 注册策略数={}", strategyMap.size());
    }

    /**
     * 注册策略
     *
     * @param strategy 策略实例
     */
    private void registerStrategy(ConsumeModeStrategy strategy) {
        String modeType = strategy.getModeType();
        strategyMap.put(modeType, strategy);
        log.info("[消费模式策略工厂] 注册策略: modeType={}, strategy={}",
                modeType, strategy.getClass().getSimpleName());
    }

    /**
     * 根据消费模式类型获取策略
     *
     * @param modeType 消费模式类型
     * @return 对应的策略实例
     * @throws IllegalArgumentException 如果模式类型不支持
     */
    public ConsumeModeStrategy getStrategy(String modeType) {
        ConsumeModeStrategy strategy = strategyMap.get(modeType);
        if (strategy == null) {
            log.error("[消费模式策略工厂] 不支持的消费模式: modeType={}", modeType);
            throw new IllegalArgumentException("不支持的消费模式: " + modeType);
        }
        return strategy;
    }

    /**
     * 检查是否支持指定的消费模式
     *
     * @param modeType 消费模式类型
     * @return 是否支持
     */
    public boolean isSupported(String modeType) {
        return strategyMap.containsKey(modeType);
    }

    /**
     * 获取所有支持的消费模式类型
     *
     * @return 消费模式类型列表
     */
    public Map<String, ConsumeModeStrategy> getAllStrategies() {
        return new ConcurrentHashMap<>(strategyMap);
    }
}
