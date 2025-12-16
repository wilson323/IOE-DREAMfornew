package net.lab1024.sa.access.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.access.controller.AccessMobileController;
import net.lab1024.sa.access.dao.AccessAreaDao;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.access.service.impl.AccessDeviceServiceImpl;
import org.springframework.http.HttpMethod;

/**
 * AccessDeviceServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of AccessDeviceServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessDeviceServiceImpl Unit Test")
@SuppressWarnings("unchecked")
class AccessDeviceServiceImplTest {

    @Mock
    private AccessDeviceDao accessDeviceDao;

    @Mock
    private AccessAreaDao accessAreaDao;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AccessDeviceServiceImpl accessDeviceServiceImpl;

    private DeviceEntity mockDevice;
    private AccessDeviceVO mockDeviceVO;
    private AreaEntity mockArea;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockDevice = new DeviceEntity();
        mockDevice.setId(1L);  // 修复：DeviceEntity的id是Long类型
        mockDevice.setDeviceName("Test Device");
        mockDevice.setDeviceCode("DEV001");
        mockDevice.setDeviceType("ACCESS");
        mockDevice.setAreaId(1L);
        mockDevice.setDeviceStatus("ONLINE");  // 修复：DeviceEntity使用deviceStatus字段（String类型）
        mockDevice.setEnabledFlag(1);  // 修复：DeviceEntity使用enabledFlag字段（Integer类型）
        mockDevice.setDeletedFlag(0);

        mockDeviceVO = new AccessDeviceVO();
        mockDeviceVO.setDeviceId(1L);  // 修复：AccessDeviceVO的deviceId是Long类型
        mockDeviceVO.setDeviceName("Test Device");
        mockDeviceVO.setDeviceCode("DEV001");
        mockDeviceVO.setDeviceStatus("ONLINE");  // 修复：AccessDeviceVO使用deviceStatus字段（String类型）

        mockArea = new AreaEntity();
        mockArea.setId(1L);
        mockArea.setAreaName("Test Area");
        mockArea.setAreaCode("AREA001");
    }

    @Test
    @DisplayName("Test queryDevices - Success Scenario")
    void test_queryDevices_Success() {
        // Given
        AccessDeviceQueryForm queryForm = new AccessDeviceQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);

        Page<DeviceEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockDevice));
        page.setTotal(1);

        when(accessDeviceDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When
        ResponseDTO<PageResult<AccessDeviceVO>> result = accessDeviceServiceImpl.queryDevices(queryForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getTotal());
        assertEquals(1, result.getData().getList().size());
        verify(accessDeviceDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test queryDevices - With Keyword Filter")
    void test_queryDevices_WithKeyword() {
        // Given
        AccessDeviceQueryForm queryForm = new AccessDeviceQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);
        queryForm.setKeyword("Test");

        Page<DeviceEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockDevice));
        page.setTotal(1);

        when(accessDeviceDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When
        ResponseDTO<PageResult<AccessDeviceVO>> result = accessDeviceServiceImpl.queryDevices(queryForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(accessDeviceDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getDeviceDetail - Success Scenario")
    void test_getDeviceDetail_Success() {
        // Given
        Long deviceId = 1L;
        when(accessDeviceDao.selectById(anyLong())).thenReturn(mockDevice);

        // When
        ResponseDTO<AccessDeviceVO> result = accessDeviceServiceImpl.getDeviceDetail(deviceId);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(accessDeviceDao, times(1)).selectById(anyLong());
    }

    @Test
    @DisplayName("Test getDeviceDetail - Device Not Found")
    void test_getDeviceDetail_NotFound() {
        // Given
        Long deviceId = 999L;
        when(accessDeviceDao.selectById(anyLong())).thenReturn(null);

        // When & Then
        ResponseDTO<AccessDeviceVO> result = accessDeviceServiceImpl.getDeviceDetail(deviceId);
        assertFalse(result.getOk());
        assertEquals(ResponseDTO.error("DEVICE_NOT_FOUND", "x").getCode(), result.getCode());
        assertTrue(result.getMessage().contains("设备不存在"));
        verify(accessDeviceDao, times(1)).selectById(anyLong());
    }

    @Test
    @DisplayName("Test addDevice - Success Scenario")
    void test_addDevice_Success() {
        // Given
        AccessDeviceAddForm addForm = new AccessDeviceAddForm();
        addForm.setDeviceName("New Device");
        addForm.setDeviceCode("NEW_DEV001");
        addForm.setAreaId(1L);

        when(accessDeviceDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null); // Device code not exists
        when(gatewayServiceClient.callCommonService(anyString(), eq(HttpMethod.GET), isNull(), eq(AreaEntity.class)))
                .thenReturn(ResponseDTO.ok(mockArea));
        doAnswer(invocation -> {
            DeviceEntity entity = invocation.getArgument(0);
            entity.setId(1L);  // 修复：DeviceEntity的id是Long类型
            return 1;
        }).when(accessDeviceDao).insert(any(DeviceEntity.class));

        // When
        ResponseDTO<Long> result = accessDeviceServiceImpl.addDevice(addForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(accessDeviceDao, times(1)).insert(any(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test addDevice - Device Code Exists")
    void test_addDevice_DeviceCodeExists() {
        // Given
        AccessDeviceAddForm addForm = new AccessDeviceAddForm();
        addForm.setDeviceCode("EXISTING_DEV");
        when(accessDeviceDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockDevice); // Device code exists

        // When
        ResponseDTO<Long> result = accessDeviceServiceImpl.addDevice(addForm);

        // Then
        assertFalse(result.getOk());
        assertEquals(ResponseDTO.error("DEVICE_CODE_EXISTS", "x").getCode(), result.getCode());
        assertTrue(result.getMessage().contains("设备编号已存在"));
        verify(accessDeviceDao, never()).insert(any(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test updateDevice - Success Scenario")
    void test_updateDevice_Success() {
        // Given
        AccessDeviceUpdateForm updateForm = new AccessDeviceUpdateForm();
        updateForm.setDeviceId(1L);
        updateForm.setDeviceName("Updated Device");
        updateForm.setDeviceCode("DEV001");
        updateForm.setAreaId(1L);

        when(accessDeviceDao.selectById(anyLong())).thenReturn(mockDevice);
        when(gatewayServiceClient.callCommonService(anyString(), eq(HttpMethod.GET), isNull(), eq(AreaEntity.class)))
                .thenReturn(ResponseDTO.ok(mockArea));
        when(accessDeviceDao.updateById(any(DeviceEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Boolean> result = accessDeviceServiceImpl.updateDevice(updateForm);

        // Then
        assertTrue(result.getOk());
        assertTrue(result.getData());
        verify(accessDeviceDao, times(1)).selectById(anyLong());
        verify(accessDeviceDao, times(1)).updateById(any(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test updateDevice - Device Not Found")
    void test_updateDevice_NotFound() {
        // Given
        AccessDeviceUpdateForm updateForm = new AccessDeviceUpdateForm();
        updateForm.setDeviceId(999L);
        when(accessDeviceDao.selectById(anyLong())).thenReturn(null);

        // When
        ResponseDTO<Boolean> result = accessDeviceServiceImpl.updateDevice(updateForm);

        // Then
        assertFalse(result.getOk());
        assertEquals(ResponseDTO.error("DEVICE_NOT_FOUND", "x").getCode(), result.getCode());
        assertTrue(result.getMessage().contains("设备不存在"));
        verify(accessDeviceDao, never()).updateById(any(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test deleteDevice - Success Scenario")
    void test_deleteDevice_Success() {
        // Given
        Long deviceId = 1L;
        when(accessDeviceDao.selectById(anyLong())).thenReturn(mockDevice);
        when(accessDeviceDao.updateById(any(DeviceEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Boolean> result = accessDeviceServiceImpl.deleteDevice(deviceId);

        // Then
        assertTrue(result.getOk());
        assertTrue(result.getData());
        verify(accessDeviceDao, times(1)).selectById(anyLong());
        verify(accessDeviceDao, times(1)).updateById(any(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test deleteDevice - Device Not Found")
    void test_deleteDevice_NotFound() {
        // Given
        Long deviceId = 999L;
        when(accessDeviceDao.selectById(anyLong())).thenReturn(null);

        // When
        ResponseDTO<Boolean> result = accessDeviceServiceImpl.deleteDevice(deviceId);

        // Then
        assertFalse(result.getOk());
        assertEquals(ResponseDTO.error("DEVICE_NOT_FOUND", "x").getCode(), result.getCode());
        assertTrue(result.getMessage().contains("设备不存在"));
        verify(accessDeviceDao, never()).updateById(any(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test updateDeviceStatus - Success Scenario")
    void test_updateDeviceStatus_Success() {
        // Given
        Long deviceId = 1L;
        Integer status = 1;
        when(accessDeviceDao.selectById(anyLong())).thenReturn(mockDevice);
        when(accessDeviceDao.updateById(any(DeviceEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Boolean> result = accessDeviceServiceImpl.updateDeviceStatus(deviceId, status);

        // Then
        assertTrue(result.getOk());
        assertTrue(result.getData());
        verify(accessDeviceDao, times(1)).selectById(anyLong());
        verify(accessDeviceDao, times(1)).updateById(any(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test updateDeviceStatus - Device Not Found")
    void test_updateDeviceStatus_NotFound() {
        // Given
        Long deviceId = 999L;
        Integer status = 1;
        when(accessDeviceDao.selectById(anyLong())).thenReturn(null);

        // When
        ResponseDTO<Boolean> result = accessDeviceServiceImpl.updateDeviceStatus(deviceId, status);

        // Then
        assertFalse(result.getOk());
        assertEquals(ResponseDTO.error("DEVICE_NOT_FOUND", "x").getCode(), result.getCode());
        assertTrue(result.getMessage().contains("设备不存在"));
        verify(accessDeviceDao, never()).updateById(any(DeviceEntity.class));
    }

    @Test
    @DisplayName("Test getNearbyDevices - Success Scenario (Stub)")
    void test_getNearbyDevices_Success() {
        // Given
        Long userId = 1L;
        Double latitude = 39.9042;
        Double longitude = 116.4074;
        Integer radius = 1000;
        when(accessDeviceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        // When
        ResponseDTO<List<AccessMobileController.MobileDeviceItem>> result =
            accessDeviceServiceImpl.getNearbyDevices(userId, latitude, longitude, radius);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("Test getMobileUserPermissions - Success Scenario (Stub)")
    void test_getMobileUserPermissions_Success() {
        // Given
        Long userId = 1L;
        when(accessDeviceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        // When
        ResponseDTO<AccessMobileController.MobileUserPermissions> result =
            accessDeviceServiceImpl.getMobileUserPermissions(userId);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("Test getMobileRealTimeStatus - Success Scenario (Stub)")
    void test_getMobileRealTimeStatus_Success() {
        // Given
        Long deviceId = 1L;
        when(accessDeviceDao.selectById(deviceId)).thenReturn(mockDevice);
        when(gatewayServiceClient.callDeviceCommService(anyString(), eq(HttpMethod.GET), isNull(), eq(Integer.class)))
                .thenReturn(ResponseDTO.ok(3));

        // When
        ResponseDTO<AccessMobileController.MobileRealTimeStatus> result =
            accessDeviceServiceImpl.getMobileRealTimeStatus(deviceId);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().getOnlineCount());
    }

    @Test
    @DisplayName("Test getMobileAreas - Success Scenario (Stub)")
    void test_getMobileAreas_Success() {
        // Given
        Long userId = 1L;
        when(accessDeviceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        // When
        ResponseDTO<List<AccessMobileController.MobileAreaItem>> result =
            accessDeviceServiceImpl.getMobileAreas(userId);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
    }
}
