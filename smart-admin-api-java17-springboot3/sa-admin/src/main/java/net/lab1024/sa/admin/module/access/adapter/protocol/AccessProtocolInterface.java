package net.lab1024.sa.admin.module.access.adapter.protocol;

import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.common.device.DeviceConnectionTest;
import net.lab1024.sa.base.common.device.DeviceDispatchResult;

import java.util.List;
import java.util.Map;

/**
 * 门禁协议接口
 * <p>
 * 定义门禁设备协议的标准操作接口，支持多种门禁厂商协议
 * 每个门禁厂商需要实现此接口以支持统一的设备操作
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
public interface AccessProtocolInterface {

    /**
     * 获取协议名称
     *
     * @return 协议名称
     */
    String getProtocolName();

    /**
     * 获取支持的制造商列表
     *
     * @return 支持的制造商列表
     */
    List<String> getSupportedManufacturers();

    /**
     * 测试设备连接
     *
     * @param device 设备实体
     * @return 连接测试结果
     * @throws Exception 连接异常
     */
    DeviceConnectionTest testConnection(SmartDeviceEntity device) throws Exception;

    /**
     * 下发人员数据
     *
     * @param device 设备实体
     * @param personData 人员数据
     * @return 下发结果
     * @throws Exception 下发异常
     */
    DeviceDispatchResult dispatchPersonData(SmartDeviceEntity device, Map<String, Object> personData) throws Exception;

    /**
     * 下发生物特征数据
     *
     * @param device 设备实体
     * @param biometricData 生物特征数据
     * @return 下发结果
     * @throws Exception 下发异常
     */
    DeviceDispatchResult dispatchBiometricData(SmartDeviceEntity device, Map<String, Object> biometricData) throws Exception;

    /**
     * 下发门禁配置数据
     *
     * @param device 设备实体
     * @param configData 配置数据
     * @return 下发结果
     * @throws Exception 下发异常
     */
    DeviceDispatchResult dispatchAccessConfig(SmartDeviceEntity device, Map<String, Object> configData) throws Exception;

    /**
     * 远程开门
     *
     * @param device 设备实体
     * @param doorId 门ID
     * @return 开门结果
     * @throws Exception 开门异常
     */
    DeviceDispatchResult remoteOpenDoor(SmartDeviceEntity device, String doorId) throws Exception;

    /**
     * 获取设备状态
     *
     * @param device 设备实体
     * @return 设备状态
     * @throws Exception 获取状态异常
     */
    Map<String, Object> getDeviceStatus(SmartDeviceEntity device) throws Exception;

    /**
     * 查询设备人员列表
     *
     * @param device 设备实体
     * @return 人员列表
     * @throws Exception 查询异常
     */
    List<Map<String, Object>> queryPersonsOnDevice(SmartDeviceEntity device) throws Exception;

    /**
     * 删除设备人员数据
     *
     * @param device 设备实体
     * @param personId 人员ID
     * @return 删除结果
     * @throws Exception 删除异常
     */
    DeviceDispatchResult deletePersonData(SmartDeviceEntity device, Long personId) throws Exception;

    /**
     * 批量下发人员数据
     *
     * @param device 设备实体
     * @param personList 人员列表
     * @return 批量下发结果
     * @throws Exception 批量下发异常
     */
    DeviceDispatchResult batchDispatchPersonData(SmartDeviceEntity device, List<Map<String, Object>> personList) throws Exception;

    /**
     * 获取门禁记录
     *
     * @param device 设备实体
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param recordCount 记录数量限制（可选）
     * @return 门禁记录列表
     * @throws Exception 获取记录异常
     */
    List<Map<String, Object>> getAccessRecords(SmartDeviceEntity device, String startTime, String endTime, Integer recordCount) throws Exception;

    /**
     * 检查设备是否支持
     *
     * @param device 设备实体
     * @return 是否支持
     */
    boolean supportsDevice(SmartDeviceEntity device);

    /**
     * 获取协议版本
     *
     * @return 协议版本
     */
    String getProtocolVersion();

    /**
     * 获取协议特性
     *
     * @return 特性列表
     */
    List<String> getProtocolFeatures();
}