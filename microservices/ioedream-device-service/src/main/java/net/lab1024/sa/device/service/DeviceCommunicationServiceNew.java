package net.lab1024.sa.device.service;

import net.lab1024.sa.device.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.device.protocol.DeviceProtocolException;

import java.util.Map;

/**
 * 设备通信服务接口 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 统一的设备通信接口
 * - 支持多种协议和设备类型
 * - 完整的异常处理机制
 * - 设备控制和状态查询
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
public interface DeviceCommunicationServiceNew {

    /**
     * 远程开门
     *
     * @param accessDeviceId 门禁设备ID
     * @return 是否成功
     * @throws DeviceProtocolException 协议异常
     */
    boolean remoteOpenDoor(Long accessDeviceId) throws DeviceProtocolException;

    /**
     * 重启设备
     *
     * @param accessDeviceId 门禁设备ID
     * @return 是否成功
     * @throws DeviceProtocolException 协议异常
     */
    boolean restartDevice(Long accessDeviceId) throws DeviceProtocolException;

    /**
     * 同步设备时间
     *
     * @param accessDeviceId 门禁设备ID
     * @return 是否成功
     * @throws DeviceProtocolException 协议异常
     */
    boolean syncDeviceTime(Long accessDeviceId) throws DeviceProtocolException;

    /**
     * 检查设备连接状态
     *
     * @param accessDeviceId 门禁设备ID
     * @return 是否在线
     * @throws DeviceProtocolException 协议异常
     */
    boolean checkDeviceConnection(Long accessDeviceId) throws DeviceProtocolException;

    /**
     * 获取设备状态信息
     *
     * @param accessDeviceId 门禁设备ID
     * @return 设备状态信息
     * @throws DeviceProtocolException 协议异常
     */
    Map<String, Object> getDeviceStatus(Long accessDeviceId) throws DeviceProtocolException;

    /**
     * 发送自定义命令
     *
     * @param accessDeviceId 门禁设备ID
     * @param command      命令内容
     * @param params       命令参数
     * @return 命令执行结果
     * @throws DeviceProtocolException 协议异常
     */
    Map<String, Object> sendCustomCommand(Long accessDeviceId, String command, Map<String, Object> params) throws DeviceProtocolException;

    /**
     * 批量远程开门
     *
     * @param accessDeviceIds 门禁设备ID列表
     * @return 执行结果
     */
    Map<Long, Boolean> batchRemoteOpenDoor(java.util.List<Long> accessDeviceIds);

    /**
     * 批量重启设备
     *
     * @param accessDeviceIds 门禁设备ID列表
     * @return 执行结果
     */
    Map<Long, Boolean> batchRestartDevice(java.util.List<Long> accessDeviceIds);

    /**
     * 批量同步设备时间
     *
     * @param accessDeviceIds 门禁设备ID列表
     * @return 执行结果
     */
    Map<Long, Boolean> batchSyncDeviceTime(java.util.List<Long> accessDeviceIds);

    /**
     * 批量检查设备连接
     *
     * @param accessDeviceIds 门禁设备ID列表
     * @return 连接状态结果
     */
    Map<Long, Boolean> batchCheckDeviceConnection(java.util.List<Long> accessDeviceIds);

    /**
     * 获取设备健康状态
     *
     * @param accessDeviceId 门禁设备ID
     * @return 健康状态信息
     */
    Map<String, Object> getDeviceHealthStatus(Long accessDeviceId);

    /**
     * 测试设备连接
     *
     * @param device 门禁设备实体
     * @return 测试结果
     */
    Map<String, Object> testDeviceConnection(AccessDeviceEntity device);

    /**
     * 获取支持的协议类型
     *
     * @return 支持的协议类型列表
     */
    java.util.List<String> getSupportedProtocols();

    /**
     * 检查设备协议是否支持
     *
     * @param device 门禁设备实体
     * @return 是否支持
     */
    boolean isProtocolSupported(AccessDeviceEntity device);
}