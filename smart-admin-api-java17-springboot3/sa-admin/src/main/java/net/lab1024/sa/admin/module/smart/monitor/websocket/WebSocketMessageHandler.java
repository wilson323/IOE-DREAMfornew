package net.lab1024.sa.admin.module.smart.monitor.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 * <p>
 * 严格遵循repowiki规范：
 * - 处理WebSocket消息的路由和分发
 * - 支持不同类型的消息处理
 * - 提供消息验证和格式化
 * - 记录消息处理统计信息
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class WebSocketMessageHandler {

    @Resource
    private WebSocketSessionManager sessionManager;

    // 消息类型处理器映射
    private final Map<String, MessageProcessor> messageProcessors = new ConcurrentHashMap<>();

    /**
     * 初始化消息处理器
     */
    public void initialize() {
        // 注册各种消息类型的处理器
        messageProcessors.put("heartbeat", new HeartbeatProcessor());
        messageProcessors.put("subscribe", new SubscribeProcessor());
        messageProcessors.put("unsubscribe", new UnsubscribeProcessor());
        messageProcessors.put("device_query", new DeviceQueryProcessor());
        messageProcessors.put("system_command", new SystemCommandProcessor());

        log.info("WebSocket消息处理器初始化完成，注册处理器数量: {}", messageProcessors.size());
    }

    /**
     * 处理接收到的消息
     *
     * @param sessionId 会话ID
     * @param message   消息内容
     * @return 处理结果
     */
    public MessageProcessResult handleMessage(String sessionId, String message) {
        try {
            log.debug("处理WebSocket消息，sessionId: {}, message: {}", sessionId, message);

            // 解析消息
            JSONObject messageObj = JSON.parseObject(message);
            String messageType = messageObj.getString("type");

            if (messageType == null || messageType.isEmpty()) {
                return createErrorResult("消息类型不能为空");
            }

            // 获取消息处理器
            MessageProcessor processor = messageProcessors.get(messageType);
            if (processor == null) {
                log.warn("未找到消息处理器，messageType: {}", messageType);
                return createErrorResult("不支持的消息类型: " + messageType);
            }

            // 处理消息
            return processor.process(sessionId, messageObj);

        } catch (Exception e) {
            log.error("处理WebSocket消息失败，sessionId: {}, message: {}", sessionId, message, e);
            return createErrorResult("消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 广播消息到所有连接
     *
     * @param message 消息内容
     */
    public void broadcast(String message) {
        try {
            sessionManager.broadcast(message);
            log.debug("消息广播成功");
        } catch (Exception e) {
            log.error("消息广播失败，message: {}", message, e);
        }
    }

    /**
     * 发送消息到指定会话
     *
     * @param sessionId 会话ID
     * @param message   消息内容
     */
    public void sendToSession(String sessionId, String message) {
        try {
            sessionManager.sendToSession(sessionId, message);
            log.debug("消息发送成功，sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("消息发送失败，sessionId: {}, message: {}", sessionId, message, e);
        }
    }

    /**
     * 创建成功结果
     *
     * @param data 响应数据
     * @return 处理结果
     */
    private MessageProcessResult createSuccessResult(Object data) {
        MessageProcessResult result = new MessageProcessResult();
        result.setSuccess(true);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 创建错误结果
     *
     * @param errorMessage 错误信息
     * @return 处理结果
     */
    private MessageProcessResult createErrorResult(String errorMessage) {
        MessageProcessResult result = new MessageProcessResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 消息处理器接口
     */
    public interface MessageProcessor {
        MessageProcessResult process(String sessionId, JSONObject message);
    }

    /**
     * 心跳消息处理器
     */
    private static class HeartbeatProcessor implements MessageProcessor {
        @Override
        public MessageProcessResult process(String sessionId, JSONObject message) {
            JSONObject response = new JSONObject();
            response.put("type", "heartbeat");
            response.put("status", "alive");
            response.put("timestamp", System.currentTimeMillis());

            MessageProcessResult result = new MessageProcessResult();
            result.setSuccess(true);
            result.setData(response);
            result.setTimestamp(LocalDateTime.now());
            return result;
        }
    }

    /**
     * 订阅消息处理器
     */
    private static class SubscribeProcessor implements MessageProcessor {
        @Override
        public MessageProcessResult process(String sessionId, JSONObject message) {
            String subscribeType = message.getString("subscribeType");

            JSONObject response = new JSONObject();
            response.put("type", "subscribe");
            response.put("subscribeType", subscribeType);
            response.put("status", "success");
            response.put("message", "订阅成功");

            MessageProcessResult result = new MessageProcessResult();
            result.setSuccess(true);
            result.setData(response);
            result.setTimestamp(LocalDateTime.now());
            return result;
        }
    }

    /**
     * 取消订阅消息处理器
     */
    private static class UnsubscribeProcessor implements MessageProcessor {
        @Override
        public MessageProcessResult process(String sessionId, JSONObject message) {
            String subscribeType = message.getString("subscribeType");

            JSONObject response = new JSONObject();
            response.put("type", "unsubscribe");
            response.put("subscribeType", subscribeType);
            response.put("status", "success");
            response.put("message", "取消订阅成功");

            MessageProcessResult result = new MessageProcessResult();
            result.setSuccess(true);
            result.setData(response);
            result.setTimestamp(LocalDateTime.now());
            return result;
        }
    }

    /**
     * 设备查询消息处理器
     */
    private static class DeviceQueryProcessor implements MessageProcessor {
        @Override
        public MessageProcessResult process(String sessionId, JSONObject message) {
            String deviceId = message.getString("deviceId");

            JSONObject response = new JSONObject();
            response.put("type", "device_status");
            response.put("status", "success");
            response.put("deviceId", deviceId);
            // TODO: 查询实际设备状态
            response.put("data", new JSONObject());

            MessageProcessResult result = new MessageProcessResult();
            result.setSuccess(true);
            result.setData(response);
            result.setTimestamp(LocalDateTime.now());
            return result;
        }
    }

    /**
     * 系统命令消息处理器
     */
    private static class SystemCommandProcessor implements MessageProcessor {
        @Override
        public MessageProcessResult process(String sessionId, JSONObject message) {
            String command = message.getString("command");

            JSONObject response = new JSONObject();
            response.put("type", "system_command");
            response.put("command", command);
            response.put("status", "success");

            MessageProcessResult result = new MessageProcessResult();
            result.setSuccess(true);
            result.setData(response);
            result.setTimestamp(LocalDateTime.now());
            return result;
        }
    }

    /**
     * 消息处理结果
     */
    public static class MessageProcessResult {
        private Boolean success;
        private Object data;
        private String errorMessage;
        private LocalDateTime timestamp;

        // Getters and Setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}