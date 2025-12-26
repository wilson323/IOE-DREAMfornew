package net.lab1024.sa.video.edge.communication.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.lab1024.sa.video.edge.EdgeConfig;
import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.model.HardwareSpec;

/**
 * EdgeCommunicationManagerImpl 单元测试
 * <p>
 * 测试目标：
 * - 边缘设备连接管理
 * - 模型同步
 * - 心跳检测
 * - 断线重连
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
@DisplayName("EdgeCommunicationManagerImpl 单元测试")
class EdgeCommunicationManagerImplTest {

    private EdgeCommunicationManagerImpl manager;
    private EdgeConfig config;

    @BeforeEach
    void setUp() {
        config = new EdgeConfig();
        config.setModelSyncEnabled(true);
        config.setHeartbeatInterval(10000L);
        config.setConnectionRetryCount(3);
        config.setConnectionRetryInterval(5000L);

        manager = new EdgeCommunicationManagerImpl(config);
    }

    @AfterEach
    void tearDown() {
        if (manager != null) {
            manager.shutdown();
        }
    }

    @Test
    @DisplayName("测试连接边缘设备-成功场景")
    void testConnectToDevice_Success() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_001");

        // When
        boolean result = manager.connectToDevice(device);

        // Then
        assertTrue(result);
        assertTrue(manager.isConnected("EDGE_001"));
    }

    @Test
    @DisplayName("测试连接边缘设备-设备ID为空")
    void testConnectToDevice_EmptyDeviceId() {
        // Given
        EdgeDevice device = new EdgeDevice();
        device.setDeviceId("");

        // When
        boolean result = manager.connectToDevice(device);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试连接边缘设备-设备为null")
    void testConnectToDevice_NullDevice() {
        // When
        boolean result = manager.connectToDevice(null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试连接边缘设备-重复连接")
    void testConnectToDevice_DuplicateConnection() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_001");
        manager.connectToDevice(device);

        // When
        boolean result = manager.connectToDevice(device);

        // Then
        assertTrue(result);
        assertTrue(manager.isConnected("EDGE_001"));
    }

    @Test
    @DisplayName("测试断开边缘设备连接-成功场景")
    void testDisconnectFromDevice_Success() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_001");
        manager.connectToDevice(device);

        // When
        manager.disconnectFromDevice(device);

        // Then
        assertFalse(manager.isConnected("EDGE_001"));
    }

    @Test
    @DisplayName("测试断开连接-设备未连接")
    void testDisconnectFromDevice_DeviceNotConnected() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_999");

        // When
        manager.disconnectFromDevice(device);

        // Then
        assertFalse(manager.isConnected("EDGE_999"));
    }

    @Test
    @DisplayName("测试断开连接-设备为null")
    void testDisconnectFromDevice_NullDevice() {
        // When & Then - 不应该抛出异常
        manager.disconnectFromDevice(null);
    }

    @Test
    @DisplayName("测试检查设备连接状态-已连接")
    void testIsConnected_True() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_001");
        manager.connectToDevice(device);

        // When
        boolean result = manager.isConnected("EDGE_001");

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("测试检查设备连接状态-未连接")
    void testIsConnected_False() {
        // When
        boolean result = manager.isConnected("EDGE_999");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试发送心跳-成功场景")
    void testSendHeartbeat_Success() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_001");
        manager.connectToDevice(device);

        // When
        boolean result = manager.sendHeartbeat("EDGE_001");

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("测试发送心跳-设备未连接")
    void testSendHeartbeat_DeviceNotConnected() {
        // When
        boolean result = manager.sendHeartbeat("EDGE_999");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试同步模型到边缘设备-成功场景")
    void testSyncModel_Success() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_001");
        manager.connectToDevice(device);

        String modelType = "FACE_DETECTION_V1";
        byte[] modelData = new byte[] { 1, 2, 3, 4, 5 };

        // When
        boolean result = manager.syncModel("EDGE_001", modelType, modelData);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("测试同步模型-设备未连接")
    void testSyncModel_DeviceNotConnected() {
        // Given
        String modelType = "FACE_DETECTION_V1";
        byte[] modelData = new byte[] { 1, 2, 3 };

        // When
        boolean result = manager.syncModel("EDGE_999", modelType, modelData);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试同步模型-模型数据为空")
    void testSyncModel_NullModelData() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_001");
        manager.connectToDevice(device);
        String modelType = "FACE_DETECTION_V1";

        // When
        boolean result = manager.syncModel("EDGE_001", modelType, null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试同步模型-模型类型为空")
    void testSyncModel_EmptyModelType() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_001");
        manager.connectToDevice(device);
        byte[] modelData = new byte[] { 1, 2, 3 };

        // When
        boolean result = manager.syncModel("EDGE_001", "", modelData);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试同步模型-模型同步未启用")
    void testSyncModel_ModelSyncDisabled() {
        // Given
        config.setModelSyncEnabled(false);
        EdgeDevice device = createMockEdgeDevice("EDGE_001");
        manager.connectToDevice(device);
        String modelType = "FACE_DETECTION_V1";
        byte[] modelData = new byte[] { 1, 2, 3 };

        // When
        boolean result = manager.syncModel("EDGE_001", modelType, modelData);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试获取连接统计-成功场景")
    void testGetConnectionStatistics_Success() {
        // Given
        manager.connectToDevice(createMockEdgeDevice("EDGE_001"));
        manager.connectToDevice(createMockEdgeDevice("EDGE_002"));

        // When
        Map<String, Object> stats = manager.getConnectionStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalConnections"));
        assertTrue(stats.containsKey("activeConnections"));
        assertEquals(2, stats.get("totalConnections"));
        assertEquals(2, stats.get("activeConnections"));
    }

    @Test
    @DisplayName("测试重连边缘设备-成功场景")
    void testReconnect_Success() {
        // Given
        EdgeDevice device = createMockEdgeDevice("EDGE_001");
        manager.connectToDevice(device);
        manager.disconnectFromDevice(device);

        // When
        boolean result = manager.reconnect("EDGE_001");

        // Then
        assertTrue(result);
        assertTrue(manager.isConnected("EDGE_001"));
    }

    @Test
    @DisplayName("测试重连-设备从未连接过")
    void testReconnect_NeverConnected() {
        // When
        boolean result = manager.reconnect("EDGE_999");

        // Then
        assertFalse(result);
    }

    /**
     * 创建模拟边缘设备
     *
     * @param deviceId 设备ID
     * @return 边缘设备对象
     */
    private EdgeDevice createMockEdgeDevice(String deviceId) {
        EdgeDevice device = new EdgeDevice();
        device.setDeviceId(deviceId);
        device.setDeviceName("测试边缘设备-" + deviceId);
        device.setDeviceType("EDGE_CAMERA");
        device.setIpAddress("192.168.1.100");
        device.setPort(8080);
        device.setStatus("ONLINE");

        HardwareSpec hardwareSpec = new HardwareSpec();
        hardwareSpec.setCpuModel("Intel i7");
        hardwareSpec.setGpuModel("NVIDIA RTX 3060");
        hardwareSpec.setMemorySize(16);
        hardwareSpec.setStorageSize(512);
        hardwareSpec.setCpuCores(8);
        hardwareSpec.setMemoryMB(16384);
        hardwareSpec.setGpu(true);
        device.setHardwareSpec(hardwareSpec);

        return device;
    }
}
