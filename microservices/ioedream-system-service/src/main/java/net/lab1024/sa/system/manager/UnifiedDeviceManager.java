package net.lab1024.sa.system.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.system.domain.entity.UnifiedDeviceEntity;

/**
 * 统一设备管理器
 * 负责设备的注册、注销、心跳监控、远程控制等核心业务逻辑
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class UnifiedDeviceManager {

    // 在线设备缓存
    private final ConcurrentMap<Long, UnifiedDeviceEntity> onlineDevices = new ConcurrentHashMap<>();

    // 设备心跳超时时间（分钟）
    private static final int HEARTBEAT_TIMEOUT = 5;

    /**
     * 注册设备
     *
     * @param device 设备实体
     */
    public void registerDevice(UnifiedDeviceEntity device) {
        onlineDevices.put(device.getDeviceId(), device);
        // TODO: 调用设备SDK进行实际注册
        log.info("设备注册成功，设备ID：{}，设备类型：{}", device.getDeviceId(), device.getDeviceType());
    }

    /**
     * 注销设备
     *
     * @param deviceId 设备ID
     */
    public void unregisterDevice(Long deviceId) {
        onlineDevices.remove(deviceId);
        // TODO: 调用设备SDK进行实际注销
        log.info("设备注销成功，设备ID：{}", deviceId);
    }

    /**
     * 更新设备信息
     *
     * @param device 设备实体
     */
    public void updateDevice(UnifiedDeviceEntity device) {
        onlineDevices.computeIfPresent(device.getDeviceId(), (id, existingDevice) -> {
            // 保留心跳时间等重要字段
            device.setLastHeartbeat(existingDevice.getLastHeartbeat());
            return device;
        });
        log.info("设备信息更新成功，设备ID：{}", device.getDeviceId());
    }

    /**
     * 远程控制设备
     *
     * @param deviceId 设备ID
     * @param command  控制命令
     * @param params   控制参数
     * @return 控制结果
     */
    public String remoteControlDevice(Long deviceId, String command, Map<String, Object> params) {
        UnifiedDeviceEntity device = onlineDevices.get(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        // TODO: 根据设备类型调用相应的设备SDK
        String result = executeDeviceControl(device, command, params);

        log.info("远程控制设备完成，设备ID：{}，命令：{}，结果：{}", deviceId, command, result);
        return result;
    }

    /**
     * 远程开门
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    public String remoteOpenDoor(Long deviceId) {
        UnifiedDeviceEntity device = onlineDevices.get(deviceId);
        if (device == null) {
            throw new RuntimeException("门禁设备不存在");
        }

        if (!"ACCESS".equals(device.getDeviceType())) {
            throw new RuntimeException("设备类型不支持远程开门");
        }

        // TODO: 调用门禁设备SDK
        String result = simulateDoorOpen(device);

        log.info("远程开门完成，设备ID：{}，结果：{}", deviceId, result);
        return result;
    }

    /**
     * 云台控制
     *
     * @param deviceId 设备ID
     * @param command  云台命令
     * @param speed    控制速度
     * @return 控制结果
     */
    public String controlPtzDevice(Long deviceId, String command, Integer speed) {
        UnifiedDeviceEntity device = onlineDevices.get(deviceId);
        if (device == null) {
            throw new RuntimeException("视频设备不存在");
        }

        if (!"VIDEO".equals(device.getDeviceType())) {
            throw new RuntimeException("设备类型不支持云台控制");
        }

        // TODO: 调用视频设备SDK
        String result = simulatePtzControl(device, command, speed);

        log.info("云台控制完成，设备ID：{}，命令：{}，速度：{}，结果：{}", deviceId, command, speed, result);
        return result;
    }

    /**
     * 获取实时视频流
     *
     * @param deviceId 设备ID
     * @return 视频流URL
     */
    public String getLiveStream(Long deviceId) {
        UnifiedDeviceEntity device = onlineDevices.get(deviceId);
        if (device == null) {
            throw new RuntimeException("视频设备不存在");
        }

        if (!"VIDEO".equals(device.getDeviceType())) {
            throw new RuntimeException("设备类型不支持视频流");
        }

        // TODO: 调用视频设备SDK获取实时流
        String streamUrl = String.format("rtsp://device-%d/live", deviceId);

        log.info("获取实时视频流成功，设备ID：{}，流URL：{}", deviceId, streamUrl);
        return streamUrl;
    }

    /**
     * 检查设备心跳状态
     */
    public void checkDeviceHeartbeat() {
        List<Long> timeoutDevices = onlineDevices.entrySet().stream()
                .filter(entry -> isDeviceTimeout(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        timeoutDevices.forEach(deviceId -> {
            log.warn("设备心跳超时，设备ID：{}", deviceId);
            // TODO: 标记设备为离线状态
        });
    }

    /**
     * 获取在线设备数量
     *
     * @return 在线设备数量
     */
    public int getOnlineDeviceCount() {
        return onlineDevices.size();
    }

    /**
     * 执行设备控制
     */
    private String executeDeviceControl(UnifiedDeviceEntity device, String command, Map<String, Object> params) {
        // TODO: 根据设备类型和命令调用相应的设备SDK
        return String.format("设备控制执行成功，设备类型：%s，命令：%s", device.getDeviceType(), command);
    }

    /**
     * 模拟开门操作
     */
    private String simulateDoorOpen(UnifiedDeviceEntity device) {
        try {
            Thread.sleep(100); // 模拟设备响应时间
            return "开门成功";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "开门失败：操作中断";
        }
    }

    /**
     * 模拟云台控制
     */
    private String simulatePtzControl(UnifiedDeviceEntity device, String command, Integer speed) {
        try {
            Thread.sleep(200); // 模拟云台响应时间
            return String.format("云台控制成功，命令：%s，速度：%d", command, speed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "云台控制失败：操作中断";
        }
    }

    /**
     * 检查设备是否超时
     */
    private boolean isDeviceTimeout(UnifiedDeviceEntity device) {
        if (device.getLastHeartbeat() == null) {
            return true;
        }

        long timeoutMillis = HEARTBEAT_TIMEOUT * 60 * 1000;
        return System.currentTimeMillis() - device.getLastHeartbeat().atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli() > timeoutMillis;
    }
}
