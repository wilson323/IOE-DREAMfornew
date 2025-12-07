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
 * AttendanceProtocolHandler单元测试
 * <p>
 * 目标覆盖率：≥80%
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

    @InjectMocks
    private AttendanceProtocolHandler attendanceProtocolHandler;

    private byte[] validMessageBytes;
    private byte[] invalidHeaderBytes;
    private byte[] shortMessageBytes;

    @BeforeEach
    void setUp() {
        // 准备有效的测试消息（模拟考勤记录消息）
        // 协议头(2字节) + 消息类型(1字节) + 设备编号(8字节) + 用户ID(4字节) + 打卡时间(4字节) + 打卡类型(1字节) + 校验和(2字节)
        validMessageBytes = new byte[22];
        ByteBuffer buffer = ByteBuffer.wrap(validMessageBytes).order(ByteOrder.LITTLE_ENDIAN);
        
        // 协议头
        buffer.put((byte) 0xAA);
        buffer.put((byte) 0x55);
        
        // 消息类型（0x01 = 考勤记录）
        buffer.put((byte) 0x01);
        
        // 设备编号（8字节，填充"DEV001  "）
        byte[] deviceCode = "DEV001  ".getBytes();
        buffer.put(deviceCode);
        
        // 用户ID（4字节）
        buffer.putInt(1001);
        
        // 打卡时间（4字节，Unix时间戳）
        buffer.putInt((int) (System.currentTimeMillis() / 1000));
        
        // 打卡类型（1字节：0-上班，1-下班）
        buffer.put((byte) 0x00);
        
        // 计算并填充校验和（简化：累加和）
        int checksum = 0;
        for (int i = 2; i < validMessageBytes.length - 2; i++) {
            checksum += validMessageBytes[i] & 0xFF;
        }
        checksum = checksum & 0xFFFF;
        buffer.putShort((short) checksum);

        // 准备无效协议头的消息
        invalidHeaderBytes = new byte[22];
        invalidHeaderBytes[0] = 0x00;
        invalidHeaderBytes[1] = 0x00;

        // 准备长度不足的消息
        shortMessageBytes = new byte[10];
    }

    @Test
    @DisplayName("测试解析有效消息-成功场景")
    void testParseMessage_ValidMessage_Success() throws ProtocolParseException {
        // When
        ProtocolMessage message = attendanceProtocolHandler.parseMessage((byte[]) validMessageBytes);

        // Then
        assertNotNull(message);
        assertEquals("ATTENDANCE_ENTROPY_V4_0", message.getProtocolType());
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
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1001);
        data.put("punchTime", (int) (System.currentTimeMillis() / 1000));
        data.put("punchType", 0);
        message.setData(data);

        // Mock网关服务调用
        ResponseDTO<Long> mockResponse = ResponseDTO.ok(2001L);
        when(gatewayServiceClient.callAttendanceService(
                eq("/api/v1/attendance/record/create"),
                eq(HttpMethod.POST),
                any(),
                eq(Long.class)
        )).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> {
            attendanceProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(gatewayServiceClient, times(1)).callAttendanceService(
                eq("/api/v1/attendance/record/create"),
                eq(HttpMethod.POST),
                any(),
                eq(Long.class)
        );
    }

    @Test
    @DisplayName("测试处理设备状态消息-成功场景")
    void testProcessMessage_DeviceStatus_Success() throws ProtocolParseException, ProtocolProcessException {
        // Given
        ProtocolMessage message = attendanceProtocolHandler.parseMessage((byte[]) validMessageBytes);
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
            attendanceProtocolHandler.processMessage(message, 1L);
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
        assertEquals("ATTENDANCE_ENTROPY_V4_0", protocolType);
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

