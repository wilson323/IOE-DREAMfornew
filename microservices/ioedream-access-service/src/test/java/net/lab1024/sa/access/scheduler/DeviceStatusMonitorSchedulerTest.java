package net.lab1024.sa.access.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.manager.AlertManager;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 设备状态监控定时任务测试类
 * <p>
 * 测试范围：
 * - 定时任务执行
 * - 单个设备状态检查
 * - 批量设备状态检查
 * - 告警触发机制
 * </p>
 * <p>
 * 测试标准：
 * - 单元测试覆盖率 ≥ 80%
 * - 定时任务逻辑正确
 * - 告警触发时机准确
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("设备状态监控定时任务测试")
class DeviceStatusMonitorSchedulerTest {

    @Mock
    private AlertManager alertManager;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private DeviceStatusMonitorScheduler scheduler;

    @BeforeEach
    void setUp() {
        log.info("[测试准备] 初始化测试环境");
    }

    @Test
    @DisplayName("测试定时任务执行 - 成功获取设备状态")
    void testMonitorDeviceStatus_Success() {
        log.info("[测试] 测试定时任务执行 - 成功获取设备状态");

        // Given - 模拟设备状态概览数据
        Map<String, Object> deviceStatusOverview = new HashMap<>();
        deviceStatusOverview.put("total", 10);
        deviceStatusOverview.put("online", 8);
        deviceStatusOverview.put("offline", 2);
        deviceStatusOverview.put("fault", 0);

        @SuppressWarnings("unchecked")
        ResponseDTO<Map<String, Object>> mockResponse = ResponseDTO.ok(deviceStatusOverview);

        when(gatewayServiceClient.callDeviceCommService(
                eq("/api/v1/device/comm/device/status/overview"),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                any(com.fasterxml.jackson.core.type.TypeReference.class)
        )).thenReturn(mockResponse);

        // When - 执行定时任务
        scheduler.monitorDeviceStatus();

        // Then - 验证调用成功
        verify(gatewayServiceClient, times(1)).callDeviceCommService(
                eq("/api/v1/device/comm/device/status/overview"),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                any(com.fasterxml.jackson.core.type.TypeReference.class)
        );

        log.info("[测试] 定时任务执行成功");
    }

    @Test
    @DisplayName("测试定时任务执行 - 服务调用失败")
    void testMonitorDeviceStatus_ServiceError() {
        log.info("[测试] 测试定时任务执行 - 服务调用失败");

        // Given - 模拟服务调用失败
        when(gatewayServiceClient.callDeviceCommService(
                anyString(),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                any(com.fasterxml.jackson.core.type.TypeReference.class)
        )).thenReturn(null);

        // When - 执行定时任务（不应抛出异常）
        assertDoesNotThrow(() -> scheduler.monitorDeviceStatus());

        // Then - 验证调用了服务
        verify(gatewayServiceClient, times(1)).callDeviceCommService(
                anyString(),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                any(com.fasterxml.jackson.core.type.TypeReference.class)
        );

        log.info("[测试] 服务调用失败处理正确");
    }

    @Test
    @DisplayName("测试单个设备状态检查 - 设备离线触发告警")
    void testCheckDeviceStatus_Offline() {
        log.info("[测试] 测试单个设备状态检查 - 设备离线触发告警");

        // Given
        Long deviceId = 1001L;
        String deviceStatus = "OFFLINE";
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("temperature", 45);
        deviceData.put("lastSeen", "2025-01-30 10:00:00");

        ResponseDTO<Boolean> alertResponse = ResponseDTO.ok(true);
        when(alertManager.detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                any(Map.class)
        )).thenReturn(alertResponse);

        // When
        scheduler.checkDeviceStatus(deviceId, deviceStatus, deviceData);

