package net.lab1024.sa.video.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.dto.EdgeAIEventDTO;

/**
 * WebSocket事件推送集成测试
 * <p>
 * 测试边缘AI事件的上报、处理和WebSocket推送流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DisplayName("WebSocket事件推送集成测试")
class WebSocketEventPushIntegrationTest {

    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    private TestRestTemplate restTemplate;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        restTemplate = new TestRestTemplate(restTemplateBuilder);
        baseUrl = "http://localhost:8092/api/v1/video";
    }

    @AfterEach
    void tearDown() {
        restTemplate = null;
    }

    @Test
    @DisplayName("测试上报边缘AI事件 - 成功")
    void testReceiveEdgeEvent_Success() {
        // Given
        EdgeAIEventDTO event = createMockEvent();
        event.setEventType("FACE_DETECTED");
        event.setDeviceId("CAM001");

        // When
        ResponseEntity<ResponseDTO> response = restTemplate.postForEntity(
                baseUrl + "/edge/event",
                event,
                ResponseDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        log.info("[集成测试] 事件上报成功: eventId={}", event.getEventId());
    }

    @Test
    @DisplayName("测试批量上报边缘AI事件 - 成功")
    void testReceiveEdgeEventsBatch_Success() {
        // Given
        EdgeAIEventDTO event1 = createMockEvent();
        event1.setEventType("FACE_DETECTED");
        event1.setDeviceId("CAM001");

        EdgeAIEventDTO event2 = createMockEvent();
        event2.setEventType("PERSON_COUNT");
        event2.setDeviceId("CAM001");

        EdgeAIEventDTO[] events = {event1, event2};

        // When
        ResponseEntity<ResponseDTO> response = restTemplate.postForEntity(
                baseUrl + "/edge/events/batch",
                events,
                ResponseDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        log.info("[集成测试] 批量事件上报成功: count={}", events.length);
    }

    @Test
    @DisplayName("测试获取最近事件 - 成功")
    void testGetRecentEvents_Success() {
        // When
        ResponseEntity<ResponseDTO> response = restTemplate.getForEntity(
                baseUrl + "/websocket/recent-events?count=5",
                ResponseDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        log.info("[集成测试] 获取最近事件成功");
    }

    @Test
    @DisplayName("测试获取设备事件 - 成功")
    void testGetDeviceEvents_Success() {
        // When
        ResponseEntity<ResponseDTO> response = restTemplate.getForEntity(
                baseUrl + "/websocket/device-events?deviceId=CAM001&count=5",
                ResponseDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        log.info("[集成测试] 获取设备事件成功");
    }

    @Test
    @DisplayName("测试获取会话统计 - 成功")
    void testGetSessionStats_Success() {
        // When
        ResponseEntity<ResponseDTO> response = restTemplate.getForEntity(
                baseUrl + "/websocket/session-stats",
                ResponseDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        log.info("[集成测试] 获取会话统计成功: stats={}", response.getBody().getData());
    }

    @Test
    @DisplayName("测试上报事件 - 参数验证失败")
    void testReceiveEdgeEvent_ValidationError() {
        // Given - 事件缺少必填字段
        EdgeAIEventDTO event = new EdgeAIEventDTO();
        event.setEventType("FACE_DETECTED");
        // 缺少deviceId和confidence

        // When
        ResponseEntity<ResponseDTO> response = restTemplate.postForEntity(
                baseUrl + "/edge/event",
                event,
                ResponseDTO.class
        );

        // Then
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
        log.info("[集成测试] 参数验证正确触发错误");
    }

    /**
     * 创建模拟事件
     */
    private EdgeAIEventDTO createMockEvent() {
        return EdgeAIEventDTO.builder()
                .eventId("TEST_" + System.currentTimeMillis())
                .deviceId("TEST_CAM_001")
                .deviceName("测试摄像头")
                .eventType("FACE_DETECTED")
                .eventData(Map.of("faceId", 12345, "bbox", List.of(100, 200, 300, 400)))
                .confidence(new BigDecimal("0.95"))
                .eventTime(LocalDateTime.now())
                .imageUrl("https://example.com/test.jpg")
                .areaId(2001L)
                .areaName("测试区域")
                .build();
    }
}
