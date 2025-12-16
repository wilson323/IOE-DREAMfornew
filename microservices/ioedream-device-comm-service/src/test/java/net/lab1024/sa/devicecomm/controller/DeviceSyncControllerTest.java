package net.lab1024.sa.devicecomm.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.devicecomm.service.DeviceSyncService;

/**
 * DeviceSyncController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：DeviceSyncController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceSyncController单元测试")
class DeviceSyncControllerTest {
    @Mock
    private DeviceSyncService deviceSyncService;
    @InjectMocks
    private DeviceSyncController deviceSyncController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("同步用户信息到设备-成功场景")
    void test_syncUser_Success() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("deviceId", "DEV001");
        request.put("userId", "1001");
        request.put("userInfo", Map.of("name", "张三"));

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("status", "success");
        mockResult.put("deviceId", "DEV001");

        when(deviceSyncService.syncUserInfo(eq("DEV001"), eq(1001L), anyMap())).thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> response = deviceSyncController.syncUser(request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("success", response.getData().get("status"));
        verify(deviceSyncService, times(1)).syncUserInfo(eq("DEV001"), eq(1001L), anyMap());
    }

    @Test
    @DisplayName("同步用户信息到设备-参数缺失")
    void test_syncUser_MissingParams() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("deviceId", "DEV001");
        // userId缺失

        // When
        ResponseDTO<Map<String, Object>> response = deviceSyncController.syncUser(request);

        // Then
        assertEquals(ResponseDTO.error("PARAM_ERROR", "x").getCode(), response.getCode());
        assertNull(response.getData());
        verify(deviceSyncService, never()).syncUserInfo(anyString(), anyLong(), anyMap());
    }

    @Test
    @DisplayName("撤销用户权限-成功场景")
    void test_revokeUserPermission_Success() {
        // Given
        String deviceId = "DEV001";
        String userId = "1001";

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("status", "success");

        when(deviceSyncService.revokeUserPermission(deviceId, 1001L)).thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> response = deviceSyncController.revokeUserPermission(deviceId, userId);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("success", response.getData().get("status"));
        verify(deviceSyncService, times(1)).revokeUserPermission(deviceId, 1001L);
    }

    @Test
    @DisplayName("获取设备用户列表-成功场景")
    void test_getDeviceUsers_Success() {
        // Given
        String deviceId = "DEV001";
        List<String> mockUsers = List.of("1001", "1002", "1003");

        when(deviceSyncService.getDeviceUsers(deviceId)).thenReturn(mockUsers);

        // When
        ResponseDTO<List<String>> response = deviceSyncController.getDeviceUsers(deviceId);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(3, response.getData().size());
        verify(deviceSyncService, times(1)).getDeviceUsers(deviceId);
    }

    @Test
    @DisplayName("同步业务属性到设备-成功场景")
    void test_syncBusinessAttributes_Success() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("deviceId", "DEV001");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("accessMode", "card");
        request.put("attributes", attributes);

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("status", "success");

        when(deviceSyncService.syncBusinessAttributes(eq("DEV001"), anyMap())).thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> response = deviceSyncController.syncBusinessAttributes(request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("success", response.getData().get("status"));
        verify(deviceSyncService, times(1)).syncBusinessAttributes(eq("DEV001"), anyMap());
    }

    @Test
    @DisplayName("设备健康检查-成功场景")
    void test_healthCheck_Success() {
        // Given
        String deviceId = "DEV001";
        Map<String, Object> mockHealth = new HashMap<>();
        mockHealth.put("status", "healthy");
        mockHealth.put("responseTime", 50);

        when(deviceSyncService.checkDeviceHealth(deviceId)).thenReturn(mockHealth);

        // When
        ResponseDTO<Map<String, Object>> response = deviceSyncController.healthCheck(deviceId);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("healthy", response.getData().get("status"));
        verify(deviceSyncService, times(1)).checkDeviceHealth(deviceId);
    }

    @Test
    @DisplayName("获取设备性能指标-成功场景")
    void test_getDeviceMetrics_Success() {
        // Given
        String deviceId = "DEV001";
        Map<String, Object> mockMetrics = new HashMap<>();
        mockMetrics.put("cpu", 50.0);
        mockMetrics.put("memory", 60.0);

        when(deviceSyncService.getDeviceMetrics(deviceId)).thenReturn(mockMetrics);

        // When
        ResponseDTO<Map<String, Object>> response = deviceSyncController.getDeviceMetrics(deviceId);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(50.0, response.getData().get("cpu"));
        verify(deviceSyncService, times(1)).getDeviceMetrics(deviceId);
    }

    @Test
    @DisplayName("设备心跳-成功场景")
    void test_heartbeat_Success() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("deviceId", "DEV001");
        request.put("timestamp", System.currentTimeMillis());

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("status", "ok");

        when(deviceSyncService.processHeartbeat(eq("DEV001"), anyMap())).thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> response = deviceSyncController.heartbeat(request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("ok", response.getData().get("status"));
        verify(deviceSyncService, times(1)).processHeartbeat(eq("DEV001"), anyMap());
    }

    @Test
    @DisplayName("设备心跳-参数缺失")
    void test_heartbeat_MissingDeviceId() {
        // Given
        Map<String, Object> request = new HashMap<>();
        // deviceId缺失

        // When
        ResponseDTO<Map<String, Object>> response = deviceSyncController.heartbeat(request);

        // Then
        assertEquals(ResponseDTO.error("PARAM_ERROR", "x").getCode(), response.getCode());
        assertNull(response.getData());
        verify(deviceSyncService, never()).processHeartbeat(anyString(), anyMap());
    }
}
