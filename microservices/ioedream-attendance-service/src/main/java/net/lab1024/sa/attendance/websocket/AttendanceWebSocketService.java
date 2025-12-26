package net.lab1024.sa.attendance.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 考勤WebSocket推送服务
 * <p>
 * 使用STOMP协议进行WebSocket通信
 * 推送实时考勤结果到前端
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Service
@Slf4j
public class AttendanceWebSocketService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 推送考勤结果到用户
     *
     * @param userId 用户ID
     * @param result 考勤结果
     */
    public void pushAttendanceResultToUser(Long userId, AttendanceResultVO result) {
        try {
            log.info("[WebSocket推送] 推送考勤结果: userId={}, status={}",
                     userId, result != null ? result.getStatus() : "null");

            Map<String, Object> message = new HashMap<>();
            message.put("type", "ATTENDANCE_RESULT");
            message.put("userId", userId);
            message.put("data", result);
            message.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 推送到用户的私人队列
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/attendance",
                message
            );

            log.info("[WebSocket推送] 推送成功: userId={}", userId);

        } catch (Exception e) {
            log.error("[WebSocket推送] 推送失败: userId={}, error={}",
                     userId, e.getMessage(), e);
        }
    }

    /**
     * 广播考勤结果到所有订阅者
     *
     * @param result 考勤结果
     */
    public void broadcastAttendanceResult(AttendanceResultVO result) {
        try {
            log.info("[WebSocket推送] 广播考勤结果");

            Map<String, Object> message = new HashMap<>();
            message.put("type", "ATTENDANCE_BROADCAST");
            message.put("data", result);
            message.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 广播到所有订阅者
            messagingTemplate.convertAndSend("/topic/attendance", message);

            log.info("[WebSocket推送] 广播成功");

        } catch (Exception e) {
            log.error("[WebSocket推送] 广播失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 推送考勤异常通知
     *
     * @param userId 用户ID
     * @param errorMessage 错误信息
     */
    public void pushAttendanceError(Long userId, String errorMessage) {
        try {
            log.warn("[WebSocket推送] 推送考勤异常: userId={}, error={}",
                     userId, errorMessage);

            Map<String, Object> message = new HashMap<>();
            message.put("type", "ATTENDANCE_ERROR");
            message.put("userId", userId);
            message.put("errorMessage", errorMessage);
            message.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/attendance",
                message
            );

        } catch (Exception e) {
            log.error("[WebSocket推送] 推送异常失败: userId={}, error={}",
                     userId, e.getMessage(), e);
        }
    }
}
