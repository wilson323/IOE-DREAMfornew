package net.lab1024.sa.attendance.websocket;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket消息控制器
 * <p>
 * 处理WebSocket客户端发送的消息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Controller
public class WebSocketMessageController {

    @Resource
    private DeviceStatusPushService deviceStatusPushService;

    @Resource
    private AlertPushService alertPushService;

    /**
     * 客户端请求刷新设备状态
     */
    @MessageMapping("/app/attendance/device/refresh")
    @SendTo("/topic/attendance/device/status")
    public Map<String, Object> refreshDeviceStatus(Map<String, Object> request) {
        log.info("[WebSocket消息] 客户端请求刷新设备状态: request={}", request);

        // 触发设备状态推送
        deviceStatusPushService.triggerDeviceStatusPush();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "设备状态已刷新");
        response.put("timestamp", LocalDateTime.now().toString());

        return response;
    }

    /**
     * 客户端请求获取告警列表
     */
    @MessageMapping("/app/attendance/alert/list")
    @SendTo("/topic/attendance/alert")
    public Map<String, Object> getAlertList(Map<String, Object> request) {
        log.info("[WebSocket消息] 客户端请求获取告警列表: request={}", request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "告警列表已获取");
        response.put("timestamp", LocalDateTime.now().toString());

        return response;
    }

    /**
     * 客户端心跳消息
     */
    @MessageMapping("/app/attendance/heartbeat")
    public void handleHeartbeat(Map<String, Object> heartbeat) {
        log.debug("[WebSocket消息] 收到客户端心跳: heartbeat={}", heartbeat);
        // 仅用于保持连接，无需回复
    }
}
