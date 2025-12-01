package net.lab1024.sa.base.common.device;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;

/**
 * 设备适配器接口
 * <p>
 * 统一的设备适配器接口规范，定义所有设备适配器必须实现的标准操作
 * 业务模块通过实现此接口来提供设备协议适配能力
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
public interface DeviceAdapterInterface {

    /**
     * 获取支持的设备类型
     *
     * @return 设备类型（ACCESS、ATTENDANCE、CONSUME、VIDEO）
     */
    String getSupportedDeviceType();

    /**
     * 获取支持的设备制造商列表
     *
     * @return 制造商名称列表
     */
    List<String> getSupportedManufacturers();

    /**
     * 检查是否支持指定设备
     *
     * @param device 设备实体
     * @return 是否支持该设备
     */
    boolean supportsDevice(SmartDeviceEntity device);

    /**
     * 测试设备连接
     *
     * @param device 设备实体
     * @return 连接测试结果
     * @throws DeviceProtocolException 协议异常
     */
    DeviceConnectionTest testConnection(SmartDeviceEntity device) throws DeviceProtocolException;

    /**
     * 下发人员基础信息
     *
     * @param device 设备实体
     * @param personData 人员数据（包含基础信息、权限等）
     * @return 下发结果
     * @throws DeviceProtocolException 协议异常
     */
    DeviceDispatchResult dispatchPersonData(SmartDeviceEntity device, Map<String, Object> personData)
            throws DeviceProtocolException;

    /**
     * 下发生物特征数据
     *
     * @param device 设备实体
     * @param biometricData 生物特征数据（包含模板、质量等信息）
     * @return 下发结果
     * @throws DeviceProtocolException 协议异常
     */
    DeviceDispatchResult dispatchBiometricData(SmartDeviceEntity device, Map<String, Object> biometricData)
            throws DeviceProtocolException;

    /**
     * 下发配置信息
     *
     * @param device 设备实体
     * @param configData 配置数据（包含设备参数、策略等）
     * @return 下发结果
     * @throws DeviceProtocolException 协议异常
     */
    DeviceDispatchResult dispatchConfigData(SmartDeviceEntity device, Map<String, Object> configData)
            throws DeviceProtocolException;

    /**
     * 查询设备状态
     *
     * @param device 设备实体
     * @return 设备状态信息
     * @throws DeviceProtocolException 协议异常
     */
    Map<String, Object> getDeviceStatus(SmartDeviceEntity device) throws DeviceProtocolException;

    /**
     * 查询设备上的人员列表
     *
     * @param device 设备实体
     * @return 人员列表
     * @throws DeviceProtocolException 协议异常
     */
    List<Map<String, Object>> queryPersonsOnDevice(SmartDeviceEntity device) throws DeviceProtocolException;

    /**
     * 删除设备上的人员数据
     *
     * @param device 设备实体
     * @param personId 人员ID
     * @return 删除结果
     * @throws DeviceProtocolException 协议异常
     */
    DeviceDispatchResult deletePersonData(SmartDeviceEntity device, Long personId) throws DeviceProtocolException;

    /**
     * 批量下发人员数据
     *
     * @param device 设备实体
     * @param personList 人员数据列表
     * @return 批量下发结果
     * @throws DeviceProtocolException 协议异常
     */
    DeviceDispatchResult batchDispatchPersonData(SmartDeviceEntity device, List<Map<String, Object>> personList)
            throws DeviceProtocolException;

    /**
     * 获取设备配置模板
     *
     * @return 设备配置模板（包含必填字段、默认值等）
     */
    Map<String, Object> getDeviceConfigTemplate();

    /**
     * 验证设备配置
     *
     * @param device 设备实体
     * @return 配置是否有效
     */
    boolean validateDeviceConfig(SmartDeviceEntity device);

    /**
     * 获取适配器描述信息
     *
     * @return 适配器描述（版本、支持的协议特性等）
     */
    AdapterInfo getAdapterInfo();
}