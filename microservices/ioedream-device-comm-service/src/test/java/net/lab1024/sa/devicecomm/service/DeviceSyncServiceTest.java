package net.lab1024.sa.devicecomm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.devicecomm.protocol.client.DeviceProtocolClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceSyncService 单元测试")
class DeviceSyncServiceTest {

    @Mock
    private DeviceDao deviceDao;

    @Mock
    private DeviceProtocolClient deviceProtocolClient;

    @InjectMocks
    private DeviceSyncService deviceSyncService;

    @Test
    @DisplayName("同步用户信息-设备不存在抛业务异常")
    void syncUserInfo_deviceNotFound_throwsBusinessException() {
        when(deviceDao.selectById("DEV001")).thenReturn(null);
        assertThrows(net.lab1024.sa.common.exception.BusinessException.class,
                () -> deviceSyncService.syncUserInfo("DEV001", 1001L, Map.of()));
    }

    @Test
    @DisplayName("同步用户信息-成功调用协议客户端")
    void syncUserInfo_success_delegatesToProtocolClient() {
        DeviceEntity device = new DeviceEntity();
        device.setId(1L);
        device.setDeviceType("ACCESS");
        device.setExtendedAttributes("{\"manufacturer\":\"TEST\"}");
        when(deviceDao.selectById("DEV001")).thenReturn(device);
        when(deviceProtocolClient.performDeviceUserSync(eq(device), eq(1001L), anyMap())).thenReturn(true);

        Map<String, Object> result = deviceSyncService.syncUserInfo("DEV001", 1001L, Map.of("name", "张三"));

        assertNotNull(result);
        assertEquals("DEV001", result.get("deviceId"));
        assertEquals(1001L, result.get("userId"));
        assertEquals("SUCCESS", result.get("syncStatus"));
        verify(deviceProtocolClient, times(1)).performDeviceUserSync(eq(device), eq(1001L), anyMap());
    }

    @Test
    @DisplayName("撤销用户权限-成功调用协议客户端")
    void revokeUserPermission_success_delegatesToProtocolClient() {
        DeviceEntity device = new DeviceEntity();
        device.setId(1L);
        when(deviceDao.selectById("DEV001")).thenReturn(device);
        when(deviceProtocolClient.performDeviceUserRevoke(device, 1001L)).thenReturn(true);

        Map<String, Object> result = deviceSyncService.revokeUserPermission("DEV001", 1001L);

        assertNotNull(result);
        assertEquals("DEV001", result.get("deviceId"));
        assertEquals(1001L, result.get("userId"));
        assertEquals("SUCCESS", result.get("revokeStatus"));
        verify(deviceProtocolClient, times(1)).performDeviceUserRevoke(device, 1001L);
    }

    @Test
    @DisplayName("获取设备用户列表-设备不存在返回空列表")
    void getDeviceUsers_deviceNotFound_returnsEmptyList() {
        when(deviceDao.selectById("DEV001")).thenReturn(null);
        List<String> users = deviceSyncService.getDeviceUsers("DEV001");
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(deviceProtocolClient, never()).performDeviceUserQuery(any());
    }

    @Test
    @DisplayName("获取设备用户列表-成功调用协议客户端")
    void getDeviceUsers_success_delegatesToProtocolClient() {
        DeviceEntity device = new DeviceEntity();
        device.setId(1L);
        when(deviceDao.selectById("DEV001")).thenReturn(device);
        when(deviceProtocolClient.performDeviceUserQuery(device)).thenReturn(List.of("1001", "1002"));

        List<String> users = deviceSyncService.getDeviceUsers("DEV001");

        assertEquals(List.of("1001", "1002"), users);
        verify(deviceProtocolClient, times(1)).performDeviceUserQuery(device);
    }

    @Test
    @DisplayName("同步业务属性-属性为空返回SKIPPED")
    void syncBusinessAttributes_emptyAttributes_skipped() {
        DeviceEntity device = new DeviceEntity();
        device.setId(1L);
        when(deviceDao.selectById("DEV001")).thenReturn(device);

        Map<String, Object> result = deviceSyncService.syncBusinessAttributes("DEV001", Map.of());

        assertEquals("DEV001", result.get("deviceId"));
        assertEquals("SKIPPED", result.get("syncStatus"));
        verify(deviceProtocolClient, never()).performDeviceAttributeSync(any(), anyMap());
    }

    @Test
    @DisplayName("健康检查-设备不存在返回UNKNOWN")
    void checkDeviceHealth_deviceNotFound_returnsUnknown() {
        when(deviceDao.selectById("DEV001")).thenReturn(null);
        Map<String, Object> result = deviceSyncService.checkDeviceHealth("DEV001");
        assertEquals("DEV001", result.get("deviceId"));
        assertEquals("UNKNOWN", result.get("healthStatus"));
        verify(deviceProtocolClient, never()).performDeviceHealthCheck(any());
    }

    @Test
    @DisplayName("性能指标-业务异常降级返回默认值")
    void getDeviceMetrics_businessException_fallbackDefaults() {
        DeviceEntity device = new DeviceEntity();
        device.setId(1L);
        when(deviceDao.selectById("DEV001")).thenReturn(device);
        when(deviceProtocolClient.performDeviceMetricsCollection(device))
                .thenThrow(new net.lab1024.sa.common.exception.BusinessException("E", "x"));

        Map<String, Object> result = deviceSyncService.getDeviceMetrics("DEV001");

        assertEquals("DEV001", result.get("deviceId"));
        assertEquals(0.0, result.get("cpuUsage"));
        assertEquals(0L, result.get("memoryUsage"));
    }

    @Test
    @DisplayName("心跳-更新设备最后在线时间并返回OK")
    void processHeartbeat_updatesLastOnlineTime() {
        DeviceEntity device = new DeviceEntity();
        device.setId(1L);
        device.setLastOnlineTime(LocalDateTime.now().minusDays(1));
        when(deviceDao.selectById("DEV001")).thenReturn(device);
        when(deviceDao.updateById(any(DeviceEntity.class))).thenReturn(1);

        Map<String, Object> result = deviceSyncService.processHeartbeat("DEV001", new HashMap<>());

        assertEquals("OK", result.get("status"));
        assertEquals("DEV001", result.get("deviceId"));
        verify(deviceDao, times(1)).updateById(any(DeviceEntity.class));
    }
}
