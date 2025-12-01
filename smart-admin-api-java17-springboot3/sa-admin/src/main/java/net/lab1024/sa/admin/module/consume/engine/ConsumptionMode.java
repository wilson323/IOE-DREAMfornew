package net.lab1024.sa.admin.module.consume.engine;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 消费模式基类
 * 定义消费模式的通用接口和行为
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ConsumptionMode {

    /**
     * 获取模式代码
     *
     * @return 模式代码
     */
    String getModeCode();

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    String getModeName();

    /**
     * 获取模式描述
     *
     * @return 模式描述
     */
    String getDescription();

    /**
     * 处理消费请求
     *
     * @param request 消费模式请求
     * @return 消费模式结果
     */
    ConsumeModeResult process(ConsumeModeRequest request);

    /**
     * 验证消费参数
     *
     * @param request 消费模式请求
     * @return 验证结果
     */
    ConsumeModeValidationResult validate(ConsumeModeRequest request);

    /**
     * 获取模式配置模板
     *
     * @return 配置模板JSON
     */
    String getConfigTemplate();

    /**
     * 检查模式是否适用于指定设备
     *
     * @param deviceId 设备ID
     * @return 是否适用
     */
    boolean isApplicableToDevice(Long deviceId);

    /**
     * 获取模式优先级
     * 数值越大优先级越高
     *
     * @return 优先级
     */
    int getPriority();
}