package net.lab1024.sa.devicecomm.protocol.handler.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.devicecomm.cache.ProtocolCacheService;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolParseException;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolProcessException;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * ConsumeProtocolHandler单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：消费协议处理器的核心功能
 * - 消息解析（parseMessage）
 * - 消息验证（validateMessage）
 * - 消息处理（processMessage）
 * - 校验和验证（validateChecksum）
 * - 余额查询响应
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeProtocolHandler单元测试")
class ConsumeProtocolHandlerTest {

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ProtocolCacheService cacheService;

    @InjectMocks
    private ConsumeProtocolHandler consumeProtocolHandler;

    private byte[] validMessageBytes;
    private byte[] invalidHeaderBytes;
    private byte[] shortMessageBytes;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        // 消费协议当前采用HTTP文本格式（制表符分隔）
        String validText = "SYS1\tCARD001\t1700000000\t1000\t5000\tREC1\t1\t1\t20250130\t1\tOP1";
        validMessageBytes = validText.getBytes(StandardCharsets.UTF_8);

        // 空白文本 -> parseMessage(String) 视为无效
        invalidHeaderBytes = " ".getBytes(StandardCharsets.UTF_8);

        // 空字节 -> parseMessage(byte[]) 直接视为无效
        shortMessageBytes = new byte[0];

        meterRegistry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(consumeProtocolHandler, "meterRegistry", meterRegistry);
    }

    @Test
    @DisplayName("测试解析有效消息-成功场景")
    void testParseMessage_ValidMessage_Success() throws ProtocolParseException {
        // When
        ProtocolMessage message = consumeProtocolHandler.parseMessage((byte[]) validMessageBytes);

        // Then
        assertNotNull(message);
        assertEquals("CONSUME_ZKTECO_V1.0", message.getProtocolType());
        assertNotNull(message.getMessageType());
        assertNotNull(message.getDeviceCode());
        assertNotNull(message.getData());
        assertEquals("PARSED", message.getStatus());
    }

    @Test
    @DisplayName("测试解析消息-空数据")
    void testParseMessage_NullData_ThrowsException() {
        // When & Then
        assertThrows(ProtocolParseException.class, () -> {
            consumeProtocolHandler.parseMessage((byte[]) null);
        });
    }

    @Test
    @DisplayName("测试解析消息-长度不足")
    void testParseMessage_ShortData_ThrowsException() {
        // When & Then
        assertThrows(ProtocolParseException.class, () -> {
            consumeProtocolHandler.parseMessage((byte[]) shortMessageBytes);
        });
    }

    @Test
    @DisplayName("测试解析消息-无效协议头")
    void testParseMessage_InvalidHeader_ThrowsException() {
        // When & Then
        assertThrows(ProtocolParseException.class, () -> {
            consumeProtocolHandler.parseMessage((byte[]) invalidHeaderBytes);
        });
    }

    @Test
    @DisplayName("测试验证消息-有效消息")
    void testValidateMessage_ValidMessage_ReturnsTrue() throws ProtocolParseException {
        // Given
        ProtocolMessage message = consumeProtocolHandler.parseMessage((byte[]) validMessageBytes);

        // When
        boolean isValid = consumeProtocolHandler.validateMessage(message);

        // Then
        assertTrue(isValid);
        assertEquals("VALIDATED", message.getStatus());
    }

    @Test
    @DisplayName("测试验证消息-空消息")
    void testValidateMessage_NullMessage_ReturnsFalse() {
        // When
        boolean isValid = consumeProtocolHandler.validateMessage(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试处理消费记录消息-成功场景")
    void testProcessMessage_ConsumeRecord_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = consumeProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType("CONSUME_RECORD");
        when(cacheService.getUserIdByCardNumber(eq("CARD001"))).thenReturn(1001L);

        // When
        assertDoesNotThrow(() -> {
            consumeProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(eq("protocol.consume.record"), anyMap());
    }

    @Test
    @DisplayName("测试处理余额查询消息-成功场景")
    void testProcessMessage_BalanceQuery_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = consumeProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType("BALANCE_QUERY");
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1001);
        message.setData(data);

        // Mock网关服务调用 - 返回余额
        @SuppressWarnings("unchecked")
        ResponseDTO<Object> mockResponse = (ResponseDTO<Object>) (ResponseDTO<?>) ResponseDTO.ok(new BigDecimal("100.50"));
        when(gatewayServiceClient.callConsumeService(
                eq("/api/v1/consume/account/balance/user/1001"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Object.class)
        )).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> {
            consumeProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(gatewayServiceClient, times(1)).callConsumeService(
                eq("/api/v1/consume/account/balance/user/1001"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Object.class)
        );
    }

    @Test
    @DisplayName("测试处理设备状态消息-成功场景")
    void testProcessMessage_DeviceStatus_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = new ProtocolMessage();
        message.setProtocolType(consumeProtocolHandler.getProtocolType());
        message.setMessageType("DEVICE_STATUS");
        
        Map<String, Object> data = new HashMap<>();
        data.put("deviceId", 1L);
        data.put("deviceStatus", "ONLINE");
        data.put("onlineStatus", "ONLINE");
        message.setData(data);
        message.setDeviceCode("DEV001");

        // When
        assertDoesNotThrow(() -> {
            consumeProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(eq("protocol.device.status"), anyMap());
    }

    @Test
    @DisplayName("测试处理消息-验证失败")
    void testProcessMessage_ValidationFailed_ThrowsException() throws ProtocolParseException {
        // Given
        ProtocolMessage message = consumeProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType(null); // 使验证失败

        // When & Then
        assertThrows(ProtocolProcessException.class, () -> {
            consumeProtocolHandler.processMessage(message, 1L);
        });
    }

    @Test
    @DisplayName("测试获取协议类型")
    void testGetProtocolType() {
        // When
        String protocolType = consumeProtocolHandler.getProtocolType();

        // Then
        assertEquals("CONSUME_ZKTECO_V1.0", protocolType);
    }

    @Test
    @DisplayName("测试获取厂商")
    void testGetManufacturer() {
        // When
        String manufacturer = consumeProtocolHandler.getManufacturer();

        // Then
        assertNotNull(manufacturer);
        assertEquals("中控智慧", manufacturer);
    }

    @Test
    @DisplayName("测试获取版本")
    void testGetVersion() {
        // When
        String version = consumeProtocolHandler.getVersion();

        // Then
        assertNotNull(version);
        assertEquals("V1.0", version);
    }
}
