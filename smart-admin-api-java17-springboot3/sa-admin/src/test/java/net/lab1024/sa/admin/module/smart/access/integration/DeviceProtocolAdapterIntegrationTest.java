package net.lab1024.sa.admin.module.smart.access.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolAdapter;
import net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolAdapterFactory;
import net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolException;

/**
 * 设备协议适配器集成测试
 * <p>
 * 测试协议适配器在实际业务场景中的使用
 * - 协议适配器工厂测试
 * - 多协议切换测试
 * - 异常处理测试
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("设备协议适配器集成测试")
class DeviceProtocolAdapterIntegrationTest {

    @Resource
    private DeviceProtocolAdapterFactory protocolAdapterFactory;

    /**
     * 测试协议适配器工厂 - TCP协议
     */
    @Test
    @DisplayName("测试协议适配器工厂 - TCP协议")
    void testProtocolAdapterFactory_TCP() {
        AccessDeviceEntity device = new AccessDeviceEntity();
        device.setAccessDeviceId(1L);
        device.setIpAddress("192.168.1.100");
        device.setPort(8080);
        device.setProtocol("TCP");
        device.setOnlineStatus(1);

        DeviceProtocolAdapter adapter = protocolAdapterFactory.getAdapter(device);
        assertNotNull(adapter, "TCP协议适配器不应该为null");
        assertEquals(DeviceProtocolAdapter.ProtocolType.TCP, adapter.getProtocolType(), "协议类型应该是TCP");
    }

    /**
     * 测试协议适配器工厂 - HTTP协议
     */
    @Test
    @DisplayName("测试协议适配器工厂 - HTTP协议")
    void testProtocolAdapterFactory_HTTP() {
        AccessDeviceEntity device = new AccessDeviceEntity();
        device.setAccessDeviceId(2L);
        device.setIpAddress("192.168.1.101");
        device.setPort(80);
        device.setProtocol("HTTP");
        device.setOnlineStatus(1);

        DeviceProtocolAdapter adapter = protocolAdapterFactory.getAdapter(device);
        assertNotNull(adapter, "HTTP协议适配器不应该为null");
        assertEquals(DeviceProtocolAdapter.ProtocolType.HTTP, adapter.getProtocolType(), "协议类型应该是HTTP");
    }

    /**
     * 测试协议适配器工厂 - HTTPS协议
     */
    @Test
    @DisplayName("测试协议适配器工厂 - HTTPS协议")
    void testProtocolAdapterFactory_HTTPS() {
        AccessDeviceEntity device = new AccessDeviceEntity();
        device.setAccessDeviceId(3L);
        device.setIpAddress("192.168.1.102");
        device.setPort(443);
        device.setProtocol("HTTPS");
        device.setOnlineStatus(1);

        DeviceProtocolAdapter adapter = protocolAdapterFactory.getAdapter(device);
        assertNotNull(adapter, "HTTPS协议适配器不应该为null");
        assertEquals(DeviceProtocolAdapter.ProtocolType.HTTP, adapter.getProtocolType(), "HTTPS应该映射到HTTP协议类型");
    }

    /**
     * 测试协议适配器工厂 - 默认协议（TCP）
     */
    @Test
    @DisplayName("测试协议适配器工厂 - 默认协议（TCP）")
    void testProtocolAdapterFactory_Default() {
        AccessDeviceEntity device = new AccessDeviceEntity();
        device.setAccessDeviceId(4L);
        device.setIpAddress("192.168.1.103");
        device.setPort(8080);
        device.setProtocol(null); // 空协议，应该默认使用TCP
        device.setOnlineStatus(1);

        DeviceProtocolAdapter adapter = protocolAdapterFactory.getAdapter(device);
        assertNotNull(adapter, "默认协议适配器不应该为null");
        assertEquals(DeviceProtocolAdapter.ProtocolType.TCP, adapter.getProtocolType(), "空协议应该默认使用TCP");
    }

    /**
     * 测试协议适配器工厂 - 不支持的协议
     */
    @Test
    @DisplayName("测试协议适配器工厂 - 不支持的协议")
    void testProtocolAdapterFactory_Unsupported() {
        AccessDeviceEntity device = new AccessDeviceEntity();
        device.setAccessDeviceId(5L);
        device.setIpAddress("192.168.1.104");
        device.setPort(8080);
        device.setProtocol("MQTT"); // MQTT协议尚未实现
        device.setOnlineStatus(1);

        assertThrows(DeviceProtocolException.class, () -> {
            protocolAdapterFactory.getAdapter(device);
        }, "不支持的协议应该抛出异常");
    }

    /**
     * 测试协议适配器工厂 - 空设备
     */
    @Test
    @DisplayName("测试协议适配器工厂 - 空设备")
    void testProtocolAdapterFactory_NullDevice() {
        assertThrows(DeviceProtocolException.class, () -> {
            protocolAdapterFactory.getAdapter(null);
        }, "空设备应该抛出异常");
    }

    /**
     * 测试获取支持的协议类型列表
     */
    @Test
    @DisplayName("测试获取支持的协议类型列表")
    void testGetSupportedProtocolTypes() {
        var supportedTypes = protocolAdapterFactory.getSupportedProtocolTypes();
        assertNotNull(supportedTypes, "支持的协议类型列表不应该为null");
        assertTrue(supportedTypes.contains(DeviceProtocolAdapter.ProtocolType.TCP), "应该支持TCP协议");
        assertTrue(supportedTypes.contains(DeviceProtocolAdapter.ProtocolType.HTTP), "应该支持HTTP协议");
    }

    /**
     * 测试协议类型是否支持
     */
    @Test
    @DisplayName("测试协议类型是否支持")
    void testIsProtocolSupported() {
        assertTrue(protocolAdapterFactory.isProtocolSupported(DeviceProtocolAdapter.ProtocolType.TCP), "TCP协议应该支持");
        assertTrue(protocolAdapterFactory.isProtocolSupported(DeviceProtocolAdapter.ProtocolType.HTTP), "HTTP协议应该支持");
        assertFalse(protocolAdapterFactory.isProtocolSupported(DeviceProtocolAdapter.ProtocolType.MQTT), "MQTT协议应该不支持");
    }
}
