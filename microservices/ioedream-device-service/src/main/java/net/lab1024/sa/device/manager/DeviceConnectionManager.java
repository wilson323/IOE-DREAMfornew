package net.lab1024.sa.device.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.domain.entity.PhysicalDeviceEntity;

/**
 * 设备连接管理器
 *
 * 负责管理设备的连接状态、连接建立和断开、心跳处理等
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class DeviceConnectionManager {

    // 设备连接状态映射
    private final Map<Long, PhysicalDeviceEntity> connectedDevices = new ConcurrentHashMap<>();

    /**
     * 连接设备
     *
     * @param device 设备实体
     * @return 是否连接成功
     */
    public boolean connectDevice(PhysicalDeviceEntity device) {
        if (device == null || device.getDeviceId() == null) {
            log.warn("设备连接失败：设备信息为空");
            return false;
        }

        try {
            Long deviceId = device.getDeviceId();

            // 检查设备是否已连接
            if (connectedDevices.containsKey(deviceId)) {
                log.info("设备已连接，设备ID：{}", deviceId);
                return true;
            }

            // 这里可以添加具体的连接逻辑，比如TCP连接、HTTP连接等
            // 目前先简单地标记为已连接
            connectedDevices.put(deviceId, device);

            log.info("设备连接成功，设备ID：{}，IP：{}，端口：{}",
                    deviceId, device.getIpAddress(), device.getPort());

            return true;
        } catch (Exception e) {
            log.error("设备连接异常，设备ID：{}", device.getDeviceId(), e);
            return false;
        }
    }

    /**
     * 断开设备连接
     *
     * @param deviceId 设备ID
     * @return 是否断开成功
     */
    public boolean disconnectDevice(Long deviceId) {
        if (deviceId == null) {
            log.warn("设备断开失败：设备ID为空");
            return false;
        }

        try {
            PhysicalDeviceEntity device = connectedDevices.remove(deviceId);

            if (device != null) {
                // 这里可以添加具体的断开连接逻辑
                log.info("设备断开连接成功，设备ID：{}", deviceId);
                return true;
            } else {
                log.warn("设备未连接，无法断开，设备ID：{}", deviceId);
                return false;
            }
        } catch (Exception e) {
            log.error("设备断开连接异常，设备ID：{}", deviceId, e);
            return false;
        }
    }

    /**
     * 处理设备心跳
     *
     * @param deviceId      设备ID
     * @param heartbeatData 心跳数据
     * @return 是否处理成功
     */
    public boolean handleDeviceHeartbeat(Long deviceId, Map<String, Object> heartbeatData) {
        if (deviceId == null) {
            log.warn("处理设备心跳失败：设备ID为空");
            return false;
        }

        try {
            PhysicalDeviceEntity device = connectedDevices.get(deviceId);

            if (device != null) {
                // 更新心跳时间
                device.setLastHeartbeatTime(java.time.LocalDateTime.now());

                // 如果有心跳数据，可以在这里处理
                if (heartbeatData != null && !heartbeatData.isEmpty()) {
                    log.debug("收到设备心跳数据，设备ID：{}，数据：{}", deviceId, heartbeatData);
                }

                return true;
            } else {
                log.warn("设备未连接，无法处理心跳，设备ID：{}", deviceId);
                return false;
            }
        } catch (Exception e) {
            log.error("处理设备心跳异常，设备ID：{}", deviceId, e);
            return false;
        }
    }

    /**
     * 更新连接管理器中的设备信息
     *
     * @param device 设备实体
     */
    public void updateDeviceInConnectionManager(PhysicalDeviceEntity device) {
        if (device == null || device.getDeviceId() == null) {
            return;
        }

        Long deviceId = device.getDeviceId();
        if (connectedDevices.containsKey(deviceId)) {
            connectedDevices.put(deviceId, device);
            log.debug("更新连接管理器中的设备信息，设备ID：{}", deviceId);
        }
    }

    /**
     * 检查设备是否已连接
     *
     * @param deviceId 设备ID
     * @return 是否已连接
     */
    public boolean isDeviceConnected(Long deviceId) {
        return deviceId != null && connectedDevices.containsKey(deviceId);
    }

    /**
     * 获取已连接设备数量
     *
     * @return 已连接设备数量
     */
    public int getConnectedDeviceCount() {
        return connectedDevices.size();
    }
}
