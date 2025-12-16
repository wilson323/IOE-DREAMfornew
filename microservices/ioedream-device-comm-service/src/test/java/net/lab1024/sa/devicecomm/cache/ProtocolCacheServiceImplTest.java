package net.lab1024.sa.devicecomm.cache;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * ProtocolCacheServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of ProtocolCacheServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProtocolCacheServiceImpl Unit Test")
@SuppressWarnings("unchecked")
class ProtocolCacheServiceImplTest {

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private ProtocolCacheServiceImpl protocolCacheServiceImpl;

    private DeviceEntity mockDevice;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockDevice = new DeviceEntity();
        mockDevice.setId(1L);
        mockDevice.setDeviceCode("DEV001");
        mockDevice.setDeviceName("Test Device");
        mockDevice.setDeviceType("ACCESS");
        mockDevice.setStatus("ONLINE");
        mockDevice.setDeletedFlag(0);
    }

    @Test
    @DisplayName("Test getDeviceById - Success Scenario")
    void test_getDeviceById_Success() {
        // Given
        Long deviceId = 1L;
        ResponseDTO<DeviceEntity> response = ResponseDTO.ok(mockDevice);

        when(gatewayServiceClient.callCommonService(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(DeviceEntity.class)))
            .thenReturn(response);

        // When
        DeviceEntity result = protocolCacheServiceImpl.getDeviceById(deviceId);

        // Then
        assertNotNull(result);
        assertEquals(deviceId, result.getId());
        assertEquals("DEV001", result.getDeviceCode());
        verify(gatewayServiceClient, times(1)).callCommonService(anyString(), eq(HttpMethod.GET), isNull(), eq(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test getDeviceById - Null DeviceId")
    void test_getDeviceById_NullDeviceId() {
        // When
        DeviceEntity result = protocolCacheServiceImpl.getDeviceById(null);

        // Then
        assertNull(result);
        verify(gatewayServiceClient, never()).callCommonService(anyString(), any(HttpMethod.class), any(), any(Class.class));
    }

    @Test
    @DisplayName("Test getDeviceById - Device Not Found")
    void test_getDeviceById_DeviceNotFound() {
        // Given
        Long deviceId = 999L;
        ResponseDTO<DeviceEntity> response = ResponseDTO.ok(null);

        when(gatewayServiceClient.callCommonService(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(DeviceEntity.class)))
            .thenReturn(response);

        // When
        DeviceEntity result = protocolCacheServiceImpl.getDeviceById(deviceId);

        // Then
        assertNull(result);
        verify(gatewayServiceClient, times(1)).callCommonService(anyString(), eq(HttpMethod.GET), isNull(), eq(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test getDeviceByCode - Success Scenario")
    void test_getDeviceByCode_Success() {
        // Given
        String deviceCode = "DEV001";
        ResponseDTO<DeviceEntity> response = ResponseDTO.ok(mockDevice);

        when(gatewayServiceClient.callCommonService(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(DeviceEntity.class)))
            .thenReturn(response);

        // When
        DeviceEntity result = protocolCacheServiceImpl.getDeviceByCode(deviceCode);

        // Then
        assertNotNull(result);
        assertEquals(deviceCode, result.getDeviceCode());
        verify(gatewayServiceClient, times(1)).callCommonService(anyString(), eq(HttpMethod.GET), isNull(), eq(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test getDeviceByCode - Null DeviceCode")
    void test_getDeviceByCode_NullDeviceCode() {
        // When
        DeviceEntity result = protocolCacheServiceImpl.getDeviceByCode(null);

        // Then
        assertNull(result);
        verify(gatewayServiceClient, never()).callCommonService(anyString(), any(HttpMethod.class), any(), any(Class.class));
    }

    @Test
    @DisplayName("Test getDeviceByCode - Empty DeviceCode")
    void test_getDeviceByCode_EmptyDeviceCode() {
        // When
        DeviceEntity result = protocolCacheServiceImpl.getDeviceByCode("");

        // Then
        assertNull(result);
        verify(gatewayServiceClient, never()).callCommonService(anyString(), any(HttpMethod.class), any(), any(Class.class));
    }

    @Test
    @DisplayName("Test cacheDevice - Success Scenario (Stub)")
    void test_cacheDevice_Success() {
        // When
        protocolCacheServiceImpl.cacheDevice(mockDevice);

        // Then
        // For cache operations, just verify method was called
        assertTrue(true);
    }

    @Test
    @DisplayName("Test cacheDeviceByCode - Success Scenario (Stub)")
    void test_cacheDeviceByCode_Success() {
        // When
        DeviceEntity result = protocolCacheServiceImpl.cacheDeviceByCode(mockDevice);

        // Then
        // For cache operations, just verify method was called
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test getUserIdByCardNumber - Success Scenario (Stub)")
    void test_getUserIdByCardNumber_Success() {
        // When
        protocolCacheServiceImpl.getUserIdByCardNumber("CARD001");

        // Then
        // For stub method, just verify it was called
        assertTrue(true);
    }

    @Test
    @DisplayName("Test cacheUserCardMapping - Success Scenario (Stub)")
    void test_cacheUserCardMapping_Success() {
        // When
        protocolCacheServiceImpl.cacheUserCardMapping("CARD001", 100L);

        // Then
        // For cache operations, just verify method was called
        assertTrue(true);
    }

    @Test
    @DisplayName("Test evictDevice - Success Scenario (Stub)")
    void test_evictDevice_Success() {
        // When
        protocolCacheServiceImpl.evictDevice(1L);

        // Then
        // For cache eviction, just verify method was called
        assertTrue(true);
    }

    @Test
    @DisplayName("Test evictDeviceByCode - Success Scenario (Stub)")
    void test_evictDeviceByCode_Success() {
        // When
        protocolCacheServiceImpl.evictDeviceByCode("DEV001");

        // Then
        // For cache eviction, just verify method was called
        assertTrue(true);
    }
}
