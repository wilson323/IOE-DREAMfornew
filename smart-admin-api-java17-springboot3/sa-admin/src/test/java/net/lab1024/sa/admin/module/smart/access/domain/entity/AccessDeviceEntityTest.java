package net.lab1024.sa.admin.module.smart.access.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AccessDeviceEntity 单元测试
 * <p>
 * 测试门禁设备实体的字段验证、业务方法和数据完整性
 * 严格遵循repowiki规范：单元测试覆盖率≥90%
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁设备实体测试")
class AccessDeviceEntityTest {

    private AccessDeviceEntity accessDevice;

    @BeforeEach
    void setUp() {
        accessDevice = new AccessDeviceEntity();

        // 基础测试数据
        accessDevice.setAccessDeviceId(1L);
        accessDevice.setDeviceId(1L);
        accessDevice.setAreaId(1L);
        accessDevice.setAccessDeviceType(1);
        accessDevice.setManufacturer("测试厂商");
        accessDevice.setDeviceModel("TEST-001");
        accessDevice.setSerialNumber("SN001");
        accessDevice.setProtocol("TCP");
        accessDevice.setIpAddress("192.168.1.100");
        accessDevice.setPort(8080);
        accessDevice.setCommKey("test_key_123");
        accessDevice.setDirection(2);
        accessDevice.setOpenMethod(1);
        accessDevice.setOpenDelay(3);
        accessDevice.setValidTime(5);
        accessDevice.setRemoteOpenEnabled(1);
        accessDevice.setAntiPassbackEnabled(0);
        accessDevice.setMultiPersonEnabled(0);
        accessDevice.setDoorSensorEnabled(1);
        accessDevice.setDoorSensorStatus(0);
        accessDevice.setOnlineStatus(1);
        accessDevice.setLastCommTime(LocalDateTime.now());
        accessDevice.setWorkMode(1);
        accessDevice.setFirmwareVersion("1.0.0");
        accessDevice.setHardwareVersion("2.0.0");
        accessDevice.setHeartbeatInterval(60);
        accessDevice.setLastHeartbeatTime(LocalDateTime.now());
        accessDevice.setEnabled(1);
        accessDevice.setInstallLocation("测试位置");
        accessDevice.setLongitude(new BigDecimal("116.397128"));
        accessDevice.setLatitude(new BigDecimal("39.916527"));
        accessDevice.setDevicePhoto("/photos/device1.jpg");
        accessDevice.setMaintenancePerson("张三");
        accessDevice.setMaintenancePhone("13800138000");
        accessDevice.setLastMaintenanceTime(LocalDateTime.now().minusDays(1));
        accessDevice.setNextMaintenanceTime(LocalDateTime.now().plusDays(30));
        accessDevice.setRemark("测试设备");
    }

    @Test
    @DisplayName("测试基础字段设置和获取")
    void testBasicFields() {
        // 验证基础字段
        assertEquals(1L, accessDevice.getAccessDeviceId());
        assertEquals(1L, accessDevice.getDeviceId());
        assertEquals(1L, accessDevice.getAreaId());
        assertEquals(1, accessDevice.getAccessDeviceType());
        assertEquals("测试厂商", accessDevice.getManufacturer());
        assertEquals("TEST-001", accessDevice.getDeviceModel());
        assertEquals("SN001", accessDevice.getSerialNumber());
        assertEquals("TCP", accessDevice.getProtocol());
        assertEquals("192.168.1.100", accessDevice.getIpAddress());
        assertEquals(8080, accessDevice.getPort());
        assertEquals("test_key_123", accessDevice.getCommKey());
    }

