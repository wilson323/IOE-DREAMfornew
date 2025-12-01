package net.lab1024.sa.access.service;

/**
 * 智能设备服务接口
 */
public interface SmartDeviceService {

    /**
     * 设备在线检查
     */
    boolean checkDeviceOnline(Long deviceId);

    /**
     * 获取设备状态
     */
    String getDeviceStatus(Long deviceId);
}