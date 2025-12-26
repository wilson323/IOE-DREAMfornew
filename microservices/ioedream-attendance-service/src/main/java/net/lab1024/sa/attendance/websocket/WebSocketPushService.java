package net.lab1024.sa.attendance.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket推送服务
 * <p>
 * 提供实时监控数据推送功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class WebSocketPushService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 广播消息到所有订阅者
     *
     * @param topic 主题
     * @param message 消息内容
     */
    public void broadcast(String topic, Object message) {
        log.info("[WebSocket推送] 广播消息: topic={}, message={}", topic, message);
        try {
            messagingTemplate.convertAndSend(topic, message);
        } catch (Exception e) {
            log.error("[WebSocket推送] 广播失败: topic={}, error={}", topic, e.getMessage(), e);
        }
    }

    /**
     * 发送消息到指定用户
     *
     * @param userId 用户ID
     * @param topic 主题
     * @param message 消息内容
     */
    public void sendToUser(String userId, String topic, Object message) {
        log.info("[WebSocket推送] 发送给用户: userId={}, topic={}, message={}", userId, topic, message);
        try {
            messagingTemplate.convertAndSendToUser(userId, topic, message);
        } catch (Exception e) {
            log.error("[WebSocket推送] 发送失败: userId={}, topic={}, error={}", userId, topic, e.getMessage(), e);
        }
    }

    /**
     * 推送设备状态更新
     *
     * @param deviceStatus 设备状态数据
     */
    public void pushDeviceStatusUpdate(Map<String, Object> deviceStatus) {
        broadcast("/topic/attendance/device/status", deviceStatus);
    }

    /**
     * 推送告警信息
     *
     * @param alert 告警数据
     */
    public void pushAlert(Map<String, Object> alert) {
        broadcast("/topic/attendance/alert", alert);
    }

    /**
     * 推送考勤统计更新
     *
     * @param statistics 统计数据
     */
    public void pushStatisticsUpdate(Map<String, Object> statistics) {
        broadcast("/topic/attendance/statistics", statistics);
    }

    /**
     * 推送实时打卡记录
     *
     * @param punchRecord 打卡记录
     */
    public void pushPunchRecord(Map<String, Object> punchRecord) {
        broadcast("/topic/attendance/punch", punchRecord);
    }
}
