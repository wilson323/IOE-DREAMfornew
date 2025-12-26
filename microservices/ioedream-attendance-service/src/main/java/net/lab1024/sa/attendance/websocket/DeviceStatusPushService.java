package net.lab1024.sa.attendance.websocket;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备状态实时推送服务
 * <p>
 * 定时推送设备状态变化
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class DeviceStatusPushService {

    private final WebSocketPushService webSocketPushService;
    private ScheduledExecutorService scheduler;

    public DeviceStatusPushService(WebSocketPushService webSocketPushService) {
        this.webSocketPushService = webSocketPushService;
    }

    /**
     * 启动设备状态推送
     */
    @PostConstruct
    public void startPushing() {
        log.info("[设备状态推送] 启动设备状态实时推送");

        scheduler = Executors.newScheduledThreadPool(1);

        // 每30秒推送一次设备状态
        scheduler.scheduleAtFixedRate(this::pushDeviceStatus, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * 停止设备状态推送
     */
    @PreDestroy
    public void stopPushing() {
        log.info("[设备状态推送] 停止设备状态实时推送");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 推送设备状态
     */
    private void pushDeviceStatus() {
        try {
            // TODO: 从实际设备表查询设备状态
            Map<String, Object> deviceStatus = new HashMap<>();
            deviceStatus.put("updateTime", LocalDateTime.now().toString());
            deviceStatus.put("onlineCount", 45);
            deviceStatus.put("offlineCount", 5);
            deviceStatus.put("totalCount", 50);

            // 模拟设备列表
            java.util.List<Map<String, Object>> devices = new java.util.ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                Map<String, Object> device = new HashMap<>();
                device.put("deviceId", (long) i);
                device.put("deviceCode", "DEV" + String.format("%03d", i));
                device.put("deviceName", "考勤设备" + i);
                device.put("deviceType", "ATTENDANCE");
                device.put("status", i % 10 == 0 ? "OFFLINE" : "ONLINE");
                device.put("lastHeartbeat", LocalDateTime.now().minusSeconds((long) (Math.random() * 60)).toString());
                devices.add(device);
            }
            deviceStatus.put("devices", devices);

            webSocketPushService.pushDeviceStatusUpdate(deviceStatus);

        } catch (Exception e) {
            log.error("[设备状态推送] 推送失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 手动触发设备状态推送
     */
    public void triggerDeviceStatusPush() {
        log.info("[设备状态推送] 手动触发推送");
        pushDeviceStatus();
    }
}
