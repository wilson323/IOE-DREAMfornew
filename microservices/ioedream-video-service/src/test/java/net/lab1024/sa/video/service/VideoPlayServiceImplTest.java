package net.lab1024.sa.video.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.service.impl.VideoPlayServiceImpl;

/**
 * VideoPlayServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of VideoPlayServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VideoPlayServiceImpl Unit Test")
@SuppressWarnings("unchecked")
class VideoPlayServiceImplTest {

    @Mock
    private DeviceDao deviceDao;

    @Mock
    private VideoDeviceService videoDeviceService;

    @InjectMocks
    private VideoPlayServiceImpl videoPlayServiceImpl;

    private DeviceEntity mockDevice;
    private VideoDeviceVO mockVideoDeviceVO;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockDevice = new DeviceEntity();
        mockDevice.setDeviceId(1L);  // Long: 设备ID
        mockDevice.setDeviceCode("CAM001");
        mockDevice.setDeviceName("Test Camera");
        mockDevice.setDeviceType(1);  // Integer: CAMERA设备类型
        mockDevice.setIpAddress("192.168.1.100");
        mockDevice.setPort(554);
        mockDevice.setEnabled(1);
        mockDevice.setDeviceStatus(1);
        mockDevice.setDeletedFlag(0);

        mockVideoDeviceVO = new VideoDeviceVO();
        mockVideoDeviceVO.setDeviceId(1L);  // Long: VO的设备ID类型
        mockVideoDeviceVO.setDeviceName("Test Camera");
        mockVideoDeviceVO.setDeviceStatus(1);
    }

    @Test
    @DisplayName("Test getVideoStream - Success Scenario")
    void test_getVideoStream_Success() {
        // Given
        Long deviceId = 1L;
        Long channelId = 1L;
        String streamType = "MAIN";

        when(deviceDao.selectById(deviceId)).thenReturn(mockDevice);

        // When
        Map<String, Object> result = videoPlayServiceImpl.getVideoStream(deviceId, channelId, streamType);

        // Then
        assertNotNull(result);
        assertNotNull(result.get("streamUrl"));
        assertEquals(streamType, result.get("streamType"));
        assertEquals(deviceId, result.get("deviceId"));
        verify(deviceDao, times(1)).selectById(deviceId);
    }

    @Test
    @DisplayName("Test getVideoStream - Null DeviceId")
    void test_getVideoStream_NullDeviceId() {
        // When & Then
        ParamException exception = assertThrows(ParamException.class, () -> {
            videoPlayServiceImpl.getVideoStream(null, null, null);
        });
        assertTrue(exception.getMessage().contains("设备ID不能为空"));
        verify(deviceDao, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("Test getVideoStream - Device Not Found")
    void test_getVideoStream_DeviceNotFound() {
        // Given
        Long deviceId = 999L;
        when(deviceDao.selectById(deviceId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoPlayServiceImpl.getVideoStream(deviceId, null, null);
        });
        assertEquals("设备不存在", exception.getMessage());
        verify(deviceDao, times(1)).selectById(deviceId);
    }

    @Test
    @DisplayName("Test getVideoStream - Device Type Mismatch")
    void test_getVideoStream_DeviceTypeMismatch() {
        // Given
        Long deviceId = 1L;
        mockDevice.setDeviceType(2);  // Integer: 非摄像头设备类型
        when(deviceDao.selectById(deviceId)).thenReturn(mockDevice);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoPlayServiceImpl.getVideoStream(deviceId, null, null);
        });
        assertEquals("设备类型不匹配", exception.getMessage());
        verify(deviceDao, times(1)).selectById(deviceId);
    }

    @Test
    @DisplayName("Test getSnapshot - Success Scenario")
    void test_getSnapshot_Success() {
        // Given
        Long deviceId = 1L;
        Long channelId = 1L;

        when(deviceDao.selectById(deviceId)).thenReturn(mockDevice);

        // When
        Map<String, Object> result = videoPlayServiceImpl.getSnapshot(deviceId, channelId);

        // Then
        assertNotNull(result);
        assertNotNull(result.get("snapshotUrl"));
        assertEquals(deviceId, result.get("deviceId"));
        verify(deviceDao, times(1)).selectById(deviceId);
    }

    @Test
    @DisplayName("Test getSnapshot - Device Not Found")
    void test_getSnapshot_DeviceNotFound() {
        // Given
        Long deviceId = 999L;
        when(deviceDao.selectById(deviceId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoPlayServiceImpl.getSnapshot(deviceId, null);
        });
        assertEquals("设备不存在", exception.getMessage());
        verify(deviceDao, times(1)).selectById(deviceId);
    }

    @Test
    @DisplayName("Test getMobileDeviceList - Success Scenario")
    void test_getMobileDeviceList_Success() {
        // Given
        String areaId = "1";
        String deviceType = "CAMERA";
        Integer status = 1;

        List<DeviceEntity> deviceList = new ArrayList<>();
        deviceList.add(mockDevice);

        when(deviceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(deviceList);

        // When
        List<VideoDeviceVO> result = videoPlayServiceImpl.getMobileDeviceList(areaId, deviceType, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(deviceDao, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getMobileDeviceList - Empty Result")
    void test_getMobileDeviceList_Empty() {
        // Given
        when(deviceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // When
        List<VideoDeviceVO> result = videoPlayServiceImpl.getMobileDeviceList(null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(deviceDao, times(1)).selectList(any(LambdaQueryWrapper.class));
    }
}
