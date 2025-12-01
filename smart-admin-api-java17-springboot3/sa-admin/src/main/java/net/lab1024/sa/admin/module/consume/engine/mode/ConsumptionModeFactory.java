package net.lab1024.sa.admin.module.consume.engine.mode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费模式工厂
 * 严格遵循repowiki规范：使用工厂模式管理消费模式的创建和获取
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Component
@Slf4j

public class ConsumptionModeFactory {

    private final Map<String, ConsumptionMode> modeRegistry = new ConcurrentHashMap<>();

    /**
     * 注册消费模式
     *
     * @param mode 消费模式
     */
    public void registerMode(ConsumptionMode mode) {
        log.info("注册消费模式: modeCode={}, modeName={}", mode.getModeCode(), mode.getModeName());
        modeRegistry.put(mode.getModeCode(), mode);
    }

    /**
     * 获取消费模式
     *
     * @param modeId 模式ID（实际使用modeCode）
     * @return 消费模式
     */
    public ConsumptionMode getMode(String modeId) {
        ConsumptionMode mode = modeRegistry.get(modeId);
        if (mode == null) {
            throw new IllegalArgumentException("未找到消费模式: " + modeId);
        }
        return mode;
    }

    /**
     * 获取所有可用的消费模式
     *
     * @return 消费模式列表
     */
    public Map<String, ConsumptionMode> getAllModes() {
        return new ConcurrentHashMap<>(modeRegistry);
    }

    /**
     * 检查消费模式是否存在
     *
     * @param modeId 模式ID
     * @return 是否存在
     */
    public boolean hasMode(String modeId) {
        return modeRegistry.containsKey(modeId);
    }

    /**
     * 移除消费模式
     *
     * @param modeId 模式ID
     * @return 被移除的模式
     */
    public ConsumptionMode removeMode(String modeId) {
        log.info("移除消费模式: modeId={}", modeId);
        return modeRegistry.remove(modeId);
    }

    /**
     * 获取模式数量
     *
     * @return 模式数量
     */
    public int getModeCount() {
        return modeRegistry.size();
    }
}