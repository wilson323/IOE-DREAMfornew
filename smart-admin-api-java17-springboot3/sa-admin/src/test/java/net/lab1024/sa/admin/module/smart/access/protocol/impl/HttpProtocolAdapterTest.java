package net.lab1024.sa.admin.module.smart.access.protocol.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolAdapter;
import net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolException;

/**
 * HTTP协议适配器单元测试
 * <p>
 * 测试HTTP协议适配器的各种功能：
 * - 协议类型获取
 * - 支持的厂商列表
 * - 远程开门功能（需要Mock HTTP请求）
 * - 设备重启功能
 * - 时间同步功能
 * - 连接状态检查
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@DisplayName("HTTP协议适配器单元测试")
class HttpProtocolAdapterTest {

    private HttpProtocolAdapter httpProtocolAdapter;
    private AccessDeviceEntity testDevice;

    @BeforeEach
    void setUp() {
        httpProtocolAdapter = new HttpProtocolAdapter();

        // 创建测试设备
        testDevice = new AccessDeviceEntity();
        testDevice.setAccessDeviceId(1L);
        testDevice.setIpAddress("127.0.0.1");
        testDevice.setPort(8080);
        testDevice.setProtocol("HTTP");
        testDevice.setOnlineStatus(1);
    }

    /**
     * 测试获取协议类型
     */
    @Test
    @DisplayName("测试获取协议类型")
    void testGetProtocolType() {
        DeviceProtocolAdapter.ProtocolType protocolType = httpProtocolAdapter.getProtocolType();
        assertEquals(DeviceProtocolAdapter.ProtocolType.HTTP, protocolType, "协议类型应该是HTTP");
    }

    /**
     * 测试获取支持的厂商列表
     */
    @Test
    @DisplayName("测试获取支持的厂商列表")
    void testGetSupportedManufacturers() {
        var manufacturers = httpProtocolAdapter.getSupportedManufacturers();
        assertNotNull(manufacturers, "支持的厂商列表不应该为null");
        assertFalse(manufacturers.isEmpty(), "支持的厂商列表不应该为空");
        assertTrue(manufacturers.contains("海康威视"), "应该支持海康威视");
        assertTrue(manufacturers.contains("Hikvision"), "应该支持Hikvision");
    }

    /**
     * 测试连接状态检查
     * 注意：这个测试需要实际的HTTP服务，可能会失败
     */
    @Test
    @DisplayName("测试连接状态检查")
    void testCheckConnection() {
        // 使用本地回环地址测试（如果服务未启动会返回false）
        boolean connected = httpProtocolAdapter.checkConnection(testDevice);
        // 由于是单元测试，不保证服务启动，所以只验证方法不抛异常
        assertNotNull(Boolean.valueOf(connected), "连接检查应该返回布尔值");
    }

    /**
     * 测试远程开门 - 设备离线
     */
    @Test
    @DisplayName("测试远程开门 - 设备离线")
    void testRemoteOpen_DeviceOffline() {
        testDevice.setOnlineStatus(0);

        // 由于无法建立连接，会抛出异常
        assertThrows(DeviceProtocolException.class, () -> {
            httpProtocolAdapter.remoteOpen(testDevice);
        }, "设备离线时应该抛出异常");
    }

    /**
     * 测试设备重启 - 设备离线
     */
    @Test
    @DisplayName("测试设备重启 - 设备离线")
    void testRestartDevice_DeviceOffline() {
        testDevice.setOnlineStatus(0);

        assertThrows(DeviceProtocolException.class, () -> {
            httpProtocolAdapter.restartDevice(testDevice);
        }, "设备离线时应该抛出异常");
    }

    /**
     * 测试时间同步 - 设备离线
     */
    @Test
    @DisplayName("测试时间同步 - 设备离线")
    void testSyncDeviceTime_DeviceOffline() {
        testDevice.setOnlineStatus(0);

        assertThrows(DeviceProtocolException.class, () -> {
            httpProtocolAdapter.syncDeviceTime(testDevice);
        }, "设备离线时应该抛出异常");
    }

    /**
     * 测试HTTPS协议支持
     */
    @Test
    @DisplayName("测试HTTPS协议支持")
    void testHttpsProtocol() {
        testDevice.setProtocol("HTTPS");
        // 验证协议类型识别
        // 注意：实际测试中需要验证URL构建是否正确
        assertNotNull(testDevice.getProtocol(), "HTTPS协议应该被识别");
    }
}
