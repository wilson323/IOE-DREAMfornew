package net.lab1024.sa.video.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.service.VideoDeviceService;

/**
 * VideoDeviceController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：VideoDeviceController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VideoDeviceController单元测试")
class VideoDeviceControllerTest {
    @Mock
    private VideoDeviceService videoDeviceService;

    @InjectMocks
    private VideoDeviceController videoDeviceController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("queryDevices-成功场景-返回分页结果")
    void test_queryDevices_Success_ReturnsPageResult() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        String keyword = "摄像头";
        String areaId = "AREA001";
        Integer status = 1;

        PageResult<VideoDeviceVO> pageResult = new PageResult<>();
        when(videoDeviceService.queryDevices(any(VideoDeviceQueryForm.class)))
            .thenReturn(pageResult);

        // When
        ResponseDTO<PageResult<VideoDeviceVO>> response = videoDeviceController.queryDevices(
            pageNum, pageSize, keyword, areaId, status);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(videoDeviceService).queryDevices(any(VideoDeviceQueryForm.class));
    }
}
