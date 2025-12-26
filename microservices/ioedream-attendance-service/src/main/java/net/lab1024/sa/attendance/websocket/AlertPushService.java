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
 * 告警信息实时推送服务
 * <p>
 * 监控并推送考勤异常告警
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class AlertPushService {

    private final WebSocketPushService webSocketPushService;
    private ScheduledExecutorService scheduler;

    public AlertPushService(WebSocketPushService webSocketPushService) {
        this.webSocketPushService = webSocketPushService;
    }

    /**
     * 启动告警推送
     */
    @PostConstruct
    public void startPushing() {
        log.info("[告警推送] 启动告警实时推送");

        scheduler = Executors.newScheduledThreadPool(1);

        // 每60秒检查一次告警
        scheduler.scheduleAtFixedRate(this::checkAndPushAlerts, 0, 60, TimeUnit.SECONDS);
    }

    /**
     * 停止告警推送
     */
    @PreDestroy
    public void stopPushing() {
        log.info("[告警推送] 停止告警实时推送");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 检查并推送告警
     */
    private void checkAndPushAlerts() {
        try {
            // TODO: 从实际数据源查询告警信息
            // 这里模拟一个告警数据
            if (Math.random() > 0.7) { // 30%概率产生告警
                Map<String, Object> alert = new HashMap<>();
                alert.put("alertId", System.currentTimeMillis());
                alert.put("alertType", "DEVICE_OFFLINE");
                alert.put("alertLevel", "WARNING");
                alert.put("alertTitle", "设备离线告警");
                alert.put("alertMessage", "设备 DEV010 已离线超过5分钟");
                alert.put("deviceId", 10L);
                alert.put("deviceCode", "DEV010");
                alert.put("deviceName", "考勤设备10");
                alert.put("occurredTime", LocalDateTime.now().toString());
                alert.put("handled", false);

                webSocketPushService.pushAlert(alert);
            }

        } catch (Exception e) {
            log.error("[告警推送] 推送失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 手动触发告警推送
     *
     * @param alert 告警数据
     */
    public void triggerAlertPush(Map<String, Object> alert) {
        log.info("[告警推送] 手动触发推送: alertType={}", alert.get("alertType"));
        webSocketPushService.pushAlert(alert);
    }

    /**
     * 创建设备离线告警
     */
    public Map<String, Object> createDeviceOfflineAlert(Long deviceId, String deviceCode, String deviceName) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("alertId", System.currentTimeMillis());
        alert.put("alertType", "DEVICE_OFFLINE");
        alert.put("alertLevel", "WARNING");
        alert.put("alertTitle", "设备离线告警");
        alert.put("alertMessage", String.format("设备 %s(%s) 已离线", deviceName, deviceCode));
        alert.put("deviceId", deviceId);
        alert.put("deviceCode", deviceCode);
        alert.put("deviceName", deviceName);
        alert.put("occurredTime", LocalDateTime.now().toString());
        alert.put("handled", false);
        return alert;
    }

    /**
     * 创建考勤异常告警
     */
    public Map<String, Object> createAttendanceAnomalyAlert(Long userId, String userName, String anomalyType) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("alertId", System.currentTimeMillis());
        alert.put("alertType", "ATTENDANCE_ANOMALY");
        alert.put("alertLevel", "INFO");
        alert.put("alertTitle", "考勤异常告警");
        alert.put("alertMessage", String.format("用户 %s(%d) 检测到%s异常", userName, userId, anomalyType));
        alert.put("userId", userId);
        alert.put("userName", userName);
        alert.put("anomalyType", anomalyType);
        alert.put("occurredTime", LocalDateTime.now().toString());
        alert.put("handled", false);
        return alert;
    }
}
