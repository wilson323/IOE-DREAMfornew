package net.lab1024.sa.video.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.video.dao.MonitorEventDao;
import net.lab1024.sa.video.dao.VideoDeviceDao;
import net.lab1024.sa.video.dao.VideoRecordDao;
import net.lab1024.sa.video.domain.entity.MonitorEventEntity;
import net.lab1024.sa.video.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.video.domain.entity.VideoRecordEntity;
import net.lab1024.sa.video.manager.VideoCacheManager;

/**
 * 视频监控服务实现类单元测试
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("视频监控服务单元测试")
class VideoSurveillanceServiceImplTest {

    @Mock
    private VideoDeviceDao videoDeviceDao;

    @Mock
    private VideoRecordDao videoRecordDao;

    @Mock
    private MonitorEventDao monitorEventDao;

    @Mock
    private VideoCacheManager videoCacheManager;

    @InjectMocks
    private VideoSurveillanceServiceImpl videoSurveillanceService;

    private VideoDeviceEntity testDevice;
    private VideoRecordEntity testRecord;

    @BeforeEach
    void setUp() {
        // 初始化测试设备
        testDevice = new VideoDeviceEntity();
        testDevice.setDeviceId(1L);
        testDevice.setDeviceCode("VID001");
        testDevice.setDeviceName("测试摄像头");
        testDevice.setDeviceStatus("ONLINE");
        testDevice.setDeviceIp("192.168.1.100");
        testDevice.setDevicePort(8000);
        testDevice.setPtzEnabled(1);
        testDevice.setRtspUrl("rtsp://192.168.1.100:554/stream");

        // 初始化测试录像记录
        testRecord = new VideoRecordEntity();
        testRecord.setRecordId(1L);
        testRecord.setDeviceId(1L);
        testRecord.setRecordStartTime(LocalDateTime.now());
        testRecord.setRecordStatus("RECORDING");
    }

    @Test
    @DisplayName("测试PTZ控制 - 成功场景")
    void testPtzControl_Success() {
        // Given
        when(videoDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(monitorEventDao.insert(any(MonitorEventEntity.class))).thenReturn(1);

        // When
        boolean result = videoSurveillanceService.ptzControl(1L, "UP", 5);

        // Then
        assertTrue(result);
        verify(videoDeviceDao, times(1)).selectById(1L);
        verify(monitorEventDao, times(1)).insert(any(MonitorEventEntity.class));
    }

    @Test
    @DisplayName("测试PTZ控制 - 设备不存在")
    void testPtzControl_DeviceNotFound() {
        // Given
        when(videoDeviceDao.selectById(1L)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            videoSurveillanceService.ptzControl(1L, "UP", 5);
        });
        verify(videoDeviceDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("测试PTZ控制 - 设备不支持云台")
    void testPtzControl_PtzNotEnabled() {
        // Given
        testDevice.setPtzEnabled(0);
        when(videoDeviceDao.selectById(1L)).thenReturn(testDevice);

        // When
        boolean result = videoSurveillanceService.ptzControl(1L, "UP", 5);

        // Then
        assertFalse(result);
        verify(videoDeviceDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("测试获取设备截图")
    void testGetDeviceSnapshot() {
        // Given
        when(videoDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(monitorEventDao.insert(any(MonitorEventEntity.class))).thenReturn(1);

        // When
        String result = videoSurveillanceService.getDeviceSnapshot(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("device_"));
        assertTrue(result.contains(".jpg"));
        verify(videoDeviceDao, times(1)).selectById(1L);
        verify(monitorEventDao, times(1)).insert(any(MonitorEventEntity.class));
    }

    @Test
    @DisplayName("测试获取录像统计")
    void testGetRecordingStats() {
        // Given
        List<VideoRecordEntity> records = new ArrayList<>();
        records.add(testRecord);
        when(videoRecordDao.selectByTimeRange(any(), any(), isNull()))
                .thenReturn(records);

        // When
        Object result = videoSurveillanceService.getRecordingStats(1L, "2025-01-01", "2025-01-31");

        // Then
        assertNotNull(result);
        verify(videoRecordDao, times(1)).selectByTimeRange(any(), any(), isNull());
    }

    @Test
    @DisplayName("测试开始录像")
    void testStartRecording() {
        // Given
        when(videoRecordDao.insert(any(VideoRecordEntity.class))).thenAnswer(invocation -> {
            VideoRecordEntity record = invocation.getArgument(0);
            record.setRecordId(1L);
            return 1;
        });

        // When
        Long result = videoSurveillanceService.startRecording(1L);

        // Then
        assertNotNull(result);
        verify(videoRecordDao, times(1)).insert(any(VideoRecordEntity.class));
    }

    @Test
    @DisplayName("测试停止录像")
    void testStopRecording() {
        // Given
        when(videoRecordDao.updateById(any(VideoRecordEntity.class))).thenReturn(1);

        // When
        boolean result = videoSurveillanceService.stopRecording(1L);

        // Then
        assertTrue(result);
        verify(videoRecordDao, times(1)).updateById(any(VideoRecordEntity.class));
    }
}
