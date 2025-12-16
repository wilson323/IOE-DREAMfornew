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
 * AttendanceProtocolHandler单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：考勤协议处理器的核心功能
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
@DisplayName("AttendanceProtocolHandler单元测试")
class AttendanceProtocolHandlerTest {

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AttendanceProtocolHandler attendanceProtocolHandler;

    private byte[] validMessageBytes;
    private byte[] invalidHeaderBytes;
    private byte[] shortMessageBytes;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        // 考勤协议当前采用HTTP文本格式（制表符分隔）
        String validText = "1001\t2025-01-30 10:00:00\t0\t3\t0";
        validMessageBytes = validText.getBytes(StandardCharsets.UTF_8);

        // 空白文本 -> parseMessage(String) 视为无效
        invalidHeaderBytes = " ".getBytes(StandardCharsets.UTF_8);

        // 空字节 -> parseMessage(byte[]) 直接视为无效
        shortMessageBytes = new byte[0];

        meterRegistry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(attendanceProtocolHandler, "meterRegistry", meterRegistry);
    }

    @Test
    @DisplayName("测试解析有效消息-成功场景")
    void testParseMessage_ValidMessage_Success() throws ProtocolParseException {
        // When
        ProtocolMessage message = attendanceProtocolHandler.parseMessage((byte[]) validMessageBytes);

        // Then
        assertNotNull(message);
        assertEquals("ATTENDANCE_ENTROPY_V4.0", message.getProtocolType());
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
            attendanceProtocolHandler.parseMessage((byte[]) null);
        });
    }

    @Test
    @DisplayName("测试解析消息-长度不足")
    void testParseMessage_ShortData_ThrowsException() {
        // When & Then
        assertThrows(ProtocolParseException.class, () -> {
            attendanceProtocolHandler.parseMessage((byte[]) shortMessageBytes);
        });
    }

    @Test
    @DisplayName("测试解析消息-无效协议头")
    void testParseMessage_InvalidHeader_ThrowsException() {
        // When & Then
        assertThrows(ProtocolParseException.class, () -> {
            attendanceProtocolHandler.parseMessage((byte[]) invalidHeaderBytes);
        });
    }

    @Test
    @DisplayName("测试验证消息-有效消息")
    void testValidateMessage_ValidMessage_ReturnsTrue() throws ProtocolParseException {
        // Given
        ProtocolMessage message = attendanceProtocolHandler.parseMessage((byte[]) validMessageBytes);

        // When
        boolean isValid = attendanceProtocolHandler.validateMessage(message);

        // Then
        assertTrue(isValid);
        assertEquals("VALIDATED", message.getStatus());
    }

    @Test
    @DisplayName("测试验证消息-空消息")
    void testValidateMessage_NullMessage_ReturnsFalse() {
        // When
        boolean isValid = attendanceProtocolHandler.validateMessage(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试处理考勤记录消息-成功场景")
    void testProcessMessage_AttendanceRecord_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = attendanceProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType("ATTENDANCE_RECORD");

        // When
        assertDoesNotThrow(() -> {
            attendanceProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(eq("protocol.attendance.record"), anyMap());
    }

    @Test
    @DisplayName("测试处理设备状态消息-成功场景")
    void testProcessMessage_DeviceStatus_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = new ProtocolMessage();
        message.setProtocolType(attendanceProtocolHandler.getProtocolType());
        message.setMessageType("DEVICE_STATUS");

        Map<String, Object> data = new HashMap<>();
        data.put("statusCode", 1);
        message.setData(data);
        message.setDeviceCode("1001");

        // When
        assertDoesNotThrow(() -> {
            attendanceProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(eq("protocol.device.status"), anyMap());
    }

    @Test
    @DisplayName("测试处理消息-验证失败")
    void testProcessMessage_ValidationFailed_ThrowsException() throws ProtocolParseException {
        // Given
        ProtocolMessage message = attendanceProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType(null); // 使验证失败

        // When & Then
        assertThrows(ProtocolProcessException.class, () -> {
            attendanceProtocolHandler.processMessage(message, 1L);
        });
    }

    @Test
    @DisplayName("测试获取协议类型")
    void testGetProtocolType() {
        // When
        String protocolType = attendanceProtocolHandler.getProtocolType();

        // Then
        assertEquals("ATTENDANCE_ENTROPY_V4.0", protocolType);
    }

    @Test
    @DisplayName("测试获取厂商")
    void testGetManufacturer() {
        // When
        String manufacturer = attendanceProtocolHandler.getManufacturer();

        // Then
        assertNotNull(manufacturer);
        assertEquals("熵基科技", manufacturer);
    }

    @Test
    @DisplayName("测试获取版本")
    void testGetVersion() {
        // When
        String version = attendanceProtocolHandler.getVersion();

        // Then
        assertNotNull(version);
        assertEquals("V4.0", version);
    }
}