        // Then
        verify(alertManager, times(1)).detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                any(Map.class)
        );

        log.info("[测试] 设备离线告警触发成功");
    }

    @Test
    @DisplayName("测试单个设备状态检查 - 设备故障触发告警")
    void testCheckDeviceStatus_Fault() {
        log.info("[测试] 测试单个设备状态检查 - 设备故障触发告警");

        // Given
        Long deviceId = 1002L;
        String deviceStatus = "FAULT";
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("errorCode", "E001");
        deviceData.put("errorMessage", "传感器故障");

        ResponseDTO<Boolean> alertResponse = ResponseDTO.ok(true);
        when(alertManager.detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                any(Map.class)
        )).thenReturn(alertResponse);

        // When
        scheduler.checkDeviceStatus(deviceId, deviceStatus, deviceData);

        // Then
        verify(alertManager, times(1)).detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                any(Map.class)
        );

        log.info("[测试] 设备故障告警触发成功");
    }

    @Test
    @DisplayName("测试单个设备状态检查 - 设备错误触发告警")
    void testCheckDeviceStatus_Error() {
        log.info("[测试] 测试单个设备状态检查 - 设备错误触发告警");

        // Given
        Long deviceId = 1003L;
        String deviceStatus = "ERROR";
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("errorType", "COMMUNICATION");

        ResponseDTO<Boolean> alertResponse = ResponseDTO.ok(true);
        when(alertManager.detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                any(Map.class)
        )).thenReturn(alertResponse);

        // When
        scheduler.checkDeviceStatus(deviceId, deviceStatus, deviceData);

        // Then
        verify(alertManager, times(1)).detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                any(Map.class)
        );

        log.info("[测试] 设备错误告警触发成功");
    }

    @Test
    @DisplayName("测试单个设备状态检查 - 设备在线不触发告警")
    void testCheckDeviceStatus_Online() {
        log.info("[测试] 测试单个设备状态检查 - 设备在线不触发告警");

        // Given
        Long deviceId = 1004L;
        String deviceStatus = "ONLINE";
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("temperature", 35);
        deviceData.put("signal", -45);

        // When
        scheduler.checkDeviceStatus(deviceId, deviceStatus, deviceData);

        // Then - 不应该触发告警
        verify(alertManager, never()).detectAndCreateAlert(
                anyLong(),
                anyString(),
                any(Map.class)
        );

        log.info("[测试] 设备在线未触发告警");
    }

    @Test
    @DisplayName("测试批量设备状态检查 - 成功")
    void testCheckDeviceStatusBatch_Success() {
        log.info("[测试] 测试批量设备状态检查 - 成功");

        // Given
        Map<Long, String> deviceStatusMap = new HashMap<>();
        deviceStatusMap.put(1001L, "ONLINE");
        deviceStatusMap.put(1002L, "OFFLINE");
        deviceStatusMap.put(1003L, "FAULT");

        // Mock告警响应
        ResponseDTO<Boolean> alertResponse = ResponseDTO.ok(true);
        when(alertManager.detectAndCreateAlert(
                anyLong(),
                anyString(),
                any(Map.class)
        )).thenReturn(alertResponse);

        // When
        scheduler.checkDeviceStatusBatch(deviceStatusMap);

        // Then - 验证OFFLINE和FAULT设备触发了告警（ONLINE不会触发）
        verify(alertManager, times(1)).detectAndCreateAlert(eq(1002L), eq("OFFLINE"), any(Map.class));
        verify(alertManager, times(1)).detectAndCreateAlert(eq(1003L), eq("FAULT"), any(Map.class));
        verify(alertManager, never()).detectAndCreateAlert(eq(1001L), eq("ONLINE"), any(Map.class));

        log.info("[测试] 批量设备状态检查成功");
    }

    @Test
    @DisplayName("测试批量设备状态检查 - 空列表")
    void testCheckDeviceStatusBatch_Empty() {
        log.info("[测试] 测试批量设备状态检查 - 空列表");

        // Given
        Map<Long, String> deviceStatusMap = new HashMap<>();

        // When
        scheduler.checkDeviceStatusBatch(deviceStatusMap);

        // Then - 不应该调用告警服务
        verify(alertManager, never()).detectAndCreateAlert(
                anyLong(),
                anyString(),
                any(Map.class)
        );

        log.info("[测试] 空列表处理正确");
    }

    @Test
    @DisplayName("测试单个设备状态检查 - 空设备数据")
    void testCheckDeviceStatus_NullDeviceData() {
        log.info("[测试] 测试单个设备状态检查 - 空设备数据");

        // Given
        Long deviceId = 1005L;
        String deviceStatus = "OFFLINE";

        ResponseDTO<Boolean> alertResponse = ResponseDTO.ok(true);
        when(alertManager.detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                any(Map.class)
        )).thenReturn(alertResponse);

        // When
        scheduler.checkDeviceStatus(deviceId, deviceStatus, null);

        // Then - 应该使用空Map作为默认值
        verify(alertManager, times(1)).detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                any(Map.class)
        );

        log.info("[测试] 空设备数据处理正确");
    }

    @Test
    @DisplayName("测试单个设备状态检查 - 告警创建失败")
    void testCheckDeviceStatus_AlertCreationFailed() {
        log.info("[测试] 测试单个设备状态检查 - 告警创建失败");

        // Given
        Long deviceId = 1006L;
        String deviceStatus = "OFFLINE";
        Map<String, Object> deviceData = new HashMap<>();

        // Mock告警创建失败
        when(alertManager.detectAndCreateAlert(
                anyLong(),
                anyString(),
                any(Map.class)
        )).thenReturn(ResponseDTO.error("ALERT_ERROR", "告警创建失败"));

        // When - 不应该抛出异常
        assertDoesNotThrow(() -> scheduler.checkDeviceStatus(deviceId, deviceStatus, deviceData));

        // Then - 验证尝试创建告警
        verify(alertManager, times(1)).detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                eq(deviceData)
        );

        log.info("[测试] 告警创建失败处理正确");
    }

    @Test
    @DisplayName("测试单个设备状态检查 - 异常处理")
    void testCheckDeviceStatus_Exception() {
        log.info("[测试] 测试单个设备状态检查 - 异常处理");

        // Given
        Long deviceId = 1007L;
        String deviceStatus = "OFFLINE";
        Map<String, Object> deviceData = new HashMap<>();

        // Mock抛出异常
        when(alertManager.detectAndCreateAlert(
                anyLong(),
                anyString(),
                any(Map.class)
        )).thenThrow(new RuntimeException("测试异常"));

        // When - 不应该抛出异常
        assertDoesNotThrow(() -> scheduler.checkDeviceStatus(deviceId, deviceStatus, deviceData));

        // Then - 验证尝试创建告警
        verify(alertManager, times(1)).detectAndCreateAlert(
                eq(deviceId),
                eq(deviceStatus),
                eq(deviceData)
        );

        log.info("[测试] 异常处理正确");
    }

    @Test
    @DisplayName("测试定时任务执行 - 空响应")
    void testMonitorDeviceStatus_EmptyResponse() {
        log.info("[测试] 测试定时任务执行 - 空响应");

        // Given - 模拟空数据响应
        @SuppressWarnings("unchecked")
        ResponseDTO<Map<String, Object>> mockResponse = ResponseDTO.ok(new HashMap<>());

        when(gatewayServiceClient.callDeviceCommService(
                anyString(),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                any(com.fasterxml.jackson.core.type.TypeReference.class)
        )).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> scheduler.monitorDeviceStatus());

        // Then - 验证调用了服务
        verify(gatewayServiceClient, times(1)).callDeviceCommService(
                anyString(),
                any(org.springframework.http.HttpMethod.class),
                isNull(),
                any(com.fasterxml.jackson.core.type.TypeReference.class)
        );

        log.info("[测试] 空响应处理正确");
    }
}
