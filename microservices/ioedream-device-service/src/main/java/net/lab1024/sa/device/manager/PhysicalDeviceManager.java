package net.lab1024.sa.device.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.domain.entity.PhysicalDeviceEntity;
import net.lab1024.sa.device.domain.form.PhysicalDeviceAddForm;
import net.lab1024.sa.device.domain.form.PhysicalDeviceUpdateForm;

/**
 * 物理设备管理器
 *
 * 负责设备连接管理、状态监控、缓存策略等复杂业务逻辑
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class PhysicalDeviceManager {

    @Resource
    private DeviceConnectionManager deviceConnectionManager;

    // 在线设备缓存
    private final Map<Long, PhysicalDeviceEntity> onlineDevices = new ConcurrentHashMap<>();

    // 设备连接状态缓存
    private final Map<Long, String> deviceConnectionStatus = new ConcurrentHashMap<>();

    // 设备心跳超时时间（分钟）
    private static final int HEARTBEAT_TIMEOUT = 5;

    /**
     * 处理设备注册
     *
     * @param addForm 设备添加表单
     * @return 设备实体
     */
    public PhysicalDeviceEntity processDeviceRegistration(PhysicalDeviceAddForm addForm) {
        log.debug("开始处理设备注册，设备编号：{}", addForm.getDeviceCode());

        PhysicalDeviceEntity device = new PhysicalDeviceEntity();
        device.setDeviceCode(addForm.getDeviceCode());
        device.setDeviceName(addForm.getDeviceName());
        device.setDeviceType(addForm.getDeviceType());
        device.setDeviceModel(addForm.getDeviceModel());
        device.setManufacturer(addForm.getManufacturer());
        device.setDeviceStatus("OFFLINE");
        device.setIpAddress(addForm.getIpAddress());
        device.setPort(addForm.getPort());
        device.setLocation(addForm.getLocation());
        device.setDescription(addForm.getDescription());
        device.setCreateTime(LocalDateTime.now());
        device.setUpdateTime(LocalDateTime.now());

        log.debug("设备注册处理完成，设备ID：{}", device.getDeviceId());
        return device;
    }

    /**
     * 更新设备信息
     *
     * @param existingDevice 现有设备
     * @param updateForm     更新表单
     * @return 更新后的设备
     */
    public PhysicalDeviceEntity processDeviceUpdate(PhysicalDeviceEntity existingDevice,
            PhysicalDeviceUpdateForm updateForm) {
        log.debug("开始处理设备更新，设备ID：{}", existingDevice.getDeviceId());

        // 更新基本信息
        if (updateForm.getDeviceName() != null) {
            existingDevice.setDeviceName(updateForm.getDeviceName());
        }
        if (updateForm.getDeviceModel() != null) {
            existingDevice.setDeviceModel(updateForm.getDeviceModel());
        }
        if (updateForm.getDeviceManufacturer() != null) {
            existingDevice.setManufacturer(updateForm.getDeviceManufacturer());
        }
        if (updateForm.getDeviceDescription() != null) {
            existingDevice.setDescription(updateForm.getDeviceDescription());
        }
        if (updateForm.getDeviceStatus() != null) {
            existingDevice.setDeviceStatus(String.valueOf(updateForm.getDeviceStatus()));
        }
        existingDevice.setUpdateTime(LocalDateTime.now());

        // 如果设备在线，同步更新连接管理器中的设备信息
        if ("ONLINE".equals(existingDevice.getDeviceStatus())) {
            deviceConnectionManager.updateDeviceInConnectionManager(existingDevice);
        }

        log.debug("设备更新处理完成，设备ID：{}", existingDevice.getDeviceId());
        return existingDevice;
    }

    /**
     * 建立设备连接
     *
     * @param device 设备实体
     * @return 连接结果
     */
    public Map<String, Object> establishDeviceConnection(PhysicalDeviceEntity device) {
        log.debug("开始建立设备连接，设备ID：{}，IP：{}，端口：{}",
                device.getDeviceId(), device.getIpAddress(), device.getPort());

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用连接管理器建立连接
            boolean connected = deviceConnectionManager.connectDevice(device);

            if (connected) {
                // 更新缓存状态
                onlineDevices.put(device.getDeviceId(), device);
                deviceConnectionStatus.put(device.getDeviceId(), "CONNECTED");

                result.put("success", true);
                result.put("message", "设备连接成功");
                result.put("connectedTime", System.currentTimeMillis());

                log.info("设备连接成功，设备ID：{}", device.getDeviceId());
            } else {
                result.put("success", false);
                result.put("message", "设备连接失败");
                result.put("error", "无法建立设备连接");

                log.warn("设备连接失败，设备ID：{}", device.getDeviceId());
            }

        } catch (Exception e) {
            log.error("设备连接异常，设备ID：{}", device.getDeviceId(), e);

            result.put("success", false);
            result.put("message", "设备连接异常");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 断开设备连接
     *
     * @param deviceId 设备ID
     * @return 断开结果
     */
    public Map<String, Object> disconnectDeviceConnection(Long deviceId) {
        log.debug("开始断开设备连接，设备ID：{}", deviceId);

        Map<String, Object> result = new HashMap<>();

        try {
            // 从在线缓存中移除
            onlineDevices.remove(deviceId);
            deviceConnectionStatus.remove(deviceId);

            // 调用连接管理器断开连接
            boolean disconnected = deviceConnectionManager.disconnectDevice(deviceId);

            if (disconnected) {
                result.put("success", true);
                result.put("message", "设备连接断开成功");
                result.put("disconnectedTime", System.currentTimeMillis());

                log.info("设备连接断开成功，设备ID：{}", deviceId);
            } else {
                result.put("success", false);
                result.put("message", "设备连接断开失败");

                log.warn("设备连接断开失败，设备ID：{}", deviceId);
            }

        } catch (Exception e) {
            log.error("设备连接断开异常，设备ID：{}", deviceId, e);

            result.put("success", false);
            result.put("message", "设备连接断开异常");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 处理设备心跳
     *
     * @param deviceId      设备ID
     * @param heartbeatData 心跳数据
     * @return 心跳处理结果
     */
    public boolean processDeviceHeartbeat(Long deviceId, Map<String, Object> heartbeatData) {
        log.debug("处理设备心跳，设备ID：{}", deviceId);

        try {
            PhysicalDeviceEntity device = onlineDevices.get(deviceId);
            if (device == null) {
                log.warn("收到离线设备心跳，设备ID：{}", deviceId);
                return false;
            }

            // 更新设备最后心跳时间
            device.setLastHeartbeatTime(LocalDateTime.now());

            // 更新心跳数据（需要转换为JSON字符串）
            if (heartbeatData != null) {
                try {
                    String heartbeatJson = com.alibaba.fastjson2.JSON.toJSONString(heartbeatData);
                    device.setHeartbeatData(heartbeatJson);
                } catch (Exception e) {
                    log.warn("心跳数据JSON转换失败，设备ID：{}", deviceId, e);
                }
            }

            // 调用连接管理器处理心跳
            return deviceConnectionManager.handleDeviceHeartbeat(deviceId, heartbeatData);

        } catch (Exception e) {
            log.error("设备心跳处理异常，设备ID：{}", deviceId, e);
            return false;
        }
    }

    /**
     * 获取设备状态信息
     *
     * @param device 设备实体
     * @return 状态信息
     */
    public Map<String, Object> getDeviceStatusInfo(PhysicalDeviceEntity device) {
        Map<String, Object> statusInfo = new HashMap<>();

        statusInfo.put("deviceId", device.getDeviceId());
        statusInfo.put("deviceCode", device.getDeviceCode());
        statusInfo.put("deviceName", device.getDeviceName());
        statusInfo.put("deviceType", device.getDeviceType());
        statusInfo.put("deviceStatus", device.getDeviceStatus());

        // 检查是否在线
        boolean isOnline = onlineDevices.containsKey(device.getDeviceId());
        statusInfo.put("isOnline", isOnline);

        if (isOnline) {
            statusInfo.put("connectionStatus", deviceConnectionStatus.get(device.getDeviceId()));
            statusInfo.put("lastHeartbeatTime", device.getLastHeartbeatTime());

            // 计算心跳延迟
            if (device.getLastHeartbeatTime() != null) {
                long heartbeatAge = java.time.Duration.between(
                        device.getLastHeartbeatTime(), LocalDateTime.now()).getSeconds();
                statusInfo.put("heartbeatAge", heartbeatAge);
                statusInfo.put("heartbeatStatus", heartbeatAge < HEARTBEAT_TIMEOUT * 60 ? "normal" : "timeout");
            }
        }

        statusInfo.put("ipAddress", device.getIpAddress());
        statusInfo.put("port", device.getPort());
        statusInfo.put("location", device.getLocation());

        return statusInfo;
    }

    /**
     * 批量连接设备
     *
     * @param devices 设备列表
     * @return 连接结果统计
     */
    public Map<String, Integer> batchConnectDevices(List<PhysicalDeviceEntity> devices) {
        Map<String, Integer> results = new HashMap<>();
        results.put("total", devices.size());
        results.put("success", 0);
        results.put("failed", 0);

        for (PhysicalDeviceEntity device : devices) {
            try {
                Map<String, Object> connectResult = establishDeviceConnection(device);
                if ((Boolean) connectResult.get("success")) {
                    results.put("success", results.get("success") + 1);
                } else {
                    results.put("failed", results.get("failed") + 1);
                }
            } catch (Exception e) {
                log.error("批量连接设备失败，设备ID：{}", device.getDeviceId(), e);
                results.put("failed", results.get("failed") + 1);
            }
        }

        log.info("批量设备连接完成，总数：{}，成功：{}，失败：{}",
                results.get("total"), results.get("success"), results.get("failed"));

        return results;
    }

    /**
     * 检查设备心跳超时
     *
     * @return 超时的设备ID列表
     */
    public List<Long> checkDeviceHeartbeatTimeout() {
        List<Long> timeoutDevices = new ArrayList<>();
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(HEARTBEAT_TIMEOUT);

        onlineDevices.entrySet().removeIf(entry -> {
            PhysicalDeviceEntity device = entry.getValue();
            if (device.getLastHeartbeatTime() == null ||
                    device.getLastHeartbeatTime().isBefore(cutoffTime)) {
                timeoutDevices.add(entry.getKey());
                deviceConnectionStatus.remove(entry.getKey());
                return true; // 从在线设备中移除
            }
            return false;
        });

        if (!timeoutDevices.isEmpty()) {
            log.warn("发现设备心跳超时，设备数量：{}", timeoutDevices.size());
        }

        return timeoutDevices;
    }

    /**
     * 获取在线设备统计
     *
     * @return 统计信息
     */
    public Map<String, Object> getOnlineDeviceStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        statistics.put("totalOnlineDevices", onlineDevices.size());

        // 按设备类型统计
        Map<String, Long> typeStatistics = new HashMap<>();
        onlineDevices.values().forEach(device -> {
            String deviceType = device.getDeviceType();
            typeStatistics.merge(deviceType, 1L, (existing, increment) -> existing + increment);
        });
        statistics.put("byType", typeStatistics);

        // 连接状态统计
        Map<String, Long> connectionStatistics = new HashMap<>();
        deviceConnectionStatus.values().forEach(status -> {
            connectionStatistics.merge(status, 1L, (existing, increment) -> existing + increment);
        });
        statistics.put("byConnectionStatus", connectionStatistics);

        return statistics;
    }

    /**
     * 清理过期的缓存数据
     */
    public void cleanExpiredCache() {
        log.debug("开始清理设备管理器缓存");

        // 检查心跳超时设备
        List<Long> timeoutDevices = checkDeviceHeartbeatTimeout();

        if (!timeoutDevices.isEmpty()) {
            log.info("清理过期设备缓存，清理数量：{}", timeoutDevices.size());
        }
    }
}
