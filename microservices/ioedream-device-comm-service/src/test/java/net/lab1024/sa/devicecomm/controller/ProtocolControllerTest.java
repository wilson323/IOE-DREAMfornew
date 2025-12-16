package net.lab1024.sa.devicecomm.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.devicecomm.cache.ProtocolCacheService;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;
import net.lab1024.sa.devicecomm.protocol.router.MessageRouter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * ProtocolController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：ProtocolController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProtocolController单元测试")
class ProtocolControllerTest {
    @Mock
    private MessageRouter messageRouter;
    @Mock
    private GatewayServiceClient gatewayServiceClient;
    private MeterRegistry meterRegistry;
    @Mock
    private ProtocolCacheService cacheService;
    @InjectMocks
    private ProtocolController protocolController;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(protocolController, "meterRegistry", meterRegistry);
    }

    @Test
    @DisplayName("接收设备推送数据-成功场景")
    void test_receivePush_Success() {
        // Given
        String protocolType = "ATTENDANCE_ENTROPY_V4.0";
        Long deviceId = 1001L;
        byte[] rawData = "test data".getBytes();

        ProtocolMessage mockMessage = new ProtocolMessage();
        CompletableFuture<ProtocolMessage> future = CompletableFuture.completedFuture(mockMessage);

        when(messageRouter.route(eq(protocolType), eq(rawData), eq(deviceId))).thenReturn(future);

        // When
        ResponseDTO<String> response = protocolController.receivePush(protocolType, deviceId, rawData);

        // Then
        assertEquals(200, response.getCode());
        assertEquals("消息处理成功", response.getData());
        verify(messageRouter, times(1)).route(protocolType, rawData, deviceId);
    }

    @Test
    @DisplayName("接收设备推送数据-参数异常")
    void test_receivePush_ParamException() {
        // Given
        String protocolType = "ATTENDANCE_ENTROPY_V4.0";
        Long deviceId = 1001L;
        byte[] rawData = "test data".getBytes();

        CompletableFuture<ProtocolMessage> future = new CompletableFuture<>();
        future.completeExceptionally(new ParamException("PARAM_ERROR", "参数错误"));

        when(messageRouter.route(eq(protocolType), eq(rawData), eq(deviceId))).thenReturn(future);

        // When
        ResponseDTO<String> response = protocolController.receivePush(protocolType, deviceId, rawData);

        // Then
        assertEquals(ResponseDTO.error("PARAM_ERROR", "x").getCode(), response.getCode());
        assertNull(response.getData());
        verify(messageRouter, times(1)).route(protocolType, rawData, deviceId);
    }

    @Test
    @DisplayName("接收设备推送数据-业务异常")
    void test_receivePush_BusinessException() {
        // Given
        String protocolType = "ATTENDANCE_ENTROPY_V4.0";
        Long deviceId = 1001L;
        byte[] rawData = "test data".getBytes();

        CompletableFuture<ProtocolMessage> future = new CompletableFuture<>();
        future.completeExceptionally(new BusinessException("BUSINESS_ERROR", "业务错误"));

        when(messageRouter.route(eq(protocolType), eq(rawData), eq(deviceId))).thenReturn(future);

        // When
        ResponseDTO<String> response = protocolController.receivePush(protocolType, deviceId, rawData);

        // Then
        assertEquals(ResponseDTO.error("BUSINESS_ERROR", "x").getCode(), response.getCode());
        assertNull(response.getData());
        verify(messageRouter, times(1)).route(protocolType, rawData, deviceId);
    }

    @Test
    @DisplayName("接收设备推送数据-自动识别协议-成功场景")
    void test_receivePushAuto_Success() {
        // Given
        String deviceType = "ATTENDANCE";
        String manufacturer = "熵基科技";
        Long deviceId = 1001L;
        byte[] rawData = "test data".getBytes();

        ProtocolMessage mockMessage = new ProtocolMessage();
        CompletableFuture<ProtocolMessage> future = CompletableFuture.completedFuture(mockMessage);

        when(messageRouter.route(eq(deviceType), eq(manufacturer), eq(rawData), eq(deviceId))).thenReturn(future);

        // When
        ResponseDTO<String> response = protocolController.receivePushAuto(deviceType, manufacturer, deviceId, rawData);

        // Then
        assertEquals(200, response.getCode());
        assertEquals("消息处理成功", response.getData());
        verify(messageRouter, times(1)).route(deviceType, manufacturer, rawData, deviceId);
    }

    @Test
    @DisplayName("接收HTTP文本推送-成功场景")
    void test_receivePushText_Success() {
        // Given
        String serialNumber = "SN001";
        String table = "ATTLOG";
        String protocolType = null;
        Long deviceId = 1001L;
        String rawData = "test text data";

        DeviceEntity mockDevice = new DeviceEntity();
        mockDevice.setId(deviceId);
        mockDevice.setDeviceCode(serialNumber);

        ProtocolMessage mockMessage = new ProtocolMessage();
        CompletableFuture<ProtocolMessage> future = CompletableFuture.completedFuture(mockMessage);

        when(messageRouter.route(anyString(), eq(rawData), eq(deviceId))).thenReturn(future);

        // When
        ResponseDTO<String> response = protocolController.receivePushText(serialNumber, table, protocolType, deviceId, rawData);

        // Then
        assertEquals(200, response.getCode());
        assertEquals("OK", response.getData());
        verify(messageRouter, times(1)).route(anyString(), eq(rawData), eq(deviceId));
    }

    @Test
    @DisplayName("接收HTTP文本推送-根据SN查找设备-成功场景")
    void test_receivePushText_FindDeviceBySerialNumber_Success() {
        // Given
        String serialNumber = "SN001";
        String table = "ATTLOG";
        String protocolType = null;
        Long deviceId = null;
        String rawData = "test text data";

        DeviceEntity mockDevice = new DeviceEntity();
        mockDevice.setId(1001L);
        mockDevice.setDeviceCode(serialNumber);

        ProtocolMessage mockMessage = new ProtocolMessage();
        CompletableFuture<ProtocolMessage> future = CompletableFuture.completedFuture(mockMessage);

        when(cacheService.getDeviceByCode(serialNumber)).thenReturn(mockDevice);
        when(messageRouter.route(anyString(), eq(rawData), eq(1001L))).thenReturn(future);

        // When
        ResponseDTO<String> response = protocolController.receivePushText(serialNumber, table, protocolType, deviceId, rawData);

        // Then
        assertEquals(200, response.getCode());
        assertEquals("OK", response.getData());
        verify(cacheService, times(1)).getDeviceByCode(serialNumber);
        verify(messageRouter, times(1)).route(anyString(), eq(rawData), eq(1001L));
    }

    @Test
    @DisplayName("接收HTTP文本推送-无法识别协议类型")
    void test_receivePushText_ProtocolTypeNotFound() {
        // Given
        String serialNumber = "SN001";
        String table = "UNKNOWN_TABLE";
        String protocolType = null;
        Long deviceId = 1001L;
        String rawData = "test text data";

        // When
        ResponseDTO<String> response = protocolController.receivePushText(serialNumber, table, protocolType, deviceId, rawData);

        // Then
        assertEquals(ResponseDTO.error("PROTOCOL_TYPE_REQUIRED", "x").getCode(), response.getCode());
        assertNull(response.getData());
        verify(messageRouter, never()).route(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("限流降级方法-成功场景")
    void test_receivePushTextFallback_Success() {
        // Given
        String serialNumber = "SN001";
        String table = "ATTLOG";
        String protocolType = "ATTENDANCE_ENTROPY_V4.0";
        Long deviceId = 1001L;
        String rawData = "test text data";
        Exception exception = new RuntimeException("Rate limit exceeded");

        // When
        ResponseDTO<String> response = protocolController.receivePushTextFallback(
                serialNumber, table, protocolType, deviceId, rawData, exception);

        // Then
        assertEquals(ResponseDTO.error("RATE_LIMIT", "x").getCode(), response.getCode());
        assertEquals("请求过于频繁，请稍后重试", response.getMessage());
        assertNull(response.getData());
    }
}
