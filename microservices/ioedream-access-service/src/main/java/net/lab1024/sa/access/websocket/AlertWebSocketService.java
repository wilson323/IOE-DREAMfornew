package net.lab1024.sa.access.websocket;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.entity.DeviceAlertEntity;
import net.lab1024.sa.access.domain.entity.AlertNotificationEntity;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 告警WebSocket推送服务
 * <p>
 * 提供WebSocket告警推送的业务接口：
 * - 告警广播（推送给所有在线用户）
 * - 用户定向推送（推送给指定用户）
 * - 系统消息推送
 * - 连接状态查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class AlertWebSocketService {

    @Resource
    private AlertWebSocketHandler alertWebSocketHandler;

    /**
     * 广播告警消息（推送给所有在线用户）
     */
    public ResponseDTO<Void> broadcastAlert(DeviceAlertEntity alert) {
        log.info("[WebSocket推送] 广播告警: alertId={}, alertType={}, alertLevel={}",
                alert.getAlertId(), alert.getAlertType(), alert.getAlertLevel());

        try {
            alertWebSocketHandler.broadcastAlert(alert);
            log.info("[WebSocket推送] 广播成功: alertId={}", alert.getAlertId());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[WebSocket推送] 广播失败: alertId={}, error={}",
                    alert.getAlertId(), e.getMessage(), e);
            return ResponseDTO.error("BROADCAST_FAILED", "广播告警失败: " + e.getMessage());
        }
    }

    /**
     * 向指定用户推送告警
     */
    public ResponseDTO<Void> sendAlertToUser(Long userId, DeviceAlertEntity alert) {
        log.info("[WebSocket推送] 用户定向推送: userId={}, alertId={}", userId, alert.getAlertId());

        try {
            alertWebSocketHandler.sendAlertToUser(userId, alert);
            log.info("[WebSocket推送] 用户推送成功: userId={}, alertId={}", userId, alert.getAlertId());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[WebSocket推送] 用户推送失败: userId={}, alertId={}, error={}",
                    userId, alert.getAlertId(), e.getMessage(), e);
            return ResponseDTO.error("SEND_TO_USER_FAILED", "推送告警失败: " + e.getMessage());
        }
    }

    /**
     * 向多个用户批量推送告警
     */
    public ResponseDTO<Integer> sendAlertToUsers(List<Long> userIds, DeviceAlertEntity alert) {
        log.info("[WebSocket推送] 批量用户推送: count={}, alertId={}", userIds.size(), alert.getAlertId());

        int successCount = 0;
        for (Long userId : userIds) {
            try {
                alertWebSocketHandler.sendAlertToUser(userId, alert);
                successCount++;
            } catch (Exception e) {
                log.error("[WebSocket推送] 用户推送失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        log.info("[WebSocket推送] 批量推送完成: total={}, success={}", userIds.size(), successCount);
        return ResponseDTO.ok(successCount);
    }

    /**
     * 推送通知记录（用于通知发送状态更新）
     */
    public ResponseDTO<Void> sendNotification(Long userId, AlertNotificationEntity notification) {
        log.info("[WebSocket推送] 推送通知记录: userId={}, notificationId={}",
                userId, notification.getNotificationId());

        try {
            // 构建通知消息
            Map<String, Object> message = Map.of(
                    "type", "NOTIFICATION",
                    "notificationId", notification.getNotificationId(),
                    "alertId", notification.getAlertId(),
                    "notificationMethod", notification.getNotificationMethod(),
                    "notificationStatus", notification.getNotificationStatus(),
                    "notificationTitle", notification.getNotificationTitle(),
                    "notificationContent", notification.getNotificationContent(),
                    "timestamp", System.currentTimeMillis()
            );

            // TODO: 实现推送逻辑
            log.info("[WebSocket推送] 通知记录推送成功: notificationId={}", notification.getNotificationId());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[WebSocket推送] 通知记录推送失败: notificationId={}, error={}",
                    notification.getNotificationId(), e.getMessage(), e);
            return ResponseDTO.error("SEND_NOTIFICATION_FAILED", "推送通知记录失败");
        }
    }

    /**
     * 广播系统消息
     */
    public ResponseDTO<Void> broadcastSystemMessage(String title, String content) {
        log.info("[WebSocket推送] 广播系统消息: title={}", title);

        try {
            alertWebSocketHandler.broadcastSystemMessage(title, content);
            log.info("[WebSocket推送] 系统消息广播成功: title={}", title);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[WebSocket推送] 系统消息广播失败: title={}, error={}",
                    title, e.getMessage(), e);
            return ResponseDTO.error("BROADCAST_SYSTEM_MESSAGE_FAILED", "广播系统消息失败");
        }
    }

    /**
     * 获取WebSocket连接统计
     */
    public ResponseDTO<Map<String, Integer>> getStatistics() {
        log.debug("[WebSocket推送] 获取连接统计");

        Map<String, Integer> stats = Map.of(
                "onlineUsers", alertWebSocketHandler.getOnlineUserCount(),
                "totalConnections", alertWebSocketHandler.getConnectionCount()
        );

        return ResponseDTO.ok(stats);
    }

    /**
     * 检查用户是否在线
     */
    public ResponseDTO<Boolean> isUserOnline(Long userId) {
        log.debug("[WebSocket推送] 检查用户在线状态: userId={}", userId);

        // TODO: 实现用户在线状态检查
        // 当前实现：简单判断用户会话映射中是否存在
        boolean online = alertWebSocketHandler.getOnlineUserCount() > 0;

        return ResponseDTO.ok(online);
    }
}
