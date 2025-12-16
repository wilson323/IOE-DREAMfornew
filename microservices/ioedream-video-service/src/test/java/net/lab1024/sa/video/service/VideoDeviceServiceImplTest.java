package net.lab1024.sa.video.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.service.impl.VideoDeviceServiceImpl;

/**
 * VideoDeviceServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：视频设备管理核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VideoDeviceServiceImpl单元测试")
@SuppressWarnings("unchecked")
class VideoDeviceServiceImplTest {

    @Mock
    private DeviceDao deviceDao;

    @InjectMocks
    private VideoDeviceServiceImpl videoDeviceService;

    private DeviceEntity mockDevice;
    private VideoDeviceQueryForm queryForm;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockDevice = new DeviceEntity();
        mockDevice.setId(1001L);
        mockDevice.setDeviceCode("CAM001");
        mockDevice.setDeviceName("摄像头001");
        mockDevice.setDeviceType("CAMERA");
        mockDevice.setAreaId(2001L);
        mockDevice.setIpAddress("192.168.1.100");
        mockDevice.setPort(554);
        mockDevice.setDeviceStatus("ONLINE");
        mockDevice.setEnabledFlag(1);
        mockDevice.setDeletedFlag(0);

        queryForm = new VideoDeviceQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(20);
    }

    @Test
    @DisplayName("测试分页查询设备-成功场景")
    void testQueryDevices_Success() {
        // Given
        List<DeviceEntity> deviceList = new ArrayList<>();
        deviceList.add(mockDevice);

        Page<DeviceEntity> pageResult = new Page<>(1, 20);
        pageResult.setRecords(deviceList);
        pageResult.setTotal(1);

        when(deviceDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // When
        PageResult<VideoDeviceVO> result = videoDeviceService.queryDevices(queryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("摄像头001", result.getList().get(0).getDeviceName());
        verify(deviceDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试分页查询设备-关键词搜索")
    void testQueryDevices_WithKeyword() {
        // Given
        queryForm.setKeyword("摄像头");

        List<DeviceEntity> deviceList = new ArrayList<>();
        deviceList.add(mockDevice);

        Page<DeviceEntity> pageResult = new Page<>(1, 20);
        pageResult.setRecords(deviceList);
        pageResult.setTotal(1);

        when(deviceDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // When
        PageResult<VideoDeviceVO> result = videoDeviceService.queryDevices(queryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(deviceDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试查询设备详情-成功场景")
    void testGetDeviceDetail_Success() {
        // Given
        when(deviceDao.selectById(1001L)).thenReturn(mockDevice);

        // When
        VideoDeviceVO result = videoDeviceService.getDeviceDetail(1001L);

        // Then
        assertNotNull(result);
        assertEquals(1001L, result.getDeviceId());
        assertEquals("摄像头001", result.getDeviceName());
        assertEquals(1, result.getDeviceStatus()); // ONLINE -> 1
        verify(deviceDao, times(1)).selectById(1001L);
    }

    @Test
    @DisplayName("测试查询设备详情-设备不存在")
    void testGetDeviceDetail_NotFound() {
        // Given
        when(deviceDao.selectById(9999L)).thenReturn(null);

        // When
        VideoDeviceVO result = videoDeviceService.getDeviceDetail(9999L);

        // Then
        assertNull(result);
        verify(deviceDao, times(1)).selectById(9999L);
    }

    @Test
    @DisplayName("测试查询设备详情-设备类型不匹配")
    void testGetDeviceDetail_WrongType() {
        // Given
        DeviceEntity nonCameraDevice = new DeviceEntity();
        nonCameraDevice.setId(1002L);
        nonCameraDevice.setDeviceType("ACCESS_CONTROL"); // 非摄像头设备

        when(deviceDao.selectById(1002L)).thenReturn(nonCameraDevice);

        // When
        VideoDeviceVO result = videoDeviceService.getDeviceDetail(1002L);

        // Then
        assertNull(result);
        verify(deviceDao, times(1)).selectById(1002L);
    }
}

