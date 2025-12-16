package net.lab1024.sa.devicecomm.protocol.handler.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.test.util.ReflectionTestUtils;

import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolParseException;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolProcessException;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * AccessProtocolHandler单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：门禁协议处理器的核心功能
 * - 消息解析（parseMessage）
 * - 消息验证（validateMessage）
 * - 消息处理（processMessage）
 * - 校验和验证（validateChecksum）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessProtocolHandler单元测试")
class AccessProtocolHandlerTest {

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AccessProtocolHandler accessProtocolHandler;

    private byte[] validMessageBytes;
    private byte[] invalidHeaderBytes;
    private byte[] shortMessageBytes;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        // 门禁协议当前采用HTTP文本格式（key=value，制表符分隔）
        String validText = "event=1\tpin=1001\ttime=2025-01-30 10:00:00\teventaddr=1\tinoutstatus=0\tverifytype=3";
        validMessageBytes = validText.getBytes(StandardCharsets.UTF_8);

        // 空白文本 -> parseMessage(String) 视为无效
        invalidHeaderBytes = " ".getBytes(StandardCharsets.UTF_8);

        // 空字节 -> parseMessage(byte[]) 直接视为无效
        shortMessageBytes = new byte[0];

        meterRegistry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(accessProtocolHandler, "meterRegistry", meterRegistry);
    }

    @Test
    @DisplayName("测试解析有效消息-成功场景")
    void testParseMessage_ValidMessage_Success() throws ProtocolParseException {
        // When
        ProtocolMessage message = accessProtocolHandler.parseMessage((byte[]) validMessageBytes);

        // Then
        assertNotNull(message);
        assertEquals("ACCESS_ENTROPY_V4.8", message.getProtocolType());
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
            accessProtocolHandler.parseMessage((byte[]) null);
        });
    }

    @Test
    @DisplayName("测试解析消息-长度不足")
    void testParseMessage_ShortData_ThrowsException() {
        // When & Then
        assertThrows(ProtocolParseException.class, () -> {
            accessProtocolHandler.parseMessage((byte[]) shortMessageBytes);
        });
    }

    @Test
    @DisplayName("测试解析消息-无效协议头")
    void testParseMessage_InvalidHeader_ThrowsException() {
        // When & Then
        assertThrows(ProtocolParseException.class, () -> {
            accessProtocolHandler.parseMessage((byte[]) invalidHeaderBytes);
        });
    }

    @Test
    @DisplayName("测试验证消息-有效消息")
    void testValidateMessage_ValidMessage_ReturnsTrue() throws ProtocolParseException {
        // Given
        ProtocolMessage message = accessProtocolHandler.parseMessage((byte[]) validMessageBytes);

        // When
        boolean isValid = accessProtocolHandler.validateMessage(message);

        // Then
        assertTrue(isValid);
        assertEquals("VALIDATED", message.getStatus());
    }

    @Test
    @DisplayName("测试验证消息-空消息")
    void testValidateMessage_NullMessage_ReturnsFalse() {
        // When
        boolean isValid = accessProtocolHandler.validateMessage(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试验证消息-缺少消息类型")
    void testValidateMessage_MissingMessageType_ReturnsFalse() throws ProtocolParseException {
        // Given
        ProtocolMessage message = accessProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType(null);

        // When
        boolean isValid = accessProtocolHandler.validateMessage(message);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试验证消息-缺少设备编号")
    void testValidateMessage_MissingDeviceCode_ReturnsFalse() throws ProtocolParseException {
        // Given
        ProtocolMessage message = accessProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setDeviceCode(null);

        // When
        boolean isValid = accessProtocolHandler.validateMessage(message);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试处理门禁记录消息-成功场景")
    void testProcessMessage_AccessRecord_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = accessProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType("ACCESS_RECORD");

        // When
        assertDoesNotThrow(() -> {
            accessProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(eq("protocol.access.record"), anyMap());
    }

    @Test
    @DisplayName("测试处理设备状态消息-成功场景")
    void testProcessMessage_DeviceStatus_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = new ProtocolMessage();
        message.setProtocolType(accessProtocolHandler.getProtocolType());
        message.setMessageType("DEVICE_STATUS");

        Map<String, Object> data = new HashMap<>();
        data.put("statusCode", 1);
        message.setData(data);
        message.setDeviceCode("1");

        // When
        assertDoesNotThrow(() -> {
            accessProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(eq("protocol.device.status"), anyMap());
    }

    @Test
    @DisplayName("测试处理报警事件消息-成功场景")
    void testProcessMessage_AlarmEvent_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = accessProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType("ALARM_EVENT");
        
        Map<String, Object> data = new HashMap<>();
        data.put("alarmType", "FORCE_OPEN");
        data.put("alarmTime", (int) (System.currentTimeMillis() / 1000));
        message.setData(data);

        // When
        assertDoesNotThrow(() -> {
            accessProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(eq("protocol.alarm.event"), anyMap());
    }

    @Test
    @DisplayName("测试处理消息-验证失败")
    void testProcessMessage_ValidationFailed_ThrowsException() throws ProtocolParseException {
        // Given
        ProtocolMessage message = accessProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType(null); // 使验证失败

        // When & Then
        assertThrows(ProtocolProcessException.class, () -> {
            accessProtocolHandler.processMessage(message, 1L);
        });
    }

    @Test
    @DisplayName("测试获取协议类型")
    void testGetProtocolType() {
        // When
        String protocolType = accessProtocolHandler.getProtocolType();

        // Then
        assertEquals("ACCESS_ENTROPY_V4.8", protocolType);
    }

    @Test
    @DisplayName("测试获取厂商")
    void testGetManufacturer() {
        // When
        String manufacturer = accessProtocolHandler.getManufacturer();

        // Then
        assertNotNull(manufacturer);
        assertEquals("熵基科技", manufacturer);
    }

    @Test
    @DisplayName("测试获取版本")
    void testGetVersion() {
        // When
        String version = accessProtocolHandler.getVersion();

        // Then
        assertNotNull(version);
        assertEquals("V4.8", version);
    }
}
