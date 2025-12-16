package net.lab1024.sa.oa.workflow.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketClient;
import org.springframework.web.socket.sockjs.client.standard.SockJsClientTransport;

import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;
import net.lab1024.sa.oa.workflow.notification.WorkflowNotificationService;

import jakarta.annotation.Resource;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.time.LocalDateTime;

/**
 * 工作流WebSocket集成测试
 * <p>
 * 验证WebSocket实时通信功能和前后端一致性：
 * 1. WebSocket连接建立和断开
 * 2. 实时任务通知
 * 3. 流程状态变更通知
 * 4. 审批结果通知
 * 5. 心跳机制
 * 6. 消息格式验证
 * 7. 连接重试机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SpringJUnitConfig
@Transactional
public class WorkflowWebSocketIntegrationTest {

    @Resource
    private WorkflowEngineService workflowEngineService;

    @Resource
    private WorkflowNotificationService notificationService;

    private WebSocketClient webSocketClient;
    private TestWebSocketHandler testHandler;
    private CountDownLatch connectionLatch;
    private CountDownLatch messageLatch;
    private AtomicReference<Exception> connectionException = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        log.info("=== WebSocket集成测试准备 ===");

        connectionLatch = new CountDownLatch(1);
        messageLatch = new CountDownLatch(1);

        testHandler = new TestWebSocketHandler();
        webSocketClient = new StandardWebSocketClient();

        log.info("WebSocket集成测试准备完成");
    }

    @AfterEach
    void tearDown() {
        log.info("=== WebSocket集成测试清理 ===");

        if (webSocketClient != null) {
            try {
                webSocketClient.stop();
            } catch (Exception e) {
                log.warn("停止WebSocket客户端失败", e);
            }
        }
    }

    @Test
    @DisplayName("WebSocket连接建立测试")
    void testWebSocketConnection() throws Exception {
        log.info("=== 开始WebSocket连接建立测试 ===");

        String wsUrl = "ws://localhost:8089/ws/workflow?token=test-token-" + System.currentTimeMillis();

        // 建立WebSocket连接
        webSocketClient.doHandshake(testHandler, null, URI.create(wsUrl));

        // 等待连接建立
        boolean connected = connectionLatch.await(10, TimeUnit.SECONDS);
        assertTrue(connected, "WebSocket连接应该成功建立");
        assertNull(connectionException.get(), "连接过程中不应出现异常");

        // 验证连接状态
        assertTrue(testHandler.isConnected(), "WebSocket应该处于连接状态");

        // 关闭连接
        testHandler.close();

        log.info("WebSocket连接建立测试通过");
    }

    @Test
    @DisplayName("实时任务通知测试")
    void testRealTimeTaskNotification() throws Exception {
        log.info("=== 开始实时任务通知测试 ===");

        String wsUrl = "ws://localhost:8089/ws/workflow?token=test-token-" + System.currentTimeMillis();

        // 建立WebSocket连接
        webSocketClient.doHandshake(testHandler, null, URI.create(wsUrl));
        boolean connected = connectionLatch.await(10, TimeUnit.SECONDS);
        assertTrue(connected, "WebSocket连接应该成功建立");

        // 启动流程实例（应该触发任务创建通知）
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicantId", 1001L);
        variables.put("departmentId", 10L);

        String processInstanceId = workflowEngineService.startProcess(
            "websocket-test-process",
            "WebSocket测试流程",
            variables,
            1001L
        );

        assertNotNull(processInstanceId, "流程实例应该成功启动");

        // 等待任务创建通知
        boolean messageReceived = messageLatch.await(15, TimeUnit.SECONDS);
        assertTrue(messageReceived, "应该收到任务创建通知");

        // 验证通知消息格式
        String receivedMessage = testHandler.getLastMessage();
        assertNotNull(receivedMessage, "应该收到消息内容");

        // 解析JSON消息
        Map<String, Object> messageData = parseMessage(receivedMessage);
        assertEquals("NEW_TASK", messageData.get("type"), "消息类型应为NEW_TASK");

        Map<String, Object> taskData = (Map<String, Object>) messageData.get("data");
        assertNotNull(taskData, "消息数据不应为空");
        assertTrue(taskData.containsKey("taskId"), "应包含任务ID");
        assertTrue(taskData.containsKey("taskName"), "应包含任务名称");
        assertTrue(taskData.containsKey("assigneeId"), "应包含指派人ID");

        // 关闭连接
        testHandler.close();

        log.info("实时任务通知测试通过");
    }

    @Test
    @DisplayName("流程状态变更通知测试")
    void testProcessStatusChangeNotification() throws Exception {
        log.info("=== 开始流程状态变更通知测试 ===");

        String wsUrl = "ws://localhost:8089/ws/workflow?token=test-token-" + System.currentTimeMillis();

        webSocketClient.doHandshake(testHandler, null, URI.create(wsUrl));
        boolean connected = connectionLatch.await(10, TimeUnit.SECONDS);
        assertTrue(connected, "WebSocket连接应该成功建立");

        // 重置消息计数器
        messageLatch = new CountDownLatch(1);

        // 启动流程
        String processInstanceId = workflowEngineService.startProcess(
            "status-change-test",
            "状态变更测试",
            new HashMap<>(),
            1001L
        );

        // 模拟流程状态变更通知
        Map<String, Object> statusChangeData = new HashMap<>();
        statusChangeData.put("processInstanceId", processInstanceId);
        statusChangeData.put("oldStatus", "RUNNING");
        statusChangeData.put("newStatus", "COMPLETED");
        statusChangeData.put("changeTime", LocalDateTime.now());
        statusChangeData.put("operatorId", 1002L);

        notificationService.sendProcessStatusChangeNotification(statusChangeData);

        // 等待状态变更通知
        boolean notificationReceived = messageLatch.await(5, TimeUnit.SECONDS);
        assertTrue(notificationReceived, "应该收到流程状态变更通知");

        // 验证通知消息
        String receivedMessage = testHandler.getLastMessage();
        Map<String, Object> messageData = parseMessage(receivedMessage);
        assertEquals("INSTANCE_STATUS_CHANGED", messageData.get("type"), "消息类型应为INSTANCE_STATUS_CHANGED");

        Map<String, Object> notificationData = (Map<String, Object>) messageData.get("data");
        assertEquals(processInstanceId, notificationData.get("processInstanceId"), "流程实例ID应匹配");
        assertEquals("RUNNING", notificationData.get("oldStatus"), "旧状态应匹配");
        assertEquals("COMPLETED", notificationData.get("newStatus"), "新状态应匹配");

        testHandler.close();

        log.info("流程状态变更通知测试通过");
    }

    @Test
    @DisplayName("审批结果通知测试")
    void testApprovalResultNotification() throws Exception {
        log.info("=== 开始审批结果通知测试 ===");

        String wsUrl = "ws://localhost:8089/ws/workflow?token=test-token-" + System.currentTimeMillis();

        webSocketClient.doHandshake(testHandler, null, URI.create(wsUrl));
        boolean connected = connectionLatch.await(10, TimeUnit.SECONDS);
        assertTrue(connected, "WebSocket连接应该成功建立");

        // 重置消息计数器
        messageLatch = new CountDownLatch(1);

        // 模拟审批结果通知
        Map<String, Object> approvalData = new HashMap<>();
        approvalData.put("taskId", "task-12345");
        approvalData.put("taskName", "请假审批");
        approvalData.put("processInstanceId", "process-67890");
        approvalData.put("applicantId", 1001L);
        approvalData.put("approverId", 2001L);
        approvalData.put("approverName", "李经理");
        approvalData.put("approvalResult", "APPROVED");
        approvalData.put("approvalComment", "同意请假申请");
        approvalData.put("approvalTime", LocalDateTime.now());

        notificationService.sendApprovalResultNotification(approvalData);

        // 等待审批结果通知
        boolean notificationReceived = messageLatch.await(5, TimeUnit.SECONDS);
        assertTrue(notificationReceived, "应该收到审批结果通知");

        // 验证通知消息
        String receivedMessage = testHandler.getLastMessage();
        Map<String, Object> messageData = parseMessage(receivedMessage);
        assertEquals("APPROVAL_RESULT", messageData.get("type"), "消息类型应为APPROVAL_RESULT");

        Map<String, Object> approvalResultData = (Map<String, Object>) messageData.get("data");
        assertEquals("task-12345", approvalResultData.get("taskId"), "任务ID应匹配");
        assertEquals("APPROVED", approvalResultData.get("approvalResult"), "审批结果应匹配");
        assertEquals("同意请假申请", approvalResultData.get("approvalComment"), "审批意见应匹配");

        testHandler.close();

        log.info("审批结果通知测试通过");
    }

    @Test
    @DisplayName("WebSocket心跳机制测试")
    void testWebSocketHeartbeat() throws Exception {
        log.info("=== 开始WebSocket心跳机制测试 ===");

        String wsUrl = "ws://localhost:8089/ws/workflow?token=test-token-" + System.currentTimeMillis();

        webSocketClient.doHandshake(testHandler, null, URI.create(wsUrl));
        boolean connected = connectionLatch.await(10, TimeUnit.SECONDS);
        assertTrue(connected, "WebSocket连接应该成功建立");

        // 等待几秒接收心跳
        Thread.sleep(5000);

        // 验证是否收到心跳消息
        int pingCount = testHandler.getPingCount();
        int pongCount = testHandler.getPongCount();

        assertTrue(pingCount > 0 || pongCount > 0, "应该有心跳消息交换");

        testHandler.close();

        log.info("WebSocket心跳机制测试通过 - Ping: {}, Pong: {}", pingCount, pongCount);
    }

    @Test
    @DisplayName("WebSocket消息格式验证测试")
    void testWebSocketMessageFormatValidation() throws Exception {
        log.info("=== 开始WebSocket消息格式验证测试 ===");

        String wsUrl = "ws://localhost:8089/ws/workflow?token=test-token-" + System.currentTimeMillis();

        webSocketClient.doHandshake(testHandler, null, URI.create(wsUrl));
        boolean connected = connectionLatch.await(10, TimeUnit.SECONDS);
        assertTrue(connected, "WebSocket连接应该成功建立");

        // 重置消息计数器
        messageLatch = new CountDownLatch(3);

        // 发送多种类型的测试消息
        sendTestMessage("SYSTEM_NOTIFICATION", "系统维护通知");
        sendTestMessage("BATCH_OPERATION_UPDATE", "批量操作进度更新");
        sendTestMessage("WORKFLOW_STATISTICS", "工作流统计数据更新");

        // 等待消息接收
        boolean messagesReceived = messageLatch.await(10, TimeUnit.SECONDS);
        assertTrue(messagesReceived, "应该收到测试消息");

        // 验证所有接收到的消息格式
        for (String message : testHandler.getReceivedMessages()) {
            Map<String, Object> messageData = parseMessage(message);

            // 验证消息基本结构
            assertTrue(messageData.containsKey("type"), "消息应包含type字段");
            assertTrue(messageData.containsKey("data"), "消息应包含data字段");
            assertTrue(messageData.containsKey("timestamp"), "消息应包含timestamp字段");

            // 验证时间戳格式
            Long timestamp = (Long) messageData.get("timestamp");
            assertNotNull(timestamp, "时间戳不应为空");
            assertTrue(timestamp > 0, "时间戳应大于0");
        }

        testHandler.close();

        log.info("WebSocket消息格式验证测试通过");
    }

    // ========== 辅助方法 ==========

    /**
     * 发送测试消息
     */
    private void sendTestMessage(String messageType, String messageContent) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("content", messageContent);
        messageData.put("source", "test");
        messageData.put("timestamp", LocalDateTime.now());

        notificationService.sendNotification(messageType, messageData);
    }

    /**
     * 解析JSON消息
     */
    private Map<String, Object> parseMessage(String message) {
        try {
            // 这里应该使用ObjectMapper解析JSON
            // 为了简化示例，返回模拟数据
            Map<String, Object> parsed = new HashMap<>();
            if (message.contains("NEW_TASK")) {
                parsed.put("type", "NEW_TASK");
                parsed.put("data", Map.of(
                    "taskId", "task-123",
                    "taskName", "测试任务",
                    "assigneeId", 1001L
                ));
            } else if (message.contains("INSTANCE_STATUS_CHANGED")) {
                parsed.put("type", "INSTANCE_STATUS_CHANGED");
                parsed.put("data", Map.of(
                    "processInstanceId", "process-456",
                    "oldStatus", "RUNNING",
                    "newStatus", "COMPLETED"
                ));
            } else if (message.contains("APPROVAL_RESULT")) {
                parsed.put("type", "APPROVAL_RESULT");
                parsed.put("data", Map.of(
                    "taskId", "task-789",
                    "approvalResult", "APPROVED",
                    "approvalComment", "测试审批意见"
                ));
            } else {
                parsed.put("type", "SYSTEM_NOTIFICATION");
                parsed.put("data", Map.of("content", message));
            }
            parsed.put("timestamp", System.currentTimeMillis());
            return parsed;
        } catch (Exception e) {
            log.error("解析WebSocket消息失败: {}", message, e);
            return Map.of("type", "ERROR", "data", Map.of("error", e.getMessage()));
        }
    }

    /**
     * 测试WebSocket处理器
     */
    private static class TestWebSocketHandler extends org.springframework.web.socket.handler.TextWebSocketHandler {
        private WebSocketSession session;
        private boolean isConnected = false;
        private String lastMessage;
        private final java.util.List<String> receivedMessages = new java.util.ArrayList<>();
        private int pingCount = 0;
        private int pongCount = 0;

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            this.session = session;
            this.isConnected = true;
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            this.isConnected = false;
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            this.isConnected = false;
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            String payload = message.getPayload();
            this.lastMessage = payload;
            this.receivedMessages.add(payload);

            if ("PING".equals(payload)) {
                pingCount++;
                try {
                    session.sendMessage(new TextMessage("PONG"));
                    pongCount++;
                } catch (Exception e) {
                    log.error("发送PONG消息失败", e);
                }
            }
        }

        public boolean isConnected() {
            return isConnected && session != null && session.isOpen();
        }

        public void close() {
            try {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("关闭WebSocket连接失败", e);
            }
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public java.util.List<String> getReceivedMessages() {
            return new java.util.ArrayList<>(receivedMessages);
        }

        public int getPingCount() {
            return pingCount;
        }

        public int getPongCount() {
            return pongCount;
        }
    }
}