    @Test
    @DisplayName("测试设备方向字段")
    void testDirectionField() {
        // 测试所有有效的方向值
        Integer[] validDirections = {0, 1, 2}; // 0:单向进入 1:单向外出 2:双向

        for (Integer direction : validDirections) {
            accessDevice.setDirection(direction);
            assertEquals(direction, accessDevice.getDirection(),
                "设备方向 " + direction + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试开门方式字段")
    void testOpenMethodField() {
        // 测试所有有效的开门方式
        Integer[] validMethods = {1, 2, 3, 4, 5, 6}; // 1:刷卡 2:密码 3:指纹 4:人脸 5:二维码 6:组合方式

        for (Integer method : validMethods) {
            accessDevice.setOpenMethod(method);
            assertEquals(method, accessDevice.getOpenMethod(),
                "开门方式 " + method + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试在线状态业务方法")
    void testIsOnlineMethod() {
        // 测试在线状态
        accessDevice.setOnlineStatus(1);
        assertTrue(accessDevice.isOnline(), "在线状态为1时，isOnline应该返回true");

        accessDevice.setOnlineStatus(0);
        assertFalse(accessDevice.isOnline(), "在线状态为0时，isOnline应该返回false");

        accessDevice.setOnlineStatus(null);
        assertFalse(accessDevice.isOnline(), "在线状态为null时，isOnline应该返回false");
    }

    @Test
    @DisplayName("测试启用状态业务方法")
    void testIsEnabledMethod() {
        // 测试启用状态
        accessDevice.setEnabled(1);
        assertTrue(accessDevice.isEnabled(), "启用状态为1时，isEnabled应该返回true");

        accessDevice.setEnabled(0);
        assertFalse(accessDevice.isEnabled(), "启用状态为0时，isEnabled应该返回false");

        accessDevice.setEnabled(null);
        assertFalse(accessDevice.isEnabled(), "启用状态为null时，isEnabled应该返回false");
    }

    @Test
    @DisplayName("测试设备类型名称业务方法")
    void testGetAccessDeviceTypeName() {
        // 测试所有设备类型名称
        accessDevice.setAccessDeviceType(1);
        assertEquals("门禁机", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(2);
        assertEquals("读卡器", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(3);
        assertEquals("指纹机", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(4);
        assertEquals("人脸识别机", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(5);
        assertEquals("密码键盘", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(6);
        assertEquals("三辊闸", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(7);
        assertEquals("翼闸", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(8);
        assertEquals("摆闸", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(9);
        assertEquals("其他", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(null);
        assertEquals("未知", accessDevice.getAccessDeviceTypeName());

        accessDevice.setAccessDeviceType(99);
        assertEquals("未知", accessDevice.getAccessDeviceTypeName());
    }

    @Test
    @DisplayName("测试工作模式名称业务方法")
    void testGetWorkModeName() {
        // 测试所有工作模式名称
        accessDevice.setWorkMode(1);
        assertEquals("正常模式", accessDevice.getWorkModeName());

        accessDevice.setWorkMode(2);
        assertEquals("维护模式", accessDevice.getWorkModeName());

        accessDevice.setWorkMode(3);
        assertEquals("紧急模式", accessDevice.getWorkModeName());

        accessDevice.setWorkMode(4);
        assertEquals("锁闭模式", accessDevice.getWorkModeName());

        accessDevice.setWorkMode(null);
        assertEquals("未知", accessDevice.getWorkModeName());

        accessDevice.setWorkMode(99);
        assertEquals("未知", accessDevice.getWorkModeName());
    }

    @Test
    @DisplayName("测试心跳超时业务方法")
    void testIsHeartbeatTimeout() {
        LocalDateTime now = LocalDateTime.now();

        // 测试从未有心跳的情况
        accessDevice.setLastHeartbeatTime(null);
        assertTrue(accessDevice.isHeartbeatTimeout(), "从未有心跳时应该超时");

        // 测试心跳时间正常（最近）
        accessDevice.setLastHeartbeatTime(now.minusMinutes(2));
        assertFalse(accessDevice.isHeartbeatTimeout(), "2分钟前的心跳不应该超时");

        // 测试心跳时间超时（超过5分钟）
        accessDevice.setLastHeartbeatTime(now.minusMinutes(6));
        assertTrue(accessDevice.isHeartbeatTimeout(), "6分钟前的心跳应该超时");

        // 测试边界情况（正好5分钟）
        accessDevice.setLastHeartbeatTime(now.minusMinutes(5));
        assertTrue(accessDevice.isHeartbeatTimeout(), "5分钟前的心跳应该超时");
    }

    @Test
    @DisplayName("测试是否需要维护业务方法")
    void testNeedsMaintenance() {
        LocalDateTime now = LocalDateTime.now();

        // 测试设备离线超过24小时
        accessDevice.setOnlineStatus(0);
        accessDevice.setLastHeartbeatTime(now.minusHours(25));
        assertTrue(accessDevice.needsMaintenance(), "离线超过24小时应该需要维护");

        // 测试设备离线未超过24小时
        accessDevice.setLastHeartbeatTime(now.minusHours(23));
        assertFalse(accessDevice.needsMaintenance(), "离线未超过24小时不应该需要维护");

        // 测试设备在线
        accessDevice.setOnlineStatus(1);
        accessDevice.setLastHeartbeatTime(now.minusMinutes(10));
        assertFalse(accessDevice.needsMaintenance(), "设备在线不应该需要维护");

        // 测试设备故障状态
        accessDevice.setStatus("FAULT");
        assertTrue(accessDevice.needsMaintenance(), "设备故障状态应该需要维护");

        // 测试设备正常状态
        accessDevice.setStatus("NORMAL");
        assertFalse(accessDevice.needsMaintenance(), "设备正常状态不应该需要维护");
    }

    @Test
    @DisplayName("测试门磁状态字段")
    void testDoorSensorStatusField() {
        // 测试所有有效的门磁状态
        Integer[] validStatuses = {0, 1, 2}; // 0:关闭 1:打开 2:故障

        for (Integer status : validStatuses) {
            accessDevice.setDoorSensorStatus(status);
            assertEquals(status, accessDevice.getDoorSensorStatus(),
                "门磁状态 " + status + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试工作模式字段")
    void testWorkModeField() {
        // 测试所有有效的工作模式
        Integer[] validModes = {1, 2, 3, 4}; // 1:正常模式 2:维护模式 3:紧急模式 4:锁闭模式

        for (Integer mode : validModes) {
            accessDevice.setWorkMode(mode);
            assertEquals(mode, accessDevice.getWorkMode(),
                "工作模式 " + mode + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试开关标志字段")
    void testFlagFields() {
        // 测试各种开关标志
        accessDevice.setRemoteOpenEnabled(0);
        assertEquals(0, accessDevice.getRemoteOpenEnabled());

        accessDevice.setRemoteOpenEnabled(1);
        assertEquals(1, accessDevice.getRemoteOpenEnabled());

        accessDevice.setAntiPassbackEnabled(1);
        assertEquals(1, accessDevice.getAntiPassbackEnabled());

        accessDevice.setMultiPersonEnabled(1);
        assertEquals(1, accessDevice.getMultiPersonEnabled());

        accessDevice.setDoorSensorEnabled(1);
        assertEquals(1, accessDevice.getDoorSensorEnabled());
    }

    @Test
    @DisplayName("测试时间相关字段")
    void testTimeFields() {
        LocalDateTime now = LocalDateTime.now();

        accessDevice.setLastCommTime(now);
        assertEquals(now, accessDevice.getLastCommTime());

        accessDevice.setLastHeartbeatTime(now);
        assertEquals(now, accessDevice.getLastHeartbeatTime());

        accessDevice.setLastMaintenanceTime(now);
        assertEquals(now, accessDevice.getLastMaintenanceTime());

        accessDevice.setNextMaintenanceTime(now);
        assertEquals(now, accessDevice.getNextMaintenanceTime());
    }

    @Test
    @DisplayName("测试数值字段边界值")
    void testNumericFieldBoundaries() {
        // 测试端口范围
        accessDevice.setPort(0);
        assertEquals(0, accessDevice.getPort());

        accessDevice.setPort(65535);
        assertEquals(65535, accessDevice.getPort());

        // 测试心跳间隔
        accessDevice.setHeartbeatInterval(1);
        assertEquals(1, accessDevice.getHeartbeatInterval());

        accessDevice.setHeartbeatInterval(3600);
        assertEquals(3600, accessDevice.getHeartbeatInterval());

        // 测试开门延时
        accessDevice.setOpenDelay(0);
        assertEquals(0, accessDevice.getOpenDelay());

        accessDevice.setOpenDelay(60);
        assertEquals(60, accessDevice.getOpenDelay());

        // 测试有效时间
        accessDevice.setValidTime(1);
        assertEquals(1, accessDevice.getValidTime());

        accessDevice.setValidTime(300);
        assertEquals(300, accessDevice.getValidTime());
    }

    @Test
    @DisplayName("测试坐标精度")
    void testCoordinatePrecision() {
        BigDecimal longitude = new BigDecimal("116.397128123456789");
        BigDecimal latitude = new BigDecimal("39.916527987654321");

        accessDevice.setLongitude(longitude);
        accessDevice.setLatitude(latitude);

        assertEquals(longitude, accessDevice.getLongitude());
        assertEquals(latitude, accessDevice.getLatitude());
    }

    @Test
    @DisplayName("测试非数据库字段")
    void testTransientFields() {
        // 测试展示用字段
        accessDevice.setAreaName("测试区域");
        assertEquals("测试区域", accessDevice.getAreaName());

        accessDevice.setAccessDeviceTypeName("门禁机");
        assertEquals("门禁机", accessDevice.getAccessDeviceTypeName());

        accessDevice.setWorkModeName("正常模式");
        assertEquals("正常模式", accessDevice.getWorkModeName());

        accessDevice.setMaintenancePersonName("张三");
        assertEquals("张三", accessDevice.getMaintenancePersonName());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        // 验证toString方法不会抛出异常
        String toString = accessDevice.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("AccessDeviceEntity"));
        assertTrue(toString.contains("manufacturer=测试厂商"));
    }

    @Test
    @DisplayName("测试equals和hashCode方法")
    void testEqualsAndHashCode() {
        AccessDeviceEntity device1 = new AccessDeviceEntity();
        device1.setAccessDeviceId(1L);
        device1.setSerialNumber("SN001");

        AccessDeviceEntity device2 = new AccessDeviceEntity();
        device2.setAccessDeviceId(1L);
        device2.setSerialNumber("SN001");

        AccessDeviceEntity device3 = new AccessDeviceEntity();
        device3.setAccessDeviceId(2L);
        device3.setSerialNumber("SN002");

        // 测试相等性
        assertEquals(device1, device2, "相同ID的设备应该相等");
        assertEquals(device1.hashCode(), device2.hashCode(), "相等对象的hashCode应该相同");

        assertNotEquals(device1, device3, "不同ID的设备应该不相等");
        assertNotEquals(device1.hashCode(), device3.hashCode(), "不相等对象的hashCode可能不同");
    }
}