package net.lab1024.sa.access.adapter.protocol;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.common.device.DeviceConnectionTest;
import net.lab1024.sa.common.device.DeviceDispatchResult;

/**
 * 门禁协议接口
 * <p>
 * 定义门禁设备协议的标准操作接口，支持多种门禁厂商协议
 * 每个门禁厂商需要实现此接口以支持统一的设备操作
 * 严格遵循repowiki规范：
 * - 接口设计原则：单一职责、开放封闭
 * - 参数验证：所有输入参数需要验证
 * - 异常处理：明确的异常定义和处理
 * - 文档规范：完整的JavaDoc注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
public interface AccessProtocolInterface {

    /**
     * 获取协议名称
     *
     * @return 协议名称，不能为null
     */
    String getProtocolName();

    /**
     * 获取支持的制造商列表
     *
     * @return 支持的制造商列表，不能为null
     */
    List<String> getSupportedManufacturers();

    /**
     * 测试设备连接
     *
     * @param device 设备实体，不能为null
     * @return 连接测试结果，包含连接状态和详细信息
     * @throws IllegalArgumentException 当device为null时抛出
     * @throws Exception                连接异常，包含网络错误、协议错误等
     */
    DeviceConnectionTest testConnection(AccessDeviceEntity device) throws Exception;

    /**
     * 下发人员数据
     *
     * @param device     设备实体，不能为null
     * @param personData 人员数据，包含人员基本信息，不能为null
     * @return 下发结果，包含成功状态和详细信息
     * @throws IllegalArgumentException 当参数为null时抛出
     * @throws Exception                下发异常，包含网络错误、设备错误、数据格式错误等
     */
    DeviceDispatchResult dispatchPersonData(AccessDeviceEntity device, Map<String, Object> personData) throws Exception;

    /**
     * 下发生物特征数据
     *
     * @param device        设备实体，不能为null
     * @param biometricData 生物特征数据，包含指纹、人脸等，不能为null
     * @return 下发结果，包含成功状态和详细信息
     * @throws IllegalArgumentException 当参数为null时抛出
     * @throws Exception                下发异常，包含网络错误、设备错误、数据格式错误等
     */
    DeviceDispatchResult dispatchBiometricData(AccessDeviceEntity device, Map<String, Object> biometricData)
            throws Exception;

    /**
     * 下发门禁配置数据
     *
     * @param device     设备实体，不能为null
     * @param configData 配置数据，包含门禁参数、时间计划等，不能为null
     * @return 下发结果，包含成功状态和详细信息
     * @throws IllegalArgumentException 当参数为null时抛出
     * @throws Exception                下发异常，包含网络错误、设备错误、配置错误等
     */
    DeviceDispatchResult dispatchAccessConfig(AccessDeviceEntity device, Map<String, Object> configData)
            throws Exception;

    /**
     * 远程开门
     *
     * @param device 设备实体，不能为null
     * @param doorId 门ID，不能为null或空
     * @return 开门结果，包含成功状态和执行时间
     * @throws IllegalArgumentException 当参数为null或空时抛出
     * @throws Exception                开门异常，包含网络错误、设备错误、权限错误等
     */
    DeviceDispatchResult remoteOpenDoor(AccessDeviceEntity device, String doorId) throws Exception;

    /**
     * 获取设备状态
     *
     * @param device 设备实体，不能为null
     * @return 设备状态，包含在线状态、锁状态、门磁状态等
     * @throws IllegalArgumentException 当device为null时抛出
     * @throws Exception                获取状态异常，包含网络错误、设备错误等
     */
    Map<String, Object> getDeviceStatus(AccessDeviceEntity device) throws Exception;

    /**
     * 查询设备人员列表
     *
     * @param device 设备实体，不能为null
     * @return 人员列表，包含人员基本信息，可能为空列表
     * @throws IllegalArgumentException 当device为null时抛出
     * @throws Exception                查询异常，包含网络错误、设备错误等
     */
    List<Map<String, Object>> queryPersonsOnDevice(AccessDeviceEntity device) throws Exception;

    /**
     * 删除设备人员数据
     *
     * @param device   设备实体，不能为null
     * @param personId 人员ID，必须大于0
     * @return 删除结果，包含成功状态和删除时间
     * @throws IllegalArgumentException 当参数无效时抛出
     * @throws Exception                删除异常，包含网络错误、设备错误等
     */
    DeviceDispatchResult deletePersonData(AccessDeviceEntity device, Long personId) throws Exception;

    /**
     * 批量下发人员数据
     *
     * @param device     设备实体，不能为null
     * @param personList 人员列表，不能为null或空
     * @return 批量下发结果，包含成功数量和失败详情
     * @throws IllegalArgumentException 当参数无效时抛出
     * @throws Exception                批量下发异常，包含网络错误、设备错误等
     */
    DeviceDispatchResult batchDispatchPersonData(AccessDeviceEntity device, List<Map<String, Object>> personList)
            throws Exception;

    /**
     * 获取门禁记录
     *
     * @param device      设备实体，不能为null
     * @param startTime   开始时间，格式：yyyy-MM-dd HH:mm:ss，可以为null
     * @param endTime     结束时间，格式：yyyy-MM-dd HH:mm:ss，可以为null
     * @param recordCount 记录数量限制，必须大于0，可以为null表示无限制
     * @return 门禁记录列表，包含开门时间、人员信息等，可能为空列表
     * @throws IllegalArgumentException 当device为null或recordCount无效时抛出
     * @throws Exception                获取记录异常，包含网络错误、设备错误等
     */
    List<Map<String, Object>> getAccessRecords(AccessDeviceEntity device, String startTime, String endTime,
            Integer recordCount) throws Exception;

    /**
     * 检查设备是否支持此协议
     *
     * @param device 设备实体，可以为null
     * @return 是否支持，false当device为null或不支持时
     */
    boolean supportsDevice(AccessDeviceEntity device);

    /**
     * 获取协议版本
     *
     * @return 协议版本，不能为null
     */
    String getProtocolVersion();

    /**
     * 获取协议特性
     *
     * @return 特性列表，包含协议支持的功能特性，不能为null
     */
    List<String> getProtocolFeatures();
}
