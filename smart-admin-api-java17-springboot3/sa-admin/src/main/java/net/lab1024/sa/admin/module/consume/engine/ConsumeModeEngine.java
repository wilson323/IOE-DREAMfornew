package net.lab1024.sa.admin.module.consume.engine;

import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeResultDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeValidationResult;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeModeConfig;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum;

/**
 * 消费模式引擎接口
 * 定义不同消费模式的处理逻辑
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ConsumeModeEngine {

    /**
     * 获取消费模式类型
     *
     * @return 消费模式枚举
     */
    ConsumeModeEnum getConsumeMode();

    /**
     * 验证消费请求
     *
     * @param consumeRequest 消费请求
     * @return 验证结果
     */
    ConsumeValidationResult validateRequest(ConsumeRequestDTO consumeRequest);

    /**
     * 处理消费
     *
     * @param consumeRequest 消费请求
     * @return 消费结果
     */
    ConsumeResultDTO processConsume(ConsumeRequestDTO consumeRequest);

    /**
     * 计算消费金额
     *
     * @param consumeRequest 消费请求
     * @return 消费金额
     */
    java.math.BigDecimal calculateAmount(ConsumeRequestDTO consumeRequest);

    /**
     * 获取消费模式描述
     *
     * @return 模式描述
     */
    String getModeDescription();

    /**
     * 检查消费模式是否可用
     *
     * @param deviceId 设备ID
     * @return 是否可用
     */
    boolean isModeAvailable(Long deviceId);

    /**
     * 获取消费模式配置
     *
     * @param deviceId 设备ID
     * @return 配置信息
     */
    ConsumeModeConfig getModeConfig(Long deviceId);
}
