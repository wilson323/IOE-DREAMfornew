package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;

/**
 * 消费模式引擎管理器接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ConsumptionModeEngineManager {

    /**
     * 处理消费模式
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param amount 消费金额
     * @param mode 消费模式
     * @return 处理结果
     */
    boolean processConsumptionMode(Long userId, Long deviceId, BigDecimal amount, String mode);

    /**
     * 获取支持的消费模式
     *
     * @param deviceId 设备ID
     * @return 模式列表
     */
    java.util.List<String> getSupportedModes(Long deviceId);

    /**
     * 验证消费模式是否有效
     *
     * @param mode 消费模式
     * @param deviceId 设备ID
     * @return 是否有效
     */
    boolean isValidMode(String mode, Long deviceId);
}