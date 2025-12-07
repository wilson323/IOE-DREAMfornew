package net.lab1024.sa.devicecomm.protocol.handler.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolParseException;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolProcessException;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;

/**
 * AccessProtocolHandler单元测试
 * <p>
 * 目标覆盖率：≥80%
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

    @InjectMocks
    private AccessProtocolHandler accessProtocolHandler;

    private byte[] validMessageBytes;
    private byte[] invalidHeaderBytes;
    private byte[] shortMessageBytes;

    @BeforeEach
    void setUp() {
        // 准备有效的测试消息（模拟门禁事件消息）
        // 协议头(2字节) + 消息类型(1字节) + 设备编号(8字节) + 用户ID(4字节) + 通行时间(4字节) + 通行类型(1字节) + 门号(1字节) + 通行方式(1字节) + 校验和(2字节)
        validMessageBytes = new byte[24];
        ByteBuffer buffer = ByteBuffer.wrap(validMessageBytes).order(ByteOrder.LITTLE_ENDIAN);
        
        // 协议头
        buffer.put((byte) 0xAA);
        buffer.put((byte) 0x55);
        
        // 消息类型（0x01 = 门禁事件）
        buffer.put((byte) 0x01);
        
        // 设备编号（8字节，填充"DEV001  "）
        byte[] deviceCode = "DEV001  ".getBytes();
        buffer.put(deviceCode);
        
        // 用户ID（4字节）
        buffer.putInt(1001);
        
        // 通行时间（4字节，Unix时间戳）
        buffer.putInt((int) (System.currentTimeMillis() / 1000));
        
        // 通行类型（1字节：0-进入）
        buffer.put((byte) 0x00);
        
        // 门号（1字节）
        buffer.put((byte) 0x01);
        
        // 通行方式（1字节：1-人脸）
        buffer.put((byte) 0x01);
        
        // 计算并填充校验和（简化：累加和）
        int checksum = 0;
        for (int i = 2; i < validMessageBytes.length - 2; i++) {
            checksum += validMessageBytes[i] & 0xFF;
        }
        checksum = checksum & 0xFFFF;
        buffer.putShort((short) checksum);

        // 准备无效协议头的消息
        invalidHeaderBytes = new byte[24];
        invalidHeaderBytes[0] = 0x00;
        invalidHeaderBytes[1] = 0x00;

        // 准备长度不足的消息
        shortMessageBytes = new byte[10];
    }

    @Test
    @DisplayName("测试解析有效消息-成功场景")
    void testParseMessage_ValidMessage_Success() throws ProtocolParseException {
        // When
        ProtocolMessage message = accessProtocolHandler.parseMessage((byte[]) validMessageBytes);

        // Then
        assertNotNull(message);
        assertEquals("ACCESS_ENTROPY_V4_8", message.getProtocolType());
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
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1001);
        data.put("passTime", (int) (System.currentTimeMillis() / 1000));
        data.put("passType", 0);
        data.put("doorNo", 1);
        data.put("passMethod", 1);
        message.setData(data);

        // Mock网关服务调用
        ResponseDTO<Long> mockResponse = ResponseDTO.ok(2001L);
        when(gatewayServiceClient.callAccessService(
                eq("/api/v1/access/record/create"),
                eq(HttpMethod.POST),
                any(),
                eq(Long.class)
        )).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> {
            accessProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(gatewayServiceClient, times(1)).callAccessService(
                eq("/api/v1/access/record/create"),
                eq(HttpMethod.POST),
                any(),
                eq(Long.class)
        );
    }

    @Test
    @DisplayName("测试处理设备状态消息-成功场景")
    void testProcessMessage_DeviceStatus_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = accessProtocolHandler.parseMessage((byte[]) validMessageBytes);
        message.setMessageType("DEVICE_STATUS");
        
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ONLINE");
        data.put("lastOnlineTime", (int) (System.currentTimeMillis() / 1000));
        message.setData(data);

        // Mock网关服务调用
        ResponseDTO<Boolean> mockResponse = ResponseDTO.ok(true);
        when(gatewayServiceClient.callCommonService(
                eq("/api/v1/device/status/update"),
                eq(HttpMethod.PUT),
                any(),
                eq(Boolean.class)
        )).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> {
            accessProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(gatewayServiceClient, times(1)).callCommonService(
                eq("/api/v1/device/status/update"),
                eq(HttpMethod.PUT),
                any(),
                eq(Boolean.class)
        );
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

        // Mock网关服务调用
        ResponseDTO<Long> mockResponse = ResponseDTO.ok(3001L);
        when(gatewayServiceClient.callCommonService(
                eq("/api/v1/alarm/record/create"),
                eq(HttpMethod.POST),
                any(),
                eq(Long.class)
        )).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> {
            accessProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(gatewayServiceClient, times(1)).callCommonService(
                eq("/api/v1/alarm/record/create"),
                eq(HttpMethod.POST),
                any(),
                eq(Long.class)
        );
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
        assertEquals("ACCESS_ENTROPY_V4_8", protocolType);
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

