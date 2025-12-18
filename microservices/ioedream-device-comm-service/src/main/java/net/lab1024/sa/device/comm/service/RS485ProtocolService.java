package net.lab1024.sa.device.comm.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.device.comm.protocol.rs485.*;

import java.util.Map;

/**
 * RS485协议服务接口
 * <p>
 * 提供RS485工业通讯协议的核心服务接口：
 * - 设备初始化和配置
 * - 消息处理和数据交换
 * - 心跳检测和状态监控
 * - 设备状态查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface RS485ProtocolService {

    /**
     * 初始化RS485设备
     *
     * @param deviceId   设备ID
     * @param deviceInfo 设备信息
     * @param config     配置参数
     * @return 初始化结果
     */
    ResponseDTO<RS485InitResultVO> initializeDevice(Long deviceId,
                                                     Map<String, Object> deviceInfo,
                                                     Map<String, Object> config);

    /**
     * 处理设备消息
     *
     * @param deviceId     设备ID
     * @param rawData      原始数据
     * @param protocolType 协议类型
     * @return 处理结果
     */
    ResponseDTO<RS485ProcessResultVO> processDeviceMessage(Long deviceId,
                                                            byte[] rawData,
                                                            String protocolType);

    /**
     * 发送心跳检测
     *
     * @param deviceId 设备ID
     * @return 心跳结果
     */
    ResponseDTO<RS485HeartbeatResultVO> sendHeartbeat(Long deviceId);

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    ResponseDTO<RS485DeviceStatusVO> getDeviceStatus(Long deviceId);

    /**
     * 处理设备心跳
     *
     * @param deviceId      设备ID
     * @param heartbeatData 心跳数据
     * @return 心跳结果
     */
    ResponseDTO<RS485HeartbeatResultVO> processDeviceHeartbeat(Long deviceId, java.util.Map<String, Object> heartbeatData);

    /**
     * 构建设备响应
     *
     * @param deviceId     设备ID
     * @param responseType 响应类型
     * @param data         响应数据
     * @return 响应结果
     */
    ResponseDTO<byte[]> buildDeviceResponse(Long deviceId, String responseType, java.util.Map<String, Object> data);

    /**
     * 断开设备连接
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<Void> disconnectDevice(Long deviceId);

    /**
     * 获取性能统计
     *
     * @return 性能统计数据
     */
    ResponseDTO<java.util.Map<String, Object>> getPerformanceStatistics();

    /**
     * 获取支持的设备型号
     *
     * @return 设备型号列表
     */
    ResponseDTO<String[]> getSupportedDeviceModels();

    /**
     * 检查设备型号是否支持
     *
     * @param deviceModel 设备型号
     * @return 是否支持
     */
    ResponseDTO<Boolean> isDeviceModelSupported(String deviceModel);
}
