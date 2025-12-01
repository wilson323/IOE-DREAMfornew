package net.lab1024.sa.admin.module.consume.engine.mode.strategy;

import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.engine.mode.config.ConsumptionModeConfig;

/**
 * 消费模式策略接口
 * 定义不同消费模式的处理策略标准
 * 所有消费模式实现都必须实现此接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ConsumptionModeStrategy {

    /**
     * 获取策略编码
     *
     * @return 策略唯一编码
     */
    String getStrategyCode();

    /**
     * 获取策略名称
     *
     * @return 策略显示名称
     */
    String getStrategyName();

    /**
     * 获取策略描述
     *
     * @return 策略功能描述
     */
    String getStrategyDescription();

    /**
     * 预处理
     * 在执行主要消费逻辑前进行验证和准备工作
     *
     * @param request 消费请求
     * @param config 消费模式配置
     * @return 预处理结果，如果返回null表示继续执行，返回失败结果表示终止处理
     */
    default ConsumeResult preProcess(ConsumeRequest request, ConsumptionModeConfig config) {
        // 默认实现：不做预处理
        return null;
    }

    /**
     * 执行消费模式处理
     *
     * @param request 消费请求
     * @param config 消费模式配置
     * @return 消费处理结果
     */
    ConsumeResult process(ConsumeRequest request, ConsumptionModeConfig config);

    /**
     * 后处理
     * 在完成主要消费逻辑后进行后续处理
     *
     * @param request 消费请求
     * @param config 消费模式配置
     * @param result 主要处理结果
     */
    default void postProcess(ConsumeRequest request, ConsumptionModeConfig config, ConsumeResult result) {
        // 默认实现：不做后处理
    }

    /**
     * 验证配置有效性
     *
     * @param config 消费模式配置
     * @return 验证结果
     */
    default boolean validateConfig(ConsumptionModeConfig config) {
        // 默认实现：检查基本字段
        return config != null &&
               config.getModeCode() != null &&
               config.isEnabled();
    }

    /**
     * 获取策略优先级
     * 数值越小优先级越高
     *
     * @return 优先级数值
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 是否支持动态配置
     *
     * @return 是否支持运行时配置更新
     */
    default boolean supportsDynamicConfig() {
        return false;
    }

    /**
     * 获取配置模板
     * 用于生成配置示例
     *
     * @return 配置模板JSON字符串
     */
    default String getConfigTemplate() {
        return "{}";
    }
}