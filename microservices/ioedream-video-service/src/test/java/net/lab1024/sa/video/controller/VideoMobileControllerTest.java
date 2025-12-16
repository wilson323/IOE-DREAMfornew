package net.lab1024.sa.video.controller;

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

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.service.VideoPlayService;

/**
 * VideoMobileController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：VideoMobileController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VideoMobileController单元测试")
class VideoMobileControllerTest {
    @Mock
    private VideoPlayService videoPlayService;
    
    @InjectMocks
    private VideoMobileController videoMobileController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("getDeviceList-成功场景-返回设备列表")
    void test_getDeviceList_Success_ReturnsDeviceList() {
        // Given
        String areaId = "AREA001";
        String deviceType = "CAMERA";
        Integer status = 1;
        
        List<VideoDeviceVO> deviceList = new ArrayList<>();
        when(videoPlayService.getMobileDeviceList(anyString(), anyString(), anyInt()))
            .thenReturn(deviceList);

        // When
        ResponseDTO<List<VideoDeviceVO>> response = videoMobileController.getDeviceList(
            areaId, deviceType, status);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        verify(videoPlayService).getMobileDeviceList(eq(areaId), eq(deviceType), eq(status));
    }
}
