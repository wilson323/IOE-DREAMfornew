package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.consume.manager.impl.ConsumeDeviceManagerImpl;

/**
 * ConsumeDeviceManager单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：ConsumeDeviceManager核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeDeviceManager单元测试")
class ConsumeDeviceManagerTest {
    @Mock
    private GatewayServiceClient gatewayServiceClient;
    @Mock
    private ObjectMapper objectMapper;
    private ConsumeDeviceManagerImpl consumeDeviceManager;

    @BeforeEach
    void setUp() {
        consumeDeviceManager = new ConsumeDeviceManagerImpl(gatewayServiceClient, objectMapper);
    }

    @Test
    @DisplayName("getDeviceById - 成功场景")
    void test_getDeviceById_Success() {
        // Given
        String deviceId = "DEV001";
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("1");
        device.setDeviceCode(deviceId);
        device.setDeviceName("消费设备1");

        when(gatewayServiceClient.callCommonService(
                eq("/api/v1/device/" + deviceId),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                eq(DeviceEntity.class)))
                .thenReturn(ResponseDTO.ok(device));

        // When
        DeviceEntity result = consumeDeviceManager.getDeviceById(deviceId);

        // Then
        assertNotNull(result);
        assertEquals(deviceId, result.getDeviceCode());
        verify(gatewayServiceClient).callCommonService(
                eq("/api/v1/device/" + deviceId),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                eq(DeviceEntity.class));
    }

    @Test
    @DisplayName("isDeviceOnline - 设备在线")
    void test_isDeviceOnline_Online() {
        // Given
        String deviceId = "1";
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId(deviceId);
        device.setDeviceCode("DEV001");
        // DeviceEntity.deviceStatus 为 String 类型，保持与公共模块定义一致
        device.setDeviceStatus("1");

        when(gatewayServiceClient.callCommonService(
                eq("/api/v1/device/" + deviceId),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                eq(DeviceEntity.class)))
                .thenReturn(ResponseDTO.ok(device));

        // When
        boolean result = consumeDeviceManager.isDeviceOnline(deviceId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("isConsumeModeSupported - 支持消费模式")
    void test_isConsumeModeSupported_Supported() {
        // Given
        String deviceId = "1";
        String consumeMode = "FIXED";
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId(deviceId);
        device.setDeviceCode("DEV001");
        device.setExtendedAttributes("{\"supportedConsumeModes\":[\"FIXED\",\"AMOUNT\"]}");

        when(gatewayServiceClient.callCommonService(
                eq("/api/v1/device/" + deviceId),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                eq(DeviceEntity.class)))
                .thenReturn(ResponseDTO.ok(device));

        // When
        boolean result = consumeDeviceManager.isConsumeModeSupported(deviceId, consumeMode);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("基础测试 - 确保测试框架正常工作")
    void test_basic_Success() {
        // Given
        // 基础测试，确保测试框架正常工作

        // When & Then
        assertNotNull(consumeDeviceManager);
        assertNotNull(gatewayServiceClient);
        assertTrue(true);
    }
}


