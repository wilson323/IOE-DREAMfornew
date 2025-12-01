package net.lab1024.sa.admin.module.consume.engine;

import net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionMode;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 消费模式引擎接口
 * 提供统一的消费模式处理框架，支持模式注册和动态扩展
 *
 * @author SmartAdmin Team
 * @date 2025/11/16
 */
public interface ConsumptionModeEngine {

    /**
     * 处理消费请求
     *
     * @param request 消费请求
     * @return 消费结果
     */
    ConsumeResult process(ConsumeRequest request);

    /**
     * 验证消费请求
     *
     * @param request 消费请求
     * @return 验证结果
     */
    boolean validate(ConsumeRequest request);

    /**
     * 注册消费模式
     *
     * @param mode 消费模式实现
     */
    void registerMode(ConsumptionMode mode);

    /**
     * 注销消费模式
     *
     * @param modeCode 模式代码
     */
    void unregisterMode(String modeCode);

    /**
     * 获取支持的所有消费模式
     *
     * @return 消费模式列表
     */
    List<ConsumptionMode> getSupportedModes();

    /**
     * 根据模式代码获取消费模式
     *
     * @param modeCode 模式代码
     * @return 消费模式实现
     */
    ConsumptionMode getMode(String modeCode);

    /**
     * 检查是否支持指定的消费模式
     *
     * @param modeCode 模式代码
     * @return 是否支持
     */
    boolean isModeSupported(String modeCode);

    /**
     * 获取设备支持的消费模式
     *
     * @param deviceId 设备ID
     * @return 支持的模式列表
     */
    Set<String> getDeviceSupportedModes(Long deviceId);

    /**
     * 获取默认消费模式
     *
     * @param deviceId 设备ID
     * @return 默认模式代码
     */
    String getDefaultMode(Long deviceId);

    /**
     * 设置设备支持的消费模式
     *
     * @param deviceId 设备ID
     * @param supportedModes 支持的模式列表
     */
    void setDeviceSupportedModes(Long deviceId, Set<String> supportedModes);

    /**
     * 设置设备默认消费模式
     *
     * @param deviceId 设备ID
     * @param defaultMode 默认模式
     */
    void setDeviceDefaultMode(Long deviceId, String defaultMode);

    /**
     * 验证模式配置
     *
     * @param modeCode 模式代码
     * @param config 配置数据
     * @return 验证结果
     */
    boolean validateModeConfig(String modeCode, Map<String, Object> config);

    /**
     * 获取模式配置模板
     *
     * @param modeCode 模式代码
     * @return 配置模板
     */
    Map<String, Object> getModeConfigTemplate(String modeCode);

    /**
     * 获取引擎状态信息
     *
     * @return 引擎状态
     */
    EngineStatus getEngineStatus();

    /**
     * 引擎状态枚举
     */
    enum EngineStatus {
        INITIALIZING,    // 初始化中
        RUNNING,        // 运行中
        STOPPED,        // 已停止
        ERROR           // 错误状态
    }
}