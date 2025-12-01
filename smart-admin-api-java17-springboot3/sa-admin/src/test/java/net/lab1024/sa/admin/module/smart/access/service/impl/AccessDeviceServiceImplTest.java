/*
 * 门禁设备服务实现类单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.smart.access.service.impl;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.smart.access.dao.AccessDeviceDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessDeviceForm;
import net.lab1024.sa.admin.module.smart.access.domain.vo.AccessDeviceDetailVO;
import net.lab1024.sa.admin.module.smart.access.manager.AccessDeviceManager;
import net.lab1024.sa.admin.module.smart.access.service.AccessDeviceService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁设备服务实现类单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁设备服务实现类单元测试")
class AccessDeviceServiceImplTest {

    @Mock
    private AccessDeviceDao accessDeviceDao;

    @Mock
    private AccessDeviceManager accessDeviceManager;

    @InjectMocks
    private AccessDeviceServiceImpl accessDeviceService;

    private AccessDeviceEntity testDevice;
    private AccessDeviceForm testDeviceForm;
    private PageParam pageParam;

    @BeforeEach
    void setUp() {
        // 测试数据初始化
        testDevice = AccessDeviceEntity.builder()
                .deviceId(1L)
                .deviceName("测试设备")
                .deviceCode("TEST_DEVICE_001")
                .deviceType("CARD_READER")
                .deviceModel("TR-5000")
                .manufacturer("测试厂商")
                .areaId(1L)
                .location("测试位置")
                .ipAddress("192.168.1.100")
                .port(8080)
                .deviceStatus("ONLINE")
                .lastHeartbeat(LocalDateTime.now())
                .installTime(LocalDateTime.now())
                .status(1)
                .createUserId(1001L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        testDeviceForm = AccessDeviceForm.builder()
                .deviceId(1L)
                .deviceName("测试设备")
                .deviceCode("TEST_DEVICE_001")
                .deviceType("CARD_READER")
                .deviceModel("TR-5000")
                .manufacturer("测试厂商")
                .areaId(1L)
                .location("测试位置")
                .ipAddress("192.168.1.100")
                .port(8080)
                .status(1)
                .build();

        pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(20);
    }

    @Test
    @DisplayName("测试获取设备列表 - 成功")
    void testGetDeviceList_Success() {
        // Arrange
        List<AccessDeviceEntity> deviceList = Arrays.asList(testDevice);
        when(accessDeviceDao.selectList(any())).thenReturn(deviceList);
        when(accessDeviceDao.selectCount(any())).thenReturn(1);

        // Act
        PageResult<AccessDeviceEntity> result = accessDeviceService.getDeviceList(pageParam);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(20, result.getPageSize());
        assertEquals(1, result.getList().size());
        assertEquals("测试设备", result.getList().get(0).getDeviceName());

        verify(accessDeviceDao, times(1)).selectList(any());
        verify(accessDeviceDao, times(1)).selectCount(any());
    }

    @Test
    @DisplayName("测试获取设备详情 - 成功")
    void testGetDeviceDetail_Success() {
        // Arrange
        when(accessDeviceDao.selectDetailById(1L)).thenReturn(
                AccessDeviceDetailVO.builder()
                        .deviceId(1L)
                        .deviceName("测试设备")
                        .deviceCode("TEST_DEVICE_001")
                        .deviceType("CARD_READER")
                        .deviceModel("TR-5000")
                        .manufacturer("测试厂商")
                        .areaId(1L)
                        .location("测试位置")
                        .ipAddress("192.168.1.100")
                        .port(8080)
                        .deviceStatus("ONLINE")
                        .lastHeartbeat(LocalDateTime.now())
                        .build()
        );

        // Act
        AccessDeviceDetailVO result = accessDeviceService.getDeviceDetail(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getDeviceId());
        assertEquals("测试设备", result.getDeviceName());
        assertEquals("TEST_DEVICE_001", result.getDeviceCode());
        assertEquals("CARD_READER", result.getDeviceType());

        verify(accessDeviceDao, times(1)).selectDetailById(1L);
    }

    @Test
    @DisplayName("测试获取设备详情 - 设备不存在")
    void testGetDeviceDetail_NotFound() {
        // Arrange
        when(accessDeviceDao.selectDetailById(999L)).thenReturn(null);

        // Act
        AccessDeviceDetailVO result = accessDeviceService.getDeviceDetail(999L);

        // Assert
        assertNull(result);
        verify(accessDeviceDao, times(1)).selectDetailById(999L);
    }

    @Test
    @DisplayName("测试添加设备 - 成功")
    void testAddDevice_Success() {
        // Arrange
        AccessDeviceForm newDeviceForm = AccessDeviceForm.builder()
                .deviceName("新设备")
                .deviceCode("NEW_DEVICE_001")
                .deviceType("FACE_RECOGNITION")
                .deviceModel("FR-6000")
                .manufacturer("新厂商")
                .areaId(1L)
                .location("新位置")
                .ipAddress("192.168.1.200")
                .port(8081)
                .status(1)
                .build();

        doNothing().when(accessDeviceManager).addDevice(any(AccessDeviceEntity.class));

        // Act
        ResponseDTO<String> result = accessDeviceService.addDevice(newDeviceForm);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("添加成功", result.getMsg());

        verify(accessDeviceManager, times(1)).addDevice(any(AccessDeviceEntity.class));
    }

    @Test
    @DisplayName("测试添加设备 - 设备名称为空")
    void testAddDevice_EmptyName() {
        // Arrange
        AccessDeviceForm invalidForm = AccessDeviceForm.builder()
                .deviceName("")
                .deviceCode("NEW_DEVICE_001")
                .deviceType("FACE_RECOGNITION")
                .build();

        // Act
        ResponseDTO<String> result = accessDeviceService.addDevice(invalidForm);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("设备名称不能为空"));
    }

    @Test
    @DisplayName("测试添加设备 - 设备编码为空")
    void testAddDevice_EmptyCode() {
        // Arrange
        AccessDeviceForm invalidForm = AccessDeviceForm.builder()
                .deviceName("测试设备")
                .deviceCode("")
                .deviceType("FACE_RECOGNITION")
                .build();

        // Act
        ResponseDTO<String> result = accessDeviceService.addDevice(invalidForm);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("设备编码不能为空"));
    }

    @Test
    @DisplayName("测试添加设备 - IP地址格式错误")
    void testAddDevice_InvalidIpAddress() {
        // Arrange
        AccessDeviceForm invalidForm = AccessDeviceForm.builder()
                .deviceName("测试设备")
                .deviceCode("TEST_DEVICE_001")
                .deviceType("CARD_READER")
                .ipAddress("invalid.ip.address")
                .build();

        // Act
        ResponseDTO<String> result = accessDeviceService.addDevice(invalidForm);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("IP地址格式不正确"));
    }

    @Test
    @DisplayName("测试更新设备 - 成功")
    void testUpdateDevice_Success() {
        // Arrange
        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        doNothing().when(accessDeviceManager).updateDevice(any(AccessDeviceEntity.class));

        // Act
        ResponseDTO<String> result = accessDeviceService.updateDevice(testDeviceForm);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("更新成功", result.getMsg());

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceManager, times(1)).updateDevice(any(AccessDeviceEntity.class));
    }

    @Test
    @DisplayName("测试更新设备 - 设备不存在")
    void testUpdateDevice_NotFound() {
        // Arrange
        when(accessDeviceDao.selectById(999L)).thenReturn(null);

        AccessDeviceForm updateForm = AccessDeviceForm.builder()
                .deviceId(999L)
                .deviceName("不存在的设备")
                .build();

        // Act
        ResponseDTO<String> result = accessDeviceService.updateDevice(updateForm);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("设备不存在"));

        verify(accessDeviceDao, times(1)).selectById(999L);
        verify(accessDeviceManager, never()).updateDevice(any());
    }

    @Test
    @DisplayName("测试删除设备 - 成功")
    void testDeleteDevice_Success() {
        // Arrange
        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        doNothing().when(accessDeviceManager).deleteDevice(1L);

        // Act
        ResponseDTO<String> result = accessDeviceService.deleteDevice(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("删除成功", result.getMsg());

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceManager, times(1)).deleteDevice(1L);
    }

    @Test
    @DisplayName("测试删除设备 - 设备不存在")
    void testDeleteDevice_NotFound() {
        // Arrange
        when(accessDeviceDao.selectById(999L)).thenReturn(null);

        // Act
        ResponseDTO<String> result = accessDeviceService.deleteDevice(999L);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("设备不存在"));

        verify(accessDeviceDao, times(1)).selectById(999L);
        verify(accessDeviceManager, never()).deleteDevice(any());
    }

    @Test
    @DisplayName("测试远程控制设备 - 开门成功")
    void testControlDevice_OpenSuccess() {
        // Arrange
        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(accessDeviceManager.controlDevice(1L, "open")).thenReturn(true);

        // Act
        ResponseDTO<String> result = accessDeviceService.controlDevice(1L, "open");

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("开门成功", result.getMsg());

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceManager, times(1)).controlDevice(1L, "open");
    }

    @Test
    @DisplayName("测试远程控制设备 - 设备离线")
    void testControlDevice_DeviceOffline() {
        // Arrange
        AccessDeviceEntity offlineDevice = AccessDeviceEntity.builder()
                .deviceId(1L)
                .deviceName("离线设备")
                .deviceStatus("OFFLINE")
                .build();

        when(accessDeviceDao.selectById(1L)).thenReturn(offlineDevice);

        // Act
        ResponseDTO<String> result = accessDeviceService.controlDevice(1L, "open");

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("设备离线，无法控制"));

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceManager, never()).controlDevice(any(), any());
    }

    @Test
    @DisplayName("测试远程控制设备 - 控制失败")
    void testControlDevice_ControlFailed() {
        // Arrange
        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(accessDeviceManager.controlDevice(1L, "open")).thenReturn(false);

        // Act
        ResponseDTO<String> result = accessDeviceService.controlDevice(1L, "open");

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("控制失败"));

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceManager, times(1)).controlDevice(1L, "open");
    }

    @Test
    @DisplayName("测试获取设备状态 - 成功")
    void testGetDeviceStatus_Success() {
        // Arrange
        when(accessDeviceDao.selectStatusById(1L)).thenReturn("ONLINE");

        // Act
        ResponseDTO<String> result = accessDeviceService.getDeviceStatus(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("ONLINE", result.getData());

        verify(accessDeviceDao, times(1)).selectStatusById(1L);
    }

    @Test
    @DisplayName("测试获取设备类型列表 - 成功")
    void testGetDeviceTypes_Success() {
        // Arrange
        List<String> deviceTypes = Arrays.asList("CARD_READER", "FACE_RECOGNITION", "FINGERPRINT");
        when(accessDeviceDao.selectDeviceTypes()).thenReturn(deviceTypes);

        // Act
        ResponseDTO<List<String>> result = accessDeviceService.getDeviceTypes();

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(3, result.getData().size());
        assertTrue(result.getData().contains("CARD_READER"));

        verify(accessDeviceDao, times(1)).selectDeviceTypes();
    }

    @Test
    @DisplayName("测试批量删除设备 - 成功")
    void testBatchDeleteDevices_Success() {
        // Arrange
        List<Long> deviceIds = Arrays.asList(1L, 2L);

        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(accessDeviceDao.selectById(2L)).thenReturn(testDevice);

        doNothing().when(accessDeviceManager).batchDeleteDevices(deviceIds);

        // Act
        ResponseDTO<String> result = accessDeviceService.batchDeleteDevices(deviceIds);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("批量删除成功", result.getMsg());

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceDao, times(1)).selectById(2L);
        verify(accessDeviceManager, times(1)).batchDeleteDevices(deviceIds);
    }

    @Test
    @DisplayName("测试批量删除设备 - 部分设备不存在")
    void testBatchDeleteDevices_SomeNotFound() {
        // Arrange
        List<Long> deviceIds = Arrays.asList(1L, 999L);

        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(accessDeviceDao.selectById(999L)).thenReturn(null);

        // Act
        ResponseDTO<String> result = accessDeviceService.batchDeleteDevices(deviceIds);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("设备不存在"));

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceDao, times(1)).selectById(999L);
        verify(accessDeviceManager, never()).batchDeleteDevices(any());
    }

    @Test
    @DisplayName("测试重启设备 - 成功")
    void testRestartDevice_Success() {
        // Arrange
        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(accessDeviceManager.restartDevice(1L)).thenReturn(true);

        // Act
        ResponseDTO<String> result = accessDeviceService.restartDevice(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("重启成功", result.getMsg());

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceManager, times(1)).restartDevice(1L);
    }

    @Test
    @DisplayName("测试同步设备时间 - 成功")
    void testSyncDeviceTime_Success() {
        // Arrange
        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(accessDeviceManager.syncDeviceTime(1L)).thenReturn(true);

        // Act
        ResponseDTO<String> result = accessDeviceService.syncDeviceTime(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("时间同步成功", result.getMsg());

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceManager, times(1)).syncDeviceTime(1L);
    }

    @Test
    @DisplayName("测试获取设备日志 - 成功")
    void testGetDeviceLogs_Success() {
        // Arrange
        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(accessDeviceDao.selectLogsByDeviceId(1L, 20, 1))
                .thenReturn(Arrays.asList("日志1", "日志2"));

        // Act
        ResponseDTO<List<String>> result = accessDeviceService.getDeviceLogs(1L, 20, 1);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(2, result.getData().size());

        verify(accessDeviceDao, times(1)).selectById(1L);
        verify(accessDeviceDao, times(1)).selectLogsByDeviceId(1L, 20, 1);
    }

    @Test
    @DisplayName("测试验证设备编码唯一性 - 编码可用")
    void testValidateDeviceCode_Available() {
        // Arrange
        when(accessDeviceDao.selectCountByDeviceCode("NEW_CODE", null)).thenReturn(0);

        // Act
        ResponseDTO<Boolean> result = accessDeviceService.validateDeviceCode("NEW_CODE", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertTrue(result.getData());

        verify(accessDeviceDao, times(1)).selectCountByDeviceCode("NEW_CODE", null);
    }

    @Test
    @DisplayName("测试验证设备编码唯一性 - 编码已存在")
    void testValidateDeviceCode_AlreadyExists() {
        // Arrange
        when(accessDeviceDao.selectCountByDeviceCode("EXIST_CODE", null)).thenReturn(1);

        // Act
        ResponseDTO<Boolean> result = accessDeviceService.validateDeviceCode("EXIST_CODE", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertFalse(result.getData());

        verify(accessDeviceDao, times(1)).selectCountByDeviceCode("EXIST_CODE", null);
    }
}