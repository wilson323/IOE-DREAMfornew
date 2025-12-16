package net.lab1024.sa.oa.workflow.websocket;

import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;

/**
 * 工作流WebSocket控制器
 * 处理WebSocket消息和实时通知
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 * @version 3.0.0
 */
@Slf4j
@Controller
@SuppressWarnings("null")
public class WorkflowWebSocketController {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 处理心跳消息
     *
     * @param message 心跳消息
     * @return 响应消息
     */
    @Observed(name = "websocket.handleHeartbeat", contextualName = "websocket-heartbeat")
    @MessageMapping("/heartbeat")
    @SendTo("/topic/heartbeat")
    public Map<String, Object> handleHeartbeat(Map<String, Object> message) {
        log.debug("收到心跳消息: {}", message);
        return Map.of(
            "type", "PONG",
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 订阅时发送欢迎消息
     *
     * @return 欢迎消息
     */
    @Observed(name = "websocket.handleSubscribe", contextualName = "websocket-subscribe")
    @SubscribeMapping("/topic/workflow/notifications")
    public Map<String, Object> handleSubscribe() {
        log.info("客户端订阅工作流通知");
        return Map.of(
            "type", "SUBSCRIBE_SUCCESS",
            "message", "已成功订阅工作流通知",
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 发送新任务通知
     *
     * @param userId 用户ID
     * @param taskData 任务数据
     */
    @Observed(name = "websocket.sendNewTaskNotification", contextualName = "websocket-send-new-task-notification")
    public void sendNewTaskNotification(Long userId, Map<String, Object> taskData) {
        try {
            Map<String, Object> message = Map.of(
                "type", "NEW_TASK",
                "data", taskData,
                "timestamp", System.currentTimeMillis()
            );

            // 发送点对点消息给指定用户
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/workflow/tasks",
                message
            );

            log.info("发送新任务通知，用户ID: {}, 任务ID: {}", userId, taskData.get("taskId"));
        } catch (ParamException | BusinessException e) {
            log.warn("[WebSocket] 发送新任务通知失败: userId={}, error={}", userId, e.getMessage());
        } catch (SystemException e) {
            log.error("[WebSocket] 发送新任务通知系统异常: userId={}, error={}", userId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("[WebSocket] 发送新任务通知失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 发送任务状态变更通知
     *
     * @param userId 用户ID
     * @param taskData 任务数据
     */
    @Observed(name = "websocket.sendTaskStatusChangedNotification", contextualName = "websocket-send-task-status-changed")
    public void sendTaskStatusChangedNotification(Long userId, Map<String, Object> taskData) {
        try {
            Map<String, Object> message = Map.of(
                "type", "TASK_STATUS_CHANGED",
                "data", taskData,
                "timestamp", System.currentTimeMillis()
            );

            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/workflow/tasks",
                message
            );

            log.info("发送任务状态变更通知，用户ID: {}, 任务ID: {}", userId, taskData.get("taskId"));
        } catch (ParamException | BusinessException e) {
            log.warn("[WebSocket] 发送任务状态变更通知失败: userId={}, error={}", userId, e.getMessage());
        } catch (SystemException e) {
            log.error("[WebSocket] 发送任务状态变更通知系统异常: userId={}, error={}", userId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("[WebSocket] 发送任务状态变更通知失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 发送流程实例状态变更通知
     *
     * @param userId 用户ID
     * @param instanceData 流程实例数据
     */
    @Observed(name = "websocket.sendInstanceStatusChangedNotification", contextualName = "websocket-send-instance-status-changed")
    public void sendInstanceStatusChangedNotification(Long userId, Map<String, Object> instanceData) {
        try {
            Map<String, Object> message = Map.of(
                "type", "INSTANCE_STATUS_CHANGED",
                "data", instanceData,
                "timestamp", System.currentTimeMillis()
            );

            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/workflow/instances",
                message
            );

            log.info("发送流程实例状态变更通知，用户ID: {}, 实例ID: {}", userId, instanceData.get("instanceId"));
        } catch (ParamException | BusinessException e) {
            log.warn("[WebSocket] 发送流程实例状态变更通知失败: userId={}, error={}", userId, e.getMessage());
        } catch (SystemException e) {
            log.error("[WebSocket] 发送流程实例状态变更通知系统异常: userId={}, error={}", userId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("[WebSocket] 发送流程实例状态变更通知失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 广播消息（发送给所有订阅的客户端）
     *
     * @param message 消息内容
     */
    @Observed(name = "websocket.broadcastMessage", contextualName = "websocket-broadcast-message")
    public void broadcastMessage(Map<String, Object> message) {
        try {
            messagingTemplate.convertAndSend("/topic/workflow/broadcast", message);
            log.debug("广播工作流消息: {}", message.get("type"));
        } catch (ParamException | BusinessException e) {
            log.warn("[WebSocket] 广播工作流消息失败: error={}", e.getMessage());
        } catch (SystemException e) {
            log.error("[WebSocket] 广播工作流消息系统异常: error={}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("[WebSocket] 广播工作流消息失败: error={}", e.getMessage(), e);
        }
    }
}





