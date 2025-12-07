package net.lab1024.sa.devicecomm.protocol.handler.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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
 * ConsumeProtocolHandler单元测试
 * <p>
 * 目标覆盖率：≥80%
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

    @InjectMocks
    private ConsumeProtocolHandler consumeProtocolHandler;

    private byte[] validMessageBytes;
    private byte[] invalidHeaderBytes;
    private byte[] shortMessageBytes;

    @BeforeEach
    void setUp() {
        // 准备有效的测试消息（模拟消费记录消息）
        // 协议头(2字节) + 消息类型(1字节) + 设备编号(8字节) + 用户ID(4字节) + 消费金额(4字节) + 消费时间(4字节) + 校验和(2字节)
        validMessageBytes = new byte[25];
        ByteBuffer buffer = ByteBuffer.wrap(validMessageBytes).order(ByteOrder.LITTLE_ENDIAN);
        
        // 协议头
        buffer.put((byte) 0xAA);
        buffer.put((byte) 0x55);
        
        // 消息类型（0x01 = 消费记录）
        buffer.put((byte) 0x01);
        
        // 设备编号（8字节，填充"DEV001  "）
        byte[] deviceCode = "DEV001  ".getBytes();
        buffer.put(deviceCode);
        
        // 用户ID（4字节）
        buffer.putInt(1001);
        
        // 消费金额（4字节，单位：分，例如1000 = 10.00元）
        buffer.putInt(1000);
        
        // 消费时间（4字节，Unix时间戳）
        buffer.putInt((int) (System.currentTimeMillis() / 1000));
        
        // 计算并填充校验和（简化：累加和）
        int checksum = 0;
        for (int i = 2; i < validMessageBytes.length - 2; i++) {
            checksum += validMessageBytes[i] & 0xFF;
        }
        checksum = checksum & 0xFFFF;
        buffer.putShort((short) checksum);

        // 准备无效协议头的消息
        invalidHeaderBytes = new byte[25];
        invalidHeaderBytes[0] = 0x00;
        invalidHeaderBytes[1] = 0x00;

        // 准备长度不足的消息
        shortMessageBytes = new byte[10];
    }

    @Test
    @DisplayName("测试解析有效消息-成功场景")
    void testParseMessage_ValidMessage_Success() throws ProtocolParseException {
        // When
        ProtocolMessage message = consumeProtocolHandler.parseMessage((byte[]) validMessageBytes);

        // Then
        assertNotNull(message);
        assertEquals("CONSUME_ZKTECO_V1_0", message.getProtocolType());
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
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1001);
        data.put("amount", 1000);
        data.put("consumeTime", (int) (System.currentTimeMillis() / 1000));
        message.setData(data);

        // Mock网关服务调用
        ResponseDTO<Object> mockResponse = ResponseDTO.ok(new Object());
        when(gatewayServiceClient.callConsumeService(
                eq("/api/v1/consume/transaction/execute"),
                eq(HttpMethod.POST),
                any(),
                eq(Object.class)
        )).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> {
            consumeProtocolHandler.processMessage(message, 1L);
        });

        // Then
        verify(gatewayServiceClient, times(1)).callConsumeService(
                eq("/api/v1/consume/transaction/execute"),
                eq(HttpMethod.POST),
                any(),
                eq(Object.class)
        );
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
        ProtocolMessage message = consumeProtocolHandler.parseMessage((byte[]) validMessageBytes);
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
            consumeProtocolHandler.processMessage(message, 1L);
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
        assertEquals("CONSUME_ZKTECO_V1_0", protocolType);
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

