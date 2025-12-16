package net.lab1024.sa.video.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.service.VideoPlayService;

/**
 * VideoPlayController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：VideoPlayController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VideoPlayController单元测试")
class VideoPlayControllerTest {
    @Mock
    private VideoPlayService videoPlayService;

    @InjectMocks
    private VideoPlayController videoPlayController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("getVideoStream-成功场景-返回视频流地址")
    void test_getVideoStream_Success_ReturnsStreamUrl() {
        // Given
        Long deviceId = 1L;
        Long channelId = 1L;
        String streamType = "main";

        Map<String, Object> streamInfo = new HashMap<>();
        streamInfo.put("streamUrl", "rtsp://example.com/stream");
        when(videoPlayService.getVideoStream(anyLong(), any(), anyString()))
            .thenReturn(streamInfo);

        // When
        ResponseDTO<Map<String, Object>> response = videoPlayController.getVideoStream(
            deviceId, channelId, streamType);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        verify(videoPlayService).getVideoStream(eq(deviceId), eq(channelId), eq(streamType));
    }
}
